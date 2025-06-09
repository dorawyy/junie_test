package com.example.biteswipe.model

import java.io.Serializable

/**
 * Data class representing a user in the BiteSwipe app.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val profileImageUrl: String? = null,
    val groups: List<String> = emptyList() // List of group IDs the user belongs to
) : Serializable