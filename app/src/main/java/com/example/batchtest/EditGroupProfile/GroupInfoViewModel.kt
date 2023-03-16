package com.example.batchtest.EditGroupProfile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * share data between fragments
 */
class GroupInfoViewModel: ViewModel() {
    //share data between fragments - using group name
    val groupName: MutableLiveData<String> = MutableLiveData()
    val groupDesc: MutableLiveData<String> = MutableLiveData()

    fun getGName(): MutableLiveData<String> {
        return groupName
    }
    fun getGDesc():MutableLiveData<String>{
        return groupDesc
    }
}
