package com.example.plugin.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugin.controller.BluetoothController
import com.example.plugin.controller.Connectionresult
import com.example.plugin.model.BluetoothDeviceDomain
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BluetoothView (
    private val bluetoothController : BluetoothController
): ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ){
        //if any value changes
        scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            messages = if(state.isConnected) state.messages else emptyList()
        )
    }
        //convert all the StateFlow up to a simple State
        .stateIn( viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)


    init {
        bluetoothController.isConnected.onEach {
            isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach {
            error ->
            _state.update { it.copy(errorMessage = error) }
        }.launchIn(viewModelScope)
    }

    private var deviceConnectionJob : Job ?= null

    fun connectToDevice(device: BluetoothDeviceDomain)
    {
        _state.update { it.copy( isConnecting = true) }
        deviceConnectionJob =  bluetoothController.connectToDevice(device).listen()
    }

    fun disconnectFromDevice(){
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update { it.copy( isConnecting = false, isConnected = false) }
    }


    fun waitForIncomingConnections(){
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController.startBluetoothServer().listen()
    }

    fun sendMessage(message : String){
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendMessage(message)
            if(bluetoothMessage != null){
                _state.update { it.copy(
                    messages = it.messages + bluetoothMessage
                ) }
            }
        }
    }

    fun startScan(){
        bluetoothController.startDiscovery()
    }

    fun stopScan(){
        bluetoothController.stopDiscovery()
    }

    private fun Flow<Connectionresult>.listen() : Job{
        return onEach {
            result ->
            when(result)
            {
                Connectionresult.ConnectionEstablished -> {
                    _state.update { it.copy(
                        isConnected = true,
                        isConnecting = false,
                        errorMessage = null
                    ) }
                }
                is Connectionresult.TransferSucceeded ->{
                    _state.update { it.copy(
                        messages = it.messages + result.message
                    ) }
                }
                is Connectionresult.Error -> {
                    _state.update { it.copy(
                        isConnected = false,
                        isConnecting = false,
                        errorMessage = result.message
                    ) }
                }
            }
        }.catch {
            throwable ->
            bluetoothController.closeConnection()
            _state.update { it.copy(
                isConnected = false,
                isConnecting = false
            ) }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}