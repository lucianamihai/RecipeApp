package com.example.flavormix.retrofit

import com.example.flavormix.model.CategoryList
import com.example.flavormix.model.MealsByCategoryList
import com.example.flavormix.model.MealList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApi {

    @GET("random.php")  // -> Get a Random Meal (from HomeViewModel)
    fun getRandomMeal() : Call<MealList>

    @GET("lookup.php?")  // -> Get a meal detail by Id (from MealViewModel)
    fun getMealDetailsById(@Query("i") id:String) : Call<MealList>

    @GET("categories.php")  // -> Get Category List (from HomeViewModel)
    fun getCategories() : Call<CategoryList>

    @GET("filter.php")
    fun getMealsByCategory(@Query("c") category: String) : Call<MealsByCategoryList>

    @GET("search.php")
    fun searchMeals(@Query("s") searchQuery: String): Call<MealList>

}