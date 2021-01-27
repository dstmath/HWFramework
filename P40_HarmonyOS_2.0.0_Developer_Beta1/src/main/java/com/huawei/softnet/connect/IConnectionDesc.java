package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class IConnectionDesc implements Parcelable {
    private static final int ARRAY_SIZE = 1;
    public static final Parcelable.Creator<IConnectionDesc> CREATOR = new Parcelable.Creator<IConnectionDesc>() {
        /* class com.huawei.softnet.connect.IConnectionDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IConnectionDesc createFromParcel(Parcel in) {
            return new IConnectionDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public IConnectionDesc[] newArray(int size) {
            return new IConnectionDesc[size];
        }
    };
    private static final int INDEX_OF_ARRAY = 0;
    private static final int PARCEL_FLAG = 0;
    private int mFd;
    private boolean mIsIncomming;

    protected IConnectionDesc(Parcel in) {
        this.mIsIncomming = in.createBooleanArray()[0];
        this.mFd = in.readInt();
    }

    private IConnectionDesc() {
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
        IConnectionDesc info = new IConnectionDesc();

        public Builder isIncomming(boolean isIncomming) {
            this.info.mIsIncomming = isIncomming;
            return this;
        }

        public Builder fd(int fd) {
            this.info.mFd = fd;
            return this;
        }

        public IConnectionDesc build() {
            return this.info;
        }
    }
}
