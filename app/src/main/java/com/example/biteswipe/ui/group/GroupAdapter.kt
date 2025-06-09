package com.example.biteswipe.ui.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.biteswipe.databinding.ItemGroupBinding
import com.example.biteswipe.model.Group
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying groups in a RecyclerView.
 */
class GroupAdapter(
    private var groups: List<Group>,
    private val onGroupClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    /**
     * Updates the adapter data with a new list of groups.
     */
    fun updateData(newGroups: List<Group>) {
        this.groups = newGroups
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for group items.
     */
    inner class GroupViewHolder(
        private val binding: ItemGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(group: Group) {
            binding.textGroupName.text = group.name
            binding.textGroupCode.text = "Code: ${group.code}"
            binding.textMemberCount.text = "${group.members.size} members"
            
            // Format the creation date
            val date = Date(group.createdAt)
            binding.textCreatedAt.text = "Created: ${dateFormat.format(date)}"
            
            // Set match status
            if (group.matchedRestaurantId != null) {
                binding.textMatchStatus.text = "Match found!"
            } else if (group.allMembersVoted()) {
                binding.textMatchStatus.text = "No match found"
            } else {
                binding.textMatchStatus.text = "Waiting for votes"
            }
            
            // Set click listener
            binding.root.setOnClickListener {
                onGroupClick(group)
            }
        }
    }
}