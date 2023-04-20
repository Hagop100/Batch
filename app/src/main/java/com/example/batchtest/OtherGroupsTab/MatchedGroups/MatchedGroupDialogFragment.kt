package com.example.batchtest.OtherGroupsTab.MatchedGroups

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentForgotPasswordDialogBinding
import com.example.batchtest.databinding.FragmentMatchedGroupBinding
import com.example.batchtest.databinding.FragmentMatchedGroupDialogBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MatchedGroupDialogFragment(groupsList: ArrayList<String>) : DialogFragment(), MatchedGroupDialogAdapter.MatchedGroupDialogRecyclerViewEvent {

    //binding variables
    private var _binding: FragmentMatchedGroupDialogBinding? = null
    private val binding get() = _binding!!

    //my groups matched array list
    private var myGroupsMatchedArrayList: ArrayList<String> = ArrayList()

    init {
        myGroupsMatchedArrayList = groupsList
    }

    //This is the necessary deprecated function to call the setPercentOfParent function
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setPercentOfParent(85, 55)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMatchedGroupDialogBinding.inflate(inflater, container, false)

        val matchedGroupDialogRV = binding.fragmentMatchedGroupDialogRv
        matchedGroupDialogRV.layoutManager = LinearLayoutManager(context)
        matchedGroupDialogRV.setHasFixedSize(true)

        // attach adapter and send groups
        val matchedGroupDialogAdapter =
            context?.let { MatchedGroupDialogAdapter(myGroupsMatchedArrayList, this, it) }
        matchedGroupDialogRV.adapter = matchedGroupDialogAdapter

        return binding.root
    }

    /*
    This function will decide the size of the dialog fragment.
    Both its height and width
     */
    private fun DialogFragment.setPercentOfParent(percentageW: Int, percentageH: Int) {
        val percentW = percentageW.toFloat() / 100
        val percentH = percentageH.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percentW
        val percentHeight = rect.height() * percentH
        dialog?.window?.setLayout(percentWidth.toInt(), percentHeight.toInt())
    }

    override fun onItemClick(position: Int) {
        Log.i(TAG, "click")
        //This will communicate to the next fragment which group we click on
        setFragmentResult("Group_Key", bundleOf("Group_Value" to myGroupsMatchedArrayList[position]))
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MGDialogFragment"
    }

}