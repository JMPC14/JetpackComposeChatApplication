package com.example.jetpackcomposechatapplication.main.chat

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.FileAttachment
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.util.*

class ChatViewModel: ViewModel() {
    var messages = MutableLiveData<List<ChatMessage>>()

    var tempWriting = MutableLiveData<String>()

    var tempUser: User? = null
    var tempCid: String? = null

    var photoAttachmentUri: Uri? = null
    var fileAttachmentUri: Uri? = null

    var imageUrl: String? = null
    var fileAttachment: FileAttachment? = null

    init {
        messages.value = mutableListOf()
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

    fun performSendMessage() {
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

//            val payload = buildNotificationPayload()
//            apiService.sendNotification(payload)!!.enqueue(
//                    object : Callback<JsonObject?> {
//                        override fun onResponse(
//                                call: Call<JsonObject?>?,
//                                response: Response<JsonObject?>
//                        ) {
//                            if (response.isSuccessful) {
//                                Log.d("TAG", "Notification sent.")
//                            }
//                        }
//
//                        override fun onFailure(
//                                call: Call<JsonObject?>?,
//                                t: Throwable?
//                        ) {}
//                    })
        }
    }
}