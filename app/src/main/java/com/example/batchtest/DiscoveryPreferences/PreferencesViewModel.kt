package com.example.batchtest.DiscoveryPreferences

import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.example.batchtest.Group
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

/**
 * Preference View Model is used for storing the the values used in
 * Preference Fragment
 * */
class PreferencesViewModel(groupName: String): ViewModel() {


    companion object
    {
        const val LATITUDE: String = "latitude"
        const val LONGITUDE: String = "longitude"
        const val MIN_AGE: String = "minimumAge"
        const val MAX_AGE: String = "maxAge"
        const val MAX_DISTANCE: String = "maxDistance"
        const val GENDER: String = "gender"
        const val CITY: String = "city"
    }

    //Create Mutable Variables that allow the Fragment to observe changes
    val minimumAge = MutableLiveData<Double>()
    val maxAge = MutableLiveData<Double>()
    var latitude =  MutableLiveData<Double>()
    var longitude = MutableLiveData<Double>()
    var distance = MutableLiveData<Double>()
    var gName: String = ""
    val gender = MutableLiveData<String>()
    var city = MutableLiveData<String>()

    //used for updating the groups discovery preferences
    var preferencesHash = HashMap<String, Any>()
    lateinit var group: Group


    init {
        gName = groupName

    }

    override fun onCleared() {
        super.onCleared()
        Log.i("PreferencesViewModel","PreferenceView Model Destroyed")
    }


}