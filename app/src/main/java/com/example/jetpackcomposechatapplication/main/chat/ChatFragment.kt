package com.example.jetpackcomposechatapplication.main.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.draw.drawShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.VerticalGradient
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.jetpackcomposechatapplication.R
import com.example.jetpackcomposechatapplication.main.blocklist.BlocklistViewModel
import com.example.jetpackcomposechatapplication.main.latestmessages.UserViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.FileAttachment
import dev.chrisbanes.accompanist.picasso.PicassoImage
import kotlinx.coroutines.*
import java.util.*

class ChatFragment : Fragment() {

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var blocklistViewModel: BlocklistViewModel

    private var presentDialog = mutableStateOf(false)

    private var sendMessageState = mutableStateOf(TextFieldValue())

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Box(alignment = Alignment.Center) {
                    Chat()

                    if (presentDialog.value) {
                        UserDialog()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        blocklistViewModel = ViewModelProvider(requireActivity()).get(BlocklistViewModel::class.java)

        with (chatViewModel) {
            messages.value = listOf()

            if (tempUser != null) {
                listenForMessages(userViewModel.user.value?.uid!!, chatViewModel.tempUser!!.uid)
                listenForTypingIndicator(userViewModel.user.value?.uid!!, chatViewModel.tempUser!!.uid)
            }

            tempWriting.observe(viewLifecycleOwner, {
                if (it != "") {
                    sendMessageState = mutableStateOf(TextFieldValue(it))
                }
            })
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            chatViewModel.photoAttachmentUri = data.data
        }

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            chatViewModel.fileAttachmentUri = data.data
        }
    }

    var previousMessage: String? = null
    var compareTime: Long = 0

    @Composable
    fun Chat() {
        Column {
            Box(alignment = Alignment.BottomCenter) {
                val scrollState = rememberScrollState()
                ScrollableColumn(
                        modifier = Modifier.fillMaxSize(),
                        scrollState = scrollState,
                        reverseScrollDirection = true
                ) {
                    val messages by chatViewModel.messages.observeAsState()
                    if (messages != null) {
                        messages?.forEach {
                            var consecutive = false
                            if (it.fromId == previousMessage) {
                                consecutive = true
                            }
                            var showTime = false
                            if ((it.time - compareTime) > 300) {
                                showTime = true
                            }
                            if (it.fromId == userViewModel.user.value?.uid) {
                                var modifier = if (chatViewModel.messages.value?.last() == it) {
                                    Modifier.padding(bottom = 70.dp)
                                } else {
                                    Modifier.padding(0.dp)
                                }
                                if (consecutive && !showTime) {
                                    modifier = modifier.then(Modifier.padding(end = 42.dp))
                                }
                                ChatMessageFromItem(it, modifier, showTime, consecutive)
                            } else {
                                var modifier = if (chatViewModel.messages.value?.last() == it) {
                                    Modifier.padding(bottom = 70.dp)
                                } else {
                                    Modifier.padding(0.dp)
                                }
                                if (consecutive && !showTime) {
                                    modifier = modifier.then(Modifier.padding(start = 42.dp))
                                }
                                ChatMessageToItem(it, modifier, showTime, consecutive)
                            }
                            previousMessage = it.fromId
                            compareTime = it.time
                        }
                        scrollState.scrollTo(0f)
                    }
                }

                ChatSendMessageBar()
            }
        }
    }

