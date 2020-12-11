package com.example.jetpackcomposechatapplication.launcher

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jetpackcomposechatapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegisterViewModel: ViewModel() {

    var profileImageUri = MutableLiveData<Uri>()

    fun uploadImageToFirebase(uri: Uri, callback: (String) -> Unit) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(uri)
            .addOnSuccessListener {
                Log.d("Main", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener { it2 ->
                    Log.d("Main", "File Location: $it2")

                    callback(it2.toString())
                }
            }
            .addOnFailureListener {
                Log.d("Main", "Image upload failed")
            }
    }

    fun saveUserToDatabase(user: User, callback: () -> Unit) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val contactRef = FirebaseDatabase.getInstance().getReference("/users/$uid/contacts")

        ref.setValue(user)
            .addOnSuccessListener {
                callback()
            }
        contactRef.setValue(listOf<String>())
    }
}