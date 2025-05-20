package com.example.bluetooth_internship.controller

sealed interface Connectionresult {
    object ConnectionEstablished : Connectionresult
    data class Error(val message : String) : Connectionresult
}