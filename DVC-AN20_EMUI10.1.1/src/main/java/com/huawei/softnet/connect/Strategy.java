package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public enum Strategy implements Parcelable {
    BLE((byte) 1),
    P2P((byte) 2),
    USB((byte) 4),
    COAP((byte) 8),
    WIFI((byte) 16);
    
    public static final Parcelable.Creator<Strategy> CREATOR = new Parcelable.Creator<Strategy>() {
        /* class com.huawei.softnet.connect.Strategy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Strategy createFromParcel(Parcel source) {
            return Strategy.values()[source.readInt()];
        }

        @Override // android.os.Parcelable.Creator
        public Strategy[] newArray(int size) {
            return new Strategy[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte mStrategyValue;

    private Strategy(byte strategyValue) {
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
