package com.android.server.usb;

import android.content.Context;
import android.util.Log;

public class HwUsbServiceFactory {
    private static final String TAG = "HwUsbServiceFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        IHwUsbDeviceManager getHuaweiUsbDeviceManager();
    }

    public interface IHwUsbDeviceManager {
        UsbDeviceManager getInstance(Context context, UsbAlsaManager usbAlsaManager, UsbSettingsManager usbSettingsManager);
    }

    private static Factory getImplObject() {
        if (obj == null) {
            synchronized (mLock) {
                if (obj == null) {
                    try {
                        obj = (Factory) Class.forName("com.android.server.usb.HwUsbServiceFactoryImpl").newInstance();
                    } catch (Exception e) {
                        Log.e(TAG, ": reflection exception is " + e);
                    }
                }
            }
            Log.v(TAG, "get allimpl object = " + obj);
        }
        return obj;
    }

    public static IHwUsbDeviceManager getHuaweiUsbDeviceManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiUsbDeviceManager();
        }
        return null;
    }
}
