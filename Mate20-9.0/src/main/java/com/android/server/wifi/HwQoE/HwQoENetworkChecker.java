package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.os.Handler;
import com.android.server.HwNetworkPropertyChecker;

public class HwQoENetworkChecker {
    /* access modifiers changed from: private */
    public HwNetworkPropertyChecker mHwNetworkPropertychecker;
    private NetworkCheckThread mNetworkCheckThread;

    private class NetworkCheckThread extends Thread {
        private Handler mHandler;

        public NetworkCheckThread(Handler handler) {
            this.mHandler = handler;
        }

        public void run() {
            HwQoEUtils.logE("NetworkCheckThread run");
            int respCode = HwQoENetworkChecker.this.mHwNetworkPropertychecker.isCaptivePortal(true);
            HwQoEUtils.logE("NetworkCheckThread respCode = " + respCode);
            HwQoENetworkChecker.this.mHwNetworkPropertychecker.release();
            if (respCode == 204) {
                this.mHandler.sendEmptyMessage(103);
            } else {
                this.mHandler.sendEmptyMessage(104);
            }
        }
    }

    public HwQoENetworkChecker(Context context, Handler handler) {
        HwNetworkPropertyChecker hwNetworkPropertyChecker = new HwNetworkPropertyChecker(context, null, null, true, null, false);
        this.mHwNetworkPropertychecker = hwNetworkPropertyChecker;
        this.mNetworkCheckThread = new NetworkCheckThread(handler);
    }

    public void start() {
        HwQoEUtils.logE("NetworkCheckThread start");
        this.mNetworkCheckThread.start();
    }
}
