package android.net;

import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.net.ISocketKeepaliveCallback;
import android.net.ITetheringEventCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ResultReceiver;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnProfile;
import java.io.FileDescriptor;
import java.util.List;

public interface IConnectivityManager extends IInterface {
    boolean addVpnAddress(String str, int i) throws RemoteException;

    int checkMobileProvisioning(int i) throws RemoteException;

    ParcelFileDescriptor establishVpn(VpnConfig vpnConfig) throws RemoteException;

    void factoryReset() throws RemoteException;

    @UnsupportedAppUsage
    LinkProperties getActiveLinkProperties() throws RemoteException;

    Network getActiveNetwork() throws RemoteException;

    Network getActiveNetworkForUid(int i, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    NetworkInfo getActiveNetworkInfo() throws RemoteException;

    NetworkInfo getActiveNetworkInfoForUid(int i, boolean z) throws RemoteException;

    NetworkQuotaInfo getActiveNetworkQuotaInfo() throws RemoteException;

    @UnsupportedAppUsage
    NetworkInfo[] getAllNetworkInfo() throws RemoteException;

    @UnsupportedAppUsage
    NetworkState[] getAllNetworkState() throws RemoteException;

    Network[] getAllNetworks() throws RemoteException;

    String getAlwaysOnVpnPackage(int i) throws RemoteException;

    String getCaptivePortalServerUrl() throws RemoteException;

    int getConnectionOwnerUid(ConnectionInfo connectionInfo) throws RemoteException;

    NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int i) throws RemoteException;

    NetworkRequest getDefaultRequest() throws RemoteException;

    ProxyInfo getGlobalProxy() throws RemoteException;

    @UnsupportedAppUsage
    int getLastTetherError(String str) throws RemoteException;

    void getLatestTetheringEntitlementResult(int i, ResultReceiver resultReceiver, boolean z, String str) throws RemoteException;

    LegacyVpnInfo getLegacyVpnInfo(int i) throws RemoteException;

    LinkProperties getLinkProperties(Network network) throws RemoteException;

    LinkProperties getLinkPropertiesForType(int i) throws RemoteException;

    String getMobileProvisioningUrl() throws RemoteException;

    int getMultipathPreference(Network network) throws RemoteException;

    NetworkCapabilities getNetworkCapabilities(Network network) throws RemoteException;

    Network getNetworkForType(int i) throws RemoteException;

    NetworkInfo getNetworkInfo(int i) throws RemoteException;

    NetworkInfo getNetworkInfoForUid(Network network, int i, boolean z) throws RemoteException;

    byte[] getNetworkWatchlistConfigHash() throws RemoteException;

    ProxyInfo getProxyForNetwork(Network network) throws RemoteException;

    int getRestoreDefaultNetworkDelay(int i) throws RemoteException;

    String[] getTetherableBluetoothRegexs() throws RemoteException;

    @UnsupportedAppUsage
    String[] getTetherableIfaces() throws RemoteException;

    @UnsupportedAppUsage
    String[] getTetherableUsbRegexs() throws RemoteException;

    @UnsupportedAppUsage
    String[] getTetherableWifiRegexs() throws RemoteException;

    String[] getTetheredDhcpRanges() throws RemoteException;

    @UnsupportedAppUsage
    String[] getTetheredIfaces() throws RemoteException;

    @UnsupportedAppUsage
    String[] getTetheringErroredIfaces() throws RemoteException;

    VpnConfig getVpnConfig(int i) throws RemoteException;

    List<String> getVpnLockdownWhitelist(int i) throws RemoteException;

    boolean isActiveNetworkMetered() throws RemoteException;

    boolean isAlwaysOnVpnPackageSupported(int i, String str) throws RemoteException;

    boolean isCallerCurrentAlwaysOnVpnApp() throws RemoteException;

    boolean isCallerCurrentAlwaysOnVpnLockdownApp() throws RemoteException;

    boolean isNetworkSupported(int i) throws RemoteException;

    boolean isTetheringSupported(String str) throws RemoteException;

    boolean isVpnLockdownEnabled(int i) throws RemoteException;

