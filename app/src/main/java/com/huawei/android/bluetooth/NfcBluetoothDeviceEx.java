package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import huawei.android.bluetooth.HwBluetoothDeviceEx;

public class NfcBluetoothDeviceEx {
    public static void addNfcPairingWhiteList(BluetoothDevice device, String address) {
        HwBluetoothDeviceEx.getDefault().addNfcPairingWhiteList(device, address);
    }

    public static void removeNfcPairingWhiteList(BluetoothDevice device, String address) {
        HwBluetoothDeviceEx.getDefault().removeNfcPairingWhiteList(device, address);
    }

    public static void clearNfcPairingWhiteList(BluetoothDevice device) {
        HwBluetoothDeviceEx.getDefault().clearNfcPairingWhiteList(device);
    }
}
