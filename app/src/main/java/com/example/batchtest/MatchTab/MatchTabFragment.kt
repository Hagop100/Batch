package com.example.batchtest.MatchTab

import android.location.Location.distanceBetween
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.batchtest.Group
import com.example.batchtest.OtherGroupsTab.PendingGroups.PendingGroupFragment.Companion.voting
import com.example.batchtest.PendingGroup
import com.example.batchtest.R
import com.example.batchtest.User
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.*
import java.time.LocalDate
import java.time.Period
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
@RequiresApi(Build.VERSION_CODES.O)
class MatchTabFragment : Fragment(), CardStackAdapter.CardStackAdapterListener, CardStackListener {
    private var _binding: FragmentMatchTabBinding? = null
    private val binding get() = _binding!!
    // view model to restore state
    private val matchTabViewModel: MatchTabViewModel by activityViewModels()
    private lateinit var cardStackView: CardStackView
    // set layout manager to card stack view to arrange recycler view
    private lateinit var manager: CardStackLayoutManager
    private lateinit var db: FirebaseFirestore
    // get the authenticated logged in user
    private lateinit var currentUser: FirebaseUser
    // fetches a user from firestore using the uid from the authenticated user
    private lateinit var currentUserDocRef: DocumentReference
    // groups to pass into adapter
    private lateinit var groups: ArrayList<Group>
    // rejected groups
    private lateinit var removeGroups: ArrayList<Group>
    // store previous group
    private var prevGroup: Group? = null
    // store current card
    private var currGroup: Group? = null
    // primary group that user will be matching as
    private var primaryGroup: String? = null
    // matched groups of a user
    private var matchedGroups: ArrayList<String> = arrayListOf()
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
        cardStackView = binding.cardStackView
        // prevent users from swiping cards
        manager.setCanScrollHorizontal(false)
        manager.setCanScrollVertical(false)
        // show one card on stack
        manager.setStackFrom(StackFrom.None)
        // manager will define the layout for the card stack view
        cardStackView.layoutManager = manager
        // fetch values from view model
        groups = matchTabViewModel.groups.value!!
        removeGroups = matchTabViewModel.removeGroups.value!!
        primaryGroup = matchTabViewModel.getPrimaryGroup()
        prevGroup = matchTabViewModel.prevGroup.value
        matchedGroups = matchTabViewModel.matchedGroups.value!!
        matchTabViewModel.matchedGroups.observe(viewLifecycleOwner) { newMatchedGroups ->
            if (newMatchedGroups != null) {
                matchedGroups = newMatchedGroups
            }
        }
        /*
        * fetch all possible groups to match based on logged in user
        * and send to adapter which will display the groups in a recycler view
         */
        // reset undo state if previous group is null
        if (prevGroup == null) {
            //currentUserDocRef.update("undoState", false)
            matchTabViewModel.undoState.value = false
        }

