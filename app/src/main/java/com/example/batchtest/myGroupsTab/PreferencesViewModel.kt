package com.example.batchtest.myGroupsTab

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

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

    fun getAddressFromLatLng(){
        viewModelScope.launch {

        }
    }
    override fun onCleared() {
        super.onCleared()
        Log.i("PreferencesViewModel","PreferenceView Model Destroyed")
    }

    interface AddressListener{
        fun onAddressFound(address: String?)
        fun onError()
    }
}