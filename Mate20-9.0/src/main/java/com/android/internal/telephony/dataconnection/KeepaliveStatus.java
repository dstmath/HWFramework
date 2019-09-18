package com.android.internal.telephony.dataconnection;

import android.os.Parcel;
import android.os.Parcelable;

public class KeepaliveStatus implements Parcelable {
    public static final Parcelable.Creator<KeepaliveStatus> CREATOR = new Parcelable.Creator<KeepaliveStatus>() {
        public KeepaliveStatus createFromParcel(Parcel source) {
            return new KeepaliveStatus(source);
        }

        public KeepaliveStatus[] newArray(int size) {
            return new KeepaliveStatus[size];
        }
    };
    public static final int ERROR_NONE = 0;
    public static final int ERROR_NO_RESOURCES = 2;
    public static final int ERROR_UNKNOWN = 3;
    public static final int ERROR_UNSUPPORTED = 1;
    public static final int INVALID_HANDLE = Integer.MAX_VALUE;
    private static final String LOG_TAG = "KeepaliveStatus";
    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_INACTIVE = 1;
    public static final int STATUS_PENDING = 2;
    public final int errorCode;
    public final int sessionHandle;
    public final int statusCode;

    public KeepaliveStatus(int error) {
        this.sessionHandle = INVALID_HANDLE;
        this.statusCode = 1;
        this.errorCode = error;
    }

    public KeepaliveStatus(int handle, int code) {
        this.sessionHandle = handle;
        this.statusCode = code;
        this.errorCode = 0;
    }

    public String toString() {
        return String.format("{errorCode=%d, sessionHandle=%d, statusCode=%d}", new Object[]{Integer.valueOf(this.errorCode), Integer.valueOf(this.sessionHandle), Integer.valueOf(this.statusCode)});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.errorCode);
        dest.writeInt(this.sessionHandle);
        dest.writeInt(this.statusCode);
    }

    private KeepaliveStatus(Parcel p) {
        this.errorCode = p.readInt();
        this.sessionHandle = p.readInt();
        this.statusCode = p.readInt();
    }
}
