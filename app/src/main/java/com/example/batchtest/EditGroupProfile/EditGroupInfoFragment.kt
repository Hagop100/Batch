package com.example.batchtest.EditGroupProfile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentEditGroupInfoBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditGroupInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditGroupInfoFragment : Fragment() {
    private var _binding: FragmentEditGroupInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var group: Group
    private val sharedViewModel: GroupInfoViewModel by activityViewModels()

    //variables for group picture
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
            users = ArrayList<String>(),
            interestTags = ArrayList(),
            aboutUsDescription = "",
            biscuits = 0,
            image = null
        )

        Log.i("print", "editmode")

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentEditGroupInfoBinding.inflate(layoutInflater, container, false)

        val db = FirebaseFirestore.getInstance()

        /**
         * display by retrieve group info using sharedviewmodel
         */
        binding.groupName.text = sharedViewModel.getGName().value
        val groupName = binding.groupName.text

        //set group name to shared view model
        sharedViewModel.setGName(groupName as String)

        //initialize an empty arraylist
        sharedViewModel.groupTags.value = ArrayList()
        var updatedList = sharedViewModel.groupTags.value //copy the arraylist to use globally, but  needs to update this list!!

        //get info from the group collection in firebase
        db.collection("groups").document(groupName as String).get().addOnSuccessListener { document ->
            //set info about group pic
            val groupPic = document.getString("image")
            if (groupPic.isNullOrEmpty()) {
                binding.groupProfile.setImageResource(R.drawable.placeholder)

            } else {
                Glide.with(this).load(document.getString("image").toString())
                    .into(binding.groupProfile)
            }

            //retrieve about us as editable text
            val aboutUs = document.getString("aboutUsDescription")
            val editableText = Editable.Factory.getInstance().newEditable(aboutUs)
            binding.editAboutUs.text = editableText

            //retrieve arraylist of interest tags from the current group in firebase
            val interestTags: ArrayList<*> = document.get("interestTags") as ArrayList<*>

            //bind the chip group
            val chipGroup: ChipGroup = binding.tagGroupChip
            for (tag in interestTags){
                updatedList?.add(tag as String)
                //inflate the interest tags in chip group
                val chip = layoutInflater.inflate(R.layout.chip_item, chipGroup, false) as Chip
                chip.text = tag as String //set the text of the chip to current tag
                chip.setChipBackgroundColorResource(R.color.purple_200) //set the chip background
                chip.isCloseIconVisible = true
                chip.setTextAppearance(R.style.page_text) //set the chip text
                chipGroup.addView(chip) //add the individual chip to chip group

                //remove individual interest tag when the close button is clicked
                chip.setOnCloseIconClickListener{
                    chipGroup.removeView(chip) //remove the chip from the view
                    interestTags.remove(chip.text) //remove the chip from the arraylist
                    updatedList?.remove(chip.text)
                    //update the list
                    sharedViewModel.groupTags.value = updatedList
                    Toast.makeText(this.context, "$updatedList", Toast.LENGTH_SHORT).show()
                }
            }

        }//end of firebase document

        /**
         * user hits the add button to add tag to the list
         * Validating text fields if empty or not
         */
        binding.addTag.setOnClickListener{
            if (binding.editTextAddTag.text.isNotEmpty()) {
                updatedList = addChip(binding.editTextAddTag.text.toString(), updatedList)

                //updatedList is pass by value. must update this to update the arraylist
//                sharedViewModel.groupTags.value = updatedList
                updatedList?.let { it1 -> sharedViewModel.updateTags(it1) }
            }
            //validate tags if empty or not
            else if (binding.editTextAddTag.text.isEmpty()) {
                binding.editTextAddTag.error = "Missing Tag"
            }
            Toast.makeText(this.context, "$updatedList", Toast.LENGTH_SHORT).show()

        }

        /**
         * user picks an image from the image gallery in their phone
         */
        binding.changeProfileBtn.setOnClickListener{
            //view gallery by accessing the internal contents from mobile media
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)

            //TODO:FIX THIS ON CHANGES
            imageURL?.let { it1 -> sharedViewModel.setGroupPicture(it1) }
        }

        //clear all the text from added tag
        clearTagText()

        // Retrieve the group about us as a String using ValueEventListener
        binding.editAboutUs.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
              //do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                //set the value of group description to the text being changed
                sharedViewModel.groupDesc.postValue(s)
            }

        })


        return binding.root
    }


    /**
     * Function: to clear the text in the tag
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun clearTagText(){
        //add the clear all text button next to edit add tag
        val drawable = ContextCompat.getDrawable(requireActivity(), R.drawable.close_icon)
        drawable?.setTint(ContextCompat.getColor(requireActivity(), R.color.gray))
        binding.editTextAddTag.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)

        //add a listener to clear the text when the icon is clicked
        val editText = binding.editTextAddTag
        editText.setOnTouchListener{v, event ->
            //when the click is mouse up, text is clear
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= editText.right - editText.compoundDrawablesRelative[2].bounds.width()) {
                    editText.text?.clear()
                    return@setOnTouchListener true
                }
            }
            false

        }
    }

    /**
    * Function: add individual tag to group profile
     * 2 parameters pass by value
     */
    private fun addChip(text: String, updatedList: ArrayList<String>?): ArrayList<String>? {

        val chip = Chip(this.context)
        chip.text = text


//    display the close button to remove tag from the list
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.purple_200)
        chip.setTextAppearance(R.style.page_text)


//    remove chip from the interest tag as user removes it from view
        chip.setOnCloseIconClickListener{
            binding.tagGroupChip.removeView(chip)
            updatedList?.remove(chip.text as String)
            sharedViewModel.groupTags.value = updatedList  //update the group tag since the list is local

        }

        //add chip to the arraylist of interest tags
        binding.tagGroupChip.addView(chip)
        updatedList?.add(chip.text.toString())

        return updatedList

    }
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
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    imageURL = uri.toString()
                }
//                imageURL = imageUri.toString()
//                Log.i(TAG, "IMAGEURL: $imageURL")

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

    companion object {

        private const val TAG = "print" //for logcat debugging
    }
}





