package com.example.bluetooth_internship.controller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.example.bluetooth_internship.model.BluetoothDeviceDomain
import com.example.bluetooth_internship.model.BluetoothStateReceiver
import com.example.bluetooth_internship.model.FoundDeviceReceiver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

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
    private val _isConnected = MutableStateFlow<Boolean>(false)
    private val _errors = MutableSharedFlow<String>()


    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver{
        device -> _scannedDevices.update{
            devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if(newDevice in devices) devices else devices + newDevice
        }
    }

    private var currentServerSocket: BluetoothServerSocket ?= null
    private var currentClientSocket: BluetoothSocket ?= null

    private val bluetoothStateReceiver = BluetoothStateReceiver{ isConnected, bluetoothDevice ->
        if(bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true)
        {
            _isConnected.update { isConnected }
        }
        else{
            CoroutineScope(Dispatchers.IO).launch{
                _errors.tryEmit("Can't connect to a non-paired device")
            }
        }
    }

    init{
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply{
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
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
    }

    override fun startBluetoothServer(): Flow<Connectionresult> {
        return flow {
            if(!hasPermissions(android.Manifest.permission.BLUETOOTH_CONNECT))
            {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            // Start the server
            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "link_arduino",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while(shouldLoop){
                currentClientSocket = try {
                    currentServerSocket?.accept()
                }
                catch (e: IOException){
                    shouldLoop = false
                    null
                }
                emit(Connectionresult.ConnectionEstablished)
                currentClientSocket?.let{
                    currentServerSocket?.close()
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<Connectionresult> {
        return flow {
            if(!hasPermissions(android.Manifest.permission.BLUETOOTH_CONNECT))
            {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            val bluetoothdevice = bluetoothAdapter?.getRemoteDevice(device.address)

            currentClientSocket = bluetoothdevice
                ?.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID))
            stopDiscovery()

            if(bluetoothAdapter?.bondedDevices?.contains(bluetoothdevice) == false)
            {

            }

            currentClientSocket?.let{ socket ->
                try {
                    socket.connect()
                    emit(Connectionresult.ConnectionEstablished)
                }
                catch (e: IOException){
                    socket.close()
                    currentClientSocket = null
                    emit(Connectionresult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        //Reset
        currentClientSocket = null
        currentServerSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
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

    companion object{
        // https://www.uuidgenerator.net/
        const val SERVICE_UUID = "9a2437a0-f4d5-4a64-8abf-3e3c45ad0293"
    }
}