package com.example.jetpackcomposechatapplication.main.latestmessages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jetpackcomposechatapplication.ui.Border
import com.example.jetpackcomposechatapplication.R
import com.example.jetpackcomposechatapplication.ui.border
import com.example.jetpackcomposechatapplication.launcher.LauncherActivity
import com.example.jetpackcomposechatapplication.main.blocklist.BlocklistViewModel
import com.example.jetpackcomposechatapplication.main.chat.ChatViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import dev.chrisbanes.accompanist.picasso.PicassoImage

class LatestMessagesFragment : Fragment() {

    lateinit var latestMessagesViewModel: LatestMessagesViewModel
    lateinit var userViewModel: UserViewModel
    lateinit var chatViewModel: ChatViewModel
    lateinit var blocklistViewModel: BlocklistViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Scaffold(
                        floatingActionButton = {
                            FloatingActionButton(onClick = {
                                findNavController().navigate(R.id.action_nav_latest_messages_to_newConversationFragment)
                            },
                                    backgroundColor = Color(resources.getColor(R.color.default_green, null))) {
                                Text(
                                        text = "+",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Light,
                                        color = Color.White,
                                        modifier = Modifier.offset(y = (-2).dp)
                                )
                            }
                        },
                        floatingActionButtonPosition = FabPosition.End
                ) {
                    LatestMessages()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        latestMessagesViewModel = ViewModelProvider(requireActivity()).get(LatestMessagesViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        blocklistViewModel = ViewModelProvider(requireActivity()).get(BlocklistViewModel::class.java)

        if (userViewModel.user.value == null) {
            userViewModel.fetchCurrentUser {
                if (it) {
                    updateNavHeader()
                } else {
                    val intent = Intent(requireContext(), LauncherActivity::class.java)
                    intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK).or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }

        blocklistViewModel.fetchBlocklist()
        chatViewModel.listenForOnlineUsers()

        blocklistViewModel.blocklist.observe(viewLifecycleOwner, {
            latestMessagesViewModel.listenForLatestMessages(blocklistViewModel.blocklist.value!!)
        })

        chatViewModel.onlineUsers.observe(viewLifecycleOwner, {
            latestMessagesViewModel.refreshRecyclerViewMessages()
        })
    }

    @Composable
    fun LatestMessages() {
        val messages by latestMessagesViewModel.sortedMap.observeAsState()
        if (messages != null) {
            ScrollableColumn {
                messages?.forEach {
                    LatestMessageItem(it.key, it.value)
                }
            }
        }
    }

    @Composable
    fun LatestMessageItem(user: User, message: ChatMessage) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(75.dp)
                        .then(Modifier.border(bottom = Border(0.5.dp, Color.LightGray)))
                        .then(Modifier.fillMaxWidth())
                        .then(Modifier.clickable {
                            chatViewModel.tempUser = user
                            findNavController().navigate(R.id.action_nav_latest_messages_to_chatFragment)
                        })
        ) {
            val color = if (chatViewModel.onlineUsers.value!!.contains(user.uid)) Color(resources.getColor(R.color.default_green, null)) else Color.Black

            PicassoImage(
                    data = user.profileImageUrl,
                    modifier = Modifier.padding(start = 10.dp)
                            .then(Modifier.border(1.5.dp, color, CircleShape))
                            .then(
                                    Modifier.preferredSize(60.dp)
                                            .clip(CircleShape)
                            ),
                    contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(start = 15.dp)) {
                Text(
                        user.username,
                        modifier = Modifier.padding(bottom = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                )

                val text = if (user.uid == userViewModel.user.value?.uid) "You: " else "Them: "
                Text(
                        text = text + message.text,
                        color = Color.Gray,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 10.dp)
                )
            }
        }
    }

    private fun updateNavHeader() {
        val imageView = requireActivity().findViewById<CircleImageView>(R.id.navHeaderImageView)
        val textViewUsername = requireActivity().findViewById<TextView>(R.id.navHeaderUsername)
        val textViewEmail = requireActivity().findViewById<TextView>(R.id.navHeaderEmail)
        Picasso.get().load(userViewModel.user.value?.profileImageUrl).into(imageView)
        textViewUsername.text = userViewModel.user.value?.username
        textViewEmail.text = userViewModel.user.value?.email
    }
}