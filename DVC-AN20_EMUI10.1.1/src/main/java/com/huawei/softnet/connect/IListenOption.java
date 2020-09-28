package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class IListenOption implements Parcelable {
    public static final Parcelable.Creator<IListenOption> CREATOR = new Parcelable.Creator<IListenOption>() {
        /* class com.huawei.softnet.connect.IListenOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IListenOption createFromParcel(Parcel in) {
            return new IListenOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public IListenOption[] newArray(int size) {
            return new IListenOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private String mExtInfo;
    private IPowerPolicy mPowerPolicy;
    private List<IServiceFilter> mServiceFilters;
    private IStrategy mStrategy;

    protected IListenOption(Parcel in) {
        if (this.mServiceFilters == null) {
            this.mServiceFilters = new ArrayList();
        }
        in.readList(this.mServiceFilters, IServiceFilter.class.getClassLoader());
        this.mStrategy = (IStrategy) in.readParcelable(IStrategy.class.getClassLoader());
        this.mPowerPolicy = (IPowerPolicy) in.readParcelable(IPowerPolicy.class.getClassLoader());
        this.mExtInfo = in.readString();
    }

    private IListenOption() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.mServiceFilters);
        dest.writeParcelable(this.mStrategy, 0);
        dest.writeParcelable(this.mPowerPolicy, 0);
        dest.writeString(this.mExtInfo);
    }

    public int describeContents() {
        return 0;
    }

    public IStrategy getStrategy() {
        return this.mStrategy;
    }

    public IPowerPolicy getPowerPolicy() {
        return this.mPowerPolicy;
    }

    public List<IServiceFilter> getServiceFilters() {
        return this.mServiceFilters;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public static class Builder {
        private IListenOption option = new IListenOption();

        public Builder serviceFilters(List<IServiceFilter> serviceFilters) {
            this.option.mServiceFilters = serviceFilters;
            return this;
        }

        public Builder strategy(IStrategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder powerPolicy(IPowerPolicy powerPolicy) {
            this.option.mPowerPolicy = powerPolicy;
            return this;
        }

        public Builder extInfo(String extInfo) {
            this.option.mExtInfo = extInfo;
            return this;
        }

        public IListenOption build() {
            return this.option;
        }
    }
}
