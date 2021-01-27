package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.os.Handler;
import com.android.server.wifi.wifipro.HwWifiProServiceManager;

public class HwQoENetworkChecker {
    private HwWifiProServiceManager mHwWifiProServiceManager;
    private NetworkCheckThread mNetworkCheckThread;

    public HwQoENetworkChecker(Context context, Handler handler) {
        this.mHwWifiProServiceManager = HwWifiProServiceManager.createHwWifiProServiceManager(context);
        this.mNetworkCheckThread = new NetworkCheckThread(handler);
    }

    public void start() {
        HwQoEUtils.logE(false, "NetworkCheckThread start", new Object[0]);
        this.mNetworkCheckThread.start();
    }

    private class NetworkCheckThread extends Thread {
        private static final int HAVE_INTERNET_ACCESS = 204;
        private Handler mHandler;

        public NetworkCheckThread(Handler handler) {
            this.mHandler = handler;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            HwQoEUtils.logE(false, "NetworkCheckThread run", new Object[0]);
            int respCode = HwQoENetworkChecker.this.mHwWifiProServiceManager.getNetworkProbeRuslt(true);
            HwQoEUtils.logE(false, "NetworkCheckThread respCode = %{public}d", Integer.valueOf(respCode));
            if (respCode == HAVE_INTERNET_ACCESS) {
                this.mHandler.sendEmptyMessage(103);
            } else {
                this.mHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_MONITOR_NO_INTERNET);
            }
        }
    }
}
