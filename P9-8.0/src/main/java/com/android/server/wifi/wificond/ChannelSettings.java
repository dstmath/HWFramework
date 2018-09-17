package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.Objects;

public class ChannelSettings implements Parcelable {
    public static final Creator<ChannelSettings> CREATOR = new Creator<ChannelSettings>() {
        public ChannelSettings createFromParcel(Parcel in) {
            ChannelSettings result = new ChannelSettings();
            result.frequency = in.readInt();
            if (in.dataAvail() != 0) {
                Log.e(ChannelSettings.TAG, "Found trailing data after parcel parsing.");
            }
            return result;
        }

        public ChannelSettings[] newArray(int size) {
            return new ChannelSettings[size];
        }
    };
    private static final String TAG = "ChannelSettings";
    public int frequency;

    public boolean equals(Object rhs) {
        boolean z = true;
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof ChannelSettings)) {
            return false;
        }
        ChannelSettings channel = (ChannelSettings) rhs;
        if (channel == null) {
            return false;
        }
        if (this.frequency != channel.frequency) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.frequency)});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.frequency);
    }
}
