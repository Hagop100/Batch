package com.example.batchtest.DiscoveryPreferences

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batchtest.Group
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class PreferencesViewModel(gId: String): ViewModel() {

    companion object
    {
        const val LATITUDE: String = "latitude"
        const val LONGITUDE: String = "longitude"
        const val MIN_AGE: String = "minimumAge"
        const val MAX_AGE: String = "maxAge"
        const val MAX_DISTANCE: String = "maxDistance"
    }

    val minimumAge = MutableLiveData<Int>()
    val maxAge = MutableLiveData<Int>()
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var distance = MutableLiveData<Int>()
    var groupId: String = ""
    var dBreakers = ArrayList<String>()
    val database = FirebaseFirestore.getInstance()

    //Use for check whether the group already has variables initialized
    var isDealBreakers: Boolean = false
    var isPrefs: Boolean = false
    //used for updating the groups discovery preferences
    val preferencesHash = HashMap<String, Any>()
    private lateinit var group: Group


    init {
        groupId = gId

        database.collection("groups").document(groupId as String).get().addOnSuccessListener { document->

            Log.i("Preference", document.get("name").toString())
            group = document!!.toObject(Group::class.java)!!
            if (group.dealBreakers != null )
            {
                dBreakers = group.dealBreakers as ArrayList<String>
                isDealBreakers = true
            }
            if(group.discoverPrefs != null)
            {
                minimumAge.value = group.discoverPrefs?.get(MIN_AGE) as Int
                maxAge.value = group.discoverPrefs?.get(MAX_AGE) as Int
                latitude = group.discoverPrefs?.get(LATITUDE) as Double
                longitude = group.discoverPrefs?.get(LONGITUDE) as Double
                distance.value = group.discoverPrefs?.get(MAX_DISTANCE) as Int
                isPrefs = true
            }

        }. addOnFailureListener {
            Log.i("print", "error getting user from documents: ", it)
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("PreferencesViewModel","PreferenceView Model Destroyed")
    }


}