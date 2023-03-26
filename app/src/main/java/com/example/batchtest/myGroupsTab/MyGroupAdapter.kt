package com.example.batchtest.myGroupsTab

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.Group
import com.example.batchtest.R
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

/**
 * bridge the communication between MyGroupFragment and GroupCreationFragment.
 * allows information that was generated from Group Creation and set into view in My Group listing.
 */
class MyGroupAdapter(
    private val context: Context,
    private val listener: GroupProfileViewEvent,
    private val groupNameList: ArrayList<Group>)
    : RecyclerView.Adapter<MyGroupAdapter.MyViewHolder>() {

    //inflate the content of the card view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_group_list, parent, false)
        return MyViewHolder(itemView)
    }

    //This function is used to bind the list items to our widgets such as TextView, ImageView, etc.
    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val info: Group = groupNameList[position]
        holder.groupName.text = info.name
        holder.aboutUs.text = info.aboutUsDescription


        //if user does not change the default picture. then set the default as group pic
        if (groupNameList[position].image.isNullOrEmpty()){
            holder.groupPic.setImageResource(R.drawable.placeholder)
        }
        else{
            Glide.with(context).load(groupNameList[position].image).into(holder.groupPic)
        }

        /**
         * clicks on the image at position n to view group profile
         */

//        holder.groupPic.setOnClickListener{ view ->
//
//            Toast.makeText(this.context, "Testing here ${info.name}", Toast.LENGTH_SHORT).show()
//
//            findNavController(holder.groupName).navigate(R.id.action_myGroupFragment_to_viewGroupInfoFragment)
//
//        }
        //set on click action to move to the group chat - implemented in the MyGroupFragment with override function
        holder.groupCardView.setOnClickListener{
            listener.onCardViewClick(position)
        }

    }

    //It returns the count of items present in the list.
    override fun getItemCount(): Int {
        return groupNameList.size
    }


    //set the initial values for the view
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val groupName : TextView = itemView.findViewById(R.id.group_list_name)
        val aboutUs: TextView = itemView.findViewById(R.id.group_list_des)
        val groupPic : CircleImageView = itemView.findViewById(R.id.group_profile)
        val groupCardView: CardView = itemView.findViewById(R.id.group_card_view)

        init {
            groupPic.setOnClickListener(this)
            groupCardView.setOnClickListener(this)
        }

        //extract the position of the group pic
        override fun onClick(v: View?) {
            val position = absoluteAdapterPosition

            //check if the current position is valid
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }

        }
    }

    //functions that are implemented in the MyGroupFragment for navigation
    interface GroupProfileViewEvent{
        fun onItemClick(position: Int)
        fun onCardViewClick(position: Int)
    }




}







