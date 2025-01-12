package com.example.flavormix.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.flavormix.model.Ingredient
import com.example.flavormix.model.User
import com.example.flavormix.model.Meal
import com.example.flavormix.model.UserFavoriteMeal

@Database(entities = [User::class, Meal::class, UserFavoriteMeal::class, Ingredient::class], version = 5, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun mealDao(): MealDao
    abstract fun userFavoriteMealDao(): UserFavoriteMealDao
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
