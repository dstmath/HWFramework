package android.app.admin;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.security.keystore.KeyProperties;
import android.util.Log;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DnsEvent extends NetworkEvent implements Parcelable {
    public static final Parcelable.Creator<DnsEvent> CREATOR = new Parcelable.Creator<DnsEvent>() {
        /* class android.app.admin.DnsEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DnsEvent createFromParcel(Parcel in) {
            if (in.readInt() != 1) {
                return null;
            }
            return new DnsEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public DnsEvent[] newArray(int size) {
            return new DnsEvent[size];
        }
    };
    public static final String TAG = "DnsEvent";
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
        String[] strArr = this.mIpAddresses;
        if (strArr == null || strArr.length == 0) {
            return Collections.emptyList();
        }
        List<InetAddress> inetAddresses = new ArrayList<>(strArr.length);
        for (String ipAddress : this.mIpAddresses) {
            try {
                inetAddresses.add(InetAddress.getByName(ipAddress));
            } catch (UnknownHostException e) {
                Log.e(TAG, "cannot get InetAddresses.");
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
        String[] strArr = this.mIpAddresses;
        objArr[2] = strArr == null ? KeyProperties.DIGEST_NONE : String.join(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, strArr);
        objArr[3] = Integer.valueOf(this.mIpAddressesCount);
        objArr[4] = Long.valueOf(this.mTimestamp);
        objArr[5] = this.mPackageName;
        return String.format("DnsEvent(%d, %s, %s, %d, %d, %s)", objArr);
    }

    @Override // android.os.Parcelable, android.app.admin.NetworkEvent
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable, android.app.admin.NetworkEvent
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
