package com.huawei.iconnect.config.btconfig.condition;

import com.huawei.iconnect.wearable.config.BluetoothDeviceData;

public abstract class AbsCondition {
    public abstract boolean isMatch(BluetoothDeviceData bluetoothDeviceData);
}