    NetworkRequest listenForNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, IBinder iBinder) throws RemoteException;

    void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent pendingIntent) throws RemoteException;

    NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent pendingIntent) throws RemoteException;

    boolean prepareVpn(String str, String str2, int i) throws RemoteException;

    int registerNetworkAgent(Messenger messenger, NetworkInfo networkInfo, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int i, NetworkMisc networkMisc, int i2) throws RemoteException;

    int registerNetworkFactory(Messenger messenger, String str) throws RemoteException;

    void registerTetheringEventCallback(ITetheringEventCallback iTetheringEventCallback, String str) throws RemoteException;

    void releaseNetworkRequest(NetworkRequest networkRequest) throws RemoteException;

    void releasePendingNetworkRequest(PendingIntent pendingIntent) throws RemoteException;

    boolean removeVpnAddress(String str, int i) throws RemoteException;

    void reportInetCondition(int i, int i2) throws RemoteException;

    void reportNetworkConnectivity(Network network, boolean z) throws RemoteException;

    boolean requestBandwidthUpdate(Network network) throws RemoteException;

    NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int i, IBinder iBinder, int i2) throws RemoteException;

    boolean requestRouteToHostAddress(int i, byte[] bArr) throws RemoteException;

    void setAcceptPartialConnectivity(Network network, boolean z, boolean z2) throws RemoteException;

    void setAcceptUnvalidated(Network network, boolean z, boolean z2) throws RemoteException;

    void setAirplaneMode(boolean z) throws RemoteException;

    boolean setAlwaysOnVpnPackage(int i, String str, boolean z, List<String> list) throws RemoteException;

    void setAvoidUnvalidated(Network network) throws RemoteException;

    void setGlobalProxy(ProxyInfo proxyInfo) throws RemoteException;

    void setProvisioningNotificationVisible(boolean z, int i, String str) throws RemoteException;

    boolean setUnderlyingNetworksForVpn(Network[] networkArr) throws RemoteException;

    int setUsbTethering(boolean z, String str) throws RemoteException;

    void setVpnPackageAuthorization(String str, int i, boolean z) throws RemoteException;

    boolean shouldAvoidBadWifi() throws RemoteException;

    void startCaptivePortalApp(Network network) throws RemoteException;

    void startCaptivePortalAppInternal(Network network, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    void startLegacyVpn(VpnProfile vpnProfile) throws RemoteException;

    void startNattKeepalive(Network network, int i, ISocketKeepaliveCallback iSocketKeepaliveCallback, String str, int i2, String str2) throws RemoteException;

    void startNattKeepaliveWithFd(Network network, FileDescriptor fileDescriptor, int i, int i2, ISocketKeepaliveCallback iSocketKeepaliveCallback, String str, String str2) throws RemoteException;

    IBinder startOrGetTestNetworkService() throws RemoteException;

    void startTcpKeepalive(Network network, FileDescriptor fileDescriptor, int i, ISocketKeepaliveCallback iSocketKeepaliveCallback) throws RemoteException;

    void startTethering(int i, ResultReceiver resultReceiver, boolean z, String str) throws RemoteException;

    void stopKeepalive(Network network, int i) throws RemoteException;

    void stopTethering(int i, String str) throws RemoteException;

    int tether(String str, String str2) throws RemoteException;

    boolean turnOffVpn(String str, int i) throws RemoteException;

    void unregisterNetworkFactory(Messenger messenger) throws RemoteException;

    void unregisterTetheringEventCallback(ITetheringEventCallback iTetheringEventCallback, String str) throws RemoteException;

    int untether(String str, String str2) throws RemoteException;

    boolean updateLockdownVpn() throws RemoteException;

    public static class Default implements IConnectivityManager {
        @Override // android.net.IConnectivityManager
        public Network getActiveNetwork() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkInfo getActiveNetworkInfo() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkInfo getNetworkInfo(int networkType) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkInfo getNetworkInfoForUid(Network network, int uid, boolean ignoreBlocked) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkInfo[] getAllNetworkInfo() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public Network getNetworkForType(int networkType) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public Network[] getAllNetworks() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public boolean isNetworkSupported(int networkType) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public LinkProperties getActiveLinkProperties() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public LinkProperties getLinkPropertiesForType(int networkType) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public LinkProperties getLinkProperties(Network network) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkCapabilities getNetworkCapabilities(Network network) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkState[] getAllNetworkState() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkQuotaInfo getActiveNetworkQuotaInfo() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public boolean isActiveNetworkMetered() throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public int tether(String iface, String callerPkg) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public int untether(String iface, String callerPkg) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public int getLastTetherError(String iface) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public boolean isTetheringSupported(String callerPkg) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi, String callerPkg) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void stopTethering(int type, String callerPkg) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public String[] getTetherableIfaces() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public String[] getTetheredIfaces() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public String[] getTetheringErroredIfaces() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public String[] getTetheredDhcpRanges() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public String[] getTetherableUsbRegexs() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public String[] getTetherableWifiRegexs() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public String[] getTetherableBluetoothRegexs() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public int setUsbTethering(boolean enable, String callerPkg) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public void reportInetCondition(int networkType, int percentage) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void reportNetworkConnectivity(Network network, boolean hasConnectivity) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public ProxyInfo getGlobalProxy() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public void setGlobalProxy(ProxyInfo p) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public ProxyInfo getProxyForNetwork(Network nework) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public boolean prepareVpn(String oldPackage, String newPackage, int userId) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public void setVpnPackageAuthorization(String packageName, int userId, boolean authorized) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public ParcelFileDescriptor establishVpn(VpnConfig config) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public VpnConfig getVpnConfig(int userId) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public void startLegacyVpn(VpnProfile profile) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public LegacyVpnInfo getLegacyVpnInfo(int userId) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public boolean updateLockdownVpn() throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public boolean isAlwaysOnVpnPackageSupported(int userId, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown, List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public String getAlwaysOnVpnPackage(int userId) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public boolean isVpnLockdownEnabled(int userId) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public List<String> getVpnLockdownWhitelist(int userId) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public int checkMobileProvisioning(int suggestedTimeOutMs) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public String getMobileProvisioningUrl() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void setAirplaneMode(boolean enable) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public int registerNetworkFactory(Messenger messenger, String name) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public boolean requestBandwidthUpdate(Network network) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public void unregisterNetworkFactory(Messenger messenger) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public int registerNetworkAgent(Messenger messenger, NetworkInfo ni, LinkProperties lp, NetworkCapabilities nc, int score, NetworkMisc misc, int factorySerialNumber) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutSec, IBinder binder, int legacy) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public void releasePendingNetworkRequest(PendingIntent operation) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public NetworkRequest listenForNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, IBinder binder) throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void releaseNetworkRequest(NetworkRequest networkRequest) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void setAcceptUnvalidated(Network network, boolean accept, boolean always) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void setAcceptPartialConnectivity(Network network, boolean accept, boolean always) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void setAvoidUnvalidated(Network network) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void startCaptivePortalApp(Network network) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void startCaptivePortalAppInternal(Network network, Bundle appExtras) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public boolean shouldAvoidBadWifi() throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public int getMultipathPreference(Network Network) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public NetworkRequest getDefaultRequest() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public int getRestoreDefaultNetworkDelay(int networkType) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public boolean addVpnAddress(String address, int prefixLength) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public boolean removeVpnAddress(String address, int prefixLength) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public boolean setUnderlyingNetworksForVpn(Network[] networks) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public void factoryReset() throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void startNattKeepalive(Network network, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddr, int srcPort, String dstAddr) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void startNattKeepaliveWithFd(Network network, FileDescriptor fd, int resourceId, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddr, String dstAddr) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void startTcpKeepalive(Network network, FileDescriptor fd, int intervalSeconds, ISocketKeepaliveCallback cb) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void stopKeepalive(Network network, int slot) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public String getCaptivePortalServerUrl() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public byte[] getNetworkWatchlistConfigHash() throws RemoteException {
            return null;
        }

        @Override // android.net.IConnectivityManager
        public int getConnectionOwnerUid(ConnectionInfo connectionInfo) throws RemoteException {
            return 0;
        }

        @Override // android.net.IConnectivityManager
        public boolean isCallerCurrentAlwaysOnVpnApp() throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public boolean isCallerCurrentAlwaysOnVpnLockdownApp() throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public void getLatestTetheringEntitlementResult(int type, ResultReceiver receiver, boolean showEntitlementUi, String callerPkg) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public boolean turnOffVpn(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.net.IConnectivityManager
        public void registerTetheringEventCallback(ITetheringEventCallback callback, String callerPkg) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public void unregisterTetheringEventCallback(ITetheringEventCallback callback, String callerPkg) throws RemoteException {
        }

        @Override // android.net.IConnectivityManager
        public IBinder startOrGetTestNetworkService() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IConnectivityManager {
        private static final String DESCRIPTOR = "android.net.IConnectivityManager";
        static final int TRANSACTION_addVpnAddress = 74;
        static final int TRANSACTION_checkMobileProvisioning = 51;
        static final int TRANSACTION_establishVpn = 41;
        static final int TRANSACTION_factoryReset = 77;
        static final int TRANSACTION_getActiveLinkProperties = 12;
        static final int TRANSACTION_getActiveNetwork = 1;
        static final int TRANSACTION_getActiveNetworkForUid = 2;
        static final int TRANSACTION_getActiveNetworkInfo = 3;
        static final int TRANSACTION_getActiveNetworkInfoForUid = 4;
        static final int TRANSACTION_getActiveNetworkQuotaInfo = 17;
        static final int TRANSACTION_getAllNetworkInfo = 7;
        static final int TRANSACTION_getAllNetworkState = 16;
        static final int TRANSACTION_getAllNetworks = 9;
        static final int TRANSACTION_getAlwaysOnVpnPackage = 48;
        static final int TRANSACTION_getCaptivePortalServerUrl = 82;
        static final int TRANSACTION_getConnectionOwnerUid = 84;
        static final int TRANSACTION_getDefaultNetworkCapabilitiesForUser = 10;
        static final int TRANSACTION_getDefaultRequest = 72;
        static final int TRANSACTION_getGlobalProxy = 36;
        static final int TRANSACTION_getLastTetherError = 22;
        static final int TRANSACTION_getLatestTetheringEntitlementResult = 87;
        static final int TRANSACTION_getLegacyVpnInfo = 44;
        static final int TRANSACTION_getLinkProperties = 14;
        static final int TRANSACTION_getLinkPropertiesForType = 13;
        static final int TRANSACTION_getMobileProvisioningUrl = 52;
        static final int TRANSACTION_getMultipathPreference = 71;
        static final int TRANSACTION_getNetworkCapabilities = 15;
        static final int TRANSACTION_getNetworkForType = 8;
        static final int TRANSACTION_getNetworkInfo = 5;
        static final int TRANSACTION_getNetworkInfoForUid = 6;
        static final int TRANSACTION_getNetworkWatchlistConfigHash = 83;
        static final int TRANSACTION_getProxyForNetwork = 38;
        static final int TRANSACTION_getRestoreDefaultNetworkDelay = 73;
        static final int TRANSACTION_getTetherableBluetoothRegexs = 32;
        static final int TRANSACTION_getTetherableIfaces = 26;
        static final int TRANSACTION_getTetherableUsbRegexs = 30;
        static final int TRANSACTION_getTetherableWifiRegexs = 31;
        static final int TRANSACTION_getTetheredDhcpRanges = 29;
        static final int TRANSACTION_getTetheredIfaces = 27;
        static final int TRANSACTION_getTetheringErroredIfaces = 28;
        static final int TRANSACTION_getVpnConfig = 42;
        static final int TRANSACTION_getVpnLockdownWhitelist = 50;
        static final int TRANSACTION_isActiveNetworkMetered = 18;
        static final int TRANSACTION_isAlwaysOnVpnPackageSupported = 46;
        static final int TRANSACTION_isCallerCurrentAlwaysOnVpnApp = 85;
        static final int TRANSACTION_isCallerCurrentAlwaysOnVpnLockdownApp = 86;
        static final int TRANSACTION_isNetworkSupported = 11;
        static final int TRANSACTION_isTetheringSupported = 23;
        static final int TRANSACTION_isVpnLockdownEnabled = 49;
        static final int TRANSACTION_listenForNetwork = 62;
        static final int TRANSACTION_pendingListenForNetwork = 63;
        static final int TRANSACTION_pendingRequestForNetwork = 60;
        static final int TRANSACTION_prepareVpn = 39;
        static final int TRANSACTION_registerNetworkAgent = 58;
        static final int TRANSACTION_registerNetworkFactory = 55;
        static final int TRANSACTION_registerTetheringEventCallback = 89;
        static final int TRANSACTION_releaseNetworkRequest = 64;
        static final int TRANSACTION_releasePendingNetworkRequest = 61;
        static final int TRANSACTION_removeVpnAddress = 75;
        static final int TRANSACTION_reportInetCondition = 34;
        static final int TRANSACTION_reportNetworkConnectivity = 35;
        static final int TRANSACTION_requestBandwidthUpdate = 56;
        static final int TRANSACTION_requestNetwork = 59;
        static final int TRANSACTION_requestRouteToHostAddress = 19;
        static final int TRANSACTION_setAcceptPartialConnectivity = 66;
        static final int TRANSACTION_setAcceptUnvalidated = 65;
        static final int TRANSACTION_setAirplaneMode = 54;
        static final int TRANSACTION_setAlwaysOnVpnPackage = 47;
        static final int TRANSACTION_setAvoidUnvalidated = 67;
        static final int TRANSACTION_setGlobalProxy = 37;
        static final int TRANSACTION_setProvisioningNotificationVisible = 53;
        static final int TRANSACTION_setUnderlyingNetworksForVpn = 76;
        static final int TRANSACTION_setUsbTethering = 33;
        static final int TRANSACTION_setVpnPackageAuthorization = 40;
        static final int TRANSACTION_shouldAvoidBadWifi = 70;
        static final int TRANSACTION_startCaptivePortalApp = 68;
        static final int TRANSACTION_startCaptivePortalAppInternal = 69;
        static final int TRANSACTION_startLegacyVpn = 43;
        static final int TRANSACTION_startNattKeepalive = 78;
        static final int TRANSACTION_startNattKeepaliveWithFd = 79;
        static final int TRANSACTION_startOrGetTestNetworkService = 91;
        static final int TRANSACTION_startTcpKeepalive = 80;
        static final int TRANSACTION_startTethering = 24;
        static final int TRANSACTION_stopKeepalive = 81;
        static final int TRANSACTION_stopTethering = 25;
        static final int TRANSACTION_tether = 20;
        static final int TRANSACTION_turnOffVpn = 88;
        static final int TRANSACTION_unregisterNetworkFactory = 57;
        static final int TRANSACTION_unregisterTetheringEventCallback = 90;
        static final int TRANSACTION_untether = 21;
        static final int TRANSACTION_updateLockdownVpn = 45;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConnectivityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectivityManager)) {
                return new Proxy(obj);
            }
            return (IConnectivityManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getActiveNetwork";
                case 2:
                    return "getActiveNetworkForUid";
                case 3:
                    return "getActiveNetworkInfo";
                case 4:
                    return "getActiveNetworkInfoForUid";
                case 5:
                    return "getNetworkInfo";
                case 6:
                    return "getNetworkInfoForUid";
                case 7:
                    return "getAllNetworkInfo";
                case 8:
                    return "getNetworkForType";
                case 9:
                    return "getAllNetworks";
                case 10:
                    return "getDefaultNetworkCapabilitiesForUser";
                case 11:
                    return "isNetworkSupported";
                case 12:
                    return "getActiveLinkProperties";
                case 13:
                    return "getLinkPropertiesForType";
                case 14:
                    return "getLinkProperties";
                case 15:
                    return "getNetworkCapabilities";
                case 16:
                    return "getAllNetworkState";
                case 17:
                    return "getActiveNetworkQuotaInfo";
                case 18:
                    return "isActiveNetworkMetered";
                case 19:
                    return "requestRouteToHostAddress";
                case 20:
                    return "tether";
                case 21:
                    return "untether";
                case 22:
                    return "getLastTetherError";
                case 23:
                    return "isTetheringSupported";
                case 24:
                    return "startTethering";
                case 25:
                    return "stopTethering";
                case 26:
                    return "getTetherableIfaces";
                case 27:
                    return "getTetheredIfaces";
                case 28:
                    return "getTetheringErroredIfaces";
                case 29:
                    return "getTetheredDhcpRanges";
                case 30:
                    return "getTetherableUsbRegexs";
                case 31:
                    return "getTetherableWifiRegexs";
                case 32:
                    return "getTetherableBluetoothRegexs";
                case 33:
                    return "setUsbTethering";
                case 34:
                    return "reportInetCondition";
                case 35:
                    return "reportNetworkConnectivity";
                case 36:
                    return "getGlobalProxy";
                case 37:
                    return "setGlobalProxy";
                case 38:
                    return "getProxyForNetwork";
                case 39:
                    return "prepareVpn";
                case 40:
                    return "setVpnPackageAuthorization";
                case 41:
                    return "establishVpn";
                case 42:
                    return "getVpnConfig";
                case 43:
                    return "startLegacyVpn";
                case 44:
                    return "getLegacyVpnInfo";
                case 45:
                    return "updateLockdownVpn";
                case 46:
                    return "isAlwaysOnVpnPackageSupported";
                case 47:
                    return "setAlwaysOnVpnPackage";
                case 48:
                    return "getAlwaysOnVpnPackage";
                case 49:
                    return "isVpnLockdownEnabled";
                case 50:
                    return "getVpnLockdownWhitelist";
                case 51:
                    return "checkMobileProvisioning";
                case 52:
                    return "getMobileProvisioningUrl";
                case 53:
                    return "setProvisioningNotificationVisible";
                case 54:
                    return "setAirplaneMode";
                case 55:
                    return "registerNetworkFactory";
                case 56:
                    return "requestBandwidthUpdate";
                case 57:
                    return "unregisterNetworkFactory";
                case 58:
                    return "registerNetworkAgent";
                case 59:
                    return "requestNetwork";
                case 60:
                    return "pendingRequestForNetwork";
                case 61:
                    return "releasePendingNetworkRequest";
                case 62:
                    return "listenForNetwork";
                case 63:
                    return "pendingListenForNetwork";
                case 64:
                    return "releaseNetworkRequest";
                case 65:
                    return "setAcceptUnvalidated";
                case 66:
                    return "setAcceptPartialConnectivity";
                case 67:
                    return "setAvoidUnvalidated";
                case 68:
                    return "startCaptivePortalApp";
                case 69:
                    return "startCaptivePortalAppInternal";
                case 70:
                    return "shouldAvoidBadWifi";
                case 71:
                    return "getMultipathPreference";
                case 72:
                    return "getDefaultRequest";
                case 73:
                    return "getRestoreDefaultNetworkDelay";
                case 74:
                    return "addVpnAddress";
                case 75:
                    return "removeVpnAddress";
                case 76:
                    return "setUnderlyingNetworksForVpn";
                case 77:
                    return "factoryReset";
                case 78:
                    return "startNattKeepalive";
                case 79:
                    return "startNattKeepaliveWithFd";
                case 80:
                    return "startTcpKeepalive";
                case 81:
                    return "stopKeepalive";
                case 82:
                    return "getCaptivePortalServerUrl";
                case 83:
                    return "getNetworkWatchlistConfigHash";
                case 84:
                    return "getConnectionOwnerUid";
                case 85:
                    return "isCallerCurrentAlwaysOnVpnApp";
                case 86:
                    return "isCallerCurrentAlwaysOnVpnLockdownApp";
                case 87:
                    return "getLatestTetheringEntitlementResult";
                case 88:
                    return "turnOffVpn";
                case 89:
                    return "registerTetheringEventCallback";
                case 90:
                    return "unregisterTetheringEventCallback";
                case 91:
                    return "startOrGetTestNetworkService";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Network _arg0;
            Network _arg02;
            Network _arg03;
            ResultReceiver _arg1;
            Network _arg04;
            ProxyInfo _arg05;
            Network _arg06;
            VpnConfig _arg07;
            VpnProfile _arg08;
            Messenger _arg09;
            Network _arg010;
            Messenger _arg011;
            Messenger _arg012;
            NetworkInfo _arg12;
            LinkProperties _arg2;
            NetworkCapabilities _arg3;
            NetworkMisc _arg5;
            NetworkCapabilities _arg013;
            Messenger _arg13;
            NetworkCapabilities _arg014;
            PendingIntent _arg14;
            PendingIntent _arg015;
            NetworkCapabilities _arg016;
            Messenger _arg15;
            NetworkCapabilities _arg017;
            PendingIntent _arg16;
            NetworkRequest _arg018;
            Network _arg019;
            Network _arg020;
            Network _arg021;
            Network _arg022;
            Network _arg023;
            Bundle _arg17;
            Network _arg024;
            Network _arg025;
            Network _arg026;
            Network _arg027;
            Network _arg028;
            ConnectionInfo _arg029;
            ResultReceiver _arg18;
            if (code != 1598968902) {
                boolean _arg22 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        Network _result = getActiveNetwork();
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        Network _result2 = getActiveNetworkForUid(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkInfo _result3 = getActiveNetworkInfo();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkInfo _result4 = getActiveNetworkInfoForUid(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkInfo _result5 = getNetworkInfo(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        NetworkInfo _result6 = getNetworkInfoForUid(_arg0, data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkInfo[] _result7 = getAllNetworkInfo();
                        reply.writeNoException();
                        reply.writeTypedArray(_result7, 1);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        Network _result8 = getNetworkForType(data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        Network[] _result9 = getAllNetworks();
                        reply.writeNoException();
                        reply.writeTypedArray(_result9, 1);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkCapabilities[] _result10 = getDefaultNetworkCapabilitiesForUser(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedArray(_result10, 1);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNetworkSupported = isNetworkSupported(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isNetworkSupported ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        LinkProperties _result11 = getActiveLinkProperties();
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        LinkProperties _result12 = getLinkPropertiesForType(data.readInt());
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        LinkProperties _result13 = getLinkProperties(_arg02);
                        reply.writeNoException();
                        if (_result13 != null) {
                            reply.writeInt(1);
                            _result13.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        NetworkCapabilities _result14 = getNetworkCapabilities(_arg03);
                        reply.writeNoException();
                        if (_result14 != null) {
                            reply.writeInt(1);
                            _result14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkState[] _result15 = getAllNetworkState();
                        reply.writeNoException();
                        reply.writeTypedArray(_result15, 1);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkQuotaInfo _result16 = getActiveNetworkQuotaInfo();
                        reply.writeNoException();
                        if (_result16 != null) {
                            reply.writeInt(1);
                            _result16.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isActiveNetworkMetered = isActiveNetworkMetered();
                        reply.writeNoException();
                        reply.writeInt(isActiveNetworkMetered ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean requestRouteToHostAddress = requestRouteToHostAddress(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(requestRouteToHostAddress ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = tether(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = untether(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = getLastTetherError(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTetheringSupported = isTetheringSupported(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isTetheringSupported ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg030 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        startTethering(_arg030, _arg1, _arg22, data.readString());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        stopTethering(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result20 = getTetherableIfaces();
                        reply.writeNoException();
                        reply.writeStringArray(_result20);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result21 = getTetheredIfaces();
                        reply.writeNoException();
                        reply.writeStringArray(_result21);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result22 = getTetheringErroredIfaces();
                        reply.writeNoException();
                        reply.writeStringArray(_result22);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result23 = getTetheredDhcpRanges();
                        reply.writeNoException();
                        reply.writeStringArray(_result23);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result24 = getTetherableUsbRegexs();
                        reply.writeNoException();
                        reply.writeStringArray(_result24);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result25 = getTetherableWifiRegexs();
                        reply.writeNoException();
                        reply.writeStringArray(_result25);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result26 = getTetherableBluetoothRegexs();
                        reply.writeNoException();
                        reply.writeStringArray(_result26);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        int _result27 = setUsbTethering(_arg22, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        reportInetCondition(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        reportNetworkConnectivity(_arg04, _arg22);
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        ProxyInfo _result28 = getGlobalProxy();
                        reply.writeNoException();
                        if (_result28 != null) {
                            reply.writeInt(1);
                            _result28.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ProxyInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        setGlobalProxy(_arg05);
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        ProxyInfo _result29 = getProxyForNetwork(_arg06);
                        reply.writeNoException();
                        if (_result29 != null) {
                            reply.writeInt(1);
                            _result29.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        boolean prepareVpn = prepareVpn(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(prepareVpn ? 1 : 0);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg031 = data.readString();
                        int _arg19 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setVpnPackageAuthorization(_arg031, _arg19, _arg22);
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = VpnConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        ParcelFileDescriptor _result30 = establishVpn(_arg07);
                        reply.writeNoException();
                        if (_result30 != null) {
                            reply.writeInt(1);
                            _result30.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        VpnConfig _result31 = getVpnConfig(data.readInt());
                        reply.writeNoException();
                        if (_result31 != null) {
                            reply.writeInt(1);
                            _result31.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = VpnProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        startLegacyVpn(_arg08);
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        LegacyVpnInfo _result32 = getLegacyVpnInfo(data.readInt());
                        reply.writeNoException();
                        if (_result32 != null) {
                            reply.writeInt(1);
                            _result32.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateLockdownVpn = updateLockdownVpn();
                        reply.writeNoException();
                        reply.writeInt(updateLockdownVpn ? 1 : 0);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAlwaysOnVpnPackageSupported = isAlwaysOnVpnPackageSupported(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isAlwaysOnVpnPackageSupported ? 1 : 0);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg032 = data.readInt();
                        String _arg110 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        boolean alwaysOnVpnPackage = setAlwaysOnVpnPackage(_arg032, _arg110, _arg22, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(alwaysOnVpnPackage ? 1 : 0);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        String _result33 = getAlwaysOnVpnPackage(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result33);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVpnLockdownEnabled = isVpnLockdownEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVpnLockdownEnabled ? 1 : 0);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result34 = getVpnLockdownWhitelist(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result34);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        int _result35 = checkMobileProvisioning(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result35);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        String _result36 = getMobileProvisioningUrl();
                        reply.writeNoException();
                        reply.writeString(_result36);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setProvisioningNotificationVisible(_arg22, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setAirplaneMode(_arg22);
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        int _result37 = registerNetworkFactory(_arg09, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result37);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        boolean requestBandwidthUpdate = requestBandwidthUpdate(_arg010);
                        reply.writeNoException();
                        reply.writeInt(requestBandwidthUpdate ? 1 : 0);
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg011 = null;
                        }
                        unregisterNetworkFactory(_arg011);
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg012 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = NetworkInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = LinkProperties.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = NetworkCapabilities.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _arg4 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = NetworkMisc.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        int _result38 = registerNetworkAgent(_arg012, _arg12, _arg2, _arg3, _arg4, _arg5, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result38);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = NetworkCapabilities.CREATOR.createFromParcel(data);
                        } else {
                            _arg013 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        NetworkRequest _result39 = requestNetwork(_arg013, _arg13, data.readInt(), data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        if (_result39 != null) {
                            reply.writeInt(1);
                            _result39.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = NetworkCapabilities.CREATOR.createFromParcel(data);
                        } else {
                            _arg014 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        NetworkRequest _result40 = pendingRequestForNetwork(_arg014, _arg14);
                        reply.writeNoException();
                        if (_result40 != null) {
                            reply.writeInt(1);
                            _result40.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg015 = null;
                        }
                        releasePendingNetworkRequest(_arg015);
                        reply.writeNoException();
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = NetworkCapabilities.CREATOR.createFromParcel(data);
                        } else {
                            _arg016 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg15 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        NetworkRequest _result41 = listenForNetwork(_arg016, _arg15, data.readStrongBinder());
                        reply.writeNoException();
                        if (_result41 != null) {
                            reply.writeInt(1);
                            _result41.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = NetworkCapabilities.CREATOR.createFromParcel(data);
                        } else {
                            _arg017 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg16 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        pendingListenForNetwork(_arg017, _arg16);
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = NetworkRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg018 = null;
                        }
                        releaseNetworkRequest(_arg018);
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg019 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg019 = null;
                        }
                        boolean _arg111 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setAcceptUnvalidated(_arg019, _arg111, _arg22);
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg020 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg020 = null;
                        }
                        boolean _arg112 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setAcceptPartialConnectivity(_arg020, _arg112, _arg22);
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg021 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg021 = null;
                        }
                        setAvoidUnvalidated(_arg021);
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg022 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg022 = null;
                        }
                        startCaptivePortalApp(_arg022);
                        reply.writeNoException();
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg023 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg023 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg17 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        startCaptivePortalAppInternal(_arg023, _arg17);
                        reply.writeNoException();
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        boolean shouldAvoidBadWifi = shouldAvoidBadWifi();
                        reply.writeNoException();
                        reply.writeInt(shouldAvoidBadWifi ? 1 : 0);
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg024 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg024 = null;
                        }
                        int _result42 = getMultipathPreference(_arg024);
                        reply.writeNoException();
                        reply.writeInt(_result42);
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkRequest _result43 = getDefaultRequest();
                        reply.writeNoException();
                        if (_result43 != null) {
                            reply.writeInt(1);
                            _result43.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        int _result44 = getRestoreDefaultNetworkDelay(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result44);
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addVpnAddress = addVpnAddress(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(addVpnAddress ? 1 : 0);
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeVpnAddress = removeVpnAddress(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeVpnAddress ? 1 : 0);
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        boolean underlyingNetworksForVpn = setUnderlyingNetworksForVpn((Network[]) data.createTypedArray(Network.CREATOR));
                        reply.writeNoException();
                        reply.writeInt(underlyingNetworksForVpn ? 1 : 0);
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        factoryReset();
                        reply.writeNoException();
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg025 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg025 = null;
                        }
                        startNattKeepalive(_arg025, data.readInt(), ISocketKeepaliveCallback.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg026 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg026 = null;
                        }
                        startNattKeepaliveWithFd(_arg026, data.readRawFileDescriptor(), data.readInt(), data.readInt(), ISocketKeepaliveCallback.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg027 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg027 = null;
                        }
                        startTcpKeepalive(_arg027, data.readRawFileDescriptor(), data.readInt(), ISocketKeepaliveCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg028 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg028 = null;
                        }
                        stopKeepalive(_arg028, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        String _result45 = getCaptivePortalServerUrl();
                        reply.writeNoException();
                        reply.writeString(_result45);
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result46 = getNetworkWatchlistConfigHash();
                        reply.writeNoException();
                        reply.writeByteArray(_result46);
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg029 = ConnectionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg029 = null;
                        }
                        int _result47 = getConnectionOwnerUid(_arg029);
                        reply.writeNoException();
                        reply.writeInt(_result47);
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCallerCurrentAlwaysOnVpnApp = isCallerCurrentAlwaysOnVpnApp();
                        reply.writeNoException();
                        reply.writeInt(isCallerCurrentAlwaysOnVpnApp ? 1 : 0);
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCallerCurrentAlwaysOnVpnLockdownApp = isCallerCurrentAlwaysOnVpnLockdownApp();
                        reply.writeNoException();
                        reply.writeInt(isCallerCurrentAlwaysOnVpnLockdownApp ? 1 : 0);
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg033 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg18 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        getLatestTetheringEntitlementResult(_arg033, _arg18, _arg22, data.readString());
                        reply.writeNoException();
                        return true;
                    case 88:
                        data.enforceInterface(DESCRIPTOR);
                        boolean turnOffVpn = turnOffVpn(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(turnOffVpn ? 1 : 0);
                        return true;
                    case 89:
                        data.enforceInterface(DESCRIPTOR);
                        registerTetheringEventCallback(ITetheringEventCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 90:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterTetheringEventCallback(ITetheringEventCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 91:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result48 = startOrGetTestNetworkService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result48);
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
        public static class Proxy implements IConnectivityManager {
            public static IConnectivityManager sDefaultImpl;
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

            @Override // android.net.IConnectivityManager
            public Network getActiveNetwork() throws RemoteException {
                Network _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveNetwork();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Network.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) throws RemoteException {
                Network _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(ignoreBlocked ? 1 : 0);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveNetworkForUid(uid, ignoreBlocked);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Network.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public NetworkInfo getActiveNetworkInfo() throws RemoteException {
                NetworkInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveNetworkInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) throws RemoteException {
                NetworkInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(ignoreBlocked ? 1 : 0);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveNetworkInfoForUid(uid, ignoreBlocked);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public NetworkInfo getNetworkInfo(int networkType) throws RemoteException {
                NetworkInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkInfo(networkType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public NetworkInfo getNetworkInfoForUid(Network network, int uid, boolean ignoreBlocked) throws RemoteException {
                NetworkInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    if (!ignoreBlocked) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkInfoForUid(network, uid, ignoreBlocked);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public NetworkInfo[] getAllNetworkInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllNetworkInfo();
                    }
                    _reply.readException();
                    NetworkInfo[] _result = (NetworkInfo[]) _reply.createTypedArray(NetworkInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public Network getNetworkForType(int networkType) throws RemoteException {
                Network _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkForType(networkType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Network.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public Network[] getAllNetworks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllNetworks();
                    }
                    _reply.readException();
                    Network[] _result = (Network[]) _reply.createTypedArray(Network.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultNetworkCapabilitiesForUser(userId);
                    }
                    _reply.readException();
                    NetworkCapabilities[] _result = (NetworkCapabilities[]) _reply.createTypedArray(NetworkCapabilities.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public boolean isNetworkSupported(int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNetworkSupported(networkType);
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

            @Override // android.net.IConnectivityManager
            public LinkProperties getActiveLinkProperties() throws RemoteException {
                LinkProperties _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveLinkProperties();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LinkProperties.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public LinkProperties getLinkPropertiesForType(int networkType) throws RemoteException {
                LinkProperties _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLinkPropertiesForType(networkType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LinkProperties.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public LinkProperties getLinkProperties(Network network) throws RemoteException {
                LinkProperties _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLinkProperties(network);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LinkProperties.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public NetworkCapabilities getNetworkCapabilities(Network network) throws RemoteException {
                NetworkCapabilities _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkCapabilities(network);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkCapabilities.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public NetworkState[] getAllNetworkState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllNetworkState();
                    }
                    _reply.readException();
                    NetworkState[] _result = (NetworkState[]) _reply.createTypedArray(NetworkState.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public NetworkQuotaInfo getActiveNetworkQuotaInfo() throws RemoteException {
                NetworkQuotaInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveNetworkQuotaInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkQuotaInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public boolean isActiveNetworkMetered() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isActiveNetworkMetered();
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

            @Override // android.net.IConnectivityManager
            public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeByteArray(hostAddress);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestRouteToHostAddress(networkType, hostAddress);
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

            @Override // android.net.IConnectivityManager
            public int tether(String iface, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeString(callerPkg);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tether(iface, callerPkg);
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

            @Override // android.net.IConnectivityManager
            public int untether(String iface, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeString(callerPkg);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().untether(iface, callerPkg);
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

            @Override // android.net.IConnectivityManager
            public int getLastTetherError(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastTetherError(iface);
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

            @Override // android.net.IConnectivityManager
            public boolean isTetheringSupported(String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTetheringSupported(callerPkg);
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

            @Override // android.net.IConnectivityManager
            public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    int i = 1;
                    if (receiver != null) {
                        _data.writeInt(1);
                        receiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!showProvisioningUi) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(callerPkg);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startTethering(type, receiver, showProvisioningUi, callerPkg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void stopTethering(int type, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(callerPkg);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopTethering(type, callerPkg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public String[] getTetherableIfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTetherableIfaces();
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

            @Override // android.net.IConnectivityManager
            public String[] getTetheredIfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTetheredIfaces();
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

            @Override // android.net.IConnectivityManager
            public String[] getTetheringErroredIfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTetheringErroredIfaces();
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

            @Override // android.net.IConnectivityManager
            public String[] getTetheredDhcpRanges() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTetheredDhcpRanges();
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

            @Override // android.net.IConnectivityManager
            public String[] getTetherableUsbRegexs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTetherableUsbRegexs();
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

            @Override // android.net.IConnectivityManager
            public String[] getTetherableWifiRegexs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTetherableWifiRegexs();
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

            @Override // android.net.IConnectivityManager
            public String[] getTetherableBluetoothRegexs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTetherableBluetoothRegexs();
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

            @Override // android.net.IConnectivityManager
            public int setUsbTethering(boolean enable, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeString(callerPkg);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setUsbTethering(enable, callerPkg);
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

            @Override // android.net.IConnectivityManager
            public void reportInetCondition(int networkType, int percentage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeInt(percentage);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportInetCondition(networkType, percentage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void reportNetworkConnectivity(Network network, boolean hasConnectivity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!hasConnectivity) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportNetworkConnectivity(network, hasConnectivity);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public ProxyInfo getGlobalProxy() throws RemoteException {
                ProxyInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGlobalProxy();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProxyInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public void setGlobalProxy(ProxyInfo p) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (p != null) {
                        _data.writeInt(1);
                        p.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGlobalProxy(p);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public ProxyInfo getProxyForNetwork(Network nework) throws RemoteException {
                ProxyInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (nework != null) {
                        _data.writeInt(1);
                        nework.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProxyForNetwork(nework);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProxyInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public boolean prepareVpn(String oldPackage, String newPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(oldPackage);
                    _data.writeString(newPackage);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().prepareVpn(oldPackage, newPackage, userId);
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

            @Override // android.net.IConnectivityManager
            public void setVpnPackageAuthorization(String packageName, int userId, boolean authorized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(authorized ? 1 : 0);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVpnPackageAuthorization(packageName, userId, authorized);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public ParcelFileDescriptor establishVpn(VpnConfig config) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().establishVpn(config);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public VpnConfig getVpnConfig(int userId) throws RemoteException {
                VpnConfig _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVpnConfig(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VpnConfig.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public void startLegacyVpn(VpnProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startLegacyVpn(profile);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public LegacyVpnInfo getLegacyVpnInfo(int userId) throws RemoteException {
                LegacyVpnInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLegacyVpnInfo(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LegacyVpnInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public boolean updateLockdownVpn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateLockdownVpn();
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

            @Override // android.net.IConnectivityManager
            public boolean isAlwaysOnVpnPackageSupported(int userId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAlwaysOnVpnPackageSupported(userId, packageName);
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

            @Override // android.net.IConnectivityManager
            public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown, List<String> lockdownWhitelist) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(lockdown ? 1 : 0);
                    _data.writeStringList(lockdownWhitelist);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAlwaysOnVpnPackage(userId, packageName, lockdown, lockdownWhitelist);
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

            @Override // android.net.IConnectivityManager
            public String getAlwaysOnVpnPackage(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAlwaysOnVpnPackage(userId);
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

            @Override // android.net.IConnectivityManager
            public boolean isVpnLockdownEnabled(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVpnLockdownEnabled(userId);
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

            @Override // android.net.IConnectivityManager
            public List<String> getVpnLockdownWhitelist(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVpnLockdownWhitelist(userId);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public int checkMobileProvisioning(int suggestedTimeOutMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(suggestedTimeOutMs);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkMobileProvisioning(suggestedTimeOutMs);
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

            @Override // android.net.IConnectivityManager
            public String getMobileProvisioningUrl() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMobileProvisioningUrl();
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

            @Override // android.net.IConnectivityManager
            public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(visible ? 1 : 0);
                    _data.writeInt(networkType);
                    _data.writeString(action);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProvisioningNotificationVisible(visible, networkType, action);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void setAirplaneMode(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAirplaneMode(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public int registerNetworkFactory(Messenger messenger, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(name);
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerNetworkFactory(messenger, name);
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

            @Override // android.net.IConnectivityManager
            public boolean requestBandwidthUpdate(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestBandwidthUpdate(network);
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

            @Override // android.net.IConnectivityManager
            public void unregisterNetworkFactory(Messenger messenger) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterNetworkFactory(messenger);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public int registerNetworkAgent(Messenger messenger, NetworkInfo ni, LinkProperties lp, NetworkCapabilities nc, int score, NetworkMisc misc, int factorySerialNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (ni != null) {
                        _data.writeInt(1);
                        ni.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (lp != null) {
                        _data.writeInt(1);
                        lp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (nc != null) {
                        _data.writeInt(1);
                        nc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(score);
                    if (misc != null) {
                        _data.writeInt(1);
                        misc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(factorySerialNumber);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerNetworkAgent(messenger, ni, lp, nc, score, misc, factorySerialNumber);
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

            @Override // android.net.IConnectivityManager
            public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutSec, IBinder binder, int legacy) throws RemoteException {
                NetworkRequest _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(1);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeoutSec);
                    _data.writeStrongBinder(binder);
                    _data.writeInt(legacy);
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestNetwork(networkCapabilities, messenger, timeoutSec, binder, legacy);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkRequest.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) throws RemoteException {
                NetworkRequest _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(1);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operation != null) {
                        _data.writeInt(1);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().pendingRequestForNetwork(networkCapabilities, operation);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkRequest.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public void releasePendingNetworkRequest(PendingIntent operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (operation != null) {
                        _data.writeInt(1);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(61, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releasePendingNetworkRequest(operation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public NetworkRequest listenForNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, IBinder binder) throws RemoteException {
                NetworkRequest _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(1);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().listenForNetwork(networkCapabilities, messenger, binder);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkRequest.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(1);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operation != null) {
                        _data.writeInt(1);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(63, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pendingListenForNetwork(networkCapabilities, operation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void releaseNetworkRequest(NetworkRequest networkRequest) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkRequest != null) {
                        _data.writeInt(1);
                        networkRequest.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releaseNetworkRequest(networkRequest);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void setAcceptUnvalidated(Network network, boolean accept, boolean always) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(accept ? 1 : 0);
                    if (!always) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAcceptUnvalidated(network, accept, always);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void setAcceptPartialConnectivity(Network network, boolean accept, boolean always) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(accept ? 1 : 0);
                    if (!always) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAcceptPartialConnectivity(network, accept, always);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void setAvoidUnvalidated(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAvoidUnvalidated(network);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void startCaptivePortalApp(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(68, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startCaptivePortalApp(network);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void startCaptivePortalAppInternal(Network network, Bundle appExtras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (appExtras != null) {
                        _data.writeInt(1);
                        appExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(69, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startCaptivePortalAppInternal(network, appExtras);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public boolean shouldAvoidBadWifi() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(70, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldAvoidBadWifi();
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

            @Override // android.net.IConnectivityManager
            public int getMultipathPreference(Network Network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (Network != null) {
                        _data.writeInt(1);
                        Network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(71, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMultipathPreference(Network);
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

            @Override // android.net.IConnectivityManager
            public NetworkRequest getDefaultRequest() throws RemoteException {
                NetworkRequest _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(72, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultRequest();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkRequest.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IConnectivityManager
            public int getRestoreDefaultNetworkDelay(int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    if (!this.mRemote.transact(73, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRestoreDefaultNetworkDelay(networkType);
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

            @Override // android.net.IConnectivityManager
            public boolean addVpnAddress(String address, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(prefixLength);
                    boolean _result = false;
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addVpnAddress(address, prefixLength);
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

            @Override // android.net.IConnectivityManager
            public boolean removeVpnAddress(String address, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(prefixLength);
                    boolean _result = false;
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeVpnAddress(address, prefixLength);
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

            @Override // android.net.IConnectivityManager
            public boolean setUnderlyingNetworksForVpn(Network[] networks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    _data.writeTypedArray(networks, 0);
                    if (!this.mRemote.transact(76, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setUnderlyingNetworksForVpn(networks);
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

            @Override // android.net.IConnectivityManager
            public void factoryReset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(77, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().factoryReset();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void startNattKeepalive(Network network, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddr, int srcPort, String dstAddr) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeInt(intervalSeconds);
                        _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                        try {
                            _data.writeString(srcAddr);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(srcPort);
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(dstAddr);
                        try {
                            if (this.mRemote.transact(78, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().startNattKeepalive(network, intervalSeconds, cb, srcAddr, srcPort, dstAddr);
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

            @Override // android.net.IConnectivityManager
            public void startNattKeepaliveWithFd(Network network, FileDescriptor fd, int resourceId, int intervalSeconds, ISocketKeepaliveCallback cb, String srcAddr, String dstAddr) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeRawFileDescriptor(fd);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(resourceId);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(intervalSeconds);
                        _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(srcAddr);
                        _data.writeString(dstAddr);
                        if (this.mRemote.transact(79, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().startNattKeepaliveWithFd(network, fd, resourceId, intervalSeconds, cb, srcAddr, dstAddr);
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
            }

            @Override // android.net.IConnectivityManager
            public void startTcpKeepalive(Network network, FileDescriptor fd, int intervalSeconds, ISocketKeepaliveCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeRawFileDescriptor(fd);
                    _data.writeInt(intervalSeconds);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(80, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startTcpKeepalive(network, fd, intervalSeconds, cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void stopKeepalive(Network network, int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(slot);
                    if (this.mRemote.transact(81, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopKeepalive(network, slot);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public String getCaptivePortalServerUrl() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(82, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCaptivePortalServerUrl();
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

            @Override // android.net.IConnectivityManager
            public byte[] getNetworkWatchlistConfigHash() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(83, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkWatchlistConfigHash();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public int getConnectionOwnerUid(ConnectionInfo connectionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connectionInfo != null) {
                        _data.writeInt(1);
                        connectionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(84, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConnectionOwnerUid(connectionInfo);
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

            @Override // android.net.IConnectivityManager
            public boolean isCallerCurrentAlwaysOnVpnApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(85, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCallerCurrentAlwaysOnVpnApp();
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

            @Override // android.net.IConnectivityManager
            public boolean isCallerCurrentAlwaysOnVpnLockdownApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(86, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCallerCurrentAlwaysOnVpnLockdownApp();
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

            @Override // android.net.IConnectivityManager
            public void getLatestTetheringEntitlementResult(int type, ResultReceiver receiver, boolean showEntitlementUi, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    int i = 1;
                    if (receiver != null) {
                        _data.writeInt(1);
                        receiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!showEntitlementUi) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(callerPkg);
                    if (this.mRemote.transact(87, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getLatestTetheringEntitlementResult(type, receiver, showEntitlementUi, callerPkg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public boolean turnOffVpn(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(88, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().turnOffVpn(packageName, userId);
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

            @Override // android.net.IConnectivityManager
            public void registerTetheringEventCallback(ITetheringEventCallback callback, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(callerPkg);
                    if (this.mRemote.transact(89, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerTetheringEventCallback(callback, callerPkg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public void unregisterTetheringEventCallback(ITetheringEventCallback callback, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(callerPkg);
                    if (this.mRemote.transact(90, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterTetheringEventCallback(callback, callerPkg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IConnectivityManager
            public IBinder startOrGetTestNetworkService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(91, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startOrGetTestNetworkService();
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
        }

        public static boolean setDefaultImpl(IConnectivityManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IConnectivityManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
