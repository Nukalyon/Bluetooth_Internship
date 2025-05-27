package com.example.bluetooth_internship.view

import com.example.bluetooth_internship.model.BluetoothDevice
import com.example.bluetooth_internship.model.BluetoothMessage


data class BluetoothUiState(
    val scannedDevices : List<BluetoothDevice> = emptyList(),
    val pairedDevices : List<BluetoothDevice> = emptyList(),
    val isConnected : Boolean = false,
    val isConnecting : Boolean = false,
    val errorMessage : String? = null,
    val isClient : Boolean = false,


    val messages: List<BluetoothMessage> = emptyList()
)
