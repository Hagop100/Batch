package com.example.batchtest

import java.util.*
import kotlin.collections.HashMap

// Pending Group data class between two groups
data class PendingGroup(
    val pendingGroupId: String? = null,
    val matchingGroup: Group? = null,
    val pendingGroup: Group? = null,
    val users: ArrayList<HashMap<String, Any>>? = null,
    val isPending: Boolean? = null,
    val isMatched: Boolean? = null
) {
}
