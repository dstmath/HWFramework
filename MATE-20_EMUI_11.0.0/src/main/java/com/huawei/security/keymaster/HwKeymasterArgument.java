package com.huawei.security.keymaster;

import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;

/* access modifiers changed from: package-private */
public abstract class HwKeymasterArgument implements Parcelable {
    public static final Parcelable.Creator<HwKeymasterArgument> CREATOR = new Parcelable.Creator<HwKeymasterArgument>() {
        /* class com.huawei.security.keymaster.HwKeymasterArgument.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwKeymasterArgument createFromParcel(Parcel in) {
            int pos = in.dataPosition();
            int keymasterTag = in.readInt();
            int tagType = HwKeymasterDefs.getTagType(keymasterTag);
            if (tagType == Integer.MIN_VALUE || tagType == -1879048192) {
                return new HwKeymasterBlobArgument(keymasterTag, in);
            }
            if (tagType != -1610612736) {
                if (tagType == 268435456 || tagType == 536870912 || tagType == 805306368 || tagType == 1073741824) {
                    return new HwKeymasterIntArgument(keymasterTag, in);
                }
                if (tagType != 1342177280) {
                    if (tagType == 1610612736) {
                        return new HwKeymasterDateArgument(keymasterTag, in);
                    }
                    if (tagType == 1879048192) {
                        return new HwKeymasterBooleanArgument(keymasterTag, in);
                    }
                    throw new ParcelFormatException("Bad tag: " + keymasterTag + " at " + pos);
                }
            }
            return new HwKeymasterLongArgument(keymasterTag, in);
        }

        @Override // android.os.Parcelable.Creator
        public HwKeymasterArgument[] newArray(int size) {
            return new HwKeymasterArgument[size];
        }
    };
    public final int tag;

    public abstract void writeValue(Parcel parcel);

    protected HwKeymasterArgument(int tag2) {
        this.tag = tag2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.tag);
        writeValue(out);
    }
}
