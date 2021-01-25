package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class IServiceDesc implements Parcelable {
    public static final Parcelable.Creator<IServiceDesc> CREATOR = new Parcelable.Creator<IServiceDesc>() {
        /* class com.huawei.softnet.connect.IServiceDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IServiceDesc createFromParcel(Parcel in) {
            return new IServiceDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public IServiceDesc[] newArray(int size) {
            return new IServiceDesc[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte[] mServiceData;
    private String mServiceId;
    private String mServiceName;

    protected IServiceDesc(Parcel in) {
        this.mServiceId = in.readString();
        this.mServiceName = in.readString();
        this.mServiceData = in.createByteArray();
    }

    private IServiceDesc() {
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
        IServiceDesc info = new IServiceDesc();

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

        public IServiceDesc build() {
            return this.info;
        }
    }
}
