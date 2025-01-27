package com.example.flavormix.viewModel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.flavormix.db.UserDatabase
import com.example.flavormix.model.Ingredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShoppingCartViewModel(application: Application) : AndroidViewModel(application) {
    private val ingredientDao = UserDatabase.getInstance(application).ingredientDao()
    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val userId = sharedPreferences.getInt("user_id", -1)

    init {
        Log.d("ShoppingCartViewModel", "Initialized with userId: $userId")
    }

    val ingredients: LiveData<List<Ingredient>> = ingredientDao.getIngredientsByUserId(userId)

    var imageUri: Uri? = null
        private set

    fun setImageUri(uri: Uri) {
        imageUri = uri
    }

    fun createImageUri(): Uri? {
        val context = getApplication<Application>().applicationContext
        return try {
            val imageCollection =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                else
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val imageDetails = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "ingredient_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            context.contentResolver.insert(imageCollection, imageDetails)
        } catch (e: Exception) {
            Log.e("ShoppingCartViewModel", "Eroare la crearea URI-ului imaginii: ${e.message}")
            null
        }
    }

    fun addIngredient(name: String) {
        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) {
            showToast("Please enter an ingredient")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ingredient = Ingredient(name = trimmedName, userId = userId)
                ingredientDao.insertIngredient(ingredient)
                Log.d("ShoppingCartViewModel", "Inserted ingredient: $trimmedName for userId: $userId")
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
            Log.d("ShoppingCartViewModel", "Deleted ingredient: ${ingredient.name}")
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
