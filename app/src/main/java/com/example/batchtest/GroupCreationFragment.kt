package com.example.batchtest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentGroupCreationBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "GroupsCreation"
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupCreationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupCreationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentGroupCreationBinding? = null
    private val binding get() = _binding!!
    private lateinit var group: GroupCreation



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        initialize the values of  GroupCreation class
//        groupCreation = GroupCreation(
//            groupCode = UUID.randomUUID(),
//            groupName = "",
//            tags = listOf(),
//            groupDescription = ""
//        )


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        _binding = FragmentGroupCreationBinding.inflate(layoutInflater, container, false)


//        user clicks the X button to navigate back to my groups tab
        binding.exitGroupCreatn.setOnClickListener{
            findNavController().navigate(R.id.to_myGroupFragment)
        }


        /**
         * Function: user creates a group and save data to database
         */
        binding.btnCreateGroup.setOnClickListener{
//            Toast.makeText(this.context, "Group Created!", Toast.LENGTH_SHORT).show()

//            TODO: need to get the shit together and figure out how to use arraylist in kotlin
            val db = Firebase.firestore
            val groupName = binding.editGroupName.text.toString()
            val aboutUs = binding.groupAboutUs.text.toString()
//            val users = group.users
//            val tags = group.interestTags
//            val bs = group.biscuits
            val groupcreation = GroupCreation(groupName, aboutUs )

            db.collection("NewGroup").add(groupcreation)
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener{}

        }

        /**
         * user hits the add button to add tag to the list
         */
        binding.addTag.setOnClickListener{
            if (binding.editTextAddTag.toString().isNotEmpty()){
                addChip(binding.editTextAddTag.text.toString())
            }
        }


        return binding.root
    } // end of onCreateView

/**
 Function: add individual tag to group profile
 */
    private fun addChip(text: String){
        val chip = Chip(this.context)
        chip.text = text


//    display the close button to remove tag from the list
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.purple_500)
        chip.setTextAppearance(R.style.page_text)

//        chip.setChipIconResource(R.drawable.close_icon)

//    TODO: remove chip from the interest tag as user removes it from view
        chip.setOnCloseIconClickListener{
            binding.tagGroupChip.removeView(chip)
//            group.interestTags?.remove(chip.text)

        }
//  TODO: add chip to the arraylist of interest tags
        binding.tagGroupChip.addView(chip)
//        group.interestTags?.add(chip.text.toString())

    }



//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//    }

//    free view from memory
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}

}


