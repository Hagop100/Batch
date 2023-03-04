package com.example.batchtest.UserProfileTab

import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentAccountSettingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AccountSettingFragment : Fragment() {

    private var _binding: FragmentAccountSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var userInfo: User
    val db = Firebase.firestore

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
        binding.textSizeBtn.setOnCheckedChangeListener{ buttonView, isChecked ->
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
                Log.i(TAG, "user is signed out")
                findNavController().navigate(R.id.loginFragment)
            }
        }

        /**
         * Delete user account
         */
        binding.deleteAccountBtn.setOnClickListener{
            val user = Firebase.auth.currentUser!!
            val userID = auth.currentUser?.uid

            this.context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            ?.setTitle("Are you sure?")
            ?.setMessage("Proceed to delete account...")

             //yes to delete user account and navigate back to the registration page
            ?.setPositiveButton("YES")
            { dialog, which ->
                user.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        if (userID != null) {
                            db.collection("users").document(userID).delete().addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                                .addOnFailureListener {
                                        e -> Log.w(TAG, "Error deleting document", e)
                                }
                            Log.i(TAG, "user with account of ${user.email} is deleted")
                        }

                        findNavController().navigate(R.id.registrationFragment)
                    }

                }
            }
                //dismiss account deletion
                ?.setNegativeButton("CANCEL"){ dialog, which ->
                    dialog.dismiss()
                }?.show()


        }




        return binding.root
    }
    companion object {
        private const val TAG = "print" //for logcat debugging

    }


    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}




