package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class NearAdvertiseOption implements Parcelable {
    public static final Parcelable.Creator<NearAdvertiseOption> CREATOR = new Parcelable.Creator<NearAdvertiseOption>() {
        /* class com.huawei.softnet.nearby.NearAdvertiseOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearAdvertiseOption createFromParcel(Parcel in) {
            return new NearAdvertiseOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public NearAdvertiseOption[] newArray(int size) {
            return new NearAdvertiseOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mCount;
    private List<NearServiceDesc> mInfos;
    private NearStrategy mStrategy;
    private int mTimeout;

    protected NearAdvertiseOption(Parcel in) {
        this.mStrategy = (NearStrategy) in.readParcelable(NearStrategy.class.getClassLoader());
        if (this.mInfos == null) {
            this.mInfos = new ArrayList();
        }
        in.readList(this.mInfos, NearServiceDesc.class.getClassLoader());
        this.mTimeout = in.readInt();
        this.mCount = in.readInt();
    }

    private NearAdvertiseOption() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mStrategy, 0);
        dest.writeList(this.mInfos);
        dest.writeInt(this.mTimeout);
        dest.writeInt(this.mCount);
    }

    public List<NearServiceDesc> getInfos() {
        return this.mInfos;
    }

    public int getCount() {
        return this.mCount;
    }

    public NearStrategy getStrategy() {
        return this.mStrategy;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public static class Builder {
        private NearAdvertiseOption option = new NearAdvertiseOption();

        public Builder strategy(NearStrategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder infos(List<NearServiceDesc> infos) {
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

        public NearAdvertiseOption build() {
            return this.option;
        }
    }
}
