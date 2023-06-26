package com.example.bluetooth_scale_2.Bt_connecting;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

public class ReceiveTread extends Thread {
    private int size_data = 0;
    public boolean ok_in_mc = false;
    public int temp_int = 0;
    public int mass_int = 0;
    public int ovr_int = 1;
    public int bat = 0;
    private int head = 0;
    public byte[] buffer_pars = new byte[50];
    public boolean receive_ok = false;
    private boolean parsing_ok = false;
    public BluetoothSocket socket;
    private InputStream inputS;
    private OutputStream outS;

    public StringBuilder mirror =  new StringBuilder();
    public String temporary = "temp";

    public ReceiveTread(BluetoothSocket socket) {    // конструктор класса который позволяет
        this.socket = socket;
        try {
            inputS = socket.getInputStream();
        } catch (IOException e) {
        }
        try {
            outS = socket.getOutputStream();
        } catch (IOException e) {
        }
    }

    private void pars (){
        int temp_cnt = 1;
        while(true){
            if(buffer_pars[temp_cnt - 1] == 'T') {
                if (buffer_pars[temp_cnt] == '=') {
                    for (int i = 0; i < 3; i++) {
                        mirror.append((char) buffer_pars[temp_cnt + i]);
                    }
                    try {
                        mirror.delete(0, 1);
                        //mirror.append('C');
                        //mirror.append('°');
                        temporary = mirror.toString();
                        temp_int = Integer.parseInt(temporary);
                        mirror.setLength(0);
                    }
                    catch (NumberFormatException e) {
                        Log.d("MyLog", "err");
                    }
                }
            }
            if (buffer_pars[temp_cnt - 1] == 'M') {
                if (buffer_pars[temp_cnt] == '=') {
                    for (int i = 0; i < 7; i++) {
                        mirror.append((char) buffer_pars[temp_cnt + i]);
                    }
                    if (buffer_pars[temp_cnt+1] == ' ') mirror.delete(0, 2);
                    else mirror.delete(0, 1);
                    if (buffer_pars[temp_cnt+1] == ' ')mirror.delete(3, 8);
                    else  mirror.delete(4, 7);
                    try {
                        temporary = mirror.toString();
                        mass_int = Integer.parseInt(temporary);
                        mirror.setLength(0);
                    }
                    catch (NumberFormatException e) {
                        Log.d("MyLog", "err");
                    }
                }
            }
            if (buffer_pars[temp_cnt - 1] == 'B') {
                if (buffer_pars[temp_cnt] == '=') {
                    for (int i = 0; i < 4; i++) {
                        mirror.append((char) buffer_pars[temp_cnt + i]);
                    }
                    mirror.delete(0, 1);
                    try {
                        temporary = mirror.toString();
                        bat = Integer.parseInt(temporary);
                        mirror.setLength(0);
                    }
                    catch (NumberFormatException e) {
                        Log.d("MyLog", "err");
                    }
                }
            }
            if (buffer_pars[temp_cnt - 1] == 'O') {
                if (buffer_pars[temp_cnt] == 'V') {
                    for (int i = 0; i < 7; i++) {
                        mirror.append((char) buffer_pars[temp_cnt + i]);
                    }
                    mirror.delete(0, 2);
                    mirror.delete(3, 6);
                    try {
                    temporary = mirror.toString();
                    ovr_int = Integer.parseInt(temporary);
                    mirror.setLength(0);
                    }
                    catch (NumberFormatException e) {
                        Log.d("MyLog", "err");
                    }
                }
            }
            if (buffer_pars[temp_cnt - 1] == 'O') {
                if (buffer_pars[temp_cnt] == 'K') {
                    ok_in_mc = true;
                }
            }
            temp_cnt ++;
            if(temp_cnt == 50) {
                receive_ok = true;
                for (int i = 0; i < 50; i ++){
                    buffer_pars[i] = 0;
                }
                break;
            }
        }
    }


    @Override
    public void run() {
        Log.d("MyLog", "while");                          // запустилось.
        while (true) {
            try {
                byte[] buffer = new byte[50];
                int size = inputS.read(buffer);
                for (int i = 0; i < size; i ++){
                    if (buffer[i] == 0x02) head = 0;
                    if (buffer[i] == 0x0D) parsing_ok = true;
                    buffer_pars[head] =  buffer[i];
                    if (head < 50) head ++;
                    else head = 0;
                }
                if (parsing_ok){
                    parsing_ok = false;
                    pars ();
                }
                /*if (buffer_pars[0] == head) {
                    head = 0;
                    temp = mass.toString();
                    receive_ok = true;
                }*/



                /*String message = new String(buffer, 0, size);
                receivedData.append(message);
                mass = receivedData.toString();
                if (mass.length() > 30){
                    receive_ok = true;

                }*/
                /*byte[] buffer = new byte[100];
                int size = inputS.read(buffer);
                if (size != -1) {
                    String message = new String(buffer, 0, size);
                    receivedData.append(message);
                    mass = receivedData.toString();
                    receive_ok = true;
                }*/


                Log.d("MyLog", "New data");                          // запустилось.
            } catch (IOException e) {
                try {
                    socket.close();
                    Log.d("MyLog", "close");                          // запустилось.
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    Log.d("MyLog", "ERR");                          // не запустилось.
                }
                socket = null;
                break;
            }
        }
    }

    public void sendMessageout(byte[] byteArray) {
        try {
            outS.write(byteArray);
            Log.d("MyLog", "Transmit");                               // запустилось.
        } catch (IOException e) {
            Log.d("MyLog", "No connection");                          // запустилось.
        }
    }

    public boolean socket_ok() {
        if (socket.isConnected()) {
            return true;
        }
        return false;
    }

}
