package com.example.bluetooth_scale_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth_scale_2.Bt_adapters.BT_adapter;
import com.example.bluetooth_scale_2.Bt_adapters.List_item;


public class Setting_activity extends AppCompatActivity {
    private SharedPreferences pref;       // класс для сохранения данных
    private Toolbar toolbar;
    private EditText edd_cal, edd_push;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init ();
        read_sh ();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.save_setting) {
            SharedPreferences.Editor editor = pref.edit();                                          // создаем елемент для открытия таблицы на запись
            editor.putString(Shared_pref.CALL_WEIGHT, edd_cal.getText().toString());                // сохраняем по ключу значение
            editor.putString(Shared_pref.CALL_PUSH, edd_push.getText().toString());                 // сохраняем по ключу значение
            editor.apply();                                                                         // применить
            Toast.makeText(Setting_activity.this,R.string.sawed, Toast.LENGTH_SHORT).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void init (){
        pref = getSharedPreferences(Shared_pref.MY_PREF, Context.MODE_PRIVATE);  // инициализация настроек SharedPreferences
        edd_cal = findViewById(R.id.ed_mas_cal);
        edd_push = findViewById(R.id.ed_mass_push);
        toolbar = findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);                            // включили его // appcompat.widget.Toolbar
        toolbar.setTitle(R.string.to_main);
        toolbar.setTitleTextColor( getResources().getColor(R.color.black));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // кнопку назад вывели
        Drawable drawable = toolbar.getNavigationIcon();
        drawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationIcon(drawable);
    }

    private void read_sh () {  // считать данные
        edd_cal.setText(pref.getString(Shared_pref.CALL_WEIGHT, "10"));
        edd_push.setText(pref.getString(Shared_pref.CALL_PUSH, "50"));
    }
}