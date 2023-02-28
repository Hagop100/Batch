package com.example.batchtest

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import com.example.batchtest.databinding.DialogLayoutBinding
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    // inflate and bind the match tab fragment after view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // bind fragment
        binding = FragmentMatchTabBinding.inflate(inflater, container, false)

        // get card stack view
        val cardStackView = binding.cardStackView
        // set layout manager to card stack view to arrange recycler view
        val manager = CardStackLayoutManager(context)
        // prevent users from swiping cards
        manager.setCanScrollHorizontal(false)
        manager.setCanScrollVertical(false)
        // show one card on stack
        manager.setStackFrom(StackFrom.None)

        // accept button accepts group when clicked
        val acceptBtn = binding.acceptBtn
        acceptBtn.setOnClickListener{
            // card swipes right for accept
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }

        // reject button rejects group when clicked
        val rejectBtn = binding.rejectBtn
        rejectBtn.setOnClickListener{
            // card swipes left for accept
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }

        // manager will define the layout for the card stack view
        cardStackView.layoutManager = manager

        // fetch groups from database using firebase's firestore
        val groups = arrayListOf<Group>()
        val db = Firebase.firestore

        // test groups without fetching for speed
//        val g1 = Group("One Direction", arrayListOf(User("Harry", "Styles", "harrystyles@gmail.com")), arrayListOf("singing", "dancing", "partying", "soccer"), "one direction test description")
//        val g2 = Group("The Beatles", arrayListOf(User("John", "Lennon", "johnlennon@gmail.com")), arrayListOf("cooking", "karaoke", "gaming", "movies"), "the beatles test description")
//        val g3 = Group("Fleetwood Mac", arrayListOf(User("Stevie", "Nicks", "stevienicks@gmail.com")), arrayListOf("studying", "swimming", "sleeping", "music"), "the beatles test description")
//        groups.add(g1)
//        groups.add(g2)
//        groups.add(g3)
//        cardStackView.adapter = CardStackAdapter(groups, this)
        /*
        * fetch all groups and send to adapter which
        * will display the groups in a recycler view
         */
        db.collection("groups")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    // get group's data in form of map
                    val group: Group = doc.toObject(Group::class.java)
                    // add group to groups
                    groups.add(group)
                }
                // attach adapter and send groups and listener
                cardStackView.adapter = CardStackAdapter(groups, this)
            }
            .addOnFailureListener { e ->
                Log.v(TAG, "error getting documents: ", e)
            }

        return binding.root
    }

    // rewind to previous card when undo button is clicked
    override fun onUndoBtnClick() {
        binding.cardStackView.rewind()
    }
}