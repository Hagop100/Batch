package com.example.batchtest

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.databinding.MatchGroupCardBinding
import org.w3c.dom.Text

private const val TAG = "CardStackAdapter"

// CardStackAdapter.kt
// returns a recycler view of cards for each group
// @params groups   list of potential groups to be matched
class CardStackAdapter (
    private val groups: ArrayList<MutableMap<String, Any>>,
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

            // pass into a holder to bind
            return CardStackHolder(binding)
        }
        // set group information to be displayed using holder once binded
        override fun onBindViewHolder(holder: CardStackHolder, position: Int) {
            val group = groups[position]
            holder.name.text = "${group["name"]}"
            holder.biscuits.text = "${group["biscuits"]}"
            holder.description.text = "${group["aboutUsDescription"]}"
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
        }

    // match tab fragment listens to when undo or more button is clicked
    interface CardStackAdapterListener {
        fun onUndoBtnClick()
        fun onMoreBtnClick()
    }
    }
