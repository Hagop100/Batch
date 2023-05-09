package com.example.batchtest.UserProfileTab

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.batchtest.FirebaseMessaging.MyFirebaseMessagingService
import com.example.batchtest.InitialProfilePersonalizationFragment
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentEditProfileBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

//TODO update the code so it considers when a user skips the initial profile initialization
class EditProfileFragment : Fragment() {

    //Binding Values
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    //Variables
    private lateinit var displayName: String
    private lateinit var birthday: String
    private lateinit var personalBio: String
    private lateinit var gender: String
    private lateinit var imageUri: String
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var imageURL: String
    private lateinit var email: String
    private var tempUri: Uri? = null
    private lateinit var user: User
    private lateinit var progressDialog: Dialog
    private lateinit var firebaseUser: FirebaseUser
    //private lateinit var token: String
    private var isToken: Boolean = false
    private val userHashMap = HashMap<String, Any>()
    //variable used to check whether URI has been set
    private var uriSet = false

    //ActivityResultLauncher must be initialized onCreate
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    companion object{
        const val PICK_IMAGE_REQUEST_CODE =44
        const val GENDER = "gender"
        const val MALE: String = "male"
        const val FEMALE: String = "female"
        const val NONBINARY: String = "nonbinary"
        const val DISPLAYNAME: String = "displayName"
        const val IMAGEURL: String = "imageUrl"
        const val IMAGEURI: String = "imageUri"
        const val BIRTHDATE: String = "birthdate"
        const val PERSONALBIO: String = "personalBio"
        const val USER_PROFILE_IMAGE: String = "User_Profile_Image"
        const val FIRSTNAME: String = "firstName"
        const val LASTNAME: String ="lastName"
        const val EMAIL: String = "email"
        const val PROFILECOMPLETE = "profileComplete"
        const val TOKEN = "userToken"
        const val NEW_MATCHES= "matches"
        const val NEW_MESSAGES = "messages"
        const val VOTING = "voting"
        const val NEW_GROUP_MEMBERS = "members"
        const val NOTIFICATION_PREFS = "notificationPrefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Must Register for activity result in OnCreate, Can only be created on initialization
        //Requesting the user for permission to access their storage and camera, will be called in onViewCreated
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
//            if (!it.isSuccessful)
//            {
//                Log.i("EditFragment","Fetching FCM token failed", it.exception)
//
//                return@OnCompleteListener
//            }
//            else
//            {
//                token = it.result
//                Log.i("Token",token)
//
//            }
//
//        })

        //Update the screen with the basic configuration
        updateEmptyProfilePage()

        getUser()

        //On click, will bring up a DatePicker Dialog so the user can set their date of birth
        binding.btnBirthday.setOnClickListener{
            birthdayPicker()
        }

        binding.btnImageUpdate.setOnClickListener{
            getUserImage(view)
        }
        binding.btnUpdateProfile.setOnClickListener {
            if(validateEntries(it))
            {
                updateUserProfileDatabase()
            }
        }
        binding.closeBtn.setOnClickListener {
            findNavController().navigate(R.id.userProfileTabFragment)
        }
    }

    //Must Unbind the View
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    /**
     * populate the edit text views with the user's current information.
     * */
    private fun updateProfilePage()
    {
        //User is allowed to complete profile without entering an image.
        if(user.imageUrl != null)
        {
            val url = user.imageUrl
            val profileImage = binding.civEditProfileImage
            //Update imageView with image stored in the database
            //Use placeholder in case it fails to get the url
            Glide.with(this).load(url).
            placeholder(R.drawable.person_icon).into(profileImage)
        }

        //User will not be able to update their email or birthday.
        binding.etEmail.setText(user.email)
        binding.etEmail.isEnabled = false
        binding.btnBirthday.text = user.birthdate

        binding.etFirstName.setText(user.firstName)
        binding.etLastName.setText(user.lastName)
        binding.etDisplayName.setText(user.displayName)

        //check the correct radio button.
        binding.rgGender.check(when(user.gender){
            "male" -> binding.maleRadioButton.id
            "female" -> binding.femaleRadioButton.id
            else -> binding.nonBinaryRadioButton.id
        })
        binding.etBio.setText(user.personalBio)

        initializeDefaultValues(user)
    }

