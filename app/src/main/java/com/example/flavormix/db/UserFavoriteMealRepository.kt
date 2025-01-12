package com.example.flavormix.repository

import androidx.lifecycle.LiveData
import com.example.flavormix.db.MealDao
import com.example.flavormix.db.UserFavoriteMealDao
import com.example.flavormix.model.Meal
import com.example.flavormix.model.UserFavoriteMeal

class UserFavoriteMealRepository(private val userFavoriteMealDao: UserFavoriteMealDao, private val mealDao: MealDao) {

    suspend fun addFavoriteMeal(userFavoriteMeal: UserFavoriteMeal, meal: Meal) {
        mealDao.upsertMeal(meal)

        userFavoriteMealDao.insertUserFavoriteMeal(userFavoriteMeal)
    }

    suspend fun removeFavoriteMeal(userId: Int, mealId: String) {
        userFavoriteMealDao.deleteUserFavoriteMeal(userId, mealId)
    }

    suspend fun getFavoriteMealIds(userId: Int): List<String> {
        return userFavoriteMealDao.getAllFavoriteMealIds(userId)
    }

    suspend fun getMealsByIds(mealIds: List<String>): List<Meal> {
        return mealDao.getMealsByIds(mealIds)
    }

}
