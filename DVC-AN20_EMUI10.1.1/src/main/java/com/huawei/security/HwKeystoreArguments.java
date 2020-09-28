package com.huawei.security;

import android.os.Parcel;
import android.os.Parcelable;

public class HwKeystoreArguments {
    public static final Parcelable.Creator<HwKeystoreArguments> CREATOR = new Parcelable.Creator<HwKeystoreArguments>() {
        /* class com.huawei.security.HwKeystoreArguments.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwKeystoreArguments createFromParcel(Parcel in) {
            return new HwKeystoreArguments(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwKeystoreArguments[] newArray(int size) {
            return new HwKeystoreArguments[size];
        }
    };
    public byte[][] mArgs;

    public HwKeystoreArguments() {
        this.mArgs = null;
    }

    public HwKeystoreArguments(byte[][] args) {
        this.mArgs = args;
    }

    private HwKeystoreArguments(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        byte[][] bArr = this.mArgs;
        if (bArr == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(bArr.length);
        for (byte[] arg : this.mArgs) {
            out.writeByteArray(arg);
        }
    }

    private void readFromParcel(Parcel in) {
        int length = in.readInt();
        this.mArgs = new byte[length][];
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                this.mArgs[i] = in.createByteArray();
            }
        }
    }

    public int describeContents() {
        return 0;
    }
}
