package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.Iterator;

public class DeviceWifiPlugin extends DevicePolicyPlugin {
    private static final String ACTION_WIFI_DEVICE_POLICY = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    private static final int CLEAR_MODE = 2;
    private static final String ENTERPRISE_CONFIG_ITEM = "profile-enterprise-config/enterprise-config-item";
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
    private static final int OP_WHITELIST = 6;
    private static final String PACKAGE_ANDROID = "android";
    private static final String POLICY_AUTO_CONNECT = "policy-auto-connect";
    private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
    private static final String POLICY_RESTRICT_NETWORK = "policy-restrict-network";
    private static final String PROFILE_ENTERPRISE_CONFIG = "profile-enterprise-config";
    private static final int REMOVE_MODE = 1;
    private static final int SECURITY_LEVEL_1 = 1;
    private static final int SET_MODE = 0;
    private static final String TAG = DeviceWifiPlugin.class.getSimpleName();
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

    private void sendSecurityLevel(int Level) {
        Intent intent = new Intent();
        intent.setAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intent.putExtra(KEY_WIFI_POLICY_CATEGORY, 0);
        intent.putExtra(KEY_SECURE_LEVEL, Level);
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
        intent.putExtra("package_name", packageName);
        intent.putExtra(KEY_CLASS_NAME, className);
        intent.putStringArrayListExtra(KEY_BLACK_LIST_SSID, blacklist);
        this.mContext.sendBroadcast(intent);
    }

    private void printBlackList(ArrayList<String> blacklist) {
        Iterator<String> it = blacklist.iterator();
        while (it.hasNext()) {
            String str = TAG;
            HwLog.d(str, "blacklist ssid:" + it.next());
        }
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        int level;
        HwLog.d(TAG, "onSetPolicy changed " + changed);
        if (policyName == null) {
            HwLog.e(TAG, "onSetPolicy: policyName is null!");
            return false;
        }
        char c = 65535;
        switch (policyName.hashCode()) {
            case -1642980473:
                if (policyName.equals(POLICY_AUTO_CONNECT)) {
                    c = 5;
                    break;
                }
                break;
            case -1586435062:
                if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
                    c = 4;
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
                    c = 3;
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
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                if (policyData != null) {
                    try {
                        level = Integer.parseInt(policyData.getString(KEY_SECURITY_LEVEL));
                    } catch (Exception e) {
                        level = 1;
                    }
                    HwLog.d(TAG, "onSetPolicy WIFI_SECURITYLEVEL " + level);
                    sendSecurityLevel(level);
                    break;
                } else {
                    return true;
                }
            case 1:
                if (policyData != null) {
                    ArrayList<String> blacklist = policyData.getStringArrayList("value");
                    if (blacklist != null) {
                        printBlackList(blacklist);
                        sendBlackList(blacklist, 0, who);
                        break;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            case 2:
                if (policyData != null) {
                    if (!changed) {
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
                    if (!changed) {
                        HwLog.d(TAG, "onSetPolicy: POLICY_RESTRICT_NETWORK no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(2, policyData, 0);
                        break;
                    }
                } else {
                    return true;
                }
            case 4:
                if (policyData == null) {
                    return true;
                }
                if (changed) {
                    notifyWifiPolicyChanged(3, policyData, 0);
                    break;
                } else {
                    HwLog.e(TAG, "onSetPolicy: ENTERPRISE_CONFIG can not be added to list");
                    return false;
                }
            case 5:
                if (policyData != null) {
                    if (!changed) {
                        HwLog.d(TAG, "onSetPolicy: POLICY_AUTO_CONNECT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(4, policyData, 0);
                        break;
                    }
                } else {
                    return true;
                }
            case 6:
                if (policyData != null) {
                    if (!changed) {
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

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        HwLog.d(TAG, "onRemovePolicy");
        if (policyName == null) {
            HwLog.e(TAG, "onRemovePolicy: policyName is null!");
            return false;
        }
        char c = 65535;
        switch (policyName.hashCode()) {
            case -1642980473:
                if (policyName.equals(POLICY_AUTO_CONNECT)) {
                    c = 5;
                    break;
                }
                break;
            case -1586435062:
                if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
                    c = 4;
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
                    c = 3;
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
                    c = 6;
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
                if (policyData != null) {
                    ArrayList<String> blacklist = policyData.getStringArrayList("value");
                    if (blacklist != null) {
                        sendBlackList(blacklist, 1, who);
                        break;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            case 2:
                if (policyData != null) {
                    if (!changed) {
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
                    if (!changed) {
                        HwLog.d(TAG, "onRemovePolicy: POLICY_RESTRICT_NETWORK no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(2, policyData, 1);
                        break;
                    }
                } else {
                    return true;
                }
            case 4:
                if (policyData == null) {
                    return true;
                }
                if (changed) {
                    notifyWifiPolicyChanged(3, policyData, 1);
                    break;
                } else {
                    HwLog.e(TAG, "onRemovePolicy: ENTERPRISE_CONFIG can not be removed from list");
                    return false;
                }
            case 5:
                if (policyData != null) {
                    if (!changed) {
                        HwLog.d(TAG, "onRemovePolicy: POLICY_AUTO_CONNECT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
                        break;
                    } else {
                        notifyWifiPolicyChanged(4, policyData, 1);
                        break;
                    }
                } else {
                    return true;
                }
            case 6:
                if (policyData != null) {
                    if (!changed) {
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
                            c = 5;
                            break;
                        }
                        break;
                    case -1586435062:
                        if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
                            c = 4;
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
                            c = 3;
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
                            c = 6;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        sendSecurityLevel(1);
                        break;
                    case 1:
                        sendBlackList(null, 2, who);
                        break;
                    case 2:
                        Bundle whiteListBundle = new Bundle();
                        whiteListBundle.putStringArrayList("value", null);
                        notifyWifiPolicyChanged(6, whiteListBundle, 2);
                        break;
                    case 3:
                        Bundle restrictBundle = new Bundle();
                        restrictBundle.putBoolean(VALUE_DISABLE, false);
                        notifyWifiPolicyChanged(2, restrictBundle, 2);
                        break;
                    case 4:
                        Bundle enterpriseBundle = new Bundle();
                        enterpriseBundle.putStringArrayList(VALUE_STRING_LIST, null);
                        notifyWifiPolicyChanged(3, enterpriseBundle, 2);
                        break;
                    case 5:
                        Bundle autoConnectBundle = new Bundle();
                        autoConnectBundle.putBoolean(VALUE_DISABLE, false);
                        notifyWifiPolicyChanged(4, autoConnectBundle, 2);
                        break;
                    case 6:
                        Bundle openSoftApBundle = new Bundle();
                        openSoftApBundle.putBoolean(VALUE_DISABLE, false);
                        notifyWifiPolicyChanged(5, openSoftApBundle, 2);
                        break;
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
}
