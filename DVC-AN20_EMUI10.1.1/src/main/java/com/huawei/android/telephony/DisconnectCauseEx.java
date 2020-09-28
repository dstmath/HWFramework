package com.huawei.android.telephony;

import android.telephony.DisconnectCause;

public class DisconnectCauseEx {
    public static final int BUSY = 4;
    public static final int CALL_DROP_IWLAN_TO_LTE_UNAVAILABLE = 1047;
    public static final int CALL_FAIL_DESTINATION_OUT_OF_ORDER = 1003;
    public static final int CALL_FAIL_NO_ANSWER_FROM_USER = 1002;
    public static final int CALL_FAIL_NO_USER_RESPONDING = 1046;
    public static final int DIAL_MODIFIED_TO_DIAL = 48;
    public static final int DIAL_MODIFIED_TO_SS = 47;
    public static final int DIAL_MODIFIED_TO_USSD = 46;
    public static final int DISCONNECT_CAUSE_NORMAL = 2;
    public static final int INCOMING_MISSED = 1;
    public static final int INCOMING_REJECTED = 16;
    public static final int LOCAL = 3;
    public static final int OUT_OF_SERVICE = 18;
    public static final int POWER_OFF = 17;
    public static final int REQUESTED_CIRCUIT_OR_CHANNEL_NOT_AVAILABLE = 1016;
    public static final int RESOURCES_UNAVAILABLE_OR_UNSPECIFIED = 1017;
    public static final int SWITCHING_EQUIPMENT_CONGESTION = 1014;
    public static final int TEMPORARY_FAILURE = 1013;
    public static final int UNOBTAINABLE_NUMBER = 25;

    public static String toString(int cause) {
        return DisconnectCause.toString(cause);
    }
}
