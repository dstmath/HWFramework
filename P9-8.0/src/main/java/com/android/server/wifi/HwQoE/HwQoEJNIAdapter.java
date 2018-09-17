package com.android.server.wifi.HwQoE;

public class HwQoEJNIAdapter {
    private static final int CMD_GET_RTT = 19;
    private static final int CMD_QUERY_PKTS = 15;
    private static final int CMD_RESET_RTT = 18;
    private static final int CMD_START_MONITOR = 10;
    private static final int WIFIPRO_MOBILE_BQE_RTT = 2;
    private static final int WIFIPRO_WLAN_BQE_RTT = 1;
    private static final int WIFIPRO_WLAN_SAMPLE_RTT = 3;
    private static HwQoEJNIAdapter mHwQoEJNIAdapter = null;

    private native HwQoEUdpNetWorkInfo nativeReadUdpNetworkStatsDetail(int i, int i2);

    public native int[] nativeSendQoECmd(int i, int i2);

    public native int nativeSetDpiMarkRule(int i, int i2, int i3);

    static {
        try {
            System.loadLibrary("hwqoe_jni");
            HwQoEUtils.logD("HwQoEJNIAdapter loading JNI succ");
        } catch (UnsatisfiedLinkError e) {
            HwQoEUtils.logE("LoadLibrary is error " + e.toString());
        }
    }

    private HwQoEJNIAdapter() {
        HwQoEUtils.logD("HwQoEJNIAdapter");
        sendQoECmd(10, 0);
    }

    public static synchronized HwQoEJNIAdapter getInstance() {
        HwQoEJNIAdapter hwQoEJNIAdapter;
        synchronized (HwQoEJNIAdapter.class) {
            if (mHwQoEJNIAdapter == null) {
                HwQoEUtils.logD("new HwQoEJNIAdapter()");
                mHwQoEJNIAdapter = new HwQoEJNIAdapter();
            }
            hwQoEJNIAdapter = mHwQoEJNIAdapter;
        }
        return hwQoEJNIAdapter;
    }

    public synchronized HwQoENetWorkInfo queryPeriodData() {
        HwQoENetWorkInfo info;
        info = new HwQoENetWorkInfo();
        int[] result = sendQoECmd(15, 0);
        if (result != null && result.length >= 10) {
            info.mTcpRTT = (long) result[0];
            info.mTcpRTTPacket = (long) result[1];
            info.mTcpRTTWhen = result[2];
            info.mTcpCongestion = (long) result[3];
            info.mTcpCong_when = (long) result[4];
            info.mTcpQuality = result[5];
            info.mTcpTxPacket = (long) result[6];
            info.mTcpRxPacket = (long) result[7];
            info.mTcpRetransPacket = (long) result[8];
        }
        return info;
    }

    public synchronized int[] sendQoECmd(int cmd, int arg) {
        return nativeSendQoECmd(cmd, arg);
    }

    public synchronized int setDpiMarkRule(int uid, int protocol, int enable) {
        return nativeSetDpiMarkRule(uid, protocol, enable);
    }

    public synchronized void resetRtt(int rtt_type) {
        if (rtt_type == 1 || rtt_type == 2 || rtt_type == 3) {
            HwQoEUtils.logD("resetRtt: rtt_type = " + rtt_type);
            sendQoECmd(18, rtt_type);
        } else {
            HwQoEUtils.logD("resetRtt: unvalid rtt_type!!");
        }
    }

    public synchronized int[] queryBQERttResult(int rtt_type) {
        int[] result;
        result = null;
        if (rtt_type == 1 || rtt_type == 2 || rtt_type == 3) {
            HwQoEUtils.logD("queryRtt: queryBQERttResult = " + rtt_type);
            result = sendQoECmd(19, rtt_type);
        } else {
            HwQoEUtils.logD("queryRtt: unvalid rtt_type!!");
        }
        return result;
    }

    /* JADX WARNING: Missing block: B:10:0x0014, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized HwQoEUdpNetWorkInfo getUdpNetworkStatsDetail(int uid, int network) {
        if (uid < 0) {
            return null;
        }
        HwQoEUdpNetWorkInfo mHwQoEUdpNetWorkInfo = nativeReadUdpNetworkStatsDetail(uid, network);
        if (mHwQoEUdpNetWorkInfo != null) {
            mHwQoEUdpNetWorkInfo.setTimestamp(System.currentTimeMillis());
        }
    }
}
