package com.example.batchtest.myGroupsTab

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.batchtest.Group
import com.example.batchtest.OtherGroupsTab.PendingGroups.PendingGroupFragment.Companion.voting
import com.example.batchtest.PendingGroup
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentJoinGroupBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "JoinGroupFragment"
/**
 * A simple [Fragment] subclass.
 * Use the [JoinGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JoinGroupFragment : Fragment() {
    private var _binding: FragmentJoinGroupBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    // get the authenticated logged in user
    private val currentUser = Firebase.auth.currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentJoinGroupBinding.inflate(layoutInflater, container, false)

        // listen to whenever user types in the group code edittext view
        binding.editGroupCode.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.v(TAG, "before text changed")
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // do nothing
            }
            // when text is changed, reset button's stylings
            override fun afterTextChanged(p0: Editable?) {
                // reset button's alpha (opacity)
                if (binding.joinGroupBtn.alpha != 1F) {
                    binding.joinGroupBtn.alpha = 1F
                }
                // reset button's text to search
                if (binding.joinGroupBtn.text != "Search") {
                    binding.joinGroupBtn.text = getString(R.string.search)
                }
                // reset button clickability if disabled
                if (!binding.joinGroupBtn.isClickable || binding.joinGroupBtn.alpha != 1F) {
                    enableBtn()
                }
            }

        })
        // default button to "Search"
        binding.joinGroupBtn.text = getString(R.string.search)
        var group: Group? = null
        // set listener on button
        binding.joinGroupBtn.setOnClickListener {
            // disable button temporarily after clicking while database operations occur
            disableBtn()
            // get group code from edittext and trim off white spaces at front or end
            val groupCode = binding.editGroupCode.text.toString().trim()
            // check if group code is empty or not
            if (groupCode.isEmpty()) {
                enableBtn()
                binding.editGroupCode.error = "Missing invite code"
            } else {
                // if button is in search state, search for group with the inputted id
                if (binding.joinGroupBtn.text == "Search") {
                    db.collection("groups")
                        .whereEqualTo("groupId", groupCode)
                        .get()
                        .addOnSuccessListener {
                            // renable button when group is successful
                            enableBtn()
                            // convert found group to an object
                            for (doc in it) {
                                group = doc.toObject(Group::class.java)
                                // change button state to Join
                                binding.joinGroupBtn.text = getString(R.string.join)
                                // get member count of group to see if it is full
                                val memberCount = group?.users?.size
                                // if group is full, disable button and change text to group is full
                                if (memberCount != null && memberCount >= 4) {
                                    disableBtn()
                                    binding.joinGroupBtn.text = getString(R.string.group_full)
                                } else if (group?.users?.contains(currentUser?.uid) == true) {
                                    // if user is already a member of the group
                                    disableBtn()
                                    binding.joinGroupBtn.text = getString(R.string.already_joined)
                                } else {
                                    // change button state to Join
                                    binding.joinGroupBtn.text = getString(R.string.join)
                                }
                                // make group card visible
                                binding.groupCardView.isVisible = true
                                // add info of found group to card
                                binding.groupName.text = group?.name.toString()
                                if (group!!.image == null || group!!.image == "") {
                                    binding.groupImg.setImageResource(R.drawable.placeholder)
                                } else {
                                    Glide.with(requireContext()).load(group?.image).into(binding.groupImg)
                                }
                                binding.memberCount.text = getString(R.string.member_count,group?.users?.size)
                                break
                            }
                            // if a group is not found, display invite code is invalid
                            if (group == null) {
                                binding.editGroupCode.error = "Invite code invalid"
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.v(TAG, "group not found:", e)
                        }
                } else if (binding.joinGroupBtn.text == "Join") {
                    // button will be in join state
                    // add user to group members in database
                    if (group != null) {
                        db.collection("groups").document(group?.name.toString())
                            .update("users", FieldValue.arrayUnion(currentUser!!.uid))
                        // add group to users myGroups
                        db.collection("users").document(currentUser.uid)
                            .update("myGroups", FieldValue.arrayUnion(group?.name))
                        // add all matched groups of group to user's matchedGroups
                        for (g in group!!.matchedGroups) {
                            db.collection("users").document(currentUser.uid).update("matchedGroups", FieldValue.arrayUnion(g))
                        }
                        // add user to all pending groups of joined group
                        db.collection("pendingGroups")
                            .whereEqualTo("matchingGroup", group?.name.toString())
                            .get()
                            .addOnSuccessListener {
                                for (doc in it) {
                                    val pendingGroup = doc.toObject(PendingGroup::class.java)
                                    val users = pendingGroup.users
                                    if (users != null) {
                                        users[currentUser.uid] = hashMapOf(
                                            "acceptor" to "false",
                                            "index" to users.size.toString(),
                                            "uid" to currentUser.uid,
                                            "vote" to "pending"
                                        )
                                    }
                                    // add current user to pending group with a pending vote
                                    db.collection("pendingGroups")
                                        .document(doc.getString("pendingGroupId").toString())
                                        .update("users", users)
                                    // recalculate the vote
                                    // voting(db,pendingGroup)
                                }
                            }
                        findNavController().navigate(R.id.action_joinGroupFragment_to_myGroupFragment)
                    }
                }
            }
        }
        /**
         * user exits back to myGroupFragment
         */
        binding.exitJoinGroup.setOnClickListener{
            findNavController().navigate(R.id.action_joinGroupFragment_to_myGroupFragment)
        }
        return binding.root
    }

    fun enableBtn() {
        binding.joinGroupBtn.isClickable = true
        binding.joinGroupBtn.alpha = 1F
    }
    fun disableBtn() {
        binding.joinGroupBtn.isClickable = false
        binding.joinGroupBtn.alpha = .5F
    }
    /**
     * Free view from memory
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}