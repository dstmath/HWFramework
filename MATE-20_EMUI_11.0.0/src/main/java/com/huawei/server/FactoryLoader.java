package com.huawei.server;

import android.util.Log;

public class FactoryLoader {
    private static final String TAG = "FactoryLoader";

    private FactoryLoader() {
    }

    public static Object loadFactory(String factoryName) {
        if (factoryName == null) {
            return null;
        }
        try {
            return Class.forName(factoryName).newInstance();
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "Class Not Found Exception" + e.getMessage());
            return null;
        } catch (IllegalAccessException e2) {
            Log.i(TAG, "IllegalAccessException" + e2.getMessage());
            return null;
        } catch (InstantiationException e3) {
            Log.i(TAG, "InstantiationException" + e3.getMessage());
            return null;
        }
    }
}
