package com.huawei.hilink.framework.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class ServiceRecord implements Parcelable {
    public static final Parcelable.Creator<ServiceRecord> CREATOR = new Parcelable.Creator<ServiceRecord>() {
        public ServiceRecord createFromParcel(Parcel source) {
            return new Builder().setRequestID(source.readInt()).setRemoteip(source.readString()).setRemotport(source.readInt()).setPayload(source.readString()).build();
        }

        public ServiceRecord[] newArray(int size) {
            return new ServiceRecord[size];
        }
    };
    private static final int IP_MAX_LEN = 40;
    private static final int PORT_MAX = 65535;
    private static final int PORT_MIN = 0;
    private String payload;
    private String remoteIP;
    private int remotePort;
    private int requestID;

    public static class Builder {
        /* access modifiers changed from: private */
        public String payload = null;
        /* access modifiers changed from: private */
        public String remoteIP = null;
        /* access modifiers changed from: private */
        public int remotePort = 0;
        /* access modifiers changed from: private */
        public int requestID = 0;

        public Builder setRequestID(int requestID2) {
            this.requestID = requestID2;
            return this;
        }

        public Builder setRemoteip(String remoteIP2) {
            this.remoteIP = remoteIP2;
            return this;
        }

        public Builder setPayload(String payload2) {
            this.payload = payload2;
            return this;
        }

        public Builder setRemotport(int remotePort2) {
            this.remotePort = remotePort2;
            return this;
        }

        public ServiceRecord build() {
            ServiceRecord serviceRecord = new ServiceRecord(this, null);
            if (serviceRecord.isLegal()) {
                return serviceRecord;
            }
            return null;
        }
    }

    private ServiceRecord(Builder para) {
        this.requestID = para.requestID;
        this.remoteIP = para.remoteIP;
        this.payload = para.payload;
        this.remotePort = para.remotePort;
    }

    /* synthetic */ ServiceRecord(Builder builder, ServiceRecord serviceRecord) {
        this(builder);
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

    public String getPayload() {
        return this.payload;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.requestID);
        dest.writeString(this.remoteIP);
        dest.writeInt(this.remotePort);
        dest.writeString(this.payload);
    }

    public boolean isLegal() {
        if (this.remoteIP == null || this.remoteIP.length() == 0 || this.remoteIP.length() > 40 || this.remotePort < 0 || this.remotePort > PORT_MAX) {
            return false;
        }
        return true;
    }
}
