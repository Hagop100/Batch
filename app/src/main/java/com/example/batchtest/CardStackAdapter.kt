package com.example.batchtest

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "CardStackAdapter";

// CardStackAdapter.kt
// returns a recycler view of cards for each group
// @params groups   list of potential groups to be matched
class CardStackAdapter (
    private val groups: ArrayList<MutableMap<String, Any>>
    ) : RecyclerView.Adapter<CardStackAdapter.CardStackHolder>() {
        // inflate parent fragment with card item layout when ViewHolder is created
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardStackHolder {
            // inflater from parent fragment
            val inflater = LayoutInflater.from(parent.context)
            // inflate parent using group card layout
            val cardItem: View = inflater.inflate(R.layout.match_group_card, parent, false)
            // pass into a holder to bind
            return CardStackHolder(cardItem)
        }
        // set group information to be displayed using holder once binded
        override fun onBindViewHolder(holder: CardStackHolder, position: Int) {
            val group = groups[position]
            Log.v(TAG, "${holder.name.text}")
            holder.name.text = "${group["name"]}"
            Log.v(TAG, "${holder.name.text}")
        }
        // returns number of groups
        override fun getItemCount(): Int {
            return groups.size
        }
        // holder class for each group card
        class CardStackHolder(view: View) : RecyclerView.ViewHolder(view) {
            // get the views
            val name: TextView = view.findViewById(R.id.card_name)
        }

    }
