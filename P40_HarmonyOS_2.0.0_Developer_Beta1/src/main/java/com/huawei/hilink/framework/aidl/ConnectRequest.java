package com.huawei.hilink.framework.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectRequest implements Parcelable {
    public static final Parcelable.Creator<ConnectRequest> CREATOR = new Parcelable.Creator<ConnectRequest>() {
        /* class com.huawei.hilink.framework.aidl.ConnectRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConnectRequest createFromParcel(Parcel source) {
            Builder builder = new Builder();
            builder.setRequestID(source.readInt());
            builder.setRemoteIP(source.readString()).setRemotePort(source.readInt());
            builder.setGatewayType(source.readInt());
            builder.setServiceType(source.readString());
            return builder.build();
        }

        @Override // android.os.Parcelable.Creator
        public ConnectRequest[] newArray(int size) {
            return new ConnectRequest[size];
        }
    };
    public static final int GATEWAY_ENTERPRISE = 2;
    public static final int GATEWAY_HOME = 1;
    private static final int IP_MAX_LEN = 40;
    private static final int PORT_MAX = 65535;
    private static final int PORT_MIN = 0;
    private static final String TAG = "hilinkService";
    private int gatewayType;
    private String remoteIP;
    private int remotePort;
    private int requestID;
    private String serviceType;

    public static class Builder {
        private int gatewayType;
        private String remoteIP = null;
        private int remotePort = 0;
        private int requestID = 0;
        private String serviceType;

        public Builder setRequestID(int requestID2) {
            this.requestID = requestID2;
            return this;
        }

        public Builder setRemoteIP(String remoteIP2) {
            this.remoteIP = remoteIP2;
            return this;
        }

        public Builder setRemotePort(int remotePort2) {
            this.remotePort = remotePort2;
            return this;
        }

        public Builder setGatewayType(int gatewayType2) {
            this.gatewayType = gatewayType2;
            return this;
        }

        public Builder setServiceType(String serviceType2) {
            this.serviceType = serviceType2;
            return this;
        }

        public Builder setParaFromServiceRecord(ServiceRecord record) {
            this.remoteIP = record.getRemoteIP();
            this.remotePort = record.getRemotePort();
            return this;
        }

        public ConnectRequest build() {
            ConnectRequest req = new ConnectRequest(this);
            if (req.isLegal()) {
                return req;
            }
            return null;
        }
    }

    private ConnectRequest(Builder builder) {
        this.requestID = builder.requestID;
        this.remoteIP = builder.remoteIP;
        this.remotePort = builder.remotePort;
        this.gatewayType = builder.gatewayType;
        this.serviceType = builder.serviceType;
    }

    public int getRequestID() {
        return this.requestID;
    }

    public String getRemoteIP() {
        return this.remoteIP;
    }

    public int getRemotePort() {
        return this.remotePort;
    }

    public int getGatewayType() {
        return this.gatewayType;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.requestID);
        dest.writeString(this.remoteIP);
        dest.writeInt(this.remotePort);
        dest.writeInt(this.gatewayType);
        dest.writeString(this.serviceType);
    }

    public boolean isLegal() {
        String str;
        int i;
        String str2;
        if (this.requestID == 0 || (str = this.remoteIP) == null || str.length() == 0 || this.remoteIP.length() > IP_MAX_LEN || (i = this.remotePort) < 0 || i > PORT_MAX) {
            return false;
        }
        int i2 = this.gatewayType;
        if ((i2 != 1 && i2 != 2) || (str2 = this.serviceType) == null || str2.length() == 0) {
            return false;
        }
        return true;
    }
}
