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

public interface HwWifiServiceManager {
    boolean autoConnectByMode(Message message);

    void createHwArpVerifier(Context context);

    SavedNetworkEvaluator createHwSavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager wifiConfigManager, Clock clock, LocalLog localLog, ClientModeImpl clientModeImpl, WifiConnectivityHelper wifiConnectivityHelper, SubscriptionManager subscriptionManager);

    SoftApManager createHwSoftApManager(Context context, Looper looper, FrameworkFacade frameworkFacade, WifiNative wifiNative, String str, WifiManager.SoftApCallback softApCallback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration softApModeConfiguration, WifiMetrics wifiMetrics, SarManager sarManager);

    WifiConfigManager createHwWifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, WifiInjector wifiInjector, NetworkListSharedStoreData networkListSharedStoreData, NetworkListUserStoreData networkListUserStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData, RandomizedMacStoreData randomizedMacStoreData, FrameworkFacade frameworkFacade, Looper looper);

    WifiConnectivityManager createHwWifiConnectivityManager(Context context, ScoringParams scoringParams, ClientModeImpl clientModeImpl, WifiInjector wifiInjector, WifiConfigManager wifiConfigManager, WifiInfo wifiInfo, WifiNetworkSelector wifiNetworkSelector, WifiConnectivityHelper wifiConnectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog);

    WifiController createHwWifiController(Context context, ClientModeImpl clientModeImpl, Looper looper, WifiSettingsStore wifiSettingsStore, Looper looper2, FrameworkFacade frameworkFacade, ActiveModeWarden activeModeWarden, WifiPermissionsUtil wifiPermissionsUtil);

    WifiCountryCode createHwWifiCountryCode(Context context, WifiNative wifiNative, String str, boolean z);

    WifiP2pServiceImpl createHwWifiP2pService(Context context, WifiInjector wifiInjector);

    WifiScanningServiceImpl createHwWifiScanningServiceImpl(Context context, Looper looper, WifiScannerImpl.WifiScannerImplFactory wifiScannerImplFactory, IBatteryStats iBatteryStats, WifiInjector wifiInjector);

    WifiServiceImpl createHwWifiService(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel);

    ClientModeImpl createHwWifiStateMachine(Context context, FrameworkFacade frameworkFacade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode wifiCountryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager, WifiTrafficPoller wifiTrafficPoller, LinkProbeManager linkProbeManager);

    boolean custApConfiguration(WifiApConfigStore wifiApConfigStore, WifiConfiguration wifiConfiguration, Context context);

    boolean custSttingsEnableSoftap(String str);

    String getAppendSsidWithRandomUuid(WifiConfiguration wifiConfiguration, Context context);

    String getCustWifiApDefaultName(WifiConfiguration wifiConfiguration);
}
