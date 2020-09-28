package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public enum NetRole implements Parcelable {
    P2P_GO(1),
    P2P_GC(2),
    HOTSPOT_AP(3),
    HOTSPOT_STA(4);
    
    public static final Parcelable.Creator<NetRole> CREATOR = new Parcelable.Creator<NetRole>() {
        /* class com.huawei.softnet.connect.NetRole.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NetRole createFromParcel(Parcel source) {
            return NetRole.values()[source.readInt()];
        }

        @Override // android.os.Parcelable.Creator
        public NetRole[] newArray(int size) {
            return new NetRole[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mNetRole;

    private NetRole(int netRole) {
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
