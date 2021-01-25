package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public class HiddenNetwork implements Parcelable {
    public static final Parcelable.Creator<HiddenNetwork> CREATOR = new Parcelable.Creator<HiddenNetwork>() {
        /* class com.android.server.wifi.wificond.HiddenNetwork.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HiddenNetwork createFromParcel(Parcel in) {
            HiddenNetwork result = new HiddenNetwork();
            result.ssid = in.createByteArray();
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public HiddenNetwork[] newArray(int size) {
            return new HiddenNetwork[size];
        }
    };
    private static final String TAG = "HiddenNetwork";
    public byte[] ssid;

    @Override // java.lang.Object
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof HiddenNetwork)) {
            return false;
        }
        return Arrays.equals(this.ssid, ((HiddenNetwork) rhs).ssid);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Arrays.hashCode(this.ssid);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.ssid);
    }
}
