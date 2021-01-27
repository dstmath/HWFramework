package com.huawei.security.keymaster;

import android.os.Parcel;

/* access modifiers changed from: package-private */
public class HwKeymasterBooleanArgument extends HwKeymasterArgument {
    public HwKeymasterBooleanArgument(int tag) {
        super(tag);
        if (HwKeymasterDefs.getTagType(tag) != 1879048192) {
            throw new IllegalArgumentException("Bad bool tag " + tag);
        }
    }

    public HwKeymasterBooleanArgument(int tag, Parcel in) {
        super(tag);
    }

    @Override // com.huawei.security.keymaster.HwKeymasterArgument
    public void writeValue(Parcel out) {
    }
}
