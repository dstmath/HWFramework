package com.android.server.hidata.channelqoe;

public interface IChannelQoECallback {
    public static final int BAD = 1;
    public static final int CONNECT_AVAILABLE = 0;
    public static final int CONNECT_SIGNAL_POOR = 2;
    public static final int CONNECT_TIMEOUT = 1;
    public static final int CONNECT_UNKNOWN = -1;
    public static final int GOOD = 0;
    public static final int NETWORK_TYPE_CELL = 801;
    public static final int NETWORK_TYPE_WIFI = 800;
    public static final int SIGNAL_STATE_NETWORK_TYPE_CELL_3G = 2;
    public static final int SIGNAL_STATE_NETWORK_TYPE_CELL_4G = 1;
    public static final int SIGNAL_STATE_NETWORK_TYPE_NULL = -1;
    public static final int SIGNAL_STATE_NETWORK_TYPE_WIFI = 0;
    public static final int UNKNOWN = 2;

    void onCellPSAvailable(boolean z, int i);

    void onChannelQuality(int i, int i2, int i3, int i4);

    void onCurrentRtt(int i);

    void onWifiLinkQuality(int i, int i2, int i3);
}
