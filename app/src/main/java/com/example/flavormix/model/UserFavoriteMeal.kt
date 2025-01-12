package com.example.flavormix.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserFavoriteMeal")
data class UserFavoriteMeal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val mealId: String
)
