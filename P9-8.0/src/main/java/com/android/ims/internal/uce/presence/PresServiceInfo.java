package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LogException;

public class PresServiceInfo implements Parcelable {
    public static final Creator<PresServiceInfo> CREATOR = new Creator<PresServiceInfo>() {
        public PresServiceInfo createFromParcel(Parcel source) {
            return new PresServiceInfo(source, null);
        }

        public PresServiceInfo[] newArray(int size) {
            return new PresServiceInfo[size];
        }
    };
    public static final int UCE_PRES_MEDIA_CAP_FULL_AUDIO_AND_VIDEO = 2;
    public static final int UCE_PRES_MEDIA_CAP_FULL_AUDIO_ONLY = 1;
    public static final int UCE_PRES_MEDIA_CAP_NONE = 0;
    public static final int UCE_PRES_MEDIA_CAP_UNKNOWN = 3;
    private int mMediaCap;
    private String mServiceDesc;
    private String mServiceID;
    private String mServiceVer;

    /* synthetic */ PresServiceInfo(Parcel source, PresServiceInfo -this1) {
        this(source);
    }

    public int getMediaType() {
        return this.mMediaCap;
    }

    public void setMediaType(int nMediaCap) {
        this.mMediaCap = nMediaCap;
    }

    public String getServiceId() {
        return this.mServiceID;
    }

    public void setServiceId(String serviceID) {
        this.mServiceID = serviceID;
    }

    public String getServiceDesc() {
        return this.mServiceDesc;
    }

    public void setServiceDesc(String serviceDesc) {
        this.mServiceDesc = serviceDesc;
    }

    public String getServiceVer() {
        return this.mServiceVer;
    }

    public void setServiceVer(String serviceVer) {
        this.mServiceVer = serviceVer;
    }

    public PresServiceInfo() {
        this.mMediaCap = 0;
        this.mServiceID = LogException.NO_VALUE;
        this.mServiceDesc = LogException.NO_VALUE;
        this.mServiceVer = LogException.NO_VALUE;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceID);
        dest.writeString(this.mServiceDesc);
        dest.writeString(this.mServiceVer);
        dest.writeInt(this.mMediaCap);
    }

    private PresServiceInfo(Parcel source) {
        this.mMediaCap = 0;
        this.mServiceID = LogException.NO_VALUE;
        this.mServiceDesc = LogException.NO_VALUE;
        this.mServiceVer = LogException.NO_VALUE;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mServiceID = source.readString();
        this.mServiceDesc = source.readString();
        this.mServiceVer = source.readString();
        this.mMediaCap = source.readInt();
    }
}
