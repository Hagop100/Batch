package com.example.batchtest.EditGroupProfile

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentViewGroupInfoBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

private const val TAG = "ViewGroupInfoFragment"

// view group info fragment will navigate to a selected group's display page
class ViewGroupInfoFragment : Fragment(), UserInfoAdapter.UserInfoListener {
    private var _binding: FragmentViewGroupInfoBinding? = null
    private val binding get() = _binding!!
    var db = Firebase.firestore
    // get the authenticated logged in user
    private val currentUser = Firebase.auth.currentUser
    private lateinit var userRecyclerView: RecyclerView
    private val sharedViewModel: GroupInfoViewModel by activityViewModels()
    private lateinit var userList: ArrayList<User>
    private lateinit var userAdapter: UserInfoAdapter

    private lateinit var groupId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentViewGroupInfoBinding.inflate(layoutInflater, container, false)

        /**
         * Retrieve data from MyGroupFragment using arguments
         * display the user group information including:
         * group name, profile picture, tags, and about us
         */
        binding.groupName.text = sharedViewModel.getGName().value
        // check if user is in group to grant certain actions in more dialog box
        val isInGroup = sharedViewModel.getIsInGroup().value

        val groupName = binding.groupName.text

        //get info from the group collection in firebase
        db.collection("groups").document(groupName as String).get().addOnSuccessListener { document ->
            // set biscuit value
            binding.biscuitValue.text = document.get("biscuits").toString()

            //set info about group pic
            val groupPic = document.getString("image")
            if (groupPic.isNullOrEmpty()){
                binding.groupPicture.setImageResource(R.drawable.placeholder)
            }
            else{
                Glide.with(this).load(document.getString("image").toString()).into(binding.groupPicture)
            }

            //retrieve group description
            val aboutUs = document.getString("aboutUsDescription")
            binding.aboutUsDescription.text = aboutUs

            //used for passing value to preference fragment
            groupId = document.getString("groupId").toString()
            /**
             * retrieve info of interest tags
             */
            //retrieve arraylist of interest tags from the current group in firebase
            val interestTags: ArrayList<*> = document.get("interestTags") as ArrayList<*>
            //get the interest tags layout
            val flexboxLayout: FlexboxLayout = binding.interestTags
            for (tag in interestTags){
                //inflate the interest_tags.xml layout
                val textView = inflater.inflate(R.layout.interest_tag, flexboxLayout, false) as TextView
                textView.text = tag as String? //set the text of the TextView to current tag
                flexboxLayout.addView(textView) //add the TextView to Flexbox

            }

            /**
             * retrieve list of users in the group to show all the team members
             */
            //retrieve arraylist of users from the current group in firebase
            val users: ArrayList<*> = document.get("users") as ArrayList<*>

            //set recyclerview
            userRecyclerView = binding.userRecyclerView
            userRecyclerView.layoutManager = LinearLayoutManager(this.context)
            userRecyclerView.setHasFixedSize(true)
            // Initialize arraylist of all users
            userList = arrayListOf()

            // Loop through the user IDs in the list
            for (userId in users){
                //access the user document based on the list
                db.collection("users").document(userId as String).get().addOnSuccessListener { document ->
                    //add the user into the userList
                    val userInfo: User? = document.toObject(User::class.java)
                    userList.add(userInfo!!)
                    userAdapter = UserInfoAdapter(requireActivity(),userList, this)
                    userRecyclerView.adapter = userAdapter
                }
                    .addOnFailureListener { e->
                        Log.i("print", "error getting user from documents: ", e)
                    }

            }

        }//end of firebase collection retrieve

        // more button on match page opens dialog
        binding.groupProfileMoreBtn.setOnClickListener {
            // create a bottom sheet dialog
            val dialog = BottomSheetDialog(requireContext())
            // inflate the view with the dialog linear layout
            val view: LinearLayout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, binding.root, false) as LinearLayout

            // create block dialog button to add into the dialog layout dynamically
            // using the dialog button layout

