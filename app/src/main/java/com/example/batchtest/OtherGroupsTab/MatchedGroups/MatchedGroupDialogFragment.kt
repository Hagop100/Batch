package com.example.batchtest.OtherGroupsTab.MatchedGroups

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.batchtest.R
import com.example.batchtest.databinding.FragmentForgotPasswordDialogBinding
import com.example.batchtest.databinding.FragmentMatchedGroupBinding
import com.example.batchtest.databinding.FragmentMatchedGroupDialogBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MatchedGroupDialogFragment : DialogFragment(), MatchedGroupDialogAdapter.MatchedGroupDialogRecyclerViewEvent {

    //binding variables
    private var _binding: FragmentMatchedGroupDialogBinding? = null
    private val binding get() = _binding!!

    //my groups matched array list
    private var myGroupsMatchedArrayList: ArrayList<String> = ArrayList()

    //This is the necessary deprecated function to call the setPercentOfParent function
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setPercentOfParent(85, 55)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMatchedGroupDialogBinding.inflate(inflater, container, false)

        val matchedGroupRV = binding.fragmentMatchedGroupDialogRv
        matchedGroupRV.layoutManager = LinearLayoutManager(context)
        matchedGroupRV.setHasFixedSize(true)

        val db = Firebase.firestore

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
        //This will communicate to the next fragment which group we click on
        val bundle = bundleOf("groupName" to myGroupsMatchedArrayList[position])
        findNavController().navigate(R.id.action_otherGroupTabFragment_to_groupChatFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MGDialogFragment"
    }

}