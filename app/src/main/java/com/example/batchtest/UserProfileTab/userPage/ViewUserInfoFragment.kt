package com.example.batchtest.UserProfileTab.userPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.batchtest.EditGroupProfile.ViewGroupInfoFragmentArgs
import com.example.batchtest.Group
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentViewUserInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [ViewUserInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewUserInfoFragment : Fragment() {
    private var _binding: FragmentViewUserInfoBinding? = null
    private val binding get() = _binding!!
    private val args: ViewUserInfoFragmentArgs by navArgs<ViewUserInfoFragmentArgs>()
    var db = Firebase.firestore
    // get the authenticated logged in user
    private val currentUser = Firebase.auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewUserInfoBinding.inflate(layoutInflater, container, false)
        val mutualGroupsRV = binding.mutualGroupsRv
        mutualGroupsRV.layoutManager = LinearLayoutManager(context)
        val mutualMatchedGroupsRV = binding.mutualMatchedGroupsRv
        mutualMatchedGroupsRV.layoutManager = LinearLayoutManager(context)
        // return to previous fragment
        binding.exitViewBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // open dialog when clicking on more buton
        binding.userProfileMoreBtn.setOnClickListener {
            // create a bottom sheet dialog
            val dialog = BottomSheetDialog(requireContext())
            // inflate the view with the dialog linear layout
            val view: LinearLayout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, binding.root, false) as LinearLayout
            // create block dialog button to add into the dialog layout dynamically
            // using the dialog button layout

            // inflate a text view to hold the block profile dialog
            val blockUserDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
            blockUserDialogBtn.text = "Block user"
            // perform action on click
            blockUserDialogBtn.setOnClickListener {
                // add code to block user here //
                dialog.dismiss()
            }

            // inflate a text view to hold the report profile dialog
            val reportUserDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
            reportUserDialogBtn.text = "Report user"
            // perform action on click
            reportUserDialogBtn.setOnClickListener {
                // add code to report user here //
                dialog.dismiss()
            }

            // add the block user dialog button to the bottom dialog view
            view.addView(blockUserDialogBtn)
            // add the report user dialog button to the bottom dialog view
            view.addView(reportUserDialogBtn)

            // set the view of the dialog using the inflated layout
            dialog.setContentView(view)
            // show the dialog
            dialog.show()
        }


        // fetch displayed user info
        db.collection("users").whereEqualTo("email", args.userEmail)
            .get()
            .addOnSuccessListener { result ->
                val user = result.documents[0].toObject(User::class.java)
                if (user != null) {
                    binding.userName.text = user.getName()
                    if (user.imageUrl.isNullOrEmpty()){
                        binding.userPicture.setImageResource(R.drawable.placeholder)
                    } else {
                        Glide.with(this).load(user.imageUrl).into(binding.userPicture)
                    }
                    val mutualGroups:ArrayList<String> = arrayListOf()
                    val mutualMatchedGroups:ArrayList<String> = arrayListOf()
                    db.collection("users").document(currentUser!!.uid).get()
                        .addOnSuccessListener {
                            val currUserObj = it.toObject(User::class.java)
                            if (currUserObj != null) {
                                for (group in user.myGroups) {
                                    if (currUserObj.myGroups.contains(group)) {
                                        mutualGroups.add(group)
                                    }
                                }
                                mutualGroupsRV.adapter = ViewUserInfoAdapter(context, mutualGroups)
                                for (group in user.matchedGroups) {
                                    if (currUserObj.matchedGroups.contains(group)) {
                                        mutualMatchedGroups.add(group)
                                    }
                                }
                                mutualMatchedGroupsRV.adapter = ViewUserInfoAdapter(context, mutualMatchedGroups)
                            }
                        }
                } else {
                    TODO("user not found")
                }
            }

        return binding.root
    }

    // free from memory
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}