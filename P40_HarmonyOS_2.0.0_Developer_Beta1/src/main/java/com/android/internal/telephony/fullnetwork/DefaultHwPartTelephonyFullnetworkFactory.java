package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.DefaultHwHotplugController;
import com.android.internal.telephony.DefaultHwSubscriptionManager;
import com.android.internal.telephony.DefaultInCallDataStateManager;

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

    public DefaultHwSubscriptionManager getHwSubscriptionManager() {
        return DefaultHwSubscriptionManager.getInstance();
    }

    public DefaultHwHotplugController getHotplugController() {
        return DefaultHwHotplugController.getInstance();
    }

    public DefaultInCallDataStateManager getInCallDataStateManager() {
        return DefaultInCallDataStateManager.getInstance();
    }
}
