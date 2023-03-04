package com.example.batchtest.OtherGroupsTab.MatchedGroups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.Group
import com.example.batchtest.databinding.MatchedGroupRecyclerViewRowBinding

class MatchedGroupAdapter(private val matchedGroupList: ArrayList<Group>,
                          private val listener: MatchedGroupRecyclerViewEvent): RecyclerView.Adapter<MatchedGroupAdapter.MatchedGroupViewHolder>() {

    inner class MatchedGroupViewHolder(val binding: MatchedGroupRecyclerViewRowBinding):
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val groupName: TextView = binding.matchedGroupRecycleViewRowGroupName
        val groupPhoto: ImageView = binding.matchedGroupRecyclerViewRowGroupPhoto

        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = absoluteAdapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchedGroupViewHolder {
        // inflater from parent fragment
        val inflater = LayoutInflater.from(parent.context)

        // inflate parent using vote group card layout
        val binding = MatchedGroupRecyclerViewRowBinding.inflate(inflater, parent, false)

        // pass binding into the holder
        return MatchedGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchedGroupViewHolder, position: Int) {
        val currentItem = matchedGroupList[position]
        holder.groupName.text = currentItem.name
        //holder.groupPhoto = currentItem.image
    }

    override fun getItemCount(): Int {
        return matchedGroupList.size
    }

    interface MatchedGroupRecyclerViewEvent {
        fun onItemClick(position: Int)
    }

}