        // get all potential groups of current user to match with
        currentUserDocRef
            .get()
            // if successful filter out certain groups for matching
            .addOnSuccessListener { result ->
                // convert the fetched user into a User object
                val user: User = result?.toObject(User::class.java)!!
                // if user does not have a primary group, display message and return
                if (user.primaryGroup.isNullOrEmpty()) {
                    binding.matchTabMessage.text = getString(R.string.set_primary_group_message)
                    return@addOnSuccessListener
                } else {
                    // if primary group has changed, clear the groups arraylist
                    if (primaryGroup != user.primaryGroup) {
                        primaryGroup = user.primaryGroup
                        matchTabViewModel.setPrimaryGroup(user.primaryGroup)
                        Log.v(TAG, "set primary group: $primaryGroup")
                        groups.clear()
                        matchTabViewModel.groups.value = groups
                    }
                }
                if (user.matchedGroups.isNotEmpty()) {
                    matchTabViewModel.matchedGroups.value = user.matchedGroups
                }
                // if user is not a group, then display message and return
                if (user.myGroups.isEmpty()) {
                    binding.matchTabMessage.text = getString(R.string.join_group_message)
                    return@addOnSuccessListener
                }
                //if the groups has not been populated (is empty), fetch the groups from firebase
                //else reuse the fetched groups
                if (groups.isEmpty()) {
                    fetchGroups(cardStackView)
                    //setAdapter(cardStackView)
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
                    setAdapter(cardStackView)
                    if (prevGroup != null) {
                        // groups[0], the previous group, will be displayed so we will skip to groups[1]
                        manager.scrollToPosition(1)
                    }
                }
            }
        return binding.root
    }
    // if adapter has not been set, set it
    // otherwise notify the adapter that groups has changed
    private fun setAdapter(cardStackView: CardStackView) {
        if (cardStackView.adapter == null) {
            if (context != null) {
                cardStackView.adapter =
                    CardStackAdapter(currentUser.uid, requireContext(), groups, this)
            }
        } else {
            cardStackView.adapter?.notifyDataSetChanged()
        }
    }
    // add a filter group and update groups
    private fun addFilterGroup(cardStackView: CardStackView, filterGroups: ArrayList<String?>, groups: ArrayList<Group>, group: Group) {
        if (!filterGroups.contains(group.name)) {
            filterGroups.add(group.name.toString())
            groups.remove(group)
//            if (groups.isEmpty()) {
//                if (_binding != null) binding.matchTabMessage.text = getString(R.string.no_group_found)
//            } else {
//                if (_binding != null) binding.matchTabMessage.text = ""
//            }
        }
    }
    // fetches groups from firebase
    private fun fetchGroups(cardStackView: CardStackView) {
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
                    if (group == matchTabViewModel.getPrimaryGroup()) {
                        primaryGroupObj = query1Docs.toObject(Group::class.java)
                    }
                    // add the name of the pending group
                    if (!filterGroups.contains(group)) {
                        filterGroups.add(group)
                    }
                }
                if (primaryGroupObj == null) return@addOnSuccessListener
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
                        // preferences
                        var longitude: Number? = null
                        var latitude: Number? = null
                        var maxAge: Number? = null
                        var minAge: Number? = null
                        var maxDistance: Number? = null
                        var genderPref: String? = null

                        // filter by interest tags
                        val interestTags = primaryGroupObj.interestTags!!.map { it -> it.lowercase() } as ArrayList<String>
                        // arraylist of groups with common interest tags will be displayed first
                        val interestGroups = arrayListOf<Group>()
                        // once all groups with common interest tags have been swiped
                        // display groups with no common interest tags
                        val noInterestGroups = arrayListOf<Group>()

                        // if the preferences of group is not null, set the values
                        // if the preferences is null, groups will be fetched via interests and noninterests
                        if (primaryGroupObj.preferences != null) {
                            longitude = primaryGroupObj.preferences?.get("longitude") as Number
                            latitude = primaryGroupObj.preferences?.get("latitude") as Number
                            maxAge = primaryGroupObj.preferences?.get("maxAge") as Number
                            minAge = primaryGroupObj.preferences?.get("minimumAge") as Number
                            maxDistance = primaryGroupObj.preferences?.get("maxDistance") as Number
                            genderPref = primaryGroupObj.preferences?.get("gender") as String
                        }
                        Log.v(TAG, matchedGroups.toString())
                        // loop through groups
                        for (doc in it) {
                            // convert the query document snapshot into a group object to access variables
                            val obj = doc.toObject(Group::class.java)
                            // if group is already filtered, continue
                            if (filterGroups.contains(obj.name)) {
                                continue
                            }
                            // if group is matched, remove it
                            if (matchedGroups.contains(obj.name)) {
                                Log.v(TAG, "matched group contains: ${obj.name}")
                                if (!filterGroups.contains(obj.name)) {
                                    filterGroups.add(obj.name)
                                }
                            }
                            if (primaryGroupObj.preferences != null) {
                                // float array stores results from distanceBetween function
                                val distanceBetweenArray = FloatArray(3)
                                // if distance preferences are set, then filter out groups based on distance
                                if ((longitude != null) && (latitude != null) && (maxDistance != null)) {
                                    // if the other groups has their longitude or latitude set, check if the group should be filtered
                                    // else filter it
                                    if (obj.preferences?.get("longitude") != null && obj.preferences["latitude"] != null) {
                                        val objLongitude = obj.preferences["longitude"] as Double
                                        val objLatitude = obj.preferences["latitude"] as Double
                                        if (!objLatitude.isNaN() && !objLongitude.isNaN()) {
                                            // get distance between 2 coordinates and store into distanceBetweenArray
                                            distanceBetween(
                                                longitude as Double,
                                                latitude as Double,
                                                objLongitude,
                                                objLatitude,
                                                distanceBetweenArray
                                            )
                                            // convert to miles
                                            val distanceBetweenMiles =
                                                distanceBetweenArray[0] * 0.000621371192
                                            // check if in range, if not then filter it
                                            if (distanceBetweenMiles > maxDistance.toDouble()) {
                                                addFilterGroup(
                                                    cardStackView,
                                                    filterGroups,
                                                    groups,
                                                    obj
                                                )
                                                continue
                                            }
                                        } else {
                                            // if the other group does not have longitude or latitude set, filter it
                                            addFilterGroup(cardStackView, filterGroups, groups, obj)
                                            continue
                                        }
                                    } else {
                                        // filter out other group if longitude or latitude it not set
                                        addFilterGroup(cardStackView, filterGroups, groups, obj)
                                        continue
                                    }
                                }
                                // loop through each user in the group to get age, gender,
                                obj.users?.forEach { user ->
                                    if (filterGroups.contains(obj.name)) {
                                        return@forEach
                                    }
                                    // query user
                                    db.collection("users").document(user)
                                        .get()
                                        .addOnSuccessListener { user ->
                                            // if genderPref is not set, ignore
                                            if (genderPref != null) {
                                                if ((genderPref == "male" && user.getString(
                                                        "gender"
                                                    ) == "female")
                                                    ||
                                                    (genderPref == "female" && user.getString(
                                                        "gender"
                                                    ) == "male")
                                                ) {
                                                    addFilterGroup(
                                                        cardStackView,
                                                        filterGroups,
                                                        groups,
                                                        obj
                                                    )
                                                }
                                            }

                                            val birthdate = user.getString("birthdate")
                                            // if birthdate, minAge, or maxAge is not null
                                            // check if a user is not within age limits
                                            //      else filter out group
                                            if (!birthdate.isNullOrEmpty() && minAge != null && maxAge != null) {
                                                // get birth date
                                                val birthdateSplit = birthdate.split("/")
                                                val month = birthdateSplit[0].toInt()
                                                val day = birthdateSplit[1].toInt()
                                                val year = birthdateSplit[2].toInt()
                                                // get age based on birth date
                                                val age = getAge(year, month, day)
                                                // if age is less than the minimum age or
                                                // greater than the maximum age, filter the group
                                                if (age < minAge.toInt() || age > maxAge.toInt()) {
                                                    addFilterGroup(
                                                        cardStackView,
                                                        filterGroups,
                                                        groups,
                                                        obj
                                                    )
                                                }
                                            } else {
                                                addFilterGroup(
                                                    cardStackView,
                                                    filterGroups,
                                                    groups,
                                                    obj
                                                )
                                            }
                                            if (cardStackView.adapter == null) {
                                                if (context != null) {
                                                    cardStackView.adapter =
                                                        CardStackAdapter(
                                                            currentUser.uid,
                                                            requireContext(),
                                                            groups,
                                                            this
                                                        )
                                                }
                                            } else {
                                                cardStackView.adapter?.notifyDataSetChanged()
                                            }
                                        }
                                }
                            }
                            // if the group is not filtered out, check if there are matching interests
                            // add the group based on interest or no interests
                            if (!filterGroups.contains(obj.name)) {
                                // check if groups have a common interest
                                for (tag in obj.interestTags!!) {
                                    // if a common interest is found, add to interestGroups and continue
                                    if (interestTags.contains(tag.lowercase().trim()) && !interestGroups.contains(obj)) {
                                        interestGroups.add(obj)
                                        continue
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
                        groups.addAll(interestGroups)
                        groups.addAll(noInterestGroups)
                        // if groups is empty, display that the user needs to join a group
                        if (groups.isEmpty()) {
                            binding.matchTabMessage.text = getString(R.string.no_group_found)
                        } else {
                            // attach adapter and send groups and listener
                            setAdapter(cardStackView)
                        }
                    }
            }
            .addOnFailureListener { e ->
                Log.v(TAG, "error getting pending groups and groups documents: ", e)
            }
    }
    // rewind to previous card when undo button is clicked
    override fun onUndoBtnClick(position: Int) {
        // rewind function
        cardStackView.rewind()
        if (removeGroups.isNotEmpty()) {
            removeGroups.remove(prevGroup)
            matchTabViewModel.removeGroups.value = removeGroups
        }
        // reset previous group to null
        prevGroup = null
        matchTabViewModel.prevGroup.value = prevGroup
        // update undo state of current user
        //currentUserDocRef.update("undoState", false)
        matchTabViewModel.undoState.value = false
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
        // check if another user in the group has already initiated matching
        db.collection("pendingGroups")
            .whereEqualTo("matchingGroup", primaryGroup)
            .whereEqualTo("pendingGroup", currGroup?.name)
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
                    if (groups[groups.size - 1] == currGroup) {
                        val size = groups.size
                        groups.clear()
                        cardStackView.adapter?.notifyItemRangeRemoved(0, size)
                        removeGroups.clear()
                        prevGroup = null
                        matchTabViewModel.groups.value = groups
                        matchTabViewModel.removeGroups.value = removeGroups
                        matchTabViewModel.prevGroup.value = prevGroup
                        matchTabViewModel.undoState.value = false
                        fetchGroups(cardStackView)
                    } else {
                        removeGroups.add(currGroup!!)
                        prevGroup = null
                        // update undo state of current user
                        matchTabViewModel.undoState.value = false
                        matchTabViewModel.removeGroups.value = removeGroups
                        matchTabViewModel.prevGroup.value = prevGroup
                    }
                    //swipe the card
                    cardStackView.swipe()
                }
                // if the pending group does not exist in the database then create a new pending group
                if (!pendingGroupExists) {
                    if (primaryGroup != null) {
                        Log.v(TAG, "primary group: $primaryGroup")
                        db.collection("groups").document(primaryGroup.toString()).get()
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
                                    pendingGroup = currGroup?.name,
                                    users = users,
                                    pending = true,
                                    matched = false
                                )
                                // add pending group to firestore pendingGroups collection
                                db.collection("pendingGroups")
                                    .document(pendingGroup.pendingGroupId.toString())
                                    .set(pendingGroup)
                                    .addOnSuccessListener {
                                        voting(db, pendingGroup, matchTabViewModel)
                                        // if last group was accepted, fetch groups again
                                        if (groups[groups.size - 1] == currGroup) {
                                            val size = groups.size
                                            groups.clear()
                                            cardStackView.adapter?.notifyItemRangeRemoved(0, size)
                                            removeGroups.clear()
                                            prevGroup = null
                                            matchTabViewModel.groups.value = groups
                                            matchTabViewModel.removeGroups.value = removeGroups
                                            matchTabViewModel.prevGroup.value = prevGroup
                                            matchTabViewModel.undoState.value = false
                                            fetchGroups(cardStackView)
                                        } else {
                                            removeGroups.add(currGroup!!)
                                            prevGroup = null
                                            matchTabViewModel.removeGroups.value = removeGroups
                                            matchTabViewModel.prevGroup.value = prevGroup
                                            // update undo state of current user
                                            matchTabViewModel.undoState.value = false
                                        }
                                        //swipe the card
                                        cardStackView.swipe()
                                    }
                            }
                    }
                }
