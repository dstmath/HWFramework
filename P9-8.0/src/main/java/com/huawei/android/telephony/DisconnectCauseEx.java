package com.huawei.android.telephony;

import android.telephony.DisconnectCause;

public class DisconnectCauseEx {
    public static final int BUSY = 4;
    public static final int CALL_FAIL_DESTINATION_OUT_OF_ORDER = 62;
    public static final int CALL_FAIL_NO_ANSWER_FROM_USER = 61;
    public static final int CALL_FAIL_NO_USER_RESPONDING = 105;
    public static final int DIAL_MODIFIED_TO_DIAL = 102;
    public static final int DIAL_MODIFIED_TO_SS = 101;
    public static final int DIAL_MODIFIED_TO_USSD = 100;
    public static final int INCOMING_MISSED = 1;
    public static final int INCOMING_REJECTED = 16;
    public static final int LOCAL = 3;
    public static final int OUT_OF_SERVICE = 18;
    public static final int REQUESTED_CIRCUIT_OR_CHANNEL_NOT_AVAILABLE = 75;
    public static final int RESOURCES_UNAVAILABLE_OR_UNSPECIFIED = 76;
    public static final int SWITCHING_EQUIPMENT_CONGESTION = 73;
    public static final int TEMPORARY_FAILURE = 72;
    public static final int UNOBTAINABLE_NUMBER = 25;

    public static String toString(int cause) {
        return DisconnectCause.toString(cause);
    }
}
