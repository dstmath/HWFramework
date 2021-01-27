package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectionResult implements Parcelable {
    public static final Parcelable.Creator<ConnectionResult> CREATOR = new Parcelable.Creator<ConnectionResult>() {
        /* class com.huawei.softnet.connect.ConnectionResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConnectionResult createFromParcel(Parcel in) {
            return new ConnectionResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public ConnectionResult[] newArray(int size) {
            return new ConnectionResult[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private byte[] mResultData;
    private int mStatus;

    protected ConnectionResult(Parcel in) {
        this.mStatus = in.readInt();
        this.mResultData = in.createByteArray();
    }

    private ConnectionResult() {
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
        ConnectionResult info = new ConnectionResult();

        public Builder status(int status) {
            this.info.mStatus = status;
            return this;
        }

        public Builder resultData(byte[] resultData) {
            this.info.mResultData = resultData;
            return this;
        }

        public ConnectionResult build() {
            return this.info;
        }
    }
}
