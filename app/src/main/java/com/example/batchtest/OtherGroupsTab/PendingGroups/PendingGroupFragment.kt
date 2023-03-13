package com.example.batchtest.OtherGroupsTab.PendingGroups

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.example.batchtest.Group
import com.example.batchtest.MatchTab.CardStackAdapter
import com.example.batchtest.OtherGroupsTab.PendingGroups.PendingGroupAdapter
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.example.batchtest.databinding.FragmentPendingGroupBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private const val TAG = "PendingGroupsLog"
/**
 * A simple [Fragment] subclass.
 * Use the [PendingGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PendingGroupFragment : Fragment() {
    private var _binding: FragmentPendingGroupBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    // get the authenticated logged in user
    private val currentUser = Firebase.auth.currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // inflate the layout for pending group fragment
        _binding = FragmentPendingGroupBinding.inflate(inflater, container, false)

        // get recycler view from pending group fragment layout binding
        val pendingGroupRV = binding.pendingGroupRecyclerView
        // use grid layout for the recycler view with 2 columns
        val layoutManager = GridLayoutManager(context, 2)
        pendingGroupRV.layoutManager = layoutManager
        // create the default divider used in recycler views in order to remove
        val dividerItemDecoration = DividerItemDecoration(
            pendingGroupRV.context,
            layoutManager.orientation
        )
        // remove the default divider from the recycler view
        pendingGroupRV.removeItemDecoration(dividerItemDecoration)


        // fetch groups from database using firebase's firestore
        val pendingGroups = arrayListOf<Group>()
        // fetches a user from firestore using the uid from the authenticated user
        val currentUserDocRef = db.collection("users").document(currentUser!!.uid)
        /*
        * fetch all groups and send to adapter which
        * will display the groups in a recycler view
         */
        currentUserDocRef
            // reads the document reference
            .get()
            // if successful filter out certain groups for matching
            .addOnSuccessListener { result ->
                // convert the fetched user into a User object
                val user: User = result.toObject(User::class.java)!!
                // filter pending groups from all groups stored in database
                val filterGroups: ArrayList<String> = ArrayList()
                // all groups that are awaiting the voting process will be filtered out
                filterGroups.addAll(user.pendingGroups!!)
                val groupsDocRef = db.collection("groups")
                // fetch all groups from the database filtering out the groups with
                if (filterGroups.isNotEmpty()) {
                    // names matching the unwanted group's name
                    groupsDocRef
                        .whereIn("name", filterGroups)
                        .get()
                        .addOnSuccessListener {
                            // convert the resulting groups into group object
                            for (doc in it) {
                                val group: Group = doc.toObject(Group::class.java)
                                // add the group to the groups list
                                pendingGroups.add(group)
                            }
                            // attach adapter and send groups and listener
                            pendingGroupRV.adapter = PendingGroupAdapter(context, pendingGroups)
                        }
                        .addOnFailureListener { e ->
                            // error in retrieving filtered group documents
                            Log.v(TAG, "error getting documents: ", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                // error in retrieving current user document
                Log.v(TAG, "error getting documents: ", e)
            }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}