            // if user is in the group displayed, then the user will be able
            // to edit the group profile or invite a user
            // else if the user is not in the group, then the user will be able
            // to report or block the group
            if (isInGroup == true) {
                // inflate a text view to hold the edit profile dialog
                val editProfileDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
                editProfileDialogBtn.text = getString(R.string.edit_group_profile)
                // perform action on click

                editProfileDialogBtn.setOnClickListener {
                    // navigate to the edit page
                    findNavController().navigate(R.id.action_viewGroupInfoFragment_to_editGroupProfile)
                    //send data using shared view model
                    sharedViewModel.groupName.value
                    dialog.dismiss()
                }

                //TODO where do I initialize the groupID
                val discoveryPreferenceBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button,view,false) as TextView
                discoveryPreferenceBtn.text = "Discovery Preferences"
                discoveryPreferenceBtn.setOnClickListener {
                    findNavController().navigate(ViewGroupInfoFragmentDirections.actionViewGroupInfoFragmentToPreferencesFragment(
                        groupName
                    ))
                    dialog.dismiss()
                }

                // inflate a text view to hold the edit profile dialog
                val groupInviteBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
                groupInviteBtn.text = getString(R.string.invite_user)

                // perform action on click
                groupInviteBtn.setOnClickListener {
                    // fetch group ID from firebase using groupName
                    db.collection("groups").document(groupName)
                        .get()
                        .addOnSuccessListener { result ->
                            // start an intent to send group id using different actions
                            // such as copying to clipboard, email, messaging
                            activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, result.data?.get("groupId").toString())
                                type = "text/plain"
                            }
                            // user chooses which method to share
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            // starts intent
                            activity?.startActivity(shareIntent)
                        }
                }
                // add the edit profile dialog button to the bottom dialog view
                view.addView(editProfileDialogBtn)

                //add the discovery profile dialog button to the bottom dialog view
                view.addView(discoveryPreferenceBtn)
                // add the group invite dialog button to the bottom dialog view
                view.addView(groupInviteBtn)
            } else {
                // inflate a text view to hold the report group dialog
                val reportGroupDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
                reportGroupDialogBtn.text = getString(R.string.report_group)

                // inflate a text view to hold the block group dialog
                val blockGroupBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
                blockGroupBtn.text = getString(R.string.block_group)

                // add the report group dialog button to the bottom dialog view
                view.addView(reportGroupDialogBtn)
                // add the block group dialog button to the bottom dialog view
                view.addView(blockGroupBtn)
            }
            // set the view of the dialog using the inflated layout
            dialog.setContentView(view)
            // show the dialog
            dialog.show()
        }

        //Exit to navigate back to the my groups page
        binding.exitViewBtn.setOnClickListener{
            //val result = arguments?.getString("fragmentNavigatedFrom")
            if(sharedViewModel.getReturnFragment().value == "MyGroupFragment") {
                findNavController().navigate(R.id.action_viewGroupInfoFragment_to_myGroupFragment)
            } else if (sharedViewModel.getReturnFragment().value == "MatchedGroupFragment"){
                findNavController().navigate(R.id.action_viewGroupInfoFragment_to_otherGroupTabFragment)
            }
        }
        return binding.root
    }

    // free from memory
    override fun onDestroyView() {
        super.onDestroyView()
        sharedViewModel.groupPic.removeObservers(viewLifecycleOwner)
        _binding = null
    }

    // navigate to user display page when clicked
    override fun onItemClick(userEmail: String) {
        // if current user clicks on own profile picture, then navigate to their profile tab
        // else navigate to the clicked user's display page
        if (userEmail == currentUser?.email) {
            val action = ViewGroupInfoFragmentDirections.actionViewGroupInfoFragmentToUserProfileTabFragment()
            findNavController().navigate(action)
        } else {
            val action =
                ViewGroupInfoFragmentDirections.actionViewGroupInfoFragmentToViewUserInfoFragment(
                    userEmail
                )
            findNavController().navigate(action)
        }
    }
}



