package com.example.bluetooth_internship.model

import android.bluetooth.BluetoothSocket
import com.example.bluetooth_internship.controller.toBluetoothMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(
    private val socket : BluetoothSocket
) {
    fun listenForIncomingMessages() : Flow<BluetoothMessage>{
        return flow{
            if(!socket.isConnected)
            {
               return@flow
            }
            val buffer = ByteArray(1024)
            while (true){
                val byteCount = try {
                    socket.inputStream.read(buffer)
                }
                catch (e: IOException){
                    throw TransferDataFailedException(e)
                }

                emit(
                    buffer.decodeToString(
                        endIndex = byteCount
                    ).toBluetoothMessage(
                        isFromLocalUser = false
                )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(bytes : ByteArray) : Boolean{
        return withContext (Dispatchers.IO){
            try {
                socket.outputStream.write(bytes)
            }
            catch (e: IOException){
                e.printStackTrace()
                return@withContext false
            }

            true
        }
    }
}

class TransferDataFailedException (exc : IOException): IOException("Reading incoming data failed\nDetails:\n${exc.printStackTrace()}")