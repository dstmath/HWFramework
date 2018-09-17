package com.huawei.android.telephony;

import android.os.Bundle;
import android.telephony.ServiceState;

public class ServiceStateEx {
    public static final int RADIO_TECHNOLOGY_1xRTT = 6;
    public static final int RADIO_TECHNOLOGY_DCHSPAP = 30;
    public static final int RADIO_TECHNOLOGY_EDGE = 2;
    public static final int RADIO_TECHNOLOGY_EHRPD = 13;
    public static final int RADIO_TECHNOLOGY_EVDO_0 = 7;
    public static final int RADIO_TECHNOLOGY_EVDO_A = 8;
    public static final int RADIO_TECHNOLOGY_EVDO_B = 12;
    public static final int RADIO_TECHNOLOGY_GPRS = 1;
    public static final int RADIO_TECHNOLOGY_GSM = 16;
    public static final int RADIO_TECHNOLOGY_HSDPA = 9;
    public static final int RADIO_TECHNOLOGY_HSPA = 11;
    public static final int RADIO_TECHNOLOGY_HSPAP = 15;
    public static final int RADIO_TECHNOLOGY_HSUPA = 10;
    public static final int RADIO_TECHNOLOGY_IS95A = 4;
    public static final int RADIO_TECHNOLOGY_IS95B = 5;
    public static final int RADIO_TECHNOLOGY_IWLAN = 18;
    public static final int RADIO_TECHNOLOGY_LTE = 14;
    public static final int RADIO_TECHNOLOGY_LTE_CA = 19;
    public static final int RADIO_TECHNOLOGY_TD_SCDMA = 17;
    public static final int RADIO_TECHNOLOGY_UMTS = 3;
    public static final int RADIO_TECHNOLOGY_UNKNOWN = 0;

    public static int getDataState(ServiceState object) {
        return object.getDataRegState();
    }

    public static boolean isEmergencyOnly(ServiceState serviceState) {
        return serviceState != null ? serviceState.isEmergencyOnly() : false;
    }

    public static ServiceState newFromBundle(Bundle m) {
        return ServiceState.newFromBundle(m);
    }
}
