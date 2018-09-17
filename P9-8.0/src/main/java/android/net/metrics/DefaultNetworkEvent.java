package android.net.metrics;

import android.net.NetworkCapabilities;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class DefaultNetworkEvent implements Parcelable {
    public static final Creator<DefaultNetworkEvent> CREATOR = new Creator<DefaultNetworkEvent>() {
        public DefaultNetworkEvent createFromParcel(Parcel in) {
            return new DefaultNetworkEvent(in, null);
        }

        public DefaultNetworkEvent[] newArray(int size) {
            return new DefaultNetworkEvent[size];
        }
    };
    public final int netId;
    public final boolean prevIPv4;
    public final boolean prevIPv6;
    public final int prevNetId;
    public final int[] transportTypes;

    /* synthetic */ DefaultNetworkEvent(Parcel in, DefaultNetworkEvent -this1) {
        this(in);
    }

    public DefaultNetworkEvent(int netId, int[] transportTypes, int prevNetId, boolean prevIPv4, boolean prevIPv6) {
        this.netId = netId;
        this.transportTypes = transportTypes;
        this.prevNetId = prevNetId;
        this.prevIPv4 = prevIPv4;
        this.prevIPv6 = prevIPv6;
    }

    private DefaultNetworkEvent(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.netId = in.readInt();
        this.transportTypes = in.createIntArray();
        this.prevNetId = in.readInt();
        if (in.readByte() > (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.prevIPv4 = z;
        if (in.readByte() <= (byte) 0) {
            z2 = false;
        }
        this.prevIPv6 = z2;
    }

    public void writeToParcel(Parcel out, int flags) {
        byte b;
        byte b2 = (byte) 1;
        out.writeInt(this.netId);
        out.writeIntArray(this.transportTypes);
        out.writeInt(this.prevNetId);
        if (this.prevIPv4) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        out.writeByte(b);
        if (!this.prevIPv6) {
            b2 = (byte) 0;
        }
        out.writeByte(b2);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        String prevNetwork = String.valueOf(this.prevNetId);
        String newNetwork = String.valueOf(this.netId);
        if (this.prevNetId != 0) {
            prevNetwork = prevNetwork + ":" + ipSupport();
        }
        if (this.netId != 0) {
            newNetwork = newNetwork + ":" + NetworkCapabilities.transportNamesOf(this.transportTypes);
        }
        return String.format("DefaultNetworkEvent(%s -> %s)", new Object[]{prevNetwork, newNetwork});
    }

    private String ipSupport() {
        if (this.prevIPv4 && this.prevIPv6) {
            return "DUAL";
        }
        if (this.prevIPv6) {
            return "IPv6";
        }
        if (this.prevIPv4) {
            return "IPv4";
        }
        return "NONE";
    }
}
