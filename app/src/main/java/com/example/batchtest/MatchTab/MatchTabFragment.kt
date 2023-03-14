package com.example.batchtest.MatchTab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.Fragment
import com.example.batchtest.Group
import com.example.batchtest.PendingGroup
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.example.batchtest.myGroupsTab.MyGroupFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.*
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


private const val TAG = "MatchTabFragmentLog"
/**
 * Match Tab Fragment
 * displays potential groups the user can match with
 * extends Fragment class
 * implements CardStackAdapterListener interface to listen to actions such as undo button clicks
 */
// groups to pass into adapter and display on match tab
private val groups = arrayListOf<Group>()
// rejected groups
private val removeGroups: ArrayList<Group> = ArrayList()
// store previous group
private var prevGroup: Group? = null
class MatchTabFragment : Fragment(), CardStackAdapter.CardStackAdapterListener {
    private var _binding: FragmentMatchTabBinding? = null
    private val binding get() = _binding!!
    // set layout manager to card stack view to arrange recycler view
    private lateinit var manager: CardStackLayoutManager
    private val db = Firebase.firestore
    // get the authenticated logged in user
    private val currentUser = Firebase.auth.currentUser
    // fetches a user from firestore using the uid from the authenticated user
    private val currentUserDocRef = db.collection("users").document(currentUser!!.uid)
    // primary group that user will be matching as
    private var primaryGroup: Group? = null
    // inflate and bind the match tab fragment after view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // bind fragment
        _binding = FragmentMatchTabBinding.inflate(inflater, container, false)
        manager = CardStackLayoutManager(context)
        // get card stack view
        val cardStackView = binding.cardStackView
        // prevent users from swiping cards
        manager.setCanScrollHorizontal(false)
        manager.setCanScrollVertical(false)
        // show one card on stack
        manager.setStackFrom(StackFrom.None)
        // manager will define the layout for the card stack view
        cardStackView.layoutManager = manager
        /*
        * fetch all possible groups to match based on logged in user
        * and send to adapter which will display the groups in a recycler view
         */
        // reset undo state if previous group is null
        if (prevGroup == null) {
            currentUserDocRef.update("undoState", false)
        }

