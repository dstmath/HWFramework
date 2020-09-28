package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public enum IStrategy implements Parcelable {
    BLE((byte) 1),
    P2P((byte) 2),
    USB((byte) 4),
    COAP((byte) 8),
    WIFI((byte) 16);
    
    public static final Parcelable.Creator<IStrategy> CREATOR = new Parcelable.Creator<IStrategy>() {
        /* class com.huawei.softnet.connect.IStrategy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IStrategy createFromParcel(Parcel source) {
            return IStrategy.values()[source.readInt()];
        }

        @Override // android.os.Parcelable.Creator
        public IStrategy[] newArray(int size) {
            return new IStrategy[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte mStrategyValue;

    private IStrategy(byte strategyValue) {
        this.mStrategyValue = strategyValue;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public byte getStrategyValue() {
        return this.mStrategyValue;
    }
}
