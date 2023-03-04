package com.example.batchtest.MatchTab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.batchtest.Group
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.*


private const val TAG = "GroupsFetchLog"
/**
 * Match Tab Fragment
 * displays potential groups the user can match with
 * extends Fragment class
 * implements CardStackAdapterListener interface to listen to actions such as undo button clicks
 */
class MatchTabFragment : Fragment(), CardStackAdapter.CardStackAdapterListener {
    private lateinit var binding: FragmentMatchTabBinding
    // set layout manager to card stack view to arrange recycler view
    private lateinit var manager: CardStackLayoutManager

    private val db = Firebase.firestore
    // get the authenticated logged in user
    private val currentUser = Firebase.auth.currentUser
    // inflate and bind the match tab fragment after view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // bind fragment
        binding = FragmentMatchTabBinding.inflate(inflater, container, false)
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
        val groups = arrayListOf<Group>()
        // fetches a user from firestore using the uid from the authenticated user
        val currentUserDocRef = db.collection("users").document(currentUser!!.uid)
        currentUserDocRef
            // reads the document reference
            .get()
            // if successful filter out certain groups for matching
            .addOnSuccessListener { result ->
                // convert the fetched user into a User object
                val user: User = result.toObject(User::class.java)!!
                // filter groups will store all group to remove from the match group pool
                val filterGroups: ArrayList<String> = ArrayList()
                // all groups the user is will be filtered out so the user cannot match with their own groups
                filterGroups.addAll(user.myGroups!!)
                // all groups the user has already matched in will be filtered out
                filterGroups.addAll(user.matchedGroups!!)
                // all groups that are awaiting the voting process will be filtered out
                filterGroups.addAll(user.pendingGroups!!)
                // fetch all groups from the database filtering out the groups with
                val groupsDocRef = db.collection("groups")
                // names matching the unwanted group's name
                if (filterGroups.isNotEmpty()) {
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
                            cardStackView.adapter = CardStackAdapter(groups, this)
                        }
                        .addOnFailureListener { e ->
                            Log.v(TAG, "error getting documents: ", e)
                        }
                } else {
                    // should display that user needs to be in a group
                    groupsDocRef
                        .get()
                        .addOnSuccessListener {
                            // convert the resulting groups into group object
                            for (doc in it) {
                                val group: Group = doc.toObject(Group::class.java)
                                // add the group to the groups list
                                groups.add(group)
                            }
                            // attach adapter and send groups and listener
                            cardStackView.adapter = CardStackAdapter(groups, this)
                        }
                        .addOnFailureListener { e ->
                            Log.v(TAG, "error getting documents: ", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.v(TAG, "error getting user from documents: ", e)
            }

        // test groups without fetching for speed
//        val g1 = Group("One Direction", arrayListOf(User("Harry", "Styles", "harrystyles@gmail.com")), arrayListOf("singing", "dancing", "partying", "soccer"), "one direction test description")
//        val g2 = Group("The Beatles", arrayListOf(User("John", "Lennon", "johnlennon@gmail.com")), arrayListOf("cooking", "karaoke", "gaming", "movies"), "the beatles test description")
//        val g3 = Group("Fleetwood Mac", arrayListOf(User("Stevie", "Nicks", "stevienicks@gmail.com")), arrayListOf("studying", "swimming", "sleeping", "music"), "the beatles test description")
//        groups.add(g1)
//        groups.add(g2)
//        groups.add(g3)
//        cardStackView.adapter = CardStackAdapter(groups, this)
        return binding.root
    }

    // rewind to previous card when undo button is clicked
    override fun onUndoBtnClick() {
        binding.cardStackView.rewind()
    }

    override fun onAcceptBtnClick(groupName:String) {
        // set animation card moving right for reject
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting)
        // add the group to the list of pending groups for the user
        db.collection("users").document(currentUser!!.uid).update("pendingGroups", FieldValue.arrayUnion(groupName))
        // swipe the card
        binding.cardStackView.swipe()
    }

    override fun onRejectBtnClick(groupName:String) {
        // set animation card moving left for reject
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Left)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting)
        // swipe the card
        binding.cardStackView.swipe()
    }
}