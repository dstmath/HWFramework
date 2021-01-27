package android.net.ip;

import android.net.INetd;
import android.net.InterfaceConfigurationParcel;
import android.net.LinkAddress;
import android.net.util.SharedLog;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.system.OsConstants;
import java.net.Inet4Address;
import java.net.InetAddress;

public class InterfaceController {
    private static final boolean DBG = false;
    private final String mIfName;
    private final SharedLog mLog;
    private final INetd mNetd;

    public InterfaceController(String ifname, INetd netd, SharedLog log) {
        this.mIfName = ifname;
        this.mNetd = netd;
        this.mLog = log;
    }

    private boolean setInterfaceAddress(LinkAddress addr) {
        InterfaceConfigurationParcel ifConfig = new InterfaceConfigurationParcel();
        ifConfig.ifName = this.mIfName;
        ifConfig.ipv4Addr = addr.getAddress().getHostAddress();
        ifConfig.prefixLength = addr.getPrefixLength();
        ifConfig.hwAddr = "";
        ifConfig.flags = new String[0];
        try {
            this.mNetd.interfaceSetCfg(ifConfig);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            logError("Setting IPv4 address to %s/%d failed: %s", ifConfig.ipv4Addr, Integer.valueOf(ifConfig.prefixLength), e);
            return false;
        }
    }

    public boolean setIPv4Address(LinkAddress address) {
        if (!(address.getAddress() instanceof Inet4Address)) {
            return false;
        }
        return setInterfaceAddress(address);
    }

    public boolean clearIPv4Address() {
        return setInterfaceAddress(new LinkAddress("0.0.0.0/0"));
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0016: APUT  (r2v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r3v0 java.lang.String) */
    private boolean setEnableIPv6(boolean enabled) {
        try {
            this.mNetd.interfaceSetEnableIPv6(this.mIfName, enabled);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            Object[] objArr = new Object[2];
            objArr[0] = enabled ? "enabling" : "disabling";
            objArr[1] = e;
            logError("%s IPv6 failed: %s", objArr);
            return false;
        }
    }

    public boolean enableIPv6() {
        return setEnableIPv6(true);
    }

    public boolean disableIPv6() {
        return setEnableIPv6(false);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0016: APUT  (r2v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r3v0 java.lang.String) */
    public boolean setIPv6PrivacyExtensions(boolean enabled) {
        try {
            this.mNetd.interfaceSetIPv6PrivacyExtensions(this.mIfName, enabled);
            return true;
        } catch (RemoteException | ServiceSpecificException e) {
            Object[] objArr = new Object[2];
            objArr[0] = enabled ? "enabling" : "disabling";
            objArr[1] = e;
            logError("error %s IPv6 privacy extensions: %s", objArr);
            return false;
        }
    }

    public boolean setIPv6AddrGenModeIfSupported(int mode) {
        try {
            this.mNetd.setIPv6AddrGenMode(this.mIfName, mode);
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
            this.mNetd.interfaceClearAddrs(this.mIfName);
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
