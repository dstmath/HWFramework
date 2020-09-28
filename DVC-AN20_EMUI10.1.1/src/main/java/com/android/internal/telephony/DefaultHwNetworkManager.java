package com.android.internal.telephony;

public class DefaultHwNetworkManager implements HwNetworkManager {
    private static HwNetworkManager sInstance = new DefaultHwNetworkManager();

    public static HwNetworkManager getDefault() {
        return sInstance;
    }
}
