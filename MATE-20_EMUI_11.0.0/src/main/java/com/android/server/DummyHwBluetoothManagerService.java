package com.android.server;

import android.content.Context;
import com.android.server.HwServiceFactory;

public class DummyHwBluetoothManagerService implements HwServiceFactory.IHwBluetoothManagerService {
    private static final String TAG = "DummyHwBluetoothManagerService";
    private static HwServiceFactory.IHwBluetoothManagerService mInstance = new DummyHwBluetoothManagerService();

    public static HwServiceFactory.IHwBluetoothManagerService getDefault() {
        return mInstance;
    }

    @Override // com.android.server.HwServiceFactory.IHwBluetoothManagerService
    public BluetoothManagerService createHwBluetoothManagerService(Context context) {
        return new BluetoothManagerService(context);
    }
}
