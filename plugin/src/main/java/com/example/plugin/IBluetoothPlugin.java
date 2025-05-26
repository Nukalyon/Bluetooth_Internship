package com.example.plugin;

import com.example.plugin.model.BluetoothDevice;

public interface IBluetoothPlugin {
    void startScan();
    void stopScan();
    void connectToDevice(BluetoothDevice device);
    void disconnect();
    void sendMessage(String message);
}
