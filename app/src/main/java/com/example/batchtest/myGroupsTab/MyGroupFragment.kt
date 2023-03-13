package com.example.batchtest.myGroupsTab

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.StateSet.TAG
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.EditGroupProfile.ViewGroupInfoFragment
import com.example.batchtest.Group
import com.example.batchtest.MatchTab.CardStackAdapter
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentMyGroupBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [MyGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class MyGroupFragment : Fragment(), MyGroupAdapter.GroupProfileViewEvent { //end of RetrieveGroups()

    private lateinit var groupInfo: Group
    private var _binding: FragmentMyGroupBinding? = null

//  Card view variables that will be use to display in my group tab
    private lateinit var recyclerView: RecyclerView
    private lateinit var myGroupList: ArrayList<Group>
    private lateinit var myAdapter: MyGroupAdapter
    private lateinit var db: FirebaseFirestore
    private val currentUser = Firebase.auth.currentUser
    private lateinit var progressDialog: Dialog
    private val binding get() = _binding!!


    /**
     * inflates the view of my group fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMyGroupBinding.inflate(layoutInflater,container, false)

        //set the layout for the group info to display in the list
        recyclerView = binding.recycleView
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.setHasFixedSize(true)
        myGroupList = arrayListOf()
        myAdapter = context?.let { MyGroupAdapter(it, this , myGroupList) }!!



        // call function to retrieve info from database
        RetrieveGroups()

        /**
         *  navigates from My Group view to Create a group view fragment
          */
        binding.btnToGroupCreation.setOnClickListener{

//            hides the bottom nav when navigate to the group creation page
            val navBar: BottomNavigationView? = activity?.findViewById(R.id.nav_bar)
            navBar?.visibility = View.GONE

            //navigate to the group creation page
            findNavController().navigate(R.id.to_groupCreationFragment)
        }

        return binding.root

    }

    /**
     * Query the document database to get the group info include group image, group name and descrption
     * This works with the Adapter class to retrieve info of the group
     */
    private fun RetrieveGroups(){
        db = FirebaseFirestore.getInstance()

                // fetches a user from firestore using the uid from the authenticated user
                val currentUserDocRef = db.collection("users").document(currentUser!!.uid)
                currentUserDocRef.addSnapshotListener{ snapshot, exception ->
                    if (exception != null){
                        // handle the error
                        Log.w(TAG, "Listen failed.", exception)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()){
                        currentUserDocRef
                            // reads the document reference
                            .get()
                            // if successful filter out certain groups for matching
                            .addOnSuccessListener { result ->
                                // convert the fetched user into a User object
                                val user: User = result.toObject(User::class.java)!!
                                // filter groups will store all group to remove from the match group pool
                                val filterGroups: ArrayList<String> = ArrayList()
                                // all groups the user is will be filtered out so the user cannot match with their own groups
                                filterGroups.addAll(user.myGroups!!)
                                // fetch all groups from the database filtering out the groups with
                                val groupsDocRef = db.collection("groups")
                                Log.i(TAG, "fetch group")

                                // display groups that the users are currently in.
                                if (filterGroups.size != 0) {
                                    groupsDocRef.whereIn("name", filterGroups)
                                        .get()
                                        .addOnSuccessListener {

                                            //clear all the groups before adding them into display list
                                            myGroupList.clear()
                                            // convert the resulting groups into group object
                                            for (doc in it) {
                                                val group: Group = doc.toObject(Group::class.java)
                                                // add the group to the groups list
                                                myGroupList.add(group)
                                            }
                                            myAdapter.notifyDataSetChanged()
                                            // attach adapter and send groups and listener
                                            recyclerView.adapter = myAdapter

                                            Log.i("print", "AM I HERE 1")
                                            //     EventChangeListener()

                                        }
                                        .addOnFailureListener { e ->
                                            Log.i(TAG, "error getting documents: ", e)
                                        }

                                }

                                //if the user have  0 groups. show a message for user to join a group
                                else {
                                    binding.noGroupMessage.text =
                                        "You have 0 group. \n Join a group to start matching!"
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.i(TAG, "error getting user from documents: ", e)
                            }

                    }


                }


    }



/**
     * free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "print" //for logcat debugging

    }

    /**
     * click on individual item of the Group pic with the right position
     * to navigate to the corresponding group
     */
    override fun onItemClick(postion: Int) {
        val groupInfo =  myGroupList[postion]
        Toast.makeText(this.context, groupInfo.name, Toast.LENGTH_SHORT).show()

        /**
         * navigate to the ViewGroupInfoFragment using the position of the group using Navigation Component
         * passing data to ViewGroupInfoFragment
         */
        val direction = MyGroupFragmentDirections.actionMyGroupFragmentToViewGroupInfoFragment(
            groupInfo.name.toString(),
            groupInfo.aboutUsDescription.toString())
        findNavController().navigate(direction)

    }
}







