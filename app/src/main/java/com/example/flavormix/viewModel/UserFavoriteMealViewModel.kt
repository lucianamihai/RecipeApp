package com.example.flavormix.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavormix.model.Meal
import com.example.flavormix.model.UserFavoriteMeal
import com.example.flavormix.repository.UserFavoriteMealRepository
import kotlinx.coroutines.launch

class UserFavoriteMealViewModel(private val repository: UserFavoriteMealRepository) : ViewModel() {

    private val _favoriteMealsLiveData = MutableLiveData<List<Meal>>()
    val favoriteMealsLiveData: LiveData<List<Meal>> get() = _favoriteMealsLiveData


    fun insertUserFavoriteMeal(userFavoriteMeal: UserFavoriteMeal, meal: Meal) = viewModelScope.launch {
        repository.addFavoriteMeal(userFavoriteMeal, meal)
    }

    fun deleteUserFavoriteMeal(userFavoriteMeal: UserFavoriteMeal) = viewModelScope.launch {
        repository.removeFavoriteMeal(userFavoriteMeal.userId, userFavoriteMeal.mealId)
    }

    suspend fun getFavoriteMealsByUser(userId: Int): List<Meal> {
        val mealIds = repository.getFavoriteMealIds(userId)
        return repository.getMealsByIds(mealIds)
    }
    fun getUserFavoriteMeals(userId: Int) {
        viewModelScope.launch {
            val favoriteMealIds = repository.getFavoriteMealIds(userId)
            if (favoriteMealIds.isNotEmpty()) {
                val meals = repository.getMealsByIds(favoriteMealIds)
                _favoriteMealsLiveData.postValue(meals)
            } else {
                _favoriteMealsLiveData.postValue(emptyList())
            }
        }
    }
}
