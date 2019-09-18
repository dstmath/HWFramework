package com.unionpay.tsmservice.data;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdateInfo implements Parcelable {
    public static final Parcelable.Creator<UpdateInfo> CREATOR = new Parcelable.Creator<UpdateInfo>() {
        public final UpdateInfo createFromParcel(Parcel parcel) {
            return new UpdateInfo(parcel);
        }

        public final UpdateInfo[] newArray(int i) {
            return new UpdateInfo[i];
        }
    };
    public static final String TYPE_MUST = "02";
    public static final String TYPE_NONE = "00";
    public static final String TYPE_OPTION = "01";
    private String mClientDigest;
    private String[] mDesc;
    private String mDownloadUrl;
    private String mType;

    public UpdateInfo() {
    }

    public UpdateInfo(Parcel parcel) {
        this.mType = parcel.readString();
        this.mDownloadUrl = parcel.readString();
        this.mClientDigest = parcel.readString();
        this.mDesc = parcel.createStringArray();
    }

    public int describeContents() {
        return 0;
    }

    public String getClientDigest() {
        return this.mClientDigest;
    }

    public String[] getDesc() {
        return this.mDesc;
    }

    public String getDownloadUrl() {
        return this.mDownloadUrl;
    }

    public String getType() {
        return this.mType;
    }

    public void setClientDigest(String str) {
        this.mClientDigest = str;
    }

    public void setDesc(String[] strArr) {
        this.mDesc = strArr;
    }

    public void setDownloadUrl(String str) {
        this.mDownloadUrl = str;
    }

    public void setType(String str) {
        this.mType = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mType);
        parcel.writeString(this.mDownloadUrl);
        parcel.writeString(this.mClientDigest);
        parcel.writeStringArray(this.mDesc);
    }
}
