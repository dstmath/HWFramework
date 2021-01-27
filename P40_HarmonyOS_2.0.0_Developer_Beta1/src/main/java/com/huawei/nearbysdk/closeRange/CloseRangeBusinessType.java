package com.huawei.nearbysdk.closeRange;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import com.huawei.nearbysdk.HwLog;

public enum CloseRangeBusinessType implements Parcelable {
    iConnect((byte) 1);
    
    public static final Parcelable.Creator<CloseRangeBusinessType> CREATOR = new Parcelable.Creator<CloseRangeBusinessType>() {
        /* class com.huawei.nearbysdk.closeRange.CloseRangeBusinessType.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CloseRangeBusinessType createFromParcel(Parcel source) {
            int val = source.readInt();
            if (val >= 0 && val < CloseRangeBusinessType.values().length) {
                return CloseRangeBusinessType.values()[val];
            }
            HwLog.e(CloseRangeBusinessType.TAG, "createFromParcel over length: " + val);
            return CloseRangeBusinessType.iConnect;
        }

        @Override // android.os.Parcelable.Creator
        public CloseRangeBusinessType[] newArray(int size) {
            return new CloseRangeBusinessType[size];
        }
    };
    private static final String TAG = "CloseRangeBusinessType";
    private static final SparseArray<CloseRangeBusinessType> lookupMap = new SparseArray<>();
    private byte tag;

    static {
        CloseRangeBusinessType[] values = values();
        for (CloseRangeBusinessType value : values) {
            lookupMap.put(value.getTag(), value);
        }
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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
