package com.android.server.devicepolicy;

import android.os.Bundle;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import java.util.HashMap;
import java.util.List;

public class HwAdminCache {
    private static HwAdminCacheData cacheData = new HwAdminCacheData();
    private final Object mLock = new Object();

    public void syncHwAdminCache(int code, boolean isDisable) {
        synchronized (this.mLock) {
            cacheData.getBooleanMap().put(Integer.valueOf(code), Boolean.valueOf(isDisable));
        }
    }

    public void syncHwAdminCache(int code, List<String> list) {
        synchronized (this.mLock) {
            HashMap<Integer, List<String>> map = cacheData.getListMap();
            if (list == null) {
                map.remove(Integer.valueOf(code));
            } else {
                map.put(Integer.valueOf(code), list);
            }
        }
    }

    public void syncHwAdminCache(String policyName, Bundle bundle) {
        if (bundle != null) {
            synchronized (this.mLock) {
                if ("wifi_p2p_item_policy_name".equals(policyName)) {
                    syncHwAdminCache(5021, bundle.getBoolean("wifi_p2p_policy_item_value", false));
                } else if ("infrared_item_policy_name".equals(policyName)) {
                    syncHwAdminCache(5022, bundle.getBoolean("infrared_item_policy_value", false));
                } else {
                    int code = PolicyConstant.getCodeByPolicyName(policyName, false);
                    if (code == -1) {
                        int code2 = PolicyConstant.getCodeByPolicyName(policyName, true);
                        if (code2 == -1) {
                            cacheData.getBundleMap().put(policyName, bundle);
                        } else {
                            syncHwAdminCache(code2, bundle.getStringArrayList(SettingsMDMPlugin.STATE_VALUE));
                        }
                    } else {
                        syncHwAdminCache(code, bundle.getBoolean(SettingsMDMPlugin.STATE_VALUE, false));
                    }
                }
            }
        }
    }

    public Bundle getCachedBundle(String policyName) {
        Bundle bundle;
        synchronized (this.mLock) {
            bundle = cacheData.getBundleMap().get(policyName);
        }
        return bundle;
    }

    public boolean getCachedValue(int code) {
        boolean booleanValue;
        synchronized (this.mLock) {
            booleanValue = cacheData.getBooleanMap().get(Integer.valueOf(code)).booleanValue();
        }
        return booleanValue;
    }

    public List<String> getCachedList(int code) {
        List<String> list;
        synchronized (this.mLock) {
            list = cacheData.getListMap().get(Integer.valueOf(code));
        }
        return list;
    }
}
