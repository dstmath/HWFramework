package com.huawei.security.keymaster;

import android.os.Parcel;

/* access modifiers changed from: package-private */
public class HwKeymasterLongArgument extends HwKeymasterArgument {
    public final long value;

    public HwKeymasterLongArgument(int tag, long value2) {
        super(tag);
        int tagType = HwKeymasterDefs.getTagType(tag);
        if (tagType == -1610612736 || tagType == 1342177280) {
            this.value = value2;
            return;
        }
        throw new IllegalArgumentException("Bad long tag " + tag);
    }

    public HwKeymasterLongArgument(int tag, Parcel in) {
        super(tag);
        this.value = in.readLong();
    }

    @Override // com.huawei.security.keymaster.HwKeymasterArgument
    public void writeValue(Parcel out) {
        out.writeLong(this.value);
    }
}
