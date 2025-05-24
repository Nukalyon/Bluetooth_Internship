package com.example.bluetooth_internship.controller

import com.example.bluetooth_internship.model.BluetoothMessage

/***
    Cette interface permet de regrouper les différents états de ConnectionResult
    Etablie -> Très bien, on continue
    Erreur -> Ajoute un message d'erreur adapté
 ***/
sealed interface Connectionresult {
    object ConnectionEstablished : Connectionresult
    data class Error(val message : String) : Connectionresult
    data class TransferSucceeded(val message: BluetoothMessage) : Connectionresult
}