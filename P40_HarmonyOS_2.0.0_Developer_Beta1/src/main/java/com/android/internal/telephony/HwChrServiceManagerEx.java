package com.android.internal.telephony;

public class HwChrServiceManagerEx {
    public static final int CALL_TYPE_ANSWER = 2;
    public static final int CALL_TYPE_DIAL = 0;
    public static final int CALL_TYPE_INCOMMING = 1;

    public static void reportCallException(String appName, int subId, int callType, String params) {
        HwChrServiceManager chrService = HwTelephonyFactory.getHwChrServiceManager();
        if (chrService != null) {
            chrService.reportCallException(appName, subId, callType, params);
        }
    }
}
