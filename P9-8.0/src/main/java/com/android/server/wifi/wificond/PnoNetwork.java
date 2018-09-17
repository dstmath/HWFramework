package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;
import java.util.Objects;

public class PnoNetwork implements Parcelable {
    public static final Creator<PnoNetwork> CREATOR = new Creator<PnoNetwork>() {
        public PnoNetwork createFromParcel(Parcel in) {
            boolean z = false;
            PnoNetwork result = new PnoNetwork();
            if (in.readInt() != 0) {
                z = true;
            }
            result.isHidden = z;
            result.ssid = in.createByteArray();
            return result;
        }

        public PnoNetwork[] newArray(int size) {
            return new PnoNetwork[size];
        }
    };
    public boolean isHidden;
    public byte[] ssid;

    public boolean equals(Object rhs) {
        boolean z = true;
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof PnoNetwork)) {
            return false;
        }
        PnoNetwork network = (PnoNetwork) rhs;
        if (!Arrays.equals(this.ssid, network.ssid)) {
            z = false;
        } else if (this.isHidden != network.isHidden) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Boolean.valueOf(this.isHidden), this.ssid});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.isHidden ? 1 : 0);
        out.writeByteArray(this.ssid);
    }
}
