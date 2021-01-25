package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public enum INetRole implements Parcelable {
    P2P_GO(1),
    P2P_GC(2),
    HOTSPOT_AP(3),
    HOTSPOT_STA(4);
    
    public static final Parcelable.Creator<INetRole> CREATOR = new Parcelable.Creator<INetRole>() {
        /* class com.huawei.softnet.connect.INetRole.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public INetRole createFromParcel(Parcel source) {
            int val = source.readInt();
            if (val >= 0 && val < INetRole.values().length) {
                return INetRole.values()[val];
            }
            Log.e(INetRole.TAG, "createFromParcel over length: " + val);
            return INetRole.P2P_GC;
        }

        @Override // android.os.Parcelable.Creator
        public INetRole[] newArray(int size) {
            return new INetRole[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private static final String TAG = "INetRole";
    private int mNetRole;

    private INetRole(int netRole) {
        this.mNetRole = netRole;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public int getNetRole() {
        return this.mNetRole;
    }
}
