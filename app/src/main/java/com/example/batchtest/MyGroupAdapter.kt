package com.example.batchtest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.w3c.dom.Text

/**
 * bridge the communication between MyGroupFragment and GroupCreationFragment.
 * allows information that was generated from Group Creation and set into view in My Group listing.
 */
class MyGroupAdapter(private val groupNameList: ArrayList<Group>): RecyclerView.Adapter<MyGroupAdapter.MyViewHolder>() {
    //inflate the content of the card view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_group_list, parent, false)
        return MyViewHolder(itemView)
    }

    //holder that helps to display the selected information on the card view.
    override fun onBindViewHolder(holder: MyGroupAdapter.MyViewHolder, position: Int) {

        val info: Group = groupNameList[position]
        holder.groupName.text = info.name
        holder.aboutUs.text = info.aboutUsDescription
    }

    //returns the number of groups on the list so the view can inflate accordingly.
    override fun getItemCount(): Int {
        return groupNameList.size
    }

    //set the initial values for the group name and description
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val groupName : TextView = itemView.findViewById(R.id.group_list_name)
        val aboutUs: TextView = itemView.findViewById(R.id.group_list_des)

    }


}

