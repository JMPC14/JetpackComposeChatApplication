package com.example.jetpackcomposechatapplication.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage

class LatestMessagesViewModel: ViewModel() {
    var latestMessages = MutableLiveData<List<ChatMessage>>()
}