package com.example.flavormix.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flavormix.db.MealDatabase
import com.example.flavormix.repository.UserFavoriteMealRepository

class HomeViewModelFactory(
    private val mealDatabase: MealDatabase,
    private val userFavoriteMealRepository: UserFavoriteMealRepository

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(mealDatabase, userFavoriteMealRepository) as T
    }
}
