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
import com.example.bluetooth_internship.model.BluetoothDataTransferService
import com.example.bluetooth_internship.model.BluetoothDeviceDomain
import com.example.bluetooth_internship.model.BluetoothMessage
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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    // lazy est ThreadSafe et appelle init() implicitement
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var dataTransferService : BluetoothDataTransferService ?= null

    // MutableStateFlow garde une seule valeur et permet de l'update facilement
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    private val _isConnected = MutableStateFlow<Boolean>(false)
    // MutableSharedFlow garde plusieures valeurs
    private val _errors = MutableSharedFlow<String>()


    // Expose la liste des appareils en tant que StateFlow en read-only
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()
    // Expose la flow des erreurs en SharedFlow pour plusieurs collecteurs, read-only
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()


    private val foundDeviceReceiver = FoundDeviceReceiver{
        device ->
        Log.d(TAG, "FoundDeviceReceiver returned $device")
        _scannedDevices.update{
            devices ->
            Log.i(TAG, "FoundDeviceReceiver _scannedDevices update")
            val newDevice = device.toBluetoothDeviceDomain()
            if(newDevice in devices) devices else devices + newDevice
        }
    }


    // Socket serveur actuel
    private var currentServerSocket: BluetoothServerSocket? = null
    // Socket client actuel
    private var currentClientSocket: BluetoothSocket? = null

    // Récepteur d'état Bluetooth pour gérer les connexions
    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        Log.d(TAG, "BluetoothStateReceiver triggered")
        // Vérifie si l'appareil est apparié
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            Log.d(TAG, "BluetoothStateReceiver device is already paired")
            // Met à jour l'état de connexion
            _isConnected.update {
                Log.d(TAG, "BluetoothStateReceiver _isconnected updated to $isConnected")
                isConnected
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "BluetoothStateReceiver can't connect to a non-paired device")
                // Émet une erreur si l'appareil n'est pas appareillé
                _errors.tryEmit("Can't connect to a non-paired device")
            }
        }
    }

    // Met à jour la liste des appareils appariés
    init {
        Log.i(TAG, "init called")
        updatePairedDevices()
        Log.i(TAG, "Register BluetoothStateReceiver")
        // Enregistre le récepteur d'état Bluetooth
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                // Ajoute des actions à écouter pour les changements d'état
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    // Vérifie les permissions pour le scan Bluetooth
    override fun startDiscovery() {
        Log.i(TAG, "startDiscovery called")
        if (!hasPermissions(android.Manifest.permission.BLUETOOTH_SCAN)) {
            // Sort si les permissions ne sont pas accordées
            return
        }
        Log.d(TAG, "register foundDeviceReceiver to context ")
        // Enregistre le récepteur pour les appareils trouvés
        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        // Met à jour la liste des appareils appariés
        updatePairedDevices()
        // Démarre la découverte des appareils
        bluetoothAdapter?.startDiscovery()
    }

    // Vérifie les permissions pour le scan Bluetooth
    override fun stopDiscovery() {
        if (!hasPermissions(android.Manifest.permission.BLUETOOTH_SCAN)) {
            // Sort si les permissions ne sont pas accordées
            return
        }
        // Annule la découverte des appareils
        bluetoothAdapter?.cancelDiscovery()
    }

    // Démarre un serveur Bluetooth et émet des résultats via un flow
    override fun startBluetoothServer(): Flow<Connectionresult> {
        Log.i(TAG, "startBluetoothServer called")
        return flow {
            if (!hasPermissions(android.Manifest.permission.BLUETOOTH_CONNECT)) {
                // Lève une exception si pas de permission BLUETOOTH_CONNECT
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            // Crée le socket serveur Bluetooth avec un service UUID
            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "link_arduino",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    // Accepte les connexions entrantes
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    // Sort de la boucle en cas d'erreur d'acceptation
                    shouldLoop = false
                    null
                }
                // Notifie une connexion établie
                emit(Connectionresult.ConnectionEstablished)
                currentClientSocket?.let {
                    // Ferme le socket serveur après une connexion
                    currentServerSocket?.close()
                    val service = BluetoothDataTransferService(it)
                    dataTransferService = service
                    emitAll(
                        service
                            .listenForIncomingMessages()
                            .map { Connectionresult.TransferSucceeded(it) }
                    )
                }
            }
        }.onCompletion {
            // Ferme la connexion à la fin du flow
            closeConnection()
        }.flowOn(Dispatchers.IO) // Exécute sur un thread IO
    }

    // Connecte à un appareil Bluetooth et émet les résultats via un flow
    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<Connectionresult> {
        Log.i(TAG, "connectToDevice called with $device")
        return flow {
            if (!hasPermissions(android.Manifest.permission.BLUETOOTH_CONNECT)) {
                // Lève une exception si pas de permission BLUETOOTH_CONNECT
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            // Récupère l'appareil distant par adresse
            val bluetoothdevice = bluetoothAdapter?.getRemoteDevice(device.address)

            // Crée un socket client RFCOMM avec le service UUID
            currentClientSocket = bluetoothdevice
                ?.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID))
            // Arrête la découverte des appareils
            stopDiscovery()

            // Vérifie si l'appareil distant n'est pas apparié
            if (bluetoothAdapter?.bondedDevices?.contains(bluetoothdevice) == false) {
                // Logique éventuelle pour non-appariement (non implémentée)
            }

            currentClientSocket?.let { socket ->
                try {
                    // Essaie de connecter le socket client
                    socket.connect()
                    // Émet que la connexion est établie
                    emit(Connectionresult.ConnectionEstablished)

                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map { Connectionresult.TransferSucceeded(it) }
                        )
                    }

                } catch (e: IOException) {
                    // Ferme le socket et reset en cas d'erreur
                    socket.close()
                    currentClientSocket = null
                    // Émet une erreur de connexion interrompue
                    emit(Connectionresult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            // Ferme la connexion à la fin du flow
            closeConnection()
        }.flowOn(Dispatchers.IO) // Exécute sur un thread IO
    }

    // Ferme les connexions clients et serveur Bluetooth
    override fun closeConnection() {
        Log.i(TAG, "closeConnection called")
        currentClientSocket?.close() // Ferme le socket client
        currentServerSocket?.close() // Ferme le socket serveur
        // Réinitialise les sockets à null
        currentClientSocket = null
        currentServerSocket = null
    }

    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if(!hasPermissions(android.Manifest.permission.BLUETOOTH_CONNECT)) {
            return null
        }
        if(dataTransferService == null){
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message,
            senderName = bluetoothAdapter?.name ?: "Unknown name",
            isFromLocalUser = true
        )
        dataTransferService?.sendMessage(bluetoothMessage.toByteArray())
        return bluetoothMessage

    }

    // Libère les récepteurs et ferme la connexion Bluetooth
    override fun release() {
        Log.i(TAG, "release called")
        Log.d(TAG, "unregister foundDeviceReceiver of context")
        context.unregisterReceiver(foundDeviceReceiver) // Désenregistre le récepteur découverte
        Log.d(TAG, "unregister bluetoothStateReceiver of context")
        context.unregisterReceiver(bluetoothStateReceiver) // Désenregistre le récepteur d'état
        closeConnection() // Ferme les connexions ouvertes
    }

    // Met à jour la liste des appareils Bluetooth appariés
    private fun updatePairedDevices() {
        Log.d(TAG, "updatePairedDevices called")
        if (!hasPermissions(android.Manifest.permission.BLUETOOTH_CONNECT)) {
            // Sort si la permission n'est pas accordée
            return
        }
        Log.d(TAG, "updatePairedDevices bonded devices update")
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() } // Convertit les appareils en domaine
            ?.also { devices -> _pairedDevices.update { devices } } // Met à jour la liste
    }

    // Vérifie si la permission spécifiée est accordée
    private fun hasPermissions(permission: String): Boolean {
        Log.d(TAG, "hasPermission for $permission")
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    // UUID du service Bluetooth utilisé pour la communication
    companion object {
        private const val TAG = "BluetoothManager"
        const val SERVICE_UUID = "9a2437a0-f4d5-4a64-8abf-3e3c45ad0293"
    }

}