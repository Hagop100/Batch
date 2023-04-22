package com.example.batchtest

import kotlin.collections.ArrayList

data class ReportInformation(
    val reportCount: Int = 0,
    val reportReason: ArrayList<String>? = null,
    val otherReason: ArrayList<String>? = null
)
