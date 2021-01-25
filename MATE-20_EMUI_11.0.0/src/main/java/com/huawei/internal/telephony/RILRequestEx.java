package com.huawei.internal.telephony;

import com.android.internal.telephony.RILRequest;

public class RILRequestEx {
    private RILRequest mRilRequest;

    public static RILRequestEx from(RILRequest rilRequest) {
        if (rilRequest == null) {
            return null;
        }
        RILRequestEx rilRequestEx = new RILRequestEx();
        rilRequestEx.setRilRequest(rilRequest);
        return rilRequestEx;
    }

    public RILRequest getRilRequest() {
        return this.mRilRequest;
    }

    public void setRilRequest(RILRequest request) {
        this.mRilRequest = request;
    }

    public int getSerial() {
        RILRequest rILRequest = this.mRilRequest;
        if (rILRequest != null) {
            return rILRequest.getSerial();
        }
        return 0;
    }

    public int getRequest() {
        RILRequest rILRequest = this.mRilRequest;
        if (rILRequest != null) {
            return rILRequest.getRequest();
        }
        return 0;
    }
}
