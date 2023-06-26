package com.example.bluetooth_scale_2.Bt_connecting;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class ConnectThread extends Thread {
    private BT_connect btConnection;
    private Context context;
    private BluetoothAdapter btAdapter;
    public BluetoothSocket mSocket;
    public ReceiveTread receiveT;
    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB"; // индетификатор для соеденения
    public boolean stap = false;

    public ConnectThread(Context context, BluetoothAdapter btAdapter, BluetoothDevice device) {
        this.btAdapter = btAdapter;
        this.context = context;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {}
            mSocket = device.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID)); // преобразовываем индетификатор для подключения
        } catch (IOException e) {
        }
    }
    @Override
    public void run() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {}
        btAdapter.cancelDiscovery();
        Log.d("MyLog", "Potok");                          // запустилось.
        try {
            mSocket.connect();
            receiveT = new ReceiveTread(mSocket);
            receiveT.start();
            Log.d("MyLog", "Connect");                          // запустилось.
        } catch (IOException e) {
            Log.d("MyLog", "No Connect");                        // ошибка.
            closeConnection();
            //btConnection.Delete();
        }
    }

    public ReceiveTread Get_ReceiveTread (){           // эта штука создает проброс между классами, так как у нас есть общая функция, ну или как то так..
        return receiveT;                            //return connectThread.getReceiveT();
    }

    public void closeConnection() {
        try {
            mSocket.close();                                               // закрыть сокет
        } catch (IOException y) {
        }
    }
}
