package com.example.jetpackcomposechatapplication.main.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.database.*

class ChatViewModel: ViewModel() {
    var messages = MutableLiveData<List<ChatMessage>>()
    var tempUser: User? = null

    init {
        messages.value = mutableListOf()
    }

    fun addMessage(message: ChatMessage) {
        val messagesCopy = messages.value?.toMutableList()
        messagesCopy?.add(message)
        messages.value = messagesCopy!!
    }

    fun listenForMessages(user: String, otherUser: String) {
        var cid: String
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$user/$otherUser/cid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cid = snapshot.value.toString()

                val newRef = FirebaseDatabase.getInstance().getReference("/conversations/$cid")
                newRef.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val message = snapshot.getValue(ChatMessage::class.java)
                        if (message != null) {
                            addMessage(message)
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onChildRemoved(snapshot: DataSnapshot) {}

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadUser(uid: String) {

    }
}