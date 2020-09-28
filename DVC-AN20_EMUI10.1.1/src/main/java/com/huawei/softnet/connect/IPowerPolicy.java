package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public enum IPowerPolicy implements Parcelable {
    High(1),
    Middle(2),
    Low(3),
    Very_Low(4);
    
    public static final Parcelable.Creator<IPowerPolicy> CREATOR = new Parcelable.Creator<IPowerPolicy>() {
        /* class com.huawei.softnet.connect.IPowerPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IPowerPolicy createFromParcel(Parcel source) {
            return IPowerPolicy.values()[source.readInt()];
        }

        @Override // android.os.Parcelable.Creator
        public IPowerPolicy[] newArray(int size) {
            return new IPowerPolicy[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mPowerPolicyValue;

    private IPowerPolicy(int powerPolicyValue) {
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
