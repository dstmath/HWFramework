package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class IConnectionResult implements Parcelable {
    public static final Parcelable.Creator<IConnectionResult> CREATOR = new Parcelable.Creator<IConnectionResult>() {
        /* class com.huawei.softnet.connect.IConnectionResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IConnectionResult createFromParcel(Parcel in) {
            return new IConnectionResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public IConnectionResult[] newArray(int size) {
            return new IConnectionResult[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte[] mResultData;
    private int mStatus;

    protected IConnectionResult(Parcel in) {
        this.mStatus = in.readInt();
        this.mResultData = in.createByteArray();
    }

    private IConnectionResult() {
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStatus);
        dest.writeByteArray(this.mResultData);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public byte[] getResultData() {
        return this.mResultData;
    }

    public static class Builder {
        IConnectionResult info = new IConnectionResult();

        public Builder status(int status) {
            this.info.mStatus = status;
            return this;
        }

        public Builder resultData(byte[] resultData) {
            this.info.mResultData = resultData;
            return this;
        }

        public IConnectionResult build() {
            return this.info;
        }
    }
}
