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
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.batchtest.databinding.FragmentForgotPasswordDialogBinding
import com.example.batchtest.databinding.FragmentMFAuthenticationVerificationDialogBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit


class MFAuthenticationVerificationDialogFragment : DialogFragment() {

    //binding variables
    private var _binding: FragmentMFAuthenticationVerificationDialogBinding? = null
    private val binding get() = _binding!!

    //code variable
    private lateinit var smsCode: String

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
        _binding = FragmentMFAuthenticationVerificationDialogBinding.inflate(inflater, container, false)

        binding.fragmentMfAuthenticationVerifyBtn.setOnClickListener {
            smsCode = binding.fragmentMfAuthenticationCodeEt.text.toString()
            setFragmentResult("SMS_Code_Key", bundleOf("SMS_Code_Value" to smsCode))
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

}