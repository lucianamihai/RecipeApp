package com.example.flavormix.activities

import UserViewModelFactory
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flavormix.R
import com.example.flavormix.db.UserDatabase
import com.example.flavormix.model.Meal
import com.example.flavormix.model.UserFavoriteMeal
import com.example.flavormix.repository.UserRepository
import com.example.flavormix.viewModel.UserViewModel

class MealDetailActivity : AppCompatActivity() {

    private lateinit var btnFavorite: Button
    private val userId = getUserId() // Assume this is obtained from the logged-in user info

    private val userViewModel: UserViewModel by viewModels {
        val userDao = UserDatabase.getInstance(this).userDao()
        val userFavoriteMealDao = UserDatabase.getInstance(this).userFavoriteMealDao()
        UserViewModelFactory(UserRepository(userDao, userFavoriteMealDao))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal)

        btnFavorite = findViewById(R.id.fabFavorites)
        val meal = intent.getParcelableExtra<Meal>("meal")

        btnFavorite.setOnClickListener {
            meal?.let {
                val favoriteMeal = UserFavoriteMeal(userId = userId, mealId = it.idMeal)
                userViewModel.insertUserFavoriteMeal(favoriteMeal)
            }
        }
    }
    private fun getUserId(): Int {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)  // Returnează -1 dacă nu există
    }

}
