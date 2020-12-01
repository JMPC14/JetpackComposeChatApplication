package com.example.jetpackcomposechatapplication.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.User

class UserViewModel: ViewModel() {
    var user = MutableLiveData<User>()
}