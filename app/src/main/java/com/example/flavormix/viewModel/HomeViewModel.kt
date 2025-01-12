package com.example.flavormix.viewModel

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flavormix.databinding.FragmentHomeBinding
import com.example.flavormix.db.MealDatabase
import com.example.flavormix.model.Category
import com.example.flavormix.model.CategoryList
import com.example.flavormix.model.Meal
import com.example.flavormix.model.MealList
import com.example.flavormix.model.UserFavoriteMeal
import com.example.flavormix.retrofit.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.flavormix.repository.UserFavoriteMealRepository
import kotlinx.coroutines.Dispatchers

class HomeViewModel(
    private val mealDatabase: MealDatabase,
    private val userFavoriteMealRepository: UserFavoriteMealRepository
) : ViewModel() {
    // To save the state of randomMeal on configuration change
    private var saveStateRandomMeal: Meal? = null
    private val _favoriteMealsLiveData = MutableLiveData<List<Meal>>()
    val favoriteMealsLiveData: LiveData<List<Meal>> get() = _favoriteMealsLiveData
    fun getFavoriteMealsForUser(userId: Int) {
        viewModelScope.launch {
            // Obține ID-urile meselor favorite ale utilizatorului
            val mealIds = mealDatabase.userFavoriteMealDao().getAllFavoriteMealIds(userId)

            if (mealIds.isEmpty()) {
                _favoriteMealsLiveData.postValue(emptyList())
                return@launch
            }

            // Obține mesele favorite din baza de date `mealInformation`
            val meals = mealDatabase.mealDao().getMealsByIds(mealIds)
            _favoriteMealsLiveData.postValue(meals)
        }
    }
    //    fun getUserFavoriteMeals(userId: Int): LiveData<List<Meal>> {
//        return userFavoriteMealRepository.getUserFavoriteMeals(userId)
//    }
    // Setting up a LiveData variable to store the random meal we get from api
    private var randomMealLiveData = MutableLiveData<Meal>()

    // Setting up a LiveData variable to store the category info we get from api
    private var categoriesLiveData = MutableLiveData<List<Category>>()

    // Livedata to store favorites meal
    private var favoritesMealsLiveData: LiveData<List<Meal>> = mealDatabase.mealDao().getAllMeals()

    // Livedata Meal for bottom sheet
    private var bottomSheetMealLiveData = MutableLiveData<Meal>()

    // Livedata MealList for search query
    private var searchedMealLiveData = MutableLiveData<List<Meal>>()



    // Setting up a observe fun on randomMealLiveData to observe it from the HomeFragment
    // It returns a LiveData so it cannot be changed from HomeFragment
    fun observeRandomMealLiveData(): LiveData<Meal> {
        return randomMealLiveData
    }

    // Setting up a observe fun on categoriesLiveData to observe it from the categoriesMealAdapter
    fun observeCategoriesLiveData(): LiveData<List<Category>> {
        return categoriesLiveData
    }

    // Observe favoritesMealsLiveData from outside
    fun observeFavoritesMealsLiveData(): LiveData<List<Meal>> {
        return favoritesMealsLiveData
    }

    // Observe BottomSheetMealLiveData from MealBottomSheetFragment
    fun observeBottomSheetMealLiveData(): LiveData<Meal>{
        return bottomSheetMealLiveData
    }

    // Observe Searched meal live data from Home
    fun observeSearchedMealLiveData(): LiveData<List<Meal>>{
        return searchedMealLiveData
    }



    // Getting a random meal from API
    fun getRandomMeal(binding : FragmentHomeBinding) {

        // Checking for any saved state
        saveStateRandomMeal?.let {randomMeal ->
            randomMealLiveData.postValue(randomMeal)

            // Hide shimmer effect
            binding.shimmerLayoutHome.stopShimmer()
            binding.shimmerLayoutHome.visibility = View.GONE
            // Make the view visible again
            binding.homeMain.visibility = View.VISIBLE

            return
        }

        // Calling the api for a Random Meal using the instance of Retrofit
        // through the getRandomMeal method of api instance of MealsApi Class
        RetrofitInstance.api.getRandomMeal().enqueue(object : Callback<MealList> {
            // -> enqueue method is a retrofit library method used for synchronous calling
            // using the callback process

            // onResponse and onFailure method are overridden to handle all the cases
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {





                // Checking if onResponse has data
                if (response.body() != null) { // -> We have proper a response
                    val randomMeal: Meal = response.body()!!.meals[0]
                    // Log.d("TEST", "meal id: ${randomMeal.idMeal}, name: ${randomMeal.strMeal}")
                    randomMealLiveData.value = randomMeal

//                    Handler(Looper.getMainLooper()).postDelayed({
//                        //Do something after 100ms
//                        // Hide shimmer effect
//                        binding.shimmerLayoutHome.stopShimmer()
//                        binding.shimmerLayoutHome.visibility = View.GONE
//                        // Make the view visible again
//                        binding.homeMain.visibility = View.VISIBLE
//                    }, 2000)

                    // saving randomMeal in saveState
                    saveStateRandomMeal = randomMeal

                } else { // -> We did not receive a proper response
                    return
                }
            }

            // In case the retrofit call fails due to any reason
            override fun onFailure(call: Call<MealList>, t: Throwable) {
                // Log will give us the reason for Failure through t.message
                Log.d("TEST", t.message.toString())
            }
        })
    }




    // Fetching category list from API
    fun getCategories() {
        // Again check up (just an api call through retrofit)
        RetrofitInstance.api.getCategories().enqueue(object : Callback<CategoryList> {
            override fun onResponse(call: Call<CategoryList>, response: Response<CategoryList>) {
                if (response.body() != null) {
                    categoriesLiveData.value = response.body()!!.categories
                } else {
                    return
                }
            }

            override fun onFailure(call: Call<CategoryList>, t: Throwable) {
                Log.d("TEST", t.message.toString())
            }
        })
    }

    // Room functions
    fun insertMeal(meal: Meal){
        // Launching under viewModel scope of coroutine
        viewModelScope.launch {
            mealDatabase.mealDao().upsertMeal(meal)
        }
    }
    // To delete meal from Room database
    fun deleteMeal(meal: Meal){
        viewModelScope.launch {
            mealDatabase.mealDao().deleteMeal(meal)
        }
    }

    fun deleteMeal(userFavoriteMeal: UserFavoriteMeal) = viewModelScope.launch(Dispatchers.IO) {
        userFavoriteMealRepository.removeFavoriteMeal(userFavoriteMeal.userId, userFavoriteMeal.mealId)
        val favoriteMealIds = userFavoriteMealRepository.getFavoriteMealIds(userFavoriteMeal.userId)
        val meals = userFavoriteMealRepository.getMealsByIds(favoriteMealIds)
        _favoriteMealsLiveData.postValue(meals)  // Actualizează lista de UI
    }


    // Function to get meal details by its id for Bottom Sheet
    fun getMealById(id: String) {
        RetrofitInstance.api.getMealDetailsById(id).enqueue(object : Callback<MealList>{
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                val meal = response.body()?.meals?.first()
                meal?.let {
                    bottomSheetMealLiveData.postValue(it)
                }
            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.d("TEST", t.message.toString())
            }

        })
    }

    // Searching meals by query
    fun searchMeals(searchQuery: String?){
        RetrofitInstance.api.searchMeals(searchQuery!!).enqueue(object: Callback<MealList>{
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                val mealsList = response.body()?.meals
                mealsList?.let {
                    searchedMealLiveData.postValue(it)
                }
            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.d("TEST", t.message.toString())
            }

        })

    }


}