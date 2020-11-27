package com.example.jetpackcomposechatapplication.launcher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.runtime.MutableState
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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.jetpackcomposechatapplication.main.MainActivity
import com.example.jetpackcomposechatapplication.R

class RegisterFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return ComposeView(context = requireContext()).apply {
            setContent {
                RegisterForm()
            }
        }
    }

    @Composable
    fun RegisterForm() {
        Box(alignment = Alignment.TopStart, modifier = Modifier.fillMaxSize()) {
            Box(
                    alignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                    .then(Modifier.background(Color(resources.getColor(R.color.default_green, null))))
            ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                                .then(Modifier.padding(top = 30.dp))
                ) {

                    Image(
                            asset = imageResource(R.drawable.image_bird),
                            modifier = Modifier.height(125.dp)
                    )

                    Text(
                            "Create a new account",
                            modifier = Modifier.padding(bottom = 25.dp, top = 10.dp),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val emailState = remember { mutableStateOf(TextFieldValue()) }
                        val usernameState = remember { mutableStateOf(TextFieldValue()) }
                        val passwordState = remember { mutableStateOf(TextFieldValue()) }
                        val passwordConfirmationState = remember { mutableStateOf(TextFieldValue()) }

                        fun createModifier(state: MutableState<TextFieldValue>): Modifier {
                            return Modifier.border(width = 2.dp,
                                    color = if (state.value.text != "") Color.White else Color(resources.getColor(R.color.half_white, null)),
                                    shape = RoundedCornerShape(10))
                                    .then(Modifier.padding(15.dp))
                                    .then(Modifier.preferredWidthIn(min = 300.dp))
                        }

                        val emailModifier = createModifier(emailState)
                        val usernameModifier = createModifier(usernameState)
                        val passwordModifier = createModifier(passwordState)
                        val passwordConfirmModifier = createModifier(passwordConfirmationState)

                        Column {
//                            Image(asset = imageResource(R.drawable.ic_baseline_account_circle_white_24), modifier = Modifier.size(100.dp, 100.dp))
                            Text("Choose a profile picture", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Box(alignment = Alignment.CenterStart, modifier = Modifier.padding(top = 20.dp)) {
                            BasicTextField(
                                    value = emailState.value,
                                    onValueChange = { emailState.value = it },
                                    maxLines = 1,
                                    modifier = emailModifier,
                                    cursorColor = Color.White,
                                    textStyle = TextStyle(color = Color.White)
                            )

                            Text(
                                    "Email",
                                    modifier = Modifier.padding(start = 14.dp).then(Modifier.drawOpacity(if (emailState.value.text == "") 0.7f else 0f)),
                                    color = (Color.White)
                            )
                        }

                        Box(alignment = Alignment.CenterStart) {
                            BasicTextField(
                                    value = usernameState.value,
                                    onValueChange = { usernameState.value = it },
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 20.dp).then(usernameModifier),
                                    cursorColor = Color.White,
                                    textStyle = TextStyle(color = Color.White)
                            )

                            Text(
                                    "Username",
                                    modifier = Modifier.padding(start = 14.dp, top = 20.dp)
                                            .then(Modifier.drawOpacity(if (passwordState.value.text == "") 0.7f else 0f)),
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
                                    modifier = Modifier.padding(top = 20.dp).then(passwordModifier),
                                    cursorColor = Color.White,
                                    textStyle = TextStyle(color = Color.White)
                            )

                            Text(
                                    "Password",
                                    modifier = Modifier.padding(start = 14.dp, top = 20.dp)
                                            .then(Modifier.drawOpacity(if (passwordState.value.text == "") 0.7f else 0f)),
                                    color = (Color.White)
                            )
                        }

                        Box(alignment = Alignment.CenterStart) {
                            BasicTextField(
                                    value = passwordConfirmationState.value,
                                    onValueChange = { passwordConfirmationState.value = it },
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.padding(top = 20.dp).then(passwordConfirmModifier),
                                    cursorColor = Color.White,
                                    textStyle = TextStyle(color = Color.White)
                            )

                            Text(
                                    "Confirm Password",
                                    modifier = Modifier.padding(start = 14.dp, top = 20.dp)
                                            .then(Modifier.drawOpacity(if (passwordState.value.text == "") 0.7f else 0f)),
                                    color = (Color.White)
                            )
                        }

                        Button(
                                onClick = {
                                    startActivity(Intent(requireContext(), MainActivity::class.java))
                                    requireActivity().finish()
                                },
                                colors = ButtonConstants.defaultButtonColors(backgroundColor = Color.White),
                                modifier = Modifier.padding(top = 30.dp).then(Modifier.size(width = 335.dp, height = 44.dp))
                        ) {
                            Text("Register", color = Color(resources.getColor(R.color.default_green, null)))
                        }
                    }
                }
            }

            Column {
                ClickableText(AnnotatedString("Back"), style = TextStyle(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp),
                        modifier = Modifier.padding(start = 20.dp, top = 0.dp), onClick = {
                    this@RegisterFragment.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                })
            }
        }
    }
}