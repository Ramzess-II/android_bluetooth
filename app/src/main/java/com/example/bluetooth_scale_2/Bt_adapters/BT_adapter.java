package com.example.bluetooth_scale_2.Bt_adapters;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.bluetooth_scale_2.R;

import java.util.ArrayList;
import java.util.List;

public class BT_adapter extends ArrayAdapter<List_item> { // <> сюда пишем данные которые передаем в адаптер

    public static final String DEF_ITEM_TYPE = "normal";            // define
    public static final String TITLE_ITEM_TYPE = "title";
    public static final String DISCOVERY_ITEM_TYPE = "discovery";
    private final List<List_item> mainList;                          // это копия листа чтоб получать доступ во всем классе
    private final List<VievHolder> listViewHolders;                  // это список элементов которые нужно рисовать
    private SharedPreferences pref;                                  // класс для сохранения данных
    boolean isDiscoveryType;
    private Context context;

    public BT_adapter(@NonNull Context context, int resource, List<List_item> btList) {   // это конструктор чек листа
        super(context, resource, btList);
        this.context = context;
        mainList = btList;
        listViewHolders = new ArrayList<>();
        pref = context.getSharedPreferences(BtCosts.MY_PREF, Context.MODE_PRIVATE);            // контест то что мы передаем + ключ
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {  // переоприделяем метод создания шаблона под себя
        switch (mainList.get(position).getItemType()) {                                   // проверяем какой итем нам рисовать
            case TITLE_ITEM_TYPE:
                convertView = titleItem(convertView, parent);
                break;
            default:
                convertView = defaultItem(convertView, position, parent);
                break;
        }
        return convertView;                                  // функция возвращает наш шаблон а не по умолчанию
    }

    private void savePref(int pos) {                         // функция для сохранения данных в памяти
        SharedPreferences.Editor editor = pref.edit();       // создаем елемент для открытия таблицы на запись
        editor.putString(BtCosts.MAC_NAME, mainList.get(pos).getBtDevice().getAddress());     // сохраняем по ключу значение из mainList адресс устройства
        editor.apply();                                       // сохранить
    }

    static class VievHolder {                                       // это класс для хранения элементов в памяти, которые мы прокрутили
        TextView tvBtName;
        CheckBox chBtSelected;
    }
    private View defaultItem(View convertView, int position, ViewGroup parent) {
        VievHolder vievHolder;              // создаем класс который описан выше
        boolean hasViewHolder = false;
        if (convertView != null) hasViewHolder = (convertView.getTag() instanceof VievHolder);
        // проверяем что находится у нас в convertView. если это объект класса VievHolder то тру
        if (convertView == null || !hasViewHolder) {      // если в convertView пусто, или там другой заголовок то рисуем заново
            vievHolder = new VievHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_itm, null, false);
            // эта конструкция позволяет формировать элементы экскиз которых мы нарисовали в bt_list_item
            vievHolder.tvBtName = convertView.findViewById(R.id.bt_itm);
            // помещаем в клас элемент который мы уже сконвертировали. конкретный элемент tvBtName
            vievHolder.chBtSelected = convertView.findViewById(R.id.checkBox);
            convertView.setTag(vievHolder);   // сохраняем в памяти ссылки на уже имеющиеся элементы
            listViewHolders.add(vievHolder);
        } else {
            vievHolder = (VievHolder) convertView.getTag();  // если в convertView не пусто значет получаем из него данные
            vievHolder.chBtSelected.setChecked(false);       // сносим старую галочку. внизу она снова установиться только в нужное место
        }

        if (mainList.get(position).getItemType().equals((BT_adapter.DISCOVERY_ITEM_TYPE))) {   // проверяем элемент класса чтоб второй параметр был DISCOVERY_ITEM_TYPE
            vievHolder.chBtSelected.setVisibility(View.GONE);                         // спрятать элемент, у нас это chBtSelected
            isDiscoveryType = true;                                                   // и запретить слушатель
        } else {
            vievHolder.chBtSelected.setVisibility(View.VISIBLE);
            isDiscoveryType = false;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        vievHolder.tvBtName.setText(mainList.get(position).getBtDevice().getName());   // тут мы уже заполняем созданный элемент текстом
        vievHolder.chBtSelected.setOnClickListener(new View.OnClickListener() {        // слушатель нажатий на чекбокс
            @Override
            public void onClick(View v) {
                if (!isDiscoveryType) {
                    for (VievHolder holder : listViewHolders) {                              // форич переберает список listViewHolders
                        holder.chBtSelected.setChecked(false);                              // сбрасывает все галочки
                    }
                    vievHolder.chBtSelected.setChecked(true);                               // и устанавливаем только текущую
                    savePref(position);                                                    // сохранить МАС в паямять
                }
            }
        });
        if (pref.getString(BtCosts.MAC_NAME, "no bt selected").equals(mainList.get(position).getBtDevice().getAddress())) {
            vievHolder.chBtSelected.setChecked(true);
        }
        // если сохраненный МАС равен элементу на который нажали get(position) то отмечаем галочкой
        isDiscoveryType = false;
        return convertView;
    }

    private View titleItem(View convertView, ViewGroup parent) {
        boolean hasVievHoler = false;
        if (convertView != null) hasVievHoler = (convertView.getTag() instanceof VievHolder);
        // мы проверяем что у нас в convertView по getTag. если там объект класса VievHolder то перерисовать
        if (convertView == null || hasVievHoler) {           // если в convertView псто то создаем заново
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_item_title, null, false);
            // эта конструкция позволяет формировать элементы экскиз которых мы нарисовали в bt_list_item
        }
        return convertView;
    }
}
