package com.example.batchtest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
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
import com.bumptech.glide.Glide
import com.example.batchtest.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.net.URI

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

    private val userHashMap = HashMap<String, Any>()

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

        getUserDataFromDatabase()

        binding.btnImageUpdate.setOnClickListener{
            getUserImage(view)
        }
        binding.btnUpdateProfile.setOnClickListener {
            if(validateEntries(it))
            {
                updateUserProfileDatabase()
            }
        }
    }

    //Must Unbind the View
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    /**
     * Retrieve the user's data from the database and populate
     * the edit text views with their current information.
     * */
    private fun getUserDataFromDatabase()
    {

        FirebaseFirestore.getInstance().collection("users")
            .document(getCurrentUserID()).get().addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)!!
                val url = user.imageUrl
                Toast.makeText(context, "$url", Toast.LENGTH_LONG).show()
                val profileImage = binding.civEditProfileImage

                //User will not be able to update their email or birthday.
                binding.etEmail.setText(user.email)
                binding.etEmail.isEnabled = false
                binding.etBirthday.setText(user.birthdate)
                binding.etBirthday.isEnabled = false
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

                //set the retrieved data as the default values
                initializeDefaultValues(user)


                //Update imageView with image stored in the database
                //Use placeholder in case it fails to get the url
                Glide.with(this).load(url).
                placeholder(R.drawable.person_icon).into(profileImage)

            }.addOnFailureListener{ e ->
                Toast.makeText(context, "Unable to retrieve User Info $e", Toast.LENGTH_SHORT).show()
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
        firstName = binding.etFirstName.text.toString().trim()
        lastName = binding.etLastName.text.toString().trim()
        gender = when(binding.rgGender.checkedRadioButtonId)
        {
            binding.maleRadioButton.id -> MALE
            binding.femaleRadioButton.id-> FEMALE
            else -> NONBINARY
        }

        birthday = binding.etBirthday.text.toString()
        personalBio = binding.etBio.text.toString()
        userHashMap[FIRSTNAME] = firstName
        userHashMap[LASTNAME] = lastName
        userHashMap[EMAIL] = email
        userHashMap[DISPLAYNAME] = displayName
        userHashMap[GENDER] = gender
        userHashMap[BIRTHDATE] = birthday
        userHashMap[PERSONALBIO] = personalBio
        userHashMap[IMAGEURI] = imageUri
        userHashMap[IMAGEURL] = imageURL

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

    private fun initializeDefaultValues(user: User)
    {

        displayName = user.displayName
        birthday = user.birthdate
        personalBio = user.personalBio
        gender = user.gender
        imageUri = user.imageUri!!
        firstName = user.firstName!!
        lastName = user.lastName!!
        imageURL = user.imageUrl!!
        email = user.email!!
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
        return when{
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
            TextUtils.isEmpty(imageURL)  -> {
                Toast.makeText(view.context,"Please Enter a profile image", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context,"Got URI",Toast.LENGTH_LONG).show()
                    tempUri = data.data!!
                    Glide.with(this).load(tempUri).into(userPic)
                    //Not necessarily the best location for this
                    //However, since it takes time to receive the URL
                    //Placing it here has led to successful results.
                    //Still must determine if there is a better solution
                    uploadUserImageToCloud(activity,tempUri)
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
}