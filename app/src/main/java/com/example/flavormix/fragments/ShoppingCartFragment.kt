package com.example.flavormix.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flavormix.adapters.IngredientAdapter
import com.example.flavormix.databinding.FragmentShoppingCartBinding
import com.example.flavormix.viewModel.ShoppingCartViewModel

class ShoppingCartFragment : Fragment() {

    private lateinit var viewModel: ShoppingCartViewModel
    private lateinit var adapter: IngredientAdapter
    private var _binding: FragmentShoppingCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingCartBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[ShoppingCartViewModel::class.java]

        adapter = IngredientAdapter { ingredient ->
            viewModel.deleteIngredient(ingredient)
        }
        binding.ingredientRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.ingredientRecyclerView.adapter = adapter
        binding.ingredientRecyclerView.visibility = View.VISIBLE

        binding.addButton.setOnClickListener {
            val ingredientName = binding.ingredientEditText.text.toString()
            if (ingredientName.isNotBlank()) {
                viewModel.addIngredient(ingredientName)
                binding.ingredientEditText.text.clear()
            } else {
                Toast.makeText(context, "Please enter an ingredient", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            if (ingredients.isNotEmpty()) {
                Log.d("ShoppingCartFragment", "Ingredients loaded: ${ingredients.size}")
            } else {
                Log.d("ShoppingCartFragment", "No ingredients found")
            }
            adapter.submitList(ingredients)
            adapter.notifyDataSetChanged()
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
