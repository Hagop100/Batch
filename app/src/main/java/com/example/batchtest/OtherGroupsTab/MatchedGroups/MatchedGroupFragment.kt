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
                    "UnMatch",
                    30,
                    R.drawable.ic_baseline_delete_24,
                    Color.parseColor("#FF3C30"),
                    object:MatchedGroupAdapter.MatchedGroupRecyclerViewEvent {
                        override fun onItemClick(position: Int) {
                            Toast.makeText(requireActivity(), "UnMatch " + matchedGroupArrayList[position].name, Toast.LENGTH_SHORT).show()
                        }
                    }
                ))

                buffer.add(MatchedGroupButton(requireActivity(),
                    "Report",
                    30,
                    R.drawable.ic_baseline_report_24,
                    Color.parseColor("#FF9502"),
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
        alertDialogBuilder.setTitle("Confirm Action")
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

    private fun selectMatchedGroups(db: FirebaseFirestore, matchedGroupRV: RecyclerView) {
        db.collection("users").document(currUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                user = doc.toObject<User>()!!
                val docRef = db.collection("groups")
                docRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    snapshot?.let {
                        for(d in it) {
                            val groups = d.toObject<Group>()
                            if(user.matchedGroups.contains(groups.name)) {
                                matchedGroupArrayList.add(groups)
                                // attach adapter and send groups
                                val matchedGroupAdapter = MatchedGroupAdapter(matchedGroupArrayList, this)
                                matchedGroupRV.adapter = matchedGroupAdapter
                            }
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
        Toast.makeText(activity, matchedGroupArrayList[position].name, Toast.LENGTH_SHORT).show()
    }

}