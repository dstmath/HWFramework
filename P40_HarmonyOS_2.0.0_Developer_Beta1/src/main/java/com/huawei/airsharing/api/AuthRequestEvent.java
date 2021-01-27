package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public class AuthRequestEvent extends Event implements Parcelable {
    public static final int AUTH_TYPE_PIN = 2;
    public static final int AUTH_TYPE_PWD = 1;
    public static final Parcelable.Creator<AuthRequestEvent> CREATOR = new Parcelable.Creator<AuthRequestEvent>() {
        /* class com.huawei.airsharing.api.AuthRequestEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AuthRequestEvent createFromParcel(Parcel in) {
            return new AuthRequestEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public AuthRequestEvent[] newArray(int size) {
            return new AuthRequestEvent[size];
        }
    };
    private int mAuthType;
    private ProjectionDevice mDevice;

    public AuthRequestEvent(int eventId, ProjectionDevice device, int type) {
        super(eventId);
        this.mDevice = device;
        this.mAuthType = type;
    }

    protected AuthRequestEvent(Parcel in) {
        super(in);
        this.mDevice = (ProjectionDevice) in.readParcelable(ProjectionDevice.class.getClassLoader());
        this.mAuthType = in.readInt();
    }

    protected AuthRequestEvent(int eventId, Parcel in) {
        super(eventId);
        this.mDevice = (ProjectionDevice) in.readParcelable(ProjectionDevice.class.getClassLoader());
        this.mAuthType = in.readInt();
    }

    @Override // com.huawei.airsharing.api.Event, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.mDevice, flags);
        dest.writeInt(this.mAuthType);
    }

    @Override // com.huawei.airsharing.api.Event, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public ProjectionDevice getDevice() {
        return this.mDevice;
    }

    public int getAuthType() {
        return this.mAuthType;
    }
}
