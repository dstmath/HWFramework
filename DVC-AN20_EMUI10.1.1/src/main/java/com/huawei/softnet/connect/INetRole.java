package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public enum INetRole implements Parcelable {
    P2P_GO(1),
    P2P_GC(2),
    HOTSPOT_AP(3),
    HOTSPOT_STA(4);
    
    public static final Parcelable.Creator<INetRole> CREATOR = new Parcelable.Creator<INetRole>() {
        /* class com.huawei.softnet.connect.INetRole.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public INetRole createFromParcel(Parcel source) {
            return INetRole.values()[source.readInt()];
        }

        @Override // android.os.Parcelable.Creator
        public INetRole[] newArray(int size) {
            return new INetRole[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mNetRole;

    private INetRole(int netRole) {
        this.mNetRole = netRole;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public int getNetRole() {
        return this.mNetRole;
    }
}
