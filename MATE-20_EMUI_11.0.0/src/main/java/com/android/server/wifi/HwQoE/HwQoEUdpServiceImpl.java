package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.HwQoEUdpNetWorkInfo;

public class HwQoEUdpServiceImpl {
    private static final int HALF_NUMBER = 2;
    private static final int KILO_BYTE = 1024;
    private static final int MAX_UDP_NO_ACCESS_SCORE = 2;
    private static final int MILLSECONDS_PER_SECOND = 1000;
    private static final int MSG_UDP_ACCESS_MONITOR_ENABLE = 101;
    private static final int MSG_UDP_BASE = 100;
    private static final int MSG_UDP_TRAFFIC_SPEED = 102;
    private static final int NO_ACCESS_STATISTICS_UDP_SOCKETS_SUM = 5;
    private static final int NO_ACCESS_STATISTICS_UDP_UID_SUM = 3;
    private static final int SYSTEM_APP_UID = 1000;
    private static final String TAG = "HwQoEUdpServiceImpl";
    private static final long UDP_ACCESS_MONITOR_INTERVAL = 2000;
    private static final int UDP_MONITOR_INTERVAL_MAX = 30000;
    private static final int UID_ALL = 0;
    private int mAddScore;
    HwQoEUdpNetWorkInfo mCurrUdpInfoForMonitor;
    HwQoEUdpNetWorkInfo mCurrUdpInfoForUID;
    UdpNetworkSpeed mCurrUdpSpeed;
    private HwQoEJNIAdapter mHwQoEJNIAdapter;
    private IHwQoEMonitorCallback mHwQoEMonitorCallback;
    private Handler mHwQoEUdpServiceImplHandler;
    private boolean mIsUDPAccessMonitorEnabled;
    HwQoEUdpNetWorkInfo mLastUdpInfoForMonitor;
    private int mUDPMonitorInterval = 0;
    private int mUdpNoAccessScore;

    public HwQoEUdpServiceImpl(Context context, IHwQoEMonitorCallback callback) {
        this.mHwQoEMonitorCallback = callback;
        this.mHwQoEJNIAdapter = HwQoEJNIAdapter.getInstance();
        initHwQoEUdpServiceImplHandler();
    }

    public synchronized HwQoEUdpNetWorkInfo getUdpNetworkStatsDetail(int uid) {
        return null;
    }

    public void setUdpInternetAccessMonitorEnabled(boolean enabled) {
        HwHiLog.d(TAG, false, "setUdpInternetAccessMonitorEnabled:%{public}s", new Object[]{String.valueOf(enabled)});
        this.mIsUDPAccessMonitorEnabled = enabled;
        if (!this.mIsUDPAccessMonitorEnabled) {
            this.mUdpNoAccessScore = 0;
            this.mAddScore = 0;
            this.mCurrUdpInfoForMonitor = null;
            this.mLastUdpInfoForMonitor = null;
            if (this.mHwQoEUdpServiceImplHandler.hasMessages(101)) {
                this.mHwQoEUdpServiceImplHandler.removeMessages(101);
            }
        } else if (!this.mHwQoEUdpServiceImplHandler.hasMessages(101)) {
            this.mHwQoEUdpServiceImplHandler.sendEmptyMessage(101);
        }
    }

    public void setInterval(int interval) {
        this.mUDPMonitorInterval = interval;
        HwHiLog.d(TAG, false, "interval: %{public}d", new Object[]{Integer.valueOf(interval)});
    }

