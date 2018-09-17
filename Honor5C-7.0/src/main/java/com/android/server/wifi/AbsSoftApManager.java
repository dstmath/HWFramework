package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import com.android.server.wifi.util.ApConfigUtil;
import java.util.ArrayList;

public abstract class AbsSoftApManager {
    public int updateApChannelConfig(WifiNative wifiNative, String countryCode, ArrayList<Integer> allowed2GChannels, WifiConfiguration config) {
        return ApConfigUtil.updateApChannelConfig(wifiNative, countryCode, allowed2GChannels, config);
    }
}
