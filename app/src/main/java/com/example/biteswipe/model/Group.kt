package com.example.biteswipe.model

import java.io.Serializable

/**
 * Data class representing a group in the BiteSwipe app.
 */
data class Group(
    val id: String,
    val name: String,
    val code: String, // Unique code for joining the group
    val creatorId: String, // ID of the user who created the group
    val members: List<String>, // List of user IDs in the group
    val restaurantPreferences: Map<String, List<String>> = emptyMap(), // User ID -> List of liked restaurant IDs
    val matchedRestaurantId: String? = null, // ID of the matched restaurant, if any
    val createdAt: Long, // Timestamp when the group was created
    val expiresAt: Long? = null // Optional timestamp when the group expires
) : Serializable {
    
    /**
     * Checks if all members have submitted their preferences.
     */
    fun allMembersVoted(): Boolean {
        return members.all { memberId -> restaurantPreferences.containsKey(memberId) }
    }
    
    /**
     * Finds the restaurant that has the most likes among group members.
     * Returns the restaurant ID or null if no match is found.
     */
    fun findBestMatch(): String? {
        if (!allMembersVoted()) return null
        
        // Count likes for each restaurant
        val restaurantLikes = mutableMapOf<String, Int>()
        
        restaurantPreferences.values.forEach { likedRestaurants ->
            likedRestaurants.forEach { restaurantId ->
                restaurantLikes[restaurantId] = (restaurantLikes[restaurantId] ?: 0) + 1
            }
        }
        
        // Find the restaurant with the most likes
        return restaurantLikes.entries
            .filter { (_, likes) -> likes > 0 }
            .maxByOrNull { (_, likes) -> likes }
            ?.key
    }
}