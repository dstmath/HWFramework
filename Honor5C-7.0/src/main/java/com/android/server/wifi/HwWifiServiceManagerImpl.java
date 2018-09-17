package com.android.server.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.SettingsEx.Systemex;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.WifiServiceImpl.LockList;
import com.android.server.wifi.p2p.HwWifiP2pService;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

public class HwWifiServiceManagerImpl implements HwWifiServiceManager {
    private static final String TAG = "HwWifiServiceManagerImpl";
    private static HwWifiServiceManager mInstance;
    private WifiStateMachine globalHwWsm;
    private HwSoftApManager mHwSoftApManager;
    private HwWifiP2pService mHwWifiP2PService;

    public HwWifiServiceManagerImpl() {
        this.mHwWifiP2PService = null;
    }

    static {
        mInstance = new HwWifiServiceManagerImpl();
    }

    public WifiServiceImpl createHwWifiService(Context context) {
        return new HwWifiService(context);
    }

    public WifiController createHwWifiController(Context context, WifiStateMachine wsm, WifiSettingsStore wss, LockList locks, Looper looper, FrameworkFacade f) {
        return new HwWifiController(context, wsm, wss, locks, looper, f);
    }

    public static HwWifiServiceManager getDefault() {
        return mInstance;
    }

    public boolean custApConfiguration(WifiApConfigStore s, WifiConfiguration config, Context context) {
        String custConf = Systemex.getString(context.getContentResolver(), "hw_softap_default");
        if (!(custConf == null || custConf.equals(""))) {
            String[] args = custConf.split(",");
            if (3 == args.length) {
                try {
                    if (args[0].equals(HwCHRWifiCPUUsage.COL_SEP)) {
                        config.SSID = context.getString(17040332);
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

    public WifiStateMachine createHwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode) {
        return new HwWifiStateMachine(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode);
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
            md5Str = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return md5Str;
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

    public WifiQualifiedNetworkSelector createHwWifiQualifiedNetworkSelector(WifiConfigManager configureStore, Context context, WifiInfo wifiInfo, Clock clock, WifiStateMachine wsm, WifiNative wifiNative) {
        this.globalHwWsm = wsm;
        return new HwWifiQualifiedNetworkSelector(configureStore, context, wifiInfo, clock, wsm, wifiNative);
    }

    public WifiStateMachine getGlobalHwWifiStateMachine() {
        return this.globalHwWsm;
    }

    public WifiConfigManager createHwWifiConfigManager(Context context, WifiNative wifiNative, FrameworkFacade frameworkFacade, Clock clock, UserManager userManager, KeyStore keyStore) {
        return new HwWifiConfigManager(context, wifiNative, frameworkFacade, clock, userManager, keyStore);
    }

    public WifiCountryCode createHwWifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, String persistentCountryCode, boolean revertCountryCodeOnCellularLoss) {
        return new HwWifiCountryCode(context, wifiNative, oemDefaultCountryCode, persistentCountryCode, revertCountryCodeOnCellularLoss);
    }

    public WifiConfigStore createHwWifiConfigStore(WifiNative wifiNative, KeyStore keyStore, LocalLog localLog, boolean showNetworks, boolean verboseDebug) {
        return new HwWifiConfigStore(wifiNative, keyStore, localLog, showNetworks, verboseDebug);
    }

    public SoftApManager createHwSoftApManager(Context context, Looper looper, WifiNative wifiNative, INetworkManagementService nmService, ConnectivityManager connectivityManager, String countryCode, ArrayList<Integer> allowed2GChannels, Listener listener) {
        if (this.mHwSoftApManager == null) {
            this.mHwSoftApManager = new HwSoftApManager(context, looper, wifiNative, nmService, connectivityManager, countryCode, allowed2GChannels, listener);
        }
        this.mHwSoftApManager.setCountryCode(countryCode);
        return this.mHwSoftApManager;
    }

    public WifiConnectivityManager createHwWifiConnectivityManager(Context context, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiQualifiedNetworkSelector qualifiedNetworkSelector, WifiInjector wifiInjector, Looper looper) {
        return new HwWifiConnectivityManager(context, stateMachine, scanner, configManager, wifiInfo, qualifiedNetworkSelector, wifiInjector, looper);
    }
}
