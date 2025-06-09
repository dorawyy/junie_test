package com.example.biteswipe.model

import java.io.Serializable

/**
 * Data class representing a restaurant in the BiteSwipe app.
 */
data class Restaurant(
    val id: String,
    val name: String,
    val imageUrl: String,
    val cuisine: String,
    val priceRange: String, // "$", "$$", "$$$", "$$$$"
    val rating: Float, // 1.0 to 5.0
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String? = null,
    val website: String? = null,
    val hours: Map<String, String> = emptyMap(), // Day of week -> hours (e.g., "Monday" -> "9:00 AM - 10:00 PM")
    val reviews: List<Review> = emptyList()
) : Serializable {
    
    /**
     * Nested data class representing a review for a restaurant.
     */
    data class Review(
        val id: String,
        val userId: String,
        val userName: String,
        val rating: Float,
        val comment: String,
        val date: Long // Timestamp
    ) : Serializable
}