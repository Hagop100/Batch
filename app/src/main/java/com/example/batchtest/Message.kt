package com.example.batchtest

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Message(
    var content: String,
    var user: User,
    @ServerTimestamp
    var createdDate: Date? = Date()
)
