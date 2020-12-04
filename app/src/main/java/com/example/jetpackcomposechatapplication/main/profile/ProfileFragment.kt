package com.example.jetpackcomposechatapplication.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonConstants
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.jetpackcomposechatapplication.R
import com.example.jetpackcomposechatapplication.main.latestmessages.UserViewModel
import dev.chrisbanes.accompanist.picasso.PicassoImage

class ProfileFragment: Fragment() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Column(modifier = Modifier.fillMaxSize()
                        .then(Modifier.background(Color(resources.getColor(R.color.default_green, null)))),
                horizontalAlignment = Alignment.CenterHorizontally) {
                    Profile()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
    }

    @Composable
    fun Profile() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PicassoImage(
                    data = userViewModel.user.value?.profileImageUrl!!,
                    modifier = Modifier.padding(30.dp)
                            .then(Modifier.drawShadow(20.dp, CircleShape))
                            .then(Modifier.size(250.dp).clip(CircleShape))
                            .then(Modifier.border(2.dp, Color.White, CircleShape)),
                            contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.fillMaxWidth(0.7f)
                    .then(Modifier.background(Color.White, RoundedCornerShape(22.dp)))
                    .then(Modifier.border(2.dp, Color.White, RoundedCornerShape(22.dp))),
                    horizontalAlignment = Alignment.Start) {

                Row(modifier = Modifier.fillMaxWidth()
                        .then(Modifier.clip(RoundedCornerShape(topLeft = 24.dp, topRight = 24.dp)))
                        .then(Modifier.background(Color(resources.getColor(R.color.profile_green,null))))
                        .then(Modifier.padding(start = 12.dp, top = 18.dp, bottom = 15.dp))) {
                    Text("Username", fontSize = 18.sp, color = Color.White)
                }

                Row(modifier = Modifier.padding(start = 12.dp, top = 15.dp, bottom = 15.dp)) {
                    Text(userViewModel.user.value?.username!!, fontSize = 18.sp, color = Color(resources.getColor(R.color.default_green, null)))
                }

                Row(modifier = Modifier.fillMaxWidth()
                        .then(Modifier.background(Color(resources.getColor(R.color.profile_green,null))))
                        .then(Modifier.padding(start = 12.dp, top = 15.dp, bottom = 15.dp))) {
                    Text("Email Address", fontSize = 18.sp, color = Color.White)
                }

                Row(modifier = Modifier.padding(start = 12.dp, top = 15.dp, bottom = 18.dp)) {
                    Text(userViewModel.user.value?.email!!, fontSize = 18.sp, color = Color(resources.getColor(R.color.default_green, null)))
                }
            }

            OutlinedButton(
                    onClick = { /*TODO*/ },
                    colors = ButtonConstants.defaultButtonColors(backgroundColor = Color.Transparent),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.padding(top = 15.dp)
            ) {
                Text("Edit", color = Color.White)
            }
        }
    }
}