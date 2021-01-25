package com.huawei.android.location;

import android.os.Bundle;

public class HwHigeoManagerEx {
    private static final String TAG = "HwHigeoManagerEx";

    public static boolean sendMmData(int type, Bundle bundle) {
        return HwHigeoManager.getDefault().sendMmData(type, bundle);
    }

    public static boolean sendHigeoData(int type, Bundle bundle) {
        return HwHigeoManager.getDefault().sendHigeoData(type, bundle);
    }

    public static boolean sendCellBatchingData(int type, Bundle bundle) {
        return HwHigeoManager.getDefault().sendCellBatchingData(type, bundle);
    }

    public static int sendWifiFenceData(int type, Bundle bundle) {
        return HwHigeoManager.getDefault().sendWifiFenceData(type, bundle);
    }

    public static boolean sendCellFenceData(int type, Bundle bundle) {
        return HwHigeoManager.getDefault().sendCellFenceData(type, bundle);
    }

    public static int sendGeoFenceData(int type, Bundle bundle) {
        return HwHigeoManager.getDefault().sendGeoFenceData(type, bundle);
    }

    public static boolean registerHigeoCallback(HwHigeoCallbackInterface higeoCallback) {
        return HwHigeoManager.getDefault().registerHigeoCallback(higeoCallback);
    }
}
