package android.net.shared;

import android.net.DhcpResults;
import android.net.DhcpResultsParcelable;
import android.net.InetAddresses;
import java.net.Inet4Address;
import java.net.InetAddress;

public final class IpConfigurationParcelableUtil {
    public static DhcpResultsParcelable toStableParcelable(DhcpResults results) {
        if (results == null) {
            return null;
        }
        DhcpResultsParcelable p = new DhcpResultsParcelable();
        p.baseConfiguration = results.toStaticIpConfiguration();
        p.leaseDuration = results.leaseDuration;
        p.mtu = results.mtu;
        p.serverAddress = parcelAddress(results.serverAddress);
        p.vendorInfo = results.vendorInfo;
        p.serverHostName = results.serverHostName;
        return p;
    }

    public static DhcpResults fromStableParcelable(DhcpResultsParcelable p) {
        if (p == null) {
            return null;
        }
        DhcpResults results = new DhcpResults(p.baseConfiguration);
        results.leaseDuration = p.leaseDuration;
        results.mtu = p.mtu;
        results.serverAddress = (Inet4Address) unparcelAddress(p.serverAddress);
        results.vendorInfo = p.vendorInfo;
        results.serverHostName = p.serverHostName;
        return results;
    }

    public static String parcelAddress(InetAddress addr) {
        if (addr == null) {
            return null;
        }
        return addr.getHostAddress();
    }

    public static InetAddress unparcelAddress(String addr) {
        if (addr == null) {
            return null;
        }
        return InetAddresses.parseNumericAddress(addr);
    }
}
