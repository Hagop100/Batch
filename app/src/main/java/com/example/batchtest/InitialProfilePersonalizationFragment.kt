package com.example.batchtest

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentInitialProfilePersonalizationBinding
import java.text.SimpleDateFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


/**
 * Use Case 3 Initial Profile Personalization page.
 * The User will be able to finish their personal user registration.
 * Here they will input their Profile Image, Display Name, Birthday, Gender and Personal Bio
 * This information will then be sent to the firebase firestore database.
 *
 */
class InitialProfilePersonalizationFragment : Fragment() {

    //Binding Values
    private var _binding: FragmentInitialProfilePersonalizationBinding? = null
    private val binding get() = _binding!!

    //Variables that will be initialized by the user
    private lateinit var displayName: String
    private lateinit var birthday: String
    private lateinit var personalBio: String
    private lateinit var gender: String
    private lateinit var imageUri: Uri
    private lateinit var userDetails: User
    private var imageURL: String? = null

    //ActivityResultLauncher must be initialized onCreate
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    //
    private val userHasMap = HashMap<String, Any>()

    /**Will hold constant for this class */
    companion object{
        const val PICK_IMAGE_REQUEST_CODE =44
        const val GENDER = "gender"
        const val MALE: String = "male"
        const val FEMALE: String = "female"
        const val NONBINARY: String = "nonbinary"
        const val NOGENDER: String = "N/A"
        const val DISPLAYNAME: String = "displayName"
        const val IMAGEURL: String = "imageUrl"
        const val BIRTHDATE: String = "birthdate"
        const val PERSONALBIO: String = "personalBio"
        const val USER_PROFILE_IMAGE: String = "User_Profile_Image"
        const val FIRSTNAME: String = "firstname"
        const val LASTNAME: String ="lastName"
        const val EMAIL: String = "email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Must Register for activity result in OnCreate, Can only be created on initialization
        //Requesting the user for permission to access their storage and camera, will be called in onViewCreated
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
            ){ permissions ->
                permissions.entries.forEach {
                    val permissionName = it.key
                    val isGranted = it.value
                    if(isGranted)
                    {
                        Toast.makeText(context,"$permissionName is Granted", Toast.LENGTH_SHORT).show()
                        if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE)
                        {
                            showImageChooser(activity)
                        }
                    }
                    else
                    {
                        Toast.makeText(context,"$permissionName is Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    //Inflate the Fragment View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInitialProfilePersonalizationBinding.inflate(inflater,container,false)
        return binding.root
    }

    //Bind all view and and functionality
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Ask for permission for camera and external storage
        //Select picture for userProfile
        binding.btnImageUpdate.setOnClickListener {
            getUserImage(view)
        }
        //On click, will bring up a DatePicker Dialog so the user can set their date of birth
        binding.btnBirthdayPicker.setOnClickListener{
            birthdayPicker()
        }

        binding.btnUpdate.setOnClickListener {

            //Verify that no entry is empty and populate the hashMap with the User inputs.
            if(validateEntries(it) && imageURL != null)
            {
                updateUserProfileDatabase()
            }
            //else wait and try again for image URL
        }
    }

    /**
     * Gathers the user's input and places it in a hashmap
     * Updates User details in the Firestore Database
     * */
    private fun updateUserProfileDatabase()
    {
        //Get the values the user has input
        //Save values in a Hashmap that will be used to update the user's information
        //in the firebase database.
        displayName = binding.etDisplayName.text.toString().trim()
        gender = when(binding.genderRadioGroup.checkedRadioButtonId)
        {
            binding.maleRadioButton.id -> MALE
            binding.femaleRadioButton.id-> FEMALE
            binding.nonBinaryRadioButton.id -> NONBINARY
            else-> NOGENDER
        }

        birthday = binding.btnBirthdayPicker.text.toString()
        personalBio = binding.etPersonalBio.text.toString()
        userHasMap[FIRSTNAME] = "Emanuel"
        userHasMap[LASTNAME] = "Ruiz"
        userHasMap[EMAIL] = "eman@email.com"
        userHasMap[DISPLAYNAME] = displayName
        userHasMap[GENDER] = gender
        userHasMap[BIRTHDATE] = birthday
        userHasMap[PERSONALBIO] = personalBio
        userHasMap[IMAGEURL] = imageURL!!

        //Get a instance of the Firebase Firestore database and update the user's information
        FirebaseFirestore.getInstance().collection("users")
            .document(getCurrentUserID())
            .update(userHasMap).addOnSuccessListener {
                Toast.makeText(context,"Successfully Updated Profile", Toast.LENGTH_SHORT).show()
                //Will navigate to the next fragment, Currently not in use should put
                findNavController().navigate(R.id.action_initialProfilePersonalizationFragment_to_groupCreationFragment)
            }.addOnFailureListener{
                Toast.makeText(context,"Failed to Update Profile", Toast.LENGTH_SHORT).show()
            }
    }

    //Must unbind the view
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Get A Firebase Storage reference to save the user image to
     * return Firebase Reference
     * */
    private fun getStorageReference(): StorageReference {
        //get the image's file type
        val imageExtension = getFileExtension(activity, imageUri)

        return FirebaseStorage.getInstance().reference.child(
            USER_PROFILE_IMAGE + System.currentTimeMillis() + "."
                    + imageExtension
        )
    }



    /**Function will get the extension of type of the profile image */
    private fun getFileExtension(activity: Activity?, uri: Uri?): String?
    {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity?.contentResolver?.getType(uri!!))
    }

