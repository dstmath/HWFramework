package android.app.admin;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ConnectEvent extends NetworkEvent implements Parcelable {
    public static final Creator<ConnectEvent> CREATOR = new Creator<ConnectEvent>() {
        public ConnectEvent createFromParcel(Parcel in) {
            if (in.readInt() != 2) {
                return null;
            }
            return new ConnectEvent(in, null);
        }

        public ConnectEvent[] newArray(int size) {
            return new ConnectEvent[size];
        }
    };
    private final String ipAddress;
    private final int port;

    /* synthetic */ ConnectEvent(Parcel in, ConnectEvent -this1) {
        this(in);
    }

    public ConnectEvent(String ipAddress, int port, String packageName, long timestamp) {
        super(packageName, timestamp);
        this.ipAddress = ipAddress;
        this.port = port;
    }

    private ConnectEvent(Parcel in) {
        this.ipAddress = in.readString();
        this.port = in.readInt();
        this.packageName = in.readString();
        this.timestamp = in.readLong();
    }

    public InetAddress getInetAddress() {
        try {
            return InetAddress.getByName(this.ipAddress);
        } catch (UnknownHostException e) {
            return InetAddress.getLoopbackAddress();
        }
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        return String.format("ConnectEvent(%s, %d, %d, %s)", new Object[]{this.ipAddress, Integer.valueOf(this.port), Long.valueOf(this.timestamp), this.packageName});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(2);
        out.writeString(this.ipAddress);
        out.writeInt(this.port);
        out.writeString(this.packageName);
        out.writeLong(this.timestamp);
    }
}
