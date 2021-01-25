package com.android.server.wifi.HwQoE;

import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.HwQoEUdpNetWorkInfo;

public class HwQoEJNIAdapter {
    private static final int CMD_GET_RTT = 19;
    private static final int CMD_QUERY_PKTS = 15;
    private static final int CMD_RESET_RTT = 18;
    private static final int CMD_START_MONITOR = 10;
    private static final int MAX_RESULT_LEN = 10;
    private static final int WIFIPRO_MOBILE_BQE_RTT = 2;
    private static final int WIFIPRO_WLAN_BQE_RTT = 1;
    private static final int WIFIPRO_WLAN_SAMPLE_RTT = 3;
    private static HwQoEJNIAdapter mHwQoEJNIAdapter = null;
    private HwHidataJniAdapter mHwHidataJniAdapter = HwHidataJniAdapter.getInstance();

    private HwQoEJNIAdapter() {
        HwQoEUtils.logD(false, "HwQoEJNIAdapter", new Object[0]);
        sendQoECmd(10, 0);
    }

    public static synchronized HwQoEJNIAdapter getInstance() {
        HwQoEJNIAdapter hwQoEJNIAdapter;
        synchronized (HwQoEJNIAdapter.class) {
            if (mHwQoEJNIAdapter == null) {
                HwQoEUtils.logD(false, "new HwQoEJNIAdapter()", new Object[0]);
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

    public final synchronized int[] sendQoECmd(int cmd, int arg) {
        return this.mHwHidataJniAdapter.sendQoECmd(cmd, arg);
    }

    public synchronized int setDpiMarkRule(int uid, int protocol, int enable) {
        return this.mHwHidataJniAdapter.setDpiMarkRule(uid, protocol, enable);
    }

    public synchronized void resetRtt(int rttType) {
        if (rttType == 1 || rttType == 2 || rttType == 3) {
            HwQoEUtils.logD(false, "resetRtt: rtt_type = %{public}d", Integer.valueOf(rttType));
            sendQoECmd(18, rttType);
        } else {
            HwQoEUtils.logD(false, "resetRtt: unvalid rtt_type!!", new Object[0]);
        }
    }

    public synchronized int[] queryBQERttResult(int rttType) {
        int[] result;
        result = null;
        if (rttType == 1 || rttType == 2 || rttType == 3) {
            HwQoEUtils.logD(false, "queryRtt: queryBQERttResult = %{public}d", Integer.valueOf(rttType));
            result = sendQoECmd(19, rttType);
        } else {
            HwQoEUtils.logD(false, "queryRtt: unvalid rtt_type!!", new Object[0]);
        }
        return result;
    }

    public synchronized HwQoEUdpNetWorkInfo getUdpNetworkStatsDetail(int uid, int network) {
        if (uid < 0) {
            return null;
        }
        HwQoEUdpNetWorkInfo mHwQoEUdpNetWorkInfo = this.mHwHidataJniAdapter.readUdpNetworkStatsDetail(uid, network);
        if (mHwQoEUdpNetWorkInfo != null) {
            mHwQoEUdpNetWorkInfo.setTimestamp(System.currentTimeMillis());
            mHwQoEUdpNetWorkInfo.setNetworkID(network);
        }
        return mHwQoEUdpNetWorkInfo;
    }
}
