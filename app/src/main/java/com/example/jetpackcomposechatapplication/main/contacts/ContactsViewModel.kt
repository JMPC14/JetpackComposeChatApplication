package com.example.jetpackcomposechatapplication.main.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.User

class ContactsViewModel: ViewModel() {
    var contacts = MutableLiveData<List<User>>()

    init {
        contacts.value = mutableListOf()
    }

    fun addContact(user: User) {
        val mutableContacts = contacts.value?.toMutableList()
        mutableContacts?.add(user)
        contacts.value = mutableContacts!!
    }
}