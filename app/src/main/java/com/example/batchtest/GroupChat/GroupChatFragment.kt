package com.example.batchtest.GroupChat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.batchtest.Group
import com.example.batchtest.Message
import com.example.batchtest.OtherGroupsTab.MatchedGroups.MatchedGroupAdapter
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentGroupChatBinding

class GroupChatFragment : Fragment() {

    //binding variables
    private var _binding: FragmentGroupChatBinding? = null
    private val binding get() = _binding!!

    //ArrayList for messages
    private var messagesArrayList: ArrayList<Message> = ArrayList<Message>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)

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

}