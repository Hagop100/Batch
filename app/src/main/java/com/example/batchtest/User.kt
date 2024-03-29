package com.example.batchtest

 import android.icu.number.NumberFormatter.DecimalSeparatorDisplay
 import android.net.Uri

// User data class
// @param firstName - first name of user
// @param lastName - last name of user
// @param email - email of user
// @param password - password of user
data class User(
    val firstName: String? = null,
    val lastName: String? = null,
    var email: String? = null,
    val displayName : String = "",
    val gender: String = "N/A",
    val imageUrl: String? = null,
    val imageUri: String? = null,
    val birthdate: String ="",
    val personalBio: String = "",
    val phoneNumber: String? = null,
    val MFA_Opt: String = "",
    val myGroups: ArrayList<String> = ArrayList(),
    val matchedGroups: ArrayList<String> = ArrayList(),
    val notificationPrefs: HashMap<String, Boolean>? = null,
    val profileComplete: Boolean = false,
    val userToken: String? = null,
    val primaryGroup: String? = null,
    val mutedGroups: ArrayList<String> = ArrayList(),
    val blockedGroups: ArrayList<String> = ArrayList(),
) {

    // returns full name of user
    fun getName(): String {
        return "$firstName $lastName"
    }
}


