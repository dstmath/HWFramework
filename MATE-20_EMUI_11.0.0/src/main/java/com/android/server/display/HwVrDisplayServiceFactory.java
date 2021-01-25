package com.android.server.display;

import android.util.Log;

public class HwVrDisplayServiceFactory {
    private static final String TAG = "HwVrDisplayServiceFactory";
    public static final String VR_FACTORY_IMPL_NAME = "com.android.server.display.HwVrDisplayServiceFactoryImpl";
    private static DefaultHwVrDisplayServiceFactory sFactory;

    private static class Instance {
        private static HwVrDisplayServiceFactory sInstance = new HwVrDisplayServiceFactory();

        private Instance() {
        }
    }

    public static HwVrDisplayServiceFactory getInstance() {
        return Instance.sInstance;
    }

    public static DefaultHwVrDisplayServiceFactory loadFactory() {
        Object mHwVrDisplayFactory = null;
        try {
            mHwVrDisplayFactory = Class.forName(VR_FACTORY_IMPL_NAME).newInstance();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException");
        } catch (InstantiationException e2) {
            Log.e(TAG, "InstantiationException");
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "IllegalAccessException");
        }
        if (mHwVrDisplayFactory instanceof DefaultHwVrDisplayServiceFactory) {
            Log.i(TAG, "load HwVrDisplayServiceFactory from part success!");
            sFactory = (DefaultHwVrDisplayServiceFactory) mHwVrDisplayFactory;
        } else {
            Log.i(TAG, "load DefaultHwVrDisplayServiceFactory!");
            sFactory = DefaultHwVrDisplayServiceFactory.getInstance();
        }
        return sFactory;
    }
}
