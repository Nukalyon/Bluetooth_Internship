package com.example.bluetooth_internship.controller

import com.example.bluetooth_internship.model.BluetoothDeviceDomain
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices : StateFlow<List<BluetoothDeviceDomain>>
    val pairedDevices : StateFlow<List<BluetoothDeviceDomain>>

    fun startDiscovery()
    fun stopDiscovery()
    fun release()
}