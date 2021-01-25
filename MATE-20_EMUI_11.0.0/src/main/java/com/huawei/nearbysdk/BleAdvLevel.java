package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public enum BleAdvLevel implements Parcelable {
    VERY_HIGH(3),
    HIGH(2),
    LOW(1),
    VERY_LOW(0);
    
    public static final Parcelable.Creator<BleAdvLevel> CREATOR = new Parcelable.Creator<BleAdvLevel>() {
        /* class com.huawei.nearbysdk.BleAdvLevel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BleAdvLevel createFromParcel(Parcel source) {
            int val = source.readInt();
            if (val >= 0 && val < BleAdvLevel.values().length) {
                return BleAdvLevel.values()[val];
            }
            HwLog.e(BleAdvLevel.TAG, "createFromParcel over length: " + val);
            return BleAdvLevel.VERY_LOW;
        }

        @Override // android.os.Parcelable.Creator
        public BleAdvLevel[] newArray(int size) {
            return new BleAdvLevel[size];
        }
    };
    private static final String TAG = "BleAdvLevel";
    private static SparseArray<BleAdvLevel> map = new SparseArray<>();
    private final int level;

    static {
        BleAdvLevel[] values = values();
        for (BleAdvLevel advLevel : values) {
            map.put(advLevel.getLevel(), advLevel);
        }
    }

    private BleAdvLevel(int level2) {
        this.level = level2;
    }

    public int getLevel() {
        return this.level;
    }

    public static BleAdvLevel fromLevel(int level2) {
        return map.get(level2);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "BleAdvLevel{ name=" + name() + ", level=" + this.level + '}';
    }
}
