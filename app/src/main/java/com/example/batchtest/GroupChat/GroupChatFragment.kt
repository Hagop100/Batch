package com.example.batchtest.GroupChat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.*
import com.example.batchtest.OtherGroupsTab.MatchedGroups.MatchedGroupAdapter
import com.example.batchtest.OtherGroupsTab.MatchedGroups.MatchedGroupFragment
import com.example.batchtest.databinding.FragmentGroupChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
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

    //groups that are a part of this chat
    private var myGroupNames: ArrayList<String> = ArrayList()
    private lateinit var theirGroupName: String

    //Firebase auth
    private var currUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currUser = Firebase.auth.currentUser
        // Use the Kotlin extension in the fragment-ktx artifact
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)

        val db = Firebase.firestore

        val groupChatRV = binding.fragmentGroupChatRecyclerView
        //This will set the recyclerview layout to be like a chat
        //Starting bottom moving up
        groupChatRV.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
                reverseLayout = false
            }
        }
        //groupChatRV.layoutManager = LinearLayoutManager(context)
        groupChatRV.setHasFixedSize(true)

        //This will receive the information of which group you clicked on from the matchedGroupFragment
        val result = arguments?.getString("groupName")
        theirGroupName = result!!
        Log.i(TAG, theirGroupName)

        //--------------------------------------------------
        //QUERY THE CHAT FROM FIRESTORE!!!!!!!!!!!!!!!!!!!!!
        queryChatFromFirestore(db, groupChatRV)
        //QUERY THE CHAT FROM FIRESTORE!!!!!!!!!!!!!!!!!!!!!
        //--------------------------------------------------

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun queryChatFromFirestore(db: FirebaseFirestore, groupChatRV: RecyclerView) {
        var user: User? = null
        val userDocRef = currUser?.let { db.collection("users").document(it.uid) }
        userDocRef?.get()?.addOnSuccessListener { doc ->
            if (doc != null) {
                user = doc.toObject<User>()
                for(myGroups in user?.myGroups!!) {
                    var group: Group? = null
                    val groupDocRef = db.collection("groups").document(myGroups)
                    groupDocRef.get().addOnSuccessListener { doc ->
                        group = doc.toObject<Group>()
                        if(group?.matchedGroups?.contains(theirGroupName) == true) {
                            myGroupNames.add(group?.name!!)
                        }
                        if(myGroupNames.size > 0) {
                            //Decision must happen here which groupChat they want to open!
                            //For now we will select the first index
                            Log.i(TAG, myGroupNames[0])
                            getChat(db, myGroupNames[0], groupChatRV)
                        }
                    }
                }
            } else {
                Log.i(TAG, "no such doc")
            }
        }
            ?.addOnFailureListener { e ->
                Log.i(TAG, "get failed with ", e)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getChat(db: FirebaseFirestore, myGroupName: String, groupChatRV: RecyclerView) {
        var chat = Chat()
        val chatsRef = db.collection("chats")
        chatsRef.whereIn("group1Name", listOf(myGroupName, theirGroupName))
            .whereIn("group2Name", listOf(myGroupName, theirGroupName))
            .addSnapshotListener { doc, e ->
                if(e != null) {
                    Log.i(TAG, "Listen failed", e)
                    return@addSnapshotListener
                }
                messagesArrayList.clear()
                for (d in doc!!) {
                    chat = d.toObject<Chat>()
                    messagesArrayList.addAll(chat.messages)
                    Log.i(TAG, messagesArrayList.toString())
                    if(groupChatRV.adapter == null) {
                        // attach adapter and send groups
                        val groupChatAdapter = GroupChatAdapter(messagesArrayList, requireActivity())
                        groupChatRV.adapter = groupChatAdapter
                    }
                    else {
                        Log.i(TAG, "reycler view is not null")
                        groupChatRV.adapter?.notifyDataSetChanged()
                    }
                }
            }
    }

    companion object {
        const val TAG = "GroupChatFragment"
    }

}