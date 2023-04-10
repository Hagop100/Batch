package com.example.batchtest.MatchTab

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.batchtest.Group

class MatchTabViewModel: ViewModel() {
    private val primaryGroupVM: MutableLiveData<String?> = MutableLiveData()
    val groups: MutableLiveData<ArrayList<Group>?> = MutableLiveData(arrayListOf())
    val removeGroups: MutableLiveData<ArrayList<Group>?> = MutableLiveData(arrayListOf())
    val prevGroup: MutableLiveData<Group?> = MutableLiveData(null)
    val currGroup: MutableLiveData<Group?> = MutableLiveData(null)
    val undoState: MutableLiveData<Boolean> = MutableLiveData()
    val fetchState: MutableLiveData<Boolean> = MutableLiveData(false)
    val isAdapterSet: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        primaryGroupVM.value = null
    }

    fun setPrimaryGroup(newPrimaryGroup: String) {
        primaryGroupVM.value = newPrimaryGroup
    }

    fun getPrimaryGroup(): MutableLiveData<String?> {
        return primaryGroupVM
    }
}