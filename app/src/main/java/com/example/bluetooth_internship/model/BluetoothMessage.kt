package com.example.bluetooth_internship.model

data class BluetoothMessage(
    val message: String,
    val senderName : String,
    val isFromLocalUser : Boolean
)
