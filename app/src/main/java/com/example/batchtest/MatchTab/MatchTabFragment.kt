package com.example.batchtest.MatchTab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.Fragment
import com.example.batchtest.Group
import com.example.batchtest.OtherGroupsTab.PendingGroups.PendingGroupAdapter
import com.example.batchtest.PendingGroup
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.example.batchtest.myGroupsTab.MyGroupFragment
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.*
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
private var groups = arrayListOf<Group>()
// rejected groups
private val removeGroups = arrayListOf<Group>()
// store previous group
private var prevGroup: Group? = null
// store current card
private var currGroup: Group? = null
class MatchTabFragment : Fragment(), CardStackAdapter.CardStackAdapterListener, CardStackListener {
    private var _binding: FragmentMatchTabBinding? = null
    private val binding get() = _binding!!
    // set layout manager to card stack view to arrange recycler view
    private lateinit var manager: CardStackLayoutManager
    private lateinit var db: FirebaseFirestore
    // get the authenticated logged in user
    private lateinit var currentUser: FirebaseUser
    // fetches a user from firestore using the uid from the authenticated user
    private lateinit var currentUserDocRef: DocumentReference
    // primary group that user will be matching as
    private var primaryGroup: String? = null
    // inflate and bind the match tab fragment after view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = Firebase.firestore
        currentUser = Firebase.auth.currentUser!!
        currentUserDocRef = db.collection("users").document(currentUser.uid)
        // bind fragment
        _binding = FragmentMatchTabBinding.inflate(inflater, container, false)
        manager = CardStackLayoutManager(context, this)
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
                // if user does not have a primary group, display message and return
                if ((user.primaryGroup == null) || (user.primaryGroup == "")) {
                    binding.matchTabMessage.text = getString(R.string.set_primary_group_message)
                    return@addOnSuccessListener
                } else {
                    if (primaryGroup == user.primaryGroup) {
                        Log.v(TAG, "true")
                    } else {
                        Log.v(TAG, "false")
                    }
                    Log.v(TAG, primaryGroup.toString())
                    Log.v(TAG, user.primaryGroup.toString())
                    primaryGroup = user.primaryGroup
                }
                // if user is not a group, then display message and return
                if (user.myGroups.isEmpty()) {
                    binding.matchTabMessage.text = getString(R.string.join_group_message)
                    return@addOnSuccessListener
                }
                 //if the groups has not been populated (is empty), fetch the groups from firebase
                 //else reuse the fetched groups
                if (groups.isEmpty()) {
                    // fetch all groups where user is not in
                    val query1 = db.collection("groups")
                        .whereArrayContains("users", currentUser.uid)
                        .get()
                    // fetch all pending groups of current user
                    val query2 = db.collection("pendingGroups")
                        .whereEqualTo("users.${currentUser.uid}.uid", currentUser.uid) // get all pending groups where user exists in users
                        .get()
                    // once all queries are successful, add the groups to adapter to display
                    Tasks.whenAllSuccess<QuerySnapshot>(query1, query2)
                        .addOnSuccessListener { results ->
                            var primaryGroupObj: Group? = null
                            // store the names of the pending group
                            val filterGroups = arrayListOf<String?>()
                            // loop thru groups containing current user and add to filter groups
                            for (query1Docs in results[0]) {
                                // convert the query document snapshot into a pending group object to access variables
                                val group: String? = query1Docs.getString("name")
                                if (group == user.primaryGroup) {
                                    primaryGroupObj = query1Docs.toObject(Group::class.java)
                                }
                                // add the name of the pending group
                                if (!filterGroups.contains(group)) {
                                    filterGroups.add(group)
                                }
                            }
                            // loop thru the pending groups query
                            for (query2Docs in results[1]) {
                                // convert the query document snapshot into a pending group object to access variables
                                val pendingGroup: String? = query2Docs.getString("pendingGroup")
                                // add the name of the pending group
                                if (!filterGroups.contains(pendingGroup)) {
                                    filterGroups.add(pendingGroup)
                                }

                            }

                            db.collection("groups").get()
                                .addOnSuccessListener {
                                    var interestTags = arrayListOf<String>()
                                    if (primaryGroupObj != null) {
                                        interestTags = primaryGroupObj.interestTags!!.map { it -> it.lowercase() } as ArrayList<String>
                                    }
                                    val interestGroups = arrayListOf<Group>()
                                    val noInterestGroups = arrayListOf<Group>()
                                    for (doc in it) {
                                        // convert the query document snapshot into a group object to access variables
                                        val obj = doc.toObject(Group::class.java)
                                        // if the group is apart of the user's pending groups, add to the list of groups to display in the match tab
                                        if (!filterGroups.contains(obj.name)) {
                                            // check if groups have a common interest
                                            for (tag in obj.interestTags!!) {
                                                // if a common interest is found, add to interestGroups and break
                                                if (interestTags.contains(tag.lowercase().trim()) && !interestGroups.contains(obj)) {
                                                    interestGroups.add(obj)
                                                    break
                                                }
                                            }
                                            // if not an interest group, add to noInterestGroups
                                            if (!interestGroups.contains(obj) && !noInterestGroups.contains(obj)) {
                                                noInterestGroups.add(obj)
                                            }
                                        }
                                    }
                                    // add interest groups to groups first to be in the front
                                    // then add non interests to after
//                                    Log.v(TAG, "interest groups$interestGroups")
//                                    Log.v(TAG, "no interest groups$noInterestGroups")
                                    groups.addAll(interestGroups)
                                    groups.addAll(noInterestGroups)
                                    // if groups is empty, display that the user needs to join a group
                                    if (groups.isEmpty()) {
                                        binding.matchTabMessage.text = getString(R.string.join_group_message)
                                    } else {
                                        // attach adapter and send groups and listener
                                        cardStackView.adapter =
                                            CardStackAdapter(currentUser.uid, context, groups, this)
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.v(TAG, "error getting pending groups and groups documents: ", e)
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
                        cardStackView.adapter = CardStackAdapter(currentUser.uid, requireContext(), groups, this)
                        if (prevGroup != null) {
                            // groups[0], the previous group, will be displayed so we will skip to groups[1]
                            manager.scrollToPosition(1)
                        }
                    }
                }
        //setPrimaryGroup("PendingGroups")
        return binding.root
    }

    fun orderGroupByInterests(unorderedGroups: ArrayList<Group>) {

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
    // accept group when accept button is clicked
    override fun onAcceptBtnClick(acceptedGrou: String) {
        // set animation card moving right for reject
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting)
//        // add the group to the list of pending groups for the user
//        db.collection("users").document(currentUser?.uid.toString())
//            .update("pendingGroups", FieldValue.arrayUnion(currGroup!!.name))
        // check if another user in the group has already initiated matching
        db.collection("pendingGroups")
            .whereEqualTo("matchingGroup", primaryGroup)
            .whereEqualTo("pendingGroup", currGroup)
            .get()
            .addOnSuccessListener { result ->
                var pendingGroupExists = false
                for (doc in result) {
                    // pending group already exists
                    pendingGroupExists = true
                    val pendingGroup = doc.toObject(PendingGroup::class.java)
                    // update the existing pending groups vote for current user to accept
                    db.collection("pendingGroups").document(pendingGroup.pendingGroupId.toString())
                        .update("users.$id.vote", "accept")
                }
                // if the pending group does not exist in the database then create a new pending group
                if (!pendingGroupExists) {
                    if (primaryGroup != null) {
                        db.collection("groups").document(primaryGroup!!).get()
                            .addOnSuccessListener {
                                val users: HashMap<String, HashMap<String, String>> = hashMapOf()
                                var index = 1
                                val primaryGroupObj = it.toObject(Group::class.java)
                                if (primaryGroupObj != null) {
                                    for (user in primaryGroupObj.users!!) {
                                        val userMap = hashMapOf<String, String>()
                                        userMap["uid"] = user
                                        if (user == currentUser.uid) {
                                            userMap["acceptor"] = "true"
                                            userMap["vote"] = "accept"
                                            userMap["index"] = "0"
                                        } else {
                                            userMap["acceptor"] = "false"
                                            userMap["vote"] = "pending"
                                            userMap["index"] = index.toString()
                                            index++
                                        }
                                        users[user] = userMap
                                    }
                                }
                                val pendingGroup = PendingGroup(
                                    pendingGroupId = UUID.randomUUID().toString(),
                                    matchingGroup = primaryGroup,
                                    pendingGroup = currGroup!!.name,
                                    users = users,
                                    pending = true,
                                    matched = false
                                )
                                // add pending group to firestore pendingGroups collection
                                db.collection("pendingGroups")
                                    .document(pendingGroup.pendingGroupId.toString())
                                    .set(pendingGroup)
                                    .addOnSuccessListener {
                                        PendingGroupAdapter.voting(db, pendingGroup)
                                        removeGroups.add(currGroup!!)
                                        prevGroup = null
                                        // update undo state of current user
                                        currentUserDocRef.update("undoState", false)
                                        //swipe the card
                                        binding.cardStackView.swipe()
                                    }
                            }
                    }
                } else {
                    removeGroups.add(currGroup!!)
                    prevGroup = null
                    // update undo state of current user
                    currentUserDocRef.update("undoState", false)
                    //swipe the card
                    binding.cardStackView.swipe()
                }
            }
    }
    // reject group when reject button is clicked
    override fun onRejectBtnClick(group: String) {
        // set animation card moving left for reject
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Left)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting)
        removeGroups.add(currGroup!!)
        if (prevGroup != null) {
            removeGroups.add(prevGroup!!)
        }
        prevGroup = currGroup
        // update undo state of current user
        currentUserDocRef.update("undoState", true)
        // swipe the card
        binding.cardStackView.swipe()
    }
    // set a primary group for testing
    private fun setPrimaryGroup(groupName: String) {
        // set the primary group variable
        this.primaryGroup = groupName
        // update primary group of user
        currentUserDocRef.update("primaryGroup", groupName)


        // add to users myGroups
        currentUserDocRef.get()
            .addOnSuccessListener { result ->
                val myGroups = result.data?.get("myGroups") as ArrayList<*>
                if (!myGroups.contains("groupName")) {
                    currentUserDocRef.update("myGroups", FieldValue.arrayUnion(groupName))
                }
            }
        // add to user to groups.users
        db.collection("groups").document(groupName).update("users", FieldValue.arrayUnion(currentUser.uid))

    }
    // free from memory
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {
        // do nothing
    }

    override fun onCardSwiped(direction: Direction?) {
        // do nothing
    }

    override fun onCardRewound() {
        // do nothing
    }

    override fun onCardCanceled() {
        // do nothing
    }

    override fun onCardAppeared(view: View?, position: Int) {
        currGroup = groups[position]
    }

    override fun onCardDisappeared(view: View?, position: Int) {
        // do nothing
    }
}