package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.SettingsEx;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.wifi.HwHiLog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.p2p.HwWifiP2pService;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.scanner.WifiScannerImpl;
import com.android.server.wifi.scanner.WifiScanningServiceImpl;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.android.server.wifi.wifipro.HwSavedNetworkEvaluator;
import java.util.UUID;

public class HwWifiServiceManagerImpl implements HwWifiServiceManager {
    private static final String TAG = "HwWifiServiceManagerImpl";
    private static HwWifiServiceManager mInstance = new HwWifiServiceManagerImpl();
    private HwWifiP2pService mHwWifiP2PService = null;

    public WifiServiceImpl createHwWifiService(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        return new HwWifiService(context, wifiInjector, asyncChannel);
    }

    public WifiController createHwWifiController(Context context, ClientModeImpl wsm, Looper wifiStateMachineLooper, WifiSettingsStore wss, Looper wifiServiceLooper, FrameworkFacade f, ActiveModeWarden wsmp, WifiPermissionsUtil wifiPermissionsUtil) {
        return new HwWifiController(context, wsm, wifiStateMachineLooper, wss, wifiServiceLooper, f, wsmp, wifiPermissionsUtil);
    }

    public static HwWifiServiceManager getDefault() {
        return mInstance;
    }

    public boolean custApConfiguration(WifiApConfigStore s, WifiConfiguration config, Context context) {
        String custConf = SettingsEx.Systemex.getString(context.getContentResolver(), "hw_softap_default");
        if (custConf != null && !"".equals(custConf)) {
            String[] args = custConf.split(",");
            if (args.length == 3) {
                try {
                    if (" ".equals(args[0])) {
                        config.SSID = context.getString(17041571);
                    } else {
                        config.SSID = args[0];
                    }
                    if (" ".equals(args[1])) {
                        config.allowedKeyManagement.set(4);
                    } else {
                        config.allowedKeyManagement.set(Integer.valueOf(args[1]).intValue());
                    }
                    if (" ".equals(args[2])) {
                        String randomUUID = UUID.randomUUID().toString();
                        config.preSharedKey = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
                    } else {
                        config.preSharedKey = args[2];
                    }
                    config.SSID = getAppendSsidWithRandomUuid(config, context);
                    s.setApConfiguration(config);
                    return true;
                } catch (Exception e) {
                    HwHiLog.e(TAG, false, "Error parse cust Ap configuration", new Object[0]);
                    return false;
                }
            }
        }
        return false;
    }

    public boolean autoConnectByMode(Message message) {
        HwHiLog.d(TAG, false, "autoConnectByMode CONNECT_NETWORK arg1:%{public}d", new Object[]{Integer.valueOf(message.arg1)});
        return message.arg1 != 100;
    }

    public WifiP2pServiceImpl createHwWifiP2pService(Context context, WifiInjector wifiInjector) {
        this.mHwWifiP2PService = new HwWifiP2pService(context, wifiInjector);
        return this.mHwWifiP2PService;
    }

    public WifiP2pServiceImpl getHwWifiP2pService() {
        return this.mHwWifiP2PService;
    }

    public void createHwArpVerifier(Context context) {
    }

