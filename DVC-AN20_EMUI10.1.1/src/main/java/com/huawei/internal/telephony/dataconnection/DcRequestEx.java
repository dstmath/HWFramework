package com.huawei.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkRequest;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.huawei.annotation.HwSystemApi;
import java.util.Objects;

@HwSystemApi
public class DcRequestEx implements Comparable<DcRequestEx> {
    private DcRequest mDcRequest;

    public DcRequestEx(NetworkRequest networkRequest, Context context) {
        this.mDcRequest = new DcRequest(networkRequest, context);
    }

    public int compareTo(DcRequestEx dcRequestEx) {
        return this.mDcRequest.compareTo(dcRequestEx.getDcRequest());
    }

    public DcRequest getDcRequest() {
        return this.mDcRequest;
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

    public String toString() {
        return "DcRequestEx{mDcRequest=" + this.mDcRequest + '}';
    }

    public boolean equals(Object o) {
        if (!(o instanceof DcRequestEx)) {
            return false;
        }
        return Objects.equals(this.mDcRequest, ((DcRequestEx) o).getDcRequest());
    }

    public int hashCode() {
        if (getNetworkRequest() != null) {
            return getNetworkRequest().hashCode();
        }
        return 0;
    }
}
