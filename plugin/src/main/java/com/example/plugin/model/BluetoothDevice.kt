package com.example.plugin.model

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name : String?,
    val address : String
)