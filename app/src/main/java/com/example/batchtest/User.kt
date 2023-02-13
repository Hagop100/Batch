package com.example.batchtest

Addimport android.icu.number.NumberFormatter.DecimalSeparatorDisplay

// User data class
// @param firstName - first name of user
// @param lastName - last name of user
// @param email - email of user
// @param password - password of user
data class User(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val displayName : String = "",
    val gender: String = "N/A",
    val imageUrl: String? = null,
    val birthdate: String ="",
    val personalBio: String = "",

    // password ???
) {
    // returns full name of user
    fun getName(): String {
        return "$firstName $lastName"
    }
}


