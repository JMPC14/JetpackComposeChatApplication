package com.example.jetpackcomposechatapplication.main.blocklist

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlocklistViewModel: ViewModel() {
    var blocklist = MutableLiveData<List<User>>()

    init {
        blocklist.value = mutableListOf()
    }

    fun addBlockedUser(user: User) {
        val mutableContacts = blocklist.value?.toMutableList()
        mutableContacts?.add(user)
        blocklist.value = mutableContacts!!
    }

    fun fetchBlocklist() {
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
                                        addBlockedUser(user)
                                        Log.d("NEWTAG", "ADDING CONTACT $user")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }
                    }
                })
    }
}