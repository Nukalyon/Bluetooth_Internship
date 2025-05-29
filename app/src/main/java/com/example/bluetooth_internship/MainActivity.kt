package com.example.bluetooth_internship

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bluetooth_internship.controller.AndroidBluetoothController
import com.example.bluetooth_internship.ui.theme.Bluetooth_internshipTheme
import com.example.bluetooth_internship.view.BluetoothScreenClient
import com.example.bluetooth_internship.view.BluetoothScreenServer
import com.example.bluetooth_internship.view.BluetoothUiState
import com.example.bluetooth_internship.view.BluetoothView
import com.example.bluetooth_internship.view.ClientConnectedScreen
import com.example.bluetooth_internship.view.ServerConnectedScreen

/***************************************
 *      REF:
 *      https://www.youtube.com/watch?v=_bMK5lwx-as
 ***************************************/
//Bluetooth BLE to use ? low data transfert ?

class MainActivity : ComponentActivity() {

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private val isBluetoothEnabled : Boolean
        get() = bluetoothAdapter?.isEnabled == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ /* Not needed ?*/}


        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){
                perms ->
            val canEnableBluetooth =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    perms[android.Manifest.permission.BLUETOOTH_CONNECT] == true
                } else { true }
            if(canEnableBluetooth && !isBluetoothEnabled){
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }

        /*
        var TIME_VISIBLE = 60
        val makeDeviceVisible = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {/* */ }

        //Make the device detectable to everyone for a periode of time
        //if granted
        val visibility = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        visibility.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, TIME_VISIBLE)
        makeDeviceVisible.launch(visibility)
        */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }

        setContent{
            Bluetooth_internshipTheme {
                val btController = AndroidBluetoothController(applicationContext)
                val view = BluetoothView(btController)
                val state = view.state.collectAsState()

                /*
                LaunchedEffect(key1 = state.value.errorMessage) {
                    state.value.errorMessage?.let { message ->
                        Toast.makeText(
                            applicationContext,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                LaunchedEffect(key1 = state.value.isConnected) {
                    if(state.value.isConnected)
                    {
                        Toast.makeText(
                            applicationContext,
                            "You're connected !",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }*/

                Surface(
                    color = MaterialTheme.colorScheme.background
                ){
                    when{
                        state.value.isConnecting ->
                        {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ){
                                CircularProgressIndicator()
                                Text(text = "Connecting ...")
                            }
                        }
                        state.value.isConnected ->{
                            when{
                                state.value.isClient ->{
                                    ClientConnectedScreen(
                                        onDisconnect = view::disconnectFromDevice,
                                        onSendMessage = view::sendMessage
                                    )
                                }
                                else -> {
                                    ServerConnectedScreen(
                                        state = state.value,
                                        onDisconnect = view::disconnectFromDevice
                                    )
                                }
                            }
                            /*ChatScreen(
                                state = state.value,
                                onDisconnect = view::disconnectFromDevice,
                                onSendMessage = view::sendMessage
                            )
                            */
                        }
                        else -> {
                            when{
                                state.value.isClient ->{
                                    BluetoothScreenClient(
                                        state = state,
                                        onStartScan = view::startScan,
                                        onStopScan = view::stopScan,
                                        onDeviceClick = view::connectToDevice,
                                        onChangeConfig = view::changeConfig
                                    )
                                }
                                else ->{
                                    BluetoothScreenServer (
                                        onChangeConfig = view::changeConfig,
                                        onStartServer = view::waitForIncomingConnections
                                    )
                                }
                            }
                            /*
                            BluetoothScreenAdmin(
                                state = state,
                                onStartScan = view::startScan,
                                onStopScan = view::stopScan,
                                onDeviceClick = view::connectToDevice,
                                onStartServer = view::waitForIncomingConnections,
                                onChangeConfig = view::changeConfig
                            )
                            */
                        }
                    }
                }
            }

        }
    }
}