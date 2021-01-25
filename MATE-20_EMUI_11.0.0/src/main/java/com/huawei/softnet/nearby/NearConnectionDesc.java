package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;

public class NearConnectionDesc implements Parcelable {
    private static final int ARRAY_SIZE = 1;
    public static final Parcelable.Creator<NearConnectionDesc> CREATOR = new Parcelable.Creator<NearConnectionDesc>() {
        /* class com.huawei.softnet.nearby.NearConnectionDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearConnectionDesc createFromParcel(Parcel in) {
            return new NearConnectionDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public NearConnectionDesc[] newArray(int size) {
            return new NearConnectionDesc[size];
        }
    };
    private static final int INDEX_OF_ARRAY = 0;
    private static final int PARCEL_FLAG = 0;
    private int mFd;
    private boolean mIsIncomming;

    protected NearConnectionDesc(Parcel in) {
        this.mIsIncomming = in.createBooleanArray()[0];
        this.mFd = in.readInt();
    }

    private NearConnectionDesc() {
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBooleanArray(new boolean[]{this.mIsIncomming});
        dest.writeInt(this.mFd);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public boolean getIsIncomming() {
        return this.mIsIncomming;
    }

    public int getFd() {
        return this.mFd;
    }

    public static class Builder {
        NearConnectionDesc info = new NearConnectionDesc();

        public Builder isIncomming(boolean isIncomming) {
            this.info.mIsIncomming = isIncomming;
            return this;
        }

        public Builder fd(int fd) {
            this.info.mFd = fd;
            return this;
        }

        public NearConnectionDesc build() {
            return this.info;
        }
    }
}
