package android.net.ip;

import android.net.INetd;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.util.SharedLog;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.system.OsConstants;
import java.net.InetAddress;

public class InterfaceController {
    private static final boolean DBG = false;
    private final String mIfName;
    private final SharedLog mLog;
    private final INetworkManagementService mNMS;
    private final INetd mNetd;

    public InterfaceController(String ifname, INetworkManagementService nms, INetd netd, SharedLog log) {
        this.mIfName = ifname;
        this.mNMS = nms;
        this.mNetd = netd;
        this.mLog = log;
    }

    public boolean setIPv4Address(LinkAddress address) {
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        ifcg.setLinkAddress(address);
        try {
            this.mNMS.setInterfaceConfig(this.mIfName, ifcg);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            logError("IPv4 configuration failed: %s", e);
            return false;
        }
    }

    public boolean clearIPv4Address() {
        try {
            InterfaceConfiguration ifcg = new InterfaceConfiguration();
            ifcg.setLinkAddress(new LinkAddress("0.0.0.0/0"));
            this.mNMS.setInterfaceConfig(this.mIfName, ifcg);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            logError("Failed to clear IPv4 address on interface %s: %s", this.mIfName, e);
            return false;
        }
    }

    public boolean enableIPv6() {
        try {
            this.mNMS.enableIpv6(this.mIfName);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            logError("enabling IPv6 failed: %s", e);
            return false;
        }
    }

    public boolean disableIPv6() {
        try {
            this.mNMS.disableIpv6(this.mIfName);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            logError("disabling IPv6 failed: %s", e);
            return false;
        }
    }

    public boolean setIPv6PrivacyExtensions(boolean enabled) {
        try {
            this.mNMS.setInterfaceIpv6PrivacyExtensions(this.mIfName, enabled);
            return true;
        } catch (RemoteException | IllegalStateException e) {
            logError("error setting IPv6 privacy extensions: %s", e);
            return false;
        }
    }

    public boolean setIPv6AddrGenModeIfSupported(int mode) {
        try {
            this.mNMS.setIPv6AddrGenMode(this.mIfName, mode);
        } catch (RemoteException e) {
            logError("Unable to set IPv6 addrgen mode: %s", e);
            return false;
        } catch (ServiceSpecificException e2) {
            if (e2.errorCode != OsConstants.EOPNOTSUPP) {
                logError("Unable to set IPv6 addrgen mode: %s", e2);
                return false;
            }
        }
        return true;
    }

    public boolean addAddress(LinkAddress addr) {
        return addAddress(addr.getAddress(), addr.getPrefixLength());
    }

    public boolean addAddress(InetAddress ip, int prefixLen) {
        try {
            this.mNetd.interfaceAddAddress(this.mIfName, ip.getHostAddress(), prefixLen);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            logError("failed to add %s/%d: %s", ip, Integer.valueOf(prefixLen), e);
            return false;
        }
    }

    public boolean removeAddress(InetAddress ip, int prefixLen) {
        try {
            this.mNetd.interfaceDelAddress(this.mIfName, ip.getHostAddress(), prefixLen);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            logError("failed to remove %s/%d: %s", ip, Integer.valueOf(prefixLen), e);
            return false;
        }
    }

    public boolean clearAllAddresses() {
        try {
            this.mNMS.clearInterfaceAddresses(this.mIfName);
            return true;
        } catch (Exception e) {
            logError("Failed to clear addresses: %s", e);
            return false;
        }
    }

    private void logError(String fmt, Object... args) {
        this.mLog.e(String.format(fmt, args));
    }
}
