package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class IServiceFilter implements Parcelable {
    public static final Parcelable.Creator<IServiceFilter> CREATOR = new Parcelable.Creator<IServiceFilter>() {
        /* class com.huawei.softnet.connect.IServiceFilter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IServiceFilter createFromParcel(Parcel in) {
            return new IServiceFilter(in);
        }

        @Override // android.os.Parcelable.Creator
        public IServiceFilter[] newArray(int size) {
            return new IServiceFilter[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte[] mFilterData;
    private byte[] mFilterMask;
    private String mServiceId;

    protected IServiceFilter(Parcel in) {
        this.mServiceId = in.readString();
        this.mFilterData = in.createByteArray();
        this.mFilterMask = in.createByteArray();
    }

    private IServiceFilter() {
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceId);
        dest.writeByteArray(this.mFilterData);
        dest.writeByteArray(this.mFilterMask);
    }

    @Override // android.os.Parcelable
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
        IServiceFilter filter = new IServiceFilter();

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

        public IServiceFilter build() {
            return this.filter;
        }
    }
}
