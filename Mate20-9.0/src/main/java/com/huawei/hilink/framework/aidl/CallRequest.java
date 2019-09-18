package com.huawei.hilink.framework.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class CallRequest implements Parcelable {
    public static final Parcelable.Creator<CallRequest> CREATOR = new Parcelable.Creator<CallRequest>() {
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
        /* access modifiers changed from: private */
        public String deviceID = null;
        /* access modifiers changed from: private */
        public int method = 0;
        /* access modifiers changed from: private */
        public String payload = null;
        /* access modifiers changed from: private */
        public String query = null;
        /* access modifiers changed from: private */
        public String remoteIP = null;
        /* access modifiers changed from: private */
        public int remotePort = 0;
        /* access modifiers changed from: private */
        public int requestID = 0;
        /* access modifiers changed from: private */
        public String serviceID = null;

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

        public CallRequest build() {
            CallRequest req = new CallRequest(this, null);
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

    /* synthetic */ CallRequest(Builder builder, CallRequest callRequest) {
        this(builder);
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

    public int describeContents() {
        return 0;
    }

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
        if (this.serviceID == null || this.serviceID.length() == 0 || this.serviceID.length() > SID_MAX_LEN) {
            return false;
        }
        if (this.query != null && this.query.length() > 128) {
            return false;
        }
        if (this.method != 1 && this.method != 2) {
            return false;
        }
        if (this.method == 2 && (this.payload == null || this.payload.length() == 0)) {
            return false;
        }
        if ((this.deviceID == null || this.deviceID.length() <= 40) && this.remoteIP != null && this.remoteIP.length() != 0 && this.remoteIP.length() <= 40 && this.remotePort >= 0 && this.remotePort <= PORT_MAX) {
            return true;
        }
        return false;
    }
}
