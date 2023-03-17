package com.example.batchtest.EditGroupProfile

import android.text.Editable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * share data between fragments
 */
class GroupInfoViewModel: ViewModel() {
    //share data between fragments - using group name
    val groupName: MutableLiveData<String> = MutableLiveData()
    val groupDesc: MutableLiveData<String> = MutableLiveData()
    val groupTags = MutableLiveData<ArrayList<*>>()
    val groupPic: MutableLiveData<String> = MutableLiveData<String>()


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

    fun setTags(tags: ArrayList<*>){
        groupTags.value = tags
    }
    fun getTags(): MutableLiveData<ArrayList<*>> {
        return groupTags
    }

    fun setGroupPicture(pic: String){
        groupPic.value = pic
    }
    fun getGroupPicture(): MutableLiveData<String> {
        return groupPic
    }
}
