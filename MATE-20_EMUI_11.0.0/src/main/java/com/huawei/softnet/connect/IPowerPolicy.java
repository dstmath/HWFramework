package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public enum IPowerPolicy implements Parcelable {
    High(1),
    Middle(2),
    Low(3),
    Very_Low(4);
    
    public static final Parcelable.Creator<IPowerPolicy> CREATOR = new Parcelable.Creator<IPowerPolicy>() {
        /* class com.huawei.softnet.connect.IPowerPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IPowerPolicy createFromParcel(Parcel source) {
            int val = source.readInt();
            if (val >= 0 && val < IPowerPolicy.values().length) {
                return IPowerPolicy.values()[val];
            }
            Log.e(IPowerPolicy.TAG, "createFromParcel over length: " + val);
            return IPowerPolicy.Very_Low;
        }

        @Override // android.os.Parcelable.Creator
        public IPowerPolicy[] newArray(int size) {
            return new IPowerPolicy[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private static final String TAG = "IPowerPolicy";
    private int mPowerPolicyValue;

    private IPowerPolicy(int powerPolicyValue) {
        this.mPowerPolicyValue = powerPolicyValue;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public int getPowerPolicyValue() {
        return this.mPowerPolicyValue;
    }
}
