package com.example.bluetooth_internship.controller

import com.example.bluetooth_internship.model.BluetoothDevice
import com.example.bluetooth_internship.model.BluetoothDeviceDomain
import com.example.bluetooth_internship.model.BluetoothMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/***
    Cette interface permet d'implémenter les variables et méthodes que l'on va utiliser
 ***/

interface BluetoothController {
    // Liste des appareils que l'on va détecter
    val scannedDevices : StateFlow<List<BluetoothDeviceDomain>>
    // Liste des appareils avec qui on a déjà établi une connexion
    val pairedDevices : StateFlow<List<BluetoothDeviceDomain>>
    // Différents états de connection
    val isConnected : StateFlow<Boolean>
    val errors : SharedFlow<String>

    // Lance la recherche d'appareils
    fun startDiscovery()
    // Stop la recherche d'appareils
    fun stopDiscovery()

    // lance un serveur qui sera en attente d'une demande de connexion
    fun startBluetoothServer() : Flow<Connectionresult>
    // Lance une tentative de connexion vers l'appareil ciblé
    fun connectToDevice(device: BluetoothDevice) : Flow<Connectionresult>
    // Ferme la connexion
    fun closeConnection()

    suspend fun trySendMessage(message : String) : BluetoothMessage?


    // Libère les éléments
    fun release()
}