package com.example.jetpackcomposechatapplication.models

class User(val uid: String, var username: String, var profileImageUrl: String, var email: String) {
    constructor() : this("", "", "", "")

    override fun equals(other: Any?): Boolean {
        if (other is User) {
            return other.uid == uid
        }

        return false
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + profileImageUrl.hashCode()
        result = 31 * result + email.hashCode()
        return result
    }
}