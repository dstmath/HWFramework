package com.android.server.wifi.wifipro;

import android.os.Handler;
import android.util.Log;
import com.android.server.wifi.HwQoE.HwQoEJNIAdapter;

public class IPQosMonitor {
    private static final int CMD_GET_RTT = 19;
    private static final int CMD_NOTIFY_MCC = 17;
    private static final int CMD_QUERY_PKTS = 15;
    private static final int CMD_QUERY_RETRANS_RTO = 16;
    private static final int CMD_RESET_RTT = 18;
    private static final int CMD_START_MONITOR = 10;
    private static final int CMD_STOP_MONITOR = 11;
    public static final int MSG_REPORT_IPQOS = 100;
    private static String TAG = "QosMonitor";
    private static final int WIFIPRO_MOBILE_BQE_RTT = 2;
    private static final int WIFIPRO_WLAN_BQE_RTT = 1;
    private static final int WIFIPRO_WLAN_SAMPLE_RTT = 3;
    private static boolean isAPK = false;
    private Handler mHandler;
    private HwQoEJNIAdapter mHwQoEJNIAdapter;
    private boolean mInit = false;

    public IPQosMonitor(Handler h) {
        this.mHandler = h;
        if (!this.mInit) {
            this.mHwQoEJNIAdapter = HwQoEJNIAdapter.getInstance();
            this.mInit = true;
        }
    }

    private synchronized void postEventFromNative(int what, int arg1, int arg2, Object obj) {
        Log.i(TAG, "postEventFromNative: msg=" + what + ",arg1=" + arg1 + ",arg2=" + arg2);
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(what, arg1, arg2, obj));
        }
    }

    public void init() {
    }

    public void release() {
    }

    public void startMonitor() {
    }

    public void stopMonitor() {
    }

    public synchronized void queryPackets(int arg) {
        if (this.mInit) {
            sendcmd(15, arg);
        } else {
            Log.e(TAG, "query:IPQos is not initial!!");
        }
    }

    public void notifyMCC(int mcc_code) {
        if (this.mInit) {
            sendcmd(17, mcc_code);
        } else {
            Log.e(TAG, "notifyMCC:IPQos is not initial!!");
        }
    }

    public void resetRtt(int rtt_type) {
        if (!this.mInit) {
            Log.e(TAG, "resetRtt:IPQos is not initial!!");
        } else if (rtt_type == 1 || rtt_type == 2 || rtt_type == 3) {
            Log.i(TAG, "resetRtt: rtt_type = " + rtt_type);
            sendcmd(18, rtt_type);
        } else {
            Log.e(TAG, "resetRtt: unvalid rtt_type!!");
        }
    }

    public void queryRtt(int rtt_type) {
        if (!this.mInit) {
            Log.e(TAG, "queryRtt:IPQos is not initial!!");
        } else if (rtt_type == 1 || rtt_type == 2 || rtt_type == 3) {
            Log.i(TAG, "queryRtt: rtt_type = " + rtt_type);
            sendcmd(19, rtt_type);
        } else {
            Log.e(TAG, "queryRtt: unvalid rtt_type!!");
        }
    }

    public int sendcmd(int cmd, int arg) {
        if (this.mHwQoEJNIAdapter == null) {
            return -1;
        }
        int[] result = this.mHwQoEJNIAdapter.sendQoECmd(cmd, arg);
        if (result != null && result.length >= 10) {
            postEventFromNative(100, result[9], result.length, result);
        }
        return 0;
    }
}
