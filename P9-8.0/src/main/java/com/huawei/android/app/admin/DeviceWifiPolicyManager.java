package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.List;

public class DeviceWifiPolicyManager {
    public static final int BLACKLIST_SIZE_LIMIT = 200;
    private static final String POLICY_AUTO_CONNECT = "policy-auto-connect";
    private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
    private static final String POLICY_RESTRICT_NETWORK = "policy-restrict-network";
    private static final String PROFILE_ENTERPRISE_CONFIG = "profile-enterprise-config";
    public static final int SECURITY_LEVEL_1 = 1;
    public static final int SECURITY_LEVEL_2 = 2;
    public static final int SECURITY_LEVEL_3 = 3;
    public static final int SECURITY_LEVEL_4 = 4;
    public static final int SECURITY_LEVEL_5 = 5;
    public static final int SECURITY_LEVEL_6 = 6;
    public static final int SSID_LENGTH_LIMIT = 64;
    private static final String TAG = DeviceWifiPolicyManager.class.getSimpleName();
    private static final String VALUE_DISABLE = "value_disable";
    private static final String VALUE_STRING_LIST = "value_string_list";
    public static final int WHITELIST_SIZE_LIMIT = 200;
    private static final String WIFI_BLACKLIST = "wifi-ssid-blacklist";
    private static final String WIFI_SECURITYLEVEL = "wifi-securitylevel";
    private static final String WIFI_WHITELIST = "wifi-ssid-whitelist";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setSecurityLevel(ComponentName admin, int level) {
        if (level > 6 || level < 1) {
            return false;
        }
        Bundle attr = new Bundle();
        attr.putString("securitylevel", String.valueOf(level));
        return this.mDpm.setPolicy(admin, WIFI_SECURITYLEVEL, attr);
    }

