package com.android.ims.internal.uce.presence;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class PresResInfo implements Parcelable {
    public static final Parcelable.Creator<PresResInfo> CREATOR = new Parcelable.Creator<PresResInfo>() {
        /* class com.android.ims.internal.uce.presence.PresResInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PresResInfo createFromParcel(Parcel source) {
            return new PresResInfo(source);
        }

        @Override // android.os.Parcelable.Creator
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

    @UnsupportedAppUsage
    public void setInstanceInfo(PresResInstanceInfo instanceInfo) {
        this.mInstanceInfo = instanceInfo;
    }

    public String getResUri() {
        return this.mResUri;
    }

    @UnsupportedAppUsage
    public void setResUri(String resUri) {
        this.mResUri = resUri;
    }

    public String getDisplayName() {
        return this.mDisplayName;
    }

    @UnsupportedAppUsage
    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    @UnsupportedAppUsage
    public PresResInfo() {
        this.mResUri = "";
        this.mDisplayName = "";
        this.mInstanceInfo = new PresResInstanceInfo();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
