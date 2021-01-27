package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.UserManager;
import com.android.server.wifi.WifiConfigurationUtil;
import java.util.Collection;
import java.util.List;

public interface IHwWifiConfigManagerInner {
    NetworkUpdateResult addOrUpdateNetwork_2(WifiConfiguration wifiConfiguration, int i);

    WifiConfigurationUtil.WifiConfigurationComparator get();

    int getCurrentUserId();

    Collection<WifiConfiguration> getInternalConfiguredNetworks_2();

    ConfigurationMap getMConfiguredNetworks();

    UserManager getManager();

    List<WifiConfiguration> getSavedNetworks(int i);

    void sendConfiguredNetworksChangedBroadcast_2();

    boolean tryEnableNetwork_2(WifiConfiguration wifiConfiguration);

    boolean updateNetworkSelectionStatus_2(WifiConfiguration wifiConfiguration, int i);
}
