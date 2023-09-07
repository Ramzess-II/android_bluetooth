package com.example.bluetooth_scale_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bluetooth_scale_2.Bt_adapters.BT_adapter;
import com.example.bluetooth_scale_2.Bt_adapters.List_item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BtListActivity extends AppCompatActivity {
    private Context context;
    private boolean isBtPermission = false;
    private Toolbar toolbar;
    private boolean isDiscovery = false;
    private final int BT_REQUEST_PERM = 123;
    private ListView listView;
    private BT_adapter adapter;
    private BluetoothAdapter btAdapter;   // просто создали объект блютуза

    List<List_item> list;                // это создание нового списка

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_list);
        init ();
        context = this;
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    }

    @Override
    protected void onResume() {       // функция которая создается после онкриет и он пауз,
        super.onResume();
        IntentFilter f1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);               // это фильтр когда находим устройство
        IntentFilter f2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);  // когда есть изменения
        IntentFilter f3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); // когда поиск закончен
        registerReceiver(brecive, f1);           // подключаем к ресирверу
        registerReceiver(brecive, f2);           // чтоб мы могли принимать только наши сообщения
        registerReceiver(brecive, f3);
    }

    @Override                         // если мы на паузе, то выключить фильтры, для оптимизации
    protected void onPause() {
        super.onPause();
        unregisterReceiver(brecive);   // выключаем фильтры
    }

    private final BroadcastReceiver brecive = new BroadcastReceiver() {       // приемник информации из системы другими словаими прием intent
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {     // если выполнилось ACTION_FOUND то
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // такой штукой получаем от intent информацию про новый девайс BluetoothDevice.EXTRA_DEVICE
                List_item item = new List_item();          // создаем новый класс
                item.setBtDevice(device);                // записываем в поле BtDevice все параметры девайса
                item.setItemType(BT_adapter.DISCOVERY_ITEM_TYPE);     // а в параметр ItemType записываем новый тип
                list.add(item);                          // добавляем в список наш класс
                adapter.notifyDataSetChanged();          // и говорим чтоб адаптер обновился
                // Toast.makeText(context,"Name" + device.getName(),Toast.LENGTH_LONG).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) { // добавляем еще один слушатель системы когда закончился поиск
                toolbar.setTitle(R.string.to_main);                   // надпись вернуть
                getPairedDevices();                     // функция для получения блютуз устройств из памяти
                isDiscovery = false;                    // сбросить поиск
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {   // если произошли изменения
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  // проверяем удалось ли подключится к устройству
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {            // если наше устройство подключено
                    getPairedDevices();                  // функция для получения блютуз устройств из памяти
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isDiscovery) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {}
                btAdapter.cancelDiscovery();            // сбросить поиск если нажали назад
                isDiscovery = false;                    // сбросим флаг поиска
                getPairedDevices();                     // функция для получения блютуз устройств
            } else {
                finish();                               // закрыть текущую активити
            }
        }
        if (item.getItemId() == R.id.search_bt) {
            if (isDiscovery) return true;
            isDiscovery = true;                                  // запрещаем еще раз нажать поиск
            list.clear();                                        // очищаем список
            List_item itemTitle = new List_item();                 // создаем новый лист, чтоб передать его в адаптер
            itemTitle.setItemType(BT_adapter.TITLE_ITEM_TYPE);    //  в него записываем всего один элемент, меняя стандартную запись
            list.add(itemTitle);                                 // добавляем его в список List<ListItem>
            adapter.notifyDataSetChanged();                      // обновляем адаптер
            btAdapter.startDiscovery();                          // начать поиск устройств по БТ
            toolbar.setTitle(R.string.search_print);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPairedDevices() {          // функция для получения блютуз устройств
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {}
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();    // берем данные из блютуз адаптера
        if (pairedDevices.size() > 0) {
            list.clear();                    // очищаем список, мало ли там была какая то информация
            for (BluetoothDevice device : pairedDevices) {     // это короче фор ич. прогоняет список до конца
                List_item item = new List_item();              // создаем класс ListItem
                item.setBtDevice(device);                      // получаем сразу все данные про БТ девайс
                list.add(item);                                // и добавляем это в список
            }
            adapter.notifyDataSetChanged();                    // обнови адаптер с новыми данными
        }
    }

    @Override
    // функция которая получает от системы разрешение или отказ в включении местоположения и тд.
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == BT_REQUEST_PERM) {               // если вернулся код который запрашивали мы
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {         // проверяем ответило согласие или нет
                isBtPermission = true;                                        // подтверждаем
            } else {
                Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show();   //или же пишем что не подтверждено
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);   // если это не наш запрос то просто ничего не делаем
        }
    }

    private void onItemClickListener() {     // слушатель нажатий на listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // приходит item, элемент на который нажали, позиция и id
                List_item item = (List_item) parent.getItemAtPosition(position);  // полученые данные преобразовываем к ListItem
                if (item.getItemType().equals(BT_adapter.DISCOVERY_ITEM_TYPE)) {       // если в объекте ItemType находится DEF_ITEM_TYPE
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {}
                    item.getBtDevice().createBond();                                   // то мы добавляем устройство в сохраненные на телефоне
                }
            }
        });
    }

    private void getBtPermission() {       // проверяем разрешение на местоположение устройства
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // проверяем разрешение на локацию устройства, если нету то бросаем запрос на разрешение включить локализацию
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, BT_REQUEST_PERM);
        } else {
            isBtPermission = true;    // а если есть разрешение то ок
        }
    }

    void init (){
        toolbar = findViewById(R.id.toolbar);           // подвязали тул бар
        setSupportActionBar(toolbar);                   // включили его
        toolbar.setTitle(R.string.to_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // кнопку назад вывели
        btAdapter = BluetoothAdapter.getDefaultAdapter();   // получаем доступ к блютуз адаптеру (связываем объект и модуль болютуза)
        listView = findViewById(R.id.list_view);            // создаем клас и используем созданный listView
        list = new ArrayList<>();
        adapter = new BT_adapter(this, R.layout.bt_list_itm, list);
        // передаем сюда что печатать будем в этом активити this, шаблон того что печатать bt_list_item, и лист для печати который создали
        listView.setAdapter(adapter);     // после того как создали BtAdapter передаем в созданный класс listView необходимые параметры которые мы переопределил
        getPairedDevices();               // функция для получения блютуз устройств
        getBtPermission();                // разрешение на местоположение устройства
        onItemClickListener();            // запускаем слушатель нажатий на listView
    }
}