    @Composable
    fun ChatSendMessageBar() {
        val otherUsingTyping by chatViewModel.otherUserTyping.observeAsState()
        var modifier = Modifier.height(50.dp)
        if (otherUsingTyping != null && otherUsingTyping!!) {
            modifier = Modifier.height(68.dp)
        }
        Column(
                modifier = modifier
                        .then(
                                Modifier.background(
                                        VerticalGradient(
                                                listOf(Color.White, Color.Transparent),
                                                startY = 100f,
                                                endY = 0f
                                        )
                                )
                        )
                        .then(Modifier.background(Color(resources.getColor(R.color.half_white, null))))
        ) {
            if (otherUsingTyping != null && otherUsingTyping!!) {
                Text(
                        text = "${chatViewModel.tempUser!!.username} is typing...",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.Bold
                )
            }

            Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxHeight()
                            .then(Modifier.padding(bottom = 4.dp))
            ) {
                Image(
                        asset = vectorResource(id = R.drawable.ic_baseline_photo_camera_green_24),
                        modifier = Modifier.size(35.dp)
                                .then(Modifier.padding(start = 8.dp, bottom = 7.dp))
                                .then(Modifier.clickable(onClick = {
                                    if (chatViewModel.photoAttachmentUri != null) {
                                        Toast.makeText(requireContext(), "Image already attached.", Toast.LENGTH_LONG).show()
                                        return@clickable
                                    }
                                    val intent = Intent(Intent.ACTION_PICK)
                                    intent.type = "image/*"
                                    startActivityForResult(intent, 0)
                                }))
                )

                Image(
                        asset = vectorResource(R.drawable.ic_baseline_folder_open_green_24),
                        modifier = Modifier.size(42.dp)
                                .then(Modifier.padding(start = 8.dp, end = 8.dp))
                                .then(Modifier.clickable(onClick = {
                                    if (chatViewModel.fileAttachmentUri != null) {
                                        Toast.makeText(requireContext(), "File already attached.", Toast.LENGTH_LONG).show()
                                        return@clickable
                                    }
                                    val intent = Intent(Intent.ACTION_PICK)
                                    intent.type = "*/*"
                                    startActivityForResult(intent, 1)
                                }))
                )

                sendMessageState = remember { mutableStateOf(TextFieldValue(chatViewModel.tempWriting.value!!)) }

                val sendMessageModifier = Modifier.border(
                        width = 1.5.dp,
                        color = if (sendMessageState.value.text != "") Color(
                                resources.getColor(
                                        R.color.default_green,
                                        null
                                )
                        ) else Color.Gray,
                        shape = RoundedCornerShape(20)
                )
                        .then(Modifier.padding(6.dp))
                        .then(Modifier.height(20.dp))
                        .then(Modifier.fillMaxWidth())

                Box(
                        modifier = Modifier.padding(end = 5.dp, bottom = 5.dp, top = 5.dp)
                                .then(Modifier.weight(1f)),
                        alignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                            value = sendMessageState.value,
                            onValueChange = {
                                sendMessageState.value = it
                                chatViewModel.tempWriting.value = it.text
                            },
                            modifier = sendMessageModifier,
                            cursorColor = Color.Black,
                            textStyle = TextStyle(color = Color.Black)
                    )

                    Text(
                            "Write something here...",
                            modifier = Modifier.padding(start = 6.dp)
                                    .then(Modifier.drawOpacity(if (sendMessageState.value.text == "") 0.7f else 0f)),
                            color = (Color.Gray)
                    )
                }

                TextButton(onClick = {
                    with(chatViewModel) {
                        when {
                            photoAttachmentUri != null -> {
                                uploadImage(userViewModel.user.value!!.username)
                            }

                            fileAttachmentUri != null -> {
                                uploadFile(userViewModel.user.value!!.username)
                            }

                            else -> {
                                performSendMessage(userViewModel.user.value!!.username)
                            }
                        }
                    }
                    sendMessageState.value = TextFieldValue()
                },
                        modifier = Modifier.offset(x = (-4).dp, y = (-4).dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonConstants.defaultButtonColors(
                                backgroundColor = Color.Transparent,
                                contentColor = Color(resources.getColor(R.color.default_green, null))
                        )
                ) {
                    Text("Send")
                }
            }
        }
    }

    @Composable
    fun ChatMessageFromItem(message: ChatMessage, modifier: Modifier, showTime: Boolean, consecutive: Boolean = false) {
        if (showTime) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(message.timestamp)
            }
        }

        Column {
            Row(modifier = modifier) {
                Spacer(modifier = Modifier.weight(1f))

                Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(if (consecutive && !showTime) 2.dp else 4.dp)
                                .then(Modifier.background(Color(resources.getColor(R.color.default_green, null)), RoundedCornerShape(8.dp)))
                                .then(Modifier.padding(5.dp))
                                .then(Modifier.preferredWidthIn(max = 250.dp))
                ) {
                    if (message.text.isNotEmpty()) {
                        Row {
                            Text(message.text)
                        }
                    }

                    with (message) {
                        if (imageUrl != null) {
                            ChatImage(imageUrl!!)
                        }

                        if (fileUrl != null && fileSize != null && fileType != null) {
                            val file = FileAttachment(fileType!!, fileSize!!, fileUrl!!)
                            ChatFile(file)
                        }
                    }
                }

                if (userViewModel.user.value?.profileImageUrl != null && (!consecutive || showTime)) {
                    Column {
                        PicassoImage(
                                data = userViewModel.user.value?.profileImageUrl!!,
                                modifier = Modifier.padding(top = 5.dp, end = 5.dp)
                                        .then(Modifier.border(1.5.dp, Color(resources.getColor(R.color.default_green, null)), CircleShape))
                                        .then(Modifier.drawShadow(4.dp, CircleShape))
                                        .then(
                                                Modifier.size(35.dp)
                                                        .clip(CircleShape)
                                        ),
                                contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ChatMessageToItem(message: ChatMessage, modifier: Modifier, showTime: Boolean, consecutive: Boolean = false) {
        if (showTime) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(message.timestamp)
            }
        }

        Column {
            Row(modifier = modifier) {
                if (chatViewModel.tempUser != null && (!consecutive || showTime)) {
                    Column {
                        val color = if (chatViewModel.onlineUsers.value!!.contains(message.fromId)) Color(resources.getColor(R.color.default_green, null)) else Color.Black

                        PicassoImage(
                                data = chatViewModel.tempUser!!.profileImageUrl,
                                modifier = Modifier.padding(top = 5.dp, start = 5.dp)
                                        .then(Modifier.border(1.5.dp, color, CircleShape))
                                        .then(Modifier.drawShadow(4.dp, CircleShape))
                                        .then(
                                                Modifier.size(35.dp)
                                                        .clip(CircleShape)
                                        )
                                        .then(Modifier.clickable(onClick = {
                                            presentDialog.value = true
                                        })),
                                contentScale = ContentScale.Crop
                        )
                    }
                }

                Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(if (consecutive && !showTime) 2.dp else 4.dp)
                                .then(Modifier.background(Color.LightGray, RoundedCornerShape(8.dp)))
                                .then(Modifier.padding(5.dp))
                                .then(Modifier.preferredWidthIn(max = 250.dp))
                ) {
                    if (message.text.isNotEmpty()) {
                        Row {
                            Text(message.text)
                        }
                    }

                    if (message.imageUrl != null) {
                        ChatImage(message.imageUrl!!)
                    }
                }
            }
        }
    }

    @Composable
    fun ChatImage(imageUrl: String) {
        PicassoImage(
                data = imageUrl,
                modifier = Modifier.padding(top = 4.dp)
                        .then(Modifier.fillMaxSize())
                        .then(Modifier.clip(RoundedCornerShape(8.dp)))
                        .then(Modifier.clickable(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
                            startActivity(intent)
                        })),
                contentScale = ContentScale.FillWidth
        )
    }

    @Composable
    fun ChatFile(file: FileAttachment) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
                        .then(Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)))
                        .then(Modifier.background(Color.White, RoundedCornerShape(8.dp)))
                        .then(Modifier.padding(5.dp))
                        .then(Modifier.preferredWidthIn(max = 250.dp))
        ) {
            Text(file.fileType, fontWeight = FontWeight.Bold)

            Image(
                    asset = vectorResource(id = R.drawable.ic_baseline_insert_drive_file_24),
                    modifier = Modifier.size(50.dp)
            )

            if (file.fileSize > 1000) {
                Text("${file.fileSize.div(1000)}mB")
            } else {
                Text("${file.fileSize}kB")
            }
        }
    }

    @Composable
    fun UserDialog() {
        Column(
                modifier = Modifier.fillMaxWidth(0.7f)
                        .then(Modifier.background(Color.White, RoundedCornerShape(10.dp)))
                        .then(Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(10.dp)))
                        .then(Modifier.padding(15.dp))
        ) {
            if (chatViewModel.tempUser != null) {
                val color = if (chatViewModel.onlineUsers.value!!.contains(chatViewModel.tempUser?.uid)) Color(resources.getColor(R.color.default_green, null)) else Color.Black

                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 30.dp)
                ) {
                    PicassoImage(
                            data = chatViewModel.tempUser!!.profileImageUrl,
                            modifier = Modifier.padding(end = 10.dp)
                                    .then(Modifier.border(1.5.dp, color, CircleShape))
                                    .then(Modifier.drawShadow(4.dp, CircleShape))
                                    .then(
                                            Modifier.size(50.dp)
                                                    .clip(CircleShape)
                                    )
                                    .then(Modifier.clickable(onClick = {
                                        presentDialog.value = true
                                    })),
                            contentScale = ContentScale.Crop
                    )

                    Text(
                            chatViewModel.tempUser!!.username,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = {
                        presentDialog.value = false
                    }) {
                        Text("Close")
                    }
                }
            }

            Row {
                Button(onClick = { /*TODO*/ }) {
                    Text("View Profile")
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(onClick = {
                    if (chatViewModel.tempUser != null) {
                        blocklistViewModel.addBlockedUser(chatViewModel.tempUser!!)
                        presentDialog.value = false
                        requireActivity().onBackPressed()
                    }
                }) {
                    Text("Block")
                }
            }
        }
    }
}