package com.example.batchtest

import android.content.Intent
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
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


class RegistrationFragment : Fragment() {




    //authentication variable
    private lateinit var auth: FirebaseAuth

    //binding variables
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    //email and password
    private lateinit var email: String //email variable
    private lateinit var password: String //password variable
    private lateinit var phone_number: String //phone number variable
    private lateinit var user: User // user variable for user class
    private lateinit var MFA_opt: String // variable to check for MFA enrollment

    //initializing variables for MFA enrollment
    private lateinit var credential: PhoneAuthCredential
    private lateinit var forceResendingToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var verificationId: String

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
            undoState = false,
            primaryGroup = null
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
            password = binding.fragmentRegistrationPasswordEt.text.toString()
            phone_number = binding.fragmentRegistrationPhoneNumberEt.text.toString()

            //Check box to see if user opt in for MFA
            MFA_opt = ""
            MFA_opt = onCheckboxClicked(binding.fragmentRegistrationMFAEnrollmentBox)


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
                            val userUID = Firebase.auth.currentUser?.uid //Grabs the userUID
                            val userInfo = User("", "", email, "", "", imageUrl = null, imageUri = null, "",
                                "", phone_number, MFA_opt, user.myGroups, user.matchedGroups, user.pendingGroups) // assigns all info from user class to userInfo

                            //Gigantic if block if MFA is not enrolled
                            if(MFA_opt != "Enrolled") {

                                if (userUID != null) { //Checks if CurrentUserUID is not NULL
                                    db.collection("users").document(userUID.toString())
                                        .set(userInfo) //database adds UUID to document and sets userinfo
                                        .addOnSuccessListener {
                                            findNavController().navigate(R.id.action_registrationFragment_to_initialProfilePersonalizationFragment) //if successful navigate
                                        }
                                        .addOnFailureListener { e ->
                                            Log.i(email, "Error writing document", e) //fails send error to logcat
                                        }
                                }
                                val user = Firebase.auth.currentUser

                                user!!.sendEmailVerification() //sends the user an email verification
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("print", "Email sent.")
                                        }
                                    }
                            }

                            //Gigantic else block if MFA is enrolled
                            else{
                                if (userUID != null) { //Checks if CurrentUserUID is not NULL
                                    db.collection("users").document(userUID.toString())
                                        .set(userInfo) //database adds UUID to document and sets userinfo
                                        .addOnSuccessListener {
                                            val user = Firebase.auth.currentUser
                                            if (user != null) {
                                                user.multiFactor.session.addOnCompleteListener{ task ->
                                                    if(task.isSuccessful){

                                                        val multiFactorSession: MultiFactorSession? = task.result

                                                        val callbacks = object : OnVerificationStateChangedCallbacks(){
                                                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                                this@RegistrationFragment.credential = credential
                                                            }

                                                            override fun onVerificationFailed(e: FirebaseException) {
                                                                if (e is FirebaseAuthInvalidCredentialsException){
                                                                    Log.d("print", "Invalid Request")
                                                                }
                                                                else if (e is FirebaseTooManyRequestsException){
                                                                    Log.d("print", "SMS quota has been exceeded")
                                                                }
                                                                Toast.makeText(activity, "Verification Failed", Toast.LENGTH_SHORT).show()
                                                            }

                                                            override fun onCodeSent( verificationID: String, forceResendingToken: ForceResendingToken){
                                                                this@RegistrationFragment.verificationId = verificationID
                                                                this@RegistrationFragment.forceResendingToken = forceResendingToken
                                                            }
                                                        }

                                                        val options = PhoneAuthOptions.newBuilder(auth)
                                                            .setPhoneNumber(phone_number)       // Phone number to verify
                                                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                                            .setActivity(requireActivity())                // Activity (for callback binding)
                                                            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                                            .build()
                                                        PhoneAuthProvider.verifyPhoneNumber(options)


                                                    }
                                                }
                                            }
                                            findNavController().navigate(R.id.action_registrationFragment_to_initialProfilePersonalizationFragment) //if successful navigate

                                        }
                                        .addOnFailureListener { e ->
                                            Log.i(email, "Error writing document", e) //fails send error to logcat
                                        }
                                }

                                val user = Firebase.auth.currentUser
                                user!!.sendEmailVerification() //sends the user an email verification
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("print", "Email sent.")
                                        }
                                    }
                            }


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