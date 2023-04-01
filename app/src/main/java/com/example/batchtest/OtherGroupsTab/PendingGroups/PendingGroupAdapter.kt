package com.example.batchtest.OtherGroupsTab.PendingGroups

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.batchtest.Chat
import com.example.batchtest.Message
import com.example.batchtest.PendingGroup
import com.example.batchtest.R
import com.example.batchtest.databinding.VoteGroupCardBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "PendingGroupAdapter"

class PendingGroupAdapter(
    private val context: Context?,
    private val groups: ArrayList<PendingGroup>,
    ) : RecyclerView.Adapter<PendingGroupAdapter.PendingGroupHolder>() {
        private val db = Firebase.firestore
        // get the authenticated logged in user
        private val currentUser = Firebase.auth.currentUser
        // colors for ui based on vote state
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
                if (matchingGroupObj.image.isNullOrEmpty()) {
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
            // handle reject button logic
            holder.rejectBtn.setOnClickListener {
                rejectPendingGroup(group, holder)
            }
            // handle accept button logic
            holder.acceptBtn.setOnClickListener {
                acceptPendingGroup(group, holder)
            }
        }
        // update current users vote to accept in database and update ui of vote card
        private fun acceptPendingGroup(group: PendingGroup, holder: PendingGroupHolder) {
            val id = currentUser?.uid
            val index = group.users?.get(id)?.get("index")
            db.collection("pendingGroups").document(group.pendingGroupId.toString())
                .update("users.$id.vote", "accept")
                .addOnSuccessListener {
                    toggleButtonUI(holder, true)
                    setIconColor(holder, index.toString(), green)
                    voting(db, group)
                }

        }
        // update current users vote to reject in database and update ui of vote card
        private fun rejectPendingGroup(group: PendingGroup, holder: PendingGroupHolder) {
            val id = currentUser?.uid
            val index = group.users?.get(id)?.get("index")
            db.collection("pendingGroups").document(group.pendingGroupId.toString())
                .update("users.$id.vote", "reject")
                .addOnSuccessListener {
                    toggleButtonUI(holder, false)
                    setIconColor(holder, index.toString(), red)
                    voting(db, group)
                }

        }
        // toggle button user interface based on user's vote
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
        // color of icon
        private fun setIconColor(holder: PendingGroupHolder, index: String, color: String) {
            holder.members[index]?.setColorFilter(Color.parseColor(color))
        }

    // returns number of pending groups
        override fun getItemCount(): Int {
            return groups.size
        }

    companion object {
        // voting algorithm runs when a user votes to check if a majority votes has occurred
        // if a majority vote (accept or reject) has occurred, then check if both groups have
        // decided to match. if one group rejects, then delete the pending group from the db
        // if voting is still pending, do nothing
        fun voting(db: FirebaseFirestore, pendingGroup: PendingGroup) {
            var acceptCount = 0
            var rejectCount = 0
            val memberCount = pendingGroup.users?.size
            if (memberCount != null && memberCount > 0) {
                Log.v(TAG, "voting")
                pendingGroup.users.forEach { (_, map) ->
                    if (map["vote"] == "accept") {
                        acceptCount++
                    } else if (map["vote"] == "reject") {
                        rejectCount++
                    }
                }
                // if majority of group members vote to accept the group:
                // update pending to be false so the pending group will not be displayed anymore
                // update matched to be true indicating the group accepted the group
                // else if vote to reject the group:
                // remove the pending group from the database
                if ((acceptCount.toFloat() / memberCount.toFloat()) >= .5F) {
                    db.collection("pendingGroups").document(pendingGroup.pendingGroupId.toString())
                        .update("pending", false, "matched", true)
                        .addOnSuccessListener {
                            // once updated, fetch the pending group for the other group to see if they had
                            // voted to accept or reject the group if both groups have accepted, then match
                            db.collection("pendingGroups")
                                .whereEqualTo("matchingGroup", pendingGroup.pendingGroup) // the pending group will be the matching group
                                .whereEqualTo("pendingGroup", pendingGroup.matchingGroup) // the matching group will be the pending group
                                .get()
                                .addOnSuccessListener {
                                    if (it.documents.isNotEmpty()) {
                                        val otherPendingGroup =
                                            it.documents[0].toObject(PendingGroup::class.java)
                                        // check if they had voted to match
                                        if (otherPendingGroup != null) {
                                            if (otherPendingGroup.matched == true) {
                                                // add to matched groups of both matched groups
                                                db.collection("groups").document(otherPendingGroup.pendingGroup.toString())
                                                    .update("matchedGroups", FieldValue.arrayUnion(otherPendingGroup.matchingGroup))
                                                db.collection("groups").document(otherPendingGroup.matchingGroup.toString())
                                                    .update("matchedGroups", FieldValue.arrayUnion(otherPendingGroup.pendingGroup))
                                                // add the user's group to the matched groups of all users in the other pending group
                                                otherPendingGroup.users?.forEach { (key, _) ->
                                                    db.collection("users").document(key)
                                                        .update(
                                                            "matchedGroups",
                                                            FieldValue.arrayUnion(otherPendingGroup.pendingGroup)
                                                        )
                                                        .addOnFailureListener { e ->
                                                            Log.v(
                                                                TAG,
                                                                "error adding current group to matched group of user:",
                                                                e
                                                            )
                                                        }
                                                }
                                                // add the other group to the matched groups of all users in the user's group
                                                pendingGroup.users.forEach { (key, _) ->
                                                    db.collection("users").document(key)
                                                        .update(
                                                            "matchedGroups",
                                                            FieldValue.arrayUnion(pendingGroup.pendingGroup)
                                                        )
                                                        .addOnFailureListener { e ->
                                                            Log.v(
                                                                TAG,
                                                                "error adding other group to matched group of user:",
                                                                e
                                                            )
                                                        }
                                                }
                                                // create a chat object
                                                val chat = Chat(
                                                    0,
                                                    arrayListOf(),
                                                    otherPendingGroup.matchingGroup,
                                                    otherPendingGroup.pendingGroup,
                                                    Date()
                                                )
                                                // add chat to db
                                                db.collection("chats").add(chat)
                                                    .addOnFailureListener { e ->
                                                        Log.v(
                                                            TAG,
                                                            "error adding chat for groups ${otherPendingGroup.matchingGroup} and ${otherPendingGroup.pendingGroup}",
                                                            e
                                                        )
                                                    }
                                                // delete both pending groups from db
                                                db.collection("pendingGroups").document(pendingGroup.pendingGroupId.toString()).delete()
                                                db.collection("pendingGroups").document(otherPendingGroup.pendingGroupId.toString()).delete()
                                            }
                                        }
                                    }
                                }
                        }
                } else if ((rejectCount.toFloat() / memberCount.toFloat()) > .5F) {
                    // remove the pending group from the database which will add the group back to the card stack
                    db.collection("pendingGroups")
                        .document(pendingGroup.pendingGroupId.toString())
                        .delete()
                }
            }
        }
    }
}