        // get all potential groups of current user to match with
        currentUserDocRef
            .get()
            // if successful filter out certain groups for matching
            .addOnSuccessListener { result ->
                // convert the fetched user into a User object
                val user: User = result?.toObject(User::class.java)!!
                if (user.primaryGroup != null) {
                    primaryGroup = user.primaryGroup
                    //binding.matchTabMessage.text = ""
                } else {
                    binding.matchTabMessage.text = "Set a primary group to start matching!"
                    return@addOnSuccessListener
                }
                // if the groups has not been populated, fetch the groups from firebase
                // else reuse the fetched groups
                if (groups.isEmpty()) {
                    // filter groups will store all group to remove from the match group pool
                    val filterGroups: ArrayList<String> = ArrayList()
                    // all groups the user is will be filtered out so the user cannot match with their own groups
                    filterGroups.addAll(user.myGroups)
                    // all groups the user has already matched in will be filtered out
                    filterGroups.addAll(user.matchedGroups)
                    // all groups that are awaiting the voting process will be filtered out
                    filterGroups.addAll(user.pendingGroups)
                    // fetch all groups from the database filtering out the groups with
                    val groupsDocRef = db.collection("groups")
                    // names matching the unwanted group's name
                    if (user.myGroups.isNotEmpty() && filterGroups.isNotEmpty()) {
                        groupsDocRef.whereNotIn("name", filterGroups)
                            .get()
                            .addOnSuccessListener {
                                // convert the resulting groups into group object
                                for (doc in it) {
                                    val group: Group = doc.toObject(Group::class.java)
                                    // add the group to the groups list
                                    groups.add(group)
                                }
                                // attach adapter and send groups and listener
                                cardStackView.adapter =
                                    CardStackAdapter(currentUser!!.uid, groups, this)
                            }
                            .addOnFailureListener { e ->
                                Log.v(TAG, "error getting documents: ", e)
                            }
                    } else {
                        binding.matchTabMessage.text = "Create or join a group to start matching!"
                    }
                } else {
                        /*
                        * when the match tab fragment gets rebuilt, we use the groups that was fetched
                        * to avoid errors in the adapter with the original groups passed, we wait until
                        * the fragment is rebuilt to remove the "dirty groups" since we are passing a
                        * new group array to the adapter
                         */
                        // if there are groups in the removeGroups, take out the previous group
                        // then remove all groups from removeGroups from the potential groups and clear it
                        if (removeGroups.isNotEmpty()) {
                            // remove the previous group because we want to keep for undo
                            removeGroups.remove(prevGroup)
                            // remove all the removeGroups from groups
                            groups.removeAll(removeGroups.toSet())
                            // clear the array
                            removeGroups.clear()
                        }
                        // recycler view will display the groups
                        cardStackView.adapter = CardStackAdapter(currentUser!!.uid, groups, this)
                        if (prevGroup != null) {
                            // groups[0], the previous group, will be displayed so we will skip to groups[1]
                            manager.scrollToPosition(1)
                        }
                    }
                }
        return binding.root
    }


    // rewind to previous card when undo button is clicked
    override fun onUndoBtnClick(position: Int) {
        // rewind function
        binding.cardStackView.rewind()
        if (removeGroups.isNotEmpty()) {
            removeGroups.remove(prevGroup)
        }

        // reset previous group to null
        prevGroup = null
        // update undo state of current user
        currentUserDocRef.update("undoState", false)
    }

    override fun onAcceptBtnClick(acceptedGroup:Group) {
        // set animation card moving right for reject
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting)
        // add the group to the list of pending groups for the user
        db.collection("users").document(currentUser?.uid.toString()).update("pendingGroups", FieldValue.arrayUnion(acceptedGroup.name))
        //
        var users: ArrayList<HashMap<String, Any>> = ArrayList()

        var index = 1
        for (user in primaryGroup?.users!!) {
            var userMap = hashMapOf<String, Any>()
            userMap["user"] = user
            if (user == currentUser?.uid) {
                userMap["acceptor"] = true
                userMap["vote"] = "accept"
                userMap["index"] = 0
            } else {
                userMap["acceptor"] = false
                userMap["vote"] = "pending"
                userMap["index"] = index
                index++
            }
            users.add(userMap)
        }
        var pendingGroup = PendingGroup(
            pendingGroupId = UUID.randomUUID().toString(),
            matchingGroup = primaryGroup,
            pendingGroup = acceptedGroup,
            users = users,
            isPending= true,
            isMatched = false
        )
        // add pending group to firestore pendingGroups collection
        db.collection("pendingGroups").document(pendingGroup.pendingGroupId.toString()).set(pendingGroup)

        removeGroups.add(acceptedGroup)
        prevGroup = null
        // update undo state of current user
        currentUserDocRef.update("undoState", false)
        // swipe the card
        binding.cardStackView.swipe()
    }

    override fun onRejectBtnClick(group: Group) {
        // set animation card moving left for reject
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Left)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting)
        removeGroups.add(group)
        if (prevGroup != null) {
            removeGroups.add(prevGroup!!)
        }
        prevGroup = group
        // update undo state of current user
        currentUserDocRef.update("undoState", true)
        // swipe the card
        binding.cardStackView.swipe()
    }

//    private fun setPrimaryGroup(groupName: String) {
//        // primary group that user will be matching as
//        db.collection("groups").document(groupName)
//            .get()
//            .addOnSuccessListener { result ->
//                this.primaryGroup = result.toObject(Group::class.java)
//            }
//
//        currentUserDocRef.get()
//            .addOnSuccessListener { result ->
//                val myGroups = result.data?.get("myGroups") as ArrayList<*>
//                if (!myGroups.contains("groupName")) {
//                    currentUserDocRef.update("myGroups", FieldValue.arrayUnion(groupName))
//                }
//            }
//        db.collection("groups").document(groupName).update("users", FieldValue.arrayUnion(currentUser?.uid))
//        currentUserDocRef.update("primaryGroup", groupName)
//    }

    // free from memory
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}