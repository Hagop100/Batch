package com.example.batchtest.EditGroupProfile

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.batchtest.*
import com.example.batchtest.databinding.FragmentViewGroupInfoBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.ArrayList

private const val TAG = "ViewGroupInfoFragment"

// view group info fragment will navigate to a selected group's display page
class ViewGroupInfoFragment : Fragment(), UserInfoAdapter.UserInfoListener {
    private var _binding: FragmentViewGroupInfoBinding? = null
    private val binding get() = _binding!!
    private val mainScope = CoroutineScope(Dispatchers.Main)
    var db = Firebase.firestore
    // get the authenticated logged in user
    private val currentUser = Firebase.auth.currentUser
    private val currentUserId = currentUser?.uid
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
        _binding = FragmentViewGroupInfoBinding.inflate(inflater, container, false)

        Log.i(TAG, "Entering viewGroupInfo")

        /**
         * Retrieve data from MyGroupFragment using arguments
         * display the user group information including:
         * group name, profile picture, tags, and about us
         */
        binding.groupName.text = sharedViewModel.getGName().value


        val groupName = binding.groupName.text

        //get info from the group collection in firebase
        db.collection("groups").document(groupName as String).get().addOnSuccessListener { document ->
            // set biscuit value
            if (_binding != null) binding.biscuitValue.text = document.get("biscuits").toString()

            //set info about group pic
            val groupPic = document.getString("image")
            if (groupPic.isNullOrEmpty() && _binding != null){
                binding.groupPicture.setImageResource(R.drawable.placeholder)
            }
            else{
                Glide.with(this).load(document.getString("image").toString()).into(binding.groupPicture)
            }

            //retrieve group description
            val aboutUs = document.getString("aboutUsDescription")
            if (_binding != null) binding.aboutUsDescription.text = aboutUs

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
            if (sharedViewModel.getReturnFragment().value == "MyGroupFragment") {
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
                // inflate a text view to hold the block group dialog
                val giveBiscuitBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
                giveBiscuitBtn.text = getString(R.string.give_biscuit)
                //check if user has given group biscuit
                if (currentUserId != null) {
                    db.collection("users").document(currentUserId).get().addOnSuccessListener {
                        if(it.get("biscuits") != null)
                        {
                            val isBiscuitGiven = it.get("biscuits.${groupName}") as Boolean
                            if (isBiscuitGiven) {
                                toggleBiscuitBtn(giveBiscuitBtn)
                            }
                        }

                    }
                }
                giveBiscuitBtn.setOnClickListener {
                    if (currentUserId != null) {
                        // update database that user has given group biscuit
                        db.collection("users").document(currentUserId).update("biscuits.$groupName", true)
                        // update biscuit value of group
                        db.collection("groups").document(groupName).update("biscuits", FieldValue.increment(1))
                        // update biscuit value
                        binding.biscuitValue.text = getString(R.string.increment_biscuit, (binding.biscuitValue.text.toString().toInt() + 1))
                        // disable biscuit button
                        toggleBiscuitBtn(giveBiscuitBtn)
                        dialog.onContentChanged()
                    }
                }
                // inflate a text view to hold the report group dialog
                val reportGroupDialogBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
                reportGroupDialogBtn.text = getString(R.string.report_group)

                //TODO, set up the report blocking
                reportGroupDialogBtn.setOnClickListener { view ->
                    dialog.dismiss()
                }

                // inflate a text view to hold the block group dialog
                val blockGroupBtn: TextView = LayoutInflater.from(view.context).inflate(R.layout.dialog_button, view, false) as TextView
                blockGroupBtn.text = getString(R.string.block_group)

                //user wishes to block group
                blockGroupBtn.setOnClickListener { blockView->
                    //dismiss more dialog
                    dialog.dismiss()
                    //create bottomsheet dialog for blocking options
                    val blockDialog = Dialog(requireContext(),R.style.pop_up_dialog_corners)

                    //Display warning to user, with groupName
                    val blockText = getString(R.string.block_group_warning, groupName)
                    //inflate the block dialog
                    val blockView = layoutInflater.inflate(R.layout.block_bottom_view,binding.root, false)

                    //set view text
                    blockView.findViewById<TextView>(R.id.tv_question_verify).text = blockText

                    //Show the blockdialog
                    val btnOk = blockView.findViewById<Button>(R.id.btn_ok)
                    val btnCancel = blockView.findViewById<Button>(R.id.btn_cancel)
                    blockDialog.setContentView(blockView)
                    blockDialog.show()

                    //Dialog will have a button that says ok.
                    //outcome will depend if the user is the leader or not
                    btnOk.setOnClickListener { it->
                        //Will update user's matched groups and blocked groups and remove pending groups from the database as well
                        if(currentUserId != null)
                        {
                            //get user collection
                            val userDocument = Firebase.firestore.collection("users")
                            //start coroutine
                            lifecycleScope.launch{
                                //get the user and group objects
                                val user = userDocument.document(currentUserId).get().await().toObject(User::class.java)
                                //get the user's primary group
                                val group = db.collection("groups").document(user?.primaryGroup!!).get().await().toObject(Group::class.java)

                                //check that the current user is the primary leader
                                // and the matchedGroups contains a match with the group the user
                                //wishes to block
                                if (currentUserId == group?.leader && group.matchedGroups.contains(groupName))
                                {

                                    blockDialog.dismiss()
                                    async {
                                        //Remove matched group from matched groups field in firestore
                                        blockGroupGroupUpdate(user.primaryGroup, groupName)
                                    }
                                    async {

                                        removedGroupUpdate(user.primaryGroup, groupName)
                                    }
                                    async {
                                            removePending(user.primaryGroup,groupName)
                                    }
                                    Log.i(TAG, "Async task have completed")
                                    blockDialog.dismiss()
                                    //will navigate back to other groups fragment
                                    mainScope.launch {
                                        //half second delay to give the fragment time to remove blocked group from matching page
                                        delay(500L)
                                        findNavController().navigate(R.id.action_viewGroupInfoFragment_to_otherGroupTabFragment)
                                    }
                                }
                                else//Inform user that only the group leader may update the
                                {
                                    blockDialog.dismiss()
                                    val notLeaderDialog = Dialog(requireContext(), R.style.pop_up_dialog_corners)
                                    val leaderLayout = layoutInflater.inflate(R.layout.not_leader_dialog,binding.root,false)
                                    val okButton = leaderLayout.findViewById<Button>(R.id.btn_okLeader)
                                    notLeaderDialog.setContentView(leaderLayout)
                                    notLeaderDialog.show()
                                    okButton.setOnClickListener{
                                        notLeaderDialog.dismiss()
                                    }
                                }
                            }//end of coroutine
                        }//end of removing group
                    }//end of button listener

                    //dismiss dialog if user presses to cancel
                    btnCancel.setOnClickListener { it->

                        blockDialog.dismiss()
                    }

                }
                // add the give biscuit dialog button to the bottom dialog view
                view.addView(giveBiscuitBtn)
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

    private fun toggleBiscuitBtn(giveBiscuitBtn: TextView) {
        giveBiscuitBtn.isClickable = false
        giveBiscuitBtn.alpha = .4F
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

    /**Function takes in the current user id and the groupName of the group they are blocking
     * User's Matched Group and Blocked groups fields are updated in the database*/
    private suspend fun blockGroupUserUpdate(currentUserId: String, groupName: String)
    {

        //get user
        val user = db.collection("users").document(currentUserId).get().await().toObject(User::class.java)
        //Initialize a blocked groups array
        var blocked = ArrayList<String>()
        //check that user was returned
        if(user != null)
        {
            //check if user already has a blocked groups list
            if(user.blockedGroups.isNotEmpty())
            {
                //get user's blockedGroupsList
                blocked = user.blockedGroups
                //add to the blocked list
                blocked.add(groupName)
            }
            else //Create a blocked groups list if not yet initialized
            {
                blocked.add(groupName)
            }
            //get user's matched groups list
            val matchedGroups = user.matchedGroups

            //remove group from matched groups
            matchedGroups.remove(groupName)
            //update user's matched and blocked groups
            //update user's matched and blocked groups
            var updateGroups = HashMap<String, Any>()
            updateGroups["matchedGroups"] = matchedGroups
            updateGroups["blockedGroups"] = blocked
            //update user in the database with new matchedGroups and BlockedGroups
            db.collection("users").document(currentUserId).update(updateGroups).await()
        }
    }

    /**Function will un-match the current group from the blocked groups matched section
     * will also iterate through all the group members and remove the group from their match and add
     * group to blocked group*/
    private suspend fun removedGroupUpdate(primaryGroup: String,groupName: String)
    {

        //get user
        val group = db.collection("groups").document(groupName).get().await().toObject(Group::class.java)
        //Initialize a blocked groups array
        var blocked = ArrayList<String>()
        //check that user was returned
        if(group != null)
        {

            //get a list of matched groups and blocked groups
            val matched = group.matchedGroups
            if (group.blockedGroups.isNotEmpty())
            {
                blocked = group.blockedGroups
                blocked.add(primaryGroup)
            }
            else{
                blocked.add(primaryGroup)
            }
            //update matched groups and blocked groups
            matched.remove(primaryGroup)
            var updateGroup = HashMap<String, Any>()
            updateGroup["matchedGroups"] = matched
            updateGroup["blockedGroups"] = blocked
            //get users from from the group
            val users = group.users!!

            lifecycleScope.launch(Dispatchers.IO){
                for (user in users)
                {
                    //iterate through all members of the group and remove group from matched
                    //and add them to blocked
                    blockGroupUserUpdate(user, groupName)
                }
            }
            //update group's matched and blocked groups
            db.collection("groups").document(groupName).update(updateGroup).await()
        }
    }
    /**
     * Function will remove all pendingGroups documents that are between the blocking group and the blocked group
     * */
    private suspend fun removePending(primaryGroup: String, blockedGroupName: String){

         db.collection("pendingGroups")
            .whereEqualTo("matchingGroup",primaryGroup)
            .whereEqualTo("pendingGroup", blockedGroupName)
            .addSnapshotListener { result, exception ->
                if (exception != null)
                {
                    Log.i(TAG,"Listen failed")
                    return@addSnapshotListener
                }
                if (result == null)
                {
                    Log.i(TAG, "No Pending found")
                    return@addSnapshotListener
                }
                else
                {
                    for(doc in result)
                    {
                        val pending = doc.toObject(PendingGroup::class.java)
                        val pendingRef = db.collection("pendingGroups").document(pending.pendingGroupId!!)

                        pendingRef.delete()
                            .addOnSuccessListener {
                                Log.i(TAG, "pending successfully deleted")
                            }.addOnFailureListener {
                                Log.i(TAG, "pending failed to delete")
                            }
                    }
                }
            }
        db.collection("pendingGroups")
            .whereEqualTo("matchingGroup",blockedGroupName)
            .whereEqualTo("pendingGroup", primaryGroup)
            .addSnapshotListener { result, exception ->
                if (exception != null)
                {
                    Log.i(TAG,"Listen failed")
                    return@addSnapshotListener
                }
                if (result == null)
                {
                    Log.i(TAG, "No Pending found")
                    return@addSnapshotListener
                }
                else
                {
                    for(doc in result)
                    {
                        val pending = doc.toObject(PendingGroup::class.java)
                        val pendingRef = db.collection("pendingGroups").document(pending.pendingGroupId!!)

                        pendingRef.delete()
                            .addOnSuccessListener {
                                Log.i(TAG, "pending successfully deleted")
                            }.addOnFailureListener {
                                Log.i(TAG, "pending failed to delete")
                            }
                    }
                }
            }


    }

    /**Function will remove blocked group, from the primary groups matchedGroups field
     * Function will also iterate through all the group's members and call blockGroupUserUpdate*/
    private suspend fun  blockGroupGroupUpdate(primaryGroup: String, blockedGroupName: String)
    {
        //get user
        val group = db.collection("groups").document(primaryGroup).get().await().toObject(Group::class.java)

        var blocked = ArrayList<String>()
        //check that user was returned
        if(group != null)
        {

            //get a list of matched groups and blocked groups
            val matched = group.matchedGroups
            if (group.blockedGroups.isNotEmpty())
            {
                blocked = group.blockedGroups
                blocked.add(blockedGroupName)
            }
            else{
                blocked.add(blockedGroupName)
            }
            //update matched groups and blocked groups
            matched.remove(blockedGroupName)
            var updateGroup = HashMap<String, Any>()
            updateGroup["matchedGroups"] = matched
            updateGroup["blockedGroups"] = blocked
            //get users from from the group
            val users = group.users!!

            lifecycleScope.launch(Dispatchers.IO){
                for (user in users)
                {
                    //iterate through all members of the group and remove group from matched
                    //and add them to blocked
                    blockGroupUserUpdate(user, blockedGroupName)
                }
            }
            //update group's matched and blocked groups
            db.collection("groups").document(primaryGroup).update(updateGroup).await()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }


}





