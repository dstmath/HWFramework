package com.android.server.hidata.channelqoe;

public class HwCHQciConfig {
    private static final int DEFAULT_INT_VALUE = 0;
    private static final int DEFAULT_QCI_CHLOAD = 200;
    private static final int DEFAULT_QCI_RSSI = -65;
    private static final int DEFAULT_QCI_RTT = 300;
    private static final int DEFAULT_QCI_TPUT = 0;
    public int mChload = 0;
    public int mQci = 0;
    public int mRssi = 0;
    public int mRtt = 0;
    public int mTput = 0;

    public static HwCHQciConfig getDefalultQci() {
        HwCHQciConfig config = new HwCHQciConfig();
        config.mQci = 0;
        config.mRtt = 300;
        config.mRssi = -65;
        config.mChload = 200;
        config.mTput = 0;
        return config;
    }
}
