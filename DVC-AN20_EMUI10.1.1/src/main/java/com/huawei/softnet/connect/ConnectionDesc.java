package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectionDesc implements Parcelable {
    public static final Parcelable.Creator<ConnectionDesc> CREATOR = new Parcelable.Creator<ConnectionDesc>() {
        /* class com.huawei.softnet.connect.ConnectionDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConnectionDesc createFromParcel(Parcel in) {
            return new ConnectionDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public ConnectionDesc[] newArray(int size) {
            return new ConnectionDesc[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private int mFd;
    private boolean mIsIncomming;

    protected ConnectionDesc(Parcel in) {
        this.mIsIncomming = in.createBooleanArray()[0];
        this.mFd = in.readInt();
    }

    private ConnectionDesc() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBooleanArray(new boolean[]{this.mIsIncomming});
        dest.writeInt(this.mFd);
    }

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
        ConnectionDesc info = new ConnectionDesc();

        public Builder isIncomming(boolean isIncomming) {
            this.info.mIsIncomming = isIncomming;
            return this;
        }

        public Builder fd(int fd) {
            this.info.mFd = fd;
            return this;
        }

        public ConnectionDesc build() {
            return this.info;
        }
    }
}
