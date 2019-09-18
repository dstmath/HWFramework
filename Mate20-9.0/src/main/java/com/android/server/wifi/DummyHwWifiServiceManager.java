package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.scanner.WifiScannerImpl;
import com.android.server.wifi.scanner.WifiScanningServiceImpl;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;

public class DummyHwWifiServiceManager implements HwWifiServiceManager {
    private static HwWifiServiceManager mInstance = new DummyHwWifiServiceManager();

    public WifiServiceImpl createHwWifiService(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        return new WifiServiceImpl(context, wifiInjector, asyncChannel);
    }

    public WifiController createHwWifiController(Context context, WifiStateMachine wsm, Looper wifiStateMachineLooper, WifiSettingsStore wss, Looper wifiServiceLooper, FrameworkFacade f, WifiStateMachinePrime wsmp) {
        WifiController wifiController = new WifiController(context, wsm, wifiStateMachineLooper, wss, wifiServiceLooper, f, wsmp);
        return wifiController;
    }

    public static HwWifiServiceManager getDefault() {
        return mInstance;
    }

    public boolean custApConfiguration(WifiApConfigStore s, WifiConfiguration config, Context context) {
        return false;
    }

    public boolean autoConnectByMode(Message message) {
        return false;
    }

    public WifiP2pServiceImpl createHwWifiP2pService(Context context) {
        return null;
    }

    public WifiStateMachine createHwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager) {
        WifiStateMachine wifiStateMachine = new WifiStateMachine(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode, wifiNative, wrongPasswordNotifier, sarManager);
        return wifiStateMachine;
    }

    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        return null;
    }

    public String getCustWifiApDefaultName(WifiConfiguration config) {
        return null;
    }

    public void createHwArpVerifier(Context context) {
    }

    public SavedNetworkEvaluator createHwSavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog, WifiStateMachine wsm, WifiConnectivityHelper connectivityHelper) {
        return null;
    }

    public WifiScanningServiceImpl createHwWifiScanningServiceImpl(Context context, Looper looper, WifiScannerImpl.WifiScannerImplFactory scannerImplFactory, IBatteryStats batteryStats, WifiInjector wifiInjector) {
        return null;
    }

    public WifiConfigManager createHwWifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiConfigStoreLegacy wifiConfigStoreLegacy, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, NetworkListStoreData networkListStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData) {
        WifiConfigManager wifiConfigManager = new WifiConfigManager(context, clock, userManager, telephonyManager, wifiKeyStore, wifiConfigStore, wifiConfigStoreLegacy, wifiPermissionsUtil, wifiPermissionsWrapper, networkListStoreData, deletedEphemeralSsidsStoreData);
        return wifiConfigManager;
    }

    public WifiCountryCode createHwWifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        return new WifiCountryCode(wifiNative, oemDefaultCountryCode, revertCountryCodeOnCellularLoss);
    }

    public WifiSupplicantControl createHwWifiSupplicantControl(TelephonyManager telephonyManager, WifiNative wifiNative, LocalLog localLog) {
        return null;
    }

    public SoftApManager createHwSoftApManager(Context context, Looper looper, FrameworkFacade frameworkFacade, WifiNative wifiNative, String countryCode, WifiManager.SoftApCallback callback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration config, WifiMetrics wifiMetrics) {
        SoftApManager softApManager = new SoftApManager(context, looper, frameworkFacade, wifiNative, countryCode, callback, wifiApConfigStore, config, wifiMetrics);
        return softApManager;
    }

    public WifiConnectivityManager createHwWifiConnectivityManager(Context context, ScoringParams scoringParams, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog, boolean enable, FrameworkFacade frameworkFacade, SavedNetworkEvaluator savedNetworkEvaluator, ScoredNetworkEvaluator scoredNetworkEvaluator, PasspointNetworkEvaluator passpointNetworkEvaluator) {
        WifiConnectivityManager wifiConnectivityManager = new WifiConnectivityManager(context, scoringParams, stateMachine, scanner, configManager, wifiInfo, networkSelector, connectivityHelper, wifiLastResortWatchdog, openNetworkNotifier, carrierNetworkNotifier, carrierNetworkConfig, wifiMetrics, looper, clock, localLog, enable, frameworkFacade, savedNetworkEvaluator, scoredNetworkEvaluator, passpointNetworkEvaluator);
        return wifiConnectivityManager;
    }

    public boolean custSttingsEnableSoftap(String packageName) {
        return false;
    }
}
