package com.example.jetpackcomposechatapplication.models

class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: String,
    val time: Long
) {
    constructor(): this("", "", "", "", "", -1)

    var imageUrl: String? = null
    var fileUrl: String? = null
    var fileSize: Double? = null
    var fileType: String? = null


    /** Constructor for image messages **/
    constructor(id: String, text: String, fromId: String, toId: String, timestamp: String, time: Long, imageUrl: String) : this(id, text, fromId, toId, timestamp, time) {
        this.imageUrl = imageUrl
    }


    /** Constructor for file attachment messages **/
    constructor(id: String, text: String, fromId: String, toId: String, timestamp: String, time: Long, fileUrl: String, fileSize: Double, fileType: String) : this(id, text, fromId, toId, timestamp, time) {
        this.fileUrl = fileUrl
        this.fileSize = fileSize
        this.fileType = fileType
    }
}