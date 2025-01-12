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
    }
}
