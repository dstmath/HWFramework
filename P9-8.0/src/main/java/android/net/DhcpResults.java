package android.net;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import java.net.Inet4Address;
import java.util.Objects;

public class DhcpResults extends StaticIpConfiguration {
    public static final Creator<DhcpResults> CREATOR = new Creator<DhcpResults>() {
        public DhcpResults createFromParcel(Parcel in) {
            DhcpResults dhcpResults = new DhcpResults();
            DhcpResults.readFromParcel(dhcpResults, in);
            return dhcpResults;
        }

        public DhcpResults[] newArray(int size) {
            return new DhcpResults[size];
        }
    };
    private static final String TAG = "DhcpResults";
    public int leaseDuration;
    public int mtu;
    public Inet4Address serverAddress;
    public String vendorInfo;

    public DhcpResults(StaticIpConfiguration source) {
        super(source);
    }

    public DhcpResults(DhcpResults source) {
        super(source);
        if (source != null) {
            this.serverAddress = source.serverAddress;
            this.vendorInfo = source.vendorInfo;
            this.leaseDuration = source.leaseDuration;
            this.mtu = source.mtu;
        }
    }

    public boolean hasMeteredHint() {
        boolean z = false;
        if (this.vendorInfo != null && (this.vendorInfo.isEmpty() ^ 1) != 0) {
            if (this.vendorInfo.contains("ANDROID_METERED")) {
                z = true;
            } else if (this.vendorInfo.startsWith("hostname:") && this.ipAddress.getAddress() != null) {
                z = this.ipAddress.getAddress().toString().startsWith("/172.20.10.");
            }
            return z;
        } else if (this.domains == null || (this.domains.isEmpty() ^ 1) == 0) {
            return false;
        } else {
            return this.domains.equals("mshome.net");
        }
    }

    public void clear() {
        super.clear();
        this.vendorInfo = null;
        this.leaseDuration = 0;
        this.mtu = 0;
    }

    public String toString() {
        StringBuffer str = new StringBuffer(super.toString());
        str.append(" DHCP server ").append(this.serverAddress);
        str.append(" Vendor info ").append(this.vendorInfo);
        str.append(" lease ").append(this.leaseDuration).append(" seconds");
        if (this.mtu != 0) {
            str.append(" MTU ").append(this.mtu);
        }
        return str.toString();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DhcpResults)) {
            return false;
        }
        DhcpResults target = (DhcpResults) obj;
        if (!super.equals((StaticIpConfiguration) obj) || !Objects.equals(this.serverAddress, target.serverAddress) || !Objects.equals(this.vendorInfo, target.vendorInfo) || this.leaseDuration != target.leaseDuration) {
            z = false;
        } else if (this.mtu != target.mtu) {
            z = false;
        }
        return z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.leaseDuration);
        dest.writeInt(this.mtu);
        NetworkUtils.parcelInetAddress(dest, this.serverAddress, flags);
        dest.writeString(this.vendorInfo);
    }

    private static void readFromParcel(DhcpResults dhcpResults, Parcel in) {
        StaticIpConfiguration.readFromParcel(dhcpResults, in);
        dhcpResults.leaseDuration = in.readInt();
        dhcpResults.mtu = in.readInt();
        dhcpResults.serverAddress = (Inet4Address) NetworkUtils.unparcelInetAddress(in);
        dhcpResults.vendorInfo = in.readString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x000f A:{ExcHandler: java.lang.IllegalArgumentException (e java.lang.IllegalArgumentException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:5:0x0010, code:
            android.util.Log.e(TAG, "setIpAddress failed with addrString " + r6 + "/" + r7);
     */
    /* JADX WARNING: Missing block: B:6:0x0036, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setIpAddress(String addrString, int prefixLength) {
        try {
            this.ipAddress = new LinkAddress((Inet4Address) NetworkUtils.numericToInetAddress(addrString), prefixLength);
            return false;
        } catch (IllegalArgumentException e) {
        }
    }

    public boolean setGateway(String addrString) {
        try {
            this.gateway = NetworkUtils.numericToInetAddress(addrString);
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "setGateway failed with addrString " + addrString);
            return true;
        }
    }

    public boolean addDns(String addrString) {
        if (!TextUtils.isEmpty(addrString)) {
            try {
                this.dnsServers.add(NetworkUtils.numericToInetAddress(addrString));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "addDns failed with addrString " + addrString);
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x000a A:{ExcHandler: java.lang.IllegalArgumentException (e java.lang.IllegalArgumentException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:5:0x000b, code:
            android.util.Log.e(TAG, "setServerAddress failed with addrString " + r5);
     */
    /* JADX WARNING: Missing block: B:6:0x0026, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setServerAddress(String addrString) {
        try {
            this.serverAddress = (Inet4Address) NetworkUtils.numericToInetAddress(addrString);
            return false;
        } catch (IllegalArgumentException e) {
        }
    }

    public void setLeaseDuration(int duration) {
        this.leaseDuration = duration;
    }

    public void setVendorInfo(String info) {
        this.vendorInfo = info;
    }

    public void setDomains(String newDomains) {
        this.domains = newDomains;
    }
}
