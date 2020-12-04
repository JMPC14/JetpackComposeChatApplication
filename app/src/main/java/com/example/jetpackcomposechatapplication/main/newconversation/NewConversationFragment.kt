package com.example.jetpackcomposechatapplication.main.newconversation

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jetpackcomposechatapplication.ui.Border
import com.example.jetpackcomposechatapplication.R
import com.example.jetpackcomposechatapplication.ui.border
import com.example.jetpackcomposechatapplication.main.latestmessages.UserViewModel
import com.example.jetpackcomposechatapplication.main.chat.ChatViewModel
import com.example.jetpackcomposechatapplication.main.contacts.ContactsViewModel
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.chrisbanes.accompanist.picasso.PicassoImage
import java.util.*

class NewConversationFragment: Fragment() {

    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Contacts()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contactsViewModel = ViewModelProvider(requireActivity()).get(ContactsViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)

        contactsViewModel.fetchContacts()
    }

    @Composable
    fun Contacts() {
        val contacts by contactsViewModel.contacts.observeAsState()
        if (contacts != null) {
            ScrollableColumn {
                contacts?.forEach {
                    ContactItem(it)
                }
            }
        }
    }

    @Composable
    fun ContactItem(user: User) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(75.dp)
                        .then(Modifier.border(bottom = Border(0.5.dp, Color.LightGray)))
                        .then(Modifier.fillMaxWidth())
                        .then(Modifier.clickable(onClick = {
                            chatViewModel.newConversation(user, userViewModel) {
                                findNavController().navigate(R.id.action_newConversationFragment_to_chatFragment)
                            }
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
                        modifier = Modifier.padding(bottom = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                )
            }
        }
    }
}