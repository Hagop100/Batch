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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentAccountSettingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class AccountSettingFragment : Fragment() {


    private var _binding: FragmentAccountSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private lateinit var group: Group
    private val db = Firebase.firestore

    private val notificationPrefs = HashMap<String, Any>()

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

        updateNotificationView()
        /**
         * Notification Checks
         * Check all switch buttons and update hashmap
         * ****REMOVED ALL NOTIFICATIONS**** required to many checks
         * */
        binding.newGroupMemNotif.setOnCheckedChangeListener { newGroupMem, isChecked ->
            notificationPrefs[NEW_GROUP_MEMBERS] = isChecked
        }
        binding.votingNotif.setOnCheckedChangeListener { newGroupMem, isChecked ->
            notificationPrefs[VOTING] = isChecked
        }
        binding.newMatchesNotif.setOnCheckedChangeListener { newGroupMem, isChecked ->
            notificationPrefs[NEW_MATCHES] = isChecked
        }
        binding.newMessagesNotif.setOnCheckedChangeListener { newGroupMem, isChecked ->
            notificationPrefs[NEW_MESSAGES] = isChecked
        }

        /**Updates the Users preferences*/
        binding.btnUpdateNotif.setOnClickListener { view ->
            viewLifecycleOwner.lifecycleScope.launch {
                val userId = auth.currentUser!!.uid
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update(NOTIFICATION_PREFS, notificationPrefs).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Notification Updated", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Notification Update Failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }



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

//                        Toast.makeText(this.context, "${users?.myGroups }", Toast.LENGTH_SHORT).show()

                        if (users?.myGroups!!.isNotEmpty()) {
                            //retrieve all groups name that include this user. this case ALSO handles the user with 0 group.
                            db.collection("groups").whereIn("name", users.myGroups).get()
                                .addOnSuccessListener { res ->

                                    //doc is the specific group //res.documents is the entire collection of groups document
                                    for (doc in res.documents) {

                                        //get the field:userID in the specific group in which we know are arraylist
                                        // if user is the only user in the group. delete the group.
                                        if ((doc.get("users") as? ArrayList<*>)?.size == 1) {

                                            //delete the group by retrieving the document (group name)
                                            db.collection("groups")
                                                .document(doc.get("name").toString()).delete()
                                                .addOnSuccessListener {

                                                    Log.i(TAG, "groups are deleted!")

                                                }
                                        }
                                        //if the group contains more than 1 user. remove the user from the userId arraylist and then update arraylist
                                        else {
                                            db.collection("groups")
                                                .document(doc.get("name").toString()).get()
                                                .addOnSuccessListener { result ->
                                                    val group: Group =
                                                        result.toObject(Group::class.java)!!

                                                    //go to the group, remove user from myGroups
                                                    group.users?.remove(userId)
                                                }

                                        }
                                    }
                                }
                        }

                        //else

                    }


                }
                //delete the user after deleting the group
                user.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        if (userId != null) {
                            //delete the user with the equivalent userID from User collection and authentication
                            db.collection("users").document(userId).delete()
                                .addOnSuccessListener {

                                    Log.i(TAG, "$userId successfully deleted!")
                                    //sign out
                                    auth.signOut()

                                    //navigate user back to login screen
                                    findNavController().navigate(R.id.loginFragment)

                                }
                                .addOnFailureListener {
                                        e -> Log.i(TAG, "Error deleting document", e)
                                }

                            Log.i(TAG, "user with account of ${user.email} is deleted")
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
        const val NEW_MATCHES= "matches"
        const val NEW_MESSAGES = "messages"
        const val VOTING = "voting"
        const val NEW_GROUP_MEMBERS = "members"
        const val NOTIFICATION_PREFS = "notificationPrefs"
    }


    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Update the notification selection according to user preferences
     * Function retrieves current user and sets the switch buttons according to preferences
     * set in the database
     * */
    private fun updateNotificationView()
    {
        /**Using a coroutine to gather the user's data*/
        viewLifecycleOwner.lifecycleScope.launch {
            FirebaseFirestore.getInstance().collection("users").document(auth.currentUser!!.uid).get()
                .addOnSuccessListener {
                    val user = it.toObject(User::class.java)!!
                    if(user.notificationPrefs != null)
                    {
                        val notifPrefs = user.notificationPrefs

                        Log.i(TAG, notifPrefs.toString())

                        /**Set switches according to user notification preferences*/
                        binding.votingNotif.isChecked = notifPrefs?.get(VOTING)!!
                        binding.newMessagesNotif.isChecked = notifPrefs.get(NEW_MESSAGES)!!
                        binding.newGroupMemNotif.isChecked = notifPrefs.get(NEW_GROUP_MEMBERS)!!
                        binding.newMatchesNotif.isChecked = notifPrefs.get(NEW_MATCHES)!!

                        /**Initialize the notificationPrefs in case the user decides to update*/
                        notificationPrefs[VOTING] = notifPrefs.get(VOTING)!!
                        notificationPrefs[NEW_MESSAGES] = notifPrefs.get(NEW_MESSAGES)!!
                        notificationPrefs[NEW_GROUP_MEMBERS] = notifPrefs.get(NEW_GROUP_MEMBERS)!!
                        notificationPrefs[NEW_MATCHES]= notifPrefs.get(NEW_MATCHES)!!
                    }
                    else
                    {
                        binding.votingNotif.isChecked = false
                        binding.newMessagesNotif.isChecked = false
                        binding.newGroupMemNotif.isChecked = false
                        binding.newMatchesNotif.isChecked = false

                        /**Initialize the notificationPrefs in case the user decides to update*/
                        notificationPrefs[VOTING] = false
                        notificationPrefs[NEW_MESSAGES] = false
                        notificationPrefs[NEW_GROUP_MEMBERS] = false
                        notificationPrefs[NEW_MATCHES]= false
                    }

                }.addOnFailureListener{
                    Toast.makeText(requireContext(), "Failed to get user Data", Toast.LENGTH_SHORT).show()
                }
        }
    }

}




