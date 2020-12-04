package com.example.jetpackcomposechatapplication.models

class User(val uid: String, var username: String, var profileImageUrl: String, var email: String, var token: String?) {
    constructor() : this("", "", "", "", "")
}