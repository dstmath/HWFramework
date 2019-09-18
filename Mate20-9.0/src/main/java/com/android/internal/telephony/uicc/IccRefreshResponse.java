package com.android.internal.telephony.uicc;

public class IccRefreshResponse {
    public static final int REFRESH_RESULT_FILE_UPDATE = 0;
    public static final int REFRESH_RESULT_INIT = 1;
    public static final int REFRESH_RESULT_RESET = 2;
    public String aid;
    public int efId;
    public int refreshResult;

    public String toString() {
        return "{" + this.refreshResult + ", " + this.aid + ", " + this.efId + "}";
    }
}
