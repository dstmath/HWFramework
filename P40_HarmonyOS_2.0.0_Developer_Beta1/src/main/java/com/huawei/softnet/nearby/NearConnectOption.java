package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;

public class NearConnectOption implements Parcelable {
    public static final Parcelable.Creator<NearConnectOption> CREATOR = new Parcelable.Creator<NearConnectOption>() {
        /* class com.huawei.softnet.nearby.NearConnectOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearConnectOption createFromParcel(Parcel in) {
            return new NearConnectOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public NearConnectOption[] newArray(int size) {
            return new NearConnectOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte[] mOption;
    private String mServiceId;
    private NearStrategy mStrategy;

    protected NearConnectOption(Parcel in) {
        this.mServiceId = in.readString();
        this.mStrategy = (NearStrategy) in.readParcelable(NearStrategy.class.getClassLoader());
        this.mOption = in.createByteArray();
    }

    private NearConnectOption() {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceId);
        dest.writeParcelable(this.mStrategy, 0);
        dest.writeByteArray(this.mOption);
    }

    public String getServiceId() {
        return this.mServiceId;
    }

    public byte[] getOption() {
        return this.mOption;
    }

    public NearStrategy getStrategy() {
        return this.mStrategy;
    }

    public static class Builder {
        NearConnectOption option = new NearConnectOption();

        public Builder serviceId(String serviceId) {
            this.option.mServiceId = serviceId;
            return this;
        }

        public Builder strategy(NearStrategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder opt(byte[] opt) {
            this.option.mOption = opt;
            return this;
        }

        public NearConnectOption build() {
            return this.option;
        }
    }
}
