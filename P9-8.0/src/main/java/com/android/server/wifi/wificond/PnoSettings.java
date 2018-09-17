package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.Objects;

public class PnoSettings implements Parcelable {
    public static final Creator<PnoSettings> CREATOR = new Creator<PnoSettings>() {
        public PnoSettings createFromParcel(Parcel in) {
            PnoSettings result = new PnoSettings();
            result.intervalMs = in.readInt();
            result.min2gRssi = in.readInt();
            result.min5gRssi = in.readInt();
            result.pnoNetworks = new ArrayList();
            in.readTypedList(result.pnoNetworks, PnoNetwork.CREATOR);
            return result;
        }

        public PnoSettings[] newArray(int size) {
            return new PnoSettings[size];
        }
    };
    public int intervalMs;
    public int min2gRssi;
    public int min5gRssi;
    public ArrayList<PnoNetwork> pnoNetworks;

    public boolean equals(Object rhs) {
        boolean z = false;
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof PnoSettings)) {
            return false;
        }
        PnoSettings settings = (PnoSettings) rhs;
        if (settings == null) {
            return false;
        }
        if (this.intervalMs == settings.intervalMs && this.min2gRssi == settings.min2gRssi && this.min5gRssi == settings.min5gRssi) {
            z = this.pnoNetworks.equals(settings.pnoNetworks);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.intervalMs), Integer.valueOf(this.min2gRssi), Integer.valueOf(this.min5gRssi), this.pnoNetworks});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.intervalMs);
        out.writeInt(this.min2gRssi);
        out.writeInt(this.min5gRssi);
        out.writeTypedList(this.pnoNetworks);
    }
}
