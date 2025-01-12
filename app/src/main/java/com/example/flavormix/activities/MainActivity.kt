package com.example.flavormix.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.flavormix.R
import com.example.flavormix.databinding.ActivityMainBinding
import com.example.flavormix.db.MealDatabase
import com.example.flavormix.repository.UserFavoriteMealRepository
import com.example.flavormix.viewModel.HomeViewModel
import com.example.flavormix.viewModel.HomeViewModelFactory

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import com.example.flavormix.NotificationReceiver
import java.util.Calendar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.widget.Toast

import android.Manifest
import android.content.pm.PackageManager
import com.example.flavormix.AlarmScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    val viewModel: HomeViewModel by lazy {
        val mealDatabase = MealDatabase.getInstance(this)
        val userFavoriteMealRepository = UserFavoriteMealRepository(
            mealDatabase.userFavoriteMealDao(),
            mealDatabase.mealDao()
        )
        val homeViewModelFactory = HomeViewModelFactory(mealDatabase, userFavoriteMealRepository)
        ViewModelProvider(this, homeViewModelFactory)[HomeViewModel::class.java]
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            AlarmScheduler.scheduleNotifications(this)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setting up navigation through Bottom Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.btmNav, navController)

        // Setting up the button and its click listener
        val profileButton = findViewById<Button>(R.id.profile_button)
        profileButton?.setOnClickListener {
            val email = intent.getStringExtra("email")
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("email", email)
            }
            startActivity(intent)
        }
        checkAndRequestNotificationPermission()

    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 și mai nou
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permisiunea este deja acordată
                    AlarmScheduler.scheduleNotifications(this)
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(
                        this,
                        "Această aplicație are nevoie de permisiunea pentru a trimite notificări.",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            AlarmScheduler.scheduleNotifications(this)
        }
    }


}
