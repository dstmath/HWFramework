package com.android.server.wifi;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Toast;
import com.android.server.devicepolicy.HwLog;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

public class HwWifiDevicePolicyImpl implements HwWifiDevicePolicy {
    private static final String ACTION_WIFI_DEVICE_POLICY = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final int CLEAR_MODE = 2;
    private static final int DEFAULT_EXTRA = 0;
    private static final int DEFAULT_TOAST_TYPE = -1;
    private static final String KEY_BLACK_LIST_CHANGE_MODE = "set_or_remove";
    private static final String KEY_BLACK_LIST_SSID = "ssid-blacklist";
    private static final String KEY_CLASS_NAME = "class_name";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String KEY_SECURE_LEVEL = "securelevel";
    private static final String KEY_SECURITY_LEVEL = "securitylevel";
    private static final String KEY_VALUE = "value";
    private static final String KEY_WIFI_POLICY_CATEGORY = "wifi_policy_category";
    private static final String KEY_WIFI_POLICY_CHANGE_MODE = "change_mode";
    private static final String KEY_WIFI_POLICY_DATA = "wifi_policy_data";
    private static final int OP_AUTO_CONNECT = 4;
    private static final int OP_BLACKLIST = 1;
    private static final int OP_BLACKLIST_ADD = 0;
    private static final int OP_BLACKLIST_CLEAR = 2;
    private static final int OP_BLACKLIST_REMOVE = 1;
    private static final int OP_ENTERPRISE_CONFIG_LIST = 3;
    private static final int OP_OPEN_HOTSPOT = 5;
    private static final int OP_RESTRICT_NETWORK = 2;
    private static final int OP_SECURITY_LEVEL = 0;
    private static final int OP_WHITELIST = 6;
    private static final String POLICY_AUTO_CONNECT = "policy-auto-connect";
    private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
    private static final String POLICY_RESTRICT_NETWORK = "policy-restrict-network";
    private static final String PROFILE_ENTERPRISE_CONFIG = "profile-enterprise-config";
    private static final int REMOVE_MODE = 1;
    private static final int SECURITY_EAP = 3;
    private static final int SECURITY_LEVEL_1 = 1;
    private static final int SECURITY_LEVEL_2 = 2;
    private static final int SECURITY_LEVEL_3 = 3;
    private static final int SECURITY_LEVEL_4 = 4;
    private static final int SECURITY_LEVEL_5 = 5;
    private static final int SECURITY_LEVEL_6 = 6;
    private static final int SECURITY_NONE = 0;
    private static final int SECURITY_PSK = 2;
    private static final int SECURITY_WAPI_CERT = 5;
    private static final int SECURITY_WAPI_PSK = 4;
    private static final int SECURITY_WEP = 1;
    private static final int SET_MODE = 0;
    private static final String TAG = "HwWifiDevicePolicyImpl";
    private static final int TOAST_DENY_OPEN_HOTSPOT = 1;
    private static final int TOAST_RESTRICT_CONNECT = 0;
    private static final String VALUE_DISABLE = "value_disable";
    private static final String VALUE_STRING_LIST = "value_string_list";
    private static final String WIFI_BLACKLIST = "wifi-ssid-blacklist";
    private static final String WIFI_BLACKLIST_ITEM = "wifi-ssid-blacklist/wifi-ssid-blacklist-item";
    private static final String WIFI_SECURITYLEVEL = "wifi-securitylevel";
    private static final String WIFI_WHITELIST = "wifi-ssid-whitelist";
    private static final String WIFI_WHITELIST_ITEM = "wifi-ssid-whitelist/wifi-ssid-whitelist-item";
    private static HashMap<ComponentName, ArrayList<String>> mGlobalSsidBlacklist = new HashMap();
    private static HwWifiDevicePolicy mInstance = new HwWifiDevicePolicyImpl();
    private static ArrayList<String> mMDMsEnterpriseConfigList = new ArrayList();
    private boolean mAutoConnectDisabled = false;
    private boolean mAutoConnectPolicyInit = false;
    private boolean mBlackListInit = false;
    private Context mContext;
    private ArrayList<String> mGlobalSsidWhitelist = new ArrayList();
    private boolean mMDMsEnterpriseConfigListInit = false;
    private boolean mOpenHotspotDisabled = false;
    private boolean mOpenHotspotPolicyInit = false;
    private boolean mRestrictNetwork = false;
    private boolean mRestrictNetworkPolicyInit = false;
    private int mSecurityLevel = 1;
    private boolean mSecurityLevelInit = false;
    private boolean mWhiteListInit = false;
    private WifiManager mWifiManager;

