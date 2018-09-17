package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import java.util.ArrayList;

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
        struct.addStruct(WIFI_SECURITYLEVEL, PolicyType.CONFIGURATION, new String[]{KEY_SECURITY_LEVEL});
        struct.addStruct(WIFI_BLACKLIST, PolicyType.LIST, new String[0]);
        struct.addStruct(WIFI_BLACKLIST_ITEM, PolicyType.LIST, new String[]{"value"});
        struct.addStruct(WIFI_WHITELIST, PolicyType.LIST, new String[0]);
        struct.addStruct(WIFI_WHITELIST_ITEM, PolicyType.LIST, new String[]{"value"});
        struct.addStruct(POLICY_RESTRICT_NETWORK, PolicyType.STATE, new String[]{VALUE_DISABLE});
        struct.addStruct(PROFILE_ENTERPRISE_CONFIG, PolicyType.LIST, new String[0]);
        struct.addStruct(ENTERPRISE_CONFIG_ITEM, PolicyType.LIST, new String[]{VALUE_STRING_LIST});
        struct.addStruct(POLICY_AUTO_CONNECT, PolicyType.STATE, new String[]{VALUE_DISABLE});
        struct.addStruct(POLICY_OPEN_HOTSPOT, PolicyType.STATE, new String[]{VALUE_DISABLE});
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
        for (String ssid : blacklist) {
            HwLog.d(TAG, "blacklist ssid:" + ssid);
        }
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        HwLog.d(TAG, "onSetPolicy changed " + changed);
        if (policyName == null) {
            HwLog.e(TAG, "onSetPolicy: policyName is null!");
            return false;
        }
        if (policyName.equals(WIFI_SECURITYLEVEL)) {
            if (policyData == null) {
                return true;
            }
            int level;
            try {
                level = Integer.parseInt(policyData.getString(KEY_SECURITY_LEVEL));
            } catch (Exception e) {
                level = 1;
            }
            HwLog.d(TAG, "onSetPolicy WIFI_SECURITYLEVEL " + level);
            sendSecurityLevel(level);
        } else if (policyName.equals(WIFI_BLACKLIST)) {
            if (policyData == null) {
                return true;
            }
            ArrayList<String> blacklist = policyData.getStringArrayList("value");
            if (blacklist == null) {
                return true;
            }
            printBlackList(blacklist);
            sendBlackList(blacklist, 0, who);
        } else if (policyName.equals(WIFI_WHITELIST)) {
            if (policyData == null) {
                return true;
            }
            if (changed) {
                notifyWifiPolicyChanged(6, policyData, 0);
            } else {
                HwLog.d(TAG, "onSetPolicy: OP_WHITELIST can not be added to list");
            }
        } else if (policyName.equals(POLICY_RESTRICT_NETWORK)) {
            if (policyData == null) {
                return true;
            }
            if (changed) {
                notifyWifiPolicyChanged(2, policyData, 0);
            } else {
                HwLog.d(TAG, "onSetPolicy: POLICY_RESTRICT_NETWORK no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
            }
        } else if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
            if (policyData == null) {
                return true;
            }
            if (changed) {
                notifyWifiPolicyChanged(3, policyData, 0);
            } else {
                HwLog.e(TAG, "onSetPolicy: ENTERPRISE_CONFIG can not be added to list");
                return false;
            }
        } else if (policyName.equals(POLICY_AUTO_CONNECT)) {
            if (policyData == null) {
                return true;
            }
            if (changed) {
                notifyWifiPolicyChanged(4, policyData, 0);
            } else {
                HwLog.d(TAG, "onSetPolicy: POLICY_AUTO_CONNECT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
            }
        } else if (!policyName.equals(POLICY_OPEN_HOTSPOT)) {
            HwLog.e(TAG, "onSetPolicy: nukonw policyName!");
        } else if (policyData == null) {
            return true;
        } else {
            if (changed) {
                notifyWifiPolicyChanged(5, policyData, 0);
            } else {
                HwLog.d(TAG, "onSetPolicy: POLICY_OPEN_HOTSPOT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
            }
        }
        return true;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        HwLog.d(TAG, "onRemovePolicy");
        if (policyName == null) {
            HwLog.e(TAG, "onRemovePolicy: policyName is null!");
            return false;
        }
        if (policyName.equals(WIFI_SECURITYLEVEL)) {
            HwLog.d(TAG, "onRemovePolicy: WIFI_SECURITYLEVEL ");
            sendSecurityLevel(1);
        } else if (policyName.equals(WIFI_BLACKLIST)) {
            if (policyData == null) {
                return true;
            }
            ArrayList<String> blacklist = policyData.getStringArrayList("value");
            if (blacklist == null) {
                return true;
            }
            sendBlackList(blacklist, 1, who);
        } else if (policyName.equals(WIFI_WHITELIST)) {
            if (policyData == null) {
                return true;
            }
            if (changed) {
                notifyWifiPolicyChanged(6, policyData, 1);
            } else {
                HwLog.e(TAG, "onRemovePolicy: OP_WHITELIST can not be removed from list");
            }
        } else if (policyName.equals(POLICY_RESTRICT_NETWORK)) {
            if (policyData == null) {
                return true;
            }
            if (changed) {
                notifyWifiPolicyChanged(2, policyData, 1);
            } else {
                HwLog.d(TAG, "onRemovePolicy: POLICY_RESTRICT_NETWORK no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
            }
        } else if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
            if (policyData == null) {
                return true;
            }
            if (changed) {
                notifyWifiPolicyChanged(3, policyData, 1);
            } else {
                HwLog.e(TAG, "onRemovePolicy: ENTERPRISE_CONFIG can not be removed from list");
                return false;
            }
        } else if (policyName.equals(POLICY_AUTO_CONNECT)) {
            if (policyData == null) {
                return true;
            }
            if (changed) {
                notifyWifiPolicyChanged(4, policyData, 1);
            } else {
                HwLog.d(TAG, "onRemovePolicy: POLICY_AUTO_CONNECT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
            }
        } else if (!policyName.equals(POLICY_OPEN_HOTSPOT)) {
            HwLog.e(TAG, "onRemovePolicy: nukonw policyName!");
        } else if (policyData == null) {
            return true;
        } else {
            if (changed) {
                notifyWifiPolicyChanged(5, policyData, 1);
            } else {
                HwLog.d(TAG, "onRemovePolicy: POLICY_OPEN_HOTSPOT no need to change. disable=" + policyData.getBoolean(VALUE_DISABLE));
            }
        }
        return true;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.d(TAG, "onGetPolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyItem> removedPolicies) {
        HwLog.d(TAG, "onActiveAdminRemoved");
        if (removedPolicies == null) {
            return true;
        }
        for (PolicyItem policy : removedPolicies) {
            if (policy != null) {
                String policyName = policy.getPolicyName();
                if (policyName.equals(WIFI_SECURITYLEVEL)) {
                    sendSecurityLevel(1);
                } else if (policyName.equals(WIFI_BLACKLIST)) {
                    sendBlackList(null, 2, who);
                } else if (policyName.equals(WIFI_WHITELIST)) {
                    Bundle whiteListBundle = new Bundle();
                    whiteListBundle.putStringArrayList("value", null);
                    notifyWifiPolicyChanged(6, whiteListBundle, 2);
                } else if (policyName.equals(POLICY_RESTRICT_NETWORK)) {
                    Bundle restrictBundle = new Bundle();
                    restrictBundle.putBoolean(VALUE_DISABLE, false);
                    notifyWifiPolicyChanged(2, restrictBundle, 2);
                } else if (policyName.equals(PROFILE_ENTERPRISE_CONFIG)) {
                    Bundle enterpriseBundle = new Bundle();
                    enterpriseBundle.putStringArrayList(VALUE_STRING_LIST, null);
                    notifyWifiPolicyChanged(3, enterpriseBundle, 2);
                } else if (policyName.equals(POLICY_AUTO_CONNECT)) {
                    Bundle autoConnectBundle = new Bundle();
                    autoConnectBundle.putBoolean(VALUE_DISABLE, false);
                    notifyWifiPolicyChanged(4, autoConnectBundle, 2);
                } else if (policyName.equals(POLICY_OPEN_HOTSPOT)) {
                    Bundle openSoftApBundle = new Bundle();
                    openSoftApBundle.putBoolean(VALUE_DISABLE, false);
                    notifyWifiPolicyChanged(5, openSoftApBundle, 2);
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
