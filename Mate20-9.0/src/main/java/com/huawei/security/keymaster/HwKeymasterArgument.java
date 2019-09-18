package com.huawei.security.keymaster;

import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;

abstract class HwKeymasterArgument implements Parcelable {
    public static final Parcelable.Creator<HwKeymasterArgument> CREATOR = new Parcelable.Creator<HwKeymasterArgument>() {
        public HwKeymasterArgument createFromParcel(Parcel in) {
            int pos = in.dataPosition();
            int tag = in.readInt();
            int tagType = HwKeymasterDefs.getTagType(tag);
            if (tagType == Integer.MIN_VALUE || tagType == -1879048192) {
                return new HwKeymasterBlobArgument(tag, in);
            }
            if (tagType != -1610612736) {
                if (tagType == 268435456 || tagType == 536870912 || tagType == 805306368 || tagType == 1073741824) {
                    return new HwKeymasterIntArgument(tag, in);
                }
                if (tagType != 1342177280) {
                    if (tagType == 1610612736) {
                        return new HwKeymasterDateArgument(tag, in);
                    }
                    if (tagType == 1879048192) {
                        return new HwKeymasterBooleanArgument(tag, in);
                    }
                    throw new ParcelFormatException("Bad tag: " + tag + " at " + pos);
                }
            }
            return new HwKeymasterLongArgument(tag, in);
        }

        public HwKeymasterArgument[] newArray(int size) {
            return new HwKeymasterArgument[size];
        }
    };
    public final int tag;

    public abstract void writeValue(Parcel parcel);

    protected HwKeymasterArgument(int tag2) {
        this.tag = tag2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.tag);
        writeValue(out);
    }
}
