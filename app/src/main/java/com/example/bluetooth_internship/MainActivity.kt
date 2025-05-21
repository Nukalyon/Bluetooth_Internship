package com.example.bluetooth_internship

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bluetooth_internship.controller.AndroidBluetoothController
import com.example.bluetooth_internship.ui.theme.Bluetooth_internshipTheme
import com.example.bluetooth_internship.view.BluetoothScreen
import com.example.bluetooth_internship.view.BluetoothView

/***************************************
 *      REF:
 *      https://www.youtube.com/watch?v=_bMK5lwx-as
 ***************************************/
//Bluetooth BLE to use ? low data transfert ?

class MainActivity : ComponentActivity() {

    var TIME_VISIBLE = 60
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

        val makeDeviceVisible = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {/* */ }


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
                        else -> {
                            BluetoothScreen(
                                state = state,
                                onStartScan = view::startScan,
                                onStopScan = view::stopScan,
                                onDeviceClick = view::connectToDevice,
                                onStartServer = view::waitForIncomingConnections
                            )
                        }
                    }
                }
            }

        }
    }

    /*
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    //Time duration of detectability of the device in seconds
    //120 default, 300 max
    var TIME_VISIBLE = 60
    var btPermission : Boolean = false

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        val btnPermission = findViewById<Button>(R.id.btn_permission) as Button
        btnPermission.setOnClickListener()
        {
            setup()
        }

        val btnScan = findViewById<Button>(R.id.btn_scan) as Button
        btnScan.setOnClickListener()
        {
            //Btn click
            scanForDevices()
        }
    }

    private fun setup() {
        if(bluetoothAdapter == null)
        {
            Toast.makeText(applicationContext, "This device doesn't support Bluetooth", Toast.LENGTH_LONG).show()
            return
        }
        else{
            if(VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
            else{
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
    }

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        isGranted : Boolean ->
        if(isGranted)
        {
            btPermission = true
            if(bluetoothAdapter?.isEnabled == false)
            {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                btActivityResultLauncher.launch(enableBtIntent)
            }
            else{
                Toast.makeText(applicationContext, "Bt disabled :(", Toast.LENGTH_LONG)
            }
        }
        else
        {
            btPermission = false
        }
    }

    private val btActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )
    {
        result : ActivityResult ->
        if(result.resultCode == RESULT_OK)
        {
            //User accepted enabling
            Toast.makeText(applicationContext, "User accepted to enable Bluetooth", Toast.LENGTH_LONG)
            btPermission = true
        }
        else{
            //user denied enabling
            Toast.makeText(applicationContext, "User refused to enable Bluetooth", Toast.LENGTH_LONG)
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun scanForDevices()
    {
        //Make the device detectable to everyone for a periode of time
        //if granted
        val visibility = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        visibility.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, TIME_VISIBLE)
        makeDeviceVisible.launch(visibility)
    }

    private val receiver = object : BroadcastReceiver(){

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(c: Context?, i: Intent?) {

            val action: String = intent.action.toString()
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        IntentCompat.getParcelableExtra<BluetoothDevice>(
                            intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    Log.d("SCAN", "Device found : $deviceName\nDevice MAC : $deviceHardwareAddress")
                }
            }
        }
    }

    private val makeDeviceVisible = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )
    {
        result : ActivityResult ->
        if(result.resultCode == RESULT_OK)
        {
            Toast.makeText(applicationContext, "This device is now detectable", Toast.LENGTH_LONG)
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
        }
        else{
            Toast.makeText(applicationContext, "This device isn't detectable", Toast.LENGTH_LONG)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
    */
}