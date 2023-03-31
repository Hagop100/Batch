package com.example.batchtest.UserProfileTab.userPage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.batchtest.EditGroupProfile.GroupInfoViewModel
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentViewUserInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [ViewUserInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "ViewUserInfoFragment"
class ViewUserInfoFragment : Fragment(), ViewUserInfoAdapter.GroupProfileViewEvent {
    private var _binding: FragmentViewUserInfoBinding? = null
    private val binding get() = _binding!!
    private val args: ViewUserInfoFragmentArgs by navArgs()
    private val sharedViewModel: GroupInfoViewModel by activityViewModels()
    var db = Firebase.firestore
    // get the authenticated logged in user
    private val currentUser = Firebase.auth.currentUser
    private val mutualGroups = arrayListOf<String>()
    private val mutualMatchedGroups = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewUserInfoBinding.inflate(layoutInflater, container, false)
        val mutualGroupsRV = binding.mutualGroupsRv
        mutualGroupsRV.layoutManager = LinearLayoutManager(context)
        val mutualMatchedGroupsRV = binding.mutualMatchedGroupsRv
        mutualMatchedGroupsRV.layoutManager = LinearLayoutManager(context)
        // return to previous fragment
        binding.exitViewBtn.setOnClickListener {
            // get return fragment value from shared viewmodel to return to
            if(sharedViewModel.getReturnFragment().value == "MyGroupFragment") {
                findNavController().navigate(R.id.action_viewUserInfoFragment_to_myGroupFragment)
            } else if (sharedViewModel.getReturnFragment().value == "MatchedGroupFragment"){
                findNavController().navigate(R.id.action_viewUserInfoFragment_to_otherGroupTabFragment)
            }
        }

        // open dialog when clicking on more button
        binding.userProfileMoreBtn.setOnClickListener {
            // create a bottom sheet dialog
            val dialog = BottomSheetDialog(requireContext())
            // inflate the view with the dialog linear layout
            val view: LinearLayout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, binding.root, false) as LinearLayout
            // create block dialog button to add into the dialog layout dynamically
            // using the dialog button layout

            // inflate a text view to hold the block profile dialog
            val blockUserDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
            blockUserDialogBtn.text = getString(R.string.block_user)
            // perform action on click
            blockUserDialogBtn.setOnClickListener {
                // add code to block user here //
                dialog.dismiss()
            }

            // inflate a text view to hold the report profile dialog
            val reportUserDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
            reportUserDialogBtn.text = getString(R.string.report_user)
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


        // fetch mutual joined groups and mutual matched groups
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
                    db.collection("users").document(currentUser!!.uid).get()
                        .addOnSuccessListener {
                            val currUserObj = it.toObject(User::class.java)
                            if (currUserObj != null) {
                                for (group in user.myGroups) {
                                    if (!mutualGroups.contains(group) && currUserObj.myGroups.contains(group)) {
                                        mutualGroups.add(group)
                                    }
                                }
                                mutualGroupsRV.adapter = ViewUserInfoAdapter(context, mutualGroups, this)
                                for (group in user.myGroups) {
                                    if (!mutualMatchedGroups.contains(group) && currUserObj.matchedGroups.contains(group)) {
                                        mutualMatchedGroups.add(group)
                                    }
                                }
                                mutualMatchedGroupsRV.adapter = ViewUserInfoAdapter(context, mutualMatchedGroups, this)
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

    // clicking on a group will pass the group name to teh shared view model then
    // navigate to the view group info fragment to display the selected group
    override fun onItemClick(groupName: String) {
        // set group name in the shared view model
        sharedViewModel.setGName(groupName)
        // if the group is apart of the mutual groups, set isInGroup to true
        if (mutualGroups.contains(groupName)) {
            sharedViewModel.setIsInGroup(true)
        } else {
            sharedViewModel.setIsInGroup(false)
        }
        // navigate to view group info fragment
        val direction = ViewUserInfoFragmentDirections.actionViewUserInfoFragmentToViewGroupInfoFragment(
            groupName
        )
//            groupInfo.aboutUsDescription.toString())
        findNavController().navigate(direction)
    }
}