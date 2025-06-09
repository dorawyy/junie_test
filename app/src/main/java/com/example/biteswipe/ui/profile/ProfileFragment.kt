package com.example.biteswipe.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.biteswipe.R
import com.example.biteswipe.databinding.FragmentProfileBinding
import com.example.biteswipe.model.User
import com.example.biteswipe.network.RetrofitClient
import com.example.biteswipe.ui.auth.LoginActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Fragment for displaying and editing user profile.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up click listeners
        binding.buttonLogout.setOnClickListener {
            logout()
        }
        
        // Load user profile
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        binding.progressBar.visibility = View.VISIBLE
        
        // TODO: Get token from shared preferences
        val token = "Bearer your_token_here"
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getCurrentUser(token)
                
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        updateUI(user)
                    }
                } else {
                    // Show error message
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
    
    private fun updateUI(user: User) {
        binding.textName.text = user.name
        binding.textEmail.text = user.email
        
        // Load profile image if available
        if (!user.profileImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.placeholder_profile)
                .error(R.drawable.placeholder_profile)
                .into(binding.imageProfile)
        }
        
        // Show group count
        val groupCount = user.groups.size
        binding.textGroupCount.text = resources.getQuantityString(
            R.plurals.group_count, groupCount, groupCount
        )
    }
    
    private fun logout() {
        // TODO: Implement actual logout logic (clear token, etc.)
        
        // Navigate to login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}