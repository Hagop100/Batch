package com.example.batchtest

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.batchtest.databinding.FragmentReportDialogBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ReportDialogFragment(entityBeingReported : String, fragmentArrivedFrom: String) : DialogFragment() {

    //binding variables
    private var _binding: FragmentReportDialogBinding? = null
    private val binding get() = _binding!!

    //variables holding constructor information
    private var entityBeingReported: String //can be group or user
    private var fragmentArrivedFrom: String //the fragment we are arriving from

    init {
        this.entityBeingReported = entityBeingReported
        this.fragmentArrivedFrom = fragmentArrivedFrom
    }

    //This is the necessary deprecated function to call the setPercentOfParent function
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setPercentOfParent(85, 70)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentReportDialogBinding.inflate(inflater, container, false)

        val db = Firebase.firestore

        if(fragmentArrivedFrom == "MatchedGroupFragment") {
            //set title
            val title: String = "Report Group"
            binding.fragmentReportDialogTitleTv.text = title

            binding.fragmentReportDialogSubmitBtn.setOnClickListener {
                //this will update the report count in the groups collection
                db.collection("groups")
                    .document(entityBeingReported)
                    .get()
                    .addOnSuccessListener { d ->
                        val group: Group? = d.toObject<Group>()
                        group!!.reportCount += 1
                        val currGroup = db.collection("groups").document(d.id)
                        currGroup.update("reportCount", group!!.reportCount)
                            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                    }
            }
        }
        else {

        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ReportDialogFragment"
    }
}