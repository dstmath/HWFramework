package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.provider.Settings;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeviceWifiPlugin extends DevicePolicyPlugin {
    private static final String ACTION_WIFI_DEVICE_POLICY = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final int CLEAR_MODE = 2;
    private static final int DISABLE_STATE = 0;
    private static final String ENTERPRISE_CONFIG_ITEM = "profile-enterprise-config/enterprise-config-item";
    private static final String KEY_BLACK_LIST_CHANGE_MODE = "set_or_remove";
    private static final String KEY_BLACK_LIST_SSID = "ssid-blacklist";
    private static final String KEY_CLASS_NAME = "class_name";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String KEY_SECURE_LEVEL = "securelevel";
    private static final String KEY_SECURITY_LEVEL = "securitylevel";
    private static final String KEY_SMART_NETWORK_SWITCHING = "smart_network_switching";
    private static final String KEY_VALUE = "value";
    private static final String KEY_WIFI_POLICY_CATEGORY = "wifi_policy_category";
    private static final String KEY_WIFI_POLICY_CHANGE_MODE = "change_mode";
    private static final String KEY_WIFI_POLICY_DATA = "wifi_policy_data";
    private static final String MDM_WIFI_PERMISSION = "com.huawei.permission.sec.MDM_WIFI";
    private static final int OP_AUTO_CONNECT = 4;
    private static final int OP_BLACKLIST = 1;
    private static final int OP_BLACKLIST_ADD = 0;
    private static final int OP_BLACKLIST_CLEAR = 2;
    private static final int OP_BLACKLIST_REMOVE = 1;
    private static final int OP_ENTERPRISE_CONFIG_LIST = 3;
    private static final int OP_OPEN_HOTSPOT = 5;
    private static final int OP_RESTRICT_NETWORK = 2;
    private static final int OP_SECURITY_LEVEL = 0;
    private static final int OP_UNAVAILABLE_SSID_LIST = 7;
    private static final int OP_WHITELIST = 6;
    private static final String PACKAGE_ANDROID = "android";
    private static final String POLICY_AUTO_CONNECT = "policy-auto-connect";
    private static final String POLICY_DISABLE_WIFIPRO = "policy-disable-wifipro";
    private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
    private static final String POLICY_RESTRICT_NETWORK = "policy-restrict-network";
    private static final String PROFILE_ENTERPRISE_CONFIG = "profile-enterprise-config";
    private static final String REMOVE_CONFIGURED_NETWORKLIST = "remove-configured-networklist";
    private static final String REMOVE_CONFIGURED_NETWORKLIST_ITEM = "remove-configured-networklist/remove-configured-networklist-item";
    private static final int REMOVE_MODE = 1;
    private static final int SECURITY_LEVEL_1 = 1;
    private static final int SET_MODE = 0;
    private static final String TAG = DeviceWifiPlugin.class.getSimpleName();
    private static final String UNAVAILABLE_SSID_LIST = "unavailable-ssid-list";
    private static final String UNAVAILABLE_SSID_LIST_ITEM = "unavailable-ssid-list/unavailable-ssid-list-item";
    private static final String VALUE_DISABLE = "value_disable";
    private static final String VALUE_STRING_LIST = "value_string_list";
    private static final String WIFI_BLACKLIST = "wifi-ssid-blacklist";
    private static final String WIFI_BLACKLIST_ITEM = "wifi-ssid-blacklist/wifi-ssid-blacklist-item";
    private static final String WIFI_SECURITYLEVEL = "wifi-securitylevel";
    private static final String WIFI_WHITELIST = "wifi-ssid-whitelist";
    private static final String WIFI_WHITELIST_ITEM = "wifi-ssid-whitelist/wifi-ssid-whitelist-item";

    public DeviceWifiPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(WIFI_SECURITYLEVEL, PolicyStruct.PolicyType.CONFIGURATION, new String[]{KEY_SECURITY_LEVEL});
        struct.addStruct(WIFI_BLACKLIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(WIFI_BLACKLIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(WIFI_WHITELIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(WIFI_WHITELIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(POLICY_RESTRICT_NETWORK, PolicyStruct.PolicyType.STATE, new String[]{VALUE_DISABLE});
        struct.addStruct(PROFILE_ENTERPRISE_CONFIG, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(ENTERPRISE_CONFIG_ITEM, PolicyStruct.PolicyType.LIST, new String[]{VALUE_STRING_LIST});
        struct.addStruct(POLICY_AUTO_CONNECT, PolicyStruct.PolicyType.STATE, new String[]{VALUE_DISABLE});
        struct.addStruct(POLICY_OPEN_HOTSPOT, PolicyStruct.PolicyType.STATE, new String[]{VALUE_DISABLE});
        struct.addStruct(UNAVAILABLE_SSID_LIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(UNAVAILABLE_SSID_LIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(REMOVE_CONFIGURED_NETWORKLIST, PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct(REMOVE_CONFIGURED_NETWORKLIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(POLICY_DISABLE_WIFIPRO, PolicyStruct.PolicyType.STATE, new String[]{VALUE_DISABLE});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.d(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission(MDM_WIFI_PERMISSION, "need permission com.huawei.permission.sec.MDM_WIFI");
        return true;
    }

    private void sendSecurityLevel(int level) {
        Intent intent = new Intent();
        intent.setAction(ACTION_WIFI_DEVICE_POLICY);
        intent.putExtra(KEY_WIFI_POLICY_CATEGORY, 0);
        intent.putExtra(KEY_SECURE_LEVEL, level);
        intent.setPackage(PACKAGE_ANDROID);
        intent.addFlags(1073741824);
        this.mContext.sendBroadcast(intent);
    }

    private void sendBlackList(ArrayList<String> blackList, int mode, ComponentName who) {
        String packageName = null;
        String className = null;
        if (who != null) {
            packageName = who.getPackageName();
            className = who.getClassName();
        }
        Intent intent = new Intent(ACTION_WIFI_DEVICE_POLICY);
        intent.setPackage(PACKAGE_ANDROID);
        intent.addFlags(1073741824);
        intent.putExtra(KEY_WIFI_POLICY_CATEGORY, 1);
        intent.putExtra(KEY_BLACK_LIST_CHANGE_MODE, mode);
        intent.putExtra(KEY_PACKAGE_NAME, packageName);
        intent.putExtra(KEY_CLASS_NAME, className);
        intent.putStringArrayListExtra(KEY_BLACK_LIST_SSID, blackList);
        this.mContext.sendBroadcast(intent);
    }

    private void setWifiSecuritylevel(Bundle policyData) {
        int level;
        try {
            level = Integer.parseInt(policyData.getString(KEY_SECURITY_LEVEL));
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "level : NumberFormatException");
            level = 1;
        }
        String str = TAG;
        HwLog.d(str, "onSetPolicy WIFI_SECURITYLEVEL " + level);
        sendSecurityLevel(level);
    }

    private void removeConfiguredNetworklist(Bundle policyData) {
        ArrayList<String> removeLists = null;
        try {
            removeLists = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "get removeList exception");
        }
        if (removeLists != null && !removeLists.isEmpty()) {
            removeConfiguredNetworks(removeLists);
        }
    }

    private boolean notifyPolicyChanged(String policyName, boolean isChanged, int policyMode, Bundle policyData, int changeMode) {
        if (isChanged) {
            notifyWifiPolicyChanged(policyMode, policyData, changeMode);
            return true;
        }
        String str = TAG;
        HwLog.d(str, "The policy not change. policyMode: " + policyMode);
        if (POLICY_DISABLE_WIFIPRO.equals(policyName) || PROFILE_ENTERPRISE_CONFIG.equals(policyName)) {
            return false;
        }
        return true;
    }

    private boolean dealWifiPro(Bundle policyData, boolean isChanged) {
        if (!isChanged) {
            HwLog.d(TAG, "onSetPolicy: OP_DISABLE_WIFIPRO no need to change");
            return false;
        } else if (!policyData.getBoolean(VALUE_DISABLE)) {
            return true;
        } else {
            disableWifiProState();
            return true;
        }
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        HwLog.d(TAG, "onSetPolicy changed " + isChanged);
        if (policyName == null) {
            return false;
        }
        if (policyData == null) {
            return true;
        }
        char c = 65535;
        switch (policyName.hashCode()) {
            case -1642980473:
                if (policyName.equals(POLICY_AUTO_CONNECT)) {
                    c = '\b';
                    break;
                }
                break;
            case -1586435062:
                if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
                    c = 7;
                    break;
                }
                break;
            case -1152564340:
                if (policyName.equals(WIFI_SECURITYLEVEL)) {
                    c = 0;
                    break;
                }
                break;
            case -617970941:
                if (policyName.equals(WIFI_BLACKLIST)) {
                    c = 1;
                    break;
                }
                break;
            case 43624280:
                if (policyName.equals(POLICY_RESTRICT_NETWORK)) {
                    c = 6;
                    break;
                }
                break;
            case 474730232:
                if (policyName.equals(POLICY_DISABLE_WIFIPRO)) {
                    c = 5;
                    break;
                }
                break;
            case 690133549:
                if (policyName.equals(WIFI_WHITELIST)) {
                    c = 2;
                    break;
                }
                break;
            case 1167441671:
                if (policyName.equals(POLICY_OPEN_HOTSPOT)) {
                    c = '\t';
                    break;
                }
                break;
            case 1173183091:
                if (policyName.equals(UNAVAILABLE_SSID_LIST)) {
                    c = 3;
                    break;
                }
                break;
            case 1437632038:
                if (policyName.equals(REMOVE_CONFIGURED_NETWORKLIST)) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                setWifiSecuritylevel(policyData);
                break;
            case 1:
                ArrayList<String> blackLists = null;
                try {
                    blackLists = policyData.getStringArrayList("value");
                } catch (ArrayIndexOutOfBoundsException e) {
                    HwLog.e(TAG, "get removeList exception");
                }
                if (blackLists != null) {
                    sendBlackList(blackLists, 0, who);
                    break;
                } else {
                    return true;
                }
            case 2:
                return notifyPolicyChanged(policyName, isChanged, 6, policyData, 0);
            case 3:
                return notifyPolicyChanged(policyName, isChanged, OP_UNAVAILABLE_SSID_LIST, policyData, 0);
            case 4:
                removeConfiguredNetworklist(policyData);
                break;
            case 5:
                return dealWifiPro(policyData, isChanged);
            case 6:
                return notifyPolicyChanged(policyName, isChanged, 2, policyData, 0);
            case OP_UNAVAILABLE_SSID_LIST /* 7 */:
                return notifyPolicyChanged(policyName, isChanged, 3, policyData, 0);
            case '\b':
                return notifyPolicyChanged(policyName, isChanged, 4, policyData, 0);
            case '\t':
                return notifyPolicyChanged(policyName, isChanged, 5, policyData, 0);
        }
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0056, code lost:
        if (r11.equals(com.android.server.devicepolicy.plugins.DeviceWifiPlugin.WIFI_SECURITYLEVEL) != false) goto L_0x006e;
     */
    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        char c = 0;
        if (policyName == null) {
            HwLog.e(TAG, "onRemovePolicy: policyName is null!");
            return false;
        } else if (policyData == null && !WIFI_SECURITYLEVEL.equals(policyName)) {
            return true;
        } else {
            switch (policyName.hashCode()) {
                case -1642980473:
                    if (policyName.equals(POLICY_AUTO_CONNECT)) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -1586435062:
                    if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1152564340:
                    break;
                case -617970941:
                    if (policyName.equals(WIFI_BLACKLIST)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 43624280:
                    if (policyName.equals(POLICY_RESTRICT_NETWORK)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 690133549:
                    if (policyName.equals(WIFI_WHITELIST)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1167441671:
                    if (policyName.equals(POLICY_OPEN_HOTSPOT)) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 1173183091:
                    if (policyName.equals(UNAVAILABLE_SSID_LIST)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwLog.d(TAG, "onRemovePolicy: WIFI_SECURITYLEVEL ");
                    sendSecurityLevel(1);
                    break;
                case 1:
                    ArrayList<String> blackLists = null;
                    try {
                        blackLists = policyData.getStringArrayList("value");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        HwLog.e(TAG, "get removeList exception");
                    }
                    if (blackLists != null) {
                        sendBlackList(blackLists, 1, who);
                        break;
                    } else {
                        return true;
                    }
                case 2:
                    return notifyPolicyChanged(policyName, isChanged, 6, policyData, 1);
                case 3:
                    return notifyPolicyChanged(policyName, isChanged, OP_UNAVAILABLE_SSID_LIST, policyData, 1);
                case 4:
                    return notifyPolicyChanged(policyName, isChanged, 2, policyData, 1);
                case 5:
                    return notifyPolicyChanged(policyName, isChanged, 3, policyData, 1);
                case 6:
                    return notifyPolicyChanged(policyName, isChanged, 4, policyData, 1);
                case OP_UNAVAILABLE_SSID_LIST /* 7 */:
                    return notifyPolicyChanged(policyName, isChanged, 5, policyData, 1);
            }
            return true;
        }
    }

    private void procStatusPolicy(int policyMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(VALUE_DISABLE, false);
        notifyWifiPolicyChanged(policyMode, bundle, 2);
    }

    private void procListPolicy(int policyMode, String attrName) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(attrName, null);
        notifyWifiPolicyChanged(policyMode, bundle, 2);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        boolean z;
        HwLog.d(TAG, "onActiveAdminRemoved");
        if (removedPolicies == null) {
            return true;
        }
        Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
        while (it.hasNext()) {
            PolicyStruct.PolicyItem policy = it.next();
            if (policy != null) {
                String policyName = policy.getPolicyName();
                switch (policyName.hashCode()) {
                    case -1642980473:
                        if (policyName.equals(POLICY_AUTO_CONNECT)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case -1586435062:
                        if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case -1152564340:
                        if (policyName.equals(WIFI_SECURITYLEVEL)) {
                            z = false;
                            break;
                        }
                        z = true;
                        break;
                    case -617970941:
                        if (policyName.equals(WIFI_BLACKLIST)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 43624280:
                        if (policyName.equals(POLICY_RESTRICT_NETWORK)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 690133549:
                        if (policyName.equals(WIFI_WHITELIST)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 1167441671:
                        if (policyName.equals(POLICY_OPEN_HOTSPOT)) {
                            z = OP_UNAVAILABLE_SSID_LIST;
                            break;
                        }
                        z = true;
                        break;
                    case 1173183091:
                        if (policyName.equals(UNAVAILABLE_SSID_LIST)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    default:
                        z = true;
                        break;
                }
                switch (z) {
                    case false:
                        sendSecurityLevel(1);
                        continue;
                    case true:
                        sendBlackList(null, 2, who);
                        continue;
                    case true:
                        procListPolicy(6, "value");
                        continue;
                    case true:
                        procListPolicy(OP_UNAVAILABLE_SSID_LIST, "value");
                        continue;
                    case true:
                        procStatusPolicy(2);
                        continue;
                    case true:
                        procListPolicy(3, VALUE_STRING_LIST);
                        continue;
                    case true:
                        procStatusPolicy(4);
                        continue;
                    case OP_UNAVAILABLE_SSID_LIST /* 7 */:
                        procStatusPolicy(5);
                        continue;
                }
            }
        }
        return true;
    }

    private void notifyWifiPolicyChanged(int policyMode, Bundle policyData, int changeMode) {
        Intent intent = new Intent();
        intent.setAction(ACTION_WIFI_DEVICE_POLICY);
        intent.setPackage(PACKAGE_ANDROID);
        intent.addFlags(1073741824);
        intent.putExtra(KEY_WIFI_POLICY_CATEGORY, policyMode);
        intent.putExtra(KEY_WIFI_POLICY_DATA, policyData);
        intent.putExtra(KEY_WIFI_POLICY_CHANGE_MODE, changeMode);
        this.mContext.sendBroadcast(intent);
    }

    private void removeConfiguredNetworks(ArrayList<String> modifyLists) {
        long identity = Binder.clearCallingIdentity();
        try {
            removeConfiguredNetworksInternal(modifyLists);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void removeConfiguredNetworksInternal(ArrayList<String> modifyLists) {
        List<WifiConfiguration> existingConfigs;
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (!(wifiManager == null || (existingConfigs = wifiManager.getConfiguredNetworks()) == null || existingConfigs.size() == 0)) {
            Iterator<String> it = modifyLists.iterator();
            while (it.hasNext()) {
                String bundleSsid = it.next();
                if (!(bundleSsid == null || bundleSsid.length() == 0)) {
                    for (WifiConfiguration existingConfig : existingConfigs) {
                        if (bundleSsid.equals(WifiInfo.removeDoubleQuotes(existingConfig.SSID))) {
                            wifiManager.forget(existingConfig.networkId, null);
                        }
                    }
                }
            }
        }
    }

    private void disableWifiProState() {
        long identityToken = Binder.clearCallingIdentity();
        try {
            Settings.System.putInt(this.mContext.getContentResolver(), KEY_SMART_NETWORK_SWITCHING, 0);
        } catch (IllegalArgumentException e) {
            HwLog.e(TAG, "write settings throw IllegalArgumentException");
        } catch (SecurityException e2) {
            HwLog.e(TAG, "write settings throw SecurityException");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
        Binder.restoreCallingIdentity(identityToken);
    }
}
