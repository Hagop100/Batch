package com.example.batchtest

import java.util.*
import kotlin.collections.HashMap

// Pending Group data class between two groups
data class PendingGroup(
    val pendingGroupId: String? = null,
    val group: String? = null,
    val pendingGroup: String? = null,
    val votes: HashMap<String, Boolean>? = null,
    val isPending: Boolean? = null,
    val isMatched: Boolean? = null
) {
}
