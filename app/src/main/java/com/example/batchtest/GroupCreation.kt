package com.example.batchtest

import java.util.*

data class GroupCreation (
    val groupCode: UUID,
    val groupName: String,
    val tags: List<String>,
    val groupDescription: String

)

