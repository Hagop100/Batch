package com.example.batchtest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.*
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "GroupsFetchLog";
/**
 * Match Tab Fragment
 * displays potential groups the user can match with
 */
class MatchTabFragment : Fragment(), CardStackAdapter.CardStackAdapterListener {
    private lateinit var binding: FragmentMatchTabBinding
    // inflate and bind the match tab fragment after view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

//        // store groups fetched from database
        val groups = arrayListOf<MutableMap<String, Any>>()

////         test groups without fetching from db
//        val g1: Group = Group("One Direction", arrayListOf(User("Harry", "Styles", "harrystyles@gmail.com")), arrayListOf("singing", "dancing"), "test_description")
//        val g1Map: MutableMap<String, Any> = mutableMapOf("name" to "One Direction", "users" to arrayListOf(User("Harry", "Styles", "harrystyles@gmail.com")), "interestTags" to arrayListOf("singing", "dancing"), "aboutUsDescription" to "description", "biscuits" to 0, "createdDate" to Date())
//        groups.add(g1Map)
//        groups.add(g1Map)
//        // set adapter using groups to display information to recycler view
//        cardStackView.adapter = CardStackAdapter(groups, this)

//      // fetch groups from database
        val db = Firebase.firestore;
        db.collection("groups")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val group: MutableMap<String, Any> = doc.data;
                    groups.add(group)
//                    Log.v(TAG, "documents: ${doc.data["users"]}")
                }
                cardStackView.adapter = CardStackAdapter(groups, this)
            }
            .addOnFailureListener { e ->
                Log.v(TAG, "error getting documents: ", e)
            }

        // find the nav bar and set it visible upon this fragment's onCreateView
        val navBar: BottomNavigationView? = getActivity()?.findViewById(R.id.nav_bar)
        navBar?.visibility = View.VISIBLE

        return binding.root
    }

    override fun onUndoBtnClick() {
        binding.cardStackView.rewind()
    }

    override fun onMoreBtnClick() {
        //
    }


}