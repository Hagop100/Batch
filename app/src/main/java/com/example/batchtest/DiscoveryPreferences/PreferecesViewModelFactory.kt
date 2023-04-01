package com.example.batchtest.DiscoveryPreferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory Class Used for instantiating the Preference view Model
 * */
class PreferencesViewModelFactory(private val gName: String): ViewModelProvider.Factory {

    /**
     * Returns a view model with the group name passed in from the bundle.
     * */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java))
        {
            return PreferencesViewModel(gName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}