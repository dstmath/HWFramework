package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LogException;
import com.android.ims.internal.uce.common.CapInfo;

public class PresCapInfo implements Parcelable {
    public static final Creator<PresCapInfo> CREATOR = new Creator<PresCapInfo>() {
        public PresCapInfo createFromParcel(Parcel source) {
            return new PresCapInfo(source, null);
        }

        public PresCapInfo[] newArray(int size) {
            return new PresCapInfo[size];
        }
    };
    private CapInfo mCapInfo;
    private String mContactUri;

    /* synthetic */ PresCapInfo(Parcel source, PresCapInfo -this1) {
        this(source);
    }

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
        this.mContactUri = LogException.NO_VALUE;
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
        this.mContactUri = LogException.NO_VALUE;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mContactUri = source.readString();
        this.mCapInfo = (CapInfo) source.readParcelable(CapInfo.class.getClassLoader());
    }
}
