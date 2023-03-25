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
import com.bumptech.glide.Glide
import com.example.batchtest.Group
import com.example.batchtest.PendingGroup
import com.example.batchtest.R
import com.example.batchtest.databinding.VoteGroupCardBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

private const val TAG = "PendingGroupAdapter"

class PendingGroupAdapter(
    private val context: Context?,
    private val groups: ArrayList<PendingGroup>,
    ) : RecyclerView.Adapter<PendingGroupAdapter.PendingGroupHolder>() {
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
            val matchingGroupImg: CircleImageView = binding.myGroupImg
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

            val matchingGroupObj = group.matchingGroupObj
            val pendingGroupObj = group.pendingGroupObj

            if (matchingGroupObj != null) {
                if (matchingGroupObj.image == null || matchingGroupObj.image == "") {
                    holder.matchingGroupImg.setImageResource(R.drawable.placeholder)
                } else {
                    if (context != null) {
                        Glide.with(context).load(matchingGroupObj.image)
                            .into(holder.matchingGroupImg)
                    }
                }
            }

            // set pending group information
            if (pendingGroupObj != null) {
                holder.pendingGroupName.text = pendingGroupObj.name
                if (pendingGroupObj.image == null || pendingGroupObj.image == "") {
                    holder.pendingGroupImg.setImageResource(R.drawable.placeholder)
                } else {
                    if (context != null) {
                        Glide.with(context).load(pendingGroupObj.image)
                            .into(holder.pendingGroupImg)
                    }
                }
            }

            group.users?.forEach { (user, map) ->
                // set color for the buttons for the current user
                if (user == currentUser?.uid) {
                    if (map["vote"] == "accept") {
                        toggleButtonUI(holder, true)
                    } else if (map["vote"] == "reject"){
                        toggleButtonUI(holder, false)
                    }
                }
                holder.members[map["index"]]?.isVisible = true
                // set color of vote icon
                if (map["vote"] == "accept") {
                    setIconColor(holder, map["index"].toString(), green)
                } else if (map["vote"] == "reject") {
                    setIconColor(holder, map["index"].toString(), red)
                }

            }

            holder.rejectBtn.setOnClickListener {
                rejectPendingGroup(group, holder)
            }
            holder.acceptBtn.setOnClickListener {
                acceptPendingGroup(group, holder)
            }
        }

        private fun acceptPendingGroup(group: PendingGroup, holder: PendingGroupHolder) {
            val id = currentUser?.uid
            val index = group.users?.get(id)?.get("index")
            db.collection("pendingGroups").document(group.pendingGroupId.toString())
                .update("users.$id.vote", "accept")
                .addOnSuccessListener {
                    toggleButtonUI(holder, true)
                    setIconColor(holder, index.toString(), green)
                    voting(group)
                }

        }

        private fun rejectPendingGroup(group: PendingGroup, holder: PendingGroupHolder) {
            val id = currentUser?.uid
            val index = group.users?.get(id)?.get("index")
            db.collection("pendingGroups").document(group.pendingGroupId.toString())
                .update("users.$id.vote", "reject")
                .addOnSuccessListener {
                    toggleButtonUI(holder, false)
                    setIconColor(holder, index.toString(), red)
                    voting(group)
                }

        }

        private fun toggleButtonUI(holder: PendingGroupHolder, accept:Boolean) {
            val acceptBtnColor: String
            val rejectBtnColor: String
            if (accept) {
                acceptBtnColor = green
                rejectBtnColor = grey
            } else {
                acceptBtnColor = grey
                rejectBtnColor = darkRed
            }
            holder.acceptBtn.setColorFilter(Color.parseColor(acceptBtnColor))
            holder.rejectBtn.setColorFilter(Color.parseColor(rejectBtnColor))
            holder.acceptBtn.isEnabled = false
            holder.rejectBtn.isEnabled = false
        }

        private fun setIconColor(holder: PendingGroupHolder, index: String, color: String) {
            holder.members[index]?.setColorFilter(Color.parseColor(color))
        }


        private fun voting(pendingGroup: PendingGroup) {
            var acceptCount = 0
            var rejectCount = 0
            val memberCount = pendingGroup.users?.size
            if (memberCount != null && memberCount > 0) {
                pendingGroup.users.forEach { (_, map) ->
                    if (map["vote"] == "accept") {
                        acceptCount++
                    } else if (map["vote"] == "reject") {
                        rejectCount++
                    }
                }
                if ((acceptCount.toFloat() / memberCount.toFloat()) >= .5F) {
                    db.collection("pendingGroup").document(pendingGroup.pendingGroupId.toString())
                        .update("pending", false)
                    Log.v(TAG, "matched")
                } else if ((rejectCount.toFloat() / memberCount.toFloat()) > .5F) {
                    Log.v(TAG, "not matched")
                }
                TODO("if two members automatically match")
            }
        }
        // returns number of pending groups
        override fun getItemCount(): Int {
            return groups.size
        }
    }