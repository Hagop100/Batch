package com.example.batchtest.MatchTab

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.databinding.InterestTagBinding
import com.example.batchtest.databinding.MatchGroupCardBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*
import kotlin.collections.ArrayList


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
        private lateinit var binding: MatchGroupCardBinding
        // inflate parent fragment with card item layout when ViewHolder is created
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardStackHolder {
            // inflater from parent fragment
            val inflater = LayoutInflater.from(parent.context)

            // inflate parent using group card layout
            binding = MatchGroupCardBinding.inflate(inflater, parent, false)

            // more button on match page opens dialog
            binding.matchMoreBtn.setOnClickListener {
                // create a bottom sheet dialog
                val dialog = BottomSheetDialog(parent.context)
                // inflate the view with the dialog linear layout
                val view:LinearLayout = LayoutInflater.from(parent.context).inflate(R.layout.dialog_layout, binding.root, false) as LinearLayout

                // create block dialog button to add into the dialog layout dynamically
                // using the dialog button layout
                // inflate a text view to hold the block dialog
                val blockDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, null, false) as TextView
                blockDialogBtn.text = "Block Group"
                // perform action on click
                blockDialogBtn.setOnClickListener {
                    // add block group logic
                    dialog.dismiss()
                }
                // add the block dialog button to the bottom dialog view
                view.addView(blockDialogBtn)

                // inflate a text view to hold the block dialog
                val reportDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, null, false) as TextView
                reportDialogBtn.text = "Report Group"
                // perform action on click
                reportDialogBtn.setOnClickListener {
                    // add block group logic
                    dialog.dismiss()
                }
                // add the report dialog button to the bottom dialog view
                view.addView(reportDialogBtn)

                // set the view of the dialog using the inflated layout
                dialog.setContentView(view)
                // show the dialog
                dialog.show()
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

            // undo on click listener
            binding.undoBtn.setOnClickListener {
                listener.onUndoBtnClick()
            }

            // accept button accepts group when clicked
            binding.acceptBtn.setOnClickListener{
                listener.onAcceptBtnClick(group.name!!)
            }

            // reject button rejects group when clicked
            binding.rejectBtn.setOnClickListener{
                listener.onRejectBtnClick(group.name!!)
            }
        }
        // returns number of groups
        override fun getItemCount(): Int {
            return groups.size
        }
        // holder class for each group card
        class CardStackHolder(val binding: MatchGroupCardBinding) : RecyclerView.ViewHolder(binding.root) {
            // get the views of card
            // name of group
            val name: TextView = binding.groupName
            // amount of biscuits
            val biscuits: TextView = binding.biscuitValue
            // description of group
            val description: TextView = binding.aboutUsDescription
            // interest tags of groups
            val interestTags: FlexboxLayout = binding.interestTags
        }

    // match tab fragment listens to when undo or more button is clicked
    interface CardStackAdapterListener {
        fun onUndoBtnClick()
        fun onAcceptBtnClick(groupName:String)
        fun onRejectBtnClick(groupName:String)
    }
}