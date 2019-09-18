package com.huawei.hardware.face;

import android.os.Parcel;
import android.os.Parcelable;

public final class Face implements Parcelable {
    public static final Parcelable.Creator<Face> CREATOR = new Parcelable.Creator<Face>() {
        public Face createFromParcel(Parcel in) {
            return new Face(in);
        }

        public Face[] newArray(int size) {
            return new Face[size];
        }
    };
    private long mDeviceId;

    public Face(long deviceId) {
        this.mDeviceId = deviceId;
    }

    private Face(Parcel in) {
        this.mDeviceId = in.readLong();
    }

    public long getDeviceId() {
        return this.mDeviceId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mDeviceId);
    }
}
