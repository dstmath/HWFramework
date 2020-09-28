package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class ServiceDesc implements Parcelable {
    public static final Parcelable.Creator<ServiceDesc> CREATOR = new Parcelable.Creator<ServiceDesc>() {
        /* class com.huawei.softnet.connect.ServiceDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ServiceDesc createFromParcel(Parcel in) {
            return new ServiceDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public ServiceDesc[] newArray(int size) {
            return new ServiceDesc[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte[] mServiceData;
    private String mServiceId;
    private String mServiceName;

    protected ServiceDesc(Parcel in) {
        this.mServiceId = in.readString();
        this.mServiceName = in.readString();
        this.mServiceData = in.createByteArray();
    }

    private ServiceDesc() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceId);
        dest.writeString(this.mServiceName);
        dest.writeByteArray(this.mServiceData);
    }

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
        ServiceDesc info = new ServiceDesc();

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

        public ServiceDesc build() {
            return this.info;
        }
    }
}
