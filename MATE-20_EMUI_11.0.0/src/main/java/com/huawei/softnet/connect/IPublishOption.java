package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class IPublishOption implements Parcelable {
    public static final Parcelable.Creator<IPublishOption> CREATOR = new Parcelable.Creator<IPublishOption>() {
        /* class com.huawei.softnet.connect.IPublishOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IPublishOption createFromParcel(Parcel in) {
            return new IPublishOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public IPublishOption[] newArray(int size) {
            return new IPublishOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mCount;
    private String mExtInfo;
    private List<IServiceDesc> mInfos;
    private IPowerPolicy mPowerPolicy;
    private int mPublishMode;
    private List<IServiceFilter> mServiceFilters;
    private IStrategy mStrategy;
    private int mTimeout;

    protected IPublishOption(Parcel in) {
        if (this.mInfos == null) {
            this.mInfos = new ArrayList();
        }
        in.readList(this.mInfos, IServiceDesc.class.getClassLoader());
        if (this.mServiceFilters == null) {
            this.mServiceFilters = new ArrayList();
        }
        in.readList(this.mServiceFilters, IServiceFilter.class.getClassLoader());
        this.mStrategy = (IStrategy) in.readParcelable(IStrategy.class.getClassLoader());
        this.mTimeout = in.readInt();
        this.mCount = in.readInt();
        this.mPowerPolicy = (IPowerPolicy) in.readParcelable(IPowerPolicy.class.getClassLoader());
        this.mPublishMode = in.readInt();
        this.mExtInfo = in.readString();
    }

    private IPublishOption() {
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.mInfos);
        dest.writeList(this.mServiceFilters);
        dest.writeParcelable(this.mStrategy, 0);
        dest.writeInt(this.mTimeout);
        dest.writeInt(this.mCount);
        dest.writeParcelable(this.mPowerPolicy, 0);
        dest.writeInt(this.mPublishMode);
        dest.writeString(this.mExtInfo);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public List<IServiceDesc> getInfos() {
        return this.mInfos;
    }

    public IStrategy getStrategy() {
        return this.mStrategy;
    }

    public int getCount() {
        return this.mCount;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public IPowerPolicy getPowerPolicy() {
        return this.mPowerPolicy;
    }

    public List<IServiceFilter> getServiceFilters() {
        return this.mServiceFilters;
    }

    public int getPublishMode() {
        return this.mPublishMode;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public static class Builder {
        private IPublishOption option = new IPublishOption();

        public Builder infos(List<IServiceDesc> infos) {
            this.option.mInfos = infos;
            return this;
        }

        public Builder serviceFilters(List<IServiceFilter> serviceFilters) {
            this.option.mServiceFilters = serviceFilters;
            return this;
        }

        public Builder strategy(IStrategy strategy) {
            this.option.mStrategy = strategy;
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

        public Builder powerPolicy(IPowerPolicy powerPolicy) {
            this.option.mPowerPolicy = powerPolicy;
            return this;
        }

        public Builder publishMode(int publishMode) {
            this.option.mPublishMode = publishMode;
            return this;
        }

        public Builder extInfo(String extInfo) {
            this.option.mExtInfo = extInfo;
            return this;
        }

        public IPublishOption build() {
            return this.option;
        }
    }
}
