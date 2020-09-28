package com.android.internal.telephony.vsim;

public class HwPartTelephonyVSimFactoryImpl extends DefaultHwPartTelephonyVSimFactory {
    public DefaultHwVSimUtils getHwVSimUtils() {
        return HwVSimUtilsImpl.getInstance();
    }
}
