package com.android.ims.internal.uce.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UceLong implements Parcelable {
    public static final Creator<UceLong> CREATOR = new Creator<UceLong>() {
        public UceLong createFromParcel(Parcel source) {
            return new UceLong(source, null);
        }

        public UceLong[] newArray(int size) {
            return new UceLong[size];
        }
    };
    private int mClientId;
    private long mUceLong;

    /* synthetic */ UceLong(Parcel source, UceLong -this1) {
        this(source);
    }

    public UceLong() {
        this.mClientId = 1001;
    }

    public long getUceLong() {
        return this.mUceLong;
    }

    public void setUceLong(long uceLong) {
        this.mUceLong = uceLong;
    }

    public int getClientId() {
        return this.mClientId;
    }

    public void setClientId(int nClientId) {
        this.mClientId = nClientId;
    }

    public static UceLong getUceLongInstance() {
        return new UceLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcel(dest);
    }

    private void writeToParcel(Parcel out) {
        out.writeLong(this.mUceLong);
        out.writeInt(this.mClientId);
    }

    private UceLong(Parcel source) {
        this.mClientId = 1001;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mUceLong = source.readLong();
        this.mClientId = source.readInt();
    }
}
