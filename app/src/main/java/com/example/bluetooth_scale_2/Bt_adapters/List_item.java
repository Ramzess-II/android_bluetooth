package com.example.bluetooth_scale_2.Bt_adapters;

import android.bluetooth.BluetoothDevice;

public class List_item {
    private BluetoothDevice btDevice;
    private String itemType = BT_adapter.DEF_ITEM_TYPE;

    public BluetoothDevice getBtDevice() {
        return btDevice;
    }

    public void setBtDevice(BluetoothDevice btDevice) {
        this.btDevice = btDevice;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
