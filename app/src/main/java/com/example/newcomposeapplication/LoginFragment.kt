package com.example.newcomposeapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.ClickableText
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonConstants
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(context = requireContext()).apply {
            setContent {
                LoginForm()
            }
        }
    }

    @Composable
    fun LoginForm() {
        Box(alignment = Alignment.TopEnd, modifier = Modifier.fillMaxSize()) {
            Box(
                    alignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                    .then(Modifier.background(Color(resources.getColor(R.color.default_green, null))))
            ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                                .then(Modifier.padding(top = 100.dp))
                ) {

                    Image(
                            asset = imageResource(R.drawable.image_bird),
                            modifier = Modifier.height(125.dp)
                    )

                    Text(
                            "Log in to your account",
                            modifier = Modifier.padding(bottom = 25.dp, top = 10.dp),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val usernameState = remember { mutableStateOf(TextFieldValue()) }
                        val passwordState = remember { mutableStateOf(TextFieldValue()) }

                        val usernameModifier = Modifier.border(width = 2.dp,
                                color = if (usernameState.value.text != "") Color.White else Color(resources.getColor(R.color.half_white, null)),
                                shape = RoundedCornerShape(10))
                                .then(Modifier.padding(15.dp))
                                .then(Modifier.preferredWidthIn(min = 300.dp))

                        val passwordModifier = Modifier.border(width = 2.dp,
                                color = if (passwordState.value.text != "") Color.White else Color(resources.getColor(R.color.half_white, null)),
                                shape = RoundedCornerShape(10))
                                .then(Modifier.padding(15.dp))
                                .then(Modifier.preferredWidthIn(min = 300.dp))

                        Box(alignment = Alignment.CenterStart) {
                            BasicTextField(
                                    value = usernameState.value,
                                    onValueChange = { usernameState.value = it },
                                    maxLines = 1,
                                    modifier = usernameModifier,
                                    cursorColor = Color.White,
                                    textStyle = TextStyle(color = Color.White)
                            )

                            Text(
                                    "Email",
                                    modifier = Modifier.padding(start = 14.dp).then(Modifier.drawOpacity(if (usernameState.value.text == "") 0.7f else 0f)),
                                    color = (Color.White)
                            )
                        }

                        Box(alignment = Alignment.CenterStart) {
                            BasicTextField(
                                    value = passwordState.value,
                                    onValueChange = { passwordState.value = it },
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.padding(top = 30.dp).then(passwordModifier),
                                    cursorColor = Color.White,
                                    textStyle = TextStyle(color = Color.White)
                            )

                            Text(
                                    "Password",
                                    modifier = Modifier.padding(start = 14.dp, top = 30.dp)
                                            .then(Modifier.drawOpacity(if (passwordState.value.text == "") 0.7f else 0f)),
                                    color = (Color.White)
                            )
                        }

                        Column(modifier = Modifier.align(Alignment.End)) {
                            ClickableText(AnnotatedString("Forgot Password?"), onClick = {
                                Log.d("TAG", "FORGOT PASSWORD")
                            }, style = TextStyle(color = Color.White, fontWeight =
                            FontWeight.Bold, fontSize = 16.sp), modifier = Modifier.padding(top = 12.dp, bottom = 15.dp))
                        }

                        Button(
                                onClick = {

                                },
                                colors = ButtonConstants.defaultButtonColors(backgroundColor = Color.White),
                                modifier = Modifier.padding(top = 10.dp).then(Modifier.size(width = 335.dp, height = 44.dp))
                        ) {
                            Text("Sign In", color = Color(resources.getColor(R.color.default_green, null)))
                        }
                    }
                }
            }

            Column {
                ClickableText(AnnotatedString("Register"), style = TextStyle(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp),
                        modifier = Modifier.padding(end = 20.dp, top = 0.dp), onClick = {
                    this@LoginFragment.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                })
            }
        }
    }
}
