package com.example.flavormix.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.flavormix.R
import com.example.flavormix.databinding.ActivityMealBinding
import com.example.flavormix.db.MealDatabase
import com.example.flavormix.fragments.HomeFragment
import com.example.flavormix.model.Meal
import com.example.flavormix.model.UserFavoriteMeal
import com.example.flavormix.repository.UserFavoriteMealRepository
import com.example.flavormix.viewModel.MealViewModel
import com.example.flavormix.viewModel.MealViewModelFactory

class MealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMealBinding

    // variables for Meal values
    private lateinit var mealId: String
    private lateinit var mealName: String
    private lateinit var mealThumb: String
    private lateinit var youtubeLink: String

    // ViewModel declaration
    private lateinit var mealMvvm: MealViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.accent)

        // Initializing the instance of MealViewModel
        // mealMvvm = ViewModelProviders.of(this)[MealViewModel::class.java]

        // Creating an instance of mealDatabase
        val mealDatabase = MealDatabase.getInstance(this)
        val userFavoriteMealRepository = UserFavoriteMealRepository(
            mealDatabase.userFavoriteMealDao(),
            mealDatabase.mealDao()  // Adaugă și `mealDao()`
        )

        // Creating instance of ViewModel Factory to pass to viewModel provider
        val viewModelFactory = MealViewModelFactory(mealDatabase, userFavoriteMealRepository)
        mealMvvm = ViewModelProvider(this, viewModelFactory)[MealViewModel::class.java]

        // Initial Data
        getMealInformationFromIntent()  // -> Getting id, name and url of meal
        setInformationInViews()  // -> Setting id, name and image through url

        // Showing the loading animation
        loadingCase()

        // Remaining Data
        mealMvvm.getMealDetailByID(mealId)  // -> Getting all the meal details using id
        observeMealDetailsLiveData()  // -> Setting up remaining views

        // Setting up onclick Listener on Youtube icon
        onYoutubeImageClick()

        // Adding meal to favorites
        onFavoriteClick()

    }

    private fun onFavoriteClick() {
        binding.fabFavorites.setOnClickListener {
            mealToSave?.let { meal ->
                val userId = getUserId()
                val favoriteMeal = UserFavoriteMeal(userId = userId, mealId = meal.idMeal)

                // Adaugă în tabela UserFavoriteMeal
                mealMvvm.insertUserFavoriteMeal(favoriteMeal,meal)

                Toast.makeText(this, "Meal Saved to Favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Function to redirect user to youtube link
    private fun onYoutubeImageClick() {
        binding.imgYoutube.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink))
//            startActivity(intent)
        }
    }

    // Getting information from intent and storing in variables
    private fun getMealInformationFromIntent() {
        val intent = intent
        mealId = intent.getStringExtra(HomeFragment.MEAL_ID)!!
        mealName = intent.getStringExtra(HomeFragment.MEAL_NAME)!!
        mealThumb = intent.getStringExtra(HomeFragment.MEAL_THUMB)!!

    }

    // Setting the initial information in Views
    private fun setInformationInViews() {
        // Using Glide library to load image in Imageview
        Glide.with(applicationContext)
            .load(mealThumb)
            .centerCrop()
            .into(binding.imgMealDetails)

        // Setting up Title
        binding.apply {
            collapsingToolbar.title = mealName
        }
    }

    // Saving meal data to a var to pass into room database
    private var mealToSave: Meal? = null
    // To set up the remaining data
    private fun observeMealDetailsLiveData() {
        // Observing Livedata to setup other fields - Instructions, category, area...
        mealMvvm.observerMealDetailsLiveData().observe(this) { meal ->
            onResponseCase()  // -> Hiding loading animation and showing other views

            mealToSave = meal  // -> saving meal instance into another var to pass to room database

            val ingredients = "• " + meal.strIngredient1 + ": " + meal.strMeasure1 + "\n" +
                    "• " + meal.strIngredient2 + ": " + meal.strMeasure2 + "\n" +
                    "• " + meal.strIngredient3 + ": " + meal.strMeasure3 + "\n" +
                    "• " + meal.strIngredient4 + ": " + meal.strMeasure4 + "\n" +
                    "• " + meal.strIngredient5 + ": " + meal.strMeasure5 + "\n" +
                    "• " + meal.strIngredient6 + ": " + meal.strMeasure6 + "\n" +
                    "• " + meal.strIngredient7 + ": " + meal.strMeasure7 + "\n"
//                    "• " + meal.strIngredient8 + ": " + meal.strMeasure8 + "\n" +
//                    "• " + meal.strIngredient9 + ": " + meal.strMeasure9 + "\n" +
//                    "• " + meal.strIngredient10+ ": " + meal.strMeasure10 + "\n" +
//                    "• " + meal.strIngredient11 + ": " + meal.strMeasure11 + "\n" +
//                    "• " + meal.strIngredient12 + ": " + meal.strMeasure12

            // Setting up views on meal activity
            binding.apply {
                tvMealCategory.text = "Category : ${meal.strCategory}"
                tvMealArea.text = "Area : ${meal.strArea}"
                tvIngredients.text = ingredients.trim()
                tvSteps.text = meal.strInstructions.toString()
            }

            // Setting up youtube link
            youtubeLink = meal.strYoutube.toString()
        }
    }

    // Setting up Loading animation
    private fun loadingCase(){  // -> Function to hide views while loading
        binding.apply {
            progressBar.visibility = View.VISIBLE

            fabFavorites.visibility = View.INVISIBLE
            tvMealCategory.visibility = View.INVISIBLE
            tvMealArea.visibility = View.INVISIBLE
            imgYoutube.visibility = View.INVISIBLE

            tvSteps.visibility = View.INVISIBLE
            tvStepsTitle.visibility = View.INVISIBLE
            tvIngredients.visibility = View.INVISIBLE
            tvIngredientsTitle.visibility = View.INVISIBLE
        }
    }

    private fun onResponseCase(){  // -> Function to hide loading when data is received
        binding.apply {
            progressBar.visibility = View.INVISIBLE

            fabFavorites.visibility = View.VISIBLE
            tvMealCategory.visibility = View.VISIBLE
            tvMealArea.visibility = View.VISIBLE
            imgYoutube.visibility = View.INVISIBLE

            tvSteps.visibility = View.VISIBLE
            tvStepsTitle.visibility = View.VISIBLE
            tvIngredients.visibility = View.VISIBLE
            tvIngredientsTitle.visibility = View.VISIBLE
        }
    }
    private fun getUserId(): Int {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)  // Dacă nu există, returnează -1
    }

}