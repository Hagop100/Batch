package com.example.batchtest

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.batchtest.databinding.FragmentForgotPasswordDialogBinding
import com.example.batchtest.databinding.FragmentLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ForgotPasswordDialogFragment : DialogFragment() {

    //binding variables
    private var _binding: FragmentForgotPasswordDialogBinding? = null
    private val binding get() = _binding!!

    //email variable
    private lateinit var email: String

    //This is the necessary deprecated function to call the setPercentOfParent function
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setPercentOfParent(85, 55)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentForgotPasswordDialogBinding.inflate(inflater, container, false)

        /*
        If you press submit then fireabse will attempt to send an email to your account asking to reset password
         */
        binding.fragmentForgotPasswordSubmitBtn.setOnClickListener {
            email = binding.fragmentForgotPasswordEmailEt.text.toString()
            sendPasswordResetEmail(email)
        }

        //Cancel button simply exits the dialog fragment
        binding.fragmentForgotPasswordCancelBtn.setOnClickListener {
            dismiss()
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

    /*
    Firebase API to send a password reset email
    If successful, then the user should receive an email asking to reset password
     */
    private fun sendPasswordResetEmail(emailAddress: String) {
        if(emailAddress.isEmpty()) {
            Toast.makeText(activity, "Enter Email Please.", Toast.LENGTH_SHORT).show()
        }
        else {
            Firebase.auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                        Toast.makeText(activity, "Email sent.", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    else {
                        Toast.makeText(activity, "No Email Found.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }

    companion object {
        private const val TAG = "FPDialogFragment"
    }
}