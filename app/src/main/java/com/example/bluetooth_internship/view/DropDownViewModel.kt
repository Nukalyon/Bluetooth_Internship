package com.example.bluetooth_internship.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class DropDownViewModel : ViewModel() {
    var selectedOption by mutableStateOf("Select an Option")
        private set

    fun updateOption(newOption: String) {
        selectedOption = newOption
    }
}