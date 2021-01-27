package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.HwLog;

public enum NearNetRole implements Parcelable {
    P2P_GO(1),
    P2P_GC(2),
    HOTSPOT_AP(3),
    HOTSPOT_STA(4);
    
    public static final Parcelable.Creator<NearNetRole> CREATOR = new Parcelable.Creator<NearNetRole>() {
        /* class com.huawei.softnet.nearby.NearNetRole.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearNetRole createFromParcel(Parcel source) {
            int val = source.readInt();
            if (val >= 0 && val < NearNetRole.values().length) {
                return NearNetRole.values()[val];
            }
            HwLog.e(NearNetRole.TAG, "createFromParcel over length: " + val);
            return NearNetRole.P2P_GC;
        }

        @Override // android.os.Parcelable.Creator
        public NearNetRole[] newArray(int size) {
            return new NearNetRole[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private static final String TAG = "NearNetRole";
    private int mNetRole;

    private NearNetRole(int netRole) {
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
