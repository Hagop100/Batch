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
import com.example.batchtest.OtherGroupsTab.PendingGroups.PendingGroupAdapter
import com.example.batchtest.databinding.FragmentPendingGroupBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private const val TAG = "PendingGroupsLog"
/**
 * A simple [Fragment] subclass.
 * Use the [PendingGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PendingGroupFragment : Fragment() {
    private lateinit var binding: FragmentPendingGroupBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for pending group fragment
        binding = FragmentPendingGroupBinding.inflate(inflater, container, false)

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
        val db = Firebase.firestore
        /*
        * fetch all groups and send to adapter which
        * will display the groups in a recycler view
         */
        db.collection("groups")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    // get group's data in form of map
                    val group: Group = doc.toObject(Group::class.java)
                    // add group to groups
                    pendingGroups.add(group)
                }
                // attach adapter and send groups
                val pendingGroupAdapter = PendingGroupAdapter(context, pendingGroups)
                pendingGroupRV.adapter = pendingGroupAdapter
            }
            .addOnFailureListener { e ->
                Log.v(TAG, "error getting documents: ", e)
            }


        return binding.root
    }
}