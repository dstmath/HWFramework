package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class IDevConfig implements Parcelable {
    public static final Parcelable.Creator<IDevConfig> CREATOR = new Parcelable.Creator<IDevConfig>() {
        /* class com.huawei.softnet.connect.IDevConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IDevConfig createFromParcel(Parcel in) {
            return new IDevConfig(in);
        }

        @Override // android.os.Parcelable.Creator
        public IDevConfig[] newArray(int size) {
            return new IDevConfig[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private INetRole mNetRole;

    protected IDevConfig(Parcel in) {
        this.mNetRole = (INetRole) in.readParcelable(INetRole.class.getClassLoader());
    }

    private IDevConfig() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mNetRole, 0);
    }

    public int describeContents() {
        return 0;
    }

    public INetRole getNetRole() {
        return this.mNetRole;
    }

    public static class Builder {
        IDevConfig config = new IDevConfig();

        public Builder netRole(INetRole netRole) {
            this.config.mNetRole = netRole;
            return this;
        }

        public IDevConfig build() {
            return this.config;
        }
    }
}
