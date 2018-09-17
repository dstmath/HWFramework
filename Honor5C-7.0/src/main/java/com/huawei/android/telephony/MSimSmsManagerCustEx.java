package com.huawei.android.telephony;

import com.huawei.android.util.NoExtAPIException;

public class MSimSmsManagerCustEx {
    public static boolean isUimSupportMeid(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static String getMeidOrPesn(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean setMeidOrPesn(String meid, String pesn, int subscription) {
        throw new NoExtAPIException("method not supported.");
    }
}
