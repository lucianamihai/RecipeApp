package com.example.flavormix.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.flavormix.R
import com.example.flavormix.activities.CategoryMealsActivity
import com.example.flavormix.activities.MainActivity
import com.example.flavormix.activities.MealActivity
import com.example.flavormix.adapters.CategoriesAdapter
import com.example.flavormix.databinding.FragmentHomeBinding
import com.example.flavormix.model.Meal
import com.example.flavormix.model.Dataa
import com.example.flavormix.model.dataa
import com.example.flavormix.viewModel.HomeViewModel
import kotlin.random.Random

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel

    // randomMeal to store the data of Random Meal appearing on Top of page
    private lateinit var randomMeal: Meal

    // Adapter variable for categories recycler view
    private lateinit var categoriesAdapter: CategoriesAdapter

    companion object {  // -> Constants for sending meal data through intent
        // For MealActivity
        const val MEAL_ID = "com.example.easyfood.fragments.idMeal"
        const val MEAL_NAME = "com.example.easyfood.fragments.nameMeal"
        const val MEAL_THUMB = "com.example.easyfood.fragments.thumbMeal"
        const val MEAL_AREA = "com.example.easyfood.fragments.areaMeal"
        // For CategoryMealActivity
        const val CATEGORY_NAME = "com.example.easyfood.fragments.categoryName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initializing the instance of HomeViewModel from MainActivity
        viewModel = (activity as MainActivity).viewModel


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Starting shimmer effect
        binding.shimmerLayoutHome.startShimmer()
        // Stops in HomeViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoriesAdapter = CategoriesAdapter()

        prepareCategoriesRecyclerView()  // -> Setting up recycler view to show categories

        // Calling the getRandomMeal method of HomeViewModel
        viewModel.getRandomMeal(binding)
        observeRandomMealLiveData()  // -> Setting up observer on the randomMeal
        onRandomMealClick()  // -> Setting up onClick Function on Random Card

//        // Calling getCategories method to set categoriesList in ViewModel
        viewModel.getCategories()
        observeCategoriesLiveData()  // -> Setting up observer on the categoriesList
        onCategoryItemClick() // -> Setting up onClick listener on category items

        onSearchItemClick()

        // Causing issue with backstack
        // onCategoriesSeeAllClick()
        binding.tvCategoriesSeeAll.visibility = View.GONE
//
    }

    private fun onCategoriesSeeAllClick() {
        binding.tvCategoriesSeeAll.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_categoriesFragment)
        }
    }

    private fun onSearchItemClick() {
        binding.imgSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }


    // Observe the RandomMeal and Update UI using Glide
    private fun observeRandomMealLiveData() {
        viewModel.observeRandomMealLiveData().observe(viewLifecycleOwner) { meal ->
            // Glide library to load Image
            Glide.with(this@HomeFragment)
                .load(meal!!.strMealThumb) // Loading the image from url
                .centerCrop() // Cropping the image to fit in view
                .into(binding.imgRandomMeal) // Show the image in imageView

            val rand = Random(10).nextInt(20)

            binding.tvRandomMealName.text = meal.strMeal
            binding.tvRandomMealRating.text = " " + dataa[rand].rating
            binding.tvRandomMealDetails.text =
                meal.strCategory + " • " + meal.strArea + " • " + dataa[rand].time

            this.randomMeal = meal  // -> storing the value of current random meal


            // Hiding shimmer effect and show main layout after data is received
            Handler(Looper.getMainLooper()).postDelayed({
                //Do something after 100ms
                // Hide shimmer effect
                binding.shimmerLayoutHome.stopShimmer()
                binding.shimmerLayoutHome.visibility = View.GONE
                // Make the view visible again
                binding.homeMain.visibility = View.VISIBLE
            }, 200)
        }
    }

    // Function to set up On Click listener on Random Meal Card
    private fun onRandomMealClick() {
        binding.cvRandomMeal.setOnClickListener {
            if (randomMeal == null) {
                Toast.makeText(activity, "Turn on the net ", Toast.LENGTH_LONG).show()

            } else {
                // Intent to navigate to Meal Activity
                val intent = Intent(activity, MealActivity::class.java)
                intent.apply {  // Passing values through intent
                    putExtra(MEAL_ID, randomMeal.idMeal)
                    putExtra(MEAL_NAME, randomMeal.strMeal)
                    putExtra(MEAL_THUMB, randomMeal.strMealThumb)
                    putExtra(MEAL_AREA, randomMeal.strArea)
                    putExtra(CATEGORY_NAME, randomMeal.strCategory)

                    startActivity(this)
                }
            }
        }
    }





    // Setting up recycler view for categories
    private fun prepareCategoriesRecyclerView() {
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
            adapter = categoriesAdapter
        }
    }

    // Observe the Categories and set up recycler view
    private fun observeCategoriesLiveData() {
        viewModel.observeCategoriesLiveData().observe(viewLifecycleOwner) { categories ->
            categoriesAdapter.setCategoryList(categories)

        }
    }

    // Function to navigate to CategoryMealsActivity from popular item list onClick
    private fun onCategoryItemClick() {
        categoriesAdapter.onItemClick = { category ->
            val intent = Intent(activity, CategoryMealsActivity::class.java)
            intent.putExtra(CATEGORY_NAME, category.strCategory)
            startActivity(intent)
        }
    }

}
