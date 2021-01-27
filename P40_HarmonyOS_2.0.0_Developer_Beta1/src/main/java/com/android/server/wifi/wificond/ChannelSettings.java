package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.Objects;

public class ChannelSettings implements Parcelable {
    public static final Parcelable.Creator<ChannelSettings> CREATOR = new Parcelable.Creator<ChannelSettings>() {
        /* class com.android.server.wifi.wificond.ChannelSettings.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ChannelSettings createFromParcel(Parcel in) {
            ChannelSettings result = new ChannelSettings();
            result.frequency = in.readInt();
            if (in.dataAvail() != 0) {
                Log.e(ChannelSettings.TAG, "Found trailing data after parcel parsing.");
            }
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public ChannelSettings[] newArray(int size) {
            return new ChannelSettings[size];
        }
    };
    private static final String TAG = "ChannelSettings";
    public int frequency;

    @Override // java.lang.Object
    public boolean equals(Object rhs) {
        ChannelSettings channel;
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof ChannelSettings) || (channel = (ChannelSettings) rhs) == null) {
            return false;
        }
        if (this.frequency == channel.frequency) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.frequency));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.frequency);
    }
}
