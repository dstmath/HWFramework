package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;

public class PresResInfo implements Parcelable {
    public static final Parcelable.Creator<PresResInfo> CREATOR = new Parcelable.Creator<PresResInfo>() {
        public PresResInfo createFromParcel(Parcel source) {
            return new PresResInfo(source);
        }

        public PresResInfo[] newArray(int size) {
            return new PresResInfo[size];
        }
    };
    private String mDisplayName;
    private PresResInstanceInfo mInstanceInfo;
    private String mResUri;

    public PresResInstanceInfo getInstanceInfo() {
        return this.mInstanceInfo;
    }

    public void setInstanceInfo(PresResInstanceInfo instanceInfo) {
        this.mInstanceInfo = instanceInfo;
    }

    public String getResUri() {
        return this.mResUri;
    }

    public void setResUri(String resUri) {
        this.mResUri = resUri;
    }

    public String getDisplayName() {
        return this.mDisplayName;
    }

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    public PresResInfo() {
        this.mResUri = "";
        this.mDisplayName = "";
        this.mInstanceInfo = new PresResInstanceInfo();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mResUri);
        dest.writeString(this.mDisplayName);
        dest.writeParcelable(this.mInstanceInfo, flags);
    }

    private PresResInfo(Parcel source) {
        this.mResUri = "";
        this.mDisplayName = "";
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mResUri = source.readString();
        this.mDisplayName = source.readString();
        this.mInstanceInfo = (PresResInstanceInfo) source.readParcelable(PresResInstanceInfo.class.getClassLoader());
    }
}
