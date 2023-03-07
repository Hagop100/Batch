package com.example.batchtest.UserProfileTab

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.batchtest.InitialProfilePersonalizationFragment
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentAccountSettingBinding
import com.example.batchtest.databinding.FragmentUserProfileTabBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserProfileTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserProfileTabFragment : Fragment() {
    private var _binding: FragmentUserProfileTabBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserProfileTabBinding.inflate(layoutInflater, container, false)

        /**
         * display the user email as username
         */
        Firebase.auth.currentUser?.let { it ->
            db.collection("users").document(it.uid).get().addOnSuccessListener {
            binding.username.text = it.get("email").toString()
        } }


        /**
         * retrieve user imageUrl from database and display as the user profile image
         */
        val currentUser = Firebase.auth.currentUser?.uid
        //query the collection of users where the imageUrl is stored
        Firebase.auth.currentUser?.let { it ->
            db.collection("users").document(it.uid).get().addOnSuccessListener  {
                //if the specific user have empty imageUrl, set the default user image to be a placeholder

                //TODO: figure out why this is not populate it correctly when user has no profile pic!!
                if (it.get("imageUrl").toString().isEmpty()){
                    binding.userImage.setImageResource(R.drawable.placeholder)
                }
                //if the imageUrl is found in the user. set the image to be the imageUrl
                else{
                    Glide.with(this).load(it.get("imageUri")).into(binding.userImage)
                }


            }

        }


        /**
         * edit profile
         */
        binding.editProfileBtn.setOnClickListener{

            //hides the bottom nav when navigate to the account setting page
            val navBar: BottomNavigationView? = activity?.findViewById(R.id.nav_bar)
            navBar?.visibility = View.GONE

            findNavController().navigate(R.id.action_userProfileTabFragment_to_editProfileFragment)
        }

        /**
         * account setting
         */
        binding.settingBtn.setOnClickListener{

            //hides the bottom nav when navigate to the account setting page
            val navBar: BottomNavigationView? = activity?.findViewById(R.id.nav_bar)
            navBar?.visibility = View.GONE

            findNavController().navigate(R.id.action_userProfileTabFragment_to_accountSettingFragment)
        }

        return binding.root
    }

    /**Function will get the extension of type of the profile image */
    private fun getFileExtension(activity: Activity?, uri: Uri?): String?
    {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity?.contentResolver?.getType(uri!!))
    }


    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}