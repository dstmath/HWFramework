package com.huawei.internal.telephony.dataconnection;

import com.android.internal.telephony.dataconnection.DcFailCause;

public enum DcFailCauseEx {
    NONE(DcFailCause.NONE.getErrorCode());
    
    private final int mErrorCode;

    private DcFailCauseEx(int errorCode) {
        this.mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }
}
