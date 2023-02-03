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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var email: String //email
    private lateinit var password: String //password

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        /*
        Pressing login button should check firebase and log in if the user is found to be in the database
         */
        binding.fragmentLoginLoginBtn.setOnClickListener {
            email = binding.fragmentLoginUsernameEt.text.toString()
            password = binding.fragmentLoginPasswordEt.text.toString()
            findNavController().navigate(R.id.action_loginFragment_to_matchTabFragment)
            /* The sign In function seems to work but is having trouble connecting to firebase
            For now, the login button continues to the next fragment without logging in.
             */
            //signIn(email, password)
        }

        //This grabs the nav_bar and sets it visible upon this fragment's onCreateView
        val navBar: BottomNavigationView? = getActivity()?.findViewById(R.id.nav_bar)
        navBar?.visibility = View.INVISIBLE

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    This is the sign in function but is not working correctly yet
     */
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener() {
            if(it.isSuccessful) {
                Log.i("print", "succeeded")
                findNavController().navigate(R.id.action_loginFragment_to_matchTabFragment)
            }
            else {
                Log.i("print", "failed")
                Toast.makeText(activity, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}