//                else {
//                    removeGroups.add(currGroup!!)
//                    prevGroup = null
//                    // update undo state of current user
//                    matchTabViewModel.undoState.value = false
//                    //swipe the card
//                    cardStackView.swipe()
//                }
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
        if (groups[groups.size - 1] == currGroup) {
            val size = groups.size
            groups.clear()
            cardStackView.adapter?.notifyItemRangeRemoved(0, size)
            removeGroups.clear()
            prevGroup = null
            matchTabViewModel.groups.value = groups
            matchTabViewModel.removeGroups.value = removeGroups
            matchTabViewModel.prevGroup.value = prevGroup
            matchTabViewModel.undoState.value = false
            fetchGroups(cardStackView)
        } else {
            removeGroups.add(currGroup!!)
            if (prevGroup != null) {
                removeGroups.add(prevGroup!!)
            }
            prevGroup = currGroup
            // update view model values
            matchTabViewModel.removeGroups.value = removeGroups
            matchTabViewModel.prevGroup.value = prevGroup
            // update undo state of current user
            matchTabViewModel.undoState.value = true
        }
        // swipe the card
        cardStackView.swipe()
    }

    override fun observeUndoState(undoBtn: ImageButton) {
        matchTabViewModel.undoState.observe(this) { state ->
            if (state) {
                undoBtn.alpha = 1F
            } else {
                undoBtn.alpha = .1F
            }
        }
    }

    // get age of user
    fun getAge(year: Int, month: Int, dayOfMonth: Int): Int {
        return Period.between(
            LocalDate.of(year, month, dayOfMonth),
            LocalDate.now()
        ).years
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