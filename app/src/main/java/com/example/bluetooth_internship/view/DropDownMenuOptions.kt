package com.example.bluetooth_internship.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DropDownMenuOptions(
    onChangeConfig : (Boolean) -> Unit,
    viewModel: DropDownViewModel = DropDownViewModel()
){
    var expanded by remember { mutableStateOf(false) }

    Button(onClick = {expanded = true}) {
        Text(text = viewModel.selectedOption)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {expanded = false}
    ) {
        val options = listOf("Admin", "Client", "Server")
        options.forEach { option ->
            DropdownMenuItem(
                onClick = {
                    viewModel.updateOption(option)
                    expanded = false
                    onChangeConfig(option != "Server")
                },
                text = {Text(option)},
                colors = MenuDefaults.itemColors(textColor = Color.White),
                contentPadding = PaddingValues(10.dp)
            )
        }
    }
}
