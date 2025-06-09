package com.example.biteswipe.network

import com.example.biteswipe.model.Group
import com.example.biteswipe.model.Restaurant
import com.example.biteswipe.model.User
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface for the BiteSwipe API.
 */
interface ApiService {
    
    // User endpoints
    @POST("users/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("users/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<User>
    
    // Restaurant endpoints
    @GET("restaurants")
    suspend fun getRestaurants(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 5000, // Default 5km radius
        @Query("cuisine") cuisine: String? = null
    ): Response<List<Restaurant>>
    
    @GET("restaurants/{id}")
    suspend fun getRestaurantById(
        @Path("id") id: String
    ): Response<Restaurant>
    
    // Group endpoints
    @POST("groups")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body request: CreateGroupRequest
    ): Response<Group>
    
    @GET("groups/{id}")
    suspend fun getGroupById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Group>
    
    @POST("groups/join")
    suspend fun joinGroup(
        @Header("Authorization") token: String,
        @Body request: JoinGroupRequest
    ): Response<Group>
    
    @POST("groups/{id}/preferences")
    suspend fun submitPreferences(
        @Header("Authorization") token: String,
        @Path("id") groupId: String,
        @Body request: PreferencesRequest
    ): Response<Group>
    
    @GET("groups/{id}/match")
    suspend fun getGroupMatch(
        @Header("Authorization") token: String,
        @Path("id") groupId: String
    ): Response<MatchResponse>
}

// Request and response data classes
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class CreateGroupRequest(
    val name: String
)

data class JoinGroupRequest(
    val code: String
)

data class PreferencesRequest(
    val likedRestaurantIds: List<String>
)

data class MatchResponse(
    val matched: Boolean,
    val restaurant: Restaurant?
)