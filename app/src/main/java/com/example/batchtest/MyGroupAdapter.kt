package com.example.batchtest

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.withContext
import org.w3c.dom.Text

/**
 * bridge the communication between MyGroupFragment and GroupCreationFragment.
 * allows information that was generated from Group Creation and set into view in My Group listing.
 */
class MyGroupAdapter(private val context: Context, private val groupNameList: ArrayList<Group>): RecyclerView.Adapter<MyGroupAdapter.MyViewHolder>() {
    //inflate the content of the card view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_group_list, parent, false)
        return MyViewHolder(itemView)
    }

    //This function is used to bind the list items to our widgets such as TextView, ImageView, etc.
    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: MyGroupAdapter.MyViewHolder, position: Int) {

        val info: Group = groupNameList[position]
        holder.groupName.text = info.name
        holder.aboutUs.text = info.aboutUsDescription
        Glide.with(context).load(groupNameList[position].image).into(holder.groupPic)

    }

    //It returns the count of items present in the list.
    override fun getItemCount(): Int {
        return groupNameList.size
    }

    //set the initial values for the view
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val groupName : TextView = itemView.findViewById(R.id.group_list_name)
        val aboutUs: TextView = itemView.findViewById(R.id.group_list_des)
        val groupPic : CircleImageView = itemView.findViewById(R.id.group_profile)

    }


}




