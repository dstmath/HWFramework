package com.android.server.devicepolicy;

import android.content.Context;
import android.util.Log;

public class HwDevicePolicyFactory {
    private static final String TAG = "HwDevicePolicyFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        IHwDevicePolicyManagerService getHuaweiDevicePolicyManagerService();
    }

    public interface IHwDevicePolicyManagerService {
        DevicePolicyManagerService getInstance(Context context);
    }

    private static Factory getImplObject() {
        if (obj == null) {
            synchronized (mLock) {
                if (obj == null) {
                    try {
                        obj = (Factory) Class.forName("com.android.server.devicepolicy.HwDevicePolicyFactoryImpl").newInstance();
                    } catch (Exception e) {
                        Log.e(TAG, ": reflection exception is " + e);
                    }
                }
            }
            Log.v(TAG, "get allimpl object = " + obj);
        }
        return obj;
    }

    public static IHwDevicePolicyManagerService getHuaweiDevicePolicyManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiDevicePolicyManagerService();
        }
        return null;
    }
}
