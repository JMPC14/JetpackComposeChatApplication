package com.example.jetpackcomposechatapplication.main.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.jetpackcomposechatapplication.R
import com.example.jetpackcomposechatapplication.main.UserViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.google.firebase.database.*
import dev.chrisbanes.accompanist.picasso.PicassoImage

class ChatFragment : Fragment() {

    lateinit var chatViewModel: ChatViewModel
    lateinit var userViewModel: UserViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Chat()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        chatViewModel.messages.value = listOf()

        if (chatViewModel.tempUser != null) {
            chatViewModel.listenForMessages(userViewModel.user.value?.uid!!, chatViewModel.tempUser!!.uid)
        }
    }

    @Composable
    fun Chat() {
        Column {
            Box(alignment = Alignment.BottomCenter) {
//                val messages by chatViewModel.messages.observeAsState()
//                if (messages != null) {
//                    LazyColumnFor(items = messages!!, modifier = Modifier.fillMaxSize()) { chatMessage ->
//                        if (chatMessage.fromId == userViewModel.user.value?.uid) {
//                            val modifier = if (chatViewModel.messages.value?.last() == chatMessage) {
//                                Modifier.padding(bottom = 55.dp)
//                            } else {
//                                Modifier.padding(0.dp)
//                            }
//                            ChatMessageFromItem(chatMessage, modifier)
//                        } else {
//                            val modifier = if (chatViewModel.messages.value?.last() == chatMessage) {
//                                Modifier.padding(bottom = 55.dp)
//                            } else {
//                                Modifier.padding(0.dp)
//                            }
//                            ChatMessageToItem(chatMessage, modifier)
//                        }
//                    }
//                }

                val scrollState = rememberScrollState()
                ScrollableColumn(
                        modifier = Modifier.fillMaxSize(),
                        scrollState = scrollState,
                        reverseScrollDirection = true
                ) {
                    val messages by chatViewModel.messages.observeAsState()
                    if (messages != null) {
                        messages?.forEach {
                            if (it.fromId == userViewModel.user.value?.uid) {
                                val modifier = if (chatViewModel.messages.value?.last() == it) {
                                    Modifier.padding(bottom = 55.dp)
                                } else {
                                    Modifier.padding(0.dp)
                                }
                                ChatMessageFromItem(it, modifier)
                            } else {
                                val modifier = if (chatViewModel.messages.value?.last() == it) {
                                    Modifier.padding(bottom = 55.dp)
                                } else {
                                    Modifier.padding(0.dp)
                                }
                                ChatMessageToItem(it, modifier)
                            }
                        }

                        scrollState.scrollTo(0f)
                    }
                }

                ChatSendMessageArea()
            }
        }
    }

    @Composable
    fun ChatSendMessageArea() {
        Row(
                modifier = Modifier.height(50.dp)
                        .then(
                                Modifier.background(
                                        VerticalGradient(
                                                listOf(Color.White, Color.Transparent),
                                                startY = 100f,
                                                endY = 0f
                                        )
                                )
                        )
                        .then(Modifier.background(Color(resources.getColor(R.color.half_white, null)))),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                    asset = vectorResource(id = R.drawable.ic_baseline_photo_camera_green_24),
                    modifier = Modifier.height(40.dp)
                            .then(Modifier.padding(start = 2.dp))
            )

            Image(
                    asset = vectorResource(R.drawable.ic_baseline_folder_open_green_24),
                    modifier = Modifier.height(40.dp)
                            .then(Modifier.padding(start = 2.dp, end = 2.dp))
            )

            val sendMessageState = remember { mutableStateOf(TextFieldValue()) }

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
                        onValueChange = { sendMessageState.value = it },
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

            },
                    modifier = Modifier.padding(end = 5.dp),
                    contentPadding = PaddingValues(0.dp)
            ) {
                Text("Send")
            }
        }
    }

    @Composable
    fun ChatMessageFromItem(message: ChatMessage, modifier: Modifier) {
        Row(modifier = modifier) {
            Spacer(modifier = Modifier.weight(1f))

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                            .then(Modifier.background(Color(resources.getColor(R.color.default_green, null)), RoundedCornerShape(8.dp)))
                            .then(Modifier.padding(5.dp))
                            .then(Modifier.preferredWidthIn(max = 250.dp))
            ) {
                Text(message.text)
            }

            if (userViewModel.user.value?.profileImageUrl != null) {
                Column {
                    PicassoImage(
                            data = userViewModel.user.value?.profileImageUrl!!,
                            modifier = Modifier.padding(top = 5.dp, end = 5.dp)
                                    .then(Modifier.background(Color.Black, CircleShape))
                                    .then(Modifier.border(1.5.dp, Color.Black, CircleShape))
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

    @Composable
    fun ChatMessageToItem(message: ChatMessage, modifier: Modifier) {
        Row(modifier = modifier) {
            if (chatViewModel.tempUser != null) {
                Column {
                    PicassoImage(
                            data = chatViewModel.tempUser!!.profileImageUrl,
                            modifier = Modifier.padding(top = 5.dp, start = 5.dp)
                                    .then(Modifier.background(Color.Black, CircleShape))
                                    .then(Modifier.border(1.5.dp, Color.Black, CircleShape))
                                    .then(Modifier.drawShadow(4.dp, CircleShape))
                                    .then(
                                            Modifier.size(35.dp)
                                                    .clip(CircleShape)
                                    ),
                            contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                            .then(Modifier.background(Color.LightGray, RoundedCornerShape(8.dp)))
                            .then(Modifier.padding(5.dp))
                            .then(Modifier.preferredWidthIn(max = 250.dp))
            ) {
                Text(message.text)
            }
        }
    }
}