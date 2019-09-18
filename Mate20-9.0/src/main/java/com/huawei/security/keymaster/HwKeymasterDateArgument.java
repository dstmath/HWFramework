package com.huawei.security.keymaster;

import android.os.Parcel;
import java.util.Date;

class HwKeymasterDateArgument extends HwKeymasterArgument {
    public final Date date;

    public HwKeymasterDateArgument(int tag, Date date2) {
        super(tag);
        if (HwKeymasterDefs.getTagType(tag) == 1610612736) {
            this.date = date2;
            return;
        }
        throw new IllegalArgumentException("Bad date tag " + tag);
    }

    public HwKeymasterDateArgument(int tag, Parcel in) {
        super(tag);
        this.date = new Date(in.readLong());
    }

    public void writeValue(Parcel out) {
        out.writeLong(this.date.getTime());
    }
}
