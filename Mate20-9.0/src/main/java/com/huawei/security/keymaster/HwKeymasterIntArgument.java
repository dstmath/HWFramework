package com.huawei.security.keymaster;

import android.os.Parcel;

class HwKeymasterIntArgument extends HwKeymasterArgument {
    public final int value;

    public HwKeymasterIntArgument(int tag, int value2) {
        super(tag);
        int tagType = HwKeymasterDefs.getTagType(tag);
        if (tagType == 268435456 || tagType == 536870912 || tagType == 805306368 || tagType == 1073741824) {
            this.value = value2;
            return;
        }
        throw new IllegalArgumentException("Bad int tag " + tag);
    }

    public HwKeymasterIntArgument(int tag, Parcel in) {
        super(tag);
        this.value = in.readInt();
    }

    public void writeValue(Parcel out) {
        out.writeInt(this.value);
    }
}
