package com.android.internal.telephony.timezone;

public class DefaultHwPartTelephonyTimeZoneFactory {
    private static final String TAG = "DefaultHwPartTelephonyTimeZoneFactory";
    private static DefaultHwPartTelephonyTimeZoneFactory sInstance = null;

    public static synchronized DefaultHwPartTelephonyTimeZoneFactory getInstance() {
        DefaultHwPartTelephonyTimeZoneFactory defaultHwPartTelephonyTimeZoneFactory;
        synchronized (DefaultHwPartTelephonyTimeZoneFactory.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwPartTelephonyTimeZoneFactory();
            }
            defaultHwPartTelephonyTimeZoneFactory = sInstance;
        }
        return defaultHwPartTelephonyTimeZoneFactory;
    }

    public DefaultHwTimeZoneManager getHwTimeZoneManager() {
        return DefaultHwTimeZoneManager.getInstance();
    }
}
