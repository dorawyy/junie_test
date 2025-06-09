package com.example.biteswipe.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.biteswipe.R
import com.example.biteswipe.databinding.DialogJoinGroupBinding
import com.example.biteswipe.network.JoinGroupRequest
import com.example.biteswipe.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Dialog fragment for joining a group by entering a group code.
 */
class JoinGroupDialogFragment : DialogFragment() {

    private var _binding: DialogJoinGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogJoinGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up click listeners
        binding.buttonJoin.setOnClickListener {
            joinGroup()
        }
        
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }
    
    private fun joinGroup() {
        val code = binding.editGroupCode.text.toString().trim()
        
        // Validate input
        if (code.isEmpty()) {
            binding.editGroupCode.error = "Group code is required"
            binding.editGroupCode.requestFocus()
            return
        }
        
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonJoin.isEnabled = false
        binding.buttonCancel.isEnabled = false
        
        // TODO: Get token from shared preferences
        val token = "Bearer your_token_here"
        
        // Make API call
        lifecycleScope.launch {
            try {
                val joinRequest = JoinGroupRequest(code)
                val response = RetrofitClient.apiService.joinGroup(token, joinRequest)
                
                if (response.isSuccessful) {
                    // Show success message
                    Toast.makeText(
                        context,
                        getString(R.string.group_joined),
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Close the dialog
                    dismiss()
                } else {
                    // Show error message
                    Toast.makeText(
                        context,
                        getString(R.string.error_group_join),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: HttpException) {
                Toast.makeText(
                    context,
                    getString(R.string.error_network),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    context,
                    getString(R.string.error_network),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Hide loading indicator
                binding.progressBar.visibility = View.GONE
                binding.buttonJoin.isEnabled = true
                binding.buttonCancel.isEnabled = true
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}