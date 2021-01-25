package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class PublishOption implements Parcelable {
    public static final Parcelable.Creator<PublishOption> CREATOR = new Parcelable.Creator<PublishOption>() {
        /* class com.huawei.softnet.connect.PublishOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PublishOption createFromParcel(Parcel in) {
            return new PublishOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public PublishOption[] newArray(int size) {
            return new PublishOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    public static final int PUBLISH_MODE_PUBLISH_OFFLINE = 3;
    public static final int PUBLISH_MODE_PUBLISH_ONLINE = 2;
    public static final int PUBLISH_MODE_REGISTER_SERVICE = 1;
    private int mCount;
    private String mExtInfo;
    private List<ServiceDesc> mInfos;
    private PowerPolicy mPowerPolicy;
    private int mPublishMode;
    private List<ServiceFilter> mServiceFilters;
    private Strategy mStrategy;
    private int mTimeout;

    protected PublishOption(Parcel in) {
        if (this.mInfos == null) {
            this.mInfos = new ArrayList();
        }
        in.readList(this.mInfos, ServiceDesc.class.getClassLoader());
        if (this.mServiceFilters == null) {
            this.mServiceFilters = new ArrayList();
        }
        in.readList(this.mServiceFilters, ServiceFilter.class.getClassLoader());
        this.mStrategy = (Strategy) in.readParcelable(Strategy.class.getClassLoader());
        this.mTimeout = in.readInt();
        this.mCount = in.readInt();
        this.mPowerPolicy = (PowerPolicy) in.readParcelable(PowerPolicy.class.getClassLoader());
        this.mPublishMode = in.readInt();
        this.mExtInfo = in.readString();
    }

    private PublishOption() {
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

    public List<ServiceDesc> getInfos() {
        return this.mInfos;
    }

    public Strategy getStrategy() {
        return this.mStrategy;
    }

    public int getCount() {
        return this.mCount;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public PowerPolicy getPowerPolicy() {
        return this.mPowerPolicy;
    }

    public List<ServiceFilter> getServiceFilters() {
        return this.mServiceFilters;
    }

    public int getPublishMode() {
        return this.mPublishMode;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public static class Builder {
        private PublishOption option = new PublishOption();

        public Builder infos(List<ServiceDesc> infos) {
            this.option.mInfos = infos;
            return this;
        }

        public Builder serviceFilters(List<ServiceFilter> serviceFilters) {
            this.option.mServiceFilters = serviceFilters;
            return this;
        }

        public Builder strategy(Strategy strategy) {
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

        public Builder powerPolicy(PowerPolicy powerPolicy) {
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

        public PublishOption build() {
            return this.option;
        }
    }
}
