package com.example.batchtest.UserProfileTab.userPage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.databinding.ListItemUserBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "ViewUserInfoAdapter"

class ViewUserInfoAdapter(
    private val context: Context?,
    private val groups: ArrayList<String>,
    private val listener: GroupProfileViewEvent
) : RecyclerView.Adapter<ViewUserInfoAdapter.ViewUserInfoHolder>() {
    private val db = Firebase.firestore

    // holder class for group
    class ViewUserInfoHolder(val binding: ListItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        val groupPicture = binding.userProfile
        val groupName = binding.userName
        val groupCardView = binding.groupCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewUserInfoHolder {
        // inflater from parent fragment
        val inflater = LayoutInflater.from(parent.context)

        // inflate parent using group list layout
        val binding = ListItemUserBinding.inflate(inflater, parent, false)

        // pass binding into the holder
        return ViewUserInfoHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewUserInfoHolder, position: Int) {
        val groupName = groups[position]
        // when group is clicked, call the onItemClick function in the listener
        // the onItemClick will display the group page
        holder.groupCardView.setOnClickListener {
            listener.onItemClick(groupName)
        }
        db.collection("groups").document(groupName).get()
            .addOnSuccessListener {
                val groupObj = it.toObject(Group::class.java)
                if (groupObj != null) {
                    holder.groupName.text = groupObj.name
                    if (groupObj.image.isNullOrEmpty()) {
                        holder.groupPicture.setImageResource(R.drawable.placeholder)
                    } else {
                        if (context != null) {
                            Glide.with(context).load(groupObj.image).into(holder.groupPicture)
                        }
                    }
                }
            }
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    // listener for when group is clicked to display group profile
    interface GroupProfileViewEvent {
        fun onItemClick(groupName: String)
    }
}