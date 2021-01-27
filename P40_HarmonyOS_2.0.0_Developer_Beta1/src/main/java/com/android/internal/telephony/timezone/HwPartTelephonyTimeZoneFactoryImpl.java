package com.android.internal.telephony.timezone;

public class HwPartTelephonyTimeZoneFactoryImpl extends DefaultHwPartTelephonyTimeZoneFactory {
    public DefaultHwTimeZoneManager getHwTimeZoneManager() {
        return HwTimeZoneManagerImpl.getInstance();
    }
}
