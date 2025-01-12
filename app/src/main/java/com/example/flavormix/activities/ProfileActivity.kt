package com.example.flavormix.activities

import UserViewModelFactory
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.flavormix.R
import com.example.flavormix.db.UserDatabase
import com.example.flavormix.repository.UserRepository
import com.example.flavormix.viewModel.UserViewModel
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: CircleImageView
    private lateinit var tvFirstName: TextView
    private lateinit var tvLastName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnLogout: Button

    private val viewModel: UserViewModel by viewModels {
        val database = UserDatabase.getInstance(this)
        UserViewModelFactory(UserRepository(database.userDao(), database.userFavoriteMealDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imgProfile = findViewById(R.id.imgProfile)
        tvFirstName = findViewById(R.id.tvFirstName)
        tvLastName = findViewById(R.id.tvLastName)
        tvEmail = findViewById(R.id.tvEmail)
        btnLogout = findViewById(R.id.btnLogout)

        // Load user data
        loadUserData()

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserData() {
        val email = intent.getStringExtra("email")
        if (email != null) {
            viewModel.loadUserByEmail(email)
            viewModel.user.observe(this, Observer { user ->
                user?.let {
                    tvFirstName.text = it.firstName
                    tvLastName.text = it.lastName
                    tvEmail.text = it.email
                    imgProfile.setImageResource(R.drawable.ic_profile)
                }
            })
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
