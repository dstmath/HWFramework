package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;

public enum NearPowerPolicy implements Parcelable {
    High(1),
    Middle(2),
    Low(3),
    Very_Low(4);
    
    public static final Parcelable.Creator<NearPowerPolicy> CREATOR = new Parcelable.Creator<NearPowerPolicy>() {
        /* class com.huawei.softnet.nearby.NearPowerPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearPowerPolicy createFromParcel(Parcel source) {
            return NearPowerPolicy.values()[source.readInt()];
        }

        @Override // android.os.Parcelable.Creator
        public NearPowerPolicy[] newArray(int size) {
            return new NearPowerPolicy[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mPowerPolicyValue;

    private NearPowerPolicy(int powerPolicyValue) {
        this.mPowerPolicyValue = powerPolicyValue;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public int getPowerPolicyValue() {
        return this.mPowerPolicyValue;
    }
}
