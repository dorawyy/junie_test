package com.example.biteswipe.ui.group

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.biteswipe.R
import com.example.biteswipe.databinding.FragmentGroupsBinding
import com.example.biteswipe.model.Group
import com.example.biteswipe.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Fragment for displaying and managing groups.
 */
class GroupsFragment : Fragment() {

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: GroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupButtons()
        
        // Load groups when the fragment is created
        loadGroups()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh groups when returning to this fragment
        loadGroups()
    }
    
    private fun setupRecyclerView() {
        adapter = GroupAdapter(emptyList()) { group ->
            // Handle group click - navigate to group detail
            navigateToGroupDetail(group)
        }
        
        binding.recyclerGroups.layoutManager = LinearLayoutManager(context)
        binding.recyclerGroups.adapter = adapter
    }
    
    private fun setupButtons() {
        binding.buttonCreateGroup.setOnClickListener {
            // Navigate to create group screen
            startActivity(Intent(requireContext(), CreateGroupActivity::class.java))
        }
        
        binding.buttonJoinGroup.setOnClickListener {
            // Show join group dialog
            JoinGroupDialogFragment().show(childFragmentManager, "JoinGroupDialog")
        }
    }
    
    private fun loadGroups() {
        binding.progressBar.visibility = View.VISIBLE
        
        // TODO: Get token from shared preferences
        val token = "Bearer your_token_here"
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // TODO: Implement API call to get user's groups
                // For now, use dummy data
                val dummyGroups = listOf(
                    Group(
                        id = "1",
                        name = "Lunch Buddies",
                        code = "ABC123",
                        creatorId = "user1",
                        members = listOf("user1", "user2", "user3"),
                        createdAt = System.currentTimeMillis()
                    ),
                    Group(
                        id = "2",
                        name = "Dinner Squad",
                        code = "XYZ789",
                        creatorId = "user1",
                        members = listOf("user1", "user4", "user5"),
                        createdAt = System.currentTimeMillis()
                    )
                )
                
                updateUI(dummyGroups)
                
            } catch (e: HttpException) {
                showError(getString(R.string.error_network))
            } catch (e: IOException) {
                showError(getString(R.string.error_network))
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateUI(groups: List<Group>) {
        adapter.updateData(groups)
        
        if (groups.isEmpty()) {
            binding.recyclerGroups.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerGroups.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }
    
    private fun navigateToGroupDetail(group: Group) {
        // Navigate to group detail screen
        val intent = Intent(requireContext(), GroupDetailActivity::class.java).apply {
            putExtra("GROUP_ID", group.id)
        }
        startActivity(intent)
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}