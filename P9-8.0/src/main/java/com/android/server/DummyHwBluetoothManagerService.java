package com.android.server;

import android.content.Context;
import com.android.server.HwServiceFactory.IHwBluetoothManagerService;

public class DummyHwBluetoothManagerService implements IHwBluetoothManagerService {
    private static final String TAG = "DummyHwBluetoothManagerService";
    private static IHwBluetoothManagerService mInstance = new DummyHwBluetoothManagerService();

    public static IHwBluetoothManagerService getDefault() {
        return mInstance;
    }

    public BluetoothManagerService createHwBluetoothManagerService(Context context) {
        return new BluetoothManagerService(context);
    }
}