    public int getSecurityLevel(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, WIFI_SECURITYLEVEL);
        if (bundle == null) {
            return 1;
        }
        int level;
        try {
            level = Integer.parseInt(bundle.getString("securitylevel"));
        } catch (Exception e) {
            level = 1;
        }
        return level;
    }

    public boolean addSSIDToBlackList(ComponentName admin, ArrayList<String> ssids) {
        if (ssids == null || ssids.size() == 0 || ssids.size() > 200) {
            Log.e(TAG, "addSSIDToBlackList(): Param ssids is invalid, invocation failed");
            return false;
        }
        List<String> list = getSSIDBlackList(admin);
        int len = 0;
        if (list != null) {
            len = list.size();
        }
        if (ssids.size() + len > 200) {
            Log.e(TAG, "addSSIDToBlackList(): The potential size of blacklist is beyond limit, invocation failed");
            return false;
        }
        for (String str : ssids) {
            if (str == null || str.isEmpty()) {
                Log.e(TAG, "addSSIDToBlackList(): There is an invalid entry in ssids, invocation failed");
                return false;
            } else if (str.length() > 64) {
                Log.e(TAG, "addSSIDToBlackList(): There is an invalid entry in ssids, invocation failed");
                return false;
            }
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", ssids);
        return this.mDpm.setPolicy(admin, WIFI_BLACKLIST, bundle);
    }

    public boolean removeSSIDFromBlackList(ComponentName admin, ArrayList<String> ssids) {
        if (ssids == null || (ssids.size() != 0 && ssids.size() <= 200)) {
            Bundle bundle = new Bundle();
            if (ssids == null) {
                ArrayList<String> list = getSSIDBlackList(admin);
                if (list == null) {
                    return false;
                }
                bundle.putStringArrayList("value", list);
            } else {
                bundle.putStringArrayList("value", ssids);
            }
            return this.mDpm.removePolicy(admin, WIFI_BLACKLIST, bundle);
        }
        Log.e(TAG, "removeSSIDFromBlackList(): Param ssids is invalid, invocation failed");
        return false;
    }

    public ArrayList<String> getSSIDBlackList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, WIFI_BLACKLIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public boolean isBlackListedSSID(ComponentName admin, String ssid) {
        ArrayList<String> list = getSSIDBlackList(admin);
        if (list == null) {
            return false;
        }
        return list.contains(ssid);
    }

    public boolean addSSIDToWhiteList(ComponentName admin, ArrayList<String> ssids) {
        if (ssids == null || ssids.size() == 0 || ssids.size() > 200) {
            Log.e(TAG, "addSSIDToWhiteList(): Param ssids is invalid, invocation failed");
            return false;
        }
        List<String> list = getSSIDWhiteList(admin);
        int len = 0;
        if (list != null) {
            len = list.size();
        }
        if (ssids.size() + len > 200) {
            Log.e(TAG, "addSSIDToWhiteList(): The potential size of blacklist is beyond limit, invocation failed");
            return false;
        }
        int listSize = ssids.size();
        for (int i = 0; i < listSize; i++) {
            String str = (String) ssids.get(i);
            if (str == null || str.isEmpty() || str.length() > 64) {
                Log.e(TAG, "addSSIDToWhiteList(): There is an invalid entry in ssids, invocation failed");
                return false;
            }
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", ssids);
        return this.mDpm.setPolicy(admin, WIFI_WHITELIST, bundle);
    }

    public ArrayList<String> getSSIDWhiteList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, WIFI_WHITELIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public boolean removeSSIDFromWhiteList(ComponentName admin, ArrayList<String> ssids) {
        if (ssids == null || (ssids.size() != 0 && ssids.size() <= 200)) {
            Bundle bundle = new Bundle();
            if (ssids == null) {
                ArrayList<String> list = getSSIDWhiteList(admin);
                if (list == null) {
                    return false;
                }
                bundle.putStringArrayList("value", list);
            } else {
                bundle.putStringArrayList("value", ssids);
            }
            return this.mDpm.removePolicy(admin, WIFI_WHITELIST, bundle);
        }
        Log.e(TAG, "removeSSIDFromWhiteList(): Param ssids is invalid, invocation failed");
        return false;
    }

    public boolean setUserProfilesDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(VALUE_DISABLE, disable);
        return this.mDpm.setPolicy(admin, POLICY_RESTRICT_NETWORK, bundle);
    }

    public boolean isUserProfilesDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_RESTRICT_NETWORK);
        return bundle == null ? false : bundle.getBoolean(VALUE_DISABLE);
    }

    public boolean setWifiProfile(ComponentName admin, WifiConfiguration config) {
        if (config == null || (config.isEnterprise() ^ 1) != 0) {
            Log.e(TAG, "setWifiProfile(): Param config is invalid, invocation failed");
            return false;
        }
        ArrayList<String> currentList = getWifiProfileList(admin);
        if (currentList == null || !currentList.contains(config.SSID)) {
            ArrayList<String> addConfig = new ArrayList();
            addConfig.add(config.SSID);
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(VALUE_STRING_LIST, addConfig);
            return this.mDpm.setPolicy(admin, PROFILE_ENTERPRISE_CONFIG, bundle);
        }
        Log.d(TAG, "setWifiProfile(): " + config.SSID + " had been added to MDMEnterpriseConfigList");
        return true;
    }

    public ArrayList<String> getWifiProfileList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, PROFILE_ENTERPRISE_CONFIG);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList(VALUE_STRING_LIST);
    }

    public boolean isWifiProfileSet(ComponentName admin, WifiConfiguration config) {
        if (config == null || (config.isEnterprise() ^ 1) != 0) {
            Log.e(TAG, "isWifiProfileSet(): Param config is invalid, invocation failed");
            return false;
        }
        ArrayList<String> currentList = getWifiProfileList(admin);
        if (currentList == null || !currentList.contains(config.SSID)) {
            return false;
        }
        return true;
    }

    public boolean removeWifiProfile(ComponentName admin, WifiConfiguration config) {
        if (config == null || (config.isEnterprise() ^ 1) != 0) {
            Log.e(TAG, "removeWifiProfile(): Param config is invalid, invocation failed");
            return false;
        }
        ArrayList<String> currentList = getWifiProfileList(admin);
        if (currentList == null || (currentList.contains(config.SSID) ^ 1) != 0) {
            Log.e(TAG, "removeWifiProfile(): config never set to list");
            return false;
        }
        ArrayList<String> removeList = new ArrayList();
        Bundle bundle = new Bundle();
        removeList.add(config.SSID);
        bundle.putStringArrayList(VALUE_STRING_LIST, removeList);
        return this.mDpm.removePolicy(admin, PROFILE_ENTERPRISE_CONFIG, bundle);
    }

    public boolean setWifiAutoConnectionDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(VALUE_DISABLE, disable);
        return this.mDpm.setPolicy(admin, POLICY_AUTO_CONNECT, bundle);
    }

    public boolean isWifiAutoConnectionDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_AUTO_CONNECT);
        return bundle == null ? false : bundle.getBoolean(VALUE_DISABLE);
    }

    public boolean setUnsecureSoftApDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(VALUE_DISABLE, disable);
        return this.mDpm.setPolicy(admin, POLICY_OPEN_HOTSPOT, bundle);
    }

    public boolean isUnsecureSoftApDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_OPEN_HOTSPOT);
        return bundle == null ? false : bundle.getBoolean(VALUE_DISABLE);
    }
}
