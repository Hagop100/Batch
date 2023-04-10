package com.example.batchtest.OtherGroupsTab.PendingGroups

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.example.batchtest.Chat
import com.example.batchtest.PendingGroup
import com.example.batchtest.databinding.FragmentPendingGroupBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.batchtest.Group
import com.example.batchtest.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


private const val TAG = "PendingGroupFragment"
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
    ): View {

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
                // fetch groups from database using firebase's firestore
                val pendingGroups = arrayListOf<PendingGroup>()
                if (result != null) {
                    var groupsFound = false
                    // iterate thru fetched pending groups
                    for (doc in result) {
                        groupsFound = true
                        // convert pending group to an object
                        val pendingGroupObj = doc.toObject(PendingGroup::class.java)
                        voting(db, pendingGroupObj)
                        // if the pending group has not completed the voting phase, display it in recyclerview
                        if (pendingGroupObj.pending == true) {
                            // create 2 queries for the matching and pending group to send to adapter
                            val query1 =
                                db.collection("groups").document(pendingGroupObj.matchingGroup!!)
                                    .get()
                            val query2 =
                                db.collection("groups").document(pendingGroupObj.pendingGroup!!)
                                    .get()
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
                                    // send pending groups arraylist to adapter to display
                                    pendingGroupRV.adapter =
                                        PendingGroupAdapter(context, pendingGroups)
                                }
                        } else {
                            if (pendingGroups.contains(pendingGroupObj)) {
                                // send pending groups arraylist to adapter to display
                                pendingGroups.remove(pendingGroupObj)
                                pendingGroupRV.adapter =
                                    PendingGroupAdapter(context, pendingGroups)
                            }
                        }
                    }
                    // if there are no pending groups, display message
                    if (!groupsFound) {
                        binding.pendingTabMessage.text = getString(R.string.no_pending_groups)
                        pendingGroupRV.adapter = PendingGroupAdapter(context, pendingGroups)
                    } else {
                        binding.pendingTabMessage.text = ""
                    }
                }
            }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener.remove()
        _binding = null
    }

    companion object {
        // voting algorithm runs when a user votes to check if a majority votes has occurred
        // if a majority vote (accept or reject) has occurred, then check if both groups have
        // decided to match. if one group rejects, then delete the pending group from the db
        // if voting is still pending, do nothing
        fun voting(db: FirebaseFirestore, pendingGroup: PendingGroup) {
            var acceptCount = 0
            var rejectCount = 0
            val memberCount = pendingGroup.users?.size
            if (memberCount != null && memberCount > 0) {
                Log.v(TAG, "voting")
                pendingGroup.users.forEach { (key, map) ->
                    if (map["vote"] == "accept") {
                        Log.v(TAG, "$key:"+map["vote"].toString())
                        acceptCount += 1
                    } else if (map["vote"] == "reject") {
                        Log.v(TAG, "$key:"+map["vote"].toString())
                        rejectCount += 1
                    } else {
                        Log.v(TAG, "$key:"+map["vote"].toString())
                    }
                }
                // if majority of group members vote to accept the group:
                // update pending to be false so the pending group will not be displayed anymore
                // update matched to be true indicating the group accepted the group
                // else if vote to reject the group:
                // remove the pending group from the database
                if ((acceptCount.toFloat() / memberCount.toFloat()) >= .5F) {
                    val reject = (acceptCount.toFloat() / memberCount.toFloat()) >= .5F
                    Log.v(TAG, reject.toString())
                    db.collection("pendingGroups").document(pendingGroup.pendingGroupId.toString())
                        .update("pending", false, "matched", true)
                        .addOnSuccessListener {
                            // once updated, fetch the pending group for the other group to see if they had
                            // voted to accept or reject the group if both groups have accepted, then match
                            db.collection("pendingGroups")
                                .whereEqualTo("matchingGroup", pendingGroup.pendingGroup) // the pending group will be the matching group
                                .whereEqualTo("pendingGroup", pendingGroup.matchingGroup) // the matching group will be the pending group
                                .get()
                                .addOnSuccessListener {
                                    if (it.documents.isNotEmpty()) {
                                        val otherPendingGroup =
                                            it.documents[0].toObject(PendingGroup::class.java)
                                        // check if they had voted to match
                                        if (otherPendingGroup != null) {
                                            if (otherPendingGroup.matched == true) {
                                                // add to matched groups of both matched groups
                                                db.collection("groups").document(otherPendingGroup.pendingGroup.toString())
                                                    .update("matchedGroups", FieldValue.arrayUnion(otherPendingGroup.matchingGroup))
                                                db.collection("groups").document(otherPendingGroup.matchingGroup.toString())
                                                    .update("matchedGroups", FieldValue.arrayUnion(otherPendingGroup.pendingGroup))
                                                // add the user's group to the matched groups of all users in the other pending group
                                                otherPendingGroup.users?.forEach { (key, _) ->
                                                    db.collection("users").document(key)
                                                        .update(
                                                            "matchedGroups",
                                                            FieldValue.arrayUnion(otherPendingGroup.pendingGroup)
                                                        )
                                                        .addOnFailureListener { e ->
                                                            Log.v(
                                                                TAG,
                                                                "error adding current group to matched group of user:",
                                                                e
                                                            )
                                                        }
                                                }
                                                // add the other group to the matched groups of all users in the user's group
                                                pendingGroup.users.forEach { (key, _) ->
                                                    db.collection("users").document(key)
                                                        .update(
                                                            "matchedGroups",
                                                            FieldValue.arrayUnion(pendingGroup.pendingGroup)
                                                        )
                                                        .addOnFailureListener { e ->
                                                            Log.v(
                                                                TAG,
                                                                "error adding other group to matched group of user:",
                                                                e
                                                            )
                                                        }
                                                }
                                                // create a chat object
                                                val chat = Chat(
                                                    0,
                                                    arrayListOf(),
                                                    otherPendingGroup.matchingGroup,
                                                    otherPendingGroup.pendingGroup,
                                                    Date()
                                                )
                                                // add chat to db
                                                db.collection("chats").add(chat)
                                                    .addOnFailureListener { e ->
                                                        Log.v(
                                                            TAG,
                                                            "error adding chat for groups ${otherPendingGroup.matchingGroup} and ${otherPendingGroup.pendingGroup}",
                                                            e
                                                        )
                                                    }
                                                // delete both pending groups from db
                                                db.collection("pendingGroups").document(pendingGroup.pendingGroupId.toString()).delete()
                                                db.collection("pendingGroups").document(otherPendingGroup.pendingGroupId.toString()).delete()
                                            }
                                        }
                                    }
                                }
                        }
                } else if ((rejectCount.toFloat() / memberCount.toFloat()) > .5F) {
                    val reject = (rejectCount.toFloat() / memberCount.toFloat()) > .5F
                    Log.v(TAG, reject.toString())
                    // remove the pending group from the database which will add the group back to the card stack
                    db.collection("pendingGroups")
                        .document(pendingGroup.pendingGroupId.toString())
                        .delete()
                } else {
                    if (pendingGroup.pending == false) {
                        db.collection("pendingGroups")
                            .document(pendingGroup.pendingGroupId.toString())
                            .update("pending", true )
                    }
                    if (pendingGroup.matched == true) {
                        db.collection("pendingGroups")
                            .document(pendingGroup.pendingGroupId.toString())
                            .update("matched", false)
                    }
                }
            }
        }
    }
}