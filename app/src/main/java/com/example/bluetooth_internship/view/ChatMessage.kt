package com.example.bluetooth_internship.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.example.bluetooth_internship.model.BluetoothMessage
import com.example.bluetooth_internship.ui.theme.Bluetooth_internshipTheme
import com.example.bluetooth_internship.ui.theme.Pink40
import com.example.bluetooth_internship.ui.theme.Purple40

@Composable
fun ChatMessage(
    message: BluetoothMessage,
    modifier: Modifier = Modifier
){
    Column (
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = 15.dp,
                    topEnd = 15.dp,
                    bottomStart = if(message.isFromLocalUser) 15.dp else 0.dp,
                    bottomEnd = if(message.isFromLocalUser) 0.dp else 15.dp
                )
            ).background(
                if(message.isFromLocalUser) Purple40 else Pink40
            )
            .padding(16.dp)
    ){
        Text(
            text = message.senderName,
            fontSize = 10.sp,
            color = Color.Black
        )
        Text(
            text = message.message,
            color = Color.Black,
            modifier = Modifier.widthIn(max= 250.dp)
        )
    }
}

@Preview
@Composable
private fun ChatMessagePreview() {
    Bluetooth_internshipTheme {
        ChatMessage(
            message = BluetoothMessage(
                message = "Hello World",
                senderName = "Jérémy",
                isFromLocalUser = true
            )
        )
    }
}