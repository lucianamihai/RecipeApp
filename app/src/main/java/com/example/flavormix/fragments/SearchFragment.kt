package com.example.flavormix.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flavormix.R
import com.example.flavormix.activities.MainActivity
import com.example.flavormix.activities.MealActivity
import com.example.flavormix.adapters.FavoritesAdapter
import com.example.flavormix.databinding.FragmentSearchBinding
import com.example.flavormix.viewModel.HomeViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: HomeViewModel

    // Using favorites Adapter
    private lateinit var searchRvAdapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = (activity as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareRecyclerView()

//        binding.imgSearchArrow.setOnClickListener { searchMeals() }

        observeSearchedMealsLiveData()

        // For automatic searching
        var searchJob: Job? = null
        binding.etSearchBox.addTextChangedListener { searchQuery ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(500)
                viewModel.searchMeals(searchQuery.toString())
//                if(binding.etSearchBox.text.isEmpty()){
//                    searchRvAdapter.differ.
//                }
            }
        }

        onCategoryMealItemClick()

//        binding.imgSearchArrow.setOnClickListener {
//
//        }
    }

    private fun observeSearchedMealsLiveData() {
        viewModel.observeSearchedMealLiveData().observe(viewLifecycleOwner, Observer{ mealsList ->
            searchRvAdapter.differ.submitList(mealsList)
        })
    }

    private fun searchMeals() {
        val searchQuery = binding.etSearchBox.text.toString()
        if(searchQuery.isNotEmpty()){
            viewModel.searchMeals(searchQuery)
        }
    }

    private fun prepareRecyclerView() {
        searchRvAdapter = FavoritesAdapter{meal ->  }
        binding.rvSearchedMeals.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchRvAdapter
        }
    }

    // Function to navigate to MealActivity from search meal item list onClick
    private fun onCategoryMealItemClick() {
        searchRvAdapter.onItemClick = { meal ->
            val intent = Intent(activity, MealActivity::class.java)
            intent.apply {
                putExtra(HomeFragment.MEAL_ID, meal.idMeal)
                putExtra(HomeFragment.MEAL_NAME, meal.strMeal)
                putExtra(HomeFragment.MEAL_THUMB, meal.strMealThumb)

                startActivity(intent)
            }
        }
    }


}