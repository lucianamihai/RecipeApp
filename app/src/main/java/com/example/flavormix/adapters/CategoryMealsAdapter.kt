package com.example.flavormix.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flavormix.databinding.MealItem2Binding
import com.example.flavormix.model.MealList
import com.example.flavormix.model.MealsByCategory
import com.example.flavormix.model.Dataa
import com.example.flavormix.model.dataa
import com.example.flavormix.model.numArray
import com.example.flavormix.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class CategoryMealsAdapter
    : RecyclerView.Adapter<CategoryMealsAdapter.CategoryMealsViewHolder>() {


//    val categoryMealsMvvm = ViewModelProviders.of()[CategoryMealsViewModel::class.java]

    lateinit var onItemClick: ((MealsByCategory) -> Unit)

    private var mealList = ArrayList<MealsByCategory>()

    fun setMealsList(mealList: List<MealsByCategory>){
        this.mealList = mealList as ArrayList<MealsByCategory>
        notifyDataSetChanged()
    }

    // View Holder
    inner class CategoryMealsViewHolder(val binding: MealItem2Binding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryMealsViewHolder {

        return CategoryMealsViewHolder(
            MealItem2Binding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return mealList.size
    }

    override fun onBindViewHolder(holder: CategoryMealsViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(mealList[position].strMealThumb)
            .into(holder.binding.imgMeal)

//        getMealDetailByID(mealList[position].idMeal)


        RetrofitInstance.api.getMealDetailsById(mealList[position].idMeal).enqueue(object : Callback<MealList>{
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                val meal = response.body()?.meals?.first()
                meal?.let {
                    holder.binding.tvMealCategory.text = response.body()!!.meals[0].strCategory
                    holder.binding.tvMealArea.text = response.body()!!.meals[0].strArea
                }
            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.d("TEST", t.message.toString())
            }

        })

        val rand = Random.nextInt(6)

        holder.binding.apply {
            tvMealName.text = mealList[position].strMeal

            tvMealTime.text = dataa[position].time
            tvMealRating.text = " " + dataa[position].rating +" (" + numArray[rand].toString() + "+)"

        }

        // lambda variable for onClick method
        holder.itemView.setOnClickListener {
            onItemClick.invoke(mealList[position])
        }


    }

    fun getMealById(id: String) {
        RetrofitInstance.api.getMealDetailsById(id).enqueue(object : Callback<MealList>{
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                val meal = response.body()?.meals?.first()
                meal?.let {
                    val currentMeal = response.body()!!.meals[0]
                }
            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.d("TEST", t.message.toString())
            }

        })
    }

}