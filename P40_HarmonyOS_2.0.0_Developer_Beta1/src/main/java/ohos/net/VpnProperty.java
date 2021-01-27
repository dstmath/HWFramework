package ohos.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCSkeleton;
import ohos.rpc.ReliableFileDescriptor;
import ohos.rpc.RemoteException;

public class VpnProperty {
    private static final int BYTE_MASK = 255;
    private static final int IPV4_MASK_MAX_LENGTH = 32;
    private static final int IPV6_MASK_MAX_LENGTH = 128;
    private static final int IP_ADDRESS_BYTE = 4;
    private static final int IP_MASK_SPLIT = 8;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "VpnProperty");
    private final List<LinkAddress> mAddresses = new ArrayList();
    private final VpnConfig mConfig = new VpnConfig();
    private final NetManagerProxy mNetManagerProxy = NetManagerProxy.getInstance();
    private final List<RouteInfo> mRoutes = new ArrayList();

    public VpnProperty() {
        this.mConfig.user = getClass().getName();
    }

    public void addAddress(String str, int i) {
        if (str == null || "".equals(str)) {
            throw new IllegalArgumentException("Bad address");
        }
        InetAddress transAddressToInetAddress = transAddressToInetAddress(str);
        if (transAddressToInetAddress != null) {
            check(transAddressToInetAddress, i);
            this.mAddresses.add(new LinkAddress(transAddressToInetAddress, i, 0, 0));
            this.mConfig.updateAcceptAddressType(transAddressToInetAddress);
            return;
        }
        throw new IllegalArgumentException("Bad length of address");
    }

    public void addRoute(String str, int i) {
        if (str == null || "".equals(str)) {
            throw new IllegalArgumentException("Bad address");
        }
        InetAddress transAddressToInetAddress = transAddressToInetAddress(str);
        if (transAddressToInetAddress != null) {
            check(transAddressToInetAddress, i);
            int i2 = i / 8;
            byte[] address = transAddressToInetAddress.getAddress();
            if (i2 < address.length) {
                address[i2] = (byte) (address[i2] << (i % 8));
                while (i2 < address.length) {
                    if (address[i2] == 0) {
                        i2++;
                    } else {
                        throw new IllegalArgumentException("Bad address");
                    }
                }
            }
            this.mRoutes.add(new RouteInfo(new IpPrefix(transAddressToInetAddress.getAddress(), i), null, null, 1));
            this.mConfig.updateAcceptAddressType(transAddressToInetAddress);
            return;
        }
        throw new IllegalArgumentException("Bad length of address");
    }

    public void addAcceptedApplication(String str, Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Bad context");
        } else if (this.mConfig.refusedApplications == null) {
            verifyApp(str, context);
            if (this.mConfig.acceptedApplications == null) {
                this.mConfig.acceptedApplications = new ArrayList();
            }
            this.mConfig.acceptedApplications.add(str);
        } else {
            throw new UnsupportedOperationException("addRefusedApplication already called");
        }
    }

    public void addRefusedApplication(String str, Context context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("Bad context");
        } else if (this.mConfig.acceptedApplications == null) {
            verifyApp(str, context);
            if (this.mConfig.refusedApplications == null) {
                this.mConfig.refusedApplications = new ArrayList();
            }
            this.mConfig.refusedApplications.add(str);
        } else {
            throw new UnsupportedOperationException("addAcceptedApplication already called");
        }
    }

    public void addDnsAddress(String str) {
        if (str == null || "".equals(str)) {
            throw new IllegalArgumentException("Bad address");
        }
        InetAddress transAddressToInetAddress = transAddressToInetAddress(str);
        if (transAddressToInetAddress == null) {
            throw new IllegalArgumentException("Bad length of address");
        } else if (transAddressToInetAddress.isLoopbackAddress() || transAddressToInetAddress.isAnyLocalAddress()) {
            throw new IllegalArgumentException("Bad address");
        } else {
            if (this.mConfig.dnsAddresses == null) {
                this.mConfig.dnsAddresses = new ArrayList();
            }
            this.mConfig.dnsAddresses.add(transAddressToInetAddress.getHostAddress());
        }
    }

    public void setMtu(int i) {
        if (i > 0) {
            this.mConfig.mtu = i;
            return;
        }
        throw new IllegalArgumentException("Bad mtu");
    }

    public void setBlock(boolean z) {
        this.mConfig.isBlock = z;
    }

    private InetAddress transAddressToInetAddress(String str) {
        String[] split = str.split("\\.");
        byte[] bArr = new byte[4];
        for (int i = 0; i < 4; i++) {
            bArr[i] = (byte) (Integer.parseInt(split[i]) & 255);
        }
        try {
            return InetAddress.getByAddress(bArr);
        } catch (UnknownHostException unused) {
            HiLog.warn(LABEL, "Failed to getByAddress, length of address is illegal", new Object[0]);
            return null;
        }
    }

    private void check(InetAddress inetAddress, int i) {
        if (inetAddress.isLoopbackAddress()) {
            throw new IllegalArgumentException("Bad address");
        } else if (inetAddress instanceof Inet4Address) {
            if (i < 0 || i > 32) {
                throw new IllegalArgumentException("Bad maskLength");
            }
        } else if (!(inetAddress instanceof Inet6Address)) {
            throw new IllegalArgumentException("Unsupported family");
        } else if (i < 0 || i > 128) {
            throw new IllegalArgumentException("Bad maskLength");
        }
    }

    private void verifyApp(String str, Context context) {
        try {
            context.getBundleManager().getApplicationInfo(str, 0, IPCSkeleton.getCallingUid());
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public ReliableFileDescriptor setUp() {
        VpnConfig vpnConfig = this.mConfig;
        vpnConfig.interfaceAddresses = this.mAddresses;
        vpnConfig.routes = this.mRoutes;
        try {
            return this.mNetManagerProxy.setUpVpn(vpnConfig);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to set up vpn", new Object[0]);
            return null;
        }
    }
}
