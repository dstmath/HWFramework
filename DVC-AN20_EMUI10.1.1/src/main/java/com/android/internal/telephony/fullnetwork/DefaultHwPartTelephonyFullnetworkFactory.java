package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.DefaultHwHotplugController;

public class DefaultHwPartTelephonyFullnetworkFactory {
    private static final String TAG = "DefaultHwPartTelephonyFullnetworkFactory";
    private static DefaultHwPartTelephonyFullnetworkFactory sInstance = null;

    public static synchronized DefaultHwPartTelephonyFullnetworkFactory getInstance() {
        DefaultHwPartTelephonyFullnetworkFactory defaultHwPartTelephonyFullnetworkFactory;
        synchronized (DefaultHwPartTelephonyFullnetworkFactory.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwPartTelephonyFullnetworkFactory();
            }
            defaultHwPartTelephonyFullnetworkFactory = sInstance;
        }
        return defaultHwPartTelephonyFullnetworkFactory;
    }

    public DefaultHwFullNetworkManager getHwFullnetworkManager() {
        return DefaultHwFullNetworkManager.getInstance();
    }

    public DefaultHwHotplugController getHotplugController() {
        return DefaultHwHotplugController.getInstance();
    }
}
