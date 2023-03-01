package com.example.batchtest

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentAccountSettingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AccountSettingFragment : Fragment() {

    private var _binding: FragmentAccountSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth //Firebase.auth initialization
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountSettingBinding.inflate(layoutInflater, container, false)

        /**
         * Navigate back to the user profile tab fragment
         */
        binding.btnToUserProfileTab.setOnClickListener{

            findNavController().navigate(R.id.action_accountSettingFragment_to_userProfileTabFragment)
        }


        /**
         * change to dark mode
         */
        binding.darkModeBtn.setOnCheckedChangeListener{ buttonView, isChecked ->

            if(isChecked){
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            }
            else{
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }

        }
        /**
         * increase font size
         */
        binding.textSizeBtn.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked){

                Toast.makeText(context, "Testing text size", Toast.LENGTH_SHORT).show()

            }
            else{
                Toast.makeText(context, "Testing text size", Toast.LENGTH_SHORT).show()
            }
        }


        /**
         * user signs out of account
         */

        binding.logOutBtn.setOnClickListener{
            auth.signOut()
            val user = auth.currentUser
            if (user != null) {
//                Log.i(LoginFragment.TAG, "user is signed in")
//                Log.i(LoginFragment.TAG, user.email.toString())
            } else {
//                Log.i(LoginFragment.TAG, "user is signed out")
                findNavController().navigate(R.id.loginFragment)
            }
        }

        /**
         * Delete user account
         */

        binding.deleteAccountBtn.setOnClickListener{

        }




        return binding.root
    }


    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


