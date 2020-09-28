package com.android.internal.telephony;

public class DefaultHwTelephonyChrManager implements HwTelephonyChrManager {
    private static HwTelephonyChrManager sInstance = null;

    public static HwTelephonyChrManager getDefault() {
        if (sInstance == null) {
            sInstance = new DefaultHwTelephonyChrManager();
        }
        return sInstance;
    }
}
