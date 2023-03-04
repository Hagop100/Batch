package com.example.batchtest

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.batchtest.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URI


class EditProfileFragment : Fragment() {

    //Binding Values
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    //Variables
    private lateinit var displayName: String
    private lateinit var birthday: String
    private lateinit var personalBio: String
    private lateinit var gender: String
    private lateinit var imageUri: Uri
    private lateinit var userDetails: User
    private lateinit var firstName: String
    private lateinit var lastName: String
    private var imageURL: String? = null


    private val userHashMap = HashMap<String, Any>()

    companion object{
        const val PICK_IMAGE_REQUEST_CODE =44
        const val GENDER = "gender"
        const val MALE: String = "male"
        const val FEMALE: String = "female"
        const val NONBINARY: String = "nonbinary"
        const val DISPLAYNAME: String = "displayName"
        const val IMAGEURL: String = "imageUrl"
        const val BIRTHDATE: String = "birthdate"
        const val PERSONALBIO: String = "personalBio"
        const val USER_PROFILE_IMAGE: String = "User_Profile_Image"
        const val FIRSTNAME: String = "firstname"
        const val LASTNAME: String ="lastName"
        const val EMAIL: String = "email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseFirestore.getInstance().collection("users")
            .document(getCurrentUserID()).get().addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)!!
                val url = user.imageUrl
                Toast.makeText(context, "$url", Toast.LENGTH_LONG).show()
                val profileImage = binding.civEditProfileImage
                binding.etEmail.setText(user.email)
                binding.etEmail.isEnabled = false
                binding.etBirthday.setText(user.birthdate)
                binding.etBirthday.isEnabled = false
                binding.etFirstName.setText(user.firstName)
                binding.etLastName.setText(user.lastName)
                binding.etDisplayName.setText(user.displayName)
                binding.rgGender.check(when(user.gender){
                    "male" -> binding.maleRadioButton.id
                    "female" -> binding.femaleRadioButton.id
                    else -> binding.nonBinaryRadioButton.id
                })
                binding.etBio.setText(user.personalBio)

                //TODO how to update image
                Glide.with(this).load(url).
                placeholder(R.drawable.person_icon).into(profileImage)

            }.addOnFailureListener{ e ->
                Toast.makeText(context, "Unable to retrieve User Info $e", Toast.LENGTH_SHORT).show()

            }


    }

    private fun getCurrentUserID(): String{
        // Get the instance of the current user
        val currentUser = FirebaseAuth.getInstance().currentUser

        //check whether we received a user Instance
        var currentUserId = ""
        if(currentUser != null)
        {
            currentUserId = currentUser.uid
        }
        return currentUser!!.uid
    }




}