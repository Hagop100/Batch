package com.example.batchtest.myGroupsTab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.R
import java.util.*

class JoinGroupAdapter(private val itemList: MutableList<String>) :
    RecyclerView.Adapter<JoinGroupAdapter.MyViewHolder>(), Filterable {

    private var filteredList: MutableList<String> = itemList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.join_group_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = filteredList[position]
        holder.bind(item)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchText = constraint?.toString()?.toLowerCase(Locale.ROOT)
                filteredList = if (searchText.isNullOrEmpty()) {
                    itemList
                } else {
                    itemList.filter {
                        it.toLowerCase(Locale.ROOT).contains(searchText)
                    }.toMutableList()
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as MutableList<String>
                notifyDataSetChanged()
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupName: TextView = itemView.findViewById(R.id.group_name)

        fun bind(item: String) {
            groupName.text = item
        }
    }
}