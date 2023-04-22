package com.example.batchtest

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.batchtest.databinding.FragmentReportDialogBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ReportDialogFragment(entityBeingReported : String, fragmentArrivedFrom: String) : DialogFragment() {

    //binding variables
    private var _binding: FragmentReportDialogBinding? = null
    private val binding get() = _binding!!

    //current user
    private lateinit var currUser: FirebaseUser

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currUser = Firebase.auth.currentUser!!
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
                //get information that user submitted and store it in a report object
                var reportReason: String? = null
                if(binding.fragmentReportDialogProfanityRb.isChecked) {
                    reportReason = binding.fragmentReportDialogProfanityRb.text.toString()
                }
                else if(binding.fragmentReportDialogImposterRb.isChecked) {
                    reportReason = binding.fragmentReportDialogImposterRb.text.toString()
                }
                else if(binding.fragmentReportDialogHarassmentRb.isChecked) {
                    reportReason = binding.fragmentReportDialogHarassmentRb.text.toString()
                }

                //get other reason that is explained
                var otherReason: String? = null
                otherReason = binding.fragmentReportDialogExplanationEt.text.toString()

                if(binding.fragmentReportDialogRg.checkedRadioButtonId == -1) {
                    Toast.makeText(context, "Please select a reason for the report", Toast.LENGTH_SHORT).show()
                }
                else {
                    //store reportCount from groups object
                    var reportCount: Int = 0
                    //this will update the report count in the groups collection
                    db.collection("groups")
                        .document(entityBeingReported)
                        .get()
                        .addOnSuccessListener { d ->
                            val group: Group? = d.toObject<Group>()
                            group!!.reportCount += 1

                            //storing reportCount
                            reportCount = group!!.reportCount

                            //build the report object
                            val reportInfo: ReportInformation = ReportInformation(
                                reportCount = reportCount,
                                reportReason = arrayListOf(reportReason!!),
                                otherReason = arrayListOf(otherReason)
                            )

                            //get reportObject from database if it exists
                            var reportObject: ReportInformation? = null
                            val docRef = db.collection("reports").document(entityBeingReported)

                            docRef.get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val document = task.result
                                    if(document != null) {
                                        if (document.exists()) {
                                            Log.d("TAG", "Document already exists.")
                                            //if it exists update else create
                                            docRef.update("reportReason", FieldValue.arrayUnion(reportReason))
                                            docRef.update("otherReason", FieldValue.arrayUnion(otherReason))
                                        } else {
                                            Log.d("TAG", "Document doesn't exist.")
                                            //we will create the reports collection
                                            db.collection("reports").document(entityBeingReported)
                                                .set(reportInfo)
                                        }
                                    }
                                } else {
                                    Log.d("TAG", "Error: ", task.exception)
                                }
                            }

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
        }
        else {
            //EMANUEL YOUR CODE GOES IN THIS BLOCK
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