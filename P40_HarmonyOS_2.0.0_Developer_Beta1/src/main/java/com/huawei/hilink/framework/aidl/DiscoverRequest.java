package com.huawei.hilink.framework.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class DiscoverRequest implements Parcelable {
    public static final Parcelable.Creator<DiscoverRequest> CREATOR = new Parcelable.Creator<DiscoverRequest>() {
        /* class com.huawei.hilink.framework.aidl.DiscoverRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DiscoverRequest createFromParcel(Parcel source) {
            return new Builder().setServiceType(source.readString()).setQuery(source.readString()).setRequestID(source.readInt()).build();
        }

        @Override // android.os.Parcelable.Creator
        public DiscoverRequest[] newArray(int size) {
            return new DiscoverRequest[size];
        }
    };
    private static final int QUERY_MAX_LEN = 128;
    private static final int ST_MAX_LEN = 32;
    private String query;
    private int requestID;
    private String serviceType;

    public static class Builder {
        private String query = null;
        private int requestID = 0;
        private String serviceType = null;

        public Builder setServiceType(String serviceType2) {
            this.serviceType = serviceType2;
            return this;
        }

        public Builder setQuery(String query2) {
            this.query = query2;
            return this;
        }

        public Builder setRequestID(int requestID2) {
            this.requestID = requestID2;
            return this;
        }

        public DiscoverRequest build() {
            DiscoverRequest req = new DiscoverRequest(this);
            if (req.isLegal()) {
                return req;
            }
            return null;
        }
    }

    private DiscoverRequest(Builder para) {
        this.serviceType = para.serviceType;
        this.query = para.query;
        this.requestID = para.requestID;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public String getQuery() {
        return this.query;
    }

    public int getRequestID() {
        return this.requestID;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serviceType);
        dest.writeString(this.query);
        dest.writeInt(this.requestID);
    }

    public boolean isLegal() {
        String str = this.serviceType;
        if (str == null || str.length() == 0 || this.serviceType.length() > 32) {
            return false;
        }
        String str2 = this.query;
        if (str2 == null || str2.length() <= 128) {
            return true;
        }
        return false;
    }
}
