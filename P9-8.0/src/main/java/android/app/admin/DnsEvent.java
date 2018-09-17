package android.app.admin;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DnsEvent extends NetworkEvent implements Parcelable {
    public static final Creator<DnsEvent> CREATOR = new Creator<DnsEvent>() {
        public DnsEvent createFromParcel(Parcel in) {
            if (in.readInt() != 1) {
                return null;
            }
            return new DnsEvent(in, null);
        }

        public DnsEvent[] newArray(int size) {
            return new DnsEvent[size];
        }
    };
    private final String hostname;
    private final String[] ipAddresses;
    private final int ipAddressesCount;

    /* synthetic */ DnsEvent(Parcel in, DnsEvent -this1) {
        this(in);
    }

    public DnsEvent(String hostname, String[] ipAddresses, int ipAddressesCount, String packageName, long timestamp) {
        super(packageName, timestamp);
        this.hostname = hostname;
        this.ipAddresses = ipAddresses;
        this.ipAddressesCount = ipAddressesCount;
    }

    private DnsEvent(Parcel in) {
        this.hostname = in.readString();
        this.ipAddresses = in.createStringArray();
        this.ipAddressesCount = in.readInt();
        this.packageName = in.readString();
        this.timestamp = in.readLong();
    }

    public String getHostname() {
        return this.hostname;
    }

    public List<InetAddress> getInetAddresses() {
        if (this.ipAddresses == null || this.ipAddresses.length == 0) {
            return Collections.emptyList();
        }
        List<InetAddress> inetAddresses = new ArrayList(this.ipAddresses.length);
        for (String ipAddress : this.ipAddresses) {
            try {
                inetAddresses.add(InetAddress.getByName(ipAddress));
            } catch (UnknownHostException e) {
            }
        }
        return inetAddresses;
    }

    public int getTotalResolvedAddressCount() {
        return this.ipAddressesCount;
    }

    public String toString() {
        String str = "DnsEvent(%s, %s, %d, %d, %s)";
        Object[] objArr = new Object[5];
        objArr[0] = this.hostname;
        objArr[1] = this.ipAddresses == null ? "NONE" : String.join(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, this.ipAddresses);
        objArr[2] = Integer.valueOf(this.ipAddressesCount);
        objArr[3] = Long.valueOf(this.timestamp);
        objArr[4] = this.packageName;
        return String.format(str, objArr);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(1);
        out.writeString(this.hostname);
        out.writeStringArray(this.ipAddresses);
        out.writeInt(this.ipAddressesCount);
        out.writeString(this.packageName);
        out.writeLong(this.timestamp);
    }
}
