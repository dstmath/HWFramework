package com.huawei.nb.client.ai;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdatePackageInfo implements Parcelable {
    public static final Parcelable.Creator<UpdatePackageInfo> CREATOR = new Parcelable.Creator<UpdatePackageInfo>() {
        /* class com.huawei.nb.client.ai.UpdatePackageInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UpdatePackageInfo createFromParcel(Parcel parcel) {
            UpdatePackageInfo updatePackageInfo = new UpdatePackageInfo();
            updatePackageInfo.resid = parcel.readString();
            updatePackageInfo.errorCode = parcel.readInt();
            updatePackageInfo.errorMessage = parcel.readString();
            updatePackageInfo.isUpdateAvailable = parcel.readByte() != 0;
            updatePackageInfo.newVersionCode = parcel.readLong();
            updatePackageInfo.newPackageSize = parcel.readLong();
            return updatePackageInfo;
        }

        @Override // android.os.Parcelable.Creator
        public UpdatePackageInfo[] newArray(int i) {
            return new UpdatePackageInfo[i];
        }
    };
    private int errorCode;
    private String errorMessage;
    private boolean isUpdateAvailable;
    private long newPackageSize;
    private long newVersionCode;
    private String resid;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.resid);
        parcel.writeInt(this.errorCode);
        parcel.writeString(this.errorMessage);
        parcel.writeByte(this.isUpdateAvailable ? (byte) 1 : 0);
        parcel.writeLong(this.newVersionCode);
        parcel.writeLong(this.newPackageSize);
    }

    public String getResid() {
        return this.resid;
    }

    public void setResid(String str) {
        this.resid = str;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int i) {
        this.errorCode = i;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String str) {
        this.errorMessage = str;
    }

    public boolean isUpdateAvailable() {
        return this.isUpdateAvailable;
    }

    public void setUpdateAvailable(boolean z) {
        this.isUpdateAvailable = z;
    }

    public long getNewVersionCode() {
        return this.newVersionCode;
    }

    public void setNewVersionCode(long j) {
        this.newVersionCode = j;
    }

    public long getNewPackageSize() {
        return this.newPackageSize;
    }

    public void setNewPackageSize(long j) {
        this.newPackageSize = j;
    }
}
