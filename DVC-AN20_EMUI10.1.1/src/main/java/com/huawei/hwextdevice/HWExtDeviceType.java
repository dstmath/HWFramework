package com.huawei.hwextdevice;

public class HWExtDeviceType {
    public static final int TYPE_MOTION_DETECTION = 1;
    private static int[] mDevicesType = null;

    public static int[] getHWExtDevieTyps() {
        if (mDevicesType == null) {
            mDevicesType = new int[]{1};
        }
        return (int[]) mDevicesType.clone();
    }
}
