package com.example.plugin.controller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.plugin.model.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain() : BluetoothDeviceDomain
{
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}