package com.example.batchtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*

class UserViewModel: ViewModel() {
    val users = mutableListOf<User>();

    init {
        viewModelScope.launch {
            users += loadUsers()
        }
    }

    suspend fun loadUsers(): List<User> {
        val result = mutableListOf<User>()
        for (i in 0 until 100) {
            val user = User(
                id = UUID.randomUUID(),
                name = "$i"
            )

            result += user
        }
        return result
    }
}