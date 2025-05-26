package com.example.plugin

import com.example.plugin.controller.AndroidBluetoothController
import com.example.plugin.controller.BluetoothController
import android.content.Context
import com.example.plugin.model.BluetoothDevice

class KotlinBluetoothPlugin(
    context : Context
) : IBluetoothPlugin{

    private val controller : BluetoothController = AndroidBluetoothController(context)

    override fun startScan() {
        controller.startDiscovery()
    }

    override fun stopScan() {
        controller.stopDiscovery()
    }

    override fun connectToDevice(device : BluetoothDevice) {
        controller.connectToDevice(device)
    }

    override fun disconnect() {
        controller.closeConnection()
    }

    override fun sendMessage(message: String?) {
        controller.trySendMessage(message)
    }
}