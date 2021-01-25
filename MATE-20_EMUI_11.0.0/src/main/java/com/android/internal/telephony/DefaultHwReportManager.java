package com.android.internal.telephony;

public class DefaultHwReportManager implements HwReportManager {
    private static DefaultHwReportManager sInstance;

    public static HwReportManager getDefault() {
        if (sInstance == null) {
            sInstance = new DefaultHwReportManager();
        }
        return sInstance;
    }
}
