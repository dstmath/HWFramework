package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public enum BleScanLevel implements Parcelable {
    VERY_HIGH(3),
    HIGH(2),
    LOW(1),
    VERY_LOW(0);
    
    public static final Parcelable.Creator<BleScanLevel> CREATOR = new Parcelable.Creator<BleScanLevel>() {
        /* class com.huawei.nearbysdk.BleScanLevel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BleScanLevel createFromParcel(Parcel source) {
            int val = source.readInt();
            if (val >= 0 && val < BleScanLevel.values().length) {
                return BleScanLevel.values()[val];
            }
            HwLog.e(BleScanLevel.TAG, "createFromParcel over length: " + val);
            return BleScanLevel.VERY_LOW;
        }

        @Override // android.os.Parcelable.Creator
        public BleScanLevel[] newArray(int size) {
            return new BleScanLevel[size];
        }
    };
    private static final String TAG = "BleScanLevel";
    private static SparseArray<BleScanLevel> map = new SparseArray<>();
    private final int level;

    static {
        BleScanLevel[] values = values();
        for (BleScanLevel scanLevel : values) {
            map.put(scanLevel.getLevel(), scanLevel);
        }
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
        return "BleScanLevel{name=" + name() + ", level=" + this.level + '}';
    }
}
