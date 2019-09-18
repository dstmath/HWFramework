package com.android.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;

public class CallInfo implements Parcelable {
    public static final Parcelable.Creator<CallInfo> CREATOR = new Parcelable.Creator<CallInfo>() {
        public CallInfo createFromParcel(Parcel source) {
            return new CallInfo(source.readString());
        }

        public CallInfo[] newArray(int size) {
            return new CallInfo[size];
        }
    };
    private String handle;

    public CallInfo(String handle2) {
        this.handle = handle2;
    }

    public String getHandle() {
        return this.handle;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(this.handle);
    }
}
