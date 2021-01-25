package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;

public class NearServiceDesc implements Parcelable {
    public static final Parcelable.Creator<NearServiceDesc> CREATOR = new Parcelable.Creator<NearServiceDesc>() {
        /* class com.huawei.softnet.nearby.NearServiceDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearServiceDesc createFromParcel(Parcel in) {
            return new NearServiceDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public NearServiceDesc[] newArray(int size) {
            return new NearServiceDesc[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte[] mServiceData;
    private String mServiceId;
    private String mServiceName;

    protected NearServiceDesc(Parcel in) {
        this.mServiceId = in.readString();
        this.mServiceName = in.readString();
        this.mServiceData = in.createByteArray();
    }

    private NearServiceDesc() {
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceId);
        dest.writeString(this.mServiceName);
        dest.writeByteArray(this.mServiceData);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getServiceId() {
        return this.mServiceId;
    }

    public String getServiceName() {
        return this.mServiceName;
    }

    public byte[] getServiceData() {
        return this.mServiceData;
    }

    public static class Builder {
        NearServiceDesc info = new NearServiceDesc();

        public Builder serviceId(String serviceId) {
            this.info.mServiceId = serviceId;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.info.mServiceName = serviceName;
            return this;
        }

        public Builder serviceData(byte[] serviceData) {
            this.info.mServiceData = serviceData;
            return this;
        }

        public NearServiceDesc build() {
            return this.info;
        }
    }
}
