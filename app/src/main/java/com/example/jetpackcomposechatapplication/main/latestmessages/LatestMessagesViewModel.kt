package com.example.jetpackcomposechatapplication.main.latestmessages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.User

class LatestMessagesViewModel: ViewModel() {
    val latestMessages = MutableLiveData<HashMap<User, ChatMessage>>()

    init {
        latestMessages.value = HashMap()
    }
}