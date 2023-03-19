package com.example.batchtest.OtherGroupsTab.MatchedGroups

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.Group
import com.example.batchtest.OtherGroupsTab.PendingGroups.PendingGroupAdapter
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentLoginBinding
import com.example.batchtest.databinding.FragmentMatchedGroupBinding
import com.example.batchtest.myGroupsTab.MyGroupAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class MatchedGroupFragment : Fragment(), MatchedGroupAdapter.MatchedGroupRecyclerViewEvent {

    //binding variables
    private var _binding: FragmentMatchedGroupBinding? = null
    private val binding get() = _binding!!

    //authentication variable
    private lateinit var auth: FirebaseAuth
    private lateinit var currUser: FirebaseUser

    //ArrayList for groups
    private var matchedGroupArrayList: ArrayList<Group> = arrayListOf<Group>()

    //AlertDialog Builder
    private var alertDialogBuilder: AlertDialog.Builder? = null

    //User variable
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alertDialogBuilder = AlertDialog.Builder(requireActivity())
        auth = Firebase.auth //Firebase.auth initialization
        currUser = auth.currentUser!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMatchedGroupBinding.inflate(inflater, container, false)

        val matchedGroupRV = binding.fragmentMatchedGroupsRecyclerView
        matchedGroupRV.layoutManager = LinearLayoutManager(context)
        matchedGroupRV.setHasFixedSize(true)

        val db = Firebase.firestore
        /*
        * fetch all groups and send to adapter which
        * will display the groups in a recycler view
         */
        selectMatchedGroups(db, matchedGroupRV)

        /*
        This portion of the code manages the buttons revealed through the action of swiping.
        Notice that these buttons use the same interface onItemClick() that the actual recycler view row buttons use
         */
        val swipe = object: MatchedGroupsSwipeHelper(requireActivity(), matchedGroupRV, 200) {
            override fun instantiateMatchedGroupButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MatchedGroupButton>
            ) {
                buffer.add(MatchedGroupButton(requireActivity(),
                    "Delete",
                    30,
                    R.drawable.ic_baseline_delete_24,
                    Color.parseColor("#FF000000"),
                    object:MatchedGroupAdapter.MatchedGroupRecyclerViewEvent {
                        override fun onItemClick(position: Int) {
                            buildDeleteAlertDialog(alertDialogBuilder!!, db, position)
                        }
                    }
                ))

                buffer.add(MatchedGroupButton(requireActivity(),
                    "Report",
                    30,
                    R.drawable.ic_baseline_report_24,
                    Color.parseColor("#4E4035"),
                    object:MatchedGroupAdapter.MatchedGroupRecyclerViewEvent {
                        override fun onItemClick(position: Int) {
                            buildReportAlertDialog(alertDialogBuilder!!, db, position)
                        }
                    }
                ))
            }
        }

        return binding.root
    }

    /*
    Builds the alert dialog required to report a group
    Furthermore, this handles the database read and write necessary to update the reportCount of the group
    being reported
     */
    private fun buildReportAlertDialog(alertDialogBuilder: AlertDialog.Builder, db: FirebaseFirestore, position: Int) {
        alertDialogBuilder.setTitle("Confirm Action: Report")
            .setMessage("Are you sure you want to report this group?")
            .setCancelable(true)
            .setPositiveButton("Report") { _, _ ->
                db.collection("groups")
                    .whereEqualTo("name", matchedGroupArrayList[position].name)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                            val group: Group = document.toObject<Group>()
                            group.reportCount += 1
                            val currGroup = db.collection("groups").document(document.id)
                            currGroup
                                .update("reportCount", group.reportCount)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                    }
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .show()
    }

    /*
    Builds the Delete Alert Dialog in order to unMatch a group from your matched group list
     */
    private fun buildDeleteAlertDialog(alertDialogBuilder: AlertDialog.Builder, db: FirebaseFirestore, position: Int) {
        alertDialogBuilder.setTitle("Confirm Action: Delete")
            .setMessage("Are you sure you want to delete this group?")
            .setCancelable(true)
            .setPositiveButton("Delete") { _, _ ->
                db.collection("users")
                    .document(currUser.uid)
                    .update(
                        "matchedGroups",
                        FieldValue.arrayRemove(matchedGroupArrayList[position].name)
                    )
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .show()
    }

    /*
    Responsible for filling up the recycler view with the current matched groups
     */
    private fun selectMatchedGroups(db: FirebaseFirestore, matchedGroupRV: RecyclerView) {
        val userDoc = db.collection("users").document(currUser.uid)
        userDoc.addSnapshotListener { snapshot, e ->
            //group arrayList must be cleared otherwise anytime data is changed in the database
            //groups will be added on top of the old groups and create duplicates
            matchedGroupArrayList.clear()
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            snapshot?.let {
                val tempUser = snapshot.toObject<User>()!!
                for(g in tempUser.matchedGroups) {
                    db.collection("groups")
                        .whereEqualTo("name", g)
                        .get()
                        .addOnSuccessListener { groupDoc ->
                            for(d in groupDoc) {
                                val group = d.toObject<Group>()
                                matchedGroupArrayList.add(group)
                            }
                            Log.i(TAG, "Data has been changed!")
                            // attach adapter and send groups
                            val matchedGroupAdapter = MatchedGroupAdapter(matchedGroupArrayList, this)
                            matchedGroupRV.adapter = matchedGroupAdapter
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MatchedGroupFragment"
    }

    /*
    This handles what happens when a group is clicked on the Matched page
    This will eventually be used to implement a group chat feature
     */
    override fun onItemClick(position: Int) {
        findNavController().navigate(R.id.action_otherGroupTabFragment_to_groupChatFragment)
        //Toast.makeText(activity, matchedGroupArrayList[position].name, Toast.LENGTH_SHORT).show()
    }

}