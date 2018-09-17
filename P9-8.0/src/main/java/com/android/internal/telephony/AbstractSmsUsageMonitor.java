package com.android.internal.telephony;

public abstract class AbstractSmsUsageMonitor {
    protected int checkDestinationHw(String destAddress, String countryIso, String simMccmnc) {
        return 0;
    }
}
