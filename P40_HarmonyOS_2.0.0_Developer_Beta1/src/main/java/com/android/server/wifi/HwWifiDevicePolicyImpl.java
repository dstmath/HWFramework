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
import com.android.server.wifi.hwUtil.StringUtilEx;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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
    private static HwWifiDevicePolicy mInstance = new HwWifiDevicePolicyImpl();
    private static ArrayList<String> mMDMsEnterpriseConfigList = new ArrayList<>();
    private static HashMap<ComponentName, ArrayList<String>> sGlobalSsidBlacklist = new HashMap<>();
    private boolean mAutoConnectDisabled = false;
    private boolean mAutoConnectPolicyInit = false;
    private boolean mBlackListInit = false;
    private Context mContext;
    private ArrayList<String> mGlobalSsidWhitelist = new ArrayList<>();
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
        Bundle bundleSecurityLevel = new HwDevicePolicyManagerEx().getPolicy((ComponentName) null, WIFI_SECURITYLEVEL);
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
        Context context = this.mContext;
        if (context == null) {
            HwLog.e(TAG, "mContext being null");
            return false;
        }
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        if (devicePolicyManager == null) {
            HwLog.e(TAG, "get device_policy service fail");
            return false;
        }
        List<ComponentName> activeApks = devicePolicyManager.getActiveAdmins();
        if (activeApks == null) {
            HwLog.e(TAG, "There are no active MDM apks installed");
            return true;
        }
        for (ComponentName apk : activeApks) {
            Bundle bundleBlackList = manager.getPolicy(apk, WIFI_BLACKLIST);
            if (bundleBlackList == null) {
                sGlobalSsidBlacklist.put(apk, new ArrayList<>());
            } else {
                ArrayList<String> blacklist = bundleBlackList.getStringArrayList(KEY_VALUE);
                if (blacklist == null) {
                    sGlobalSsidBlacklist.put(apk, new ArrayList<>());
                } else {
                    sGlobalSsidBlacklist.put(apk, removeRepetitiveItem(blacklist));
                }
            }
        }
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
            this.mGlobalSsidWhitelist = new ArrayList<>();
        }
        this.mWhiteListInit = true;
    }

    private boolean isSSIDInWhiteList(String ssid, boolean isShowToast) {
        boolean inWhiteList = false;
        ArrayList<String> arrayList = this.mGlobalSsidWhitelist;
        if (arrayList != null && arrayList.contains(ssid)) {
            HwLog.i(TAG, "isSSIDInWhiteList(): " + StringUtilEx.safeDisplaySsid(ssid) + " is in MDM WiFi WhiteList");
            inWhiteList = true;
        }
        if (!inWhiteList && isShowToast) {
            showToast(0);
        }
        return inWhiteList;
    }

    private Bundle getWifiPolicyData(String policyName) {
        return new HwDevicePolicyManagerEx().getPolicy((ComponentName) null, policyName);
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
            mMDMsEnterpriseConfigList = new ArrayList<>();
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
        HwLog.i(TAG, "initSecurityLevelPolicy(): get mSecurityLevel = " + this.mSecurityLevel);
        this.mSecurityLevelInit = true;
    }

    private void initBlackListPolicy() {
        this.mBlackListInit = getBlackListPolicy();
    }

    public static HwWifiDevicePolicy getDefault() {
        return mInstance;
    }

    private static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return 3;
        }
        if (config.allowedKeyManagement.get(16) || config.allowedKeyManagement.get(18)) {
            return 4;
        }
        if (config.allowedKeyManagement.get(17) || config.allowedKeyManagement.get(19)) {
            return 5;
        }
        if (config.wepKeys[0] != null) {
            return 1;
        }
        return 0;
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
        if (getSecurity(config) != 3) {
            return false;
        }
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
        return false;
    }

    private boolean isSecurityLevel5(WifiConfiguration config) {
        if (getSecurity(config) == 3 && config.enterpriseConfig.getEapMethod() == 0) {
            return true;
        }
        return false;
    }

    private boolean isSecurityLevel6(WifiConfiguration config) {
        int eapMethod = config.enterpriseConfig.getEapMethod();
        if (eapMethod == 1 || eapMethod == 2 || eapMethod == 4 || eapMethod == 5 || eapMethod == 6) {
            return true;
        }
        return false;
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
        Context context = this.mContext;
        if (context == null) {
            HwLog.e(TAG, "showToast: mContext is null, can't show toast");
        } else if (toastType == 0) {
            Toast.makeText(context, context.getString(33686010), 0).show();
        } else if (toastType != 1) {
            HwLog.e(TAG, "showToast(): unknow toastType " + toastType);
        } else {
            Toast.makeText(context, context.getString(33685942), 0).show();
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
        HwLog.i(TAG, "ap " + StringUtilEx.safeDisplaySsid(config.SSID) + " level is " + wifiLevel + " service level is " + this.mSecurityLevel);
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
            Iterator<Map.Entry<ComponentName, ArrayList<String>>> it = sGlobalSsidBlacklist.entrySet().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ArrayList<String> list = it.next().getValue();
                if (list != null) {
                    if (list.contains(ssid)) {
                        result = true;
                        break;
                    }
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
            } else if (!config.isEnterprise() || !isSSIDInMDMEnterpriseConfigList(trimSsid(config.SSID))) {
                HwLog.i(TAG, "get network disable policy data: true");
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
        ArrayList<String> arrayList = mMDMsEnterpriseConfigList;
        if (arrayList == null || !arrayList.contains(ssid)) {
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
            if (config == null || isSSIDInWhiteList(trimSsid(config.SSID), isShowToast)) {
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
        if (ssid == null || !ssid.startsWith("\"") || !ssid.endsWith("\"")) {
            return ssid;
        }
        return ssid.substring(1, ssid.length() - 1);
    }

    private WifiConfiguration getWifiConfigurationForNetworkId(int networkId) {
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs == null) {
            return null;
        }
        for (WifiConfiguration config : configs) {
            if (networkId == config.networkId) {
                return config;
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
        } else if (isWifiRestricted(currentNetwork, true)) {
            HwLog.i(TAG, "RestrictedBySecurityPolicy, disconnect wifi -> " + StringUtilEx.safeDisplaySsid(connectionInfo.getSSID()));
            this.mWifiManager.disconnect();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void procBroadCasts(Intent intent) {
        HwLog.d(TAG, "registerBroadcasts");
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        Bundle bundle = intent.getBundleExtra(KEY_WIFI_POLICY_DATA);
        boolean z = false;
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
                break;
            case 2:
                if (bundle == null) {
                    if (1 == changeMode || 2 == changeMode) {
                        HwLog.e(TAG, "procBroadCasts(): OP_RESTRICT_NETWORK, bundle is null!");
                        this.mRestrictNetwork = false;
                        break;
                    }
                } else {
                    if (changeMode == 0) {
                        z = bundle.getBoolean(VALUE_DISABLE);
                    }
                    this.mRestrictNetwork = z;
                    break;
                }
            case 3:
                if (mMDMsEnterpriseConfigList == null) {
                    mMDMsEnterpriseConfigList = new ArrayList<>();
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
                            } else {
                                mMDMsEnterpriseConfigList.addAll(changeList);
                                break;
                            }
                        } else {
                            HwLog.e(TAG, "procBroadCasts(): OP_ENTERPRISE_CONFIG_LIST, changeList is null!");
                            break;
                        }
                    } else {
                        HwLog.e(TAG, "procBroadCasts(): OP_ENTERPRISE_CONFIG_LIST, bundle is null!");
                        break;
                    }
                } else {
                    mMDMsEnterpriseConfigList.clear();
                    break;
                }
            case 4:
                if (bundle == null) {
                    if (1 == changeMode || 2 == changeMode) {
                        HwLog.e(TAG, "procBroadCasts(): OP_AUTO_CONNECT, bundle is null!");
                        this.mAutoConnectDisabled = false;
                        break;
                    }
                } else {
                    if (changeMode == 0) {
                        z = bundle.getBoolean(VALUE_DISABLE);
                    }
                    this.mAutoConnectDisabled = z;
                    break;
                }
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
                    this.mGlobalSsidWhitelist = new ArrayList<>();
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
                            } else {
                                this.mGlobalSsidWhitelist.addAll(modifyList);
                                break;
                            }
                        } else {
                            HwLog.e(TAG, "procBroadCasts(): OP_WHITELISTE, modifyList is null!");
                            break;
                        }
                    } else {
                        HwLog.e(TAG, "procBroadCasts(): OP_WHITELISTE, bundle is null!");
                        break;
                    }
                } else {
                    this.mGlobalSsidWhitelist.clear();
                    break;
                }
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
            /* class com.android.server.wifi.HwWifiDevicePolicyImpl.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwWifiDevicePolicyImpl.this.procBroadCasts(intent);
            }
        }, intentFilter);
    }

    private void procBlacklist(int mode, String packageName, String className, ArrayList<String> increment) {
        synchronized (this) {
            for (Map.Entry<ComponentName, ArrayList<String>> entry : sGlobalSsidBlacklist.entrySet()) {
                ComponentName component = entry.getKey();
                if (component.getPackageName().equals(packageName) && component.getClassName().equals(className)) {
                    ArrayList<String> list = entry.getValue();
                    if (list == null) {
                        list = new ArrayList<>();
                        sGlobalSsidBlacklist.put(component, list);
                    }
                    if (mode == 0) {
                        list.addAll(increment);
                    } else if (mode == 1) {
                        list.removeAll(increment);
                    } else if (mode == 2) {
                        list.clear();
                    }
                    sGlobalSsidBlacklist.put(component, removeRepetitiveItem(list));
                    return;
                }
            }
            if (1 != 0) {
                HwLog.d(TAG, "New active admin here, add it to local copy");
                ComponentName newComponent = new ComponentName(packageName, className);
                if (mode == 0) {
                    sGlobalSsidBlacklist.put(newComponent, removeRepetitiveItem(increment));
                } else if (mode == 1 || mode == 2) {
                    sGlobalSsidBlacklist.put(newComponent, new ArrayList<>());
                }
            }
        }
    }

    private ArrayList<String> removeRepetitiveItem(ArrayList<String> list) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.addAll(list);
        ArrayList<String> newList = new ArrayList<>();
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
