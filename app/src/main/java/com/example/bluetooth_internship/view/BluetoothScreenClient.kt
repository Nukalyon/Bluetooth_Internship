package com.example.bluetooth_internship.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.example.bluetooth_internship.model.BluetoothDevice


@Composable
fun BluetoothScreenClient(
    state: State<BluetoothUiState>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit,
    onChangeConfig : (Boolean) -> Unit
) {
    Column (
        modifier = Modifier.fillMaxSize()
    ){
        DropDownMenuOptions(onChangeConfig, DropDownViewModel())
        BluetoothDeviceList(
            pairedDevices = state.value.pairedDevices,
            scannedDevices = state.value.scannedDevices,
            onClick = onDeviceClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ){
            Button(onClick = onStartScan) {
                Text(text = "Start Scan")
            }
            Button(onClick = onStopScan) {
                Text(text = "Stop Scan")
            }
        }
    }
}