package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Message;
import com.android.internal.util.State;

public interface IHwSoftApManagerEx {
    boolean checkOpenHotsoptPolicy(WifiConfiguration wifiConfiguration);

    void logStateAndMessage(State state, Message message);
}
