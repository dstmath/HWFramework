package com.huawei.hilink.framework.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class CallRequest implements Parcelable {
    public static final Parcelable.Creator<CallRequest> CREATOR = new Parcelable.Creator<CallRequest>() {
        /* class com.huawei.hilink.framework.aidl.CallRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CallRequest createFromParcel(Parcel source) {
            Builder builder = new Builder();
            builder.setServiceID(source.readString()).setQuery(source.readString());
            builder.setMethod(source.readInt());
            builder.setPayload(source.readString());
            builder.setRequestID(source.readInt());
            builder.setDeviceID(source.readString());
            builder.setRemoteIP(source.readString()).setRemotePort(source.readInt());
            return builder.build();
        }

        @Override // android.os.Parcelable.Creator
        public CallRequest[] newArray(int size) {
            return new CallRequest[size];
        }
    };
    private static final int DEVID_MAX_LEN = 40;
    private static final int IP_MAX_LEN = 40;
    public static final int METHOD_GET = 1;
    public static final int METHOD_POST = 2;
    private static final int PORT_MAX = 65535;
    private static final int PORT_MIN = 0;
    private static final int QUERY_MAX_LEN = 128;
    private static final int SID_MAX_LEN = 64;
    private String deviceID;
    private int method;
    private String payload;
    private String query;
    private String remoteIP;
    private int remotePort;
    private int requestID;
    private String serviceID;

    public static class Builder {
        private String deviceID = null;
        private int method = 0;
        private String payload = null;
        private String query = null;
        private String remoteIP = null;
        private int remotePort = 0;
        private int requestID = 0;
        private String serviceID = null;

        public Builder setServiceID(String serviceid) {
            this.serviceID = serviceid;
            return this;
        }

        public Builder setRequestID(int requestID2) {
            this.requestID = requestID2;
            return this;
        }

        public Builder setQuery(String query2) {
            this.query = query2;
            return this;
        }

        public Builder setMethod(int method2) {
            this.method = method2;
            return this;
        }

        public Builder setPayload(String payload2) {
            this.payload = payload2;
            return this;
        }

        public Builder setDeviceID(String deviceID2) {
            this.deviceID = deviceID2;
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

        public Builder setParaFromServiceRecord(ServiceRecord record) {
            this.remoteIP = record.getRemoteIP();
            this.remotePort = record.getRemotePort();
            return this;
        }

        public CallRequest build() {
            CallRequest req = new CallRequest(this);
            if (req.isLegal()) {
                return req;
            }
            return null;
        }
    }

    private CallRequest(Builder builder) {
        this.serviceID = builder.serviceID;
        this.query = builder.query;
        this.method = builder.method;
        this.payload = builder.payload;
        this.requestID = builder.requestID;
        this.deviceID = builder.deviceID;
        this.remoteIP = builder.remoteIP;
        this.remotePort = builder.remotePort;
    }

    public String getServiceID() {
        return this.serviceID;
    }

    public String getQuery() {
        return this.query;
    }

    public int getMethod() {
        return this.method;
    }

    public String getPayload() {
        return this.payload;
    }

    public int getRequestID() {
        return this.requestID;
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public String getRemoteIP() {
        return this.remoteIP;
    }

    public int getRemotePort() {
        return this.remotePort;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serviceID);
        dest.writeString(this.query);
        dest.writeInt(this.method);
        dest.writeString(this.payload);
        dest.writeInt(this.requestID);
        dest.writeString(this.deviceID);
        dest.writeString(this.remoteIP);
        dest.writeInt(this.remotePort);
    }

    public boolean isLegal() {
        String str;
        int i;
        String str2;
        String str3 = this.serviceID;
        if (str3 == null || str3.length() == 0 || this.serviceID.length() > 64) {
            return false;
        }
        String str4 = this.query;
        if (str4 != null && str4.length() > 128) {
            return false;
        }
        int i2 = this.method;
        if (i2 != 1 && i2 != 2) {
            return false;
        }
        if (this.method == 2 && ((str2 = this.payload) == null || str2.length() == 0)) {
            return false;
        }
        String str5 = this.deviceID;
        if ((str5 == null || str5.length() <= 40) && (str = this.remoteIP) != null && str.length() != 0 && this.remoteIP.length() <= 40 && (i = this.remotePort) >= 0 && i <= PORT_MAX) {
            return true;
        }
        return false;
    }
}
