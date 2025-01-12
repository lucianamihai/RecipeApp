package com.example.flavormix.viewModel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.flavormix.db.UserDatabase
import com.example.flavormix.model.Ingredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class ShoppingCartViewModel(application: Application) : AndroidViewModel(application) {
    private val ingredientDao = UserDatabase.getInstance(application).ingredientDao()
    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val userId = sharedPreferences.getInt("user_id", -1)

    val ingredients: LiveData<List<Ingredient>> = ingredientDao.getIngredientsByUserId(userId)

    fun addIngredient(name: String) {
        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) {
            showToast("Please enter an ingredient")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                ingredientDao.insertIngredientManually(userId, trimmedName)
                withContext(Dispatchers.Main) {
                    showToast("Ingredient added: $trimmedName")
                    Log.d("ShoppingCartViewModel", "Ingredient inserted successfully")
                }
            } catch (e: Exception) {
                Log.e("ShoppingCartViewModel", "Error inserting ingredient", e)
            }
        }
    }

    fun deleteIngredient(ingredient: Ingredient) = viewModelScope.launch(Dispatchers.IO) {
        try {
            ingredientDao.deleteIngredient(ingredient)
            withContext(Dispatchers.Main) {
                showToast("Ingredient deleted: ${ingredient.name}")
            }
        } catch (e: Exception) {
            Log.e("ShoppingCartViewModel", "Error deleting ingredient", e)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}
