package com.example.batchtest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executor
import kotlin.properties.Delegates


class LoginFragment : Fragment() {

    //authentication variable
    private lateinit var auth: FirebaseAuth

    //binding variables
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    //SharedPreferences
    private lateinit var myPrefs: SharedPreferences

    //email/password
    private lateinit var email: String //email
    private lateinit var password: String //password

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth //Firebase.auth initialization
        myPrefs = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE) //sharedPreferences initialization
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        /*
        A debugging tool to check if the user is signed in or not upon running the application
        If a user is not explicitly signed out, then it is the case that the user could be
        logged in or out depending on the startup of the application
         */
        val user = auth.currentUser
        if (user != null) {
            Log.i(TAG, "user is signed in")
            Log.i(TAG, user.email.toString())
        } else {
            Log.i(TAG, "user is signed out")
        }

        //Sign Up navigates to registration fragment
        binding.fragmentLoginSignUpBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        /*
        This code block handles automatically loading the email/password without having to type it out again
        if you already selected the rememberMe checkBox.
        If the rememberMe checkBox was checked when logging in at an earlier time,
        then it is the case that we have some preferences saved (email/password).
        If we do have preferences saved, then we want to automatically input them into our
        EditText views. We will then check the rememberMe checkBox again.
         */
        if(myPrefs.getString(emailKey, null) != null && myPrefs.getString(passwordKey, null) != null) {
            binding.fragmentLoginUsernameEt.setText(myPrefs.getString(emailKey, null))
            binding.fragmentLoginPasswordEt.setText(myPrefs.getString(passwordKey, null))
            binding.fragmentLoginRememberMeBtn.isChecked = true
        }

        /*
        The login button will attempt to sign in the user via firebase
        It will read the username edit text and password edit text
        Then pass it into the signIn function
         */
        binding.fragmentLoginLoginBtn.setOnClickListener {
            //We store the email and password upon clicking the login button
            email = binding.fragmentLoginUsernameEt.text.toString()
            password = binding.fragmentLoginPasswordEt.text.toString()
            /*
            If we have the rememberMe checkBox checked when we press the login button,
            then store the email and password into our sharedPreferences object.
            If we have the rememberMe checkBox unchecked when we press the login button,
            then clear the sharePreferences object from any data we had stored in there.
             */
            if(binding.fragmentLoginRememberMeBtn.isChecked) {
                val editor: SharedPreferences.Editor = myPrefs.edit()
                editor.putString(emailKey, email)
                editor.putString(passwordKey, password)
                editor.apply()
            }
            else {
                myPrefs.edit().clear().apply()
            }
            //signIn function using firebase API
            //This is the real code
            signIn(email, password)

            //This is for testing for now
            //findNavController().navigate(R.id.action_loginFragment_to_matchTabFragment)
        }

        /*
        This is the forgot Password button where it prompts you to enter an email with a dialog fragment
        Upon entering your email, you should receive an email about resetting your password
         */
        binding.fragmentLoginForgotPasswordBtn.setOnClickListener {
            val dialog = ForgotPasswordDialogFragment()
            dialog.show(childFragmentManager, "forgotPasswordDialog")
        }


        /*
        This button remains for debugging purposes and will be removed upon release of the final build
        The user is signed out upon clicking the sign out button
        If the user is signed in, then it will display that in the logcat(although this should never occurr)
        If the user is logged out, then it will display that in the logcat
         */
        binding.fragmentLoginSignOutBtn.setOnClickListener {
            auth.signOut()
            val user = auth.currentUser
            if (user != null) {
                Log.i(TAG, "user is signed in")
                Log.i(TAG, user.email.toString())
            } else {
                Log.i(TAG, "user is signed out")
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    Function allows you to sign in with proper email and password authentication via Firebase
     */
    private fun signIn(email: String, password: String) {
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Please Enter Email and Password.", Toast.LENGTH_SHORT).show()
        }
        else {
            activity?.let {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(it) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            findNavController().navigate(R.id.action_loginFragment_to_matchTabFragment)
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(activity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

    }

    companion object {
        private const val TAG = "LoginFragment" //for logcat debugging
        private const val emailKey: String = "email" //key for emails in SharedPreferences
        private const val passwordKey: String = "password" //key for passwords in SharedPreferences
    }

}