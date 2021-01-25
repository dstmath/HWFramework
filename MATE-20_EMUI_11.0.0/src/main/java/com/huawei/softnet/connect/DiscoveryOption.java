package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryOption implements Parcelable {
    public static final Parcelable.Creator<DiscoveryOption> CREATOR = new Parcelable.Creator<DiscoveryOption>() {
        /* class com.huawei.softnet.connect.DiscoveryOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DiscoveryOption createFromParcel(Parcel in) {
            return new DiscoveryOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public DiscoveryOption[] newArray(int size) {
            return new DiscoveryOption[size];
        }
    };
    public static final int DEVICE_FILTER_BY_NONE = 0;
    public static final int DEVICE_FILTER_BY_SAME_HWACCOUNT_ID = 1;
    public static final int DISCOVERY_MODE_DISCOVERY = 1;
    public static final int DISCOVERY_MODE_SUBSCRIBE = 2;
    private static final int PARCEL_FLAG = 0;
    private int mCount;
    private int mDiscoveryMode;
    private String mExtInfo;
    private List<ServiceDesc> mInfos;
    private PowerPolicy mPowerPolicy;
    private List<ServiceFilter> mServiceFilters;
    private Strategy mStrategy;
    private int mTimeout;

    protected DiscoveryOption(Parcel in) {
        this.mStrategy = (Strategy) in.readParcelable(Strategy.class.getClassLoader());
        if (this.mInfos == null) {
            this.mInfos = new ArrayList();
        }
        in.readList(this.mInfos, ServiceDesc.class.getClassLoader());
        if (this.mServiceFilters == null) {
            this.mServiceFilters = new ArrayList();
        }
        in.readList(this.mServiceFilters, ServiceFilter.class.getClassLoader());
        this.mTimeout = in.readInt();
        this.mCount = in.readInt();
        this.mPowerPolicy = (PowerPolicy) in.readParcelable(PowerPolicy.class.getClassLoader());
        this.mDiscoveryMode = in.readInt();
        this.mExtInfo = in.readString();
    }

    private DiscoveryOption() {
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

    public List<ServiceDesc> getInfos() {
        return this.mInfos;
    }

    public List<ServiceFilter> getServiceFilters() {
        return this.mServiceFilters;
    }

    public int getCount() {
        return this.mCount;
    }

    public Strategy getStrategy() {
        return this.mStrategy;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public PowerPolicy getPowerPolicy() {
        return this.mPowerPolicy;
    }

    public int getDiscoveryMode() {
        return this.mDiscoveryMode;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public static class Builder {
        private DiscoveryOption option = new DiscoveryOption();

        public Builder strategy(Strategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder infos(List<ServiceDesc> infos) {
            this.option.mInfos = infos;
            return this;
        }

        public Builder serviceFilters(List<ServiceFilter> serviceFilters) {
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

        public Builder powerPolicy(PowerPolicy powerPolicy) {
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

        public DiscoveryOption build() {
            return this.option;
        }
    }
}
