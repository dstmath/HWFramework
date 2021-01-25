package com.android.server.wifi;

public interface IHwWifiNativeInner {
    int[] getChannelsForBand(int i);

    String getClientInterfaceName();

    String getSoftApInterfaceName();
}
