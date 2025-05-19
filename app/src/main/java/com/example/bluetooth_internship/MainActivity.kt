package com.example.bluetooth_internship

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.IntentCompat

/***************************************
 *      REF:
 *      https://www.youtube.com/watch?v=_bMK5lwx-as
 ***************************************/

class MainActivity : ComponentActivity() {

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
}



