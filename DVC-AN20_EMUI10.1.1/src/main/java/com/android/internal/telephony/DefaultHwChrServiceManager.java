package com.android.internal.telephony;

public class DefaultHwChrServiceManager implements HwChrServiceManager {
    private static HwChrServiceManager sInstance = null;

    public static HwChrServiceManager getDefault() {
        if (sInstance == null) {
            sInstance = new DefaultHwChrServiceManager();
        }
        return sInstance;
    }
}
