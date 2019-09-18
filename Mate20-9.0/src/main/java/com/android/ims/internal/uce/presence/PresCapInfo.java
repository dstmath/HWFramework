package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.ims.internal.uce.common.CapInfo;

public class PresCapInfo implements Parcelable {
    public static final Parcelable.Creator<PresCapInfo> CREATOR = new Parcelable.Creator<PresCapInfo>() {
        public PresCapInfo createFromParcel(Parcel source) {
            return new PresCapInfo(source);
        }

        public PresCapInfo[] newArray(int size) {
            return new PresCapInfo[size];
        }
    };
    private CapInfo mCapInfo;
    private String mContactUri;

    public CapInfo getCapInfo() {
        return this.mCapInfo;
    }

    public void setCapInfo(CapInfo capInfo) {
        this.mCapInfo = capInfo;
    }

    public String getContactUri() {
        return this.mContactUri;
    }

    public void setContactUri(String contactUri) {
        this.mContactUri = contactUri;
    }

    public PresCapInfo() {
        this.mContactUri = "";
        this.mCapInfo = new CapInfo();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mContactUri);
        dest.writeParcelable(this.mCapInfo, flags);
    }

    private PresCapInfo(Parcel source) {
        this.mContactUri = "";
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mContactUri = source.readString();
        this.mCapInfo = (CapInfo) source.readParcelable(CapInfo.class.getClassLoader());
    }
}
