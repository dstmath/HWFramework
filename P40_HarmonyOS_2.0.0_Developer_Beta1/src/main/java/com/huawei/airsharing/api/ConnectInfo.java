package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectInfo implements Parcelable {
    public static final Parcelable.Creator<ConnectInfo> CREATOR = new Parcelable.Creator<ConnectInfo>() {
        /* class com.huawei.airsharing.api.ConnectInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConnectInfo[] newArray(int size) {
            return new ConnectInfo[size];
        }

        @Override // android.os.Parcelable.Creator
        public ConnectInfo createFromParcel(Parcel source) {
            Object objectInfo = source.readValue(ProjectionDevice.class.getClassLoader());
            if (objectInfo instanceof ProjectionDevice) {
                return new ConnectInfo((ProjectionDevice) objectInfo, EProjectionMode.valueOf(source.readString()));
            }
            return null;
        }
    };
    private ProjectionDevice mProjectionDevice = null;
    private EProjectionMode mProjectionMode = EProjectionMode.MIRROR;

    public ConnectInfo(ProjectionDevice projectionDevice) {
        if (projectionDevice != null) {
            this.mProjectionDevice = projectionDevice;
            return;
        }
        throw new IllegalArgumentException("projectionDevice can't be null");
    }

    public ConnectInfo(ProjectionDevice projectionDevice, EProjectionMode projectionMode) {
        if (projectionDevice != null) {
            this.mProjectionDevice = projectionDevice;
            this.mProjectionMode = projectionMode;
            return;
        }
        throw new IllegalArgumentException("projectionDevice can't be null");
    }

    public ProjectionDevice getProjectionDevice() {
        return this.mProjectionDevice;
    }

    public EProjectionMode getProjectionMode() {
        return this.mProjectionMode;
    }

    public void setProjectionMode(EProjectionMode projectionMode) {
        this.mProjectionMode = projectionMode;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.mProjectionDevice);
        dest.writeString(this.mProjectionMode.toString());
    }

    @Override // java.lang.Object
    public String toString() {
        return "{" + this.mProjectionDevice.toString() + "}, {mProjectionMode: " + this.mProjectionMode.toString() + "}";
    }

    private ConnectInfo() {
    }
}
