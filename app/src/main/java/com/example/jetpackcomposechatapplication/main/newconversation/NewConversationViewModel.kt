package com.example.jetpackcomposechatapplication.main.newconversation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.User

class NewConversationViewModel: ViewModel() {
    var contacts = MutableLiveData<List<User>>()
}