package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.IApInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.SettingsEx.Systemex;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.p2p.HwWifiP2pService;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.scanner.WifiScannerImpl.WifiScannerImplFactory;
import com.android.server.wifi.scanner.WifiScanningServiceImpl;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.android.server.wifi.wifipro.HwSavedNetworkEvaluator;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class HwWifiServiceManagerImpl implements HwWifiServiceManager {
    private static final String TAG = "HwWifiServiceManagerImpl";
    private static HwWifiServiceManager mInstance = new HwWifiServiceManagerImpl();
    private HwWifiP2pService mHwWifiP2PService = null;

    public WifiServiceImpl createHwWifiService(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        return new HwWifiService(context, wifiInjector, asyncChannel);
    }

    public WifiController createHwWifiController(Context context, WifiStateMachine wsm, WifiSettingsStore wss, WifiLockManager wifiLockManager, Looper looper, FrameworkFacade f) {
        return new HwWifiController(context, wsm, wss, wifiLockManager, looper, f);
    }

    public static HwWifiServiceManager getDefault() {
        return mInstance;
    }

    public boolean custApConfiguration(WifiApConfigStore s, WifiConfiguration config, Context context) {
        String custConf = Systemex.getString(context.getContentResolver(), "hw_softap_default");
        if (!(custConf == null || (custConf.equals("") ^ 1) == 0)) {
            String[] args = custConf.split(",");
            if (3 == args.length) {
                try {
                    if (args[0].equals(HwCHRWifiCPUUsage.COL_SEP)) {
                        config.SSID = context.getString(17041262);
                    } else {
                        config.SSID = args[0];
                    }
                    if (args[1].equals(HwCHRWifiCPUUsage.COL_SEP)) {
                        config.allowedKeyManagement.set(4);
                    } else {
                        config.allowedKeyManagement.set(Integer.valueOf(args[1]).intValue());
                    }
                    if (args[2].equals(HwCHRWifiCPUUsage.COL_SEP)) {
                        String randomUUID = UUID.randomUUID().toString();
                        config.preSharedKey = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
                    } else {
                        config.preSharedKey = args[2];
                    }
                    config.SSID = getAppendSsidWithRandomUuid(config, context);
                    s.setApConfiguration(config);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error parse cust Ap configuration " + e);
                    return false;
                }
            }
        }
        return false;
    }

    public boolean autoConnectByMode(Message message) {
        Log.d(TAG, "autoConnectByMode CONNECT_NETWORK arg1:" + message.arg1);
        return message.arg1 != 100;
    }

    public WifiP2pServiceImpl createHwWifiP2pService(Context context) {
        this.mHwWifiP2PService = new HwWifiP2pService(context);
        return this.mHwWifiP2PService;
    }

    public WifiP2pServiceImpl getHwWifiP2pService() {
        return this.mHwWifiP2PService;
    }

    public void createHwArpVerifier(Context context) {
        HwArpVerifier.newInstance(context);
    }

    public WifiStateMachine createHwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative) {
        return new HwWifiStateMachine(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode, wifiNative);
    }

    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        if (1 == Systemex.getInt(context.getContentResolver(), "hw_softap_ssid_extend", 0)) {
            config.SSID += UUID.randomUUID().toString().substring(13, 18);
            Log.d(TAG, "new SSID:" + config.SSID);
        }
        return config.SSID;
    }

    private String getMD5str(String str) {
        String md5Str = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes("UTF-8"));
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return md5Str;
        } catch (Exception e1) {
            e1.printStackTrace();
            return md5Str;
        }
    }

    public String getCustWifiApDefaultName(WifiConfiguration config) {
        StringBuilder sb = new StringBuilder();
        String brandName = SystemProperties.get("ro.product.brand", "");
        String productName = SystemProperties.get("ro.product.name", "");
        String randomUUID = UUID.randomUUID().toString();
        String marketing_name = SystemProperties.get("ro.config.marketing_name");
        if (TextUtils.isEmpty(marketing_name)) {
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
        } else {
            config.SSID = marketing_name;
        }
        Log.d(TAG, "getCustWifiApDefaultName returns:" + config.SSID);
        return config.SSID;
    }

    public SavedNetworkEvaluator createHwSavedNetworkEvaluator(Context context, WifiConfigManager configManager, Clock clock, LocalLog localLog, WifiStateMachine wsm, WifiConnectivityHelper connectivityHelper) {
        return new HwSavedNetworkEvaluator(context, configManager, clock, localLog, wsm, connectivityHelper);
    }

    public WifiScanningServiceImpl createHwWifiScanningServiceImpl(Context context, Looper looper, WifiScannerImplFactory scannerImplFactory, IBatteryStats batteryStats, WifiInjector wifiInjector) {
        return new HwWifiScanningServiceImpl(context, looper, scannerImplFactory, batteryStats, wifiInjector);
    }

    public WifiConfigManager createHwWifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiConfigStoreLegacy wifiConfigStoreLegacy, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, NetworkListStoreData networkListStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData) {
        return new HwWifiConfigManager(context, clock, userManager, telephonyManager, wifiKeyStore, wifiConfigStore, wifiConfigStoreLegacy, wifiPermissionsUtil, wifiPermissionsWrapper, networkListStoreData, deletedEphemeralSsidsStoreData);
    }

    public WifiCountryCode createHwWifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        return new HwWifiCountryCode(context, wifiNative, oemDefaultCountryCode, revertCountryCodeOnCellularLoss);
    }

    public WifiSupplicantControl createHwWifiSupplicantControl(TelephonyManager telephonyManager, WifiNative wifiNative, LocalLog localLog) {
        return null;
    }

    public SoftApManager createHwSoftApManager(Context context, Looper looper, WifiNative wifiNative, String countryCode, Listener listener, IApInterface apInterface, INetworkManagementService nms, WifiApConfigStore wifiApConfigStore, WifiConfiguration config, WifiMetrics wifiMetrics) {
        return new HwSoftApManager(context, looper, wifiNative, countryCode, listener, apInterface, nms, wifiApConfigStore, config, wifiMetrics);
    }

    public WifiConnectivityManager createHwWifiConnectivityManager(Context context, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog, boolean enable, FrameworkFacade frameworkFacade, SavedNetworkEvaluator savedNetworkEvaluator, ScoredNetworkEvaluator scoredNetworkEvaluator, PasspointNetworkEvaluator passpointNetworkEvaluator) {
        return new HwWifiConnectivityManager(context, stateMachine, scanner, configManager, wifiInfo, networkSelector, connectivityHelper, wifiLastResortWatchdog, wifiMetrics, looper, clock, localLog, enable, frameworkFacade, savedNetworkEvaluator, scoredNetworkEvaluator, passpointNetworkEvaluator);
    }

    public boolean custSttingsEnableSoftap(String packageName) {
        if (SystemProperties.getBoolean("ro.config.custapp.enableAp", false)) {
            Log.d(TAG, "setWifiApEnabled packageName is: " + packageName);
            if (!packageName.equals("com.android.settings")) {
                return true;
            }
        }
        return false;
    }
}
