package com.huawei.nearbysdk.closeRange;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public enum CloseRangeBusinessType implements Parcelable {
    iConnect((byte) 1);
    
    public static final Parcelable.Creator<CloseRangeBusinessType> CREATOR = null;
    private static final SparseArray<CloseRangeBusinessType> lookupMap = null;
    private byte tag;

    static {
        int i;
        lookupMap = new SparseArray<>();
        for (CloseRangeBusinessType value : values()) {
            lookupMap.put(value.getTag(), value);
        }
        CREATOR = new Parcelable.Creator<CloseRangeBusinessType>() {
            public CloseRangeBusinessType createFromParcel(Parcel source) {
                return CloseRangeBusinessType.values()[source.readInt()];
            }

            public CloseRangeBusinessType[] newArray(int size) {
                return new CloseRangeBusinessType[size];
            }
        };
    }

    private CloseRangeBusinessType(byte tag2) {
        this.tag = tag2;
    }

    public byte getTag() {
        return this.tag;
    }

    public static CloseRangeBusinessType fromTag(byte tag2) {
        return lookupMap.get(tag2);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public int describeContents() {
        return 0;
    }
}
