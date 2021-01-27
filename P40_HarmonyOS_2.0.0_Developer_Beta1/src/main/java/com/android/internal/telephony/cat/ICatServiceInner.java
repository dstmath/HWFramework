package com.android.internal.telephony.cat;

public interface ICatServiceInner {
    public static final int DELAY_SEND_TIME = 25000;
    public static final int MSG_ID_NO_OPEN_CHANNEL_RECEIVED = 12;
    public static final int MSG_ID_OTA_SET_RESULT_NOTIFY = 10;
    public static final int MSG_ID_START_OTA = 11;
    public static final int MSG_ID_UIM_LOCK_NOTIFY = 13;
    public static final int OTA_TYPE_CHANGE_IMSI = 1;
    public static final int OTA_TYPE_OPEN_SERVICE = 0;
    public static final int OTA_TYPE_UPDATE_COUNTRY_INFO = 2;
}
