package com.huawei.server.util;

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
            Log.i(TAG, "loadFactory() ClassNotFoundException !");
            return null;
        } catch (InstantiationException e2) {
            Log.e(TAG, "loadFactory() InstantiationException !");
            return null;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "loadFactory() IllegalAccessException !");
            return null;
        } catch (Exception e4) {
            Log.e(TAG, "loadFactory() Exception !");
            return null;
        }
    }
}
