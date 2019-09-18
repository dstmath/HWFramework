package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import com.android.server.wifi.util.ApConfigUtil;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsSoftApManager {
    public int updateApChannelConfig(WifiNative wifiNative, String countryCode, ArrayList<Integer> allowed2GChannels, WifiConfiguration config) {
        return ApConfigUtil.updateApChannelConfig(wifiNative, countryCode, allowed2GChannels, config);
    }

    public int getApChannel(WifiConfiguration config) {
        return config.apChannel;
    }

    public void notifyApLinkedStaListChange(Bundle bundle) {
    }

    public List<String> getApLinkedStaList() {
        return null;
    }

    public boolean isHideBroadcastSsid() {
        return false;
    }
}
