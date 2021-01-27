package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IccRefreshResponseEx {
    public static final int REFRESH_RESULT_FILE_UPDATE = 0;
    public static final int REFRESH_RESULT_INIT = 1;
    private static final int REFRESH_RESULT_INVALIDE = -1;
    public static final int REFRESH_RESULT_RESET = 2;
    public String aid;
    public int efId;
    private IccRefreshResponse mIccRefreshResponse;
    public int refreshResult;

    public static String toString(IccRefreshResponse obj) {
        return obj.toString();
    }

    public static IccRefreshResponseEx from(Object object) {
        if (!(object instanceof IccRefreshResponse)) {
            return null;
        }
        IccRefreshResponseEx iccRefreshResponseEx = new IccRefreshResponseEx();
        iccRefreshResponseEx.setIccRefreshResponse((IccRefreshResponse) object);
        return iccRefreshResponseEx;
    }

    private void setIccRefreshResponse(IccRefreshResponse iccRefreshResponse) {
        this.mIccRefreshResponse = iccRefreshResponse;
    }

    public int getRefreshResult() {
        IccRefreshResponse iccRefreshResponse = this.mIccRefreshResponse;
        if (iccRefreshResponse != null) {
            return iccRefreshResponse.refreshResult;
        }
        return -1;
    }
}
