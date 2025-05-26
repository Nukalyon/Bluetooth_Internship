package com.example.plugin.model

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION

class BluetoothStateReceiver (
    private val onStateChanged : (isConnect : Boolean, BluetoothDevice) -> Unit
) : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        val device =
            if(VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            }
            else{
                intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }
        when(intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                onStateChanged(true, device ?: return)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                onStateChanged(false, device ?: return)
            }
        }
    }
}