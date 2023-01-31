package com.example.batchtest

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Group(
    val name: String,
    val users: Array<User>,
    val interestTags: Array<String>,
    val biscuits: Number = 0,
    @ServerTimestamp
    var createdDate: Date? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Group

        if (name != other.name) return false
        if (!users.contentEquals(other.users)) return false
        if (!interestTags.contentEquals(other.interestTags)) return false
        if (biscuits != other.biscuits) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + users.contentHashCode()
        result = 31 * result + interestTags.contentHashCode()
        return result
    }

}
