package com.example.bluetooth_internship.model

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name : String?,
    val address : String
)