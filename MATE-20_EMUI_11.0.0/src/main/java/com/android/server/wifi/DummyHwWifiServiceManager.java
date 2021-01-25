package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.scanner.WifiScannerImpl;
import com.android.server.wifi.scanner.WifiScanningServiceImpl;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;

public class DummyHwWifiServiceManager implements HwWifiServiceManager {
    private static HwWifiServiceManager mInstance = new DummyHwWifiServiceManager();

    @Override // com.android.server.wifi.HwWifiServiceManager
    public WifiServiceImpl createHwWifiService(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        return new WifiServiceImpl(context, wifiInjector, asyncChannel);
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public WifiController createHwWifiController(Context context, ClientModeImpl wsm, Looper wifiStateMachineLooper, WifiSettingsStore wss, Looper wifiServiceLooper, FrameworkFacade f, ActiveModeWarden wsmp, WifiPermissionsUtil wifiPermissionsUtil) {
        return new WifiController(context, wsm, wifiStateMachineLooper, wss, wifiServiceLooper, f, wsmp, wifiPermissionsUtil);
    }

    public static HwWifiServiceManager getDefault() {
        return mInstance;
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public boolean custApConfiguration(WifiApConfigStore s, WifiConfiguration config, Context context) {
        return false;
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public boolean autoConnectByMode(Message message) {
        return false;
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public WifiP2pServiceImpl createHwWifiP2pService(Context context, WifiInjector wifiInjector) {
        return null;
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public ClientModeImpl createHwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager, WifiTrafficPoller wifiTrafficPoller, LinkProbeManager linkProbeManager) {
        return new ClientModeImpl(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode, wifiNative, wrongPasswordNotifier, sarManager, wifiTrafficPoller, linkProbeManager);
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        return null;
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public String getCustWifiApDefaultName(WifiConfiguration config) {
        return null;
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public void createHwArpVerifier(Context context) {
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public SavedNetworkEvaluator createHwSavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog, ClientModeImpl wsm, WifiConnectivityHelper connectivityHelper, SubscriptionManager subscriptionManager) {
        return null;
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public WifiScanningServiceImpl createHwWifiScanningServiceImpl(Context context, Looper looper, WifiScannerImpl.WifiScannerImplFactory scannerImplFactory, IBatteryStats batteryStats, WifiInjector wifiInjector) {
        return null;
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public WifiConfigManager createHwWifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, WifiInjector wifiInjector, NetworkListSharedStoreData networkListSharedStoreData, NetworkListUserStoreData networkListUserStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData, RandomizedMacStoreData randomizedMacStoreData, FrameworkFacade frameworkFacade, Looper looper) {
        return new WifiConfigManager(context, clock, userManager, telephonyManager, wifiKeyStore, wifiConfigStore, wifiPermissionsUtil, wifiPermissionsWrapper, wifiInjector, networkListSharedStoreData, networkListUserStoreData, deletedEphemeralSsidsStoreData, randomizedMacStoreData, frameworkFacade, looper);
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public WifiCountryCode createHwWifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        return new WifiCountryCode(wifiNative, oemDefaultCountryCode, revertCountryCodeOnCellularLoss);
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public SoftApManager createHwSoftApManager(Context context, Looper looper, FrameworkFacade frameworkFacade, WifiNative wifiNative, String countryCode, WifiManager.SoftApCallback callback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration config, WifiMetrics wifiMetrics, SarManager sarManager) {
        return new SoftApManager(context, looper, frameworkFacade, wifiNative, countryCode, callback, wifiApConfigStore, config, wifiMetrics, sarManager);
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public WifiConnectivityManager createHwWifiConnectivityManager(Context context, ScoringParams scoringParams, ClientModeImpl stateMachine, WifiInjector injector, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog) {
        return new WifiConnectivityManager(context, scoringParams, stateMachine, injector, configManager, wifiInfo, networkSelector, connectivityHelper, wifiLastResortWatchdog, openNetworkNotifier, carrierNetworkNotifier, carrierNetworkConfig, wifiMetrics, looper, clock, localLog);
    }

    @Override // com.android.server.wifi.HwWifiServiceManager
    public boolean custSttingsEnableSoftap(String packageName) {
        return false;
    }
}
