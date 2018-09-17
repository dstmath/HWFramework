package com.android.internal.telephony.imsphone;

public interface CallFailCause {
    public static final int ACM_LIMIT_EXCEEDED = 68;
    public static final int BEARER_NOT_AVAIL = 58;
    public static final int CALL_BARRED = 240;
    public static final int CHANNEL_NOT_AVAIL = 44;
    public static final int ERROR_UNSPECIFIED = 65535;
    public static final int FDN_BLOCKED = 241;
    public static final int IMS_BUSY_HERE_486 = 18918;
    public static final int IMS_DECLINE_603 = 19035;
    public static final int IMS_ERROR_BASE = 18432;
    public static final int IMS_NORMAL = 0;
    public static final int IMS_NOT_FOUND_404 = 18836;
    public static final int IMS_PRECOND_FAIL_580 = 19012;
    public static final int IMS_REQ_TIME_OUT_408 = 18840;
    public static final int IMS_SERVER_INTERNAL_ERROR_500 = 18932;
    public static final int IMS_SERVICE_UNAVAIL_503 = 18935;
    public static final int IMS_TEMP_UNAVAIL_480 = 18912;
    public static final int NORMAL_CLEARING = 16;
    public static final int NORMAL_UNSPECIFIED = 31;
    public static final int NO_CIRCUIT_AVAIL = 34;
    public static final int NUMBER_CHANGED = 22;
    public static final int QOS_NOT_AVAIL = 49;
    public static final int STATUS_ENQUIRY = 30;
    public static final int SWITCHING_CONGESTION = 42;
    public static final int TEMPORARY_FAILURE = 41;
    public static final int UNOBTAINABLE_NUMBER = 1;
    public static final int USER_BUSY = 17;
}
