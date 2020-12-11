package com.example.jetpackcomposechatapplication.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonConstants
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.draw.drawShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.jetpackcomposechatapplication.main.MainActivity
import com.example.jetpackcomposechatapplication.R
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import dev.chrisbanes.accompanist.picasso.PicassoImage

class RegisterFragment : Fragment() {

    lateinit var registerViewModel: RegisterViewModel

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        registerViewModel = ViewModelProvider(requireActivity()).get(RegisterViewModel::class.java)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            registerViewModel.profileImageUri.value = data.data
        }
    }

    @Composable
    fun RegisterForm() {
        Box(alignment = Alignment.TopStart, modifier = Modifier.fillMaxSize()) {
            Box(
                    alignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                    .then(Modifier.background(Color(resources.getColor(R.color.default_green, null))))
            ) {
                ScrollableColumn(
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

                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"
                            startActivityForResult(intent, 0)
                        }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val image by registerViewModel.profileImageUri.observeAsState()
                                if (image != null) {
                                    PicassoImage(
                                            data = image!!,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                                    .then(Modifier.drawShadow(20.dp, CircleShape))
                                                    .then(Modifier.size(150.dp).clip(CircleShape))
                                                    .then(Modifier.border(2.dp, Color.White, CircleShape)),
                                            contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                            asset = vectorResource(R.drawable.ic_baseline_account_circle_white_24),
                                            modifier = Modifier.size(100.dp, 100.dp)
                                    )
                                }
                                Text("Choose a profile picture", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
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
                                            .then(Modifier.drawOpacity(if (usernameState.value.text == "") 0.7f else 0f)),
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
                                            .then(Modifier.drawOpacity(if (passwordConfirmationState.value.text == "") 0.7f else 0f)),
                                    color = (Color.White)
                            )
                        }

                        Button(
                                onClick = {
                                    val email = emailState.value.text
                                    val username = usernameState.value.text
                                    val password = passwordState.value.text
                                    val passwordConfirm = passwordConfirmationState.value.text

                                    if (email.isEmpty() ||
                                            username.isEmpty() ||
                                            password.isEmpty() ||
                                            passwordConfirm.isEmpty()) {
                                        Toast.makeText(requireContext(), "Please enter an email address, username and password.", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    if (password != passwordConfirm) {
                                        Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    if (registerViewModel.profileImageUri.value != null) {

                                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        Log.d("Main", "User created with ID: ${task.result?.user?.uid}")

                                                        registerViewModel.uploadImageToFirebase(registerViewModel.profileImageUri.value!!) {
                                                            val uid = FirebaseAuth.getInstance().uid ?: ""
                                                            val user = User(
                                                                    uid,
                                                                    username,
                                                                    it,
                                                                    email,
                                                                    null
                                                            )

                                                            registerViewModel.saveUserToDatabase(user) {
                                                                val intent = Intent(requireActivity(), MainActivity::class.java)
                                                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                startActivity(intent)
                                                            }
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(requireContext(), "Invalid parameters: ${it.message}", Toast.LENGTH_LONG).show()
                                                }
                                    }
                                },
                                colors = ButtonConstants.defaultButtonColors(backgroundColor = Color.White),
                                modifier = Modifier.padding(top = 30.dp, bottom = 30.dp).then(Modifier.size(width = 335.dp, height = 44.dp))
                        ) {
                            Text("Register", color = Color(resources.getColor(R.color.default_green, null)))
                        }
                    }
                }
            }

            Column {
                ClickableText(AnnotatedString("Back"), style = TextStyle(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp),
                        modifier = Modifier.padding(start = 20.dp, top = 10.dp), onClick = {
                    this@RegisterFragment.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                })
            }
        }
    }
}