package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public enum EProjectionMode implements Parcelable {
    MIRROR("MIRROR"),
    MEDIA_RESOURCE("MEDIA_RESOURCE");
    
    public static final Parcelable.Creator<EProjectionMode> CREATOR = new Parcelable.Creator<EProjectionMode>() {
        /* class com.huawei.airsharing.api.EProjectionMode.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EProjectionMode createFromParcel(Parcel source) {
            return EProjectionMode.valueOf(source.readString());
        }

        @Override // android.os.Parcelable.Creator
        public EProjectionMode[] newArray(int size) {
            return new EProjectionMode[size];
        }
    };
    private String projectionMode;

    private EProjectionMode(String projectionMode2) {
        this.projectionMode = projectionMode2;
    }

    public String getProjectionMode() {
        return this.projectionMode;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }
}
