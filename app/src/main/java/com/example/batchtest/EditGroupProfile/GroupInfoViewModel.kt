package com.example.batchtest.EditGroupProfile

import android.app.Application
import android.content.Context
import android.text.Editable
import android.widget.Toast
import androidx.lifecycle.*

import kotlinx.coroutines.launch


/**
 * share data between fragments
 */
class GroupInfoViewModel: ViewModel() {
    //share data between fragments - using group name
    val groupName: MutableLiveData<String> = MutableLiveData()
    val groupDesc: MutableLiveData<String> = MutableLiveData()
    val groupTags: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val groupPic: MutableLiveData<String> = MutableLiveData()


    init{
        groupTags.value = ArrayList()
    }


    fun setGName(text: String){
        groupName.value = text
    }

    fun getGName(): MutableLiveData<String> {
        return groupName
    }

    fun setGDesc(text: Editable){
        groupDesc.value = text.toString()
    }
    fun getGDesc(): MutableLiveData<String> {
        return groupDesc
    }

    //setter
    fun updateTags(newTags: ArrayList<String>){
        groupTags.value = newTags
    }

    fun setGroupPicture(pic: String){
        groupPic.value = pic
    }
    fun getGroupPicture(): MutableLiveData<String> {
        return groupPic
    }
}
