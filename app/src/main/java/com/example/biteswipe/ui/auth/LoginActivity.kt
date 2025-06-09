package com.example.biteswipe.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.biteswipe.R
import com.example.biteswipe.databinding.ActivityLoginBinding
import com.example.biteswipe.network.LoginRequest
import com.example.biteswipe.network.RetrofitClient
import com.example.biteswipe.ui.MainActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Activity for user login.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listeners
        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.textRegister.setOnClickListener {
            // Navigate to register screen
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        // Validate input
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

        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonLogin.isEnabled = false

        // Make API call
        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = RetrofitClient.apiService.loginUser(loginRequest)

                if (response.isSuccessful) {
                    // Save token and user info
                    val authResponse = response.body()
                    // TODO: Save token and user info to shared preferences

                    // Navigate to main screen
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Show error message
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.error_login),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: HttpException) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.error_network),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.error_network),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Hide loading indicator
                binding.progressBar.visibility = View.GONE
                binding.buttonLogin.isEnabled = true
            }
        }
    }
}