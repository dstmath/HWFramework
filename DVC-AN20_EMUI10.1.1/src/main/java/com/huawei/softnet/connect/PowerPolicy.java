package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public enum PowerPolicy implements Parcelable {
    High(1),
    Middle(2),
    Low(3),
    Very_Low(4);
    
    public static final Parcelable.Creator<PowerPolicy> CREATOR = new Parcelable.Creator<PowerPolicy>() {
        /* class com.huawei.softnet.connect.PowerPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PowerPolicy createFromParcel(Parcel source) {
            return PowerPolicy.values()[source.readInt()];
        }

        @Override // android.os.Parcelable.Creator
        public PowerPolicy[] newArray(int size) {
            return new PowerPolicy[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mPowerPolicyValue;

    private PowerPolicy(int powerPolicyValue) {
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
