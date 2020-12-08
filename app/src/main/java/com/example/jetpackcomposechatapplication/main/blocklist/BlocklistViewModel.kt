package com.example.jetpackcomposechatapplication.main.blocklist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlocklistViewModel : ViewModel() {
    var blocklist = MutableLiveData<List<User>>()

    init {
        blocklist.value = mutableListOf()
    }

    fun readBlockedUser(user: User) {
        val mutableContacts = blocklist.value?.toMutableList()
        mutableContacts?.add(user)
        blocklist.value = mutableContacts!!
    }

    fun addBlockedUser(user: User) {
        val mutableContacts = blocklist.value?.toMutableList()
        if (!mutableContacts!!.contains(user)) {
            mutableContacts.add(user)
            blocklist.value = mutableContacts.toList()
            val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/blocklist")
            val uidList = mutableListOf<String>()
            blocklist.value!!.forEach {
                uidList.add(it.uid)
            }
            ref.setValue(uidList)
        }
    }

    fun removeBlockedUser(user: User) {
        val mutableContacts = blocklist.value?.toMutableList()
        if (mutableContacts!!.contains(user)) {
            mutableContacts.remove(user)
            blocklist.value = mutableContacts.toList()
            val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/blocklist")
            val uidList = mutableListOf<String>()
            blocklist.value!!.forEach {
                uidList.add(it.uid)
            }
            ref.setValue(uidList)
        }
    }

    fun fetchBlocklist() {
        blocklist.value = mutableListOf()
        val uid = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().getReference("/users/$uid/blocklist")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        p0.children.forEach {
                            val newRef = FirebaseDatabase.getInstance().getReference("/users/${it.value.toString()}")
                            newRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val user = snapshot.getValue(User::class.java)
                                    if (user != null) {
                                        readBlockedUser(user)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    }
                })
    }
}