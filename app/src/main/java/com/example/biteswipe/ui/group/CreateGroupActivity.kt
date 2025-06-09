package com.example.biteswipe.ui.group

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.biteswipe.R
import com.example.biteswipe.databinding.ActivityCreateGroupBinding
import com.example.biteswipe.network.CreateGroupRequest
import com.example.biteswipe.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Activity for creating a new group.
 */
class CreateGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.create_group)
        
        // Set up click listeners
        binding.buttonCreate.setOnClickListener {
            createGroup()
        }
    }
    
    private fun createGroup() {
        val groupName = binding.editGroupName.text.toString().trim()
        
        // Validate input
        if (groupName.isEmpty()) {
            binding.editGroupName.error = "Group name is required"
            binding.editGroupName.requestFocus()
            return
        }
        
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonCreate.isEnabled = false
        
        // TODO: Get token from shared preferences
        val token = "Bearer your_token_here"
        
        // Make API call
        lifecycleScope.launch {
            try {
                val createRequest = CreateGroupRequest(groupName)
                val response = RetrofitClient.apiService.createGroup(token, createRequest)
                
                if (response.isSuccessful) {
                    // Show success message
                    Toast.makeText(
                        this@CreateGroupActivity,
                        getString(R.string.group_created),
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Close the activity
                    finish()
                } else {
                    // Show error message
                    Toast.makeText(
                        this@CreateGroupActivity,
                        getString(R.string.error_group_create),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: HttpException) {
                Toast.makeText(
                    this@CreateGroupActivity,
                    getString(R.string.error_network),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    this@CreateGroupActivity,
                    getString(R.string.error_network),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Hide loading indicator
                binding.progressBar.visibility = View.GONE
                binding.buttonCreate.isEnabled = true
            }
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}