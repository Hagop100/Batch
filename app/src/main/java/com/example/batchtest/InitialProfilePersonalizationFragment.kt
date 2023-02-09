package com.example.batchtest

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.batchtest.databinding.FragmentInitialProfilePersonalizationBinding
import java.text.SimpleDateFormat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [InitialProfilePersonalizationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InitialProfilePersonalizationFragment : Fragment() {

    private var _binding: FragmentInitialProfilePersonalizationBinding? = null
    private val binding get() = _binding!!

    private lateinit var displayName: String
    private lateinit var birthday: Date
    private lateinit var personalBio: String
    private lateinit var gender: String
    private lateinit var imageUri: String

    /**Will hold constant for this class*/
    companion object{
        private const val CAMERA_PERMISSION_CODE = 1
        private const val EXTERNAL_STORAGE_PERMISSION_CODE = 2
        private const val PICK_IMAGE_REQUEST_CODE =44
        private const val GENDER = "gender"


        fun showImageChooser(activity: Activity?)
        {

            val galleryIntent = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            activity?.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInitialProfilePersonalizationBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Ask for permission for camera and external storage
        //Select picture for userProfile
        binding.btnImagePrompt.setOnClickListener {

        }
        //On click, will bring up a DatePicker Dialog so the user can set their date of birth
        binding.btnBirthdayPicker.setOnClickListener{
            birthdayPicker()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    //Open a DatePicker Dialog that the user can use to select their birthdate
    private fun birthdayPicker(): Calendar
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
                birthday = sdf.parse(dateSelected) as Date

            }
            ,year,month,day)

        //TODO: Make sure the date entered is corresponds to the user being over 18
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
        return calendar
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
            birthday == null -> {
                Toast.makeText(view.context,"Please Select Your Date of Birth", Toast.LENGTH_LONG).show()
                return false
            }
            imageUri == null -> {
                Toast.makeText(view.context,"Please Enter a profile image", Toast.LENGTH_LONG).show()
                return false
            }
            else->{return true}
        }
    }

    /**Get the current Users UID */
    fun getCurrentUserID(): String{
        // Get the instance of the current user
        val currentUser = FirebaseAuth.getInstance().currentUser

        //check whether we received a user Instance
        var currentUserId = ""
        if(currentUser != null)
        {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun completeUserProfileData(fragment: Fragment, userHasMap: HashMap<String, Any>)
    {

    }

    /**Permission Requests*/
    fun getUserImage(view: View){
        //Check if Permission have already been granted
        if(ContextCompat.checkSelfPermission(view.context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            //TODO application already has access, go to image selection
        }
        else //Request for Permissions
        {
            //TODO Permission not yet established. Request for PERMISSION.
            //Set up the Request Permission Launcher
            val requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
                ){ permissions ->
                    permissions.entries.forEach {
                        val permissionName = it.key
                        val isGranted = it.value
                        if(isGranted)
                        {
                            Toast.makeText(view.context,"$permissionName is Granted", Toast.LENGTH_SHORT).show()
                        }
                        else
                        {
                            Toast.makeText(view.context,"$permissionName is Denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            //Launch the permission launcher
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

}