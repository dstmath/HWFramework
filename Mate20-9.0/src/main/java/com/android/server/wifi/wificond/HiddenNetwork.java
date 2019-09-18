package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
import java.util.Objects;

public class HiddenNetwork implements Parcelable {
    public static final Parcelable.Creator<HiddenNetwork> CREATOR = new Parcelable.Creator<HiddenNetwork>() {
        public HiddenNetwork createFromParcel(Parcel in) {
            HiddenNetwork result = new HiddenNetwork();
            result.ssid = in.createByteArray();
            return result;
        }

        public HiddenNetwork[] newArray(int size) {
            return new HiddenNetwork[size];
        }
    };
    private static final String TAG = "HiddenNetwork";
    public byte[] ssid;

    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof HiddenNetwork)) {
            return false;
        }
        return Arrays.equals(this.ssid, ((HiddenNetwork) rhs).ssid);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.ssid});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.ssid);
    }
}
