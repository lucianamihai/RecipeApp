package com.example.flavormix.activities

import UserViewModelFactory
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.flavormix.R
import com.example.flavormix.db.UserDatabase
import com.example.flavormix.repository.UserRepository
import com.example.flavormix.viewModel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private val viewModel: UserViewModel by viewModels {
        val database = UserDatabase.getInstance(this)
        UserViewModelFactory(UserRepository(database.userDao(), database.userFavoriteMealDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            Log.d("LoginActivity", "Email: $email, Password: $password")

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    viewModel.getUserByEmail(email)
                }
                if (user != null) {
                    if (user.password == password) {
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putInt("user_id", user.id)
                        editor.apply()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("email", user.email)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Incorrect password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
