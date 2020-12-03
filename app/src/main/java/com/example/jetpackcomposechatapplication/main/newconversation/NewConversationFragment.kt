package com.example.jetpackcomposechatapplication.main.newconversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.jetpackcomposechatapplication.main.contacts.ContactsFragment
import com.example.jetpackcomposechatapplication.main.contacts.ContactsViewModel

class NewConversationFragment: Fragment() {

    private lateinit var contactsViewModel: ContactsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Contacts()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contactsViewModel = ViewModelProvider(requireActivity()).get(ContactsViewModel::class.java)

        contactsViewModel.fetchContacts()
    }

    @Composable
    fun Contacts() {
        val contacts by contactsViewModel.contacts.observeAsState()
        val modifier = Modifier.clickable(onClick = { /*TODO*/ })
        if (contacts != null) {
            ScrollableColumn {
                contacts?.forEach {
                    ContactsFragment().ContactItem(it, modifier)
                }
            }
        }
    }
}