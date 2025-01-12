package com.example.flavormix.activities

import UserViewModelFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flavormix.R
import com.example.flavormix.db.UserDatabase
import com.example.flavormix.model.User
import com.example.flavormix.repository.UserRepository
import com.example.flavormix.viewModel.UserViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var registerButton: Button

    private val viewModel: UserViewModel by viewModels {
        val database = UserDatabase.getInstance(this)
        UserViewModelFactory(UserRepository(database.userDao(), database.userFavoriteMealDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firstNameInput = findViewById(R.id.register_first_name_input)
        lastNameInput = findViewById(R.id.register_last_name_input)
        emailInput = findViewById(R.id.register_email_input)
        passwordInput = findViewById(R.id.register_password_input)
        confirmPasswordInput = findViewById(R.id.register_confirm_password_input)
        registerButton = findViewById(R.id.register_button)

        registerButton.setOnClickListener {
            val firstName = firstNameInput.text.toString()
            val lastName = lastNameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (password == confirmPassword) {
                val user = User(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )
                Log.d("RegisterActivity", "Attempting to register user: $user")
                viewModel.upsertUser(user)
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
