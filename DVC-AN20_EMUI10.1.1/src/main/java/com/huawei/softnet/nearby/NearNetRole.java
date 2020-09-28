package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;

public enum NearNetRole implements Parcelable {
    P2P_GO(1),
    P2P_GC(2),
    HOTSPOT_AP(3),
    HOTSPOT_STA(4);
    
    public static final Parcelable.Creator<NearNetRole> CREATOR = new Parcelable.Creator<NearNetRole>() {
        /* class com.huawei.softnet.nearby.NearNetRole.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearNetRole createFromParcel(Parcel source) {
            return NearNetRole.values()[source.readInt()];
        }

        @Override // android.os.Parcelable.Creator
        public NearNetRole[] newArray(int size) {
            return new NearNetRole[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mNetRole;

    private NearNetRole(int netRole) {
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
