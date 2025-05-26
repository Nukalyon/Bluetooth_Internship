package com.example.plugin.model

import android.bluetooth.BluetoothSocket
import com.example.plugin.controller.toBluetoothMessage
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

class TransferDataFailedException (
    stack : IOException
): IOException("Reading incoming data failed\n${stack.printStackTrace()}")