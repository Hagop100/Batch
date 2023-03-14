package com.example.batchtest.OtherGroupsTab.PendingGroups

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import com.example.batchtest.Group
import com.example.batchtest.PendingGroup
import com.example.batchtest.databinding.VoteGroupCardBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

private const val TAG = "PendingGroupAdapter"

class PendingGroupAdapter(private val context: Context?, private val groups: ArrayList<PendingGroup>) : RecyclerView.Adapter<PendingGroupAdapter.PendingGroupHolder>() {
    private val db = Firebase.firestore
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
        // members
        val members = hashMapOf(0 to binding.member0, 1 to binding.member1, 2 to binding.member2, 3 to binding.member3)
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
        var matchingGroup: Group? = group.matchingGroup
        var pendingGroup: Group? = group.pendingGroup
        if (pendingGroup != null) {
            holder.pendingGroupName.text = pendingGroup.name
        }

        for (user in group.users!!) {
            holder.members[user["index"]]?.setColorFilter(Color.GREEN)
            if (user["vote"] == "accept") {

            }
//            if (user["index"] == 0) {
//                holder.member0.setColorFilter(Color.GREEN)
//            } else if (user["index"] == 1) {
//                val member = holder.member1
//                member.setColorFilter(Color.GREEN)
//            }
//            user["index"]
        }

//        for (user in group.users!!) {
//            if (user["vote"] == "accept") {
//                holder.member
//            }
//            if (user["acceptor"] == true) {
//
//                holder.member1.setColorFilter()
//            }
//        }
//        group.votes?.forEach { (user, vote) ->
//            if (vote) {
//
//            }
//        }
        holder.rejectBtn.setOnClickListener {
            Log.v(TAG, "group removed:$group")
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