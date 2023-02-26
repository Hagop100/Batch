package com.example.batchtest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.batchtest.databinding.FragmentOtherGroupTabBinding


/**
 * A simple [Fragment] subclass.
 * Use the [OtherGroupTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private var TAG = "OtherGroupsTab"

class OtherGroupTabFragment : Fragment() {
    private lateinit var binding: FragmentOtherGroupTabBinding
    // initially set to matched fragment
    private var isMatching: Boolean = true;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // check if a state was saved
        // if a saved state exists, restore it
        // else initialize to the matched fragment
        if (savedInstanceState != null) {
            // get the saved nested fragment state
            isMatching = savedInstanceState.getBoolean("isMatching")
//            val childFragmentManager = child.addOnBackStackChangedListener {
//
//            }
            // begin a nested fragment transaction
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            // if the saved state was on the matched tab, then restore it
            // else the saved state was on the pending tab and will be restored
            if (isMatching) {
                // initialize MatchedGroupFragment
                val matchedGroupFragment = MatchedGroupFragment()
                // use fragment transaction method to replace the current fragment with the matched group fragment
                transaction.replace(R.id.other_group_fragment_container, matchedGroupFragment)
            } else {
                // initialize PendingGroupFragment
                val pendingGroupFragment = PendingGroupFragment()
                // use fragment transaction method to replace the current fragment with the pending group fragment
                transaction.replace(R.id.other_group_fragment_container, pendingGroupFragment)
            }
            // commit the transaction
            transaction.addToBackStack(null)
            transaction.commit()
        } else {
            // initialize the nested fragment with the matched group fragment
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            val matchedGroupFragment = MatchedGroupFragment()
            transaction.replace(R.id.other_group_fragment_container, matchedGroupFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // bind to other group tab layout
        binding = FragmentOtherGroupTabBinding.inflate(inflater, container, false)

        binding.pendingBtn.setOnClickListener {
            binding.pendingBtn.requestFocus()
            // set pending button as selected to add styling
            binding.pendingBtn.isSelected = true
            // set matched button as unselected to remove styling
            binding.matchedBtn.isSelected = false
            isMatching = false
            // initialize PendingGroupFragment
            val pendingGroupFragment = PendingGroupFragment()
            // replace nested matched fragment with pending fragment
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.other_group_fragment_container, pendingGroupFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // set click listener for matched button
        binding.matchedBtn.setOnClickListener {
            binding.matchedBtn.requestFocus()
            // set matched button as selected to add styling
            binding.matchedBtn.isSelected = true
            // set pending button as unselected to remove styling
            binding.pendingBtn.isSelected = false
            isMatching = true
            // initialize MatchedGroupFragment
            val matchedGroupFragment = MatchedGroupFragment()
            // replace nested pending fragment with matched fragment
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.other_group_fragment_container, matchedGroupFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        // inflate the layout for this fragment
        return binding.root
    }

    // on start or restart, set the focus to matched or pending depending on isMatching value
    override fun onStart() {
        super.onStart()
        // if matched focus on matched else pending
        if (isMatching) {
            binding.matchedBtn.requestFocus()
            binding.matchedBtn.isSelected = true
            binding.pendingBtn.isSelected = false
        } else {
            binding.pendingBtn.requestFocus()
            binding.matchedBtn.isSelected = false
            binding.pendingBtn.isSelected = true
        }
    }

    // save the state when the user switches fragments into isMatching variable
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isMatching", isMatching)
    }
}