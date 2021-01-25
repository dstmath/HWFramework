package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class DevConfig implements Parcelable {
    public static final Parcelable.Creator<DevConfig> CREATOR = new Parcelable.Creator<DevConfig>() {
        /* class com.huawei.softnet.connect.DevConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DevConfig createFromParcel(Parcel in) {
            return new DevConfig(in);
        }

        @Override // android.os.Parcelable.Creator
        public DevConfig[] newArray(int size) {
            return new DevConfig[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private NetRole mNetRole;

    protected DevConfig(Parcel in) {
        this.mNetRole = (NetRole) in.readParcelable(NetRole.class.getClassLoader());
    }

    private DevConfig() {
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mNetRole, 0);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public NetRole getNetRole() {
        return this.mNetRole;
    }

    public static class Builder {
        DevConfig config = new DevConfig();

        public Builder netRole(NetRole netRole) {
            this.config.mNetRole = netRole;
            return this;
        }

        public DevConfig build() {
            return this.config;
        }
    }
}
