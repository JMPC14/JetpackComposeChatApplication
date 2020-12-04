package com.example.jetpackcomposechatapplication.main.blocklist

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
import com.example.jetpackcomposechatapplication.main.chat.ChatViewModel
import com.example.jetpackcomposechatapplication.main.contacts.ContactsFragment

class BlocklistFragment: Fragment() {

    private lateinit var blocklistViewModel: BlocklistViewModel
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Blocklist()
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
        if (blocklist != null) {
            ScrollableColumn {
                blocklist?.forEach {
                    val modifier = Modifier.clickable(onClick = {
                        chatViewModel.tempUser = it
                    })
                    ContactsFragment().ContactItem(it)
                }
            }
        }
    }
}