package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.UserManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConfigurationMap {
    private int mCurrentUserId = 0;
    private final Map<Integer, WifiConfiguration> mPerID = new HashMap();
    private final Map<Integer, WifiConfiguration> mPerIDForCurrentUser = new HashMap();
    private final Map<ScanResultMatchInfo, WifiConfiguration> mScanResultMatchInfoMapForCurrentUser = new HashMap();
    private final UserManager mUserManager;

    ConfigurationMap(UserManager userManager) {
        this.mUserManager = userManager;
    }

    public WifiConfiguration put(WifiConfiguration config) {
        WifiConfiguration current = this.mPerID.put(Integer.valueOf(config.networkId), config);
        if (WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            this.mPerIDForCurrentUser.put(Integer.valueOf(config.networkId), config);
            this.mScanResultMatchInfoMapForCurrentUser.put(ScanResultMatchInfo.fromWifiConfiguration(config), config);
        }
        return current;
    }

    public WifiConfiguration remove(int netID) {
        WifiConfiguration config = this.mPerID.remove(Integer.valueOf(netID));
        if (config == null) {
            return null;
        }
        this.mPerIDForCurrentUser.remove(Integer.valueOf(netID));
        Iterator<Map.Entry<ScanResultMatchInfo, WifiConfiguration>> scanResultMatchInfoEntries = this.mScanResultMatchInfoMapForCurrentUser.entrySet().iterator();
        while (true) {
            if (scanResultMatchInfoEntries.hasNext()) {
                if (scanResultMatchInfoEntries.next().getValue().networkId == netID) {
                    scanResultMatchInfoEntries.remove();
                    break;
                }
            } else {
                break;
            }
        }
        return config;
    }

    public void clear() {
        this.mPerID.clear();
        this.mPerIDForCurrentUser.clear();
        this.mScanResultMatchInfoMapForCurrentUser.clear();
    }

    public void setNewUser(int userId) {
        this.mCurrentUserId = userId;
    }

    public WifiConfiguration getForAllUsers(int netid) {
        return this.mPerID.get(Integer.valueOf(netid));
    }

    public WifiConfiguration getForCurrentUser(int netid) {
        return this.mPerIDForCurrentUser.get(Integer.valueOf(netid));
    }

    public int sizeForAllUsers() {
        return this.mPerID.size();
    }

    public int sizeForCurrentUser() {
        return this.mPerIDForCurrentUser.size();
    }

    public WifiConfiguration getByConfigKeyForCurrentUser(String key) {
        if (key == null) {
            return null;
        }
        for (WifiConfiguration config : this.mPerIDForCurrentUser.values()) {
            if (config.configKey().equals(key)) {
                return config;
            }
        }
        return null;
    }

    public WifiConfiguration getByScanResultForCurrentUser(ScanResult scanResult) {
        return this.mScanResultMatchInfoMapForCurrentUser.get(ScanResultMatchInfo.fromScanResult(scanResult));
    }

    public Collection<WifiConfiguration> valuesForAllUsers() {
        return this.mPerID.values();
    }

    public Collection<WifiConfiguration> valuesForCurrentUser() {
        return this.mPerIDForCurrentUser.values();
    }
}
