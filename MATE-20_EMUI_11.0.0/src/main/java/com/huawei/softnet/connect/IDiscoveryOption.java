package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class IDiscoveryOption implements Parcelable {
    public static final Parcelable.Creator<IDiscoveryOption> CREATOR = new Parcelable.Creator<IDiscoveryOption>() {
        /* class com.huawei.softnet.connect.IDiscoveryOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IDiscoveryOption createFromParcel(Parcel in) {
            return new IDiscoveryOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public IDiscoveryOption[] newArray(int size) {
            return new IDiscoveryOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mCount;
    private int mDiscoveryMode;
    private String mExtInfo;
    private List<IServiceDesc> mInfos;
    private IPowerPolicy mPowerPolicy;
    private List<IServiceFilter> mServiceFilters;
    private IStrategy mStrategy;
    private int mTimeout;

    protected IDiscoveryOption(Parcel in) {
        this.mStrategy = (IStrategy) in.readParcelable(IStrategy.class.getClassLoader());
        if (this.mInfos == null) {
            this.mInfos = new ArrayList();
        }
        in.readList(this.mInfos, IServiceDesc.class.getClassLoader());
        if (this.mServiceFilters == null) {
            this.mServiceFilters = new ArrayList();
        }
        in.readList(this.mServiceFilters, IServiceFilter.class.getClassLoader());
        this.mTimeout = in.readInt();
        this.mCount = in.readInt();
        this.mPowerPolicy = (IPowerPolicy) in.readParcelable(IPowerPolicy.class.getClassLoader());
        this.mDiscoveryMode = in.readInt();
        this.mExtInfo = in.readString();
    }

    private IDiscoveryOption() {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mStrategy, 0);
        dest.writeList(this.mInfos);
        dest.writeList(this.mServiceFilters);
        dest.writeInt(this.mTimeout);
        dest.writeInt(this.mCount);
        dest.writeParcelable(this.mPowerPolicy, 0);
        dest.writeInt(this.mDiscoveryMode);
        dest.writeString(this.mExtInfo);
    }

    public List<IServiceDesc> getInfos() {
        return this.mInfos;
    }

    public List<IServiceFilter> getServiceFilters() {
        return this.mServiceFilters;
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

    public IPowerPolicy getPowerPolicy() {
        return this.mPowerPolicy;
    }

    public int getDiscoveryMode() {
        return this.mDiscoveryMode;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public static class Builder {
        private IDiscoveryOption option = new IDiscoveryOption();

        public Builder strategy(IStrategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder infos(List<IServiceDesc> infos) {
            this.option.mInfos = infos;
            return this;
        }

        public Builder serviceFilters(List<IServiceFilter> serviceFilters) {
            this.option.mServiceFilters = serviceFilters;
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

        public Builder discoveryMode(int discoveryMode) {
            this.option.mDiscoveryMode = discoveryMode;
            return this;
        }

        public Builder extInfo(String extInfo) {
            this.option.mExtInfo = extInfo;
            return this;
        }

        public IDiscoveryOption build() {
            return this.option;
        }
    }
}
