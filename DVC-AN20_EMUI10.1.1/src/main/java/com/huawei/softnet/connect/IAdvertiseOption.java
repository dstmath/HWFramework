package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class IAdvertiseOption implements Parcelable {
    public static final Parcelable.Creator<IAdvertiseOption> CREATOR = new Parcelable.Creator<IAdvertiseOption>() {
        /* class com.huawei.softnet.connect.IAdvertiseOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IAdvertiseOption createFromParcel(Parcel in) {
            return new IAdvertiseOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public IAdvertiseOption[] newArray(int size) {
            return new IAdvertiseOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mCount;
    private String mExtInfo;
    private List<IServiceDesc> mInfos;
    private List<IServiceFilter> mServiceFilters;
    private IStrategy mStrategy;
    private int mTimeout;

    protected IAdvertiseOption(Parcel in) {
        this.mStrategy = (IStrategy) in.readParcelable(IStrategy.class.getClassLoader());
        if (this.mServiceFilters == null) {
            this.mServiceFilters = new ArrayList();
        }
        in.readList(this.mServiceFilters, IServiceFilter.class.getClassLoader());
        if (this.mInfos == null) {
            this.mInfos = new ArrayList();
        }
        in.readList(this.mInfos, IServiceDesc.class.getClassLoader());
        this.mTimeout = in.readInt();
        this.mCount = in.readInt();
        this.mExtInfo = in.readString();
    }

    private IAdvertiseOption() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mStrategy, 0);
        dest.writeList(this.mServiceFilters);
        dest.writeList(this.mInfos);
        dest.writeInt(this.mTimeout);
        dest.writeInt(this.mCount);
        dest.writeString(this.mExtInfo);
    }

    public List<IServiceFilter> getServiceFilters() {
        return this.mServiceFilters;
    }

    public List<IServiceDesc> getInfos() {
        return this.mInfos;
    }

    public int getCount() {
        return this.mCount;
    }

    public IStrategy getStrategy() {
        return this.mStrategy;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public static class Builder {
        private IAdvertiseOption option = new IAdvertiseOption();

        public Builder strategy(IStrategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder serviceFilters(List<IServiceFilter> serviceFilters) {
            this.option.mServiceFilters = serviceFilters;
            return this;
        }

        public Builder infos(List<IServiceDesc> infos) {
            this.option.mInfos = infos;
            return this;
        }

        public Builder timeout(int timeout) {
            this.option.mTimeout = timeout;
            return this;
        }

        public Builder count(int count) {
            this.option.mCount = count;
            return this;
        }

        public Builder extInfo(String extInfo) {
            this.option.mExtInfo = extInfo;
            return this;
        }

        public IAdvertiseOption build() {
            return this.option;
        }
    }
}
