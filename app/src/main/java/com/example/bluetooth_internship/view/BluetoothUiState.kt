package com.example.bluetooth_internship.view

import com.example.bluetooth_internship.model.BluetoothDevice


data class BluetoothUiState(
    val scannedDevices : List<BluetoothDevice> = emptyList(),
    val pairedDevices : List<BluetoothDevice> = emptyList(),
)
