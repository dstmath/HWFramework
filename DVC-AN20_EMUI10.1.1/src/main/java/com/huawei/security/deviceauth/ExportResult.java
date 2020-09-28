package com.huawei.security.deviceauth;

import android.os.Parcel;
import android.os.Parcelable;

public class ExportResult implements Parcelable {
    public static final Parcelable.Creator<ExportResult> CREATOR = new Parcelable.Creator<ExportResult>() {
        /* class com.huawei.security.deviceauth.ExportResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ExportResult createFromParcel(Parcel in) {
            return new ExportResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public ExportResult[] newArray(int size) {
            return new ExportResult[size];
        }
    };
    private byte[] mAuthInfoBlob;
    private int mBlobLength;
    private int mResult;

    public ExportResult() {
        this.mResult = 1;
        this.mAuthInfoBlob = new byte[0];
        this.mBlobLength = 0;
    }

    public ExportResult(int result, byte[] authInfoBlob) {
        this.mResult = result;
        if (authInfoBlob != null) {
            this.mAuthInfoBlob = (byte[]) authInfoBlob.clone();
            this.mBlobLength = this.mAuthInfoBlob.length;
            return;
        }
        this.mAuthInfoBlob = new byte[0];
        this.mBlobLength = 0;
    }

    protected ExportResult(Parcel in) {
        this.mResult = in.readInt();
        this.mBlobLength = in.readInt();
        int i = this.mBlobLength;
        if (i >= 0) {
            this.mAuthInfoBlob = new byte[i];
            in.readByteArray(this.mAuthInfoBlob);
            return;
        }
        this.mAuthInfoBlob = new byte[0];
        this.mBlobLength = 0;
    }

    public int getResult() {
        return this.mResult;
    }

    public void setResult(int result) {
        this.mResult = result;
    }

    public byte[] getAuthInfoBlob() {
        byte[] bArr = this.mAuthInfoBlob;
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public void setAuthInfoBlob(byte[] authInfoBlob) {
        if (authInfoBlob != null) {
            this.mAuthInfoBlob = (byte[]) authInfoBlob.clone();
            this.mBlobLength = this.mAuthInfoBlob.length;
            return;
        }
        this.mAuthInfoBlob = new byte[0];
        this.mBlobLength = 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResult);
        dest.writeInt(this.mBlobLength);
        dest.writeByteArray(this.mAuthInfoBlob);
    }
}
