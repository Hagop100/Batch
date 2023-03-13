package com.example.batchtest.OtherGroupsTab.PendingGroups

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.util.Log
import android.widget.ImageButton
import com.example.batchtest.Group
import com.example.batchtest.databinding.VoteGroupCardBinding
import de.hdodenhof.circleimageview.CircleImageView

private const val TAG = "PendingGroupAdapter"

class PendingGroupAdapter(private val context: Context?, private val groups: ArrayList<Group>) : RecyclerView.Adapter<PendingGroupAdapter.PendingGroupHolder>() {
    // holder class for each pending group
    class PendingGroupHolder(val binding: VoteGroupCardBinding) : RecyclerView.ViewHolder(binding.root) {
        // get the views of card
        // name of pending group
        val pendingGroupName: TextView = binding.pendingGroupName
        // profile picture of user's group that initiated voting
        val myGroupImg: CircleImageView = binding.myGroupImg
        // profile picture of pending group
        val pendingGroupImg: CircleImageView = binding.pendingGroupImg
        // accept button
        val acceptBtn: ImageButton = binding.acceptBtn
        // reject button
        val rejectBtn: ImageButton = binding.rejectBtn
    }

    // inflate parent fragment with card item layout when ViewHolder is created
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingGroupHolder {
        // inflater from parent fragment
        val inflater = LayoutInflater.from(parent.context)

        // inflate parent using vote group card layout
        val binding = VoteGroupCardBinding.inflate(inflater, parent, false)

        // pass binding into the holder
        return PendingGroupHolder(binding)
    }

    // set the group information for each pending group that will be displayed
    override fun onBindViewHolder(holder: PendingGroupHolder, position: Int) {
        val group = groups[position]
        holder.pendingGroupName.text = group.name
        holder.rejectBtn.setOnClickListener {
            Log.v(TAG, "group removed:$group")
            groups.remove(group)
        }
//        if (context != null) {
//            Glide.with(context).load(group.image).into(holder.pendingGroupImg)
//        }

    }

    // returns number of pending groups
    override fun getItemCount(): Int {
        return groups.size
    }

}