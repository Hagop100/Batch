package com.example.batchtest

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.databinding.FragmentMatchTabBinding

class GroupHolder(
    val itemView: View
) : RecyclerView.ViewHolder(itemView) {
    fun bind(group: Group) {
//        binding.nameListTitle.text = user.getName()
    }
}

class GroupAdapter(
    private val groups: List<Group>
) : RecyclerView.Adapter<GroupHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.match_group_card, parent, false)
//        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.match_group_card, parent, false)
//        val binding = LayoutInflater.from(parent.context).inflate(R.layout.match_group_card, parent, false)
        return GroupHolder(inflater)
    }

    override fun onBindViewHolder(holder: GroupHolder, position: Int) {
        val group = groups[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

}