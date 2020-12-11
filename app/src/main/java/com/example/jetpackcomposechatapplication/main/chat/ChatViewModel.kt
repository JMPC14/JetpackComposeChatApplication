package com.example.jetpackcomposechatapplication.main.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.jetpackcomposechatapplication.R
import com.example.jetpackcomposechatapplication.main.latestmessages.UserViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.FileAttachment
import com.example.jetpackcomposechatapplication.models.User
import com.example.jetpackcomposechatapplication.notifications.ApiClient.apiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.*

class ChatViewModel: ViewModel() {
    var messages = MutableLiveData<List<ChatMessage>>()

    var onlineUsers = MutableLiveData<List<String>>()

    var tempWriting = MutableLiveData<String>()
    var otherUserTyping = MutableLiveData<Boolean>()

    var tempUser: User? = null
    var tempCid: String? = null

    var photoAttachmentUri: Uri? = null
    var fileAttachmentUri: Uri? = null

    var imageUrl: String? = null
    var fileAttachment: FileAttachment? = null

    init {
        messages.value = mutableListOf()
        onlineUsers.value = mutableListOf()
        tempWriting.value = ""
    }

    fun addMessage(message: ChatMessage) {
        val messagesCopy = messages.value?.toMutableList()
        messagesCopy?.add(message)
        messages.value = messagesCopy!!
    }

    fun listenForMessages(user: String, otherUser: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$user/$otherUser/cid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tempCid = snapshot.value.toString()

                val newRef = FirebaseDatabase.getInstance().getReference("/conversations/$tempCid")
                newRef.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val message = snapshot.getValue(ChatMessage::class.java)
                        if (message != null) {
                            if (messages.value!!.isNotEmpty()) {
                                if (messages.value!!.last().time != message.time) {
                                    addMessage(message)
                                    return
                                } else {
                                    return
                                }
                            }
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

    fun listenForTypingIndicator(user: String, otherUser: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$user/$otherUser/")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typing = snapshot.child("typing")
                if (typing.exists()) {
                    otherUserTyping.value = typing.value == true
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addOnlineUser(string: String) {
        val users = onlineUsers.value?.toMutableList()
        users?.add(string)
        onlineUsers.value = users!!
    }

    private fun removeOnlineUser(string: String) {
        val users = onlineUsers.value?.toMutableList()
        users?.remove(string)
        onlineUsers.value = users!!
    }

    fun checkOnlineUser(snapshot: DataSnapshot) {
        if (snapshot.value == true) {
            addOnlineUser(snapshot.key!!)
        } else if (snapshot.value == false && onlineUsers.value!!.contains(snapshot.key!!)) {
            removeOnlineUser(snapshot.key!!)
        }
    }

    fun listenForOnlineUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/online-users/")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                checkOnlineUser(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                checkOnlineUser(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun uploadImage(from: String) {
        if (photoAttachmentUri == null) {
            return
        }
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(photoAttachmentUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        imageUrl = it.toString()
                        performSendMessage(from)
                    }
                }
    }

    fun uploadFile(from: String) {
        if (fileAttachmentUri == null) { return }
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/files/$filename")
        ref.putFile(fileAttachmentUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { it2 ->
                val attachedFileSize = "%.2f".format(it.metadata!!.sizeBytes.toDouble() / 1000).toDouble()
                fileAttachment = FileAttachment(it.metadata?.contentType!!, attachedFileSize, it2.toString())
                performSendMessage(from)
            }
        }
    }

    fun performSendMessage(from: String) {
        val text = tempWriting.value!!

        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = tempUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("/conversations/${tempCid}").push()
        val chatMessage: ChatMessage?
        val time = System.currentTimeMillis() / 1000
        val month = LocalDateTime.now().month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
        val date = LocalDateTime.now().dayOfMonth
        val hour = LocalDateTime.now().hour
        val minute = LocalDateTime.now().minute
        val newHour = if (hour < 10) {
            "0$hour"
        } else {
            hour.toString()
        }
        val newMinute = if (minute < 10) {
            "0$minute"
        } else {
            minute.toString()
        }
//        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//        val timestring = dateFormat.format(messageData.timestamp)
        val timestamp = "$date $month, $newHour:$newMinute"

        if (text.isNotEmpty()) {

            chatMessage = ChatMessage(
                    ref.key!!,
                    text,
                    fromId,
                    toId,
                    timestamp,
                    time
            )

            if (imageUrl != null) {
                chatMessage.imageUrl = imageUrl
                imageUrl = null
                photoAttachmentUri = null
            }

            if (fileAttachment != null) {
                with (fileAttachment!!) {
                    chatMessage.fileSize = fileSize
                    chatMessage.fileType = fileType
                    chatMessage.fileUrl = fileUrl
                }

                fileAttachment = null
                fileAttachmentUri = null
            }

            ref.setValue(chatMessage)
                    .addOnSuccessListener {
                        tempWriting.value = ""
                    }

            val latestMessageRef =
                    FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
            latestMessageRef.setValue(chatMessage)

            val latestMessageToRef =
                    FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
            latestMessageToRef.setValue(chatMessage)

            val payload = buildNotificationPayload(chatMessage.text, from)
            apiService.sendNotification(payload)!!.enqueue(
                    object : Callback<JsonObject?> {
                        override fun onResponse(
                                call: Call<JsonObject?>?,
                                response: Response<JsonObject?>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("TAG", "Notification sent.")
                            }
                        }

                        override fun onFailure(
                                call: Call<JsonObject?>?,
                                t: Throwable?
                        ) {}
                    })
        }
    }

    private fun buildNotificationPayload(text: String, from: String): JsonObject {
        val payload = JsonObject()
        payload.addProperty("to", tempUser?.token)
        val data = JsonObject()
//        data.addProperty("title", FirebaseAuth.getInstance().uid)
//        data.addProperty("body", FirebaseManager.messageKey)
        data.addProperty("title", from)
        data.addProperty("body", text)
        payload.add("notification", data)
        Log.d("TAG", payload.toString())
        return payload
    }

    fun newConversation(user: User, userViewModel: UserViewModel, callback: () -> Unit) {
        fun moveOn() {
            tempUser = user
            callback()
        }

        val cid = UUID.randomUUID().toString()
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/${userViewModel.user.value?.uid}/${user.uid}")
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/${user.uid}/${userViewModel.user.value?.uid}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.child("cid").exists()) {
                    ref.child("cid").setValue(cid)
                    toRef.child("cid").setValue(cid)
                            .addOnSuccessListener {
                                moveOn()
                            }
                } else {
                    moveOn()
                }
            }
        })
    }
}