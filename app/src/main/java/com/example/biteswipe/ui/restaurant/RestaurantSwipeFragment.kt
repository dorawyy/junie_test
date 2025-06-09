package com.example.biteswipe.ui.restaurant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.biteswipe.R
import com.example.biteswipe.databinding.FragmentRestaurantSwipeBinding
import com.example.biteswipe.model.Restaurant
import com.example.biteswipe.network.RetrofitClient
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Fragment for swiping through restaurant cards.
 */
class RestaurantSwipeFragment : Fragment(), CardStackListener {

    private var _binding: FragmentRestaurantSwipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private lateinit var adapter: RestaurantCardAdapter
    
    private val likedRestaurants = mutableListOf<String>()
    private var restaurants = listOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRestaurantSwipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCardStackView()
        setupButtons()
        loadRestaurants()
    }
    
    private fun setupCardStackView() {
        cardStackLayoutManager = CardStackLayoutManager(context, this)
        adapter = RestaurantCardAdapter(emptyList())
        
        binding.cardStackView.layoutManager = cardStackLayoutManager
        binding.cardStackView.adapter = adapter
    }
    
    private fun setupButtons() {
        binding.buttonLike.setOnClickListener {
            val direction = Direction.Right
            binding.cardStackView.swipe(direction)
        }
        
        binding.buttonDislike.setOnClickListener {
            val direction = Direction.Left
            binding.cardStackView.swipe(direction)
        }
    }
    
    private fun loadRestaurants() {
        binding.progressBar.visibility = View.VISIBLE
        
        // TODO: Get user's location
        val latitude = 37.7749 // Example: San Francisco
        val longitude = -122.4194
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getRestaurants(latitude, longitude)
                
                if (response.isSuccessful) {
                    restaurants = response.body() ?: emptyList()
                    adapter.updateData(restaurants)
                    
                    if (restaurants.isEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                    }
                } else {
                    showError(getString(R.string.error_network))
                }
            } catch (e: HttpException) {
                showError(getString(R.string.error_network))
            } catch (e: IOException) {
                showError(getString(R.string.error_network))
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun showEmptyState() {
        binding.cardStackView.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
    }
    
    private fun hideEmptyState() {
        binding.cardStackView.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    // CardStackListener implementation
    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    
    override fun onCardSwiped(direction: Direction) {
        val position = cardStackLayoutManager.topPosition - 1
        if (position >= 0 && position < restaurants.size) {
            val restaurant = restaurants[position]
            
            if (direction == Direction.Right) {
                // User liked this restaurant
                likedRestaurants.add(restaurant.id)
                Toast.makeText(context, "Liked: ${restaurant.name}", Toast.LENGTH_SHORT).show()
            } else if (direction == Direction.Left) {
                // User disliked this restaurant
                Toast.makeText(context, "Disliked: ${restaurant.name}", Toast.LENGTH_SHORT).show()
            }
            
            // Check if we've reached the end of the list
            if (cardStackLayoutManager.topPosition == adapter.itemCount) {
                // TODO: Submit preferences to server if in a group
                Toast.makeText(context, getString(R.string.no_more_restaurants), Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onCardRewound() {}
    
    override fun onCardCanceled() {}
    
    override fun onCardAppeared(view: View, position: Int) {}
    
    override fun onCardDisappeared(view: View, position: Int) {}
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}