package com.example.batchtest

import java.util.*
import kotlin.collections.HashMap

// Pending Group data class between two groups
data class PendingGroup(
    val pendingGroupId: String? = null,
    val matchingGroup: String? = null,
    val pendingGroup: String? = null,
    val users: HashMap<String, HashMap<String, String>>? = null,
    val isPending: Boolean? = null,
    val isMatched: Boolean? = null
) {
    var pendingGroupObj: Group? = null
    var matchingGroupObj: Group? = null
}
