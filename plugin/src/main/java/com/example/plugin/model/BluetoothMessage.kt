package com.example.plugin.model

data class BluetoothMessage(
    val message: String,
    val senderName : String,
    val isFromLocalUser : Boolean
)
