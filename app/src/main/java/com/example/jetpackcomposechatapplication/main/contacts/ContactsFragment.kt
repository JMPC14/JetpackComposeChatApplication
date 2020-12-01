package com.example.jetpackcomposechatapplication.main.contacts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.lifecycle.ViewModelProvider
import com.example.jetpackcomposechatapplication.Border
import com.example.jetpackcomposechatapplication.border
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.chrisbanes.accompanist.picasso.PicassoImage

class ContactsFragment : Fragment() {

    lateinit var contactsViewModel: ContactsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Contacts()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contactsViewModel = ViewModelProvider(requireActivity()).get(ContactsViewModel::class.java)

        fetchContacts()
    }

    @Composable
    fun Contacts() {
        contactsViewModel.contacts.observe(viewLifecycleOwner, {
            Log.d("NEWTAG", "VIEWMODEL CHANGED")
        })
        val contacts by contactsViewModel.contacts.observeAsState()
        if (contacts != null) {
            Log.d("NEWTAG", "size: ${contacts?.size}")
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
                .then(
                    Modifier.fillMaxWidth()
                        .then(Modifier.clickable {
                            Log.d("NEWTAG", "${user.uid} CLICKED")
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
            }
        }
    }

    private fun fetchContacts() {
        contactsViewModel.contacts.value = listOf()
        val uid = FirebaseAuth.getInstance().uid
        FirebaseDatabase.getInstance().getReference("/users/$uid/contacts")
            .addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val newRef = FirebaseDatabase.getInstance().getReference("/users/${it.value.toString()}")
                    newRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(User::class.java)
                            if (user != null) {
                                contactsViewModel.addContact(user)
                                Log.d("NEWTAG", "ADDING CONTACT $user")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
        })
    }
}