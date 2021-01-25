package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.HwLog;

public enum NearPowerPolicy implements Parcelable {
    High(1),
    Middle(2),
    Low(3),
    Very_Low(4);
    
    public static final Parcelable.Creator<NearPowerPolicy> CREATOR = new Parcelable.Creator<NearPowerPolicy>() {
        /* class com.huawei.softnet.nearby.NearPowerPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearPowerPolicy createFromParcel(Parcel source) {
            int val = source.readInt();
            if (val >= 0 && val < NearPowerPolicy.values().length) {
                return NearPowerPolicy.values()[val];
            }
            HwLog.e(NearPowerPolicy.TAG, "createFromParcel over length: " + val);
            return NearPowerPolicy.Very_Low;
        }

        @Override // android.os.Parcelable.Creator
        public NearPowerPolicy[] newArray(int size) {
            return new NearPowerPolicy[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private static final String TAG = "NearPowerPolicy";
    private int mPowerPolicyValue;

    private NearPowerPolicy(int powerPolicyValue) {
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
