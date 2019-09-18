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

public interface HwWifiServiceManager {
    boolean autoConnectByMode(Message message);

    void createHwArpVerifier(Context context);

    SavedNetworkEvaluator createHwSavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager wifiConfigManager, Clock clock, LocalLog localLog, WifiStateMachine wifiStateMachine, WifiConnectivityHelper wifiConnectivityHelper);

    SoftApManager createHwSoftApManager(Context context, Looper looper, FrameworkFacade frameworkFacade, WifiNative wifiNative, String str, WifiManager.SoftApCallback softApCallback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration softApModeConfiguration, WifiMetrics wifiMetrics);

    WifiConfigManager createHwWifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiConfigStoreLegacy wifiConfigStoreLegacy, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, NetworkListStoreData networkListStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData);

    WifiConnectivityManager createHwWifiConnectivityManager(Context context, ScoringParams scoringParams, WifiStateMachine wifiStateMachine, WifiScanner wifiScanner, WifiConfigManager wifiConfigManager, WifiInfo wifiInfo, WifiNetworkSelector wifiNetworkSelector, WifiConnectivityHelper wifiConnectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog, boolean z, FrameworkFacade frameworkFacade, SavedNetworkEvaluator savedNetworkEvaluator, ScoredNetworkEvaluator scoredNetworkEvaluator, PasspointNetworkEvaluator passpointNetworkEvaluator);

    WifiController createHwWifiController(Context context, WifiStateMachine wifiStateMachine, Looper looper, WifiSettingsStore wifiSettingsStore, Looper looper2, FrameworkFacade frameworkFacade, WifiStateMachinePrime wifiStateMachinePrime);

    WifiCountryCode createHwWifiCountryCode(Context context, WifiNative wifiNative, String str, boolean z);

    WifiP2pServiceImpl createHwWifiP2pService(Context context);

    WifiScanningServiceImpl createHwWifiScanningServiceImpl(Context context, Looper looper, WifiScannerImpl.WifiScannerImplFactory wifiScannerImplFactory, IBatteryStats iBatteryStats, WifiInjector wifiInjector);

    WifiServiceImpl createHwWifiService(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel);

    WifiStateMachine createHwWifiStateMachine(Context context, FrameworkFacade frameworkFacade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode wifiCountryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager);

    WifiSupplicantControl createHwWifiSupplicantControl(TelephonyManager telephonyManager, WifiNative wifiNative, LocalLog localLog);

    boolean custApConfiguration(WifiApConfigStore wifiApConfigStore, WifiConfiguration wifiConfiguration, Context context);

    boolean custSttingsEnableSoftap(String str);

    String getAppendSsidWithRandomUuid(WifiConfiguration wifiConfiguration, Context context);

    String getCustWifiApDefaultName(WifiConfiguration wifiConfiguration);
}
