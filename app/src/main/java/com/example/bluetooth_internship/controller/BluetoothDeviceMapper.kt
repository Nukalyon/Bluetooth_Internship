package com.example.bluetooth_internship.controller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.bluetooth_internship.model.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain() : BluetoothDeviceDomain
{
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}