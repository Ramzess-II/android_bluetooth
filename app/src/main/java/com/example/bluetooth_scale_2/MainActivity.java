package com.example.bluetooth_scale_2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth_scale_2.Bt_connecting.BT_connect;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private boolean connect_ok = false;
    private int count_tap;
    private MenuItem button_on_bt, button_connect;
    private BT_connect btConnection;
    private BluetoothAdapter btAdapter;   // это стандартный класс
    private SharedPreferences pref;       // класс для сохранения данных
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private Button zero, auto_push, calib, push;
    private String push_string, call_string;
    private TextView display_massa, display_temp, display_overload, display_battery;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        btConnection = new BT_connect(this);                         // создаем класс BtConnection
        timer();
        read_sh ();
        getBtPermission();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = bluetoothManager.getAdapter();
        enableBluetoothLauncher = registerForActivityResult(                // обработка ответов от пользователя
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        button_on_bt.setIcon(R.drawable.baseline_bluetooth_24);// Bluetooth был успешно включен
                        Log.d("MyLog", "BT_ENABLE" );
                    } else {
                        button_on_bt.setIcon(R.drawable.baseline_bluetooth_disabled_24);
                        // Пользователь отменил запрос на включение Bluetooth или произошла ошибка
                    }
                });
        zero.setOnClickListener(v -> {
            showDialog("Виконати обнулення?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {         // Выполнение команды 1
                    String send = "CALZERO";
                    byte [] transfer = send.getBytes(StandardCharsets.UTF_8);
                    if (connect_ok) {
                        btConnection.Get_ConnectThread().Get_ReceiveTread().sendMessageout(transfer);
                    }
                }
            });
        });
        auto_push.setOnClickListener(v -> {
            showDialog("Захопити вагу відпускання?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {         // Выполнение команды 2
                    String send = "CALPUS";
                    byte [] transfer = send.getBytes(StandardCharsets.UTF_8);
                    if (connect_ok) {
                        btConnection.Get_ConnectThread().Get_ReceiveTread().sendMessageout(transfer);
                    }
                }
            });
        });
        calib.setOnClickListener(v -> {
            showDialog("Калібрувати ваги?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {         // Выполнение команды 3
                    String send = "CALM"+ call_string;
                    byte [] transfer = send.getBytes(StandardCharsets.UTF_8);
                    if (connect_ok) {
                        btConnection.Get_ConnectThread().Get_ReceiveTread().sendMessageout(transfer);
                    }
                }
            });
        });
        push.setOnClickListener(v -> {
            showDialog("Калібрувати вагу відпускання?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {         // Выполнение команды 4
                    String send = "CALPH"+ push_string;
                    byte [] transfer = send.getBytes(StandardCharsets.UTF_8);
                    if (connect_ok) {
                        btConnection.Get_ConnectThread().Get_ReceiveTread().sendMessageout(transfer);
                    }
                }
            });
        });
        display_massa.setOnClickListener(v -> {
            count_tap ++;
            if (count_tap == 5){
                count_tap = 0;
                display_overload.setVisibility(View.VISIBLE);
                String send = "GET";
                byte [] transfer = send.getBytes(StandardCharsets.UTF_8);
                if (connect_ok) {
                    btConnection.Get_ConnectThread().Get_ReceiveTread().sendMessageout(transfer);
                }
            }
        });
    }

    private void getBtPermission() {       // проверяем разрешение на местоположение устройства
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // проверяем разрешение на локацию устройства, если нету то бросаем запрос на разрешение включить локализацию
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 123);
        } else {
            //isBtPermission = true;    // а если есть разрешение то ок
        }
    }
    @Override
    // функция которая получает от системы разрешение или отказ в включении местоположения и тд.
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 123) {               // если вернулся код который запрашивали мы
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {         // проверяем ответило согласие или нет
                //isBtPermission = true;                                        // подтверждаем
            } else {
                Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show();   //или же пишем что не подтверждено
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);   // если это не наш запрос то просто ничего не делаем
        }
    }

    private void showDialog(String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("Так", listener);
        builder.setNegativeButton("Ні", null);
        builder.show();
    }

    /*private boolean question_user (String question){
        final boolean flag = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // функция вызова диалогового окна
        builder.setMessage(question);                                       // основная строка
        builder.setPositiveButton("Так", new DialogInterface.OnClickListener() { // если нажали да
            @Override
            public void onClick(DialogInterface dialog, int which) {       // если нажали да
                flag = true;
            }
        });
        builder.setNegativeButton("Ні", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                flag = false;
            }
        });
        builder.show();
        return flag;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                      // создать меню верхнее
        getMenuInflater().inflate(R.menu.main_menu, menu);
        button_on_bt = menu.findItem(R.id.bt_button);
        button_connect = menu.findItem(R.id.bt_connect);
        if (btAdapter.isEnabled()) {
            button_on_bt.setIcon(R.drawable.baseline_bluetooth_24);
        } else {
            button_on_bt.setIcon(R.drawable.baseline_bluetooth_disabled_24);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {         // отслеживаем нажатия на кнопки тул бара
        if (item.getItemId() == R.id.bt_button) {                          // иконка блютуза
            if (!btAdapter.isEnabled()) {
                enable_BT();
            } else {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {}
                    if (connect_ok){
                        connect_ok = false;
                        btConnection.Get_ConnectThread().closeConnection();
                    }
                    btAdapter.disable();
                    button_connect.setIcon(R.drawable.baseline_close_fullscreen_24);
                    button_on_bt.setIcon(R.drawable.baseline_bluetooth_disabled_24);
            }
        } else if (item.getItemId() == R.id.bt_list) {
            if (btAdapter.isEnabled()) {                                                      // пока блютуз не включен не переходить чтоб не получить ошибку
                Intent i = new Intent(MainActivity.this, BtListActivity.class);  // создаем новый интент с сылкой на нужное активити
                startActivity(i);                                                             // запускаем нужное активити
            } else {
                Toast.makeText(this, R.string.bt_off, Toast.LENGTH_LONG).show();       // если не включен показать сообщение
            }
        } else if (item.getItemId() == R.id.bt_connect) {
            if (!connect_ok) {
                if(btAdapter.isEnabled()) {
                    btConnection.connect();
                }
            } else {
                btConnection.Get_ConnectThread().closeConnection();
            }
        } else if (item.getItemId() == R.id.setting) {
            Intent i = new Intent(MainActivity.this, Setting_activity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private void enable_BT (){
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothLauncher.launch(enableBluetoothIntent);          // запросим у пользователя разрешение на вкл бт
    }

    public void timer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
               /* if (bt_on_off) {
                    if (btConnection.Get_ConnectThread().mSocket.isConnected()) {
                        button_connect.setIcon(R.drawable.baseline_close_fullscreen_24_red);
                        connect_ok = true;
                        if (btConnection.Get_ConnectThread().Get_ReceiveTread() != null) {
                            if (btConnection.Get_ConnectThread().Get_ReceiveTread().receive_ok) {
                                btConnection.Get_ConnectThread().Get_ReceiveTread().receive_ok = false;
                                display_temp.setText(btConnection.Get_ConnectThread().Get_ReceiveTread().temp);
                            }
                        } else {
                            button_connect.setIcon(R.drawable.baseline_close_fullscreen_24);
                        }
                    }
                }*/
                if (btConnection != null && btConnection.Get_ConnectThread() != null) {
                    if (btConnection.Get_ConnectThread().mSocket.isConnected()) {
                        connect_ok = true;
                        button_connect.setIcon(R.drawable.baseline_close_fullscreen_24_red);
                        if (btConnection.Get_ConnectThread().Get_ReceiveTread() != null) {
                            if (btConnection.Get_ConnectThread().Get_ReceiveTread().receive_ok) {
                                btConnection.connectThread.receiveT.receive_ok = false;
                                display_temp.setText(btConnection.Get_ConnectThread().Get_ReceiveTread().temp_int + "C°");
                                display_massa.setText(btConnection.Get_ConnectThread().Get_ReceiveTread().mass_int + "kg");
                                display_overload.setText(btConnection.Get_ConnectThread().Get_ReceiveTread().ovr_int + "kg");
                                display_battery.setText(btConnection.Get_ConnectThread().Get_ReceiveTread().bat + "%");
                                if (btConnection.Get_ConnectThread().Get_ReceiveTread().ok_in_mc){
                                    btConnection.Get_ConnectThread().Get_ReceiveTread().ok_in_mc = false;
                                    Toast.makeText(MainActivity.this, R.string.sawed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else {
                        display_massa.setText("------");
                        display_temp.setText("----");
                        display_overload.setText("---");
                        display_battery.setText("----");
                        button_connect.setIcon(R.drawable.baseline_close_fullscreen_24);
                        connect_ok = false;
                    }
                }
                    handler.postDelayed(this, 100);   //задержка потока 100мс
            }
        });
    }
    private void read_sh () {  // считать данные
        push_string = pref.getString(Shared_pref.CALL_PUSH, "10");
        call_string = pref.getString(Shared_pref.CALL_WEIGHT, "50");
    }
    private void init (){
        pref = getSharedPreferences(Shared_pref.MY_PREF, Context.MODE_PRIVATE);  // инициализация настроек SharedPreferences
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.titl_name);
        setSupportActionBar(toolbar);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        zero = findViewById(R.id.zero);
        push = findViewById(R.id.set_push);
        auto_push = findViewById(R.id.button_set_push_auto);
        calib = findViewById(R.id.calib);
        display_massa = findViewById(R.id.textView_massa);
        display_temp = findViewById(R.id.textView_temp);
        display_overload = findViewById(R.id.overload);
        display_battery = findViewById(R.id.text_bat);
    }

    @Override
    protected void onResume() {
        read_sh ();
        super.onResume();
    }
}