package com.example.batchtest

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentGroupCreationBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var group: Group
    lateinit var imageView: ImageView
    private var imageUri: Uri? = null
    private val pickImage = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//      initialize the values of  Group class
        group = Group(
            name = "",
            users = ArrayList<User>(),
            interestTags = ArrayList(),
            aboutUsDescription = "",
            biscuits = 0
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        _binding = FragmentGroupCreationBinding.inflate(layoutInflater, container, false)

        /**
         * user clicks the X button to navigate back to my groups tab
          */

        binding.exitGroupCreatn.setOnClickListener{
            findNavController().navigate(R.id.to_myGroupFragment)
        }

        /**
         * add group profile picture
         */

        binding.changeProfileBtn.setOnClickListener{
            //view gallery
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

            startActivityForResult(gallery, pickImage)

        }

        /**
         * user creates a group and save data to database
         */
        binding.btnCreateGroup.setOnClickListener{

//        TODO: retrieve data from database
            val db = Firebase.firestore

            val groupName = binding.editGroupName.text.toString()
            val aboutUs = binding.editGroupAboutUs.text.toString()
            val users = group.users
            val tags = group.interestTags
            val biscuit = group.biscuits
            val groupcreation = Group(groupName, users, tags, aboutUs,biscuit)

//            Validating text fields if empty or not
            if (groupName.isEmpty()){
                binding.editGroupName.error = "Missing Group's Name"
            }

            else{
                db.collection("NewGroup").add(groupcreation)
                    .addOnSuccessListener { Log.d(TAG, "Group successfully created") }
                    .addOnFailureListener{}
                Toast.makeText(this.context, "Group Created!", Toast.LENGTH_SHORT).show()
            }

        }

        /**
         * user hits the add button to add tag to the list
         * Validating text fields if empty or not
         */
        binding.addTag.setOnClickListener{
            if (binding.editTextAddTag.text.isNotEmpty()){
                addChip(binding.editTextAddTag.text.toString())
            }
            else if (binding.editTextAddTag.text.isEmpty()){
                binding.editTextAddTag.error = "Invalid Entry"
            }
        }

        return binding.root
    } // end of onCreateView


    /**
     * set profile image
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(resultCode, resultCode, data)

        if(resultCode == RESULT_OK && resultCode == pickImage){
            imageUri = data?.data
            binding.groupProfile.setImageURI(imageUri)
        }

    }


    /**
 Function: add individual tag to group profile
 */
    private fun addChip(text: String){
        val tags = group.interestTags
        val chip = Chip(this.context)
        chip.text = text


//    display the close button to remove tag from the list
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.purple_500)
        chip.setTextAppearance(R.style.page_text)

//        chip.setChipIconResource(R.drawable.close_icon)

//    remove chip from the interest tag as user removes it from view
        chip.setOnCloseIconClickListener{
            binding.tagGroupChip.removeView(chip)
            tags?.remove(chip.text as String)
        }
//   add chip to the arraylist of interest tags
        binding.tagGroupChip.addView(chip)
        tags?.add(chip.text.toString())


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



