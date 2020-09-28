package com.android.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;

public class CallInfo implements Parcelable {
    public static final Parcelable.Creator<CallInfo> CREATOR = new Parcelable.Creator<CallInfo>() {
        /* class com.android.internal.telephony.CallInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CallInfo createFromParcel(Parcel source) {
            return new CallInfo(source.readString());
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(this.handle);
    }
}
