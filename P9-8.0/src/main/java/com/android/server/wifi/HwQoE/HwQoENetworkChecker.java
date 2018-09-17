package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.os.Handler;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.wifipro.HwNetworkPropertyRechecker;

public class HwQoENetworkChecker {
    private HwNetworkPropertyRechecker mHwNetworkPropertyRechecker;
    private NetworkCheckThread mNetworkCheckThread;

    private class NetworkCheckThread extends Thread {
        private Handler mHandler;

        public NetworkCheckThread(Handler handler) {
            this.mHandler = handler;
        }

        public void run() {
            HwQoEUtils.logE("NetworkCheckThread run");
            int respCode = HwQoENetworkChecker.this.mHwNetworkPropertyRechecker.isCaptivePortal(false);
            HwQoEUtils.logE("NetworkCheckThread respCode = " + respCode);
            HwQoENetworkChecker.this.mHwNetworkPropertyRechecker.release();
            if (respCode == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                this.mHandler.sendEmptyMessage(103);
            } else {
                this.mHandler.sendEmptyMessage(104);
            }
        }
    }

    public HwQoENetworkChecker(Context context, Handler handler) {
        this.mHwNetworkPropertyRechecker = new HwNetworkPropertyRechecker(context, null, null, true, null, null);
        this.mNetworkCheckThread = new NetworkCheckThread(handler);
    }

    public void start() {
        HwQoEUtils.logE("NetworkCheckThread start");
        this.mNetworkCheckThread.start();
    }
}
