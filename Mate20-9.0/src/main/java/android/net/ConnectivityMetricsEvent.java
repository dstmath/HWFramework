package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.BitUtils;

public final class ConnectivityMetricsEvent implements Parcelable {
    public static final Parcelable.Creator<ConnectivityMetricsEvent> CREATOR = new Parcelable.Creator<ConnectivityMetricsEvent>() {
        public ConnectivityMetricsEvent createFromParcel(Parcel source) {
            return new ConnectivityMetricsEvent(source);
        }

        public ConnectivityMetricsEvent[] newArray(int size) {
            return new ConnectivityMetricsEvent[size];
        }
    };
    public Parcelable data;
    public String ifname;
    public int netId;
    public long timestamp;
    public long transports;

    public ConnectivityMetricsEvent() {
    }

    private ConnectivityMetricsEvent(Parcel in) {
        this.timestamp = in.readLong();
        this.transports = in.readLong();
        this.netId = in.readInt();
        this.ifname = in.readString();
        this.data = in.readParcelable(null);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timestamp);
        dest.writeLong(this.transports);
        dest.writeInt(this.netId);
        dest.writeString(this.ifname);
        dest.writeParcelable(this.data, 0);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder("ConnectivityMetricsEvent(");
        buffer.append(String.format("%tT.%tL", new Object[]{Long.valueOf(this.timestamp), Long.valueOf(this.timestamp)}));
        if (this.netId != 0) {
            buffer.append(", ");
            buffer.append("netId=");
            buffer.append(this.netId);
        }
        if (this.ifname != null) {
            buffer.append(", ");
            buffer.append(this.ifname);
        }
        for (int t : BitUtils.unpackBits(this.transports)) {
            buffer.append(", ");
            buffer.append(NetworkCapabilities.transportNameOf(t));
        }
        buffer.append("): ");
        buffer.append(this.data.toString());
        return buffer.toString();
    }
}
