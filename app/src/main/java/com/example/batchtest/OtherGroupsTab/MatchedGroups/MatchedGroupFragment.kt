package com.example.batchtest.OtherGroupsTab.MatchedGroups

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.*
import com.example.batchtest.EditGroupProfile.GroupInfoViewModel
import com.example.batchtest.GroupChat.GroupChatFragment
import com.example.batchtest.OtherGroupsTab.PendingGroups.PendingGroupAdapter
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentLoginBinding
import com.example.batchtest.databinding.FragmentMatchedGroupBinding
import com.example.batchtest.myGroupsTab.MyGroupAdapter
import com.example.batchtest.myGroupsTab.MyGroupFragmentDirections
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class MatchedGroupFragment : Fragment(), MatchedGroupAdapter.MatchedGroupRecyclerViewEvent {

    //binding variables
    private var _binding: FragmentMatchedGroupBinding? = null
    private val binding get() = _binding!!

    //authentication variable
    private lateinit var auth: FirebaseAuth
    private lateinit var currUser: FirebaseUser

    //ArrayList for groups
    private var matchedGroupArrayList: ArrayList<String> = arrayListOf<String>()

    //AlertDialog Builder
    private var alertDialogBuilder: AlertDialog.Builder? = null

    //User variable
    private lateinit var user: User

    //Other Group Click On
    private lateinit var theirGroupName: String

    //Group Info View Model
    private val sharedViewModel: GroupInfoViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alertDialogBuilder = AlertDialog.Builder(requireActivity())
        auth = Firebase.auth //Firebase.auth initialization
        currUser = auth.currentUser!!
        childFragmentManager.setFragmentResultListener("Group_Key", this) { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val groupNameSelected = bundle.getString("Group_Value")
            Log.i(TAG, groupNameSelected!!)
            val sendBundle = Bundle()
            sendBundle.putString("groupName", theirGroupName)
            sendBundle.putString("myGroupName", groupNameSelected)
            findNavController().navigate(R.id.action_otherGroupTabFragment_to_groupChatFragment, sendBundle)
        }
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
                            buildDeleteAlertDialog(alertDialogBuilder!!, db, position, matchedGroupRV)
                        }

                        override fun onGroupPictureClick(position: Int) {
                            //Do nothing
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

                        override fun onGroupPictureClick(position: Int) {
                            //Do nothing
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
        /*alertDialogBuilder.setTitle("Confirm Action: Report")
            .setMessage("Are you sure you want to report this group?")
            .setCancelable(true)
            .setPositiveButton("Report") { _, _ ->
                db.collection("groups")
                    .whereEqualTo("name", matchedGroupArrayList[position])
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
            .show()*/
        val reportDialog = ReportDialogFragment(matchedGroupArrayList[position], "MatchedGroupFragment")
        reportDialog.show(childFragmentManager, "reportDialog")

    }

    /*
    Builds the Delete Alert Dialog in order to unMatch a group from your matched group list
     */
    private fun buildDeleteAlertDialog(alertDialogBuilder: AlertDialog.Builder, db: FirebaseFirestore, position: Int, matchedGroupRV: RecyclerView) {
        alertDialogBuilder.setTitle("Confirm Action: Delete")
            .setMessage("Are you sure you want to un-match this group? " +
                    "Other members of your group will not be affected. ")
            .setCancelable(true)
            .setPositiveButton("Delete") { _, _ ->
                db.collection("users")
                    .document(currUser.uid)
                    .update(
                        "matchedGroups",
                        FieldValue.arrayRemove(matchedGroupArrayList[position])
                    )
                //manually delete the item from the application
                //previously we were listening to the database for real-time updates
                //for some reason this was causing the application to crash
                deleteItemFromRecyclerView(position, matchedGroupRV)
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .show()
    }

    /*
    Responsible for filling up the recycler view with the current matched groups
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun selectMatchedGroups(db: FirebaseFirestore, matchedGroupRV: RecyclerView) {
        val userDoc = db.collection("users").document(currUser.uid)
        userDoc.get().addOnSuccessListener { doc ->
            //group arrayList must be cleared otherwise anytime data is changed in the database
            //groups will be added on top of the old groups and create duplicates
            matchedGroupArrayList.clear()
            val tempUser = doc.toObject<User>()!!
            matchedGroupArrayList.addAll(tempUser.matchedGroups)

            if(matchedGroupRV.adapter == null) {
                // attach adapter and send groups
                val matchedGroupAdapter =
                    context?.let { MatchedGroupAdapter(matchedGroupArrayList, this, it) }
                matchedGroupRV.adapter = matchedGroupAdapter
            }
            else {
                matchedGroupRV.adapter?.notifyDataSetChanged()
                Log.i(TAG, "adapter is already set")
            }
        }
    }

    //This manually deletes a row from the recycler view
    private fun deleteItemFromRecyclerView(position: Int, matchedGroupRV: RecyclerView) {
        matchedGroupArrayList.removeAt(position)
        matchedGroupRV.adapter?.notifyItemChanged(position)
        matchedGroupRV.adapter?.notifyItemRangeRemoved(position, 1)
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
        theirGroupName = matchedGroupArrayList[position]
        queryMyGroupsMatched(position)
    }

    private fun queryMyGroupsMatched(position: Int) {
        //This will communicate to the next fragment which group we click on
        val db = Firebase.firestore
        val myGroupNames = ArrayList<String>()
        var user: User? = null

        //Build progress dialog
        val pDialog = ProgressDialog(context)
        pDialog.setTitle("Fetching Group(s)")
        pDialog.setMessage("Loading...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()

        val userDocRef = currUser.let { db.collection("users").document(it.uid) }
        userDocRef.get().addOnSuccessListener { doc ->
            if (doc != null) {
                user = doc.toObject<User>()
                Log.i(TAG, "pDialog shown")
                for(myGroups in user?.myGroups!!) {
                    var group: Group? = null
                    val groupDocRef = db.collection("groups").document(myGroups)
                    //runBlocking coroutine will block the main thread until the inside function
                    //has completed
                    runBlocking {
                        group = groupDocRef.get().await().toObject<Group>()
                        if(group?.matchedGroups?.contains(matchedGroupArrayList[position]) == true) {
                            myGroupNames.add(group?.name!!)
                        }
                    }
                }
                pDialog.dismiss() //dismiss the loading progress dialog
                if(myGroupNames.size > 1) {
                    //show dialog because we have more than one group that matched with
                    //the group we clicked on
                    val dialog = MatchedGroupDialogFragment(myGroupNames)
                    dialog.show(childFragmentManager, "matchedGroupDialogFragment")
                }
                else {
                    val sendBundle = Bundle()
                    sendBundle.putString("groupName", theirGroupName)
                    sendBundle.putString("myGroupName", myGroupNames[0])
                    findNavController().navigate(R.id.action_otherGroupTabFragment_to_groupChatFragment, sendBundle)
                }
            } else {
                Log.i(TAG, "no such doc")
            }
        }
            .addOnFailureListener { e ->
                Log.i(TAG, "get failed with ", e)
            }
    }

    //Clicking on the group photo in the recycler view will take you to the group's info page
    override fun onGroupPictureClick(position: Int) {
        val groupInfo = matchedGroupArrayList[position]
        //val bundle = bundleOf("fragmentNavigatedFrom" to TAG)
        // add group name and fragment to return to in shared view model
        sharedViewModel.setGName(groupInfo)
        sharedViewModel.setReturnFragment("MatchedGroupFragment")
        //navigate action to the requesting fragment
        findNavController().navigate(R.id.action_otherGroupTabFragment_to_viewGroupInfoFragment)
    }

}