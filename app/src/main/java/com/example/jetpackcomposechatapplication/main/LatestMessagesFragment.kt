package com.example.jetpackcomposechatapplication.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.loadImageResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.ui.tooling.preview.Preview
import com.example.jetpackcomposechatapplication.Border
import com.example.jetpackcomposechatapplication.FirebaseManager
import com.example.jetpackcomposechatapplication.R
import com.example.jetpackcomposechatapplication.border
import com.example.jetpackcomposechatapplication.launcher.LauncherActivity
import com.example.jetpackcomposechatapplication.models.ChatMessage
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class LatestMessagesFragment : Fragment() {

    private var latestMessages = mutableListOf<ChatMessage>()
    lateinit var latestMessagesViewModel: LatestMessagesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(context = requireContext()).apply {
            setContent {
                LatestMessages()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        latestMessagesViewModel = ViewModelProvider(requireActivity()).get(LatestMessagesViewModel::class.java)

        if (FirebaseManager.user == null) {
            fetchCurrentUser()
        }

        listenForLatestMessages()

        latestMessages.add(
            ChatMessage(
                "testId",
                "testText1",
                "testFromId1",
                "testToId",
                "timestamp",
                0
            )
        )
        latestMessages.add(
            ChatMessage(
                "testId",
                "testText2",
                "testFromId2",
                "testToId",
                "timestamp",
                0
            )
        )
    }

    @Composable
    fun LatestMessages() {
        Column {
            LazyColumnFor(items = latestMessages) { chatMessage ->
                LatestMessageItem(chatMessage) {
                    // onclick
                }
            }
//            latestMessagesViewModel.latestMessages.observe(viewLifecycleOwner, {
//                latestMessages = it
//            })
        }
    }

    @Composable
    fun LatestMessageItem(message: ChatMessage, onClick: (ChatMessage) -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(75.dp)
                .then(Modifier.border(bottom = Border(0.5.dp, Color.LightGray)))
                .then(Modifier.fillMaxWidth())
        ) {
            val image = loadImageResource(id = R.drawable.dog)
            image.resource.resource?.let {
                Image(
                        asset = it,
                        modifier = Modifier.padding(start = 10.dp)
                                .then(Modifier.background(Color.Black, CircleShape))
                                .then(Modifier.border(2.dp, Color.Black, CircleShape))
                                .then(Modifier.preferredSize(50.dp)
                                        .clip(CircleShape)),
                        contentScale = ContentScale.Crop,
                )
            }

            Column(modifier = Modifier.padding(start = 15.dp)) {
                Text(message.fromId, modifier = Modifier.padding(bottom = 10.dp))

                Text(message.text, color = Color.LightGray)
            }
        }
    }

    @Preview
    @Composable
    fun Preview() {
        LatestMessageItem(
            ChatMessage(
                "testId",
                "testText1",
                "testFromId1",
                "testToId",
                "timestamp",
                0
            )
        ) {

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
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    FirebaseManager.user = p0.getValue(User::class.java)
                    val imageView = requireActivity().findViewById<CircleImageView>(R.id.navHeaderImageView)
                    val textViewUsername = requireActivity().findViewById<TextView>(R.id.navHeaderUsername)
                    val textViewEmail = requireActivity().findViewById<TextView>(R.id.navHeaderEmail)
                    Picasso.get().load(FirebaseManager.user?.profileImageUrl).into(imageView)
                    textViewUsername.text = FirebaseManager.user?.username
                    textViewEmail.text = FirebaseManager.user?.email
                }
            })
        }
    }

    val latestMessageMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        val sortedMap = latestMessageMap.toList().sortedByDescending { it.second.time }.toMap()
        latestMessages = latestMessageMap.values.toMutableList()
//        sortedMap.values.forEach { adapter.add(LatestMessageRow(it)) }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return

//                if (FirebaseManager.blocklist != null ) {
//                    if (FirebaseManager.blocklist!!.contains(chatMessage.fromId) || FirebaseManager.blocklist!!.contains(chatMessage.toId)) { return }
//                }

                latestMessageMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return

//                if (FirebaseManager.blocklist != null ) {
//                    if (FirebaseManager.blocklist!!.contains(chatMessage.fromId) || FirebaseManager.blocklist!!.contains(chatMessage.toId)) { return }
//                }

                latestMessageMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }
}