package com.huawei.security.keymaster;

import android.os.Parcel;

/* access modifiers changed from: package-private */
public class HwKeymasterBlobArgument extends HwKeymasterArgument {
    public final byte[] blob;

    public HwKeymasterBlobArgument(int tag, byte[] blob2) {
        super(tag);
        int tagType = HwKeymasterDefs.getTagType(tag);
        if (tagType == Integer.MIN_VALUE || tagType == -1879048192) {
            this.blob = blob2;
            return;
        }
        throw new IllegalArgumentException("Bad blob tag " + tag);
    }

    public HwKeymasterBlobArgument(int tag, Parcel in) {
        super(tag);
        this.blob = in.createByteArray();
    }

    @Override // com.huawei.security.keymaster.HwKeymasterArgument
    public void writeValue(Parcel out) {
        out.writeByteArray(this.blob);
    }
}
