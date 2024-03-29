package com.example.batchtest.myGroupsTab

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.Group
import com.example.batchtest.R
import de.hdodenhof.circleimageview.CircleImageView

/**
 * bridge the communication between MyGroupFragment and GroupCreationFragment.
 * allows information that was generated from Group Creation and set into view in My Group listing.
 */
class MyGroupAdapter(
    val context: Context,
    private val listener: GroupProfileViewEvent,
    private val groupNameList: ArrayList<Group>,
    private val mutedGroupList: ArrayList<String>,
    private var primaryGroup: String)
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

        // make mute icon visible if the muted group list contains the group
        holder.muteBtn.isVisible = mutedGroupList.contains(info.name)

        //make primary icon visible if primary group is set
        //updates when the page is refreshed
        holder.primaryBtn.isVisible = primaryGroup.equals(info.name)

    }

    //It returns the count of items present in the list.
    override fun getItemCount(): Int {
        return groupNameList.size
    }

    //update the primary group icon
    fun primaryGroupUpdate(newItem: String){
        primaryGroup = newItem
        notifyItemChanged(0)


    }

    //set the initial values for the view
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val groupName : TextView = itemView.findViewById(R.id.group_list_name)
        val aboutUs: TextView = itemView.findViewById(R.id.group_list_des)
        val groupPic : CircleImageView = itemView.findViewById(R.id.group_profile)
        val groupCardView: CardView = itemView.findViewById(R.id.group_card_view)
        val muteBtn: ImageView = itemView.findViewById(R.id.mute_icon)
        val primaryBtn: ImageView = itemView.findViewById(R.id.primary_icon)

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
//                listener.onCardViewClick(position)

            }

        }
    }

    //functions that are implemented in the MyGroupFragment for navigation
    interface GroupProfileViewEvent{
        fun onItemClick(position: Int) //group profile
        fun onCardViewClick(position: Int) //group chat
    }




}







