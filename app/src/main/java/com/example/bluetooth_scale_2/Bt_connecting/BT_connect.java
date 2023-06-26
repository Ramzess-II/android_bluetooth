package com.example.bluetooth_scale_2.Bt_connecting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetooth_scale_2.Bt_adapters.BtCosts;

public class BT_connect {
    private Context context;
    private SharedPreferences pref;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice device;
    public ConnectThread connectThread;

    public BT_connect(Context context) {                // конструктор класса
        this.context = context;                           // чето там контекст
        pref = context.getSharedPreferences(BtCosts.MY_PREF, Context.MODE_PRIVATE);    // это обьект для работы с памятью
        btAdapter = BluetoothAdapter.getDefaultAdapter();   // объект для работы с блютузом
    }

    public void connect() {
        String mac = pref.getString(BtCosts.MAC_NAME, ""); // считываем по ключу мак адре, если нету то ставим пустоту
        if (mac.isEmpty()) {               // если пустота то выбить ошибку
            Toast.makeText(context, "Девайс не обрано!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!btAdapter.isEnabled()) {           // если блютуз не включен показать ошибку
            Toast.makeText(context, "Блютуз не ввімкнено!", Toast.LENGTH_LONG).show();
            return;
        }
        device = btAdapter.getRemoteDevice(mac);    // подключаемся к устройству
        if (device == null) {                       // если устройство не ответило то опять ошибка
            Toast.makeText(context, "Немає підключення", Toast.LENGTH_LONG).show();
            return;
        }
        connectThread = new ConnectThread(context, btAdapter, device);      // инициализируем класс ConnectThread

        //Log.d("MyLog", "Potok" );
        connectThread.start();          // запускаем второстепенный поток
    }



   /* public void sendMessage(String massege){
        connectThread.getReceiveT().sendMessageout(massege.getBytes());
    }

    public ReceiveTread TransmitTread (){           // эта штука создает проброс между классами, так как у нас есть общая функция, ну или как то так..
        return connectThread.getReceiveT();          //return connectThread.getReceiveT();
    }*/
    public ConnectThread Get_ConnectThread (){           // эта штука создает проброс между классами, так как у нас есть общая функция, ну или как то так..
        return connectThread;                            //return connectThread.getReceiveT();
    }



}
