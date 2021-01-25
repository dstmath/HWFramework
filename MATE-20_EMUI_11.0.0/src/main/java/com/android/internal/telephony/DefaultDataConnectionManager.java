package com.android.internal.telephony;

public class DefaultDataConnectionManager implements HwDataConnectionManager {
    private static HwDataConnectionManager sInstance = new DefaultDataConnectionManager();

    public static HwDataConnectionManager getDefault() {
        return sInstance;
    }
}
