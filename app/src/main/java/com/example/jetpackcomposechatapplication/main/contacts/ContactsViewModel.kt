package com.example.jetpackcomposechatapplication.main.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ContactsViewModel : ViewModel() {
    var contacts = MutableLiveData<List<User>>()

    init {
        contacts.value = mutableListOf()
    }

    fun addContact(user: User) {
        val mutableContacts = contacts.value?.toMutableList()
        mutableContacts?.add(user)
        contacts.value = mutableContacts!!
    }

    fun fetchContacts() {
        contacts.value = mutableListOf()
        val uid = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().getReference("/users/$uid/contacts")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        p0.children.forEach {
                            val newRef = FirebaseDatabase.getInstance().getReference("/users/${it.value.toString()}")
                            newRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val user = snapshot.getValue(User::class.java)
                                    if (user != null) {
                                        addContact(user)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    }
                })
    }
}