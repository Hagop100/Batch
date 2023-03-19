package com.example.batchtest

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Chat (
    var count: Int,
    var messages: ArrayList<Message>,
    var group1Name: String,
    var group2Name: String,
    @ServerTimestamp
    var createdDate: Date? = Date()
)