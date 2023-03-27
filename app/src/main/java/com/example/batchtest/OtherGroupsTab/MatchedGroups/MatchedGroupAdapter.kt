package com.example.batchtest.OtherGroupsTab.MatchedGroups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.Group
import com.example.batchtest.MainActivity
import com.example.batchtest.R
import com.example.batchtest.databinding.MatchedGroupRecyclerViewRowBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class MatchedGroupAdapter(private val matchedGroupList: ArrayList<String>,
                          private val listener: MatchedGroupRecyclerViewEvent,
                          private var mContext: Context): RecyclerView.Adapter<MatchedGroupAdapter.MatchedGroupViewHolder>() {

    inner class MatchedGroupViewHolder(val binding: MatchedGroupRecyclerViewRowBinding):
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val card: CardView = binding.matchedGroupRecyclerViewRowCardView
        val groupName: TextView = binding.matchedGroupRecycleViewRowGroupName
        //This needs to be fixed
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
        //holder.card.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.slide_in_from_right)
        val currentItem = matchedGroupList[position]
        holder.groupName.text = currentItem
        setGroupPhoto(holder, position)
    }

    override fun getItemCount(): Int {
        return matchedGroupList.size
    }

    private fun setGroupPhoto(holder: MatchedGroupViewHolder, position: Int) {
        var group: Group? = null
        val db = Firebase.firestore
        val currentItem = matchedGroupList[position]
        db.collection("groups").document(currentItem)
            .get()
            .addOnSuccessListener { doc ->
                group = doc.toObject<Group>()
                Glide.with(mContext).load(group?.image).into(holder.groupPhoto)
            }
    }

    /*
    Interface used to have buttons in recycler views be clicked
     */
    interface MatchedGroupRecyclerViewEvent {
        fun onItemClick(position: Int)
    }

}