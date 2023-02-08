package com.example.batchtest

// User data class
// @param firstName - first name of user
// @param lastName - last name of user
// @param email - email of user
// @param password - password of user
data class User(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    // password ???
) {
    // returns full name of user
    fun getName(): String {
        return "$firstName $lastName"
    }
}


