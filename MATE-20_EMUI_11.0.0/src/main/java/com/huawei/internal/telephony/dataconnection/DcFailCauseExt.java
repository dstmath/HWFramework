package com.huawei.internal.telephony.dataconnection;

import com.android.internal.telephony.dataconnection.DcFailCause;

public class DcFailCauseExt {
    private DcFailCause dcFailCause = null;

    public DcFailCause getDcFailCause() {
        return this.dcFailCause;
    }

    public void setDcFailCause(DcFailCause dcFailCause2) {
        this.dcFailCause = dcFailCause2;
    }

    public int getErrorCode() {
        DcFailCause dcFailCause2 = this.dcFailCause;
        if (dcFailCause2 != null) {
            return dcFailCause2.getErrorCode();
        }
        return 0;
    }
}
