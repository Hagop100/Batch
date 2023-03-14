package com.example.batchtest

import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

data class Group(
    val name: String? = null,
//    val users: ArrayList<User>? = null,
    val users: ArrayList<String>? = null,
    val interestTags: ArrayList<String>? = null,
    val aboutUsDescription: String? = null,
    val biscuits: Int = 0,
    val image: String? = null,
    var reportCount: Int = 0,

    @ServerTimestamp
    var createdDate: Date? = Date(),

    )




