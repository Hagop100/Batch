package com.example.batchtest.myGroupsTab

import android.util.Log
import androidx.lifecycle.ViewModel

class PreferencesViewModel(): ViewModel() {

    val locationHashMap = HashMap<String, Any>()
    var minimumAge: Int = 18
    var maxAge: Int = 100
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var distance: Int = 0
    var groupId: String = ""

    init {

    }

    override fun onCleared() {
        super.onCleared()
        Log.i("PreferencesViewModel","PreferenceView Model Destroyed")
    }
}