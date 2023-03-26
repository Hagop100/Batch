package com.example.batchtest

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Chat (
    var count: Int = 0,
    var messages: ArrayList<Message> = ArrayList(),
    var group1Name: String? = null,
    var group2Name: String? = null,
    @ServerTimestamp
    var createdDate: Date? = Date()
)