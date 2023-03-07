package com.example.batchtest.myGroupsTab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.firebase.ktx.Firebase

import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [MyGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "GroupsFetchLog"
class MyGroupFragment : Fragment() {

    private lateinit var groupInfo: Group
    private var _binding: FragmentMyGroupBinding? = null

//  Card view variables that will be use to display in my group tab
    private lateinit var recyclerView: RecyclerView
    private lateinit var myGroupList: ArrayList<Group>
    private lateinit var myAdapter: MyGroupAdapter
    private lateinit var db: FirebaseFirestore
    private val currentUser = Firebase.auth.currentUser

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
        myAdapter = context?.let { MyGroupAdapter(it,myGroupList) }!!
      //  recyclerView.adapter = myAdapter

        // call function to retrieve info from database
        EventChangeListener()

        /**
         *
         */


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
    private fun EventChangeListener(){
        db = FirebaseFirestore.getInstance()
        db.collection("groups").addSnapshotListener(object: com.google.firebase.firestore.EventListener<QuerySnapshot>{
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null){
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                //loop thru all the groups and add to my group list from the database document
//                for (doc : DocumentChange in value ?.documentChanges!!){
//                    if (doc.type == DocumentChange.Type.ADDED){
//                        myGroupList.add(doc.document.toObject(Group::class.java))
                        // fetches a user from firestore using the uid from the authenticated user
                        val currentUserDocRef = db.collection("users").document(currentUser!!.uid)
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
                                // Log.v(com.example.batchtest.MatchTab.TAG, "fetch group")
                                // names matching the unwanted group's name
                                if (filterGroups.isNotEmpty()) {
                                    groupsDocRef.whereIn("name", filterGroups)
                                        .get()
                                        .addOnSuccessListener {
                                            // convert the resulting groups into group object
                                            for (doc in it) {
                                                val group: Group = doc.toObject(Group::class.java)
                                                // add the group to the groups list
                                                myGroupList.add(group)
                                            }
                                            // attach adapter and send groups and listener
                                            recyclerView.adapter = myAdapter
                                            Log.i("print", "AM I HER 1")
                                            //     EventChangeListener()

                                        }
                                        .addOnFailureListener { e ->
                                            //Log.v(com.example.batchtest.MatchTab.TAG, "error getting documents: ", e)
                                        }
                                } else {
                                    // should display that user needs to be in a group
                                    groupsDocRef
                                        .get()
                                        .addOnSuccessListener {
                                            // convert the resulting groups into group object
                                            for (doc in it) {
                                                val group: Group = doc.toObject(Group::class.java)
                                                // add the group to the groups list
                                                myGroupList.add(group)
                                            }
                                            // attach adapter and send groups and listener
                                            recyclerView.adapter = myAdapter
                                            Log.i("print", "AM I HER 2")
                                            // EventChangeListener()

                                        }
                                        .addOnFailureListener { e ->
                                            // Log.v(com.example.batchtest.MatchTab.TAG, "error getting documents: ", e)
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                //  Log.v(com.example.batchtest.MatchTab.TAG, "error getting user from documents: ", e)
                            }
              //      }
//                }

                myAdapter.notifyDataSetChanged()
            }

        })
    }


    /**
     * free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