    private boolean getSecuritLevelPolicy() {
        Bundle bundleSecurityLevel = new HwDevicePolicyManagerEx().getPolicy(null, WIFI_SECURITYLEVEL);
        if (bundleSecurityLevel == null) {
            HwLog.e(TAG, "Init err bundleBlackList is null !");
            return false;
        }
        String LevelString = bundleSecurityLevel.getString(KEY_SECURITY_LEVEL);
        HwLog.i(TAG, "securitylevel " + this.mSecurityLevel + " to " + LevelString);
        if (LevelString == null) {
            HwLog.e(TAG, "LevelString within bundle is null");
            this.mSecurityLevel = 1;
        } else {
            try {
                this.mSecurityLevel = Integer.parseInt(LevelString);
            } catch (Exception e) {
                this.mSecurityLevel = 1;
            }
        }
        return true;
    }

    private boolean getBlackListPolicy() {
        HwDevicePolicyManagerEx manager = new HwDevicePolicyManagerEx();
        if (this.mContext == null) {
            HwLog.e(TAG, "mContext being null");
            return false;
        }
        List<ComponentName> activeApks = ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).getActiveAdmins();
        if (activeApks == null) {
            HwLog.e(TAG, "There are no active MDM apks installed");
            return true;
        }
        for (ComponentName apk : activeApks) {
            Bundle bundleBlackList = manager.getPolicy(apk, WIFI_BLACKLIST);
            if (bundleBlackList == null) {
                mGlobalSsidBlacklist.put(apk, new ArrayList());
            } else {
                ArrayList<String> blacklist = bundleBlackList.getStringArrayList(KEY_VALUE);
                if (blacklist == null) {
                    mGlobalSsidBlacklist.put(apk, new ArrayList());
                } else {
                    mGlobalSsidBlacklist.put(apk, removeRepetitiveItem(blacklist));
                }
            }
        }
        printBlacklist();
        return true;
    }

    private void initWhiteListPolicy() {
        Bundle bundle = getWifiPolicyData(WIFI_WHITELIST);
        if (bundle != null) {
            try {
                this.mGlobalSsidWhitelist = bundle.getStringArrayList(KEY_VALUE);
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "get mGlobalSsidWhitelist exception: " + e.getMessage());
            }
        }
        if (this.mGlobalSsidWhitelist == null) {
            this.mGlobalSsidWhitelist = new ArrayList();
        }
        this.mWhiteListInit = true;
    }

    private boolean isSSIDInWhiteList(String ssid, boolean isShowToast) {
        boolean inWhiteList = false;
        if (this.mGlobalSsidWhitelist != null && this.mGlobalSsidWhitelist.contains(ssid)) {
            HwLog.d(TAG, "isSSIDInWhiteList(): " + ssid + " is in MDM WiFi WhiteList");
            inWhiteList = true;
        }
        if (!inWhiteList && isShowToast) {
            showToast(0);
        }
        return inWhiteList;
    }

    private Bundle getWifiPolicyData(String policyName) {
        return new HwDevicePolicyManagerEx().getPolicy(null, policyName);
    }

    private void initNetworkRestrictPolicy() {
        Bundle bundle = getWifiPolicyData(POLICY_RESTRICT_NETWORK);
        if (bundle != null) {
            this.mRestrictNetwork = bundle.getBoolean(VALUE_DISABLE);
        }
        this.mRestrictNetworkPolicyInit = true;
    }

    private void initEnterpriseConfigPolicy() {
        Bundle bundle = getWifiPolicyData(PROFILE_ENTERPRISE_CONFIG);
        if (bundle != null) {
            try {
                mMDMsEnterpriseConfigList = bundle.getStringArrayList(VALUE_STRING_LIST);
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "get mMDMsEnterpriseConfigList exception: " + e.getMessage());
            }
        }
        if (mMDMsEnterpriseConfigList == null) {
            mMDMsEnterpriseConfigList = new ArrayList();
        }
        this.mMDMsEnterpriseConfigListInit = true;
    }

    private void initAutoConnectPolicy() {
        Bundle bundle = getWifiPolicyData(POLICY_AUTO_CONNECT);
        if (bundle != null) {
            this.mAutoConnectDisabled = bundle.getBoolean(VALUE_DISABLE);
        }
        this.mAutoConnectPolicyInit = true;
    }

    private void initOpenHotspotPolicy() {
        Bundle bundle = getWifiPolicyData(POLICY_OPEN_HOTSPOT);
        if (bundle != null) {
            this.mOpenHotspotDisabled = bundle.getBoolean(VALUE_DISABLE);
        }
        this.mOpenHotspotPolicyInit = true;
    }

    private void initSecurityLevelPolicy() {
        getSecuritLevelPolicy();
        HwLog.d(TAG, "initSecurityLevelPolicy(): get mSecurityLevel = " + this.mSecurityLevel);
        this.mSecurityLevelInit = true;
    }

    private void initBlackListPolicy() {
        getBlackListPolicy();
        this.mBlackListInit = true;
    }

    public static HwWifiDevicePolicy getDefault() {
        return mInstance;
    }

    private static int getSecurity(WifiConfiguration config) {
        int i = 1;
        if (config.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return 3;
        }
        if (config.allowedKeyManagement.get(8) || config.allowedKeyManagement.get(10)) {
            return 4;
        }
        if (config.allowedKeyManagement.get(9) || config.allowedKeyManagement.get(11)) {
            return 5;
        }
        if (config.wepKeys[0] == null) {
            i = 0;
        }
        return i;
    }

    private boolean isSecurityLevel1(WifiConfiguration config) {
        if (getSecurity(config) == 0) {
            return true;
        }
        return false;
    }

    private boolean isSecurityLevel2(WifiConfiguration config) {
        if (getSecurity(config) == 1) {
            return true;
        }
        return false;
    }

    private boolean isSecurityLevel3(WifiConfiguration config) {
        if (getSecurity(config) == 2) {
            return true;
        }
        return false;
    }

    private boolean isSecurityLevel4(WifiConfiguration config) {
        if (getSecurity(config) == 3) {
            if (config.allowedAuthAlgorithms != null) {
                BitSet allowedAuthAlgorithms = new BitSet();
                allowedAuthAlgorithms.set(2);
                if (config.allowedAuthAlgorithms.equals(allowedAuthAlgorithms)) {
                    return true;
                }
            }
            if (config.enterpriseConfig.getEapMethod() == 3) {
                return true;
            }
        }
        return false;
    }

    private boolean isSecurityLevel5(WifiConfiguration config) {
        if (getSecurity(config) == 3 && config.enterpriseConfig.getEapMethod() == 0) {
            return true;
        }
        return false;
    }

    private boolean isSecurityLevel6(WifiConfiguration config) {
        switch (config.enterpriseConfig.getEapMethod()) {
            case 1:
            case 2:
            case 4:
            case 5:
            case 6:
                return true;
            default:
                return false;
        }
    }

    private int getSecurityLevel(WifiConfiguration config) {
        if (isSecurityLevel1(config)) {
            return 1;
        }
        if (isSecurityLevel2(config)) {
            return 2;
        }
        if (isSecurityLevel3(config)) {
            return 3;
        }
        if (isSecurityLevel4(config)) {
            return 4;
        }
        if (isSecurityLevel5(config)) {
            return 5;
        }
        return isSecurityLevel6(config) ? 6 : 6;
    }

    private void showToast(int toastType) {
        if (this.mContext == null) {
            HwLog.e(TAG, "showToast: mContext is null, can't show toast");
            return;
        }
        switch (toastType) {
            case 0:
                Toast.makeText(this.mContext, this.mContext.getString(33686010), 0).show();
                break;
            case 1:
                Toast.makeText(this.mContext, this.mContext.getString(33685942), 0).show();
                break;
            default:
                HwLog.e(TAG, "showToast(): unknow toastType " + toastType);
                break;
        }
    }

    private boolean isWifiSecure(WifiConfiguration config, boolean isShowToast) {
        if (config == null) {
            HwLog.e(TAG, "isWifiSecure(): config is null");
            return false;
        }
        if (!this.mSecurityLevelInit) {
            initSecurityLevelPolicy();
        }
        int wifiLevel = getSecurityLevel(config);
        HwLog.i(TAG, "ap " + config.SSID + " level is " + wifiLevel + " service level is " + this.mSecurityLevel);
        if (wifiLevel >= this.mSecurityLevel) {
            return true;
        }
        if (isShowToast) {
            showToast(0);
        }
        return false;
    }

    public boolean isSSIDOnBlackList(String ssid, boolean isToastNeeded) {
        if (!this.mBlackListInit) {
            initBlackListPolicy();
        }
        boolean result = false;
        HwLog.d(TAG, "isSSIDOnBlackList");
        synchronized (this) {
            for (Entry<ComponentName, ArrayList<String>> entry : mGlobalSsidBlacklist.entrySet()) {
                ArrayList<String> list = (ArrayList) entry.getValue();
                if (list != null && list.contains(ssid)) {
                    result = true;
                    break;
                }
            }
        }
        if (result && isToastNeeded) {
            showToast(0);
        }
        return result;
    }

    public boolean isUserProfilesRestricted(WifiConfiguration config, boolean isShowToast) {
        if (!this.mRestrictNetworkPolicyInit) {
            initNetworkRestrictPolicy();
        }
        if (this.mRestrictNetwork) {
            if (config == null) {
                HwLog.e(TAG, "isUserProfilesRestricted(): config is null!");
                return true;
            } else if (!(config.isEnterprise() && (isSSIDInMDMEnterpriseConfigList(trimSsid(config.SSID)) ^ 1) == 0)) {
                HwLog.d(TAG, "get network disable policy data: true");
                if (isShowToast) {
                    showToast(0);
                }
                return true;
            }
        }
        return false;
    }

    private boolean isSSIDInMDMEnterpriseConfigList(String ssid) {
        if (!this.mMDMsEnterpriseConfigListInit) {
            initEnterpriseConfigPolicy();
        }
        if (mMDMsEnterpriseConfigList == null || !mMDMsEnterpriseConfigList.contains(ssid)) {
            return false;
        }
        HwLog.d(TAG, "isSSIDInMDMEnterpriseConfigList(): " + ssid + " is in MDM enterprise list");
        return true;
    }

    public boolean isWifiRestricted(WifiConfiguration config, boolean isShowToast) {
        if (!this.mWhiteListInit) {
            initWhiteListPolicy();
        }
        HwLog.d(TAG, " isWifiRestricted, mGlobalSsidWhitelist =" + this.mGlobalSsidWhitelist);
        if (this.mGlobalSsidWhitelist.size() > 0) {
            if (config == null || (isSSIDInWhiteList(trimSsid(config.SSID), isShowToast) ^ 1) == 0) {
                return false;
            }
            HwLog.d(TAG, "isSSIDInWhiteList(): false");
            return true;
        } else if (!isWifiSecure(config, isShowToast)) {
            return true;
        } else {
            if (config != null && isSSIDOnBlackList(trimSsid(config.SSID), isShowToast)) {
                return true;
            }
            if (!isUserProfilesRestricted(config, isShowToast)) {
                return false;
            }
            HwLog.d(TAG, "isUserProfilesRestricted(): true");
            return true;
        }
    }

    private String trimSsid(String ssid) {
        if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    private WifiConfiguration getWifiConfigurationForNetworkId(int networkId) {
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (networkId == config.networkId) {
                    return config;
                }
            }
        }
        return null;
    }

    private void procConnectedWifi() {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        WifiInfo connectionInfo = this.mWifiManager.getConnectionInfo();
        if (connectionInfo == null) {
            HwLog.e(TAG, "wifiInfo being null");
            return;
        }
        WifiConfiguration currentNetwork = getWifiConfigurationForNetworkId(connectionInfo.getNetworkId());
        if (currentNetwork == null) {
            HwLog.e(TAG, "currentNetwork being null");
            return;
        }
        if (isWifiRestricted(currentNetwork, true)) {
            HwLog.i(TAG, "RestrictedBySecurityPolicy, disconnect wifi -> " + connectionInfo.getSSID());
            this.mWifiManager.disconnect();
        }
    }

    private void procBroadCasts(Intent intent) {
        boolean z = false;
        HwLog.d(TAG, "registerBroadcasts");
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        Bundle bundle = intent.getBundleExtra(KEY_WIFI_POLICY_DATA);
        int changeMode = intent.getIntExtra(KEY_WIFI_POLICY_CHANGE_MODE, 0);
        switch (intent.getIntExtra(KEY_WIFI_POLICY_CATEGORY, 0)) {
            case 0:
                int level = intent.getIntExtra(KEY_SECURE_LEVEL, 1);
                HwLog.i(TAG, "old level is " + this.mSecurityLevel + " new level is " + level);
                this.mSecurityLevel = level;
                break;
            case 1:
                int mode = intent.getIntExtra(KEY_BLACK_LIST_CHANGE_MODE, 0);
                String packageName = intent.getStringExtra(KEY_PACKAGE_NAME);
                String className = intent.getStringExtra(KEY_CLASS_NAME);
                ArrayList<String> listFromIntent = intent.getStringArrayListExtra(KEY_BLACK_LIST_SSID);
                HwLog.i(TAG, "packageName is " + packageName + ", className is " + className);
                procBlacklist(mode, packageName, className, listFromIntent);
                printBlacklist();
                break;
            case 2:
                if (bundle == null) {
                    if (1 == changeMode || 2 == changeMode) {
                        HwLog.e(TAG, "procBroadCasts(): OP_RESTRICT_NETWORK, bundle is null!");
                        this.mRestrictNetwork = false;
                        break;
                    }
                }
                this.mRestrictNetwork = changeMode == 0 ? bundle.getBoolean(VALUE_DISABLE) : false;
                break;
            case 3:
                if (mMDMsEnterpriseConfigList == null) {
                    mMDMsEnterpriseConfigList = new ArrayList();
                }
                if (2 != changeMode) {
                    if (bundle != null) {
                        ArrayList<String> changeList = bundle.getStringArrayList(VALUE_STRING_LIST);
                        if (changeList != null) {
                            if (changeMode != 0) {
                                if (1 != changeMode) {
                                    HwLog.e(TAG, "procBroadCasts(): OP_ENTERPRISE_CONFIG_LIST, unknown changeMode=" + changeMode);
                                    break;
                                } else {
                                    mMDMsEnterpriseConfigList.removeAll(changeList);
                                    break;
                                }
                            }
                            mMDMsEnterpriseConfigList.addAll(changeList);
                            break;
                        }
                        HwLog.e(TAG, "procBroadCasts(): OP_ENTERPRISE_CONFIG_LIST, changeList is null!");
                        break;
                    }
                    HwLog.e(TAG, "procBroadCasts(): OP_ENTERPRISE_CONFIG_LIST, bundle is null!");
                    break;
                }
                mMDMsEnterpriseConfigList.clear();
                break;
            case 4:
                if (bundle == null) {
                    if (1 == changeMode || 2 == changeMode) {
                        HwLog.e(TAG, "procBroadCasts(): OP_AUTO_CONNECT, bundle is null!");
                        this.mAutoConnectDisabled = false;
                        break;
                    }
                }
                if (changeMode == 0) {
                    z = bundle.getBoolean(VALUE_DISABLE);
                }
                this.mAutoConnectDisabled = z;
                break;
            case 5:
                if (bundle != null) {
                    if (changeMode == 0) {
                        z = bundle.getBoolean(VALUE_DISABLE);
                    }
                    this.mOpenHotspotDisabled = z;
                } else if (1 == changeMode || 2 == changeMode) {
                    HwLog.e(TAG, "procBroadCasts(): OP_OPEN_HOTSPOT, bundle is null!");
                    this.mOpenHotspotDisabled = false;
                }
                procOpenHotspot();
                break;
            case 6:
                if (this.mGlobalSsidWhitelist == null) {
                    this.mGlobalSsidWhitelist = new ArrayList();
                }
                if (2 != changeMode) {
                    if (bundle != null) {
                        ArrayList<String> modifyList = bundle.getStringArrayList(KEY_VALUE);
                        if (modifyList != null) {
                            if (changeMode != 0) {
                                if (1 != changeMode) {
                                    HwLog.e(TAG, "procBroadCasts(): OP_WHITELISTE, unknown changeMode=" + changeMode);
                                    break;
                                } else {
                                    this.mGlobalSsidWhitelist.removeAll(modifyList);
                                    break;
                                }
                            }
                            this.mGlobalSsidWhitelist.addAll(modifyList);
                            break;
                        }
                        HwLog.e(TAG, "procBroadCasts(): OP_WHITELISTE, modifyList is null!");
                        break;
                    }
                    HwLog.e(TAG, "procBroadCasts(): OP_WHITELISTE, bundle is null!");
                    break;
                }
                this.mGlobalSsidWhitelist.clear();
                break;
            default:
                HwLog.e(TAG, "procBroadCasts(): unknow policy operate intent!");
                break;
        }
        procConnectedWifi();
    }

    public void registerBroadcasts(Context regContext) {
        this.mContext = regContext;
        this.mWifiManager = null;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_WIFI_DEVICE_POLICY);
        regContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                HwWifiDevicePolicyImpl.this.procBroadCasts(intent);
            }
        }, intentFilter);
    }

    /* JADX WARNING: Missing block: B:29:0x0076, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void procBlacklist(int mode, String packageName, String className, ArrayList<String> increment) {
        synchronized (this) {
            for (Entry<ComponentName, ArrayList<String>> entry : mGlobalSsidBlacklist.entrySet()) {
                ComponentName component = (ComponentName) entry.getKey();
                if (component.getPackageName().equals(packageName) && component.getClassName().equals(className)) {
                    ArrayList<String> list = (ArrayList) entry.getValue();
                    if (list == null) {
                        list = new ArrayList();
                        mGlobalSsidBlacklist.put(component, list);
                    }
                    switch (mode) {
                        case 0:
                            list.addAll(increment);
                            break;
                        case 1:
                            list.removeAll(increment);
                            break;
                        case 2:
                            list.clear();
                            break;
                    }
                    mGlobalSsidBlacklist.put(component, removeRepetitiveItem(list));
                    return;
                }
            }
            if (true) {
                HwLog.d(TAG, "New active admin here, add it to local copy");
                ComponentName newComponent = new ComponentName(packageName, className);
                switch (mode) {
                    case 0:
                        mGlobalSsidBlacklist.put(newComponent, removeRepetitiveItem(increment));
                        break;
                    case 1:
                    case 2:
                        mGlobalSsidBlacklist.put(newComponent, new ArrayList());
                        break;
                }
            }
        }
    }

    private void printBlacklist() {
        synchronized (this) {
            for (Entry<ComponentName, ArrayList<String>> entry : mGlobalSsidBlacklist.entrySet()) {
                ComponentName component = (ComponentName) entry.getKey();
                ArrayList<String> list = (ArrayList) entry.getValue();
                HwLog.d(TAG, component.getPackageName() + "" + component.getClassName());
                if (list != null) {
                    for (String str : list) {
                        HwLog.d(TAG, "->" + str);
                    }
                }
            }
        }
    }

    private ArrayList<String> removeRepetitiveItem(ArrayList<String> list) {
        LinkedHashSet<String> set = new LinkedHashSet();
        set.addAll(list);
        ArrayList<String> newList = new ArrayList();
        newList.addAll(set);
        return newList;
    }

    public boolean isAutoConnectDisabled(boolean isShowToast) {
        if (!this.mAutoConnectPolicyInit) {
            initAutoConnectPolicy();
        }
        if (isShowToast) {
            showToast(-1);
        }
        return this.mAutoConnectDisabled;
    }

    public boolean isOpenHotspotDisabled(boolean isShowToast) {
        if (!this.mOpenHotspotPolicyInit) {
            initOpenHotspotPolicy();
        }
        if (!this.mOpenHotspotDisabled) {
            return false;
        }
        HwLog.d(TAG, "get open hotspot disabled: true");
        if (isShowToast) {
            showToast(1);
        }
        return true;
    }

    private void procOpenHotspot() {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        WifiConfiguration wifiConfig = this.mWifiManager.getWifiApConfiguration();
        if (!this.mWifiManager.isWifiApEnabled()) {
            return;
        }
        if ((wifiConfig == null || wifiConfig.preSharedKey == null) && isOpenHotspotDisabled(true)) {
            this.mWifiManager.stopSoftAp();
        }
    }
}
