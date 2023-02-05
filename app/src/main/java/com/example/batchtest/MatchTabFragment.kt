package com.example.batchtest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.example.batchtest.databinding.FragmentUserListBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "GroupsFetchLog";
/**
 * Match Tab Fragment
 * displays potential groups the user can match with
 */
class MatchTabFragment : Fragment() {
    // inflate and bind the match tab fragment after view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // bind fragment
        val binding = FragmentMatchTabBinding.inflate(inflater, container, false)
        // get card stack view
        val cardStackView = binding.cardStackView
//      // set layout manager to card stack view to arrange recycler view
        cardStackView.layoutManager = CardStackLayoutManager(context)
        // store groups fetched from database
        val groups = arrayListOf<MutableMap<String, Any>>()
        val g1: Group = Group("One Direction", arrayListOf(User("Harry", "Styles", "harrystyles@gmail.com")), arrayListOf("singing", "dancing"))
        val g1Map: MutableMap<String, Any> = mutableMapOf("name" to "One Direction", "users" to arrayListOf(User("Harry", "Styles", "harrystyles@gmail.com")), "interestTags" to arrayListOf("singing", "dancing"), "questions" to arrayListOf(Question("q1", "answer")), "biscuits" to 0, "createdDate" to Date())
        groups.add(g1Map)
        groups.add(g1Map)
        // set adapter using groups to display information to recycler view
        cardStackView.adapter = CardStackAdapter(groups)
//        val db = Firebase.firestore;
//        db.collection("groups")
//            .get()
//            .addOnSuccessListener { result ->
//                for (doc in result) {
//                    val group: MutableMap<String, Any> = doc.data;
//                    groups.add(group)
////                    Log.v(TAG, "documents: ${doc.data["users"]}")
//                }
//                cardStackView.adapter = CardStackAdapter(groups)
//            }
//            .addOnFailureListener { e ->
//                Log.v(TAG, "error getting documents: ", e)
//            }
        // find the nav bar and set it visible upon this fragment's onCreateView
        val navBar: BottomNavigationView? = getActivity()?.findViewById(R.id.nav_bar)
        navBar?.visibility = View.VISIBLE

        return binding.root
    }
}