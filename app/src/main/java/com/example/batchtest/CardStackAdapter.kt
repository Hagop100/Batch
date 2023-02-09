package com.example.batchtest

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.databinding.MatchGroupCardBinding
import com.google.android.flexbox.FlexboxLayout


private const val TAG = "CardStackAdapter"

// CardStackAdapter.kt
//  the adapter receives a list of groups from the parent fragment and sends to the holder.
//  the holder will bind the views that will be recycled and change the text according to
//  group being displayed. the fragment uses the adapter to display data in a recycler view
//
// returns a recycler view of cards for each group
// @params groups   list of potential groups to be matched
class CardStackAdapter(
    private val groups: ArrayList<Group>,
    private val listener: CardStackAdapterListener
    ) : RecyclerView.Adapter<CardStackAdapter.CardStackHolder>() {
        // inflate parent fragment with card item layout when ViewHolder is created
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardStackHolder {
            // inflater from parent fragment
            val inflater = LayoutInflater.from(parent.context)

            // inflate parent using group card layout
            val binding = MatchGroupCardBinding.inflate(inflater, parent, false)

            // undo on click listener
            binding.undoBtn.setOnClickListener {
                listener.onUndoBtnClick()
            }

            binding.matchMoreBtn.setOnClickListener {
                listener.onMoreBtnClick(parent.context)
            }
            // pass into a holder to bind
            return CardStackHolder(binding)
        }
        // set group information to be displayed using holder once bound
        override fun onBindViewHolder(holder: CardStackHolder, position: Int) {
            // create recycle layout for group
            val group = groups[position]
            // get interest tags of group
            val tags = group.interestTags
            // set group name
            holder.name.text = group.name
            // set biscuit value
            holder.biscuits.text = group.biscuits.toString()
            // set group description
            holder.description.text = group.aboutUsDescription

            // inflate the interest tag container
            val inflater: LayoutInflater = LayoutInflater.from(holder.interestTags.context)
            var interestTag: TextView
            // set the layout parameter and margins for interest tag
            val params: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(10, 10, 10, 10)
            // dynamically add group tags
            for (tag in tags!!) {
                // use existing interest tag layout
                interestTag = inflater.inflate(R.layout.interest_tag, holder.interestTags, false) as TextView
                // set text of tag
                interestTag.text = tag
                // set the defined layout parameters
                interestTag.layoutParams = params
                // add the interest tag text view to the layout
                holder.interestTags.addView(interestTag)
            }
        }
        // returns number of groups
        override fun getItemCount(): Int {
            return groups.size
        }
        // holder class for each group card
        class CardStackHolder(val binding: MatchGroupCardBinding) : RecyclerView.ViewHolder(binding.root) {
            // get the views
            val name: TextView = binding.groupName
            val biscuits: TextView = binding.biscuitValue
            val description: TextView = binding.aboutUsDescription
            val interestTags: FlexboxLayout = binding.interestTags
        }

    // match tab fragment listens to when undo or more button is clicked
    interface CardStackAdapterListener {
        fun onUndoBtnClick()
        fun onMoreBtnClick(context: Context)
    }
}
