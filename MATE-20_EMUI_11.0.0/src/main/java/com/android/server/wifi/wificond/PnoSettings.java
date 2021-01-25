package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Objects;

public class PnoSettings implements Parcelable {
    public static final Parcelable.Creator<PnoSettings> CREATOR = new Parcelable.Creator<PnoSettings>() {
        /* class com.android.server.wifi.wificond.PnoSettings.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PnoSettings createFromParcel(Parcel in) {
            PnoSettings result = new PnoSettings();
            result.intervalMs = in.readInt();
            result.min2gRssi = in.readInt();
            result.min5gRssi = in.readInt();
            result.pnoNetworks = new ArrayList<>();
            in.readTypedList(result.pnoNetworks, PnoNetwork.CREATOR);
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public PnoSettings[] newArray(int size) {
            return new PnoSettings[size];
        }
    };
    public int intervalMs;
    public int min2gRssi;
    public int min5gRssi;
    public ArrayList<PnoNetwork> pnoNetworks;

    @Override // java.lang.Object
    public boolean equals(Object rhs) {
        PnoSettings settings;
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof PnoSettings) || (settings = (PnoSettings) rhs) == null) {
            return false;
        }
        if (this.intervalMs == settings.intervalMs && this.min2gRssi == settings.min2gRssi && this.min5gRssi == settings.min5gRssi && this.pnoNetworks.equals(settings.pnoNetworks)) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.intervalMs), Integer.valueOf(this.min2gRssi), Integer.valueOf(this.min5gRssi), this.pnoNetworks);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.intervalMs);
        out.writeInt(this.min2gRssi);
        out.writeInt(this.min5gRssi);
        out.writeTypedList(this.pnoNetworks);
    }
}
