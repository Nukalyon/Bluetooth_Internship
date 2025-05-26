package com.example.plugin.controller

import com.example.plugin.model.BluetoothMessage

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