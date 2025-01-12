package com.example.flavormix.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.flavormix.model.Meal
import com.example.flavormix.model.UserFavoriteMeal

@Database(entities = [Meal::class, UserFavoriteMeal::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MealDatabase : RoomDatabase() {

    abstract fun mealDao(): MealDao
    abstract fun userFavoriteMealDao(): UserFavoriteMealDao  // Adaugă această linie

    companion object {
        @Volatile
        var INSTANCE: MealDatabase? = null

        @Synchronized
        fun getInstance(context: Context): MealDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    MealDatabase::class.java,
                    "meal.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE as MealDatabase
        }
    }
}
