package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import com.android.server.wifi.WifiConfigurationUtil;
import java.util.List;

public interface IHwWifiConfigManagerEx {
    void disableAllNetworksNative();

    void enableAllNetworks();

    void enableSimNetworks();

    WifiConfigurationUtil.WifiConfigurationComparator get();

    void initLastPriority();

    void partOfAddOrUpdateNetworkInternal(WifiConfiguration wifiConfiguration, WifiConfiguration wifiConfiguration2);

    String partOfDisableNetWork(int i, int i2);

    void partOfRetrieveHiddenNetworkList(List<WifiConfiguration> list, List<WifiConfiguration> list2, List<WifiConfiguration> list3);

    boolean updatePriority(WifiConfiguration wifiConfiguration, int i);
}
