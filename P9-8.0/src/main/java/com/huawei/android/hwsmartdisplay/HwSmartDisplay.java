package com.huawei.android.hwsmartdisplay;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.hwsmartdisplay.IHwSmartDisplayService;
import huawei.android.hwsmartdisplay.IHwSmartDisplayService.Stub;

public class HwSmartDisplay {
    public static final int DISPLAY_EFFECT_ARSR1P = 16;
    public static final int DISPLAY_EFFECT_HIACE = 1;
    public static final int FEATURE_COLOR_ENHANCEMENT = 2;
    public static final int FEATURE_COMFORT = 1;
    private static final String TAG = "HwSmartDisplay";
    private final IHwSmartDisplayService mService = Stub.asInterface(ServiceManager.getService("smartDisplay_service"));

    public boolean isFeatureSupported(int feature) {
        try {
            if (this.mService != null) {
                return this.mService.isFeatureSupported(feature);
            }
            Log.e(TAG, "Instance of smartDisplay_service is null,return false for isFeatureSupported");
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException caught in isComfortFunctionSupported");
            return false;
        }
    }

    public int setDisplayEffectParam(int type, int[] buffer, int length) {
        try {
            if (this.mService != null) {
                return this.mService.setDisplayEffectParam(type, buffer, length);
            }
            Log.e(TAG, "Instance of smartDisplay_service is null,return false for setDisplayEffectParam");
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException caught in setDisplayEffectParam");
            return -1;
        }
    }

    public int getDisplayEffectSupported(int type) {
        try {
            if (this.mService != null) {
                return this.mService.getDisplayEffectSupported(type);
            }
            Log.e(TAG, "Instance of smartDisplay_service is null,return false for getDisplayEffectSupported");
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException caught in getDisplayEffectSupported");
            return 0;
        }
    }
}
