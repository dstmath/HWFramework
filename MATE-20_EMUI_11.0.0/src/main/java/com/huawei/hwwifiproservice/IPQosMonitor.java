package com.huawei.hwwifiproservice;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class IPQosMonitor {
    private static final int CMD_GET_RTT = 19;
    private static final int CMD_NOTIFY_MCC = 17;
    private static final int CMD_QUERY_PKTS = 15;
    private static final int CMD_QUERY_RETRANS_RTO = 16;
    private static final int CMD_RESET_RTT = 18;
    private static final int CMD_START_MONITOR = 10;
    private static final int CMD_STOP_MONITOR = 11;
    public static final int MSG_REPORT_IPQOS = 100;
    private static final String TAG = "QosMonitor";
    private static final int WIFIPRO_MOBILE_BQE_RTT = 2;
    private static final int WIFIPRO_WLAN_BQE_RTT = 1;
    private static final int WIFIPRO_WLAN_SAMPLE_RTT = 3;
    private static boolean isAPK = false;
    private Handler mHandler;
    private boolean mInit = false;

    public IPQosMonitor(Handler h) {
        this.mHandler = h;
        this.mInit = true;
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

    public void notifyMCC(int mccCode) {
        if (this.mInit) {
            sendcmd(17, mccCode);
        } else {
            Log.e(TAG, "notifyMCC:IPQos is not initial!!");
        }
    }

    public void resetRtt(int rttType) {
        if (!this.mInit) {
            Log.e(TAG, "resetRtt:IPQos is not initial!!");
        } else if (rttType == 1 || rttType == 2 || rttType == 3) {
            Log.i(TAG, "resetRtt: rttType = " + rttType);
            sendcmd(18, rttType);
        } else {
            Log.e(TAG, "resetRtt: unvalid rttType!!");
        }
    }

    public void queryRtt(int rttType) {
        if (!this.mInit) {
            Log.e(TAG, "queryRtt:IPQos is not initial!!");
        } else if (rttType == 1 || rttType == 2 || rttType == 3) {
            Log.i(TAG, "queryRtt: rttType = " + rttType);
            sendcmd(19, rttType);
        } else {
            Log.e(TAG, "queryRtt: unvalid rttType!!");
        }
    }

    public int sendcmd(int cmd, int arg) {
        Bundle data = new Bundle();
        data.putInt("cmd", cmd);
        data.putInt("arg", arg);
        Bundle resultCmd = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 8, data);
        if (resultCmd == null) {
            Log.e(TAG, "sendcmd: get Bundle fail, return");
            return -1;
        }
        int[] result = resultCmd.getIntArray("resultCmd");
        if (result == null) {
            return -1;
        }
        if (result.length < 10) {
            return 0;
        }
        postEventFromNative(100, result[9], result.length, result);
        return 0;
    }
}
