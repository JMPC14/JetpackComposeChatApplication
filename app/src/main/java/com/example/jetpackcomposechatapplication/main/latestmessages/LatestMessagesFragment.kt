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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jetpackcomposechatapplication.Border
import com.example.jetpackcomposechatapplication.R
import com.example.jetpackcomposechatapplication.border
import com.example.jetpackcomposechatapplication.launcher.LauncherActivity
import com.example.jetpackcomposechatapplication.main.UserViewModel
import com.example.jetpackcomposechatapplication.main.chat.ChatViewModel
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import dev.chrisbanes.accompanist.picasso.PicassoImage

class LatestMessagesFragment : Fragment() {

    lateinit var latestMessagesViewModel: LatestMessagesViewModel
    lateinit var userViewModel: UserViewModel
    lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                LatestMessages()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        latestMessagesViewModel = ViewModelProvider(requireActivity()).get(LatestMessagesViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)

        if (userViewModel.user.value == null) {
            fetchCurrentUser()
        }

        listenForLatestMessages()
    }

    @Composable
    fun LatestMessages() {
        val messages by latestMessagesViewModel.latestMessages.observeAsState()
        if (messages != null) {
            ScrollableColumn {
                latestMessagesViewModel.latestMessages.value?.forEach {
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
                .then(
                    Modifier.fillMaxWidth()
                        .then(Modifier.clickable {
                            chatViewModel.tempUser = user
                            findNavController().navigate(R.id.action_nav_latest_messages_to_chatFragment)
                        })
                )
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
                    modifier = Modifier.padding(bottom = 6.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                val text = if (user.uid == userViewModel.user.value?.uid) "You: " else "Them: "
                Text(text + message.text, color = Color.Gray, fontSize = 16.sp)
            }
        }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(requireContext(), LauncherActivity::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK).or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
//            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
//                FirebaseManager.token = it.result?.token
//                FirebaseDatabase.getInstance().getReference("/users/$uid").child("token").setValue(
//                        FirebaseManager.token)
//            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    userViewModel.user.value = p0.getValue(User::class.java)
                    updateNavHeader()
                }
            })
        }
    }

    fun updateNavHeader() {
        val imageView = requireActivity().findViewById<CircleImageView>(R.id.navHeaderImageView)
        val textViewUsername = requireActivity().findViewById<TextView>(R.id.navHeaderUsername)
        val textViewEmail = requireActivity().findViewById<TextView>(R.id.navHeaderEmail)
        Picasso.get().load(userViewModel.user.value?.profileImageUrl).into(imageView)
        textViewUsername.text = userViewModel.user.value?.username
        textViewEmail.text = userViewModel.user.value?.email
    }

    private fun refreshRecyclerViewMessages() {
        val map = HashMap<User, ChatMessage>()
        latestMessagesViewModel.latestMessages.value!!.toList().sortedByDescending { it.second.text }.toMap(map)
        latestMessagesViewModel.latestMessages.value = map
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return

//                if (FirebaseManager.blocklist != null ) {
//                    if (FirebaseManager.blocklist!!.contains(chatMessage.fromId) || FirebaseManager.blocklist!!.contains(chatMessage.toId)) { return }
//                }

                fetchUserForMessage(p0.key!!, chatMessage)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return

//                if (FirebaseManager.blocklist != null ) {
//                    if (FirebaseManager.blocklist!!.contains(chatMessage.fromId) || FirebaseManager.blocklist!!.contains(chatMessage.toId)) { return }
//                }

                fetchUserForMessage(p0.key!!, chatMessage)
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    fun fetchUserForMessage(key: String, chatMessage: ChatMessage) {
        var user: User?
        val userRef = FirebaseDatabase.getInstance().getReference("users/$key")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
                if (user != null) {
                    latestMessagesViewModel.latestMessages.value!![user!!] = chatMessage
                    refreshRecyclerViewMessages()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}