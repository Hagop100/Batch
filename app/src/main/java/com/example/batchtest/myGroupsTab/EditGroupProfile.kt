package com.example.batchtest.myGroupsTab

import android.location.GnssAntennaInfo.Listener
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.batchtest.EditGroupProfile.EditGroupInfoFragment
import com.example.batchtest.EditGroupProfile.GroupInfoViewModel
import com.example.batchtest.EditGroupProfile.GroupProfileAdapter
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentEditGroupProfileBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditGroupProfile.newInstance] factory method to
 * create an instance of this fragment.
 */

val tabArray = arrayOf(
    "Edit",
    "Preview"

)
class EditGroupProfile : Fragment() {
    private var _binding: FragmentEditGroupProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val sharedViewModel: GroupInfoViewModel by activityViewModels()
    private lateinit var groupInfo: Group
    var db = Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        _binding = FragmentEditGroupProfileBinding.inflate(layoutInflater, container, false)

        var groupDesc: String = ""
        var groupImg: String = ""
        var groupTags: ArrayList<String> = ArrayList()

        //get the group name
        val groupName: String = sharedViewModel.groupName.value.toString()

        //get the updated group description
        sharedViewModel.groupDesc.observe(viewLifecycleOwner, Observer{ newDesc ->
            groupDesc = newDesc.toString() //cast newDesc to String from Editable
        })


        //get the updated group picture
        sharedViewModel.groupPic.observe(viewLifecycleOwner, Observer { imageUrl ->
            //if there is no changes. see database
            if(imageUrl.isNullOrEmpty()){
                //inflate the group pic if there is no changes - use database
                db.collection("groups").document(groupName).get().addOnSuccessListener { document ->
                    //set info about group pic
                    val groupPic = document.getString("image").toString()
                    Log.i("print", "groupic here: $groupPic")
                    groupImg = groupPic
                    Log.i("print", "group img1.0: $groupImg")
                }
            }
            else{
                //if changes are made. set the group picture
                groupImg = imageUrl.toString()

                Log.i("print", "group img1: $groupImg")
            }
        })

        //get the updated tags
        sharedViewModel.groupTags.observe(viewLifecycleOwner, Observer<ArrayList<String>> { newTags: ArrayList<String> ->
            groupTags = newTags
        })



        val db = Firebase.firestore //access database
        val docRef = db.collection("groups").document(groupName) //access the group

        //save group edits to database
        binding.saveBtn.setOnClickListener{
            val updates = hashMapOf<String, Any>(
                "aboutUsDescription" to groupDesc, //change the field to updated groupDescription
            )
            val updateImg = hashMapOf<String, Any>(
                "image" to groupImg, //change the field to updated image
            )
            Log.i("print", "group img2: $groupImg")
            docRef.update("interestTags",groupTags) //update the database for interest tags with the new group tags

            if (groupImg.isNullOrEmpty()){
                db.collection("groups").document(groupName).get().addOnSuccessListener { document ->
                    //set info about group pic
                    val groupPic = document.getString("image").toString()
                    if (groupPic.isNullOrEmpty()) {
                        //dont do anything if there is no group pic
                    }
                }
            }
            //update firebase if the group pic is set
            else {
                docRef.update(updateImg)
            }
            docRef.update(updates) //update the database for about us the new edits
            findNavController().navigate(R.id.action_editGroupProfile_to_viewGroupInfoFragment)
            sharedViewModel.groupPic.value = "" //reset the observing changes

        }

        //set up the 2 tab layout: edit and preview
        setUpTabs()

        //cancel to navigate back to my group page
        binding.cancelGroupEdit.setOnClickListener{
            findNavController().navigate(R.id.action_editGroupProfile_to_viewGroupInfoFragment)
        }

        return binding.root

    }


    /**
     * display the 2 edit and preview tabs
     */
    private fun setUpTabs() {

        viewPager = binding.groupProfileViewpager
        tabLayout = binding.groupEditTabs

        val adapter = GroupProfileAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->

            tab.text = tabArray[position]
        }.attach()


    }


    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        sharedViewModel.groupPic.removeObservers(viewLifecycleOwner)
//        super.onDestroyView()
        viewPager.let {
            (viewPager.parent as? ViewGroup)?.removeView(viewPager)
            viewPager.adapter = null
//            viewPager = null
        }
        tabLayout.let {
            (tabLayout.parent as? ViewGroup)?.removeView(tabLayout)
            tabLayout.clearOnTabSelectedListeners()
//            tabLayout = null
        }
        _binding = null
    }
}

