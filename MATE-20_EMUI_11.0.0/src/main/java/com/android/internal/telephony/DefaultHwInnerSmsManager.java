package com.android.internal.telephony;

public class DefaultHwInnerSmsManager implements HwInnerSmsManager {
    private static HwInnerSmsManager sInstance;

    public static HwInnerSmsManager getDefault() {
        if (sInstance == null) {
            sInstance = new DefaultHwInnerSmsManager();
        }
        return sInstance;
    }
}
