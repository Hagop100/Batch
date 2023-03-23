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
import androidx.core.view.isVisible
import com.example.batchtest.Group
import com.example.batchtest.PendingGroup
import com.example.batchtest.databinding.VoteGroupCardBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

private const val TAG = "PendingGroupAdapter"

class PendingGroupAdapter(private val context: Context?, private val groups: ArrayList<PendingGroup>) : RecyclerView.Adapter<PendingGroupAdapter.PendingGroupHolder>() {
    private val db = Firebase.firestore
    // get the authenticated logged in user
    private val currentUser = Firebase.auth.currentUser
    private val green = "#5BC368"
    private val red = "#C35B5B"
    private val darkRed = "#994646"
    private val grey = "#dbdbdb"

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
        val acceptBtn: ImageView = binding.acceptBtn
        // reject button
        val rejectBtn: ImageView = binding.rejectBtn
        // members
        val members = hashMapOf("0" to binding.member0, "1" to binding.member1, "2" to binding.member2, "3" to binding.member3)
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
        group.users?.forEach { (user, map) ->
            // set color for the buttons for the current user
            if (user == currentUser?.uid) {
                if (map["vote"] == "accept") {
                    Log.v(TAG, "${map["vote"]} = accept")
                    holder.acceptBtn.setColorFilter(Color.parseColor(green))
                    holder.rejectBtn.setColorFilter(Color.parseColor(grey))
                    holder.acceptBtn.isEnabled = false
                    holder.rejectBtn.isEnabled = false
                } else if (map["vote"] == "reject"){
                    Log.v(TAG, "${map["vote"]} != accept")
                    holder.acceptBtn.setColorFilter(Color.parseColor(grey))
                    holder.rejectBtn.setColorFilter(Color.parseColor(darkRed))
                    holder.acceptBtn.isEnabled = false
                    holder.rejectBtn.isEnabled = false
                }
            }
            // set color of vote icon
            if (map["vote"] == "accept") {
                Log.v(TAG, map["index"].toString())
                Log.v(TAG, holder.members[map["index"]].toString())
                holder.members[map["index"]]?.setColorFilter(Color.parseColor(green))
            } else if (map["vote"] == "reject") {
                Log.v(TAG, map["index"].toString())
                Log.v(TAG, holder.members[map["index"]].toString())
                holder.members[map["index"]]?.setColorFilter(Color.parseColor(red))
            }
        }

        holder.rejectBtn.setOnClickListener {
            Log.v(TAG, "group rejected:$group")
        }
        holder.acceptBtn.setOnClickListener {
//            var usersMap = group.users
//            usersMap[currentUser.uid]
//            db.collection("pendingGroups").document(group.pendingGroupId.toString()).update("users", FieldValue.arrayUnion())
            Log.v(TAG, "group accepted:$group")
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