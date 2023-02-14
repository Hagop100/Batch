package com.example.batchtest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentMatchTabBinding
import com.example.batchtest.databinding.FragmentOtherGroupTabBinding

/**
 * A simple [Fragment] subclass.
 * Use the [OtherGroupTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private var TAG = "OtherGroupsTab"
class OtherGroupTabFragment : Fragment() {
    private lateinit var binding: FragmentOtherGroupTabBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // bind to other group tab layout
        binding = FragmentOtherGroupTabBinding.inflate(inflater, container, false)
        // matched button will be focused initially, displaying list of matched groups
//        // on click
//        binding.matchedBtn.setOnClickListener {
//            Log.v(TAG, "clicked")
//            binding.matchedBtn.requestFocus();
//        }
//        // on focus
//        binding.matchedBtn.setOnFocusChangeListener { view, b ->
//            Log.v(TAG, "focused")
//        }
        // inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.matchedBtn.requestFocus()
        val matchedGroupFragment = MatchedGroupFragment()
        val pendingGroupFragment = PendingGroupFragment()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
//        binding.matchedBtn.setOnClickListener {
//            transaction.replace(R.id.other_group_fragment_container, pendingGroupFragment)
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }
//        binding.pendingBtn.setOnClickListener {
//            transaction.replace(R.id.other_group_fragment_container, matchedGroupFragment)
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }
//            binding.matchedBtn.setOnClickListener {
//                remove(pendingGroupFragment)
//                add(R.id.other_group_fragment_container, matchedGroupFragment)
//                commit()
//            }
//            binding.pendingBtn.setOnClickListener {
//                remove(matchedGroupFragment)
//                add(R.id.other_group_fragment_container, pendingGroupFragment)
//                commit()
//            }


    }
}