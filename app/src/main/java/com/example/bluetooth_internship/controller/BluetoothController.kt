package com.example.bluetooth_internship.controller

import com.example.bluetooth_internship.model.BluetoothDevice
import com.example.bluetooth_internship.model.BluetoothDeviceDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices : StateFlow<List<BluetoothDeviceDomain>>
    val pairedDevices : StateFlow<List<BluetoothDeviceDomain>>
    val isConnected : StateFlow<Boolean>
    val errors : SharedFlow<String>

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer() : Flow<Connectionresult>
    fun connectToDevice(device: BluetoothDevice) : Flow<Connectionresult>
    fun closeConnection()

    fun release()
}