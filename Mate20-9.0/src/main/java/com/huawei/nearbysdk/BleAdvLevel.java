package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public enum BleAdvLevel implements Parcelable {
    VERY_HIGH(3),
    HIGH(2),
    LOW(1),
    VERY_LOW(0);
    
    public static final Parcelable.Creator<BleAdvLevel> CREATOR = null;
    private static SparseArray<BleAdvLevel> map;
    private final int level;

    static {
        int i;
        map = new SparseArray<>();
        for (BleAdvLevel advLevel : values()) {
            map.put(advLevel.getLevel(), advLevel);
        }
        CREATOR = new Parcelable.Creator<BleAdvLevel>() {
            public BleAdvLevel createFromParcel(Parcel source) {
                return BleAdvLevel.values()[source.readInt()];
            }

            public BleAdvLevel[] newArray(int size) {
                return new BleAdvLevel[size];
            }
        };
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

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "BleAdvLevel{name=" + name() + ", level=" + this.level + '}';
    }
}
