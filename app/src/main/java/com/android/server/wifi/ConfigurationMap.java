package com.android.server.wifi;

import android.content.pm.UserInfo;
import android.net.wifi.WifiConfiguration;
import android.os.UserManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ConfigurationMap {
    private int mCurrentUserId;
    private final Set<Integer> mHiddenNetworkIdsForCurrentUser;
    private final Map<Integer, WifiConfiguration> mPerConfigKey;
    private final Map<String, WifiConfiguration> mPerFQDNForCurrentUser;
    private final Map<Integer, WifiConfiguration> mPerID;
    private final Map<Integer, WifiConfiguration> mPerIDForCurrentUser;
    private final UserManager mUserManager;

    ConfigurationMap(UserManager userManager) {
        this.mPerID = new HashMap();
        this.mPerConfigKey = new HashMap();
        this.mPerIDForCurrentUser = new HashMap();
        this.mPerFQDNForCurrentUser = new HashMap();
        this.mHiddenNetworkIdsForCurrentUser = new HashSet();
        this.mCurrentUserId = 0;
        this.mUserManager = userManager;
    }

    public WifiConfiguration put(WifiConfiguration config) {
        WifiConfiguration current = (WifiConfiguration) this.mPerID.put(Integer.valueOf(config.networkId), config);
        this.mPerConfigKey.put(Integer.valueOf(config.configKey().hashCode()), config);
        if (WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            this.mPerIDForCurrentUser.put(Integer.valueOf(config.networkId), config);
            if (config.FQDN != null && config.FQDN.length() > 0) {
                this.mPerFQDNForCurrentUser.put(config.FQDN, config);
            }
            if (config.hiddenSSID) {
                this.mHiddenNetworkIdsForCurrentUser.add(Integer.valueOf(config.networkId));
            }
        }
        return current;
    }

    public WifiConfiguration remove(int netID) {
        WifiConfiguration config = (WifiConfiguration) this.mPerID.remove(Integer.valueOf(netID));
        if (config == null) {
            return null;
        }
        this.mPerConfigKey.remove(Integer.valueOf(config.configKey().hashCode()));
        this.mPerIDForCurrentUser.remove(Integer.valueOf(netID));
        Iterator<Entry<String, WifiConfiguration>> entries = this.mPerFQDNForCurrentUser.entrySet().iterator();
        while (entries.hasNext()) {
            if (((WifiConfiguration) ((Entry) entries.next()).getValue()).networkId == netID) {
                entries.remove();
                break;
            }
        }
        this.mHiddenNetworkIdsForCurrentUser.remove(Integer.valueOf(netID));
        return config;
    }

    public void clear() {
        this.mPerID.clear();
        this.mPerConfigKey.clear();
        this.mPerIDForCurrentUser.clear();
        this.mPerFQDNForCurrentUser.clear();
        this.mHiddenNetworkIdsForCurrentUser.clear();
    }

    public List<WifiConfiguration> handleUserSwitch(int userId) {
        this.mPerIDForCurrentUser.clear();
        this.mPerFQDNForCurrentUser.clear();
        this.mHiddenNetworkIdsForCurrentUser.clear();
        List<UserInfo> previousUserProfiles = this.mUserManager.getProfiles(this.mCurrentUserId);
        this.mCurrentUserId = userId;
        List<UserInfo> currentUserProfiles = this.mUserManager.getProfiles(this.mCurrentUserId);
        List<WifiConfiguration> hiddenConfigurations = new ArrayList();
        for (Entry<Integer, WifiConfiguration> entry : this.mPerID.entrySet()) {
            WifiConfiguration config = (WifiConfiguration) entry.getValue();
            if (WifiConfigurationUtil.isVisibleToAnyProfile(config, currentUserProfiles)) {
                this.mPerIDForCurrentUser.put((Integer) entry.getKey(), config);
                if (config.FQDN != null && config.FQDN.length() > 0) {
                    this.mPerFQDNForCurrentUser.put(config.FQDN, config);
                }
                if (config.hiddenSSID) {
                    this.mHiddenNetworkIdsForCurrentUser.add(Integer.valueOf(config.networkId));
                }
            } else if (WifiConfigurationUtil.isVisibleToAnyProfile(config, previousUserProfiles)) {
                hiddenConfigurations.add(config);
            }
        }
        return hiddenConfigurations;
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

    public WifiConfiguration getByConfigKeyIDForAllUsers(int id) {
        return (WifiConfiguration) this.mPerConfigKey.get(Integer.valueOf(id));
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

    public Set<Integer> getHiddenNetworkIdsForCurrentUser() {
        return this.mHiddenNetworkIdsForCurrentUser;
    }
}
