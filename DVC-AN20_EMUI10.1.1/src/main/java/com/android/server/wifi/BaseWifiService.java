package com.android.server.wifi;

import android.content.pm.ParceledListSlice;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.wifi.HotspotConfig;
import android.net.wifi.IDppCallback;
import android.net.wifi.INetworkRequestMatchCallback;
import android.net.wifi.IOnWifiUsabilityStatsListener;
import android.net.wifi.ISoftApCallback;
import android.net.wifi.ITrafficStateCallback;
import android.net.wifi.IWifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiDeviceConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkedInfo;
import android.net.wifi.WifiNetworkSuggestion;
import android.net.wifi.WifiScanInfo;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.WorkSource;
import java.util.List;
import java.util.Map;

public class BaseWifiService extends IWifiManager.Stub {
    private static final String TAG = BaseWifiService.class.getSimpleName();

    @Override // android.net.wifi.IWifiManager
    public long getSupportedFeatures() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public WifiActivityEnergyInfo reportActivityInfo() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void requestActivityInfo(ResultReceiver result) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public ParceledListSlice getConfiguredNetworks(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public ParceledListSlice getPrivilegedConfiguredNetworks(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public Map<String, Map<Integer, List<ScanResult>>> getAllMatchingFqdnsForScanResults(List<ScanResult> list) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public Map<OsuProvider, List<ScanResult>> getMatchingOsuProviders(List<ScanResult> list) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public Map<OsuProvider, PasspointConfiguration> getMatchingPasspointConfigsForOsuProviders(List<OsuProvider> list) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public int addOrUpdateNetwork(WifiConfiguration config, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean addOrUpdatePasspointConfiguration(PasspointConfiguration config, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean removePasspointConfiguration(String fqdn, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public List<PasspointConfiguration> getPasspointConfigurations(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public List<WifiConfiguration> getWifiConfigsForPasspointProfiles(List<String> list) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void queryPasspointIcon(long bssid, String fileName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public int matchProviderWithCurrentNetwork(String fqdn) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void deauthenticateNetwork(long holdoff, boolean ess) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean removeNetwork(int netId, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean enableNetwork(int netId, boolean disableOthers, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean disableNetwork(int netId, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean startScan(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public List<ScanResult> getScanResults(String callingPackage) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean disconnect(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean reconnect(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean reassociate(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public WifiInfo getConnectionInfo(String callingPackage) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean setWifiEnabled(String packageName, boolean enable) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public int getWifiEnabledState() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void setCountryCode(String country) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public String getCountryCode() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean isDualBandSupported() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean needs5GHzToAnyApBandConversion() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public DhcpInfo getDhcpInfo() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean isScanAlwaysAvailable() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean acquireWifiLock(IBinder lock, int lockType, String tag, WorkSource ws) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void updateWifiLockWorkSource(IBinder lock, WorkSource ws) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean releaseWifiLock(IBinder lock) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void initializeMulticastFiltering() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean isMulticastEnabled() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void acquireMulticastLock(IBinder binder, String tag) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void releaseMulticastLock(String tag) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void updateInterfaceIpState(String ifaceName, int mode) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean startSoftAp(WifiConfiguration wifiConfig) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean stopSoftAp() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public int startLocalOnlyHotspot(Messenger messenger, IBinder binder, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void stopLocalOnlyHotspot() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void startWatchLocalOnlyHotspot(Messenger messenger, IBinder binder) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void stopWatchLocalOnlyHotspot() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public int getWifiApEnabledState() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public WifiConfiguration getWifiApConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void notifyUserOfApBandConversion(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public Messenger getWifiServiceMessenger(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void enableTdls(String remoteIPAddress, boolean enable) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public String getCurrentNetworkWpsNfcConfigurationToken() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void enableVerboseLogging(int verbose) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public int getVerboseLoggingLevel() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void enableWifiConnectivityManager(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void disableEphemeralNetwork(String SSID, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void factoryReset(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public Network getCurrentNetwork() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public byte[] retrieveBackupData() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void restoreBackupData(byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void restoreSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void startSubscriptionProvisioning(OsuProvider provider, IProvisioningCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void registerSoftApCallback(IBinder binder, ISoftApCallback callback, int callbackIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void unregisterSoftApCallback(int callbackIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void registerTrafficStateCallback(IBinder binder, ITrafficStateCallback callback, int callbackIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void unregisterTrafficStateCallback(int callbackIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void registerNetworkRequestMatchCallback(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void unregisterNetworkRequestMatchCallback(int callbackIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public int addNetworkSuggestions(List<WifiNetworkSuggestion> list, String callingPackageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public int removeNetworkSuggestions(List<WifiNetworkSuggestion> list, String callingPackageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public String[] getFactoryMacAddresses() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void setDeviceMobilityState(int state) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void startDppAsConfiguratorInitiator(IBinder binder, String enrolleeUri, int selectedNetworkId, int netRole, IDppCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void startDppAsEnrolleeInitiator(IBinder binder, String configuratorUri, IDppCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void stopDppSession() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void addOnWifiUsabilityStatsListener(IBinder binder, IOnWifiUsabilityStatsListener listener, int listenerIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void removeOnWifiUsabilityStatsListener(int listenerIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean setVoWifiDetectMode(WifiDetectConfInfo info) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public int addOrUpdateWifiDeviceConfig(WifiDeviceConfig config, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public ParceledListSlice getWifiDeviceConfigs(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public List<WifiScanInfo> getScanInfoList(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean enableHotspot(boolean enable, HotspotConfig hotspotConfig) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public boolean setHotspotConfig(HotspotConfig hotspotConfig, String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public HotspotConfig getHotspotConfig() {
        throw new UnsupportedOperationException();
    }

    @Override // android.net.wifi.IWifiManager
    public WifiLinkedInfo getLinkedInfo(String packageName) {
        throw new UnsupportedOperationException();
    }
}
