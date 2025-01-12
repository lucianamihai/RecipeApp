package com.example.flavormix.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.flavormix.model.Ingredient
import androidx.room.*

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients WHERE userId = :userId")
    fun getIngredientsByUserId(userId: Int): LiveData<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE userId = :userId")
    suspend fun loadAllUsersIngredients(userId: Int): List<Ingredient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Query("INSERT INTO ingredients (userId, name) VALUES ( :userId, :name)")
    suspend fun insertIngredientManually( userId: Int, name: String)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)
}
