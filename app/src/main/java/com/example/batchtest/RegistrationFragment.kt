package com.example.batchtest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegistrationFragment : Fragment() {

    //authentication variable
    private lateinit var auth: FirebaseAuth

    //binding variables
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    //email and password
    private lateinit var email: String//email variable
    private lateinit var password: String //password variable
    private lateinit var phone_number: String //phone number variable
    private lateinit var user: User // user variable for user class
    private lateinit var MFA_opt: String // variable to check for MFA enrollment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth //Firebase.auth initialization


        //initialize user class variables
        user = User(
            firstName = null,
            lastName = null,
            email = null,
            displayName = "",
            gender = "",
            imageUrl = null,
            imageUri = null,
            birthdate = "",
            personalBio = "",
            phoneNumber = null,
            MFA_Opt = "",
            myGroups = ArrayList(),
            matchedGroups = ArrayList(),
            pendingGroups = ArrayList(),
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val db = Firebase.firestore // database



        // Inflate the layout for this fragment
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        // Binds the registration button to a command
        binding.fragmentRegistrationBtn.setOnClickListener{

            // Grabs user input for email and password and assigns it to the variables
            email = binding.fragmentRegistrationEmailEt.text.toString()
            Log.i("print", "$email")
            password = binding.fragmentRegistrationPasswordEt.text.toString()
            Log.i("print", "$password")

            phone_number = binding.fragmentRegistrationPhoneNumberEt.text.toString()
            Log.i("print", "$phone_number")


            //Check box to see if user opt in for MFA
            MFA_opt = ""
            MFA_opt = onCheckboxClicked(binding.fragmentRegistrationMFAEnrollmentBox)

            //user info value to store all user information in registration
            val userInfo = User(email, MFA_opt, phone_number)


            val currentUser = Firebase.auth.currentUser?.uid
            // Backend function to complete registration
            registration(email, password, phone_number, MFA_opt)



            //Adds info to database with email as document title
//            db.collection("users").whereEqualTo("email", email).get()
//                .addOnSuccessListener { documents ->
//                    if(documents.isEmpty)
//                    {
//                        //Checks if email is empty
//                        if(binding.fragmentRegistrationEmailEt.text.isEmpty())
//                        {
//                            binding.fragmentRegistrationEmailEt.error = "Missing email"
//                        }
//                        else
//                        {
//                            db.collection("users").document(email).set(userInfo)
//                            Toast.makeText(this.context, "Account Created", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    else{
//                        for(doc in documents){
//                            if(doc.data.getValue("email") == email){
//                                binding.fragmentRegistrationEmailEt.error = "Email already taken"
//                            }
//                        }
//                    }
//                }
//                .addOnFailureListener{ e ->
//                    Log.i(email, "Error writing document", e)
//                }
            /**
             * after registration , just create the document with user info
             */
            //Adds info to database
            db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener { documents ->
                    Log.i("print", "email $email")
                    if(documents.isEmpty)
                    {
                        //Checks if email is empty
                        if(binding.fragmentRegistrationEmailEt.text.isEmpty())
                        {
                            binding.fragmentRegistrationEmailEt.error = "Missing email"
                        }
                        else
                        {


                            if (currentUser != null) {
                                db.collection("users").document(currentUser).set(userInfo)
                            }
                                Log.i("print", "i am here2")

                            Toast.makeText(this.context, "Account Created", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        for(doc in documents){
                            if(doc.data.getValue("email") == email){
                                binding.fragmentRegistrationEmailEt.error = "Email already taken"
                            }
                        }
                    }
                }
                .addOnFailureListener{ e ->
                    Log.i(email, "Error writing document", e)
                }


        }

        return binding.root
    }

    //Function to check if the MFA enrollment box is checked.
    fun onCheckboxClicked(view: View): String {
        if(view is CheckBox) {
            val checked: Boolean = view.isChecked

            when (view.id) {
                R.id.fragment_registration_MFA_enrollment_box -> {
                    if (checked) {
                        MFA_opt = "Enrolled"
                    }
                    else{
                        MFA_opt = "Not Enrolled"
                    }
                }
            }
        }
        return MFA_opt
    }

    //function to check if the MFA enrollment checkbox is checked
//    private fun onCheckboxClicked(): String {
//        if (view is CheckBox){
//            val checked: Boolean = (view as CheckBox).isChecked
//            when((view as CheckBox).id){
//                R.id.fragment_registration_MFA_enrollment_box ->{
//                    if (checked) {
//                        MFA_opt = "Enrolled"
//                    }
//                    else
//                    {
//                        MFA_opt = "Not Enrolled"
//                    }
//                }
//            }
//        }
//        return MFA_opt
//    }

    //Registration function
    private fun registration(email: String, password: String, phone_number: String, MFA_opt: String){
        val db = Firebase.firestore // database

        //if the email or password slot is empty prompt the user to enter email and password
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Please Enter Email and Password.", Toast.LENGTH_SHORT).show()
        }

        //When pressing the registration button sends the user to the profile personalization screen
        else{
            activity?.let{
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //val userUID = auth.createUserWithEmailAndPassword(email, password) // Grabs UUID from the
                                                                                               // createUserwithEmailandPassword function
                            val userUID = Firebase.auth.currentUser?.uid

                            val userInfo = User("", "", email, "", "", imageUrl = null, imageUri = null, "",
                                "", phone_number, MFA_opt, user.myGroups, user.matchedGroups, user.pendingGroups) // assigns all info from user class to userInfo

                            if (userUID != null) { //Checks if CurrentUserUID is not NULL
                                db.collection("users").document(userUID.toString()).set(userInfo) //database adds UUID to document and sets userinfo
                                    .addOnSuccessListener {
                                        findNavController().navigate(R.id.action_registrationFragment_to_initialProfilePersonalizationFragment) //if successful navigate
                                    }
                                    .addOnFailureListener{ e->
                                        Log.i(email, "Error writing document", e) //fails send error to logcat
                                    }
                            }
                            val user = auth.currentUser


                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(activity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

            }
        }
    }

    //Old Registration function for email
//    private fun registration(email: String, password: String){
//
//        //if the email or password slot is empty prompt the user to enter email and password
//        if(email.isEmpty() || password.isEmpty()) {
//            Toast.makeText(activity, "Please Enter Email and Password.", Toast.LENGTH_SHORT).show()
//        }
//
//        //When pressing the registration button sends the user to the profile personalization screen
//        else{
//            activity?.let{
//                auth.createUserWithEmailAndPassword(email, password)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            // Sign in success, update UI with the signed-in user's information
//                            val user = auth.currentUser
//                            findNavController().navigate(R.id.action_registrationFragment_to_initialProfilePersonalizationFragment)
//
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Toast.makeText(activity, "Authentication failed.",
//                                Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//            }
//        }
//    }
}