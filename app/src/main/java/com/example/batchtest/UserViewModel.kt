package com.example.batchtest

import androidx.lifecycle.ViewModel

class UserViewModel: ViewModel() {
    val users = mutableListOf<User>();

    init {
        users += User("steven nguyen");
    }
}