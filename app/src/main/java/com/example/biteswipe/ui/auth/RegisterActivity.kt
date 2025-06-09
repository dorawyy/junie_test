package com.example.biteswipe.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.biteswipe.R
import com.example.biteswipe.databinding.ActivityRegisterBinding
import com.example.biteswipe.network.RegisterRequest
import com.example.biteswipe.network.RetrofitClient
import com.example.biteswipe.ui.MainActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Activity for user registration.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listeners
        binding.buttonRegister.setOnClickListener {
            registerUser()
        }

        binding.textLogin.setOnClickListener {
            // Navigate back to login screen
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.editName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        val confirmPassword = binding.editConfirmPassword.text.toString().trim()

        // Validate input
        if (name.isEmpty()) {
            binding.editName.error = "Name is required"
            binding.editName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.editEmail.error = "Email is required"
            binding.editEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.editPassword.error = "Password is required"
            binding.editPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.editPassword.error = "Password must be at least 6 characters"
            binding.editPassword.requestFocus()
            return
        }

        if (confirmPassword != password) {
            binding.editConfirmPassword.error = "Passwords do not match"
            binding.editConfirmPassword.requestFocus()
            return
        }

        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonRegister.isEnabled = false

        // Make API call
        lifecycleScope.launch {
            try {
                val registerRequest = RegisterRequest(name, email, password)
                val response = RetrofitClient.apiService.registerUser(registerRequest)

                if (response.isSuccessful) {
                    // Save token and user info
                    val authResponse = response.body()
                    // TODO: Save token and user info to shared preferences

                    // Navigate to main screen
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Show error message
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.error_register),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: HttpException) {
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.error_network),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.error_network),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Hide loading indicator
                binding.progressBar.visibility = View.GONE
                binding.buttonRegister.isEnabled = true
            }
        }
    }
}