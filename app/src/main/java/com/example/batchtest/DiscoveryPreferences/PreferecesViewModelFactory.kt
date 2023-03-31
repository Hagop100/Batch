package com.example.batchtest.DiscoveryPreferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PreferencesViewModelFactory(private val gId: String): ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java))
        {
            return PreferencesViewModel(gId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}