    /**
     * Called after the user has given permission to use the external memory and camera
     * Creates and starts an Activity for result that will open the user's gallery folder.
     * The user can then choose the image they wish for their personal user profile
     * */
    private fun showImageChooser(activity: Activity?)
    {
        val galleryIntent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    /**Automatically called after startActivityForResult
     * Function will set the image of the user profile.
     * Function will call the uploadUserImagetoCloud*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val userPic = binding.ivUserPhoto
        if(resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE){

            if(data != null)
            {
                try {
                    Toast.makeText(context,"Got URI",Toast.LENGTH_LONG).show()
                    imageUri = data?.data!!
                    userPic.setImageURI(imageUri)
                    //Not necessarily the best location for this
                    //However, since it takes time to receive the URL
                    //Placing it here has led to successful results.
                    //Still must determine if there is a better solution
                    uploadUserImageToCloud(activity,imageUri)
                }catch (e: IOException){
                    e.printStackTrace()
                    Toast.makeText(context, "Image Selection Failed", Toast.LENGTH_LONG).show()
                }
            }
        }
        else
        {
            Toast.makeText(context,"Something went Wrong trying to access image files", Toast.LENGTH_LONG).show()
        }

    }
    /**Check whether the User has already given permissions to the application
     * If permission has not been granted, Launch the request for permission*/
    private fun getUserImage(view: View){
        //Check if Permission have already been granted
        if(ContextCompat.checkSelfPermission(view.context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(view.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            showImageChooser(activity)
        }
        else //Request for Permissions
        {
            //Launch the permission launcher
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE))
        }
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
                imageURL = taskSnapshot.metadata!!.reference!!.downloadUrl.toString()

            }.addOnFailureListener {
                Toast.makeText(context,"Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }


    /**Function Returns whether any User input is empty
     * Displays a toast to the user to fill in property*/
    private fun validateEntries(view: View):Boolean{
        return when{
            TextUtils.isEmpty(binding.etDisplayName.text.toString().trim{it <= ' '})-> {
                Toast.makeText(view.context,"Please Enter a Display Name", Toast.LENGTH_LONG).show()
                //TODO check if user name is unique.
                return false
            }
            TextUtils.isEmpty(binding.etPersonalBio.text.toString().trim{it <= ' '})->{
                Toast.makeText(view.context,"Please Enter a short Bio", Toast.LENGTH_LONG).show()
                return false
            }
            TextUtils.isEmpty(binding.btnBirthdayPicker.text.toString()) -> {
                Toast.makeText(view.context,"Please Select Your Date of Birth", Toast.LENGTH_LONG).show()
                return false
            }
            imageURL == "holder"  -> {
                Toast.makeText(view.context,"Please Enter a profile image", Toast.LENGTH_LONG).show()
                return false
            }
            else->{return true}
        }
    }

    /**Get the current Users UID
     * Currently only returns the UID of a Specific user
     * This function will be used once User Registration fragment has been implemented*/
    private fun getCurrentUserID(): String{
        // Get the instance of the current user
        val currentUser = FirebaseAuth.getInstance().currentUser

        //check whether we received a user Instance
        var currentUserId = ""
        if(currentUser != null)
        {
            currentUserId = currentUser.uid
        }
        return "u8hmrDrrQqSCr7fWqxBYL9XlgNH2"
    }



    /**Opens a Date picker dialog that allows the user to show their birthday
     * A Calender window will pop up and they can choose the date. */
    private fun birthdayPicker()
    {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this.requireContext(),
            DatePickerDialog.OnDateSetListener { view, yearSelected, monthSelected, daySelected ->
                //Create a String with mm/dd/yyyy format
                val dateSelected = "${monthSelected+1}/$daySelected/$yearSelected"
                //set the text of the birthdayPicker button
                binding.btnBirthdayPicker.text = dateSelected

                //Create a simple date format, format the date string
                val sdf = SimpleDateFormat("mm/dd/yyyy", Locale.US)
                birthday = sdf.toString()

            }
            ,year,month,day)

        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }
}