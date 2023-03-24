package com.example.batchtest.GroupChat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.Message
import com.example.batchtest.OtherGroupsTab.MatchedGroups.MatchedGroupAdapter
import com.example.batchtest.databinding.MatchedGroupRecyclerViewRowBinding
import com.example.batchtest.databinding.MeMessageRecyclerViewRowBinding
import com.example.batchtest.databinding.ThemMessageRecyclerViewRowBinding
import com.google.android.gms.common.util.DataUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class GroupChatAdapter(private var mMessageList: ArrayList<Message>,
                       private var mContext: Context,): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ReceivedMessageHolder(val binding: ThemMessageRecyclerViewRowBinding): RecyclerView.ViewHolder(binding.root) {
        val messageContent = binding.gcMessageContentTv
        val timeText = binding.gcTimeStampTv
        val nameText = binding.gcUsernameTv
        val date = binding.gcDateTv
        val profileImage = binding.gcProiflePicIv

        @SuppressLint("SetTextI18n")
        fun bind(message: Message) {
            messageContent.text = message.content
            timeText.text = message.createdDate?.hours.toString() + ":" + message.createdDate?.minutes.toString()
            nameText.text = message.username
            if(mMessageList.size > 0 &&
                mMessageList[mMessageList.size - 1].createdDate?.month == message.createdDate?.month &&
                mMessageList[mMessageList.size - 1].createdDate?.date == message.createdDate?.date) {
                date.visibility = View.GONE
            }
            else {
                date.visibility = View.VISIBLE
                date.text = (message.createdDate?.month?.plus(1)).toString() + "/" + message.createdDate?.date.toString()
            }
            //profileImage.setImageURI(message.user.imageUri)
        }
    }

    inner class SentMessageHolder(val binding: MeMessageRecyclerViewRowBinding): RecyclerView.ViewHolder(binding.root) {
        val messageContent = binding.gcMeMessageContentTv
        val timeText = binding.gcMeTimeStampTv
        val date = binding.gcMeDateTv

        @SuppressLint("SetTextI18n")
        fun bind(message: Message) {
            messageContent.text = message.content
            timeText.text = message.createdDate?.hours.toString() + ":" + message.createdDate?.minutes.toString()
            if(mMessageList.size > 0 &&
                mMessageList[mMessageList.size - 1].createdDate?.month == message.createdDate?.month &&
                mMessageList[mMessageList.size - 1].createdDate?.date == message.createdDate?.date) {
                date.visibility = View.GONE
            }
            else {
                date.visibility = View.VISIBLE
                date.text = (message.createdDate?.month?.plus(1)).toString() + "/" + message.createdDate?.date.toString()
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        val message = mMessageList[position]

        if(message.username == Firebase.auth.currentUser?.email) {
            return 0
        }
        else {
            return 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if(viewType == 0) {
            val inflater = LayoutInflater.from(parent.context)
            val binding = MeMessageRecyclerViewRowBinding.inflate(inflater, parent, false)
            SentMessageHolder(binding)
        } else {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ThemMessageRecyclerViewRowBinding.inflate(inflater, parent, false)
            ReceivedMessageHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = mMessageList[position]

        if(holder.itemViewType == 0) {
            (holder as SentMessageHolder).bind(currentMessage)
        }
        else if(holder.itemViewType == 1) {
            (holder as ReceivedMessageHolder).bind(currentMessage)
        }
    }

    override fun getItemCount(): Int {
        return mMessageList.size
    }

}