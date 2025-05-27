package com.example.bluetooth_internship.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun BluetoothScreenServer(
    onChangeConfig : (Boolean) -> Unit,
    onStartServer: () -> Unit
) {
    Column (
        modifier = Modifier.fillMaxSize()
    ){
        DropDownMenuOptions(onChangeConfig, DropDownViewModel())
        Row (
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ){
            Button(onClick = onStartServer) {
                Text(text = "Start Server")
            }
        }
    }
}