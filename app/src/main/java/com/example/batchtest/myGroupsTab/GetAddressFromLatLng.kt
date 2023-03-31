package com.example.batchtest.myGroupsTab

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import java.util.*

class GetAddressFromLatLng(context: Context, private val latitude: Double, private val longitude: Double) {

    // Geocoder converts the latitude and longitude into a readable address
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    fun getAddress():String
    {
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude,longitude, 1, )

            if(addressList != null && addressList.isNotEmpty())
            {
                val address = addressList[0].locality
                return address.toString()
            }
        }catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
        return "error"
    }

}