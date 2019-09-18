package com.huawei.internal.telephony;

public interface PhoneConstantsEx {
    public static final int APN_ALREADY_ACTIVE = 0;
    public static final int APN_REQUEST_FAILED = 3;
    public static final int APN_REQUEST_STARTED = 1;
    public static final String APN_TYPE_ALL = "*";
    public static final String APN_TYPE_CBS = "cbs";
    public static final String APN_TYPE_DEFAULT = "default";
    public static final String APN_TYPE_FOTA = "fota";
    public static final String APN_TYPE_HIPRI = "hipri";
    public static final String APN_TYPE_IMS = "ims";
    public static final String APN_TYPE_MMS = "mms";
    public static final String APN_TYPE_SUPL = "supl";
    public static final int FAILURE = 1;
    public static final int PHONE_TYPE_CDMA = 2;
    public static final int PHONE_TYPE_GSM = 1;
    public static final int PHONE_TYPE_IMS = 4;
    public static final int PHONE_TYPE_NONE = 0;
    public static final int PHONE_TYPE_SIP = 3;
    public static final int PHONE_TYPE_THIRD_PARTY = 4;
    public static final String SLOT_KEY = "slot";
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    public static final String SUBSCRIPTION_KEY = "subscription";
    public static final int SUCCESS = 0;

    public enum DataState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
        SUSPENDED
    }
}
