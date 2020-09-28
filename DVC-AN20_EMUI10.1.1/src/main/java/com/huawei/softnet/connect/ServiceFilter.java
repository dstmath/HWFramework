package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class ServiceFilter implements Parcelable {
    public static final Parcelable.Creator<ServiceFilter> CREATOR = new Parcelable.Creator<ServiceFilter>() {
        /* class com.huawei.softnet.connect.ServiceFilter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ServiceFilter createFromParcel(Parcel in) {
            return new ServiceFilter(in);
        }

        @Override // android.os.Parcelable.Creator
        public ServiceFilter[] newArray(int size) {
            return new ServiceFilter[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte[] mFilterData;
    private byte[] mFilterMask;
    private String mServiceId;

    protected ServiceFilter(Parcel in) {
        this.mServiceId = in.readString();
        this.mFilterData = in.createByteArray();
        this.mFilterMask = in.createByteArray();
    }

    private ServiceFilter() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceId);
        dest.writeByteArray(this.mFilterData);
        dest.writeByteArray(this.mFilterMask);
    }

    public int describeContents() {
        return 0;
    }

    public byte[] getFilterData() {
        return this.mFilterData;
    }

    public String getServiceId() {
        return this.mServiceId;
    }

    public byte[] getFilterMask() {
        return this.mFilterMask;
    }

    public static class Builder {
        ServiceFilter filter = new ServiceFilter();

        public Builder serviceId(String serviceId) {
            this.filter.mServiceId = serviceId;
            return this;
        }

        public Builder filterData(byte[] filterData) {
            this.filter.mFilterData = filterData;
            return this;
        }

        public Builder filterMask(byte[] filterMask) {
            this.filter.mFilterMask = filterMask;
            return this;
        }

        public ServiceFilter build() {
            return this.filter;
        }
    }
}
