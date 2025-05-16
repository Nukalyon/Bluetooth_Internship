package com.example.bluetooth_internship

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission

/***************************************
 *      REF:
 *      https://www.youtube.com/watch?v=_bMK5lwx-as
 ***************************************/

class MainActivity : ComponentActivity() {

    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var bluetoothDevice: Set<BluetoothDevice>
    val REQUEST_ENABLE_BT = 1
    var btPermission : Boolean = false

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        val btnScan = findViewById<Button>(R.id.btn_scan) as Button
        btnScan.setOnClickListener()
        {
            setup()
        }
    }

    private fun setup() {
        if(bluetoothAdapter == null)
        {
            Toast.makeText(applicationContext, "This device doesn't support Bluetooth", Toast.LENGTH_LONG)
            return
        }
        else{
            if(VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                bluetoothPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
            }
            else{
                bluetoothPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_ADMIN)
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
        }
        else{
            //user denied enabling
            Toast.makeText(applicationContext, "User refused to enable Bluetooth", Toast.LENGTH_LONG)
        }
    }
}