package com.example.jetpackcomposechatapplication.main.latestmessages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.main.blocklist.BlocklistViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LatestMessagesViewModel: ViewModel() {
    val latestMessages = MutableLiveData<HashMap<User, ChatMessage>>()

    init {
        latestMessages.value = HashMap()
    }

    private fun refreshRecyclerViewMessages() {
        val map = HashMap<User, ChatMessage>()
        latestMessages.value!!.toList().sortedByDescending { it.second.time }.toMap(map)
        latestMessages.value = map
    }

    fun listenForLatestMessages(blocklistViewModel: BlocklistViewModel) {
        fun handleSnapshot(snapshot: DataSnapshot) {
            val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

            if (blocklistViewModel.blocklist.value != null && blocklistViewModel.blocklist.value!!.isNotEmpty() ) {
                blocklistViewModel.blocklist.value?.forEach {
                    if (chatMessage.fromId == it.uid) {
                        return
                    }
                }
            }

            fetchUserForMessage(snapshot.key!!, chatMessage)
        }

        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                handleSnapshot(p0)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                handleSnapshot(p0)
            }

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
                    latestMessages.value!![user!!] = chatMessage
                    refreshRecyclerViewMessages()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}