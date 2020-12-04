package com.example.jetpackcomposechatapplication.main.latestmessages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class UserViewModel: ViewModel() {
    var user = MutableLiveData<User>()

    fun fetchCurrentUser(callback: (bool: Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            callback(false)
        } else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                FirebaseDatabase.getInstance().getReference("/users/$uid").child("token").setValue(it.result)
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    user.value = p0.getValue(User::class.java)
                    FirebaseDatabase.getInstance().getReference("/online-users/$uid").setValue(true)
                    callback(true)
                }
            })
        }
    }
}