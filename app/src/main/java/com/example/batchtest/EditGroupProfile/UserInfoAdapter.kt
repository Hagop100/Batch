package com.example.batchtest.EditGroupProfile

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.R
import com.example.batchtest.User
import de.hdodenhof.circleimageview.CircleImageView


class UserInfoAdapter(
    private val context: Context,
    private val userList: ArrayList<User>,
    private val listener: UserInfoListener) : RecyclerView.Adapter<UserInfoAdapter.ViewHolder>()
{

    //inflate the list item user layout to display user info
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false)
        return ViewHolder(itemView)
    }

    //display user info in position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        holder.userCard.setOnClickListener {
            listener.onItemClick(user.email.toString())
        }

        // set display name of user if exists
        if (user.getName().trim() != "") {
            holder.userNameTextView.text = user.getName()
            holder.userNameTextView.setTypeface(null, Typeface.NORMAL)
        } else {
            holder.userNameTextView.setTypeface(null, Typeface.ITALIC)
        }


        //if user does not change the default picture. then set the default as user pic
        if (userList[position].imageUrl == null){
            holder.userPic.setImageResource(R.drawable.placeholder)
        }
        else{
            Glide.with(context).load(userList[position].imageUrl).into(holder.userPic)
        }

    }

    //get the size of the list of user according to firebase
    override fun getItemCount() = userList.size

    //bind the values
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userCard: CardView = itemView.findViewById(R.id.group_card_view)
        val userNameTextView: TextView = itemView.findViewById(R.id.user_name)
        val userPic : CircleImageView = itemView.findViewById(R.id.user_profile)
//        val userPic : CircleImageView = itemView.findViewById(R.id.group_profile)
    }

    // listener will listen whenever user is clicked to navigate to user display page
    interface UserInfoListener {
        fun onItemClick(userId: String)
    }
}