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
    private lateinit var email: String //email variable
    private lateinit var password: String //password variable
    private lateinit var phone_number: String //phone number variable
    private lateinit var user: User // user variable for user class
    private lateinit var MFA_opt: String // variable to check for MFA enrollment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth //Firebase.auth initialization
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

            MFA_opt = ""
            MFA_opt = onCheckboxClicked()

            //user info value to store all user information in registration
            val userInfo = User(email, phone_number, MFA_opt)

            // Backend function to complete registration
            registration(email, password)

            //Adds info to database
            db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener { documents ->
                    if(documents.isEmpty)
                    {
                        //Checks if email is empty
                        if(binding.fragmentRegistrationEmailEt.text.isEmpty())
                        {
                            binding.fragmentRegistrationEmailEt.error = "Missing email"
                        }
                        else
                        {
                            db.collection("users").document(email).set(userInfo)
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

    //function to check if the MFA enrollment checkbox is checked
    private fun onCheckboxClicked(): String {
        if (view is CheckBox){
            val checked: Boolean = (view as CheckBox).isChecked
            when((view as CheckBox).id){
                R.id.fragment_registration_MFA_enrollment_box ->{
                    if (checked) {
                        MFA_opt = "Enrolled"
                    }
                    else
                    {
                        MFA_opt = "Not Enrolled"
                    }
                }
            }
        }
        return MFA_opt
    }

    //Registration function
    private fun registration(email: String, password: String){

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
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser
                            findNavController().navigate(R.id.action_registrationFragment_to_initialProfilePersonalizationFragment)

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(activity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

            }
        }
    }

}