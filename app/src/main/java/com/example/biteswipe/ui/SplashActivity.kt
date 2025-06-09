package com.example.biteswipe.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.biteswipe.R
import com.example.biteswipe.ui.auth.LoginActivity

/**
 * Splash screen activity that displays the app logo and transitions to the login screen.
 */
class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Use a handler to delay the transition to the next screen
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in
            val isLoggedIn = false // TODO: Implement actual login check
            
            // Navigate to the appropriate screen
            val intent = if (isLoggedIn) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            
            startActivity(intent)
            finish() // Close the splash activity
        }, SPLASH_DELAY)
    }
}