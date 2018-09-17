package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.UserManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ConfigurationMap {
    private int mCurrentUserId = 0;
    private final Map<String, WifiConfiguration> mPerFQDNForCurrentUser = new HashMap();
    private final Map<Integer, WifiConfiguration> mPerID = new HashMap();
    private final Map<Integer, WifiConfiguration> mPerIDForCurrentUser = new HashMap();
    private final UserManager mUserManager;

    ConfigurationMap(UserManager userManager) {
        this.mUserManager = userManager;
    }

    public WifiConfiguration put(WifiConfiguration config) {
        WifiConfiguration current = (WifiConfiguration) this.mPerID.put(Integer.valueOf(config.networkId), config);
        if (WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            this.mPerIDForCurrentUser.put(Integer.valueOf(config.networkId), config);
            if (config.FQDN != null && config.FQDN.length() > 0) {
                this.mPerFQDNForCurrentUser.put(config.FQDN, config);
            }
        }
        return current;
    }

    public WifiConfiguration remove(int netID) {
        WifiConfiguration config = (WifiConfiguration) this.mPerID.remove(Integer.valueOf(netID));
        if (config == null) {
            return null;
        }
        this.mPerIDForCurrentUser.remove(Integer.valueOf(netID));
        Iterator<Entry<String, WifiConfiguration>> entries = this.mPerFQDNForCurrentUser.entrySet().iterator();
        while (entries.hasNext()) {
            if (((WifiConfiguration) ((Entry) entries.next()).getValue()).networkId == netID) {
                entries.remove();
                break;
            }
        }
        return config;
    }

    public void clear() {
        this.mPerID.clear();
        this.mPerIDForCurrentUser.clear();
        this.mPerFQDNForCurrentUser.clear();
    }

    public void setNewUser(int userId) {
        this.mCurrentUserId = userId;
    }

    public WifiConfiguration getForAllUsers(int netid) {
        return (WifiConfiguration) this.mPerID.get(Integer.valueOf(netid));
    }

    public WifiConfiguration getForCurrentUser(int netid) {
        return (WifiConfiguration) this.mPerIDForCurrentUser.get(Integer.valueOf(netid));
    }

    public int sizeForAllUsers() {
        return this.mPerID.size();
    }

    public int sizeForCurrentUser() {
        return this.mPerIDForCurrentUser.size();
    }

    public WifiConfiguration getByFQDNForCurrentUser(String fqdn) {
        return (WifiConfiguration) this.mPerFQDNForCurrentUser.get(fqdn);
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

    public Collection<WifiConfiguration> getEnabledNetworksForCurrentUser() {
        List<WifiConfiguration> list = new ArrayList();
        for (WifiConfiguration config : this.mPerIDForCurrentUser.values()) {
            if (config.status != 1) {
                list.add(config);
            }
        }
        return list;
    }

    public WifiConfiguration getEphemeralForCurrentUser(String ssid) {
        for (WifiConfiguration config : this.mPerIDForCurrentUser.values()) {
            if (ssid.equals(config.SSID) && config.ephemeral) {
                return config;
            }
        }
        return null;
    }

    public Collection<WifiConfiguration> valuesForAllUsers() {
        return this.mPerID.values();
    }

    public Collection<WifiConfiguration> valuesForCurrentUser() {
        return this.mPerIDForCurrentUser.values();
    }
}
