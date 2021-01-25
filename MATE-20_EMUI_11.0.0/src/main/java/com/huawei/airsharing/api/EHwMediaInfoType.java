package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public enum EHwMediaInfoType implements Parcelable {
    IMAGE,
    AUDIO,
    VIDEO,
    FOLDER,
    IMAGE_VIDEO,
    IDLE,
    CUSTOM,
    UNKNOWN;
    
    public static final Parcelable.Creator<EHwMediaInfoType> CREATOR = new Parcelable.Creator<EHwMediaInfoType>() {
        /* class com.huawei.airsharing.api.EHwMediaInfoType.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EHwMediaInfoType createFromParcel(Parcel source) {
            return EHwMediaInfoType.valueOf(source.readString());
        }

        @Override // android.os.Parcelable.Creator
        public EHwMediaInfoType[] newArray(int size) {
            return new EHwMediaInfoType[size];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }
}
