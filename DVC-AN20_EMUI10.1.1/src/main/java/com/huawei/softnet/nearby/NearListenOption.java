package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class NearListenOption implements Parcelable {
    public static final Parcelable.Creator<NearListenOption> CREATOR = new Parcelable.Creator<NearListenOption>() {
        /* class com.huawei.softnet.nearby.NearListenOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearListenOption createFromParcel(Parcel in) {
            return new NearListenOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public NearListenOption[] newArray(int size) {
            return new NearListenOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private NearPowerPolicy mPowerPolicy;
    private List<NearServiceFilter> mServiceFilters;
    private NearStrategy mStrategy;

    protected NearListenOption(Parcel in) {
        if (this.mServiceFilters == null) {
            this.mServiceFilters = new ArrayList();
        }
        in.readList(this.mServiceFilters, NearServiceFilter.class.getClassLoader());
        this.mStrategy = (NearStrategy) in.readParcelable(NearStrategy.class.getClassLoader());
        this.mPowerPolicy = (NearPowerPolicy) in.readParcelable(NearPowerPolicy.class.getClassLoader());
    }

    private NearListenOption() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.mServiceFilters);
        dest.writeParcelable(this.mStrategy, 0);
        dest.writeParcelable(this.mPowerPolicy, 0);
    }

    public int describeContents() {
        return 0;
    }

    public NearStrategy getStrategy() {
        return this.mStrategy;
    }

    public NearPowerPolicy getPowerPolicy() {
        return this.mPowerPolicy;
    }

    public List<NearServiceFilter> getServiceFilters() {
        return this.mServiceFilters;
    }

    public static class Builder {
        private NearListenOption option = new NearListenOption();

        public Builder serviceFilters(List<NearServiceFilter> serviceFilters) {
            this.option.mServiceFilters = serviceFilters;
            return this;
        }

        public Builder strategy(NearStrategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder powerPolicy(NearPowerPolicy powerPolicy) {
            this.option.mPowerPolicy = powerPolicy;
            return this;
        }

        public NearListenOption build() {
            return this.option;
        }
    }
}
