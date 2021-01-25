package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectOption implements Parcelable {
    public static final Parcelable.Creator<ConnectOption> CREATOR = new Parcelable.Creator<ConnectOption>() {
        /* class com.huawei.softnet.connect.ConnectOption.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConnectOption createFromParcel(Parcel in) {
            return new ConnectOption(in);
        }

        @Override // android.os.Parcelable.Creator
        public ConnectOption[] newArray(int size) {
            return new ConnectOption[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    public static final int STATE_CONNECTION_COMPLETED = 0;
    public static final int STATE_DISCONNECT = 1;
    private String mExtInfo;
    private byte[] mOption;
    private String mServiceId;
    private Strategy mStrategy;

    protected ConnectOption(Parcel in) {
        this.mServiceId = in.readString();
        this.mStrategy = (Strategy) in.readParcelable(Strategy.class.getClassLoader());
        this.mOption = in.createByteArray();
        this.mExtInfo = in.readString();
    }

    private ConnectOption() {
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

    public Strategy getStrategy() {
        return this.mStrategy;
    }

    public static class Builder {
        ConnectOption option = new ConnectOption();

        public Builder serviceId(String serviceId) {
            this.option.mServiceId = serviceId;
            return this;
        }

        public Builder extInfo(String extInfo) {
            this.option.mExtInfo = extInfo;
            return this;
        }

        public Builder strategy(Strategy strategy) {
            this.option.mStrategy = strategy;
            return this;
        }

        public Builder opt(byte[] opt) {
            this.option.mOption = opt;
            return this;
        }

        public ConnectOption build() {
            return this.option;
        }
    }
}
