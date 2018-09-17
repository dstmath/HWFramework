package com.huawei.android.telephony;

import android.telephony.SmsManager;
import com.huawei.android.util.NoExtAPIException;

public final class SmsManagerEx {
    public static String getSmscAddr(SmsManager obj) {
        return obj.getSmscAddr();
    }

    public static boolean setSmscAddr(SmsManager obj, String smscAddr) {
        return obj.setSmscAddr(smscAddr);
    }

    public static boolean enableCdmaBroadcast(SmsManager obj, int messageIdentifier) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean disableCdmaBroadcast(SmsManager obj, int messageIdentifier) {
        throw new NoExtAPIException("method not supported.");
    }
}
