package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.HwLog;

public enum NearStrategy implements Parcelable {
    BLE((byte) 1),
    P2P((byte) 2),
    USB((byte) 4),
    COAP((byte) 8),
    WIFI((byte) 16),
    NAN((byte) 32);
    
    public static final Parcelable.Creator<NearStrategy> CREATOR = new Parcelable.Creator<NearStrategy>() {
        /* class com.huawei.softnet.nearby.NearStrategy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearStrategy createFromParcel(Parcel source) {
            int val = source.readInt();
            if (val >= 0 && val < NearStrategy.values().length) {
                return NearStrategy.values()[val];
            }
            HwLog.e(NearStrategy.TAG, "createFromParcel over length: " + val);
            return NearStrategy.BLE;
        }

        @Override // android.os.Parcelable.Creator
        public NearStrategy[] newArray(int size) {
            return new NearStrategy[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private static final String TAG = "NearStrategy";
    private byte mStrategyValue;

    private NearStrategy(byte strategyValue) {
        this.mStrategyValue = strategyValue;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public byte getStrategyValue() {
        return this.mStrategyValue;
    }
}
