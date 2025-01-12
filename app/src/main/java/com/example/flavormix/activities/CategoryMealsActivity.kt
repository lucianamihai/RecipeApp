package com.example.flavormix.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.flavormix.adapters.CategoryMealsAdapter
import com.example.flavormix.databinding.ActivityCategoryMealsBinding
import com.example.flavormix.fragments.HomeFragment
import com.example.flavormix.viewModel.CategoryMealsViewModel

class CategoryMealsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryMealsBinding
    private lateinit var categoryMealsMvvm: CategoryMealsViewModel

    // Adapter declaration
    private lateinit var categoryMealsAdapter: CategoryMealsAdapter

    // To store the name of category received from HomeFragment
    private lateinit var categoryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryMealsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Starting shimmer effect - Hidden in observeMealsByCategoryLiveData() in this activity
        binding.shimmerLayoutMeals.startShimmer()

        // Preparing Recycler view to show data
        prepareRecyclerView()

        // Initialising CategoryMealsViewModel instance
        categoryMealsMvvm = ViewModelProvider(this)[CategoryMealsViewModel::class.java]

        getCategoryNameFromIntent()
        categoryMealsMvvm.getMealsByCategory(categoryName)

        observeMealsByCategoryLiveData()

        onCategoryMealItemClick()
    }

    // Preparing Recycler View
    private fun prepareRecyclerView() {
        // Initialising adapter
        categoryMealsAdapter = CategoryMealsAdapter()

        binding.rvMeals.apply {
            layoutManager = GridLayoutManager(context, 1,
                GridLayoutManager.VERTICAL, false)
            adapter = categoryMealsAdapter
        }
    }

    private fun getCategoryNameFromIntent() {
        val intent = intent
        categoryName = intent.getStringExtra(HomeFragment.CATEGORY_NAME)!!
    }

    private fun observeMealsByCategoryLiveData() {
        categoryMealsMvvm.observeMealsLiveData().observe(this, Observer { mealsList ->
            categoryMealsAdapter.setMealsList(mealsList)

            binding.tvCategoryCount.text = categoryName

            Handler(Looper.getMainLooper()).postDelayed({
                // Hide shimmer effect
                binding.shimmerLayoutMeals.stopShimmer()
                binding.shimmerLayoutMeals.visibility = View.GONE
                // Make the view visible
                binding.mealsMain.visibility = View.VISIBLE
            }, 100)
        })
    }

    // Function to navigate to MealActivity from category meal item list onClick
    private fun onCategoryMealItemClick() {
        categoryMealsAdapter.onItemClick = { meal ->
            val intent = Intent(this, MealActivity::class.java)
            intent.apply {
                putExtra(HomeFragment.MEAL_ID, meal.idMeal)
                putExtra(HomeFragment.MEAL_NAME, meal.strMeal)
                putExtra(HomeFragment.MEAL_THUMB, meal.strMealThumb)

                startActivity(intent)
            }
        }
    }
}
