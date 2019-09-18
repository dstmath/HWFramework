package com.huawei.nb.client.ai;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdatePackageInfo implements Parcelable {
    public static final Parcelable.Creator<UpdatePackageInfo> CREATOR = new Parcelable.Creator<UpdatePackageInfo>() {
        public UpdatePackageInfo createFromParcel(Parcel in) {
            UpdatePackageInfo info = new UpdatePackageInfo();
            String unused = info.resid = in.readString();
            int unused2 = info.errorCode = in.readInt();
            String unused3 = info.errorMessage = in.readString();
            boolean unused4 = info.isUpdateAvailable = in.readByte() != 0;
            long unused5 = info.newVersionCode = in.readLong();
            long unused6 = info.newPackageSize = in.readLong();
            return info;
        }

        public UpdatePackageInfo[] newArray(int size) {
            return new UpdatePackageInfo[size];
        }
    };
    /* access modifiers changed from: private */
    public int errorCode;
    /* access modifiers changed from: private */
    public String errorMessage;
    /* access modifiers changed from: private */
    public boolean isUpdateAvailable;
    /* access modifiers changed from: private */
    public long newPackageSize;
    /* access modifiers changed from: private */
    public long newVersionCode;
    /* access modifiers changed from: private */
    public String resid;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(this.resid);
        dest.writeInt(this.errorCode);
        dest.writeString(this.errorMessage);
        dest.writeByte((byte) (this.isUpdateAvailable ? 1 : 0));
        dest.writeLong(this.newVersionCode);
        dest.writeLong(this.newPackageSize);
    }

    public String getResid() {
        return this.resid;
    }

    public void setResid(String resid2) {
        this.resid = resid2;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode2) {
        this.errorCode = errorCode2;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage2) {
        this.errorMessage = errorMessage2;
    }

    public boolean isUpdateAvailable() {
        return this.isUpdateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.isUpdateAvailable = updateAvailable;
    }

    public long getNewVersionCode() {
        return this.newVersionCode;
    }

    public void setNewVersionCode(long newVersionCode2) {
        this.newVersionCode = newVersionCode2;
    }

    public long getNewPackageSize() {
        return this.newPackageSize;
    }

    public void setNewPackageSize(long newPackageSize2) {
        this.newPackageSize = newPackageSize2;
    }
}
