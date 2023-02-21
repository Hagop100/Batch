package com.example.batchtest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.databinding.FragmentMyGroupBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyGroupFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var groupInfo: Group
    private var _binding: FragmentMyGroupBinding? = null

//  Card view variables that will be use to display in my group tab
    private lateinit var recyclerView: RecyclerView
    private lateinit var myGroupList: ArrayList<Group>
    private lateinit var myAdapter: MyGroupAdapter
    private lateinit var db: FirebaseFirestore


    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }


    }

    /**
     * inflates the view of my group fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMyGroupBinding.inflate(layoutInflater,container, false)

        //for the card view of the group list info
        recyclerView = binding.recycleView
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        recyclerView.setHasFixedSize(true)
        myGroupList = arrayListOf()
        myAdapter = context?.let { MyGroupAdapter(it,myGroupList) }!!
        recyclerView.adapter = myAdapter

        EventChangeListener()



//      this button navigates from My Group view to Create a group view fragment
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
     * Query the document database to get the group info
     */
    private fun EventChangeListener(){
        db = FirebaseFirestore.getInstance()
        db.collection("NewGroup").addSnapshotListener(object: com.google.firebase.firestore.EventListener<QuerySnapshot>{
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null){
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                //loop thru all the groups and add to my group list from the database document
                for (doc : DocumentChange in value ?.documentChanges!!){
                    if (doc.type == DocumentChange.Type.ADDED){
                        myGroupList.add(doc.document.toObject(Group::class.java))
                    }
                }

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





