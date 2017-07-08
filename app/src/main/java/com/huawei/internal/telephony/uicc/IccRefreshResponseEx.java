package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccRefreshResponse;

public class IccRefreshResponseEx {
    public static final int REFRESH_RESULT_FILE_UPDATE = 0;
    public static final int REFRESH_RESULT_INIT = 1;
    public static final int REFRESH_RESULT_RESET = 2;
    public String aid;
    public int efId;
    public int refreshResult;

    public static String toString(IccRefreshResponse obj) {
        return obj.toString();
    }
}
