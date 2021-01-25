package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothAdapterEx;
import android.bluetooth.BluetoothBleAgentEx;
import android.os.Parcel;
import android.util.Log;
import java.util.LinkedList;

public class BluetoothBleAgent {
    private static final int CODE_UPDATE_BLE_AGENT_INFORMATION = 1009;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetooth";
    private static final String TAG = "BluetoothBleAgent";
    private static final int UPDATE_FAIL = 1;
    private static final int UPDATE_SUCCESS = 0;

    public int updateBleAgentInformationList(LinkedList<BleAgentRegisterInformation> information) {
        if (information == null) {
            Log.e(TAG, "updateBleAgentInformationList information is null");
            return 1;
        }
        int size = information.size();
        for (int i = 0; i < size; i++) {
            if (updateBleAgentInformation(information.get(i)) != 0) {
                Log.e(TAG, "updateBleAgentInformationList update fail");
                return 1;
            }
        }
        return 0;
    }

    private int updateBleAgentInformation(BleAgentRegisterInformation information) {
        Log.d(TAG, "updateBleAgentInformation");
        if (information == null) {
            Log.e(TAG, "updateBleAgentInformation information is null");
            return 1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(DESCRIPTOR);
        information.writeToParcel(data, 0);
        return BluetoothBleAgentEx.updateBleAgentInfo(CODE_UPDATE_BLE_AGENT_INFORMATION, data, reply) == 1 ? 0 : 1;
    }

    public int getRemainFilterNum() {
        Log.d(TAG, "getRemainFilterNum");
        return BluetoothAdapterEx.getRemainFilterNum();
    }
}
