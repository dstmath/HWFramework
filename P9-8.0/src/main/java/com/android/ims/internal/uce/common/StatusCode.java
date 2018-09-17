package com.android.ims.internal.uce.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class StatusCode implements Parcelable {
    public static final Creator<StatusCode> CREATOR = new Creator<StatusCode>() {
        public StatusCode createFromParcel(Parcel source) {
            return new StatusCode(source, null);
        }

        public StatusCode[] newArray(int size) {
            return new StatusCode[size];
        }
    };
    public static final int UCE_FAILURE = 1;
    public static final int UCE_FETCH_ERROR = 6;
    public static final int UCE_INSUFFICIENT_MEMORY = 8;
    public static final int UCE_INVALID_LISTENER_HANDLE = 4;
    public static final int UCE_INVALID_PARAM = 5;
    public static final int UCE_INVALID_SERVICE_HANDLE = 3;
    public static final int UCE_LOST_NET = 9;
    public static final int UCE_NOT_FOUND = 11;
    public static final int UCE_NOT_SUPPORTED = 10;
    public static final int UCE_NO_CHANGE_IN_CAP = 13;
    public static final int UCE_REQUEST_TIMEOUT = 7;
    public static final int UCE_SERVICE_UNAVAILABLE = 12;
    public static final int UCE_SERVICE_UNKNOWN = 14;
    public static final int UCE_SUCCESS = 0;
    public static final int UCE_SUCCESS_ASYC_UPDATE = 2;
    private int mStatusCode;

    /* synthetic */ StatusCode(Parcel source, StatusCode -this1) {
        this(source);
    }

    public StatusCode() {
        this.mStatusCode = 0;
    }

    public int getStatusCode() {
        return this.mStatusCode;
    }

    public void setStatusCode(int nStatusCode) {
        this.mStatusCode = nStatusCode;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStatusCode);
    }

    private StatusCode(Parcel source) {
        this.mStatusCode = 0;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mStatusCode = source.readInt();
    }
}
