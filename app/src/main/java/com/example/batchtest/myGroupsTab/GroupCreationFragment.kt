package com.example.batchtest.myGroupsTab



import android.annotation.SuppressLint
import android.app.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentGroupCreationBinding
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "print"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupCreationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupCreationFragment : Fragment() {
    private var _binding: FragmentGroupCreationBinding? = null
    private val binding get() = _binding!!
    private lateinit var group: Group
    lateinit var grouppic: CircleImageView
    private var imageUri: Uri? = null
    private val pickImage = 100
    private var imageURL: String? = null


    /**
     * initialize the values of Group class when the app is starting up
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//      initialize the values of Group class
        group = Group(
            name = "",
            users = ArrayList<User>(),
            interestTags = ArrayList(),
            aboutUsDescription = "",
            biscuits = 0,
            image = null
        )
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentGroupCreationBinding.inflate(layoutInflater, container, false)

        /**
         * user clicks the close button to navigate back to my groups tab
          */
        binding.exitGroupCreatn.setOnClickListener{
            findNavController().navigate(R.id.to_myGroupFragment)
        }

        /**
         * user picks an image from the image gallery in their phone
         */
        binding.changeProfileBtn.setOnClickListener{
            //view gallery by accessing the internal contents from mobile media
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        /**
         * user creates a group and save data to database
         * This function use entry validation by checking for existing values in the database
         */

        binding.btnCreateGroup.setOnClickListener{

            val db = Firebase.firestore
            val groupName = binding.editGroupName.text.toString()
            val aboutUs = binding.editGroupAboutUs.text.toString()
            val users = group.users
            val tags = group.interestTags
            val biscuit = group.biscuits
            val image = imageURL

            val groupInfo = Group(groupName, users, tags, aboutUs,biscuit, image)

                //Validating group name and tag if empty or not
                if (groupName.isEmpty() && binding.editTextAddTag.text.isEmpty()){
                    binding.editGroupName.error = "Missing Group's Name"
                    binding.editTextAddTag.error = "Missing Tag"
                }

                //validate if tag is not empty but group name is empty
                else if (binding.editTextAddTag.text.isNotEmpty() && groupName.isEmpty()) {
                    binding.editGroupName.error = "Missing Group's Name"
                }

                //check to see if the user added the tag into the tag list
                else if (binding.tagGroupChip.isEmpty()){
                    binding.editTextAddTag.error = "Must add tags"
                }
                //if entry not empty, validate existing group name
                else {

                        //find existing group name in database that matches with the name entry
                        db.collection("groups").whereEqualTo("name", groupName).get()
                            .addOnSuccessListener { documents ->

                                //if entry is not found(not match with) in database, create a new group
                                if (documents.isEmpty){

                                    //if valid group name is entered, check whether the tag is empty
                                    if (binding.editTextAddTag.text.isEmpty()) {
                                        binding.editTextAddTag.error = "Missing Tag"
                                    }
                                    // if tag is not empty, create a new group
                                    else{
                                        //set the group name as the document name in firebase
                                        db.collection("groups").document(groupName).set(groupInfo)
                                        Toast.makeText(this.context, "Group Created!", Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.to_myGroupFragment)
                                    }

                                }

                                //if entry matches the name in the database, alert user to reenter a new group name
                                else{
                                    for (doc in documents) {
//                            Log.i(TAG, "${doc.id} => ${doc.data}")
//                            Log.i(TAG, doc.data.getValue("name") as String)
                                        if (doc.data.getValue("name") == groupName) {
                                            binding.editGroupName.error = "Group name is already taken"

                                        }

                                    }
                                }

                            }
                            //database could not find the match with the entry
                            .addOnFailureListener { e ->
                                Log.i(TAG, "Error writing document", e)
                            }
                    }


        }// end of button group creation

        /**
         * user hits the add button to add tag to the list
         * Validating text fields if empty or not
         */
        binding.addTag.setOnClickListener{
            if (binding.editTextAddTag.text.isNotEmpty()) {
                addChip(binding.editTextAddTag.text.toString())
            }
            //validate tags if empty or not
            else if (binding.editTextAddTag.text.isEmpty()) {
                binding.editTextAddTag.error = "Missing Tag"
            }

        }


        return binding.root
    } // end of onCreateView
//
    /**Function will get the extension of type of the profile image */
    private fun getFileExtension(activity: Activity?, uri: Uri?): String?
    {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity?.contentResolver?.getType(uri!!))
    }

    /**
     * Get A Firebase Storage reference to save the user image to
     * Set the file name and extension to the time that the image is uploaded
     * return Firebase Reference
     * */
    private fun getStorageReference(): StorageReference {
        //get the image's file type
        val imageExtension = getFileExtension(activity, imageUri)
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)

        return FirebaseStorage.getInstance().getReference("GroupPic/$fileName.$imageExtension")
        //get the image's file type
//        val imageExtension = getFileExtension(activity, imageUri)
//
//        return FirebaseStorage.getInstance().reference.child(
//            InitialProfilePersonalizationFragment.USER_PROFILE_IMAGE + System.currentTimeMillis() + "."
//                    + imageExtension)
    }
    /**
     * Using the Firebase Storage Reference, upload the user image to
     * the Firebase Storage area.
     * The SuccessListener returns the URL of the image.
     * imageURL is updated with the returned URl
     * */
    private fun uploadUserImageToCloud(activity: Activity?, imageUri: Uri?)
    {

        val storageReference: StorageReference = getStorageReference()
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // Get the downloadable url from the task snapshot
                // The Success Listener is Asynchronous, and any following code will run
                // Getting the TaskSnapshot takes a bit of time,
//                imageURL = taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                imageURL = imageUri.toString()
                Log.i(TAG, "IMAGEURL: $imageURL")

            }.addOnFailureListener {
                Toast.makeText(context,"Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }


    /**
     * set profile image
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        grouppic = binding.groupProfile

        //setting that was selected from the gallery
        if(resultCode == Activity.RESULT_OK && requestCode == pickImage){
            imageUri = data?.data
            Log.i(TAG, "Image URi: $imageUri.toString()")
            grouppic.setImageURI(imageUri)
            uploadUserImageToCloud(activity, imageUri)

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


//    remove chip from the interest tag as user removes it from view
        chip.setOnCloseIconClickListener{
            binding.tagGroupChip.removeView(chip)
            tags?.remove(chip.text as String)
        }

//   add chip to the arraylist of interest tags
        binding.tagGroupChip.addView(chip)
        tags?.add(chip.text.toString())


    }


    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


