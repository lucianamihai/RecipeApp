package com.example.flavormix.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavormix.databinding.MealItem2Binding
import com.example.flavormix.model.Meal
import com.example.flavormix.model.Dataa
import com.example.flavormix.model.dataa
import com.example.flavormix.model.numArray
import kotlin.random.Random
import android.view.MenuInflater
import android.widget.PopupMenu
import com.example.flavormix.R

class FavoritesAdapter(private val onDeleteClick: (Meal) -> Unit) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    lateinit var onItemClick : ((Meal) -> Unit)

    // ViewHolder
    inner class FavoritesViewHolder(val binding: MealItem2Binding)
        : RecyclerView.ViewHolder(binding.root)


    private val diffUtil = object: DiffUtil.ItemCallback<Meal>(){
        override fun areItemsTheSame(oldItem: Meal, newItem: Meal): Boolean {
            // checks if the items are same
            return oldItem.idMeal == newItem.idMeal  // returns true if item id is same
        }

        override fun areContentsTheSame(oldItem: Meal, newItem: Meal): Boolean {
            // check if the contents of item same
            return oldItem == newItem  // returns true if the items are same
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        return FavoritesViewHolder(
            MealItem2Binding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val meal = differ.currentList[position]

        Glide.with(holder.itemView)
            .load(meal.strMealThumb)
            .into(holder.binding.imgMeal)

        val rand = Random.nextInt( 6)

        holder.binding.apply {
            tvMealName.text = meal.strMeal
            tvMealRating.text = dataa[position].rating +" (" + numArray[rand].toString() + "+)"
            tvMealCategory.text = "Category: " + meal.strCategory
            tvMealArea.text = "Area: " + meal.strArea
            tvMealTime.text = dataa[position].time

            btnMenu.setOnClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.menuInflater.inflate(R.menu.menu_favorites, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_delete -> {
                            onDeleteClick(meal)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick.invoke(meal)
        }
    }
}