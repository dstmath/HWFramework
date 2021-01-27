package com.huawei.internal.telephony.dataconnection;

import android.net.NetworkRequest;
import com.android.internal.telephony.dataconnection.DcRequest;

public class DcRequestExt {
    private DcRequest mDcRequest;

    public static DcRequestExt getDcRequestExt(Object dcRequest) {
        DcRequestExt dcRequestExt = new DcRequestExt();
        if (dcRequest instanceof DcRequest) {
            dcRequestExt.setDcRequest((DcRequest) dcRequest);
        }
        return dcRequestExt;
    }

    public void setDcRequest(DcRequest dcRequest) {
        this.mDcRequest = dcRequest;
    }

    public NetworkRequest getNetworkRequest() {
        DcRequest dcRequest = this.mDcRequest;
        if (dcRequest == null) {
            return null;
        }
        return dcRequest.networkRequest;
    }

    public int getApnType() {
        DcRequest dcRequest = this.mDcRequest;
        if (dcRequest == null) {
            return 0;
        }
        return dcRequest.apnType;
    }
}
