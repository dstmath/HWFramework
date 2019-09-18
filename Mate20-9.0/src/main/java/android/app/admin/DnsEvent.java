package android.app.admin;

import android.os.Parcel;
import android.os.Parcelable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DnsEvent extends NetworkEvent implements Parcelable {
    public static final Parcelable.Creator<DnsEvent> CREATOR = new Parcelable.Creator<DnsEvent>() {
        public DnsEvent createFromParcel(Parcel in) {
            if (in.readInt() != 1) {
                return null;
            }
            return new DnsEvent(in);
        }

        public DnsEvent[] newArray(int size) {
            return new DnsEvent[size];
        }
    };
    private final String mHostname;
    private final String[] mIpAddresses;
    private final int mIpAddressesCount;

    public DnsEvent(String hostname, String[] ipAddresses, int ipAddressesCount, String packageName, long timestamp) {
        super(packageName, timestamp);
        this.mHostname = hostname;
        this.mIpAddresses = ipAddresses;
        this.mIpAddressesCount = ipAddressesCount;
    }

    private DnsEvent(Parcel in) {
        this.mHostname = in.readString();
        this.mIpAddresses = in.createStringArray();
        this.mIpAddressesCount = in.readInt();
        this.mPackageName = in.readString();
        this.mTimestamp = in.readLong();
        this.mId = in.readLong();
    }

    public String getHostname() {
        return this.mHostname;
    }

    public List<InetAddress> getInetAddresses() {
        if (this.mIpAddresses == null || this.mIpAddresses.length == 0) {
            return Collections.emptyList();
        }
        List<InetAddress> inetAddresses = new ArrayList<>(this.mIpAddresses.length);
        for (String ipAddress : this.mIpAddresses) {
            try {
                inetAddresses.add(InetAddress.getByName(ipAddress));
            } catch (UnknownHostException e) {
            }
        }
        return inetAddresses;
    }

    public int getTotalResolvedAddressCount() {
        return this.mIpAddressesCount;
    }

    public String toString() {
        Object[] objArr = new Object[6];
        objArr[0] = Long.valueOf(this.mId);
        objArr[1] = this.mHostname;
        objArr[2] = this.mIpAddresses == null ? "NONE" : String.join(" ", this.mIpAddresses);
        objArr[3] = Integer.valueOf(this.mIpAddressesCount);
        objArr[4] = Long.valueOf(this.mTimestamp);
        objArr[5] = this.mPackageName;
        return String.format("DnsEvent(%d, %s, %s, %d, %d, %s)", objArr);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(1);
        out.writeString(this.mHostname);
        out.writeStringArray(this.mIpAddresses);
        out.writeInt(this.mIpAddressesCount);
        out.writeString(this.mPackageName);
        out.writeLong(this.mTimestamp);
        out.writeLong(this.mId);
    }
}
