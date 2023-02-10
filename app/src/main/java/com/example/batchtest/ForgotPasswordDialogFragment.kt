package com.example.batchtest

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment


class ForgotPasswordDialogFragment : DialogFragment() {

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setPercentOfParent(85, 55)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forgot_password_dialog, container, false)
    }

    private fun DialogFragment.setPercentOfParent(percentageW: Int, percentageH: Int) {
        val percentW = percentageW.toFloat() / 100
        val percentH = percentageH.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percentW
        val percentHeight = rect.height() * percentH
        dialog?.window?.setLayout(percentWidth.toInt(), percentHeight.toInt())
    }



}