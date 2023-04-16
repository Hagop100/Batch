package com.example.batchtest.OtherGroupsTab.MatchedGroups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.databinding.MatchedGroupDialogRecyclerViewRowBinding

class MatchedGroupDialogAdapter(private val myGroupsMatchedList: ArrayList<String>,
                                private val listener: MatchedGroupDialogRecyclerViewEvent,
                                private val context: Context):
    RecyclerView.Adapter<MatchedGroupDialogAdapter.MatchedGroupDialogViewHolder>() {

    inner class MatchedGroupDialogViewHolder(val binding: MatchedGroupDialogRecyclerViewRowBinding):
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.fragmentMatchedGroupDialogRecyclerViewTv.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = absoluteAdapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchedGroupDialogAdapter.MatchedGroupDialogViewHolder {
        // inflater from parent fragment
        val inflater = LayoutInflater.from(parent.context)

        // inflate parent using vote group card layout
        val binding = MatchedGroupDialogRecyclerViewRowBinding.inflate(inflater, parent, false)

        // pass binding into the holder
        return MatchedGroupDialogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchedGroupDialogAdapter.MatchedGroupDialogViewHolder, position: Int) {
        val currentItem = myGroupsMatchedList[position]
        holder.binding.fragmentMatchedGroupDialogRecyclerViewTv.text = currentItem
    }

    override fun getItemCount(): Int {
        return myGroupsMatchedList.size
    }

    /*
    Interface used to have buttons in recycler views be clicked
     */
    interface MatchedGroupDialogRecyclerViewEvent {
        fun onItemClick(position: Int)
    }
}