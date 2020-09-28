package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class IConnectOption implements Parcelable {
    public static final Parcelable.Creator<IConnectOption> CREATOR = new Parcelable.Creator<IConnectOption>() {
        /* class com.huawei.softnet.connect.IConnectOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IConnectOption createFromParcel(Parcel in) {
            return new IConnectOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public IConnectOption[] newArray(int size) {
            return new IConnectOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private String mExtInfo;
    private byte[] mOption;
    private String mServiceId;
    private IStrategy mStrategy;

    protected IConnectOption(Parcel in) {
        this.mServiceId = in.readString();
        this.mStrategy = (IStrategy) in.readParcelable(IStrategy.class.getClassLoader());
        this.mOption = in.createByteArray();
        this.mExtInfo = in.readString();
    }

    private IConnectOption() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceId);
        dest.writeParcelable(this.mStrategy, 0);
        dest.writeByteArray(this.mOption);
        dest.writeString(this.mExtInfo);
    }

    public String getServiceId() {
        return this.mServiceId;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public byte[] getOption() {
        return this.mOption;
    }

    public IStrategy getStrategy() {
        return this.mStrategy;
    }

    public static class Builder {
        IConnectOption option = new IConnectOption();

        public Builder serviceId(String serviceId) {
            this.option.mServiceId = serviceId;
            return this;
        }

        public Builder extInfo(String extInfo) {
            this.option.mExtInfo = extInfo;
            return this;
        }

        public Builder strategy(IStrategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder opt(byte[] opt) {
            this.option.mOption = opt;
            return this;
        }

        public IConnectOption build() {
            return this.option;
        }
    }
}