    public ClientModeImpl createHwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager, WifiTrafficPoller wifiTrafficPoller, LinkProbeManager linkProbeManager) {
        return new HwWifiStateMachine(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode, wifiNative, wrongPasswordNotifier, sarManager, wifiTrafficPoller, linkProbeManager);
    }

    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        if (1 == SettingsEx.Systemex.getInt(context.getContentResolver(), "hw_softap_ssid_extend", 0)) {
            String randomUUID = UUID.randomUUID().toString();
            config.SSID += randomUUID.substring(13, 18);
            HwHiLog.d(TAG, false, "new SSID:%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID)});
        }
        return config.SSID;
    }

    public String getCustWifiApDefaultName(WifiConfiguration config) {
        StringBuilder sb = new StringBuilder();
        String brandName = SystemProperties.get("ro.product.brand", "");
        String productName = SystemProperties.get("ro.product.name", "");
        String randomUUID = UUID.randomUUID().toString();
        String marketing_name = SystemProperties.get("ro.config.marketing_name");
        if (!TextUtils.isEmpty(marketing_name)) {
            config.SSID = marketing_name;
        } else {
            if (TextUtils.isEmpty(brandName)) {
                sb.append("HUAWEI");
            } else {
                sb.append(brandName);
            }
            if (!TextUtils.isEmpty(productName)) {
                String productNameShort = productName.contains("-") ? productName.substring(0, productName.indexOf(45)) : productName;
                sb.append('_');
                sb.append(productNameShort);
            }
            if (!TextUtils.isEmpty(randomUUID)) {
                sb.append('_');
                sb.append(randomUUID.substring(24, 28));
            }
            config.SSID = sb.toString();
        }
        HwHiLog.d(TAG, false, "getCustWifiApDefaultName returns:%{public}s", new Object[]{config.SSID});
        return config.SSID;
    }

    public SavedNetworkEvaluator createHwSavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog, ClientModeImpl wsm, WifiConnectivityHelper connectivityHelper, SubscriptionManager subscriptionManager) {
        return new HwSavedNetworkEvaluator(context, scoringParams, configManager, clock, localLog, wsm, connectivityHelper, subscriptionManager);
    }

    public WifiScanningServiceImpl createHwWifiScanningServiceImpl(Context context, Looper looper, WifiScannerImpl.WifiScannerImplFactory scannerImplFactory, IBatteryStats batteryStats, WifiInjector wifiInjector) {
        return new HwWifiScanningServiceImpl(context, looper, scannerImplFactory, batteryStats, wifiInjector);
    }

    public WifiConfigManager createHwWifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, WifiInjector wifiInjector, NetworkListSharedStoreData networkListSharedStoreData, NetworkListUserStoreData networkListUserStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData, RandomizedMacStoreData randomizedMacStoreData, FrameworkFacade frameworkFacade, Looper looper) {
        return new HwWifiConfigManager(context, clock, userManager, telephonyManager, wifiKeyStore, wifiConfigStore, wifiPermissionsUtil, wifiPermissionsWrapper, wifiInjector, networkListSharedStoreData, networkListUserStoreData, deletedEphemeralSsidsStoreData, randomizedMacStoreData, frameworkFacade, looper);
    }

    public WifiCountryCode createHwWifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        return new HwWifiCountryCode(context, wifiNative, oemDefaultCountryCode, revertCountryCodeOnCellularLoss);
    }

    public SoftApManager createHwSoftApManager(Context context, Looper looper, FrameworkFacade frameworkFacade, WifiNative wifiNative, String countryCode, WifiManager.SoftApCallback callback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration config, WifiMetrics wifiMetrics, SarManager sarManager) {
        return new HwSoftApManager(context, looper, frameworkFacade, wifiNative, countryCode, callback, wifiApConfigStore, config, wifiMetrics, sarManager);
    }

    public WifiConnectivityManager createHwWifiConnectivityManager(Context context, ScoringParams scoringParams, ClientModeImpl stateMachine, WifiInjector injector, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog) {
        return new HwWifiConnectivityManager(context, scoringParams, stateMachine, injector, configManager, wifiInfo, networkSelector, connectivityHelper, wifiLastResortWatchdog, openNetworkNotifier, carrierNetworkNotifier, carrierNetworkConfig, wifiMetrics, looper, clock, localLog);
    }

    public boolean custSttingsEnableSoftap(String packageName) {
        if (SystemProperties.getBoolean("ro.config.custapp.enableAp", false)) {
            HwHiLog.d(TAG, false, "setWifiApEnabled packageName is: %{public}s", new Object[]{packageName});
            if (!packageName.equals("com.android.settings")) {
                return true;
            }
        }
        return false;
    }
}
