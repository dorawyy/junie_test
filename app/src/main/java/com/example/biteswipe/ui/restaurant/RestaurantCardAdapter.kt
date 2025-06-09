package com.example.biteswipe.ui.restaurant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.biteswipe.R
import com.example.biteswipe.databinding.ItemRestaurantCardBinding
import com.example.biteswipe.model.Restaurant

/**
 * Adapter for displaying restaurant cards in the CardStackView.
 */
class RestaurantCardAdapter(
    private var restaurants: List<Restaurant>
) : RecyclerView.Adapter<RestaurantCardAdapter.RestaurantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val binding = ItemRestaurantCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RestaurantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(restaurants[position])
    }

    override fun getItemCount(): Int = restaurants.size

    /**
     * Updates the adapter data with a new list of restaurants.
     */
    fun updateData(newRestaurants: List<Restaurant>) {
        this.restaurants = newRestaurants
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for restaurant card items.
     */
    inner class RestaurantViewHolder(
        private val binding: ItemRestaurantCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: Restaurant) {
            binding.textName.text = restaurant.name
            binding.textCuisine.text = restaurant.cuisine
            binding.textPriceRange.text = restaurant.priceRange
            binding.ratingBar.rating = restaurant.rating
            binding.textAddress.text = restaurant.address

            // Load restaurant image with Glide
            Glide.with(binding.imageRestaurant.context)
                .load(restaurant.imageUrl)
                .transform(CenterCrop(), RoundedCorners(16))
                .placeholder(R.drawable.placeholder_restaurant)
                .error(R.drawable.placeholder_restaurant)
                .into(binding.imageRestaurant)

            // Set visibility of optional fields
            binding.textPhone.text = restaurant.phone ?: ""
            binding.textPhone.visibility = if (restaurant.phone.isNullOrEmpty()) View.GONE else View.VISIBLE

            binding.textWebsite.text = restaurant.website ?: ""
            binding.textWebsite.visibility = if (restaurant.website.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }
}