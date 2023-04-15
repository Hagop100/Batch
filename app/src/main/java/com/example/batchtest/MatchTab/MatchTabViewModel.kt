package com.example.batchtest.MatchTab

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.batchtest.Group

class MatchTabViewModel: ViewModel() {
    private val primaryGroupVM: MutableLiveData<String?> = MutableLiveData()
    val primaryGroupObj: MutableLiveData<Group?> = MutableLiveData()
    val groups: MutableLiveData<ArrayList<Group>?> = MutableLiveData(arrayListOf())
    val removeGroups: MutableLiveData<ArrayList<Group>?> = MutableLiveData(arrayListOf())
    val matchedGroups: MutableLiveData<ArrayList<String>?> = MutableLiveData(arrayListOf())
    val prevGroup: MutableLiveData<Group?> = MutableLiveData(null)
    val currGroup: MutableLiveData<Group?> = MutableLiveData(null)
    val undoState: MutableLiveData<Boolean> = MutableLiveData()

    init {
        primaryGroupVM.value = null
    }

    fun setPrimaryGroup(newPrimaryGroup: String) {
        primaryGroupVM.value = newPrimaryGroup
    }

    fun getPrimaryGroup(): String {
        return primaryGroupVM.value.toString()
    }
}