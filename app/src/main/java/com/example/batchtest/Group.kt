package com.example.batchtest

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Group(
    val name: String? = null,
    val users: ArrayList<User>? = null,
    val interestTags: ArrayList<String>? = null,
    val aboutUsDescription: String? = null,
    val biscuits: Int = 0,
    @ServerTimestamp
    var createdDate: Date? = Date(),

) {

}


