package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public enum BleScanLevel implements Parcelable {
    VERY_HIGH(3),
    HIGH(2),
    LOW(1),
    VERY_LOW(0);
    
    public static final Parcelable.Creator<BleScanLevel> CREATOR = null;
    private static SparseArray<BleScanLevel> map;
    private final int level;

    static {
        int i;
        map = new SparseArray<>();
        for (BleScanLevel scanLevel : values()) {
            map.put(scanLevel.getLevel(), scanLevel);
        }
        CREATOR = new Parcelable.Creator<BleScanLevel>() {
            public BleScanLevel createFromParcel(Parcel source) {
                return BleScanLevel.values()[source.readInt()];
            }

            public BleScanLevel[] newArray(int size) {
                return new BleScanLevel[size];
            }
        };
    }

    private BleScanLevel(int level2) {
        this.level = level2;
    }

    public static BleScanLevel fromLevel(int level2) {
        return map.get(level2);
    }

    public int getLevel() {
        return this.level;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "BleScanLevel{name=" + name() + ", level=" + this.level + '}';
    }
}
