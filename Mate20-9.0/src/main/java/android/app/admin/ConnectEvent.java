package android.app.admin;

import android.os.Parcel;
import android.os.Parcelable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ConnectEvent extends NetworkEvent implements Parcelable {
    public static final Parcelable.Creator<ConnectEvent> CREATOR = new Parcelable.Creator<ConnectEvent>() {
        public ConnectEvent createFromParcel(Parcel in) {
            if (in.readInt() != 2) {
                return null;
            }
            return new ConnectEvent(in);
        }

        public ConnectEvent[] newArray(int size) {
            return new ConnectEvent[size];
        }
    };
    private final String mIpAddress;
    private final int mPort;

    public ConnectEvent(String ipAddress, int port, String packageName, long timestamp) {
        super(packageName, timestamp);
        this.mIpAddress = ipAddress;
        this.mPort = port;
    }

    private ConnectEvent(Parcel in) {
        this.mIpAddress = in.readString();
        this.mPort = in.readInt();
        this.mPackageName = in.readString();
        this.mTimestamp = in.readLong();
        this.mId = in.readLong();
    }

    public InetAddress getInetAddress() {
        try {
            return InetAddress.getByName(this.mIpAddress);
        } catch (UnknownHostException e) {
            return InetAddress.getLoopbackAddress();
        }
    }

    public int getPort() {
        return this.mPort;
    }

    public String toString() {
        return String.format("ConnectEvent(%d, %s, %d, %d, %s)", new Object[]{Long.valueOf(this.mId), this.mIpAddress, Integer.valueOf(this.mPort), Long.valueOf(this.mTimestamp), this.mPackageName});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(2);
        out.writeString(this.mIpAddress);
        out.writeInt(this.mPort);
        out.writeString(this.mPackageName);
        out.writeLong(this.mTimestamp);
        out.writeLong(this.mId);
    }
}
