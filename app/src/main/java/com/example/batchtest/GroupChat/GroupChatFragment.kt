package com.example.batchtest.GroupChat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.batchtest.*
import com.example.batchtest.OtherGroupsTab.MatchedGroups.MatchedGroupAdapter
import com.example.batchtest.databinding.FragmentGroupChatBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class GroupChatFragment : Fragment() {

    //binding variables
    private var _binding: FragmentGroupChatBinding? = null
    private val binding get() = _binding!!

    //ArrayList for messages
    private var messagesArrayList: ArrayList<Message> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)

        val db = Firebase.firestore
        val group1 = "Big Chungus"
        val group2 = "Batch test"

        val message1 = Message("Hello", "kylebatch491b@gmail.com", Date())
        val message2 = Message("Welcome", "goofy", Date())
        messagesArrayList.add(message1)
        messagesArrayList.add(message2)
        val chat = Chat(0, messagesArrayList, group1, group2, Date())

        db.collection("chats").add(chat)
            .addOnSuccessListener { doc ->

            }
            .addOnFailureListener {e ->

            }

        val groupChatRV = binding.fragmentGroupChatRecyclerView
        groupChatRV.layoutManager = LinearLayoutManager(context)
        groupChatRV.setHasFixedSize(true)
        // attach adapter and send groups
        val groupChatAdapter = GroupChatAdapter(messagesArrayList, requireActivity())
        groupChatRV.adapter = groupChatAdapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GroupChatFragment"
    }

}