package com.android.server.hidata.histream;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class HwHiStreamUdpMonitor {
    private static final int BAD_TIMES_THRESHOLD = 3;
    private static final int BIT_UNIT = 8;
    private static final int BYTE_UNIT = 1024;
    private static final Object LOCK_OBJECT = new Object();
    private static final int MSG_GET_UDP_INFO = 1;
    private static final int PERCENTAGE_UNIT = 100;
    private static final float RX_RATE_THRESHOLD = 100.0f;
    private static final int SEND_MAX_PERIOD_TIME = 5000;
    private static final int SEND_PERIOD_TIME = 1000;
    private static final int VIDEO_STALL_REPORT_MIN_INTERVAL = 6;
    private static volatile HwHiStreamUdpMonitor mHwHiStreamUdpMonitor;
    private boolean isMonitoring = false;
    private Context mContext;
    private long mFirstRxBytes = 0;
    private Handler mHandler;
    private Handler mLocalHandler;
    private int mScenesId = 0;
    private int mUid = 0;

    private HwHiStreamUdpMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    public static HwHiStreamUdpMonitor getInstance(Context context, Handler handler) {
        if (mHwHiStreamUdpMonitor == null) {
            synchronized (LOCK_OBJECT) {
                if (mHwHiStreamUdpMonitor == null) {
                    mHwHiStreamUdpMonitor = new HwHiStreamUdpMonitor(context, handler);
                }
            }
        }
        return mHwHiStreamUdpMonitor;
    }

    public void startMonitor(int uid, int scenesId) {
        HwHiStreamUtils.logD(false, "Start HwHiStreamUdpMonitor, appUid = %{public}d, scenesId = %{public}d", Integer.valueOf(uid), Integer.valueOf(scenesId));
        if (!this.isMonitoring) {
            initHandlerThread();
            this.mFirstRxBytes = 0;
            this.isMonitoring = true;
            this.mUid = uid;
            this.mScenesId = scenesId;
            this.mLocalHandler.sendEmptyMessage(1);
        } else if (uid != this.mUid) {
            stopMonitor();
            startMonitor(uid, scenesId);
        }
    }

    public void stopMonitor() {
        HwHiStreamUtils.logD(false, "Stop HwHiStreamUdpMonitor", new Object[0]);
        if (this.isMonitoring) {
            this.isMonitoring = false;
            this.mFirstRxBytes = 0;
            this.mUid = 0;
            this.mScenesId = 0;
            this.mLocalHandler.removeMessages(1);
            release();
        }
    }

    private void release() {
        Looper looper;
        Handler handler = this.mLocalHandler;
        if (handler != null && (looper = handler.getLooper()) != null && looper != Looper.getMainLooper()) {
            looper.quitSafely();
        }
    }

    private void initHandlerThread() {
        HandlerThread handlerThread = new HandlerThread("HwHiStreamUdpMonitor Thread");
        handlerThread.start();
        this.mLocalHandler = new LocalHandler(handlerThread.getLooper());
    }

    public class LocalHandler extends Handler {
        private int mBadUdpTimes = 0;
        private long rxBytesResult = 0;
        private float rxRate = 0.0f;

        public LocalHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                this.rxBytesResult = TrafficStats.getUidRxBytes(HwHiStreamUdpMonitor.this.mUid);
                if (HwHiStreamUdpMonitor.this.mFirstRxBytes != 0) {
                    HwHiStreamUdpMonitor hwHiStreamUdpMonitor = HwHiStreamUdpMonitor.this;
                    this.rxRate = hwHiStreamUdpMonitor.computeRxRate(hwHiStreamUdpMonitor.mFirstRxBytes, this.rxBytesResult);
                    if (this.rxRate < HwHiStreamUdpMonitor.RX_RATE_THRESHOLD) {
                        this.mBadUdpTimes++;
                    } else {
                        this.mBadUdpTimes = 0;
                    }
                    int i = this.mBadUdpTimes;
                    if (i == 3) {
                        HwHiStreamUdpMonitor.this.sendToArbitration(i);
                        this.rxBytesResult = 0;
                        this.mBadUdpTimes = 0;
                        sendEmptyMessageDelayed(1, 5000);
                    } else {
                        sendEmptyMessageDelayed(1, 1000);
                    }
                } else {
                    sendEmptyMessageDelayed(1, 1000);
                }
                HwHiStreamUdpMonitor.this.mFirstRxBytes = this.rxBytesResult;
            }
            super.handleMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendToArbitration(int badTimes) {
        HwHiStreamUtils.logD(false, "Video stream udp monitor detect freeze and send message to brain", Integer.valueOf(this.mScenesId));
        Bundle bundle = new Bundle();
        bundle.putInt("appSceneId", this.mScenesId);
        bundle.putInt("detectResult", (badTimes * 100) / 6);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(9, bundle));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float computeRxRate(long startRxBytes, long endRxBytes) {
        long rxBytes = endRxBytes - startRxBytes;
        float rxRate = ((float) (8 * rxBytes)) / 1024.0f;
        HwHiStreamUtils.logD(false, "Current video stream trafficStats: rxByte = %{public}s, rxRate: %{public}s", String.valueOf(rxBytes), String.valueOf(rxRate));
        return rxRate;
    }
}
