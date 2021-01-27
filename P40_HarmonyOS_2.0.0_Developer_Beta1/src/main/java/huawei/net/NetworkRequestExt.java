package huawei.net;

import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

public class NetworkRequestExt {
    private static final int INVAILID_PDU_SESSION_TYPE = -1;
    private static final int INVALID_REQUEST_ID = -1;
    private static final int INVALID_TYPE_5G = -1;
    private NetworkRequest mNetworkRequest;

    public NetworkRequestExt() {
    }

    public NetworkRequestExt(NetworkCapabilities nc, int legacyType, int rId) {
        this.mNetworkRequest = new NetworkRequest(nc, legacyType, rId, NetworkRequest.Type.REQUEST);
    }

    public NetworkRequest getNetworkRequest() {
        return this.mNetworkRequest;
    }

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

    public int getRequestId() {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest == null) {
            return -1;
        }
        return networkRequest.requestId;
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

    public void setDnn(String dnn) {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest != null && networkRequest.networkCapabilities != null) {
            this.mNetworkRequest.networkCapabilities.setDnn(dnn);
        }
    }

    public void setSnssai(String snssai) {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest != null && networkRequest.networkCapabilities != null) {
            this.mNetworkRequest.networkCapabilities.setSnssai(snssai);
        }
    }

    public void setSscMode(byte sscMode) {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest != null && networkRequest.networkCapabilities != null) {
            this.mNetworkRequest.networkCapabilities.setSscMode(sscMode);
        }
    }

    public void setPduSessionType(int pduSessionType) {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest != null && networkRequest.networkCapabilities != null) {
            this.mNetworkRequest.networkCapabilities.setPduSessionType(pduSessionType);
        }
    }

    public void setRouteBitmap(byte routeBitmap) {
        NetworkRequest networkRequest = this.mNetworkRequest;
        if (networkRequest != null && networkRequest.networkCapabilities != null) {
            this.mNetworkRequest.networkCapabilities.setRouteBitmap(routeBitmap);
        }
    }

    public static NetworkCapabilities getNetworkCapabilities(NetworkRequest networkRequest) {
        return networkRequest.networkCapabilities;
    }
}
