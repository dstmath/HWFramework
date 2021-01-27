package com.huawei.dfr;

import android.util.Log;

public class HwDFRFrameworkFactory {
    private static final String TAG = "HwDFRFrameworkFactory";

    private static Object loadFactory(String factoryName) {
        if (factoryName == null) {
            return null;
        }
        try {
            return Class.forName(factoryName).newInstance();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class Not found Exception");
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "Illegal access exception");
            return null;
        } catch (InstantiationException e3) {
            Log.e(TAG, "Instantiation exception");
            return null;
        }
    }

    public static DefaultZrHungFrameworkFactory getZrHungFrameworkFactory() {
        DefaultZrHungFrameworkFactory factory = (DefaultZrHungFrameworkFactory) loadFactory("android.zrhung.ZrHungFrameworkFactory");
        if (factory != null) {
            return factory;
        }
        Log.i(TAG, "No ZrHung framework implements exists.");
        return DefaultZrHungFrameworkFactory.getFactory();
    }

    public static DefaultRMSFrameworkFactory getRMSFrameworkFactory() {
        DefaultRMSFrameworkFactory factory = (DefaultRMSFrameworkFactory) loadFactory("android.rms.RMSFrameworkFactory");
        if (factory != null) {
            return factory;
        }
        Log.i(TAG, "No RMS framework implements exists");
        return DefaultRMSFrameworkFactory.getInstance();
    }
}
