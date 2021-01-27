package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothDeviceAdapterEx;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;

public class HwBluetoothDeviceFoundEx {
    private static final int DEFALUT_PARAMETER = 0;
    private static final String TAG = "HwBluetoothDeviceFoundEx";

    public static ArrayList<HwBindDevice> getBindDeviceList() {
        return BluetoothDeviceAdapterEx.getBindDeviceList();
    }

    public static void sendFindDevList(ArrayList<HwFindDevice> devList) {
        if (devList == null) {
            Log.w(TAG, "sendFindDevList: devList is null");
        } else {
            BluetoothDeviceAdapterEx.sendFindDevList(devList);
        }
    }

    public static void startSearch(String deviceId, long searchTime, long reportInterval, JSONObject threshold) {
        if (deviceId == null) {
            Log.w(TAG, "startSearch: device is null");
        } else if (searchTime <= 0 || reportInterval <= 0) {
            Log.w(TAG, "startSearch: invalid parameters");
        } else {
            BluetoothDeviceAdapterEx.startSearch(deviceId, searchTime, reportInterval, threshold);
        }
    }

    public static void controlDevAction(String deviceId, long controlTime, HashMap<Integer, Integer> controlType, JSONObject deviceObject) {
        if (deviceId == null) {
            Log.w(TAG, "controlDevAction: device is null");
        } else if (controlTime <= 0) {
            Log.w(TAG, "controlDevAction: invalid parameters");
        } else {
            BluetoothDeviceAdapterEx.controlDevAction(deviceId, controlTime, controlType, deviceObject);
        }
    }
}
