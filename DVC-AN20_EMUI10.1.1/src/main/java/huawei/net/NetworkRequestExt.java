package huawei.net;

import android.net.NetworkRequest;

public class NetworkRequestExt {
    private static final int INVAILID_PDU_SESSION_TYPE = -1;
    private static final int INVALID_TYPE_5G = -1;
    private NetworkRequest mNetworkRequest;

    public void setNetworkRequest(NetworkRequest networkRequest) {
        this.mNetworkRequest = networkRequest;
    }

    public int getNetCapability5GSliceType() {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest == null || networkRequest.networkCapabilities == null) {
            return -1;
        }
        return this.mNetworkRequest.networkCapabilities.getNetCapability5GSliceType();
    }

    public String getDnn() {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest == null || networkRequest.networkCapabilities == null) {
            return "";
        }
        return this.mNetworkRequest.networkCapabilities.getDnn();
    }

    public String getSnssai() {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest == null || networkRequest.networkCapabilities == null) {
            return "";
        }
        return this.mNetworkRequest.networkCapabilities.getSnssai();
    }

    public byte getSscMode() {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest == null || networkRequest.networkCapabilities == null) {
            return 0;
        }
        return this.mNetworkRequest.networkCapabilities.getSscMode();
    }

    public int getPduSessionType() {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest == null || networkRequest.networkCapabilities == null) {
            return -1;
        }
        return this.mNetworkRequest.networkCapabilities.getPduSessionType();
    }

    public byte getRouteBitmap() {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest == null || networkRequest.networkCapabilities == null) {
            return 0;
        }
        return this.mNetworkRequest.networkCapabilities.getRouteBitmap();
    }
}
