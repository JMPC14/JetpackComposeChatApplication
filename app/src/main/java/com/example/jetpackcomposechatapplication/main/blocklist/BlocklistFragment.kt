package com.example.jetpackcomposechatapplication.main.blocklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.jetpackcomposechatapplication.main.chat.ChatViewModel
import com.example.jetpackcomposechatapplication.models.User
import com.example.jetpackcomposechatapplication.ui.Border
import com.example.jetpackcomposechatapplication.ui.border
import dev.chrisbanes.accompanist.picasso.PicassoImage

class BlocklistFragment : Fragment() {

    private lateinit var blocklistViewModel: BlocklistViewModel
    private lateinit var chatViewModel: ChatViewModel

    private var presentDialog = mutableStateOf(false)
    private var selectedUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Box {
                    Blocklist()
                    if (presentDialog.value) {
                        UnblockDialog()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        blocklistViewModel = ViewModelProvider(requireActivity()).get(BlocklistViewModel::class.java)
        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)

        blocklistViewModel.fetchBlocklist()
    }

    @Composable
    fun Blocklist() {
        val blocklist by blocklistViewModel.blocklist.observeAsState()
        if (blocklist != null && blocklist!!.isNotEmpty()) {
            ScrollableColumn {
                blocklist?.forEach {
                    BlocklistItem(it)
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                        "You have no blocked users!",
                        modifier = Modifier.padding(10.dp),
                        fontSize = 16.sp
                )
            }
        }
    }

    @Composable
    fun BlocklistItem(user: User) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(75.dp)
                        .then(Modifier.border(bottom = Border(0.5.dp, Color.LightGray)))
                        .then(Modifier.fillMaxWidth())
                        .then(Modifier.clickable(onClick = {
                            selectedUser = user
                            presentDialog.value = true
                        }))
        ) {
            PicassoImage(
                    data = user.profileImageUrl,
                    modifier = Modifier.padding(start = 10.dp)
                            .then(Modifier.background(Color.Black, CircleShape))
                            .then(Modifier.border(1.5.dp, Color.Black, CircleShape))
                            .then(
                                    Modifier.preferredSize(60.dp)
                                            .clip(CircleShape)
                            ),
                    contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(start = 15.dp)) {
                Text(
                        user.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                )
            }
        }
    }

    @Composable
    fun UnblockDialog() {
        AlertDialog(
                onDismissRequest = {
                },
                title = {
                    Text("Unblock user")
                },
                text = {
                    Text("Do you want to unblock this user?")
                },
                dismissButton = {
                    Button(onClick = {
                        presentDialog.value = false
                    }) {
                        Text("Cancel")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (selectedUser != null) {
                            blocklistViewModel.removeBlockedUser(selectedUser!!)
                            presentDialog.value = false
                        }
                    }) {
                        Text("Confirm")
                    }
                }
        )
    }
}