    /**
     * This will populate the views with default hints
     * */
    private fun updateEmptyProfilePage()
    {
        binding.civEditProfileImage.setImageResource(R.drawable.profile_icon)
        binding.etEmail.setText(R.string.personalEmail)
        binding.etEmail.isEnabled = false
        binding.btnBirthday.setHint(R.string.birthday)
        binding.etFirstName.setHint(R.string.firstName)
        binding.etLastName.setHint(R.string.lastName)
        binding.etDisplayName.setHint(R.string.displayName)
        binding.etBio.setHint(R.string.personalBio)
    }

    /**
     * Retrieve the User Data from Firestore database
     * Return whether profile is complete or not
     * */
    private fun getUser()
    {
        showProgressDialog()
        FirebaseFirestore.getInstance().collection("users")
            .document(getCurrentUserID()).get().addOnSuccessListener { document ->
                user = document.toObject(User::class.java)!!
                if (user.profileComplete)
                {
                    updateProfilePage()
                }
                //Even if profile is not complete email will be set
                binding.etEmail.setText(user.email)
                dismissProgressDialog()
            }.addOnFailureListener {
                Toast.makeText(context, "Unable to retrieve User Info $it", Toast.LENGTH_SHORT).show()
            }
    }


    /**
     * Gathers the user's input and places it in a hashmap
     * Updates User details in the Firestore Database
     * */
    private fun updateUserProfileDatabase()
    {
//        /**For Testing*/
//        val notifPrefs = HashMap<String, Any>()
//        notifPrefs[VOTING] = false
//        notifPrefs[NEW_MATCHES] = false
//        notifPrefs[NEW_MESSAGES] = true
//        notifPrefs[NEW_GROUP_MEMBERS] = true
//        /**  **********          *************/
        //Get the values the user has input
        //Save values in a Hashmap that will be used to update the user's information
        //in the firebase database.
        displayName = binding.etDisplayName.text.toString().trim()
        firstName = binding.etFirstName.text.toString().trim()
        lastName = binding.etLastName.text.toString().trim()
        gender = when(binding.rgGender.checkedRadioButtonId)
        {
            binding.maleRadioButton.id -> MALE
            binding.femaleRadioButton.id-> FEMALE
            else -> NONBINARY
        }

        birthday = binding.btnBirthday.text.toString()
        personalBio = binding.etBio.text.toString()
        userHashMap[FIRSTNAME] = firstName
        userHashMap[LASTNAME] = lastName
        userHashMap[DISPLAYNAME] = displayName
        userHashMap[GENDER] = gender
        userHashMap[BIRTHDATE] = birthday
        userHashMap[PERSONALBIO] = personalBio
        userHashMap[PROFILECOMPLETE] = true

        /**Temp Values that should be set in initialprofileInitialization or login*/
        //userHashMap[TOKEN] = token
        //userHashMap[NOTIFICATION_PREFS] = notifPrefs //


        if (uriSet)
        {
            userHashMap[IMAGEURI] = imageUri
            userHashMap[IMAGEURL] = imageURL
        }


        //Get a instance of the Firebase Firestore database and update the user's information
        FirebaseFirestore.getInstance().collection("users")
            .document(getCurrentUserID())
            .update(userHashMap).addOnSuccessListener {
                Toast.makeText(context,"Successfully Updated Profile", Toast.LENGTH_SHORT).show()
                //Will navigate to the next fragment, Currently not in use should put
                findNavController().navigate(R.id.action_editProfileFragment_to_userProfileTabFragment)
            }.addOnFailureListener{
                Toast.makeText(context,"Failed to Update Profile", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Initialize the variables to those passed in from the database
     * */
    private fun initializeDefaultValues(user: User)
    {

        displayName = user.displayName
        birthday = user.birthdate
        personalBio = user.personalBio
        gender = user.gender
        imageUri = user.imageUri.toString()
        firstName = user.firstName.toString()
        lastName = user.lastName.toString()
        imageURL = user.imageUrl.toString()
        email = user.email.toString()
    }

    /**
     * Get the UID of the current logged in User
     * */
    private fun getCurrentUserID(): String{
        // Get the instance of the current user
        val currentUser = FirebaseAuth.getInstance().currentUser

        //check whether we received a user Instance
        var currentUserId = ""
        if(currentUser != null)
        {
            currentUserId = currentUser.uid
        }
        return currentUser!!.uid
    }

    /**Function Returns whether any User input is empty
     * Displays a toast to the user to fill in property*/
    private fun validateEntries(view: View):Boolean{
         when{
            TextUtils.isEmpty(binding.etFirstName.text.toString().trim{it <= ' '})-> {
                Toast.makeText(view.context,"Please Enter Your First Name", Toast.LENGTH_LONG).show()
                return false
            }
            TextUtils.isEmpty(binding.etLastName.text.toString().trim{it <= ' '})-> {
                Toast.makeText(view.context,"Please Enter Your Last Name", Toast.LENGTH_LONG).show()
                return false
            }
            TextUtils.isEmpty(binding.etDisplayName.text.toString().trim{it <= ' '})-> {
                Toast.makeText(view.context,"Please Enter a Display Name", Toast.LENGTH_LONG).show()
                //TODO check if user name is unique.
                return false
            }
            TextUtils.isEmpty(binding.etBio.text.toString().trim{it <= ' '})->{
                Toast.makeText(view.context,"Please Enter a short Bio", Toast.LENGTH_LONG).show()
                return false
            }
            else->{return true}
        }
    }

    /**
     * Get A Firebase Storage reference to save the user image to
     * return Firebase Reference
     * */
    private fun getStorageReference(): StorageReference {
        //get the image's file type
        val imageExtension = getFileExtension(activity, tempUri)

        return FirebaseStorage.getInstance().reference.child(
            InitialProfilePersonalizationFragment.USER_PROFILE_IMAGE + System.currentTimeMillis() + "."
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
        startActivityForResult(galleryIntent,
            InitialProfilePersonalizationFragment.PICK_IMAGE_REQUEST_CODE
        )
    }

    /**Automatically called after startActivityForResult
     * Function will set the image of the user profile.
     * Function will call the uploadUserImagetoCloud*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val userPic = binding.civEditProfileImage
        if(resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE){

            if(data != null)
            {
                try {
                    tempUri = data.data!!
                    uriSet = true
                    Glide.with(this).load(tempUri).into(userPic)
                    //Not necessarily the best location for this
                    //However, since it takes time to receive the URL
                    //Placing it here has led to successful results.
                    //Still must determine if there is a better solution
                    //Progress Dialog giving time to get the storage locations URL
                    //This might not be the best location, however, it is the
                    //only location that it works since uploadUserImage is an asynchronous listener.

                    lifecycleScope.launch {
                        uploadUserImageToCloud(activity,tempUri)
                    }

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

    /**
     * Using the Firebase Storage Reference, upload the user image to
     * the Firebase Storage area.
     * The SuccessListener returns the URL of the image.
     * imageURL is updated with the returned URl
     * */
    private fun uploadUserImageToCloud(activity: Activity?, imaUri: Uri?)
    {
        val storageReference: StorageReference = getStorageReference()

        storageReference.putFile(imaUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // Get the downloadable url from the task snapshot
                // The Success Listener is Asynchronous, and any following code will run
                // Getting the TaskSnapshot takes a bit of time,
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    imageURL = uri.toString()
                    imageUri = imaUri.toString()
                    dismissProgressDialog()
                }

            }.addOnFailureListener {

                Toast.makeText(context,"Failed to upload image", Toast.LENGTH_SHORT).show()
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
     * Function Will Display a progress dialog window.
     * that can only be dismissed by calling dismissProgressDialog()
     * */
    private fun showProgressDialog()
    {
        progressDialog = Dialog(requireContext())

        progressDialog.setContentView(R.layout.dialog_progress)

        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

        progressDialog.show()
    }

    /**
     * Called to dismiss the progress dialog
     * */
    private fun dismissProgressDialog()
    {
        progressDialog.dismiss()
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
                binding.btnBirthday.text = dateSelected

                //Create a simple date format, format the date string
                val sdf = SimpleDateFormat("mm/dd/yyyy", Locale.US)
                birthday = sdf.toString()

            }
            ,year,month,day)

        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }
}