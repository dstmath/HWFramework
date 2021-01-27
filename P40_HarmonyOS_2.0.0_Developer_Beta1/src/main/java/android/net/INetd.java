package android.net;

import android.net.INetdUnsolicitedEventListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface INetd extends IInterface {
    public static final int CONF = 1;
    public static final int FIREWALL_BLACKLIST = 1;
    public static final int FIREWALL_CHAIN_DOZABLE = 1;
    public static final int FIREWALL_CHAIN_NONE = 0;
    public static final int FIREWALL_CHAIN_POWERSAVE = 3;
    public static final int FIREWALL_CHAIN_STANDBY = 2;
    public static final int FIREWALL_RULE_ALLOW = 1;
    public static final int FIREWALL_RULE_DENY = 2;
    public static final int FIREWALL_WHITELIST = 0;
    public static final String IF_FLAG_BROADCAST = "broadcast";
    public static final String IF_FLAG_LOOPBACK = "loopback";
    public static final String IF_FLAG_MULTICAST = "multicast";
    public static final String IF_FLAG_POINTOPOINT = "point-to-point";
    public static final String IF_FLAG_RUNNING = "running";
    public static final String IF_STATE_DOWN = "down";
    public static final String IF_STATE_UP = "up";
    public static final String IPSEC_INTERFACE_PREFIX = "ipsec";
    public static final int IPV4 = 4;
    public static final int IPV6 = 6;
    public static final int IPV6_ADDR_GEN_MODE_DEFAULT = 0;
    public static final int IPV6_ADDR_GEN_MODE_EUI64 = 0;
    public static final int IPV6_ADDR_GEN_MODE_NONE = 1;
    public static final int IPV6_ADDR_GEN_MODE_RANDOM = 3;
    public static final int IPV6_ADDR_GEN_MODE_STABLE_PRIVACY = 2;
    public static final int LOCAL_NET_ID = 99;
    public static final int NEIGH = 2;
    public static final String NEXTHOP_NONE = "";
    public static final String NEXTHOP_THROW = "throw";
    public static final String NEXTHOP_UNREACHABLE = "unreachable";
    public static final int NO_PERMISSIONS = 0;
    public static final int PENALTY_POLICY_ACCEPT = 1;
    public static final int PENALTY_POLICY_LOG = 2;
    public static final int PENALTY_POLICY_REJECT = 3;
    public static final int PERMISSION_INTERNET = 4;
    public static final int PERMISSION_NETWORK = 1;
    public static final int PERMISSION_NONE = 0;
    public static final int PERMISSION_SYSTEM = 2;
    public static final int PERMISSION_UNINSTALLED = -1;
    public static final int PERMISSION_UPDATE_DEVICE_STATS = 8;
    public static final int VERSION = 2;

    void bandwidthAddNaughtyApp(int i) throws RemoteException;

    void bandwidthAddNiceApp(int i) throws RemoteException;

    boolean bandwidthEnableDataSaver(boolean z) throws RemoteException;

    void bandwidthRemoveInterfaceAlert(String str) throws RemoteException;

    void bandwidthRemoveInterfaceQuota(String str) throws RemoteException;

    void bandwidthRemoveNaughtyApp(int i) throws RemoteException;

    void bandwidthRemoveNiceApp(int i) throws RemoteException;

    void bandwidthSetGlobalAlert(long j) throws RemoteException;

    void bandwidthSetInterfaceAlert(String str, long j) throws RemoteException;

    void bandwidthSetInterfaceQuota(String str, long j) throws RemoteException;

    String clatdStart(String str, String str2) throws RemoteException;

    void clatdStop(String str) throws RemoteException;

    void firewallAddUidInterfaceRules(String str, int[] iArr) throws RemoteException;

    void firewallEnableChildChain(int i, boolean z) throws RemoteException;

    void firewallRemoveUidInterfaceRules(int[] iArr) throws RemoteException;

    boolean firewallReplaceUidChain(String str, boolean z, int[] iArr) throws RemoteException;

    void firewallSetFirewallType(int i) throws RemoteException;

    void firewallSetInterfaceRule(String str, int i) throws RemoteException;

    void firewallSetUidRule(int i, int i2, int i3) throws RemoteException;

    int getInterfaceVersion() throws RemoteException;

    IBinder getOemNetd() throws RemoteException;

    String getProcSysNet(int i, int i2, String str, String str2) throws RemoteException;

    void idletimerAddInterface(String str, int i, String str2) throws RemoteException;

    void idletimerRemoveInterface(String str, int i, String str2) throws RemoteException;

    void interfaceAddAddress(String str, String str2, int i) throws RemoteException;

    void interfaceClearAddrs(String str) throws RemoteException;

    void interfaceDelAddress(String str, String str2, int i) throws RemoteException;

    InterfaceConfigurationParcel interfaceGetCfg(String str) throws RemoteException;

    String[] interfaceGetList() throws RemoteException;

    void interfaceSetCfg(InterfaceConfigurationParcel interfaceConfigurationParcel) throws RemoteException;

    void interfaceSetEnableIPv6(String str, boolean z) throws RemoteException;

    void interfaceSetIPv6PrivacyExtensions(String str, boolean z) throws RemoteException;

    void interfaceSetMtu(String str, int i) throws RemoteException;

    void ipSecAddSecurityAssociation(int i, int i2, String str, String str2, int i3, int i4, int i5, int i6, String str3, byte[] bArr, int i7, String str4, byte[] bArr2, int i8, String str5, byte[] bArr3, int i9, int i10, int i11, int i12, int i13) throws RemoteException;

    void ipSecAddSecurityPolicy(int i, int i2, int i3, String str, String str2, int i4, int i5, int i6, int i7) throws RemoteException;

    void ipSecAddTunnelInterface(String str, String str2, String str3, int i, int i2, int i3) throws RemoteException;

    int ipSecAllocateSpi(int i, String str, String str2, int i2) throws RemoteException;

    void ipSecApplyTransportModeTransform(ParcelFileDescriptor parcelFileDescriptor, int i, int i2, String str, String str2, int i3) throws RemoteException;

    void ipSecDeleteSecurityAssociation(int i, String str, String str2, int i2, int i3, int i4, int i5) throws RemoteException;

    void ipSecDeleteSecurityPolicy(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException;

    void ipSecRemoveTransportModeTransform(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void ipSecRemoveTunnelInterface(String str) throws RemoteException;

    void ipSecSetEncapSocketOwner(ParcelFileDescriptor parcelFileDescriptor, int i) throws RemoteException;

    void ipSecUpdateSecurityPolicy(int i, int i2, int i3, String str, String str2, int i4, int i5, int i6, int i7) throws RemoteException;

    void ipSecUpdateTunnelInterface(String str, String str2, String str3, int i, int i2, int i3) throws RemoteException;

    void ipfwdAddInterfaceForward(String str, String str2) throws RemoteException;

    void ipfwdDisableForwarding(String str) throws RemoteException;

    void ipfwdEnableForwarding(String str) throws RemoteException;

    boolean ipfwdEnabled() throws RemoteException;

    String[] ipfwdGetRequesterList() throws RemoteException;

    void ipfwdRemoveInterfaceForward(String str, String str2) throws RemoteException;

    boolean isAlive() throws RemoteException;

    void networkAddInterface(int i, String str) throws RemoteException;

    void networkAddLegacyRoute(int i, String str, String str2, String str3, int i2) throws RemoteException;

    void networkAddRoute(int i, String str, String str2, String str3) throws RemoteException;

    void networkAddUidRanges(int i, UidRangeParcel[] uidRangeParcelArr) throws RemoteException;

    boolean networkCanProtect(int i) throws RemoteException;

    void networkClearDefault() throws RemoteException;

    void networkClearPermissionForUser(int[] iArr) throws RemoteException;

    void networkCreatePhysical(int i, int i2) throws RemoteException;

    void networkCreateVpn(int i, boolean z) throws RemoteException;

    void networkDestroy(int i) throws RemoteException;

    int networkGetDefault() throws RemoteException;

    void networkRejectNonSecureVpn(boolean z, UidRangeParcel[] uidRangeParcelArr) throws RemoteException;

    void networkRemoveInterface(int i, String str) throws RemoteException;

    void networkRemoveLegacyRoute(int i, String str, String str2, String str3, int i2) throws RemoteException;

    void networkRemoveRoute(int i, String str, String str2, String str3) throws RemoteException;

    void networkRemoveUidRanges(int i, UidRangeParcel[] uidRangeParcelArr) throws RemoteException;

    void networkSetDefault(int i) throws RemoteException;

    void networkSetPermissionForNetwork(int i, int i2) throws RemoteException;

    void networkSetPermissionForUser(int i, int[] iArr) throws RemoteException;

    void networkSetProtectAllow(int i) throws RemoteException;

    void networkSetProtectDeny(int i) throws RemoteException;

    void registerUnsolicitedEventListener(INetdUnsolicitedEventListener iNetdUnsolicitedEventListener) throws RemoteException;

    void setIPv6AddrGenMode(String str, int i) throws RemoteException;

    void setProcSysNet(int i, int i2, String str, String str2, String str3) throws RemoteException;

    void setTcpRWmemorySize(String str, String str2) throws RemoteException;

    void socketDestroy(UidRangeParcel[] uidRangeParcelArr, int[] iArr) throws RemoteException;

    void strictUidCleartextPenalty(int i, int i2) throws RemoteException;

    void tetherAddForward(String str, String str2) throws RemoteException;

    boolean tetherApplyDnsInterfaces() throws RemoteException;

    String[] tetherDnsList() throws RemoteException;

    void tetherDnsSet(int i, String[] strArr) throws RemoteException;

    TetherStatsParcel[] tetherGetStats() throws RemoteException;

    void tetherInterfaceAdd(String str) throws RemoteException;

    String[] tetherInterfaceList() throws RemoteException;

    void tetherInterfaceRemove(String str) throws RemoteException;

    boolean tetherIsEnabled() throws RemoteException;

    void tetherRemoveForward(String str, String str2) throws RemoteException;

    void tetherStart(String[] strArr) throws RemoteException;

    void tetherStop() throws RemoteException;

    void trafficSetNetPermForUids(int i, int[] iArr) throws RemoteException;

    void trafficSwapActiveStatsMap() throws RemoteException;

    void wakeupAddInterface(String str, String str2, int i, int i2) throws RemoteException;

    void wakeupDelInterface(String str, String str2, int i, int i2) throws RemoteException;

    public static class Default implements INetd {
        @Override // android.net.INetd
        public boolean isAlive() throws RemoteException {
            return false;
        }

        @Override // android.net.INetd
        public boolean firewallReplaceUidChain(String chainName, boolean isWhitelist, int[] uids) throws RemoteException {
            return false;
        }

        @Override // android.net.INetd
        public boolean bandwidthEnableDataSaver(boolean enable) throws RemoteException {
            return false;
        }

        @Override // android.net.INetd
        public void networkCreatePhysical(int netId, int permission) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkCreateVpn(int netId, boolean secure) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkDestroy(int netId) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkAddInterface(int netId, String iface) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkRemoveInterface(int netId, String iface) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkAddUidRanges(int netId, UidRangeParcel[] uidRanges) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkRemoveUidRanges(int netId, UidRangeParcel[] uidRanges) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkRejectNonSecureVpn(boolean add, UidRangeParcel[] uidRanges) throws RemoteException {
        }

        @Override // android.net.INetd
        public void socketDestroy(UidRangeParcel[] uidRanges, int[] exemptUids) throws RemoteException {
        }

        @Override // android.net.INetd
        public boolean tetherApplyDnsInterfaces() throws RemoteException {
            return false;
        }

        @Override // android.net.INetd
        public TetherStatsParcel[] tetherGetStats() throws RemoteException {
            return null;
        }

        @Override // android.net.INetd
        public void interfaceAddAddress(String ifName, String addrString, int prefixLength) throws RemoteException {
        }

        @Override // android.net.INetd
        public void interfaceDelAddress(String ifName, String addrString, int prefixLength) throws RemoteException {
        }

        @Override // android.net.INetd
        public String getProcSysNet(int ipversion, int which, String ifname, String parameter) throws RemoteException {
            return null;
        }

        @Override // android.net.INetd
        public void setProcSysNet(int ipversion, int which, String ifname, String parameter, String value) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecSetEncapSocketOwner(ParcelFileDescriptor socket, int newUid) throws RemoteException {
        }

        @Override // android.net.INetd
        public int ipSecAllocateSpi(int transformId, String sourceAddress, String destinationAddress, int spi) throws RemoteException {
            return 0;
        }

        @Override // android.net.INetd
        public void ipSecAddSecurityAssociation(int transformId, int mode, String sourceAddress, String destinationAddress, int underlyingNetId, int spi, int markValue, int markMask, String authAlgo, byte[] authKey, int authTruncBits, String cryptAlgo, byte[] cryptKey, int cryptTruncBits, String aeadAlgo, byte[] aeadKey, int aeadIcvBits, int encapType, int encapLocalPort, int encapRemotePort, int interfaceId) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecDeleteSecurityAssociation(int transformId, String sourceAddress, String destinationAddress, int spi, int markValue, int markMask, int interfaceId) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecApplyTransportModeTransform(ParcelFileDescriptor socket, int transformId, int direction, String sourceAddress, String destinationAddress, int spi) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecRemoveTransportModeTransform(ParcelFileDescriptor socket) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecAddSecurityPolicy(int transformId, int selAddrFamily, int direction, String tmplSrcAddress, String tmplDstAddress, int spi, int markValue, int markMask, int interfaceId) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecUpdateSecurityPolicy(int transformId, int selAddrFamily, int direction, String tmplSrcAddress, String tmplDstAddress, int spi, int markValue, int markMask, int interfaceId) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecDeleteSecurityPolicy(int transformId, int selAddrFamily, int direction, int markValue, int markMask, int interfaceId) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecAddTunnelInterface(String deviceName, String localAddress, String remoteAddress, int iKey, int oKey, int interfaceId) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecUpdateTunnelInterface(String deviceName, String localAddress, String remoteAddress, int iKey, int oKey, int interfaceId) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipSecRemoveTunnelInterface(String deviceName) throws RemoteException {
        }

        @Override // android.net.INetd
        public void wakeupAddInterface(String ifName, String prefix, int mark, int mask) throws RemoteException {
        }

        @Override // android.net.INetd
        public void wakeupDelInterface(String ifName, String prefix, int mark, int mask) throws RemoteException {
        }

        @Override // android.net.INetd
        public void setIPv6AddrGenMode(String ifName, int mode) throws RemoteException {
        }

        @Override // android.net.INetd
        public void idletimerAddInterface(String ifName, int timeout, String classLabel) throws RemoteException {
        }

        @Override // android.net.INetd
        public void idletimerRemoveInterface(String ifName, int timeout, String classLabel) throws RemoteException {
        }

        @Override // android.net.INetd
        public void strictUidCleartextPenalty(int uid, int policyPenalty) throws RemoteException {
        }

        @Override // android.net.INetd
        public String clatdStart(String ifName, String nat64Prefix) throws RemoteException {
            return null;
        }

        @Override // android.net.INetd
        public void clatdStop(String ifName) throws RemoteException {
        }

        @Override // android.net.INetd
        public boolean ipfwdEnabled() throws RemoteException {
            return false;
        }

        @Override // android.net.INetd
        public String[] ipfwdGetRequesterList() throws RemoteException {
            return null;
        }

        @Override // android.net.INetd
        public void ipfwdEnableForwarding(String requester) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipfwdDisableForwarding(String requester) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipfwdAddInterfaceForward(String fromIface, String toIface) throws RemoteException {
        }

        @Override // android.net.INetd
        public void ipfwdRemoveInterfaceForward(String fromIface, String toIface) throws RemoteException {
        }

        @Override // android.net.INetd
        public void bandwidthSetInterfaceQuota(String ifName, long bytes) throws RemoteException {
        }

        @Override // android.net.INetd
        public void bandwidthRemoveInterfaceQuota(String ifName) throws RemoteException {
        }

        @Override // android.net.INetd
        public void bandwidthSetInterfaceAlert(String ifName, long bytes) throws RemoteException {
        }

        @Override // android.net.INetd
        public void bandwidthRemoveInterfaceAlert(String ifName) throws RemoteException {
        }

        @Override // android.net.INetd
        public void bandwidthSetGlobalAlert(long bytes) throws RemoteException {
        }

        @Override // android.net.INetd
        public void bandwidthAddNaughtyApp(int uid) throws RemoteException {
        }

        @Override // android.net.INetd
        public void bandwidthRemoveNaughtyApp(int uid) throws RemoteException {
        }

        @Override // android.net.INetd
        public void bandwidthAddNiceApp(int uid) throws RemoteException {
        }

        @Override // android.net.INetd
        public void bandwidthRemoveNiceApp(int uid) throws RemoteException {
        }

        @Override // android.net.INetd
        public void tetherStart(String[] dhcpRanges) throws RemoteException {
        }

        @Override // android.net.INetd
        public void tetherStop() throws RemoteException {
        }

        @Override // android.net.INetd
        public boolean tetherIsEnabled() throws RemoteException {
            return false;
        }

        @Override // android.net.INetd
        public void tetherInterfaceAdd(String ifName) throws RemoteException {
        }

        @Override // android.net.INetd
        public void tetherInterfaceRemove(String ifName) throws RemoteException {
        }

        @Override // android.net.INetd
        public String[] tetherInterfaceList() throws RemoteException {
            return null;
        }

        @Override // android.net.INetd
        public void tetherDnsSet(int netId, String[] dnsAddrs) throws RemoteException {
        }

        @Override // android.net.INetd
        public String[] tetherDnsList() throws RemoteException {
            return null;
        }

        @Override // android.net.INetd
        public void networkAddRoute(int netId, String ifName, String destination, String nextHop) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkRemoveRoute(int netId, String ifName, String destination, String nextHop) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkAddLegacyRoute(int netId, String ifName, String destination, String nextHop, int uid) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkRemoveLegacyRoute(int netId, String ifName, String destination, String nextHop, int uid) throws RemoteException {
        }

        @Override // android.net.INetd
        public int networkGetDefault() throws RemoteException {
            return 0;
        }

        @Override // android.net.INetd
        public void networkSetDefault(int netId) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkClearDefault() throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkSetPermissionForNetwork(int netId, int permission) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkSetPermissionForUser(int permission, int[] uids) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkClearPermissionForUser(int[] uids) throws RemoteException {
        }

        @Override // android.net.INetd
        public void trafficSetNetPermForUids(int permission, int[] uids) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkSetProtectAllow(int uid) throws RemoteException {
        }

        @Override // android.net.INetd
        public void networkSetProtectDeny(int uid) throws RemoteException {
        }

        @Override // android.net.INetd
        public boolean networkCanProtect(int uid) throws RemoteException {
            return false;
        }

        @Override // android.net.INetd
        public void firewallSetFirewallType(int firewalltype) throws RemoteException {
        }

        @Override // android.net.INetd
        public void firewallSetInterfaceRule(String ifName, int firewallRule) throws RemoteException {
        }

        @Override // android.net.INetd
        public void firewallSetUidRule(int childChain, int uid, int firewallRule) throws RemoteException {
        }

        @Override // android.net.INetd
        public void firewallEnableChildChain(int childChain, boolean enable) throws RemoteException {
        }

        @Override // android.net.INetd
        public String[] interfaceGetList() throws RemoteException {
            return null;
        }

        @Override // android.net.INetd
        public InterfaceConfigurationParcel interfaceGetCfg(String ifName) throws RemoteException {
            return null;
        }

        @Override // android.net.INetd
        public void interfaceSetCfg(InterfaceConfigurationParcel cfg) throws RemoteException {
        }

        @Override // android.net.INetd
        public void interfaceSetIPv6PrivacyExtensions(String ifName, boolean enable) throws RemoteException {
        }

        @Override // android.net.INetd
        public void interfaceClearAddrs(String ifName) throws RemoteException {
        }

        @Override // android.net.INetd
        public void interfaceSetEnableIPv6(String ifName, boolean enable) throws RemoteException {
        }

        @Override // android.net.INetd
        public void interfaceSetMtu(String ifName, int mtu) throws RemoteException {
        }

        @Override // android.net.INetd
        public void tetherAddForward(String intIface, String extIface) throws RemoteException {
        }

        @Override // android.net.INetd
        public void tetherRemoveForward(String intIface, String extIface) throws RemoteException {
        }

        @Override // android.net.INetd
        public void setTcpRWmemorySize(String rmemValues, String wmemValues) throws RemoteException {
        }

        @Override // android.net.INetd
        public void registerUnsolicitedEventListener(INetdUnsolicitedEventListener listener) throws RemoteException {
        }

        @Override // android.net.INetd
        public void firewallAddUidInterfaceRules(String ifName, int[] uids) throws RemoteException {
        }

        @Override // android.net.INetd
        public void firewallRemoveUidInterfaceRules(int[] uids) throws RemoteException {
        }

        @Override // android.net.INetd
        public void trafficSwapActiveStatsMap() throws RemoteException {
        }

        @Override // android.net.INetd
        public IBinder getOemNetd() throws RemoteException {
            return null;
        }

        @Override // android.net.INetd
        public int getInterfaceVersion() {
            return -1;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetd {
        private static final String DESCRIPTOR = "android.net.INetd";
        static final int TRANSACTION_bandwidthAddNaughtyApp = 50;
        static final int TRANSACTION_bandwidthAddNiceApp = 52;
        static final int TRANSACTION_bandwidthEnableDataSaver = 3;
        static final int TRANSACTION_bandwidthRemoveInterfaceAlert = 48;
        static final int TRANSACTION_bandwidthRemoveInterfaceQuota = 46;
        static final int TRANSACTION_bandwidthRemoveNaughtyApp = 51;
        static final int TRANSACTION_bandwidthRemoveNiceApp = 53;
        static final int TRANSACTION_bandwidthSetGlobalAlert = 49;
        static final int TRANSACTION_bandwidthSetInterfaceAlert = 47;
        static final int TRANSACTION_bandwidthSetInterfaceQuota = 45;
        static final int TRANSACTION_clatdStart = 37;
        static final int TRANSACTION_clatdStop = 38;
        static final int TRANSACTION_firewallAddUidInterfaceRules = 91;
        static final int TRANSACTION_firewallEnableChildChain = 79;
        static final int TRANSACTION_firewallRemoveUidInterfaceRules = 92;
        static final int TRANSACTION_firewallReplaceUidChain = 2;
        static final int TRANSACTION_firewallSetFirewallType = 76;
        static final int TRANSACTION_firewallSetInterfaceRule = 77;
        static final int TRANSACTION_firewallSetUidRule = 78;
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_getOemNetd = 94;
        static final int TRANSACTION_getProcSysNet = 17;
        static final int TRANSACTION_idletimerAddInterface = 34;
        static final int TRANSACTION_idletimerRemoveInterface = 35;
        static final int TRANSACTION_interfaceAddAddress = 15;
        static final int TRANSACTION_interfaceClearAddrs = 84;
        static final int TRANSACTION_interfaceDelAddress = 16;
        static final int TRANSACTION_interfaceGetCfg = 81;
        static final int TRANSACTION_interfaceGetList = 80;
        static final int TRANSACTION_interfaceSetCfg = 82;
        static final int TRANSACTION_interfaceSetEnableIPv6 = 85;
        static final int TRANSACTION_interfaceSetIPv6PrivacyExtensions = 83;
        static final int TRANSACTION_interfaceSetMtu = 86;
        static final int TRANSACTION_ipSecAddSecurityAssociation = 21;
        static final int TRANSACTION_ipSecAddSecurityPolicy = 25;
        static final int TRANSACTION_ipSecAddTunnelInterface = 28;
        static final int TRANSACTION_ipSecAllocateSpi = 20;
        static final int TRANSACTION_ipSecApplyTransportModeTransform = 23;
        static final int TRANSACTION_ipSecDeleteSecurityAssociation = 22;
        static final int TRANSACTION_ipSecDeleteSecurityPolicy = 27;
        static final int TRANSACTION_ipSecRemoveTransportModeTransform = 24;
        static final int TRANSACTION_ipSecRemoveTunnelInterface = 30;
        static final int TRANSACTION_ipSecSetEncapSocketOwner = 19;
        static final int TRANSACTION_ipSecUpdateSecurityPolicy = 26;
        static final int TRANSACTION_ipSecUpdateTunnelInterface = 29;
        static final int TRANSACTION_ipfwdAddInterfaceForward = 43;
        static final int TRANSACTION_ipfwdDisableForwarding = 42;
        static final int TRANSACTION_ipfwdEnableForwarding = 41;
        static final int TRANSACTION_ipfwdEnabled = 39;
        static final int TRANSACTION_ipfwdGetRequesterList = 40;
        static final int TRANSACTION_ipfwdRemoveInterfaceForward = 44;
        static final int TRANSACTION_isAlive = 1;
        static final int TRANSACTION_networkAddInterface = 7;
        static final int TRANSACTION_networkAddLegacyRoute = 64;
        static final int TRANSACTION_networkAddRoute = 62;
        static final int TRANSACTION_networkAddUidRanges = 9;
        static final int TRANSACTION_networkCanProtect = 75;
        static final int TRANSACTION_networkClearDefault = 68;
        static final int TRANSACTION_networkClearPermissionForUser = 71;
        static final int TRANSACTION_networkCreatePhysical = 4;
        static final int TRANSACTION_networkCreateVpn = 5;
        static final int TRANSACTION_networkDestroy = 6;
        static final int TRANSACTION_networkGetDefault = 66;
        static final int TRANSACTION_networkRejectNonSecureVpn = 11;
        static final int TRANSACTION_networkRemoveInterface = 8;
        static final int TRANSACTION_networkRemoveLegacyRoute = 65;
        static final int TRANSACTION_networkRemoveRoute = 63;
        static final int TRANSACTION_networkRemoveUidRanges = 10;
        static final int TRANSACTION_networkSetDefault = 67;
        static final int TRANSACTION_networkSetPermissionForNetwork = 69;
        static final int TRANSACTION_networkSetPermissionForUser = 70;
        static final int TRANSACTION_networkSetProtectAllow = 73;
        static final int TRANSACTION_networkSetProtectDeny = 74;
        static final int TRANSACTION_registerUnsolicitedEventListener = 90;
        static final int TRANSACTION_setIPv6AddrGenMode = 33;
        static final int TRANSACTION_setProcSysNet = 18;
        static final int TRANSACTION_setTcpRWmemorySize = 89;
        static final int TRANSACTION_socketDestroy = 12;
        static final int TRANSACTION_strictUidCleartextPenalty = 36;
        static final int TRANSACTION_tetherAddForward = 87;
        static final int TRANSACTION_tetherApplyDnsInterfaces = 13;
        static final int TRANSACTION_tetherDnsList = 61;
        static final int TRANSACTION_tetherDnsSet = 60;
        static final int TRANSACTION_tetherGetStats = 14;
        static final int TRANSACTION_tetherInterfaceAdd = 57;
        static final int TRANSACTION_tetherInterfaceList = 59;
        static final int TRANSACTION_tetherInterfaceRemove = 58;
        static final int TRANSACTION_tetherIsEnabled = 56;
        static final int TRANSACTION_tetherRemoveForward = 88;
        static final int TRANSACTION_tetherStart = 54;
        static final int TRANSACTION_tetherStop = 55;
        static final int TRANSACTION_trafficSetNetPermForUids = 72;
        static final int TRANSACTION_trafficSwapActiveStatsMap = 93;
        static final int TRANSACTION_wakeupAddInterface = 31;
        static final int TRANSACTION_wakeupDelInterface = 32;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetd asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetd)) {
                return new Proxy(obj);
            }
            return (INetd) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelFileDescriptor _arg0;
            ParcelFileDescriptor _arg02;
            ParcelFileDescriptor _arg03;
            InterfaceConfigurationParcel _arg04;
            if (code == TRANSACTION_getInterfaceVersion) {
                data.enforceInterface(DESCRIPTOR);
                reply.writeNoException();
                reply.writeInt(getInterfaceVersion());
                return true;
            } else if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAlive = isAlive();
                        reply.writeNoException();
                        reply.writeInt(isAlive ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean firewallReplaceUidChain = firewallReplaceUidChain(_arg05, _arg1, data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(firewallReplaceUidChain ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean bandwidthEnableDataSaver = bandwidthEnableDataSaver(_arg1);
                        reply.writeNoException();
                        reply.writeInt(bandwidthEnableDataSaver ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        networkCreatePhysical(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        networkCreateVpn(_arg06, _arg1);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        networkDestroy(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        networkAddInterface(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        networkRemoveInterface(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        networkAddUidRanges(data.readInt(), (UidRangeParcel[]) data.createTypedArray(UidRangeParcel.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        networkRemoveUidRanges(data.readInt(), (UidRangeParcel[]) data.createTypedArray(UidRangeParcel.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        networkRejectNonSecureVpn(_arg1, (UidRangeParcel[]) data.createTypedArray(UidRangeParcel.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        socketDestroy((UidRangeParcel[]) data.createTypedArray(UidRangeParcel.CREATOR), data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean tetherApplyDnsInterfaces = tetherApplyDnsInterfaces();
                        reply.writeNoException();
                        reply.writeInt(tetherApplyDnsInterfaces ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        TetherStatsParcel[] _result = tetherGetStats();
                        reply.writeNoException();
                        reply.writeTypedArray(_result, 1);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        interfaceAddAddress(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        interfaceDelAddress(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getProcSysNet(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        setProcSysNet(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        ipSecSetEncapSocketOwner(_arg0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = ipSecAllocateSpi(data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        ipSecAddSecurityAssociation(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.createByteArray(), data.readInt(), data.readString(), data.createByteArray(), data.readInt(), data.readString(), data.createByteArray(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        ipSecDeleteSecurityAssociation(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_ipSecApplyTransportModeTransform /* 23 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        ipSecApplyTransportModeTransform(_arg02, data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_ipSecRemoveTransportModeTransform /* 24 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        ipSecRemoveTransportModeTransform(_arg03);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_ipSecAddSecurityPolicy /* 25 */:
                        data.enforceInterface(DESCRIPTOR);
                        ipSecAddSecurityPolicy(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        ipSecUpdateSecurityPolicy(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_ipSecDeleteSecurityPolicy /* 27 */:
                        data.enforceInterface(DESCRIPTOR);
                        ipSecDeleteSecurityPolicy(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_ipSecAddTunnelInterface /* 28 */:
                        data.enforceInterface(DESCRIPTOR);
                        ipSecAddTunnelInterface(data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        ipSecUpdateTunnelInterface(data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        ipSecRemoveTunnelInterface(data.readString());
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        wakeupAddInterface(data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        wakeupDelInterface(data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        setIPv6AddrGenMode(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        idletimerAddInterface(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        idletimerRemoveInterface(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        strictUidCleartextPenalty(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = clatdStart(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        clatdStop(data.readString());
                        reply.writeNoException();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        boolean ipfwdEnabled = ipfwdEnabled();
                        reply.writeNoException();
                        reply.writeInt(ipfwdEnabled ? 1 : 0);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result5 = ipfwdGetRequesterList();
                        reply.writeNoException();
                        reply.writeStringArray(_result5);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        ipfwdEnableForwarding(data.readString());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        ipfwdDisableForwarding(data.readString());
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        ipfwdAddInterfaceForward(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        ipfwdRemoveInterfaceForward(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        bandwidthSetInterfaceQuota(data.readString(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_bandwidthRemoveInterfaceQuota /* 46 */:
                        data.enforceInterface(DESCRIPTOR);
                        bandwidthRemoveInterfaceQuota(data.readString());
                        reply.writeNoException();
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        bandwidthSetInterfaceAlert(data.readString(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        bandwidthRemoveInterfaceAlert(data.readString());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        bandwidthSetGlobalAlert(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        bandwidthAddNaughtyApp(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        bandwidthRemoveNaughtyApp(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        bandwidthAddNiceApp(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        bandwidthRemoveNiceApp(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        tetherStart(data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        tetherStop();
                        reply.writeNoException();
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        boolean tetherIsEnabled = tetherIsEnabled();
                        reply.writeNoException();
                        reply.writeInt(tetherIsEnabled ? 1 : 0);
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        tetherInterfaceAdd(data.readString());
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        tetherInterfaceRemove(data.readString());
                        reply.writeNoException();
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result6 = tetherInterfaceList();
                        reply.writeNoException();
                        reply.writeStringArray(_result6);
                        return true;
                    case TRANSACTION_tetherDnsSet /* 60 */:
                        data.enforceInterface(DESCRIPTOR);
                        tetherDnsSet(data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_tetherDnsList /* 61 */:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result7 = tetherDnsList();
                        reply.writeNoException();
                        reply.writeStringArray(_result7);
                        return true;
                    case TRANSACTION_networkAddRoute /* 62 */:
                        data.enforceInterface(DESCRIPTOR);
                        networkAddRoute(data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_networkRemoveRoute /* 63 */:
                        data.enforceInterface(DESCRIPTOR);
                        networkRemoveRoute(data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        networkAddLegacyRoute(data.readInt(), data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        networkRemoveLegacyRoute(data.readInt(), data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = networkGetDefault();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        networkSetDefault(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        networkClearDefault();
                        reply.writeNoException();
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        networkSetPermissionForNetwork(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        networkSetPermissionForUser(data.readInt(), data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        networkClearPermissionForUser(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        trafficSetNetPermForUids(data.readInt(), data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        networkSetProtectAllow(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        networkSetProtectDeny(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        boolean networkCanProtect = networkCanProtect(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(networkCanProtect ? 1 : 0);
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        firewallSetFirewallType(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        firewallSetInterfaceRule(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        firewallSetUidRule(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        firewallEnableChildChain(_arg07, _arg1);
                        reply.writeNoException();
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result9 = interfaceGetList();
                        reply.writeNoException();
                        reply.writeStringArray(_result9);
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        InterfaceConfigurationParcel _result10 = interfaceGetCfg(data.readString());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = InterfaceConfigurationParcel.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        interfaceSetCfg(_arg04);
                        reply.writeNoException();
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        interfaceSetIPv6PrivacyExtensions(_arg08, _arg1);
                        reply.writeNoException();
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        interfaceClearAddrs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        interfaceSetEnableIPv6(_arg09, _arg1);
                        reply.writeNoException();
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        interfaceSetMtu(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        tetherAddForward(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_tetherRemoveForward /* 88 */:
                        data.enforceInterface(DESCRIPTOR);
                        tetherRemoveForward(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setTcpRWmemorySize /* 89 */:
                        data.enforceInterface(DESCRIPTOR);
                        setTcpRWmemorySize(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_registerUnsolicitedEventListener /* 90 */:
                        data.enforceInterface(DESCRIPTOR);
                        registerUnsolicitedEventListener(INetdUnsolicitedEventListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_firewallAddUidInterfaceRules /* 91 */:
                        data.enforceInterface(DESCRIPTOR);
                        firewallAddUidInterfaceRules(data.readString(), data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_firewallRemoveUidInterfaceRules /* 92 */:
                        data.enforceInterface(DESCRIPTOR);
                        firewallRemoveUidInterfaceRules(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_trafficSwapActiveStatsMap /* 93 */:
                        data.enforceInterface(DESCRIPTOR);
                        trafficSwapActiveStatsMap();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getOemNetd /* 94 */:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result11 = getOemNetd();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result11);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INetd {
            public static INetd sDefaultImpl;
            private int mCachedVersion = -1;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.net.INetd
            public boolean isAlive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAlive();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public boolean firewallReplaceUidChain(String chainName, boolean isWhitelist, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(chainName);
                    boolean _result = true;
                    _data.writeInt(isWhitelist ? 1 : 0);
                    _data.writeIntArray(uids);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().firewallReplaceUidChain(chainName, isWhitelist, uids);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public boolean bandwidthEnableDataSaver(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().bandwidthEnableDataSaver(enable);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkCreatePhysical(int netId, int permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(permission);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkCreatePhysical(netId, permission);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkCreateVpn(int netId, boolean secure) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(secure ? 1 : 0);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkCreateVpn(netId, secure);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkDestroy(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkDestroy(netId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkAddInterface(int netId, String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(iface);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkAddInterface(netId, iface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkRemoveInterface(int netId, String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(iface);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkRemoveInterface(netId, iface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkAddUidRanges(int netId, UidRangeParcel[] uidRanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeTypedArray(uidRanges, 0);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkAddUidRanges(netId, uidRanges);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkRemoveUidRanges(int netId, UidRangeParcel[] uidRanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeTypedArray(uidRanges, 0);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkRemoveUidRanges(netId, uidRanges);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkRejectNonSecureVpn(boolean add, UidRangeParcel[] uidRanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(add ? 1 : 0);
                    _data.writeTypedArray(uidRanges, 0);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkRejectNonSecureVpn(add, uidRanges);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void socketDestroy(UidRangeParcel[] uidRanges, int[] exemptUids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(uidRanges, 0);
                    _data.writeIntArray(exemptUids);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().socketDestroy(uidRanges, exemptUids);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public boolean tetherApplyDnsInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tetherApplyDnsInterfaces();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public TetherStatsParcel[] tetherGetStats() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tetherGetStats();
                    }
                    _reply.readException();
                    TetherStatsParcel[] _result = (TetherStatsParcel[]) _reply.createTypedArray(TetherStatsParcel.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void interfaceAddAddress(String ifName, String addrString, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeString(addrString);
                    _data.writeInt(prefixLength);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().interfaceAddAddress(ifName, addrString, prefixLength);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void interfaceDelAddress(String ifName, String addrString, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeString(addrString);
                    _data.writeInt(prefixLength);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().interfaceDelAddress(ifName, addrString, prefixLength);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public String getProcSysNet(int ipversion, int which, String ifname, String parameter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ipversion);
                    _data.writeInt(which);
                    _data.writeString(ifname);
                    _data.writeString(parameter);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProcSysNet(ipversion, which, ifname, parameter);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void setProcSysNet(int ipversion, int which, String ifname, String parameter, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ipversion);
                    _data.writeInt(which);
                    _data.writeString(ifname);
                    _data.writeString(parameter);
                    _data.writeString(value);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProcSysNet(ipversion, which, ifname, parameter, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void ipSecSetEncapSocketOwner(ParcelFileDescriptor socket, int newUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (socket != null) {
                        _data.writeInt(1);
                        socket.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(newUid);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().ipSecSetEncapSocketOwner(socket, newUid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public int ipSecAllocateSpi(int transformId, String sourceAddress, String destinationAddress, int spi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeString(sourceAddress);
                    _data.writeString(destinationAddress);
                    _data.writeInt(spi);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ipSecAllocateSpi(transformId, sourceAddress, destinationAddress, spi);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void ipSecAddSecurityAssociation(int transformId, int mode, String sourceAddress, String destinationAddress, int underlyingNetId, int spi, int markValue, int markMask, String authAlgo, byte[] authKey, int authTruncBits, String cryptAlgo, byte[] cryptKey, int cryptTruncBits, String aeadAlgo, byte[] aeadKey, int aeadIcvBits, int encapType, int encapLocalPort, int encapRemotePort, int interfaceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeInt(mode);
                    _data.writeString(sourceAddress);
                    _data.writeString(destinationAddress);
                    _data.writeInt(underlyingNetId);
                    _data.writeInt(spi);
                    _data.writeInt(markValue);
                    _data.writeInt(markMask);
                    _data.writeString(authAlgo);
                    _data.writeByteArray(authKey);
                    _data.writeInt(authTruncBits);
                    _data.writeString(cryptAlgo);
                    _data.writeByteArray(cryptKey);
                    _data.writeInt(cryptTruncBits);
                    _data.writeString(aeadAlgo);
                    _data.writeByteArray(aeadKey);
                    _data.writeInt(aeadIcvBits);
                    _data.writeInt(encapType);
                    _data.writeInt(encapLocalPort);
                    _data.writeInt(encapRemotePort);
                    _data.writeInt(interfaceId);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().ipSecAddSecurityAssociation(transformId, mode, sourceAddress, destinationAddress, underlyingNetId, spi, markValue, markMask, authAlgo, authKey, authTruncBits, cryptAlgo, cryptKey, cryptTruncBits, aeadAlgo, aeadKey, aeadIcvBits, encapType, encapLocalPort, encapRemotePort, interfaceId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void ipSecDeleteSecurityAssociation(int transformId, String sourceAddress, String destinationAddress, int spi, int markValue, int markMask, int interfaceId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(transformId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(sourceAddress);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(destinationAddress);
                        try {
                            _data.writeInt(spi);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(markValue);
                            _data.writeInt(markMask);
                            _data.writeInt(interfaceId);
                            if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().ipSecDeleteSecurityAssociation(transformId, sourceAddress, destinationAddress, spi, markValue, markMask, interfaceId);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.INetd
            public void ipSecApplyTransportModeTransform(ParcelFileDescriptor socket, int transformId, int direction, String sourceAddress, String destinationAddress, int spi) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (socket != null) {
                        _data.writeInt(1);
                        socket.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeInt(transformId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(direction);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(sourceAddress);
                        try {
                            _data.writeString(destinationAddress);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(spi);
                            if (this.mRemote.transact(Stub.TRANSACTION_ipSecApplyTransportModeTransform, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().ipSecApplyTransportModeTransform(socket, transformId, direction, sourceAddress, destinationAddress, spi);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.INetd
            public void ipSecRemoveTransportModeTransform(ParcelFileDescriptor socket) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (socket != null) {
                        _data.writeInt(1);
                        socket.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_ipSecRemoveTransportModeTransform, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().ipSecRemoveTransportModeTransform(socket);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void ipSecAddSecurityPolicy(int transformId, int selAddrFamily, int direction, String tmplSrcAddress, String tmplDstAddress, int spi, int markValue, int markMask, int interfaceId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(transformId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(selAddrFamily);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(direction);
                        _data.writeString(tmplSrcAddress);
                        _data.writeString(tmplDstAddress);
                        _data.writeInt(spi);
                        _data.writeInt(markValue);
                        _data.writeInt(markMask);
                        _data.writeInt(interfaceId);
                        if (this.mRemote.transact(Stub.TRANSACTION_ipSecAddSecurityPolicy, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().ipSecAddSecurityPolicy(transformId, selAddrFamily, direction, tmplSrcAddress, tmplDstAddress, spi, markValue, markMask, interfaceId);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.INetd
            public void ipSecUpdateSecurityPolicy(int transformId, int selAddrFamily, int direction, String tmplSrcAddress, String tmplDstAddress, int spi, int markValue, int markMask, int interfaceId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(transformId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(selAddrFamily);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(direction);
                        _data.writeString(tmplSrcAddress);
                        _data.writeString(tmplDstAddress);
                        _data.writeInt(spi);
                        _data.writeInt(markValue);
                        _data.writeInt(markMask);
                        _data.writeInt(interfaceId);
                        if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().ipSecUpdateSecurityPolicy(transformId, selAddrFamily, direction, tmplSrcAddress, tmplDstAddress, spi, markValue, markMask, interfaceId);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.INetd
            public void ipSecDeleteSecurityPolicy(int transformId, int selAddrFamily, int direction, int markValue, int markMask, int interfaceId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(transformId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(selAddrFamily);
                        try {
                            _data.writeInt(direction);
                            try {
                                _data.writeInt(markValue);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(markMask);
                        try {
                            _data.writeInt(interfaceId);
                            if (this.mRemote.transact(Stub.TRANSACTION_ipSecDeleteSecurityPolicy, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().ipSecDeleteSecurityPolicy(transformId, selAddrFamily, direction, markValue, markMask, interfaceId);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.INetd
            public void ipSecAddTunnelInterface(String deviceName, String localAddress, String remoteAddress, int iKey, int oKey, int interfaceId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(deviceName);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(localAddress);
                        try {
                            _data.writeString(remoteAddress);
                            try {
                                _data.writeInt(iKey);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(oKey);
                        try {
                            _data.writeInt(interfaceId);
                            if (this.mRemote.transact(Stub.TRANSACTION_ipSecAddTunnelInterface, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().ipSecAddTunnelInterface(deviceName, localAddress, remoteAddress, iKey, oKey, interfaceId);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.INetd
            public void ipSecUpdateTunnelInterface(String deviceName, String localAddress, String remoteAddress, int iKey, int oKey, int interfaceId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(deviceName);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(localAddress);
                        try {
                            _data.writeString(remoteAddress);
                            try {
                                _data.writeInt(iKey);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(oKey);
                        try {
                            _data.writeInt(interfaceId);
                            if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().ipSecUpdateTunnelInterface(deviceName, localAddress, remoteAddress, iKey, oKey, interfaceId);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.INetd
            public void ipSecRemoveTunnelInterface(String deviceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceName);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().ipSecRemoveTunnelInterface(deviceName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void wakeupAddInterface(String ifName, String prefix, int mark, int mask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeString(prefix);
                    _data.writeInt(mark);
                    _data.writeInt(mask);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().wakeupAddInterface(ifName, prefix, mark, mask);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void wakeupDelInterface(String ifName, String prefix, int mark, int mask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeString(prefix);
                    _data.writeInt(mark);
                    _data.writeInt(mask);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().wakeupDelInterface(ifName, prefix, mark, mask);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void setIPv6AddrGenMode(String ifName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIPv6AddrGenMode(ifName, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void idletimerAddInterface(String ifName, int timeout, String classLabel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(timeout);
                    _data.writeString(classLabel);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().idletimerAddInterface(ifName, timeout, classLabel);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void idletimerRemoveInterface(String ifName, int timeout, String classLabel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(timeout);
                    _data.writeString(classLabel);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().idletimerRemoveInterface(ifName, timeout, classLabel);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void strictUidCleartextPenalty(int uid, int policyPenalty) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(policyPenalty);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().strictUidCleartextPenalty(uid, policyPenalty);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public String clatdStart(String ifName, String nat64Prefix) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeString(nat64Prefix);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clatdStart(ifName, nat64Prefix);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void clatdStop(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(38, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clatdStop(ifName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public boolean ipfwdEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ipfwdEnabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public String[] ipfwdGetRequesterList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ipfwdGetRequesterList();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void ipfwdEnableForwarding(String requester) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(requester);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().ipfwdEnableForwarding(requester);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void ipfwdDisableForwarding(String requester) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(requester);
                    if (this.mRemote.transact(42, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().ipfwdDisableForwarding(requester);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void ipfwdAddInterfaceForward(String fromIface, String toIface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fromIface);
                    _data.writeString(toIface);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().ipfwdAddInterfaceForward(fromIface, toIface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void ipfwdRemoveInterfaceForward(String fromIface, String toIface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fromIface);
                    _data.writeString(toIface);
                    if (this.mRemote.transact(44, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().ipfwdRemoveInterfaceForward(fromIface, toIface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void bandwidthSetInterfaceQuota(String ifName, long bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeLong(bytes);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bandwidthSetInterfaceQuota(ifName, bytes);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void bandwidthRemoveInterfaceQuota(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(Stub.TRANSACTION_bandwidthRemoveInterfaceQuota, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bandwidthRemoveInterfaceQuota(ifName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void bandwidthSetInterfaceAlert(String ifName, long bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeLong(bytes);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bandwidthSetInterfaceAlert(ifName, bytes);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void bandwidthRemoveInterfaceAlert(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bandwidthRemoveInterfaceAlert(ifName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void bandwidthSetGlobalAlert(long bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(bytes);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bandwidthSetGlobalAlert(bytes);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void bandwidthAddNaughtyApp(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bandwidthAddNaughtyApp(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void bandwidthRemoveNaughtyApp(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bandwidthRemoveNaughtyApp(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void bandwidthAddNiceApp(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bandwidthAddNiceApp(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void bandwidthRemoveNiceApp(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().bandwidthRemoveNiceApp(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void tetherStart(String[] dhcpRanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(dhcpRanges);
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tetherStart(dhcpRanges);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void tetherStop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(55, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tetherStop();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public boolean tetherIsEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tetherIsEnabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void tetherInterfaceAdd(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tetherInterfaceAdd(ifName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void tetherInterfaceRemove(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(58, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tetherInterfaceRemove(ifName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public String[] tetherInterfaceList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tetherInterfaceList();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void tetherDnsSet(int netId, String[] dnsAddrs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeStringArray(dnsAddrs);
                    if (this.mRemote.transact(Stub.TRANSACTION_tetherDnsSet, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tetherDnsSet(netId, dnsAddrs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public String[] tetherDnsList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_tetherDnsList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tetherDnsList();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkAddRoute(int netId, String ifName, String destination, String nextHop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(ifName);
                    _data.writeString(destination);
                    _data.writeString(nextHop);
                    if (this.mRemote.transact(Stub.TRANSACTION_networkAddRoute, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkAddRoute(netId, ifName, destination, nextHop);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkRemoveRoute(int netId, String ifName, String destination, String nextHop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(ifName);
                    _data.writeString(destination);
                    _data.writeString(nextHop);
                    if (this.mRemote.transact(Stub.TRANSACTION_networkRemoveRoute, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkRemoveRoute(netId, ifName, destination, nextHop);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkAddLegacyRoute(int netId, String ifName, String destination, String nextHop, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(ifName);
                    _data.writeString(destination);
                    _data.writeString(nextHop);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkAddLegacyRoute(netId, ifName, destination, nextHop, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkRemoveLegacyRoute(int netId, String ifName, String destination, String nextHop, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(ifName);
                    _data.writeString(destination);
                    _data.writeString(nextHop);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkRemoveLegacyRoute(netId, ifName, destination, nextHop, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public int networkGetDefault() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(66, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().networkGetDefault();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkSetDefault(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkSetDefault(netId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkClearDefault() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(68, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkClearDefault();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkSetPermissionForNetwork(int netId, int permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(permission);
                    if (this.mRemote.transact(69, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkSetPermissionForNetwork(netId, permission);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkSetPermissionForUser(int permission, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(permission);
                    _data.writeIntArray(uids);
                    if (this.mRemote.transact(70, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkSetPermissionForUser(permission, uids);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkClearPermissionForUser(int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uids);
                    if (this.mRemote.transact(71, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkClearPermissionForUser(uids);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void trafficSetNetPermForUids(int permission, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(permission);
                    _data.writeIntArray(uids);
                    if (this.mRemote.transact(72, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().trafficSetNetPermForUids(permission, uids);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkSetProtectAllow(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(73, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkSetProtectAllow(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void networkSetProtectDeny(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(74, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().networkSetProtectDeny(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public boolean networkCanProtect(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().networkCanProtect(uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void firewallSetFirewallType(int firewalltype) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(firewalltype);
                    if (this.mRemote.transact(76, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().firewallSetFirewallType(firewalltype);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void firewallSetInterfaceRule(String ifName, int firewallRule) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(firewallRule);
                    if (this.mRemote.transact(77, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().firewallSetInterfaceRule(ifName, firewallRule);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void firewallSetUidRule(int childChain, int uid, int firewallRule) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(childChain);
                    _data.writeInt(uid);
                    _data.writeInt(firewallRule);
                    if (this.mRemote.transact(78, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().firewallSetUidRule(childChain, uid, firewallRule);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void firewallEnableChildChain(int childChain, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(childChain);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(79, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().firewallEnableChildChain(childChain, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public String[] interfaceGetList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(80, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().interfaceGetList();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public InterfaceConfigurationParcel interfaceGetCfg(String ifName) throws RemoteException {
                InterfaceConfigurationParcel _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (!this.mRemote.transact(81, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().interfaceGetCfg(ifName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = InterfaceConfigurationParcel.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void interfaceSetCfg(InterfaceConfigurationParcel cfg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cfg != null) {
                        _data.writeInt(1);
                        cfg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(82, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().interfaceSetCfg(cfg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void interfaceSetIPv6PrivacyExtensions(String ifName, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(83, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().interfaceSetIPv6PrivacyExtensions(ifName, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void interfaceClearAddrs(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(84, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().interfaceClearAddrs(ifName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void interfaceSetEnableIPv6(String ifName, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(85, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().interfaceSetEnableIPv6(ifName, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void interfaceSetMtu(String ifName, int mtu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(mtu);
                    if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().interfaceSetMtu(ifName, mtu);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void tetherAddForward(String intIface, String extIface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(intIface);
                    _data.writeString(extIface);
                    if (this.mRemote.transact(87, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tetherAddForward(intIface, extIface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void tetherRemoveForward(String intIface, String extIface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(intIface);
                    _data.writeString(extIface);
                    if (this.mRemote.transact(Stub.TRANSACTION_tetherRemoveForward, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tetherRemoveForward(intIface, extIface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void setTcpRWmemorySize(String rmemValues, String wmemValues) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rmemValues);
                    _data.writeString(wmemValues);
                    if (this.mRemote.transact(Stub.TRANSACTION_setTcpRWmemorySize, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTcpRWmemorySize(rmemValues, wmemValues);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void registerUnsolicitedEventListener(INetdUnsolicitedEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_registerUnsolicitedEventListener, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerUnsolicitedEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void firewallAddUidInterfaceRules(String ifName, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeIntArray(uids);
                    if (this.mRemote.transact(Stub.TRANSACTION_firewallAddUidInterfaceRules, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().firewallAddUidInterfaceRules(ifName, uids);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void firewallRemoveUidInterfaceRules(int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uids);
                    if (this.mRemote.transact(Stub.TRANSACTION_firewallRemoveUidInterfaceRules, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().firewallRemoveUidInterfaceRules(uids);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public void trafficSwapActiveStatsMap() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_trafficSwapActiveStatsMap, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().trafficSwapActiveStatsMap();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public IBinder getOemNetd() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getOemNetd, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOemNetd();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetd
            public int getInterfaceVersion() throws RemoteException {
                if (this.mCachedVersion == -1) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        data.writeInterfaceToken(Stub.DESCRIPTOR);
                        this.mRemote.transact(Stub.TRANSACTION_getInterfaceVersion, data, reply, 0);
                        reply.readException();
                        this.mCachedVersion = reply.readInt();
                    } finally {
                        reply.recycle();
                        data.recycle();
                    }
                }
                return this.mCachedVersion;
            }
        }

        public static boolean setDefaultImpl(INetd impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetd getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
