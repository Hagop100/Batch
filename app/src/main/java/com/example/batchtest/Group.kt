package com.example.batchtest

import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Group(
    val groupId: String? = null,
    var name: String? = null,
//    val users: ArrayList<User>? = null,
    val users: ArrayList<String>? = null,
    val interestTags: ArrayList<String>? = null,
    val aboutUsDescription: String? = null,
    val image: String? = null,
    val leader: String? = null,
    var reportCount: Int = 0,
    var matchedGroups: ArrayList<String> = ArrayList(),
    var blockedGroups: ArrayList<String> = ArrayList(),
    val preferences: HashMap<String, Any>? = null,
    val biscuitsArray: ArrayList<String>? = null,
    @ServerTimestamp
    var createdDate: Date? = Date(),


    )




