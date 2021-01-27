package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public enum IStrategy implements Parcelable {
    BLE((byte) 1),
    P2P((byte) 2),
    USB((byte) 4),
    COAP((byte) 8),
    WIFI((byte) 16),
    NAN((byte) 32);
    
    public static final Parcelable.Creator<IStrategy> CREATOR = new Parcelable.Creator<IStrategy>() {
        /* class com.huawei.softnet.connect.IStrategy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IStrategy createFromParcel(Parcel source) {
            int val = source.readInt();
            if (val >= 0 && val < IStrategy.values().length) {
                return IStrategy.values()[val];
            }
            Log.e(IStrategy.TAG, "createFromParcel over length: " + val);
            return IStrategy.BLE;
        }

        @Override // android.os.Parcelable.Creator
        public IStrategy[] newArray(int size) {
            return new IStrategy[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private static final String TAG = "IStrategy";
    private byte mStrategyValue;

    private IStrategy(byte strategyValue) {
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
