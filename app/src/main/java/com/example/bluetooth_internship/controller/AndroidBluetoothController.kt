package com.example.bluetooth_internship.controller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.example.bluetooth_internship.model.BluetoothDeviceDomain
import com.example.bluetooth_internship.model.FoundDeviceReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/*****************************
 *      Ref:
 *      https://www.youtube.com/watch?v=A41hkHoYu4M
 *****************************/

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context : Context
): BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver{
        device -> _scannedDevices.update{
            devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            Log.d("DIM", "Device found with name = ${device.name}")
            if(newDevice in devices) devices else devices + newDevice
        }
    }

    init{
        updatePairedDevices()
    }

    override fun startDiscovery() {
        if(!hasPermissions(android.Manifest.permission.BLUETOOTH_SCAN))
        {
            return
        }
        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if(!hasPermissions(android.Manifest.permission.BLUETOOTH_SCAN))
        {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
        context.unregisterReceiver(foundDeviceReceiver)
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
    }

    private fun updatePairedDevices()
    {
        if(!hasPermissions(android.Manifest.permission.BLUETOOTH_CONNECT))
        {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices -> _pairedDevices.update { devices } }
    }

    private fun hasPermissions(permission : String) : Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}