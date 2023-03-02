package com.example.batchtest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth //Firebase.auth initialization
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        // Binds the registration button to a command
        binding.fragmentRegistrationBtn.setOnClickListener{

            // Grabs user input for email and password and assigns it to the variables
            email = binding.fragmentRegistrationEmailEt.text.toString()
            password = binding.fragmentRegistrationPasswordEt.text.toString()
            phone_number = binding.fragmentRegistrationPhoneNumberEt.text.toString()

            // Backend function to complete registration
            registration(email, password)

        }

        return binding.root
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

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(activity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                findNavController().navigate(R.id.action_registrationFragment_to_initialProfilePersonalizationFragment)
            }
        }
    }

}