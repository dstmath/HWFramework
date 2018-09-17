package com.huawei.hwextdevice;

import android.util.Log;

public class HWExtDeviceData implements Cloneable {
    public HWExtDeviceData cloneDeep() {
        return (HWExtDeviceData) clone();
    }

    public Object clone() {
        HWExtDeviceData deviceData = null;
        try {
            return (HWExtDeviceData) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e("HWExtDeviceData", "clone not support");
            return deviceData;
        }
    }
}
