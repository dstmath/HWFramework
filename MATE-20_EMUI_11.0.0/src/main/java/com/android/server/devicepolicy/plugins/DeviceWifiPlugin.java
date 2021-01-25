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

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.d(TAG, "onInit");
        if (policyStruct == null) {
            return false;
        }
        return true;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.d(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission(MDM_WIFI_PERMISSION, "need permission com.huawei.permission.sec.MDM_WIFI");
        return true;
    }

    private void sendSecurityLevel(int level) {
        Intent intent = new Intent();
        intent.setAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intent.putExtra(KEY_WIFI_POLICY_CATEGORY, 0);
        intent.putExtra(KEY_SECURE_LEVEL, level);
        intent.setPackage(PACKAGE_ANDROID);
        intent.addFlags(1073741824);
        this.mContext.sendBroadcast(intent);
    }

    private void sendBlackList(ArrayList<String> blacklist, int mode, ComponentName who) {
        String packageName = null;
        String className = null;
        if (who != null) {
            packageName = who.getPackageName();
            className = who.getClassName();
        }
        Intent intent = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intent.setPackage(PACKAGE_ANDROID);
        intent.addFlags(1073741824);
        intent.putExtra(KEY_WIFI_POLICY_CATEGORY, 1);
        intent.putExtra(KEY_BLACK_LIST_CHANGE_MODE, mode);
        intent.putExtra(KEY_PACKAGE_NAME, packageName);
        intent.putExtra(KEY_CLASS_NAME, className);
        intent.putStringArrayListExtra(KEY_BLACK_LIST_SSID, blacklist);
        this.mContext.sendBroadcast(intent);
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        int level;
        ArrayList<String> blacklist;
        HwLog.d(TAG, "onSetPolicy changed " + isChanged);
        if (policyName == null) {
            HwLog.e(TAG, "onSetPolicy: policyName is null!");
            return false;
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
                    c = OP_UNAVAILABLE_SSID_LIST;
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
                if (policyData != null) {
                    try {
                        level = Integer.parseInt(policyData.getString(KEY_SECURITY_LEVEL));
                    } catch (NumberFormatException e) {
                        HwLog.e(TAG, "level : NumberFormatException");
                        level = 1;
                    }
                    HwLog.d(TAG, "onSetPolicy WIFI_SECURITYLEVEL " + level);
                    sendSecurityLevel(level);
                    break;
                } else {
                    return true;
                }
            case 1:
                if (policyData != null && (blacklist = policyData.getStringArrayList("value")) != null) {
                    sendBlackList(blacklist, 0, who);
                    break;
                } else {
                    return true;
                }
            case 2:
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.d(TAG, "onSetPolicy: OP_WHITELIST can not be added to list");
                        break;
                    } else {
                        notifyWifiPolicyChanged(6, policyData, 0);
                        break;
                    }
                } else {
                    return true;
                }
            case 3:
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.d(TAG, "onSetPolicy: OP_UNAVAILABLE_SSID_LIST can not be added to list");
                        break;
                    } else {
                        notifyWifiPolicyChanged(OP_UNAVAILABLE_SSID_LIST, policyData, 0);
                        break;
                    }
                } else {
                    return true;
                }
            case 4:
                if (policyData != null) {
                    ArrayList<String> removeList = null;
                    try {
                        removeList = policyData.getStringArrayList("value");
                    } catch (ArrayIndexOutOfBoundsException e2) {
                        HwLog.e(TAG, "get removeList exception");
                    }
                    if (removeList != null && removeList.size() != 0) {
                        removeConfiguredNetworks(removeList);
                        break;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
                break;
            case 5:
                if (policyData != null) {
                    if (isChanged) {
                        if (policyData.getBoolean(VALUE_DISABLE)) {
                            disableWifiProState();
                            break;
                        }
                    } else {
                        HwLog.d(TAG, "onSetPolicy: OP_DISABLE_WIFIPRO no need to change");
                        return false;
                    }
                } else {
                    return true;
                }
                break;
            case 6:
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.d(TAG, "onSetPolicy: POLICY_RESTRICT_NETWORK no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(2, policyData, 0);
                        break;
                    }
                } else {
                    return true;
                }
            case OP_UNAVAILABLE_SSID_LIST /* 7 */:
                if (policyData == null) {
                    return true;
                }
                if (isChanged) {
                    notifyWifiPolicyChanged(3, policyData, 0);
                    break;
                } else {
                    HwLog.e(TAG, "onSetPolicy: ENTERPRISE_CONFIG can not be added to list");
                    return false;
                }
            case '\b':
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.d(TAG, "onSetPolicy: POLICY_AUTO_CONNECT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(4, policyData, 0);
                        break;
                    }
                } else {
                    return true;
                }
            case '\t':
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.d(TAG, "onSetPolicy: POLICY_OPEN_HOTSPOT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(5, policyData, 0);
                        break;
                    }
                } else {
                    return true;
                }
            default:
                HwLog.e(TAG, "onSetPolicy: nukonw policyName!");
                break;
        }
        return true;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        ArrayList<String> blacklist;
        HwLog.d(TAG, "onRemovePolicy");
        if (policyName == null) {
            HwLog.e(TAG, "onRemovePolicy: policyName is null!");
            return false;
        }
        char c = 65535;
        switch (policyName.hashCode()) {
            case -1642980473:
                if (policyName.equals(POLICY_AUTO_CONNECT)) {
                    c = 6;
                    break;
                }
                break;
            case -1586435062:
                if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
                    c = 5;
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
                    c = 4;
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
                    c = OP_UNAVAILABLE_SSID_LIST;
                    break;
                }
                break;
            case 1173183091:
                if (policyName.equals(UNAVAILABLE_SSID_LIST)) {
                    c = 3;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                HwLog.d(TAG, "onRemovePolicy: WIFI_SECURITYLEVEL ");
                sendSecurityLevel(1);
                break;
            case 1:
                if (policyData != null && (blacklist = policyData.getStringArrayList("value")) != null) {
                    sendBlackList(blacklist, 1, who);
                    break;
                } else {
                    return true;
                }
                break;
            case 2:
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.e(TAG, "onRemovePolicy: OP_WHITELIST can not be removed from list");
                        break;
                    } else {
                        notifyWifiPolicyChanged(6, policyData, 1);
                        break;
                    }
                } else {
                    return true;
                }
            case 3:
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.e(TAG, "onRemovePolicy: OP_UNAVAILABLE_SSID_LIST can not be removed from list");
                        break;
                    } else {
                        notifyWifiPolicyChanged(OP_UNAVAILABLE_SSID_LIST, policyData, 1);
                        break;
                    }
                } else {
                    return true;
                }
            case 4:
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.d(TAG, "onRemovePolicy: POLICY_RESTRICT_NETWORK no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(2, policyData, 1);
                        break;
                    }
                } else {
                    return true;
                }
            case 5:
                if (policyData == null) {
                    return true;
                }
                if (isChanged) {
                    notifyWifiPolicyChanged(3, policyData, 1);
                    break;
                } else {
                    HwLog.e(TAG, "onRemovePolicy: ENTERPRISE_CONFIG can not be removed from list");
                    return false;
                }
            case 6:
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.d(TAG, "onRemovePolicy: POLICY_AUTO_CONNECT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(4, policyData, 1);
                        break;
                    }
                } else {
                    return true;
                }
            case OP_UNAVAILABLE_SSID_LIST /* 7 */:
                if (policyData != null) {
                    if (!isChanged) {
                        HwLog.d(TAG, "onRemovePolicy: POLICY_OPEN_HOTSPOT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(5, policyData, 1);
                        break;
                    }
                } else {
                    return true;
                }
            default:
                HwLog.e(TAG, "onRemovePolicy: nukonw policyName!");
                break;
        }
        return true;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.d(TAG, "onGetPolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.d(TAG, "onActiveAdminRemoved");
        if (removedPolicies == null) {
            return true;
        }
        Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
        while (it.hasNext()) {
            PolicyStruct.PolicyItem policy = it.next();
            if (policy != null) {
                String policyName = policy.getPolicyName();
                char c = 65535;
                switch (policyName.hashCode()) {
                    case -1642980473:
                        if (policyName.equals(POLICY_AUTO_CONNECT)) {
                            c = 6;
                            break;
                        }
                        break;
                    case -1586435062:
                        if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
                            c = 5;
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
                            c = 4;
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
                            c = OP_UNAVAILABLE_SSID_LIST;
                            break;
                        }
                        break;
                    case 1173183091:
                        if (policyName.equals(UNAVAILABLE_SSID_LIST)) {
                            c = 3;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        sendSecurityLevel(1);
                        continue;
                    case 1:
                        sendBlackList(null, 2, who);
                        continue;
                    case 2:
                        Bundle whiteListBundle = new Bundle();
                        whiteListBundle.putStringArrayList("value", null);
                        notifyWifiPolicyChanged(6, whiteListBundle, 2);
                        continue;
                    case 3:
                        Bundle unavailableSsidListBundle = new Bundle();
                        unavailableSsidListBundle.putStringArrayList("value", null);
                        notifyWifiPolicyChanged(OP_UNAVAILABLE_SSID_LIST, unavailableSsidListBundle, 2);
                        continue;
                    case 4:
                        Bundle restrictBundle = new Bundle();
                        restrictBundle.putBoolean(VALUE_DISABLE, false);
                        notifyWifiPolicyChanged(2, restrictBundle, 2);
                        continue;
                    case 5:
                        Bundle enterpriseBundle = new Bundle();
                        enterpriseBundle.putStringArrayList(VALUE_STRING_LIST, null);
                        notifyWifiPolicyChanged(3, enterpriseBundle, 2);
                        continue;
                    case 6:
                        Bundle autoConnectBundle = new Bundle();
                        autoConnectBundle.putBoolean(VALUE_DISABLE, false);
                        notifyWifiPolicyChanged(4, autoConnectBundle, 2);
                        continue;
                    case OP_UNAVAILABLE_SSID_LIST /* 7 */:
                        Bundle openSoftApBundle = new Bundle();
                        openSoftApBundle.putBoolean(VALUE_DISABLE, false);
                        notifyWifiPolicyChanged(5, openSoftApBundle, 2);
                        continue;
                    default:
                        continue;
                }
            }
        }
        return true;
    }

    private void notifyWifiPolicyChanged(int policyMode, Bundle policyData, int changeMode) {
        Intent intent = new Intent();
        intent.setAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intent.setPackage(PACKAGE_ANDROID);
        intent.addFlags(1073741824);
        intent.putExtra(KEY_WIFI_POLICY_CATEGORY, policyMode);
        intent.putExtra(KEY_WIFI_POLICY_DATA, policyData);
        intent.putExtra(KEY_WIFI_POLICY_CHANGE_MODE, changeMode);
        this.mContext.sendBroadcast(intent);
    }

    private void removeConfiguredNetworks(ArrayList<String> modifyList) {
        long identity = Binder.clearCallingIdentity();
        try {
            removeConfiguredNetworksInternal(modifyList);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void removeConfiguredNetworksInternal(ArrayList<String> modifyList) {
        List<WifiConfiguration> existingConfigs;
        WifiManager wifiManager = null;
        Object object = this.mContext.getSystemService("wifi");
        if (object instanceof WifiManager) {
            wifiManager = (WifiManager) object;
        }
        if (!(wifiManager == null || (existingConfigs = wifiManager.getConfiguredNetworks()) == null || existingConfigs.size() == 0)) {
            for (int i = 0; i < modifyList.size(); i++) {
                String bundleSsid = modifyList.get(i);
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