    private void initHwQoEUdpServiceImplHandler() {
        HandlerThread handlerThread = new HandlerThread("hw_udpqoe_handler_thread");
        handlerThread.start();
        this.mHwQoEUdpServiceImplHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.HwQoE.HwQoEUdpServiceImpl.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 101) {
                    HwQoEUdpServiceImpl hwQoEUdpServiceImpl = HwQoEUdpServiceImpl.this;
                    hwQoEUdpServiceImpl.mCurrUdpInfoForMonitor = hwQoEUdpServiceImpl.mHwQoEJNIAdapter.getUdpNetworkStatsDetail(0, 1);
                    HwQoEUdpServiceImpl hwQoEUdpServiceImpl2 = HwQoEUdpServiceImpl.this;
                    hwQoEUdpServiceImpl2.mAddScore = hwQoEUdpServiceImpl2.calculateNewUdpAccessScore(hwQoEUdpServiceImpl2.mCurrUdpInfoForMonitor, HwQoEUdpServiceImpl.this.mLastUdpInfoForMonitor);
                    HwQoEUdpServiceImpl.this.mUdpNoAccessScore += HwQoEUdpServiceImpl.this.mAddScore;
                    HwQoEUdpServiceImpl hwQoEUdpServiceImpl3 = HwQoEUdpServiceImpl.this;
                    hwQoEUdpServiceImpl3.mUdpNoAccessScore = Math.max(0, hwQoEUdpServiceImpl3.mUdpNoAccessScore);
                    HwHiLog.d(HwQoEUdpServiceImpl.TAG, false, "mUdpNoAccessScore: %{public}d, mAddScore: %{public}d", new Object[]{Integer.valueOf(HwQoEUdpServiceImpl.this.mUdpNoAccessScore), Integer.valueOf(HwQoEUdpServiceImpl.this.mAddScore)});
                    if (HwQoEUdpServiceImpl.this.mLastUdpInfoForMonitor == null) {
                        HwQoEUdpServiceImpl.this.mLastUdpInfoForMonitor = new HwQoEUdpNetWorkInfo();
                    }
                    HwQoEUdpServiceImpl.this.mLastUdpInfoForMonitor.setUdpNetWorkInfo(HwQoEUdpServiceImpl.this.mCurrUdpInfoForMonitor);
                    if (HwQoEUdpServiceImpl.this.mIsUDPAccessMonitorEnabled) {
                        if (HwQoEUdpServiceImpl.this.mUdpNoAccessScore >= 2 && HwQoEUdpServiceImpl.this.mHwQoEMonitorCallback != null && HwQoEUdpServiceImpl.this.mAddScore > 0) {
                            HwQoEUdpServiceImpl.this.mHwQoEMonitorCallback.onUDPInternetAccessStatusChange(true);
                        }
                        HwQoEUdpServiceImpl.this.mHwQoEUdpServiceImplHandler.sendEmptyMessageDelayed(101, (long) HwQoEUdpServiceImpl.this.mUDPMonitorInterval);
                    }
                } else if (i == 102) {
                    HwQoEUdpServiceImpl hwQoEUdpServiceImpl4 = HwQoEUdpServiceImpl.this;
                    hwQoEUdpServiceImpl4.mCurrUdpInfoForUID = hwQoEUdpServiceImpl4.mHwQoEJNIAdapter.getUdpNetworkStatsDetail(1000, 1);
                    if (HwQoEUdpServiceImpl.this.mLastUdpInfoForMonitor != null) {
                        HwQoEUdpServiceImpl hwQoEUdpServiceImpl5 = HwQoEUdpServiceImpl.this;
                        hwQoEUdpServiceImpl5.mCurrUdpSpeed = hwQoEUdpServiceImpl5.calculateUDPSpeed(hwQoEUdpServiceImpl5.mCurrUdpInfoForUID, HwQoEUdpServiceImpl.this.mLastUdpInfoForMonitor);
                    } else {
                        HwQoEUdpServiceImpl.this.mLastUdpInfoForMonitor = new HwQoEUdpNetWorkInfo();
                        HwQoEUdpServiceImpl.this.mLastUdpInfoForMonitor.setUdpNetWorkInfo(HwQoEUdpServiceImpl.this.mCurrUdpInfoForMonitor);
                    }
                    if (HwQoEUdpServiceImpl.this.mIsUDPAccessMonitorEnabled) {
                        HwQoEUdpServiceImpl.this.mHwQoEUdpServiceImplHandler.sendEmptyMessageDelayed(101, 1000);
                    }
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int calculateNewUdpAccessScore(HwQoEUdpNetWorkInfo currUdpInfo, HwQoEUdpNetWorkInfo lastUdpInfo) {
        char c;
        IHwQoEMonitorCallback iHwQoEMonitorCallback;
        int score = 0;
        if (currUdpInfo == null || lastUdpInfo == null) {
            return 0;
        }
        if (currUdpInfo.getUid() != lastUdpInfo.getUid()) {
            HwHiLog.d(TAG, false, "uid is error,ignore calculate score", new Object[0]);
            return 0;
        }
        long timestamp = currUdpInfo.getTimestamp() - lastUdpInfo.getTimestamp();
        if (timestamp <= 0 || timestamp > 30000) {
            return 0;
        }
        long rxUdpBytes = currUdpInfo.getRxUdpBytes() - lastUdpInfo.getRxUdpBytes();
        long rxUdpPackets = currUdpInfo.getRxUdpPackets() - lastUdpInfo.getRxUdpPackets();
        long rxSumPackets = currUdpInfo.getSumRxPackets() - lastUdpInfo.getSumRxPackets();
        int sumUdpSockets = (currUdpInfo.getSumUdpSockets() + lastUdpInfo.getSumUdpSockets()) / 2;
        int sumUdpUids = (currUdpInfo.getSumUdpUids() + lastUdpInfo.getSumUdpUids()) / 2;
        if (rxUdpBytes > 0 || rxUdpPackets > 0 || rxSumPackets > 0) {
            if (this.mUdpNoAccessScore < 2 || (iHwQoEMonitorCallback = this.mHwQoEMonitorCallback) == null) {
                c = 0;
            } else {
                c = 0;
                iHwQoEMonitorCallback.onUDPInternetAccessStatusChange(false);
            }
            score = 0 - this.mUdpNoAccessScore;
        } else if (sumUdpSockets > 5 || sumUdpUids > 3) {
            score = 2;
            c = 0;
        } else if (sumUdpSockets > 1 || sumUdpUids > 1) {
            score = 1;
            c = 0;
        } else {
            HwHiLog.d(TAG, false, "no condition match, score is 0", new Object[0]);
            c = 0;
        }
        Object[] objArr = new Object[6];
        objArr[c] = Integer.valueOf(score);
        objArr[1] = String.valueOf(rxUdpBytes);
        objArr[2] = String.valueOf(rxUdpPackets);
        objArr[3] = Integer.valueOf(sumUdpSockets);
        objArr[4] = Integer.valueOf(sumUdpUids);
        objArr[5] = String.valueOf(rxSumPackets);
        HwHiLog.d(TAG, false, "add: score:%{public}d, rxUdpBytes:%{public}s, rxUdpPackets:%{public}s, sumUdpSockets:%{public}d, sumUdpUids:%{public}d, rxSumPackets:%{public}s", objArr);
        return score;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UdpNetworkSpeed calculateUDPSpeed(HwQoEUdpNetWorkInfo currUdpInfo, HwQoEUdpNetWorkInfo lastUdpInfo) {
        HwHiLog.d(TAG, false, "calculateUDPSpeed:", new Object[0]);
        if (currUdpInfo == null || lastUdpInfo == null) {
            return null;
        }
        if (currUdpInfo.getUid() != lastUdpInfo.getUid()) {
            HwHiLog.d(TAG, false, "uid is error,ignore calculate speed", new Object[0]);
            return null;
        }
        long timestamp = currUdpInfo.getTimestamp() - lastUdpInfo.getTimestamp();
        long rxSpeedKbs = ((currUdpInfo.getRxUdpBytes() - lastUdpInfo.getRxUdpBytes()) / 1024) / (timestamp / 1000);
        long txSpeedKbs = ((currUdpInfo.getTxUdpBytes() - lastUdpInfo.getTxUdpBytes()) / 1024) / (timestamp / 1000);
        HwHiLog.d(TAG, false, "txSpeedKbs:%{public}s Kb/s, rxSpeedKbs:%{public}s Kb/s, timestamp:%{public}s", new Object[]{String.valueOf(txSpeedKbs), String.valueOf(rxSpeedKbs), String.valueOf(timestamp)});
        UdpNetworkSpeed networkSpeed = new UdpNetworkSpeed();
        networkSpeed.updateUdpSpeed(txSpeedKbs, rxSpeedKbs, currUdpInfo.getUid());
        return networkSpeed;
    }

    public void release() {
        Looper looper;
        Handler handler = this.mHwQoEUdpServiceImplHandler;
        if (handler != null && (looper = handler.getLooper()) != null && looper != Looper.getMainLooper()) {
            looper.quitSafely();
            HwQoEUtils.logD(false, "HwQoEUdpServiceImpl$HandlerThread::Release", new Object[0]);
        }
    }
}
