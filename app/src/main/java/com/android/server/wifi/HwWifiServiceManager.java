package com.android.server.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.security.KeyStore;
import android.util.LocalLog;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.WifiServiceImpl.LockList;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import java.util.ArrayList;

public interface HwWifiServiceManager {
    boolean autoConnectByMode(Message message);

    void createHwArpVerifier(Context context);

    SoftApManager createHwSoftApManager(Context context, Looper looper, WifiNative wifiNative, INetworkManagementService iNetworkManagementService, ConnectivityManager connectivityManager, String str, ArrayList<Integer> arrayList, Listener listener);

    WifiConfigManager createHwWifiConfigManager(Context context, WifiNative wifiNative, FrameworkFacade frameworkFacade, Clock clock, UserManager userManager, KeyStore keyStore);

    WifiConfigStore createHwWifiConfigStore(WifiNative wifiNative, KeyStore keyStore, LocalLog localLog, boolean z, boolean z2);

    WifiConnectivityManager createHwWifiConnectivityManager(Context context, WifiStateMachine wifiStateMachine, WifiScanner wifiScanner, WifiConfigManager wifiConfigManager, WifiInfo wifiInfo, WifiQualifiedNetworkSelector wifiQualifiedNetworkSelector, WifiInjector wifiInjector, Looper looper);

    WifiController createHwWifiController(Context context, WifiStateMachine wifiStateMachine, WifiSettingsStore wifiSettingsStore, LockList lockList, Looper looper, FrameworkFacade frameworkFacade);

    WifiCountryCode createHwWifiCountryCode(Context context, WifiNative wifiNative, String str, String str2, boolean z);

    WifiP2pServiceImpl createHwWifiP2pService(Context context);

    WifiQualifiedNetworkSelector createHwWifiQualifiedNetworkSelector(WifiConfigManager wifiConfigManager, Context context, WifiInfo wifiInfo, Clock clock, WifiStateMachine wifiStateMachine, WifiNative wifiNative);

    WifiServiceImpl createHwWifiService(Context context);

    WifiStateMachine createHwWifiStateMachine(Context context, FrameworkFacade frameworkFacade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode wifiCountryCode);

    boolean custApConfiguration(WifiApConfigStore wifiApConfigStore, WifiConfiguration wifiConfiguration, Context context);

    String getAppendSsidWithRandomUuid(WifiConfiguration wifiConfiguration, Context context);

    String getCustWifiApDefaultName(WifiConfiguration wifiConfiguration);

    WifiStateMachine getGlobalHwWifiStateMachine();
}
