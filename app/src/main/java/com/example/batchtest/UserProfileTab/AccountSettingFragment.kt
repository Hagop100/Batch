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
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentAccountSettingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase


class AccountSettingFragment : Fragment() {

    private var _binding: FragmentAccountSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private lateinit var group: Group
    private val db = Firebase.firestore

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
         * Delete user account by deleting the users that belong in certain groups
         * and then delete the user in the User collection of db.
         */
        binding.deleteAccountBtn.setOnClickListener{
            val user = Firebase.auth.currentUser!!
            Log.i(TAG, "user: $user")
            val userId = auth.currentUser?.uid
            Log.i(TAG, "userID: $userId")

            this.context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            ?.setTitle("Are you sure?")
            ?.setMessage("Proceed to delete account...")

             //yes to delete user account and navigate back to the registration page
            ?.setPositiveButton("YES")
            { dialog, which ->

                //delete the user with the equivalent userID from User collection and authentication
                if (userId != null) {

                    db.collection("users").document(userId).get().addOnSuccessListener { result ->

                        val users: User? = result.toObject(User::class.java)

                        Toast.makeText(this.context, "${users?.myGroups }", Toast.LENGTH_SHORT).show()

                        //if the user does not have any group. delete the userid document and user authentication
                        if (users?.myGroups?.size == 0){
                            user.delete().addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    if (userId != null) {
                                        //delete the user with the equivalent userID from User collection and authentication
                                        db.collection("users").document(userId).delete()
                                            .addOnSuccessListener {

                                                Log.i(TAG, "$userId successfully deleted!") }
                                            .addOnFailureListener {
                                                    e -> Log.i(TAG, "Error deleting document", e)
                                            }

                                        Log.i(TAG, "user with account of ${user.email} is deleted")
                                    }

                                    findNavController().navigate(R.id.registrationFragment)
                                }

                            }
                        }

                        //if user belong in at least 1 group
                        else{
                            //retrieve all groups name that include this user
                            db.collection("groups").whereIn("name", users?.myGroups!!).get().addOnSuccessListener { res ->

                                //doc is the specific group //res.documents is the entire collection of groups document
                                for (doc in res.documents){

                                    //get the field:userID in the specific group in which we know are arraylist
                                    // if user is the only user in the group. delete the group.
                                    if ((doc.get("userID") as? ArrayList<*>)?.size == 1){

                                        //delete the group by retrieving the document (group name)
                                        db.collection("groups").document(doc.get("name").toString()).delete().addOnSuccessListener {
                                            Log.i(TAG, "I am here2")

                                        }
                                    }
                                }
                            }
                        }

                    }

                }
                //delete the user after deleting the group
                if (user != null){
                    user.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            if (userId != null) {
                                //delete the user with the equivalent userID from User collection and authentication
                                db.collection("users").document(userId).delete()
                                    .addOnSuccessListener {

                                        Log.i(TAG, "$userId successfully deleted!") }
                                    .addOnFailureListener {
                                            e -> Log.i(TAG, "Error deleting document", e)
                                    }

                                Log.i(TAG, "user with account of ${user.email} is deleted")
                            }

                            findNavController().navigate(R.id.registrationFragment)
                        }

                    }
                }

            }
                //dismiss account deletion when user select CANCEL
                ?.setNegativeButton("CANCEL"){ dialog, which ->
                    dialog.dismiss()
                }?.show()
        }

        return binding.root
    }

    private fun deleteUser(){

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




