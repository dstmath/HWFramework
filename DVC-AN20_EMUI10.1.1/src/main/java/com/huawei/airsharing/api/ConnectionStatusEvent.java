package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectionStatusEvent extends Event implements Parcelable {
    public static final int CONNECTING_NORMAL = 0;
    public static final int CONNECTING_WAITING_CONFIRM = 1;
    public static final int CONNECT_ERROR_NONE = 0;
    public static final int CONNECT_FAIL_REMOTE_DEVICE_BUSY = 4;
    public static final int CONNECT_FAIL_RETRY_PIN_OVER_THREE = 5;
    public static final int CONNECT_FAIL_RETRY_PWD_OVER_THREE = 6;
    public static final int CONNECT_FAIL_TIMEOUT = 1;
    public static final int CONNECT_FAIL_UNKNOWN = 10;
    public static final int CONNECT_FAIL_WRONG_HICHAIN_AUTH_CODE = 3;
    public static final int CONNECT_FAIL_WRONG_USER_AUTH_CODE = 2;
    public static final Parcelable.Creator<ConnectionStatusEvent> CREATOR = new Parcelable.Creator<ConnectionStatusEvent>() {
        /* class com.huawei.airsharing.api.ConnectionStatusEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConnectionStatusEvent createFromParcel(Parcel in) {
            return new ConnectionStatusEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public ConnectionStatusEvent[] newArray(int size) {
            return new ConnectionStatusEvent[size];
        }
    };
    private int mCauseReason;
    private String mDetails;
    private ProjectionDevice mDevice;
    private EProjectionMode mProjectionMode;

    public ConnectionStatusEvent(int eventId, ProjectionDevice device) {
        this(eventId, device, 0, EProjectionMode.MIRROR);
    }

    public ConnectionStatusEvent(int eventId, ProjectionDevice device, int reason) {
        this(eventId, device, reason, EProjectionMode.MIRROR);
    }

    public ConnectionStatusEvent(int eventId, ProjectionDevice device, int reason, EProjectionMode projectionMode) {
        super(eventId);
        this.mDetails = "connect state event";
        this.mCauseReason = 0;
        this.mDevice = device;
        this.mCauseReason = reason;
        this.mProjectionMode = projectionMode;
    }

    protected ConnectionStatusEvent(Parcel in) {
        super(in);
        this.mDetails = "connect state event";
        this.mCauseReason = 0;
        this.mDetails = in.readString();
        this.mDevice = (ProjectionDevice) in.readParcelable(ProjectionDevice.class.getClassLoader());
        this.mCauseReason = in.readInt();
        this.mProjectionMode = EProjectionMode.valueOf(in.readString());
    }

    protected ConnectionStatusEvent(int eventId, Parcel in) {
        super(eventId);
        this.mDetails = "connect state event";
        this.mCauseReason = 0;
        this.mDetails = in.readString();
        this.mDevice = (ProjectionDevice) in.readParcelable(ProjectionDevice.class.getClassLoader());
        this.mCauseReason = in.readInt();
        this.mProjectionMode = EProjectionMode.valueOf(in.readString());
    }

    @Override // com.huawei.airsharing.api.Event
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mDetails);
        dest.writeParcelable(this.mDevice, flags);
        dest.writeInt(this.mCauseReason);
        dest.writeString(this.mProjectionMode.toString());
    }

    @Override // com.huawei.airsharing.api.Event
    public int describeContents() {
        return 0;
    }

    public ProjectionDevice getDevice() {
        return this.mDevice;
    }

    public int getExceptionReason() {
        return this.mCauseReason;
    }

    public EProjectionMode getProjectionMode() {
        return this.mProjectionMode;
    }
}
