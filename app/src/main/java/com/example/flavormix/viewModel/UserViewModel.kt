package com.example.flavormix.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavormix.model.Meal
import com.example.flavormix.model.User
import com.example.flavormix.model.UserFavoriteMeal
import com.example.flavormix.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    suspend fun getUserByEmail(email: String): User? {
        return repository.getUserByEmail(email)
    }


    fun loadUserByEmail(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            _user.postValue(user)
        }
    }

    fun upsertUser(user: User) = viewModelScope.launch {
        repository.upsertUser(user)
    }

    fun insertUserFavoriteMeal(userFavoriteMeal: UserFavoriteMeal) = viewModelScope.launch {
        repository.insertUserFavoriteMeal(userFavoriteMeal)
    }

    fun deleteUserFavoriteMeal(userId: Int, mealId: String) = viewModelScope.launch {
        repository.deleteUserFavoriteMeal(userId, mealId)
    }

    fun getUserFavoriteMeals(userId: Int): LiveData<List<UserFavoriteMeal>> {
        return repository.getUserFavoriteMeals(userId)
    }

    suspend fun getUserFavoriteMeal(userId: Int, mealId: String): UserFavoriteMeal? {
        return repository.getUserFavoriteMeal(userId, mealId)
    }

}
