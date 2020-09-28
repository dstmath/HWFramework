package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;

public class NearDevConfig implements Parcelable {
    public static final Parcelable.Creator<NearDevConfig> CREATOR = new Parcelable.Creator<NearDevConfig>() {
        /* class com.huawei.softnet.nearby.NearDevConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearDevConfig createFromParcel(Parcel in) {
            return new NearDevConfig(in);
        }

        @Override // android.os.Parcelable.Creator
        public NearDevConfig[] newArray(int size) {
            return new NearDevConfig[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private NearNetRole mNetRole;

    protected NearDevConfig(Parcel in) {
        this.mNetRole = (NearNetRole) in.readParcelable(NearNetRole.class.getClassLoader());
    }

    private NearDevConfig() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mNetRole, 0);
    }

    public int describeContents() {
        return 0;
    }

    public NearNetRole getNetRole() {
        return this.mNetRole;
    }

    public static class Builder {
        NearDevConfig config = new NearDevConfig();

        public Builder netRole(NearNetRole netRole) {
            this.config.mNetRole = netRole;
            return this;
        }

        public NearDevConfig build() {
            return this.config;
        }
    }
}
