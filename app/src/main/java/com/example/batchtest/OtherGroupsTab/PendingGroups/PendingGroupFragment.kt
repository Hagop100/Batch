package com.example.batchtest.OtherGroupsTab.PendingGroups

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.example.batchtest.PendingGroup
import com.example.batchtest.databinding.FragmentPendingGroupBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.example.batchtest.Group


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
    private lateinit var listener: ListenerRegistration
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
        val pendingGroups = arrayListOf<PendingGroup>()
        // fetches a user from firestore using the uid from the authenticated user
        val currentUserDocRef = db.collection("users").document(currentUser!!.uid)
        /*
        * fetch all groups and send to adapter which
        * will display the groups in a recycler view
         */
        // fetch pending groups of user from database using firebase's firestore
        listener = db.collection("pendingGroups")
            .whereEqualTo("users.${currentUser.uid}.uid", currentUser.uid)
            .addSnapshotListener { result, exception ->
                if (exception != null){
                    // handle the error
                    Log.w(TAG, "listen failed.", exception)
                    return@addSnapshotListener
                }
                if (result != null) {
                    // iterate thru fetched pending groups
                    for (doc in result) {
                        // convert pending group to an object
                        val pendingGroupObj = doc.toObject(PendingGroup::class.java)
                        // create 2 queries for the matching and pending group to send to adapter
                        val query1 = db.collection("groups").document(pendingGroupObj.matchingGroup!!).get()
                        val query2 = db.collection("groups").document(pendingGroupObj.pendingGroup!!).get()
                        // once all queries are successful, add the groups to adapter to display
                        Tasks.whenAllSuccess<DocumentSnapshot>(query1, query2)
                            .addOnSuccessListener { results ->
                                // the matching group from query 1 will be stored in results[0]
                                // convert it to a group object
                                val matchingGroupDoc = results[0].toObject(Group::class.java)
                                // the matching group from query 2 will be stored in results[1]
                                // convert it to a group objet
                                val pendingGroupDoc = results[1].toObject(Group::class.java)
                                // if matching group or pending group is found, attach to pending group object
                                // add to pending groups arraylist
                                if (matchingGroupDoc != null && pendingGroupDoc != null) {
                                    pendingGroupObj.matchingGroupObj = matchingGroupDoc
                                    pendingGroupObj.pendingGroupObj = pendingGroupDoc
                                    pendingGroups.add(pendingGroupObj)
                                }
                                // if there are no pending groups, display message
                                if (pendingGroups.isEmpty()) {
                                    Log.v(TAG, "no pending groups 2")
                                    binding.pendingTabMessage.text = "No pending groups"
                                    return@addOnSuccessListener
                                } else {
                                    binding.pendingTabMessage.text = ""
                                }
                                // send pending groups arraylist to adapter to display
                                pendingGroupRV.adapter = PendingGroupAdapter(context, pendingGroups)
                            }
                    }
                    if (pendingGroups.isEmpty()) {
                        binding.pendingTabMessage.text = "No pending groups"
                    }
                }
            }
        Log.v(TAG, " pending groups ")
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener.remove()
        _binding = null
    }
}