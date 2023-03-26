package com.example.batchtest

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Message(
    var content: String? = null,
    var username: String? = null,
    @ServerTimestamp
    var createdDate: Date? = Date()
)
