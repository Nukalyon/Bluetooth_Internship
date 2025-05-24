package com.example.bluetooth_internship.controller

import com.example.bluetooth_internship.model.BluetoothMessage

fun BluetoothMessage.toByteArray() : ByteArray{
    return "$senderName#$message".encodeToByteArray()
}

fun String.toBluetoothMessage(isFromLocalUser : Boolean) : BluetoothMessage{
    val name = substringBeforeLast('#')
    val mess = substringAfter('#')
    return BluetoothMessage(
        senderName = name,
        message = mess,
        isFromLocalUser = isFromLocalUser)
}