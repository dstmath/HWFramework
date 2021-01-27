package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
import java.util.Objects;

public class PnoNetwork implements Parcelable {
    public static final Parcelable.Creator<PnoNetwork> CREATOR = new Parcelable.Creator<PnoNetwork>() {
        /* class com.android.server.wifi.wificond.PnoNetwork.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PnoNetwork createFromParcel(Parcel in) {
            PnoNetwork result = new PnoNetwork();
            result.isHidden = in.readInt() != 0;
            result.ssid = in.createByteArray();
            result.frequencies = in.createIntArray();
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public PnoNetwork[] newArray(int size) {
            return new PnoNetwork[size];
        }
    };
    public int[] frequencies;
    public boolean isHidden;
    public byte[] ssid;

    @Override // java.lang.Object
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof PnoNetwork)) {
            return false;
        }
        PnoNetwork network = (PnoNetwork) rhs;
        if (!Arrays.equals(this.ssid, network.ssid) || !Arrays.equals(this.frequencies, network.frequencies) || this.isHidden != network.isHidden) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.isHidden), Integer.valueOf(Arrays.hashCode(this.ssid)), Integer.valueOf(Arrays.hashCode(this.frequencies)));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.isHidden ? 1 : 0);
        out.writeByteArray(this.ssid);
        out.writeIntArray(this.frequencies);
    }
}
