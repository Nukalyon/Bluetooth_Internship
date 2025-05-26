package com.example.plugin.view

import com.example.plugin.model.BluetoothDevice
import com.example.plugin.model.BluetoothMessage


data class BluetoothUiState(
    val scannedDevices : List<BluetoothDevice> = emptyList(),
    val pairedDevices : List<BluetoothDevice> = emptyList(),
    val isConnected : Boolean = false,
    val isConnecting : Boolean = false,
    val errorMessage : String? = null,


    val messages: List<BluetoothMessage> = emptyList()
)
