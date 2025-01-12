package com.example.flavormix.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.flavormix.R
import com.example.flavormix.activities.MainActivity
import com.example.flavormix.activities.MealActivity
import com.example.flavormix.adapters.FavoritesAdapter
import com.example.flavormix.databinding.FragmentFavoritesBinding
import com.example.flavormix.db.MealDatabase
import com.example.flavormix.model.UserFavoriteMeal
import com.example.flavormix.repository.UserFavoriteMealRepository
import com.example.flavormix.viewModel.HomeViewModel
import com.example.flavormix.viewModel.HomeViewModelFactory
import com.google.android.material.snackbar.Snackbar

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var viewModel: HomeViewModel

    // Adapter
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mealDatabase = MealDatabase.getInstance(requireContext()) // Inițializează baza de date Room
        val userFavoriteMealRepository = UserFavoriteMealRepository(
            mealDatabase.userFavoriteMealDao(),
            mealDatabase.mealDao()
        )
        val viewModelFactory = HomeViewModelFactory(mealDatabase, userFavoriteMealRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareRecyclerView()
        observeFavorites()

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = true

            // Deleting on swipe
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Getting the position of current item
                val position = viewHolder.adapterPosition
                // Saving an instance of current meal in case of undo
                val mealUndo = favoritesAdapter.differ.currentList[position]
                // Deleting meal from database
                viewModel.deleteMeal(favoritesAdapter.differ.currentList[position])

                // Showing a SnackBar with undo option
                Snackbar.make(requireView(), "Meal Deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", View.OnClickListener {
                        viewModel.insertMeal(mealUndo)
                    }).show()
            }
        }

        // Attaching itemTouchHelper to Recycler View
        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(binding.rvFavorites)

        onCategoryMealItemClick()  // -> setting up onClick method
    }


    private fun prepareRecyclerView() {
        favoritesAdapter = FavoritesAdapter { meal ->
            val userId = getUserId()
            val favoriteMeal = UserFavoriteMeal(userId = userId, mealId = meal.idMeal)
            viewModel.deleteMeal(favoriteMeal)

            Snackbar.make(requireView(), "${meal.strMeal} removed from favorites", Snackbar.LENGTH_LONG)
                .setAction("UNDO") {
                    viewModel.insertMeal(meal) // Opțiune de undo pentru a adăuga înapoi meal-ul
                }.show()
        }

        binding.rvFavorites.apply {
            layoutManager = GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
            adapter = favoritesAdapter
        }
    }


    private fun observeFavorites() {
        val userId = getUserId()
        viewModel.getFavoriteMealsForUser(userId)

        viewModel.favoriteMealsLiveData.observe(viewLifecycleOwner) { meals ->
            favoritesAdapter.differ.submitList(meals)
        }
    }



    // Function to navigate to MealActivity from favorite meal item list onClick
    private fun onCategoryMealItemClick() {
        favoritesAdapter.onItemClick = { meal ->
            val intent = Intent(activity, MealActivity::class.java)
            intent.apply {
                putExtra(HomeFragment.MEAL_ID, meal.idMeal)
                putExtra(HomeFragment.MEAL_NAME, meal.strMeal)
                putExtra(HomeFragment.MEAL_THUMB, meal.strMealThumb)

                startActivity(intent)
            }
        }
    }
    private fun getUserId(): Int {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)  // Returnează -1 dacă nu există user_id
    }

}