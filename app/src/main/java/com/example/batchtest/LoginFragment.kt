package com.example.batchtest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth //authentication variable
    //binding variables
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var email: String //email
    private lateinit var password: String //password

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth //Firebase.auth initialization
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

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

        /*
        The login button will attempt to sign in the user via firebase
        It will read the username edit text and password edit text
        Then pass it into the signIn function
         */
        binding.fragmentLoginLoginBtn.setOnClickListener {
            email = binding.fragmentLoginUsernameEt.text.toString()
            password = binding.fragmentLoginPasswordEt.text.toString()
            signIn(email, password)
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

    //A simple logcat tag for this fragment
    //Used for debugging purposes
    companion object {
        private const val TAG = "LoginFragment"
    }

}