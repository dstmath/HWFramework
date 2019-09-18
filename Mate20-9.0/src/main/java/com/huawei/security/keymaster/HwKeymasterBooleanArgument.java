package com.huawei.security.keymaster;

import android.os.Parcel;

class HwKeymasterBooleanArgument extends HwKeymasterArgument {
    public HwKeymasterBooleanArgument(int tag) {
        super(tag);
        if (HwKeymasterDefs.getTagType(tag) != 1879048192) {
            throw new IllegalArgumentException("Bad bool tag " + tag);
        }
    }

    public HwKeymasterBooleanArgument(int tag, Parcel in) {
        super(tag);
    }

    public void writeValue(Parcel out) {
    }
}
