package com.example.batchtest.OtherGroupsTab.MatchedGroups

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.batchtest.Group
import com.example.batchtest.OtherGroupsTab.PendingGroups.PendingGroupAdapter
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentLoginBinding
import com.example.batchtest.databinding.FragmentMatchedGroupBinding
import com.example.batchtest.myGroupsTab.MyGroupAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MatchedGroupFragment : Fragment(), MatchedGroupAdapter.MatchedGroupRecyclerViewEvent {

    //binding variables
    private var _binding: FragmentMatchedGroupBinding? = null
    private val binding get() = _binding!!

    //ArrayList for groups
    private var matchedGroupArrayList: ArrayList<Group> = arrayListOf<Group>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMatchedGroupBinding.inflate(inflater, container, false)

        val matchedGroupRV = binding.fragmentMatchedGroupsRecyclerView
        matchedGroupRV.layoutManager = LinearLayoutManager(context)
        matchedGroupRV.setHasFixedSize(true)

        matchedGroupArrayList = arrayListOf<Group>()
        val db = Firebase.firestore
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
                    matchedGroupArrayList.add(group)
                }
                // attach adapter and send groups
                val matchedGroupAdapter = MatchedGroupAdapter(matchedGroupArrayList, this)
                matchedGroupRV.adapter = matchedGroupAdapter
            }
            .addOnFailureListener { e ->
                Log.v(TAG, "error getting documents: ", e)
            }

        val swipe = object: MatchedGroupsSwipeHelper(requireActivity(), matchedGroupRV, 200) {
            override fun instantiateMatchedGroupButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MatchedGroupButton>
            ) {
                buffer.add(MatchedGroupButton(requireActivity(),
                    "Delete",
                    30,
                    R.drawable.ic_baseline_delete_24,
                    Color.parseColor("#FF3C30"),
                    object:MatchedGroupAdapter.MatchedGroupRecyclerViewEvent {
                        override fun onItemClick(position: Int) {
                            Toast.makeText(requireActivity(), "Delete" + position, Toast.LENGTH_SHORT).show()
                        }
                    }
                ))

                buffer.add(MatchedGroupButton(requireActivity(),
                    "Report",
                    30,
                    R.drawable.ic_baseline_report_24,
                    Color.parseColor("#FF9502"),
                    object:MatchedGroupAdapter.MatchedGroupRecyclerViewEvent {
                        override fun onItemClick(position: Int) {
                            Toast.makeText(requireActivity(), "Report" + position, Toast.LENGTH_SHORT).show()
                        }
                    }
                ))
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MatchedGroupFragment"
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(activity, matchedGroupArrayList[position].name, Toast.LENGTH_SHORT).show()
    }

}