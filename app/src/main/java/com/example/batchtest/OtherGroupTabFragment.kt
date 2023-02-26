package com.example.batchtest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
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
    private var isMatched: Boolean = true;
    // initially set was matched to false
    private var wasMatched: Boolean = false;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize the child fragment manager to handle nested child fragment transactions
        val cfm = childFragmentManager

        // check if a state was saved
        // if a saved state exists, restore it
        // else initialize to the matched fragment
        if (savedInstanceState != null) {
            // get the saved nested fragment states

            // true if the user was on matched
            // false if the user was on pending
            isMatched = savedInstanceState.getBoolean("isMatched")
            // true if the user was on pending
            // false if the user was on matched
            wasMatched = savedInstanceState.getBoolean("wasMatched")
            // begin a nested fragment transaction
            val transaction: FragmentTransaction = cfm.beginTransaction()
            cfm.popBackStack()
            // if the saved state was on the matched tab, then restore it
            // else the saved state was on the pending tab and will be restored
            if (isMatched) {
                // initialize MatchedGroupFragment
                val matchedGroupFragment = MatchedGroupFragment()
                // use fragment transaction method to replace the current fragment with the matched group fragment
                transaction.replace(R.id.other_group_fragment_container, matchedGroupFragment)
                transaction.addToBackStack("matched")
                wasMatched = false
            } else {
                // initialize PendingGroupFragment
                val pendingGroupFragment = PendingGroupFragment()
                // use fragment transaction method to replace the current fragment with the pending group fragment
                transaction.replace(R.id.other_group_fragment_container, pendingGroupFragment, "pending")
                transaction.addToBackStack("pending")
                wasMatched = true
            }
            // commit the transaction
            transaction.commit()
        } else {
            // initialize the nested fragment with the matched group fragment
            val transaction: FragmentTransaction = cfm.beginTransaction()
            val matchedGroupFragment = MatchedGroupFragment()
            transaction.replace(R.id.other_group_fragment_container, matchedGroupFragment)
            transaction.addToBackStack("matched")
            transaction.commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // bind to other group tab layout
        binding = FragmentOtherGroupTabBinding.inflate(inflater, container, false)

        // request focus on focusBtn
        // while removing focus from unfocusBtn
        fun focusBtn(focusBtn: TextView, unfocusBtn: TextView) {
            // request focus for focus button
            focusBtn.requestFocus()
            // select focus button
            focusBtn.isSelected = true
            // unselect unfocus button
            unfocusBtn.isSelected = false
            // disable clicking again for focused button
            focusBtn.isClickable = false
            // enable clicking for unfocused button
            unfocusBtn.isClickable = true
        }

        // set initial focus of the top buttons when the view is created / restarted
        if (isMatched) {
            focusBtn(binding.matchedBtn, binding.pendingBtn)
            isMatched = true
            wasMatched = false
        } else {
            focusBtn(binding.pendingBtn, binding.matchedBtn)
            isMatched = false
            wasMatched = true
        }

        // when the back button is pressed, it will call this callback to refocus on the previous tab
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // initialize a fragment manager for the nested children fragments
            val cfm = childFragmentManager
            // get the count of child fragments in the stack
            val count = cfm.backStackEntryCount
            // check if fragment is not at the bottom of stack, which is an empty fragment
            if (count > 1) {
                // get the tag name of the fragment from when addToBackStack(name) was called
                val name = cfm.getBackStackEntryAt(count - 1).name
                // if the tag name was matched then we know the previous state was pending
                wasMatched = name !== "matched"
                // if isMatched is not true, then it will focus on the matched button
                // because the current will be the pending button
                // else if isMatched is true, focus on the pending button
                if (wasMatched) {
                    focusBtn(binding.matchedBtn, binding.pendingBtn)
                    isMatched = true
                    wasMatched = false
                } else {
                    focusBtn(binding.pendingBtn, binding.matchedBtn)
                    isMatched = false
                    wasMatched = true
                }
            } else {
                // when at the bottom of the stack (empty nested fragment)
                // reenable ability to click button
                if (isMatched) {
                    binding.matchedBtn.isClickable = true
                } else {
                    binding.pendingBtn.isClickable = true
                }
            }
            // this will call the normal back button callback
            // so the fragment transaction can be reverted
            if (isEnabled) {
                isEnabled = false
                requireActivity().onBackPressed()
                isEnabled = true
            }
        }

        // when the pending button is clicked, it will replace the
        // nested fragment with the pending fragment
        binding.pendingBtn.setOnClickListener {
            focusBtn(binding.pendingBtn, binding.matchedBtn)
            isMatched = false
            wasMatched = true
            // initialize PendingGroupFragment
            val pendingGroupFragment = PendingGroupFragment()
            // replace nested matched fragment with pending fragment using transaction
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.other_group_fragment_container, pendingGroupFragment)
            transaction.addToBackStack("pending")
            transaction.commit()
        }

        // when the matched button is clicked, it will replace the
        // nested fragment with the matched fragment
        binding.matchedBtn.setOnClickListener {
            focusBtn(binding.matchedBtn, binding.pendingBtn)
            isMatched = true
            wasMatched = false
            // initialize MatchedGroupFragment
            val matchedGroupFragment = MatchedGroupFragment()
            // replace nested pending fragment with matched fragment using transaction
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.other_group_fragment_container, matchedGroupFragment)
            transaction.addToBackStack("matched")
            transaction.commit()
        }
        // inflate the layout for this fragment
        return binding.root
    }

    // save the state when the user switches fragments
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // save the latest isMatched value
        outState.putBoolean("isMatched", isMatched)
        // save the latest wasMatched value
        outState.putBoolean("wasMatched", wasMatched)
    }
}