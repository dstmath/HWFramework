package com.huawei.security;

import android.os.Parcel;
import android.os.Parcelable;

public class HwKeystoreArguments {
    public static final Parcelable.Creator<HwKeystoreArguments> CREATOR = new Parcelable.Creator<HwKeystoreArguments>() {
        public HwKeystoreArguments createFromParcel(Parcel in) {
            return new HwKeystoreArguments(in);
        }

        public HwKeystoreArguments[] newArray(int size) {
            return new HwKeystoreArguments[size];
        }
    };
    public byte[][] args;

    public HwKeystoreArguments() {
        this.args = null;
    }

    public HwKeystoreArguments(byte[][] args2) {
        this.args = args2;
    }

    private HwKeystoreArguments(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.args == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(this.args.length);
        for (byte[] arg : this.args) {
            out.writeByteArray(arg);
        }
    }

    private void readFromParcel(Parcel in) {
        int length = in.readInt();
        this.args = new byte[length][];
        for (int i = 0; i < length; i++) {
            this.args[i] = in.createByteArray();
        }
    }

    public int describeContents() {
        return 0;
    }
}
