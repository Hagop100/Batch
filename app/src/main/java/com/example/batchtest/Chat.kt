package com.example.batchtest

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Chat (
    var count: Int,
    var messages: Message,
    var cid: UUID,
    var group1: Group,
    var group2: Group,
    @ServerTimestamp
    var createdDate: Date? = Date()
)