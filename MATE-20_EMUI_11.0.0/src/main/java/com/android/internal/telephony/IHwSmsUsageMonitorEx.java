package com.android.internal.telephony;

public interface IHwSmsUsageMonitorEx {
    default int checkDestinationHw(String destAddress, String countryIso, String simMccmnc) {
        return 0;
    }
}
