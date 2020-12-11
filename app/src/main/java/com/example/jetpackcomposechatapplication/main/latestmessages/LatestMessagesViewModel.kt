package com.example.jetpackcomposechatapplication.main.latestmessages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.HashMap

class LatestMessagesViewModel : ViewModel() {
    val latestMessages = MutableLiveData<HashMap<User, ChatMessage>>()
    val sortedMap = MutableLiveData<Map<User, ChatMessage>>()

    init {
        latestMessages.value = HashMap()
    }

    fun refreshRecyclerViewMessages() {
        sortedMap.value = HashMap()
        sortedMap.value = latestMessages.value!!.toList().sortedByDescending { it.second.time }.toMap()
    }

    fun listenForLatestMessages(blocklist: List<User>) {

        fun handleSnapshot(snapshot: DataSnapshot) {
            val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

            if (blocklist.isNotEmpty()) {
                blocklist.forEach {
                    if (chatMessage.fromId == it.uid || chatMessage.toId == it.uid) {
                        return
                    } else {
                        fetchUserForMessage(snapshot.key!!, chatMessage)
                    }
                }
            } else {
                fetchUserForMessage(snapshot.key!!, chatMessage)
            }
        }

        latestMessages.value = HashMap()
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) = handleSnapshot(p0)

            override fun onChildChanged(p0: DataSnapshot, p1: String?) = handleSnapshot(p0)

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun fetchUserForMessage(key: String, chatMessage: ChatMessage) {
        var user: User?
        val userRef = FirebaseDatabase.getInstance().getReference("users/$key")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
                if (user != null) {
                    var keyRemove: User? = null
                    latestMessages.value?.forEach {
                        if (it.key.uid == user!!.uid) {
                            keyRemove = it.key
                            return@forEach
                        }
                    }
                    if (keyRemove != null) {
                        latestMessages.value?.remove(keyRemove!!)
                    }
                    latestMessages.value!![user!!] = chatMessage
                    refreshRecyclerViewMessages()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}