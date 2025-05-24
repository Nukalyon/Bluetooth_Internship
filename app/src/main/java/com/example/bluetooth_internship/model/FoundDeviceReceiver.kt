package com.example.bluetooth_internship.model

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION


class FoundDeviceReceiver (
    private val onDeviceFound : (BluetoothDevice) -> Unit
) : BroadcastReceiver(){

    // onReceive est appellé lorsque on commence la discovery dans le AndroidBluetoothController
    override fun onReceive(context: Context?, intent: Intent?) {
        // Check de l'action de l'Intent
        when(intent?.action) {
            // Si l'action est qu'on trouve un BluetoothDevice
            BluetoothDevice.ACTION_FOUND -> {
                // On récupère les infos de l'appareil
                val device =
                    // Check pour savoir si on appelle une version deprecated de la méthode
                    if(VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    }
                    else{
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                device?.let {
                    // Trigger l'event qu'on a trouvé un appareil (it)
                    onDeviceFound(it)
                }
            }
        }
    }
}