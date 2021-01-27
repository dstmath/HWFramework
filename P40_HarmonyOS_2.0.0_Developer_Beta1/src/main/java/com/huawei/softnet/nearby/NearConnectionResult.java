package com.huawei.softnet.nearby;

import android.os.Parcel;
import android.os.Parcelable;

public class NearConnectionResult implements Parcelable {
    public static final Parcelable.Creator<NearConnectionResult> CREATOR = new Parcelable.Creator<NearConnectionResult>() {
        /* class com.huawei.softnet.nearby.NearConnectionResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NearConnectionResult createFromParcel(Parcel in) {
            return new NearConnectionResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public NearConnectionResult[] newArray(int size) {
            return new NearConnectionResult[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte[] mResultData;
    private int mStatus;

    protected NearConnectionResult(Parcel in) {
        this.mStatus = in.readInt();
        this.mResultData = in.createByteArray();
    }

    private NearConnectionResult() {
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
        NearConnectionResult info = new NearConnectionResult();

        public Builder status(int status) {
            this.info.mStatus = status;
            return this;
        }

        public Builder resultData(byte[] resultData) {
            this.info.mResultData = resultData;
            return this;
        }

        public NearConnectionResult build() {
            return this.info;
        }
    }
}
