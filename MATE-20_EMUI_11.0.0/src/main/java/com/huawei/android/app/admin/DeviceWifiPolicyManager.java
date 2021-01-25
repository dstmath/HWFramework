package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeviceWifiPolicyManager {
    public static final int BLACKLIST_SIZE_LIMIT = 200;
    private static final int MAX_SSID_LENGTH = 32;
    private static final String POLICY_AUTO_CONNECT = "policy-auto-connect";
    private static final String POLICY_DISABLE_WIFIPRO = "policy-disable-wifipro";
    private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
    private static final String POLICY_RESTRICT_NETWORK = "policy-restrict-network";
    private static final String PROFILE_ENTERPRISE_CONFIG = "profile-enterprise-config";
    private static final String REMOVE_CONFIGURED_NETWORKLIST = "remove-configured-networklist";
    public static final int SECURITY_LEVEL_1 = 1;
    public static final int SECURITY_LEVEL_2 = 2;
    public static final int SECURITY_LEVEL_3 = 3;
    public static final int SECURITY_LEVEL_4 = 4;
    public static final int SECURITY_LEVEL_5 = 5;
    public static final int SECURITY_LEVEL_6 = 6;
    public static final int SSID_LENGTH_LIMIT = 64;
    private static final String TAG = DeviceWifiPolicyManager.class.getSimpleName();
    private static final String UNAVAILABLE_SSID_LIST = "unavailable-ssid-list";
    private static final String VALUE_DISABLE = "value_disable";
    private static final String VALUE_STRING_LIST = "value_string_list";
    public static final int WHITELIST_SIZE_LIMIT = 200;
    private static final String WIFI_BLACKLIST = "wifi-ssid-blacklist";
    private static final String WIFI_BLOCKLIST = "wifi-ssid-blocklist";
    private static final String WIFI_SECURITYLEVEL = "wifi-securitylevel";
    private static final String WIFI_TRUSTLIST = "wifi-ssid-trustlist";
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
        try {
            return Integer.parseInt(bundle.getString("securitylevel"));
        } catch (NumberFormatException e) {
            Log.e(TAG, "level : NumberFormatException");
            return 1;
        } catch (Exception e2) {
            return 1;
        }
    }

    @Deprecated
    public boolean addSSIDToBlackList(ComponentName admin, ArrayList<String> ssids) {
        return addSsidToCacheBlockList(admin, ssids, WIFI_BLACKLIST);
    }

    public boolean addSsidToBlockList(ComponentName admin, ArrayList<String> ssids) {
        return addSsidToCacheBlockList(admin, ssids, WIFI_BLOCKLIST);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0036  */
    private boolean addSsidToCacheBlockList(ComponentName admin, ArrayList<String> ssids, String policyName) {
        if (ssids == null || ssids.isEmpty() || ssids.size() > 200) {
            Log.e(TAG, "addSsidToBlockList(): Param ssids is invalid, invocation failed");
            return false;
        }
        List<String> lists = getSsidBlockList(admin);
        int len = 0;
        if (lists != null) {
            len = lists.size();
        }
        if (ssids.size() + len > 200) {
            Log.e(TAG, "addSsidToBlockList(): The potential size of blocklist is beyond limit, invocation failed");
            return false;
        }
        Iterator<String> it = ssids.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (str == null || str.isEmpty() || str.length() > 64) {
                Log.e(TAG, "addSsidToBlockList(): There is an invalid entry in ssids, invocation failed");
                return false;
            }
            while (it.hasNext()) {
            }
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", ssids);
        return this.mDpm.setPolicy(admin, policyName, bundle);
    }

    @Deprecated
    public boolean removeSSIDFromBlackList(ComponentName admin, ArrayList<String> ssids) {
        return removeSsidFromBlockList(admin, ssids);
    }

    public boolean removeSsidFromBlockList(ComponentName admin, ArrayList<String> ssids) {
        if (ssids == null || (!ssids.isEmpty() && ssids.size() <= 200)) {
            Bundle bundle = new Bundle();
            if (ssids == null) {
                ArrayList<String> lists = getSsidBlockList(admin);
                if (lists == null) {
                    return false;
                }
                bundle.putStringArrayList("value", lists);
            } else {
                bundle.putStringArrayList("value", ssids);
            }
            return this.mDpm.removePolicy(admin, WIFI_BLACKLIST, bundle);
        }
        Log.e(TAG, "removeSsidFromBlockList(): Param ssids is invalid, invocation failed");
        return false;
    }

    @Deprecated
    public ArrayList<String> getSSIDBlackList(ComponentName admin) {
        return getSsidBlockList(admin);
    }

    public ArrayList<String> getSsidBlockList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, WIFI_BLACKLIST);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getSsidBlockList exception.");
            return null;
        }
    }

    @Deprecated
    public boolean isBlackListedSSID(ComponentName admin, String ssid) {
        return isBlockListedSsid(admin, ssid);
    }

    public boolean isBlockListedSsid(ComponentName admin, String ssid) {
        ArrayList<String> lists = getSsidBlockList(admin);
        if (lists == null) {
            return false;
        }
        return lists.contains(ssid);
    }

    @Deprecated
    public boolean addSSIDToWhiteList(ComponentName admin, ArrayList<String> ssids) {
        return addSsidToCacheTrustList(admin, ssids, WIFI_WHITELIST);
    }

    public boolean addSsidToTrustList(ComponentName admin, ArrayList<String> ssids) {
        return addSsidToCacheTrustList(admin, ssids, WIFI_TRUSTLIST);
    }

    private boolean addSsidToCacheTrustList(ComponentName admin, ArrayList<String> ssids, String policyName) {
        if (ssids == null || ssids.isEmpty() || ssids.size() > 200) {
            Log.e(TAG, "addSsidToTrustList(): Param ssids is invalid, invocation failed");
            return false;
        }
        List<String> lists = getSsidTrustList(admin);
        int len = 0;
        if (lists != null) {
            len = lists.size();
        }
        if (ssids.size() + len > 200) {
            Log.e(TAG, "addSsidToTrustList(): The potential size of trustlist is beyond limit, invocation failed");
            return false;
        }
        int listSize = ssids.size();
        for (int i = 0; i < listSize; i++) {
            String str = ssids.get(i);
            if (str == null || str.isEmpty() || str.length() > 64) {
                Log.e(TAG, "addSsidToTrustList(): There is an invalid entry in ssids, invocation failed");
                return false;
            }
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", ssids);
        return this.mDpm.setPolicy(admin, policyName, bundle);
    }

    @Deprecated
    public ArrayList<String> getSSIDWhiteList(ComponentName admin) {
        return getSsidTrustList(admin);
    }

    public ArrayList<String> getSsidTrustList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, WIFI_WHITELIST);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getSsidTrustList exception.");
            return null;
        }
    }

    @Deprecated
    public boolean removeSSIDFromWhiteList(ComponentName admin, ArrayList<String> ssids) {
        return removeSsidFromTrustList(admin, ssids);
    }

    public boolean removeSsidFromTrustList(ComponentName admin, ArrayList<String> ssids) {
        if (ssids == null || (!ssids.isEmpty() && ssids.size() <= 200)) {
            Bundle bundle = new Bundle();
            if (ssids == null) {
                ArrayList<String> lists = getSsidTrustList(admin);
                if (lists == null) {
                    return false;
                }
                bundle.putStringArrayList("value", lists);
            } else {
                bundle.putStringArrayList("value", ssids);
            }
            return this.mDpm.removePolicy(admin, WIFI_WHITELIST, bundle);
        }
        Log.e(TAG, "removeSsidFromTrustList(): Param ssids is invalid, invocation failed");
        return false;
    }

    public boolean setUserProfilesDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(VALUE_DISABLE, disable);
        return this.mDpm.setPolicy(admin, POLICY_RESTRICT_NETWORK, bundle);
    }

    public boolean isUserProfilesDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_RESTRICT_NETWORK);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean(VALUE_DISABLE);
    }

    public boolean setWifiProfile(ComponentName admin, WifiConfiguration config) {
        if (config == null || !config.isEnterprise()) {
            Log.e(TAG, "setWifiProfile(): Param config is invalid, invocation failed");
            return false;
        }
        ArrayList<String> currentList = getWifiProfileList(admin);
        if (currentList != null && currentList.contains(config.SSID)) {
            return true;
        }
        ArrayList<String> addConfig = new ArrayList<>();
        addConfig.add(config.SSID);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(VALUE_STRING_LIST, addConfig);
        return this.mDpm.setPolicy(admin, PROFILE_ENTERPRISE_CONFIG, bundle);
    }

    public ArrayList<String> getWifiProfileList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, PROFILE_ENTERPRISE_CONFIG);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList(VALUE_STRING_LIST);
    }

    public boolean isWifiProfileSet(ComponentName admin, WifiConfiguration config) {
        if (config == null || !config.isEnterprise()) {
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
        if (config == null || !config.isEnterprise()) {
            Log.e(TAG, "removeWifiProfile(): Param config is invalid, invocation failed");
            return false;
        }
        ArrayList<String> currentList = getWifiProfileList(admin);
        if (currentList == null || !currentList.contains(config.SSID)) {
            Log.e(TAG, "removeWifiProfile(): config never set to list");
            return false;
        }
        ArrayList<String> removeList = new ArrayList<>();
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
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean(VALUE_DISABLE);
    }

    public boolean setUnsecureSoftApDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(VALUE_DISABLE, disable);
        return this.mDpm.setPolicy(admin, POLICY_OPEN_HOTSPOT, bundle);
    }

    public boolean isUnsecureSoftApDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_OPEN_HOTSPOT);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean(VALUE_DISABLE);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0036  */
    public boolean addUnavailableSsidToList(ComponentName admin, ArrayList<String> ssids) {
        if (ssids == null || ssids.size() == 0 || ssids.size() > 200) {
            Log.e(TAG, "addUnavailableSsidToList(): Param ssids is invalid, invocation failed");
            return false;
        }
        List<String> list = getUnavailableSsidList(admin);
        int len = 0;
        if (list != null) {
            len = list.size();
        }
        if (ssids.size() + len > 200) {
            Log.e(TAG, "addUnavailableSsidToList(): blacklist will beyond limit, invocation failed");
            return false;
        }
        Iterator<String> it = ssids.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (str == null || str.isEmpty() || str.length() > MAX_SSID_LENGTH) {
                Log.e(TAG, "addUnavailableSsidToList(): one ssid's length more then 32, invocation failed");
                return false;
            }
            while (it.hasNext()) {
            }
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", ssids);
        return this.mDpm.setPolicy(admin, UNAVAILABLE_SSID_LIST, bundle);
    }

    public boolean removeUnavailableSsidFromList(ComponentName admin, ArrayList<String> ssids) {
        if (ssids == null || (ssids.size() != 0 && ssids.size() <= 200)) {
            Bundle bundle = new Bundle();
            if (ssids == null) {
                ArrayList<String> list = getUnavailableSsidList(admin);
                if (list == null) {
                    return false;
                }
                bundle.putStringArrayList("value", list);
            } else {
                bundle.putStringArrayList("value", ssids);
            }
            return this.mDpm.removePolicy(admin, UNAVAILABLE_SSID_LIST, bundle);
        }
        Log.e(TAG, "removeUnavailableSsidFromList(): Param ssids is invalid, invocation failed");
        return false;
    }

    public ArrayList<String> getUnavailableSsidList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, UNAVAILABLE_SSID_LIST);
        if (bundle == null) {
            return new ArrayList<>();
        }
        return bundle.getStringArrayList("value");
    }

    public boolean removeConfiguredNetworks(ComponentName admin, ArrayList<String> ssids) {
        if (ssids == null || ssids.size() == 0) {
            Log.e(TAG, "removeConfiguredNetworks(): Param ssids is invalid, invocation failed");
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", ssids);
        return this.mDpm.setPolicy(admin, REMOVE_CONFIGURED_NETWORKLIST, bundle);
    }

    public boolean setWifiProDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(VALUE_DISABLE, disabled);
        return this.mDpm.setPolicy(admin, POLICY_DISABLE_WIFIPRO, bundle);
    }

    public boolean isWifiProDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_DISABLE_WIFIPRO);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean(VALUE_DISABLE);
    }
}
