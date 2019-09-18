package com.android.server.hidata.appqoe;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;

public class HwAPKQoEQualityMonitor {
    private static final int BAD_AVE_RTT = 800;
    private static final int CMD_QUERY_HIDATA_INFO = 23;
    private static final int CMD_QUERY_PKTS = 15;
    private static final float LESS_PKTS_BAD_RATE = 0.3f;
    private static final float LESS_PKTS_VERY_BAD_RATE = 0.4f;
    private static final int MIN_PERIOD_TIME = 1000;
    private static final int MIN_RX_PKTS = 100;
    private static final int MIN_TX_PKTS = 3;
    private static final float MORE_PKTS_BAD_RATE = 0.2f;
    private static final float MORE_PKTS_VERY_BAD_RATE = 0.3f;
    private static final int MORE_TX_PKTS = 20;
    private static final int MSG_GET_TCP_INFO = 2;
    private static final int MSG_INIT_TCP_INFO = 1;
    private static final int STRONG_RSSI = 2;
    private static String TAG = "HiData_HwAPKQoEQualityMonitor";
    private static final float TX_GOOD_RATE = 0.2f;
    private static final int VERY_BAD_AVE_RTT = 1500;
    public boolean isMonitoring = false;
    private HwAPPQoEAPKConfig mAPKConfig;
    private HwAPPChrExcpInfo mAPPQoEInfo = new HwAPPChrExcpInfo();
    /* access modifiers changed from: private */
    public HwAPKTcpInfo mCurrentHwAPKTcpInfo;
    private HwAPPStateInfo mCurrentInfo;
    /* access modifiers changed from: private */
    public int mCurrentUID = 0;
    private Handler mHandler;
    private HwAPPQoEResourceManger mHwAPPQoEResourceManger;
    /* access modifiers changed from: private */
    public HwHidataJniAdapter mHwHidataJniAdapter;
    private HwWifiBoost mHwWifiBoost;
    private int mIsNoRxTime = 0;
    /* access modifiers changed from: private */
    public HwAPKTcpInfo mLastHwAPKTcpInfo;
    private HwAPPQoEUserLearning mLearningManager = null;
    private Handler mLocalHandler;
    private Object mLock = new Object();
    /* access modifiers changed from: private */
    public int mPeriodTime = 0;
    private int mScenceId = 0;
    private int mSystemTotalRxpackte = 0;
    private WifiManager mWifiManager;

    private static class HwAPKTcpInfo {
        public int rttPackteNum;
        public int rttTime;
        public int tcpReSendPackte;
        public int tcpRxPackte;
        public int tcpTxPackte;
        public int txBad;
        public int txGood;

        public HwAPKTcpInfo() {
            this.rttTime = 0;
            this.rttPackteNum = 0;
            this.tcpTxPackte = 0;
            this.tcpRxPackte = 0;
            this.tcpReSendPackte = 0;
        }

        public HwAPKTcpInfo(int[] info, RssiPacketCountInfo otaInfo) {
            this.rttTime = info[0];
            this.rttPackteNum = info[1];
            this.tcpTxPackte = info[6];
            this.tcpRxPackte = info[7];
            this.tcpReSendPackte = info[8];
            this.txGood = otaInfo.txgood;
            this.txBad = otaInfo.txbad;
        }
    }

    public HwAPKQoEQualityMonitor(Handler handler, Context context) {
        this.mHandler = handler;
        this.mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
        this.mHwHidataJniAdapter = HwHidataJniAdapter.getInstance();
        this.mHwWifiBoost = HwWifiBoost.getInstance(context);
        this.mWifiManager = (WifiManager) context.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
        this.mLearningManager = HwAPPQoEUserLearning.getInstance();
    }

    public void startMonitor(HwAPPStateInfo info) {
        int uid = info.mAppUID;
        int scenceId = info.mScenceId;
        int appId = info.mAppId;
        if (this.isMonitoring) {
            String str = TAG;
            HwAPPQoEUtils.logD(str, " uid = " + uid + " mCurrentUID = " + this.mCurrentUID + " scenceId = " + scenceId + " mScenceId = " + this.mScenceId);
            if (uid == this.mCurrentUID && scenceId != this.mScenceId) {
                this.mLocalHandler.removeMessages(1);
                this.mLocalHandler.removeMessages(2);
            } else {
                return;
            }
        } else {
            String str2 = TAG;
            HwAPPQoEUtils.logD(str2, "uid = " + uid + " appId = " + appId + " scenceId = " + scenceId);
            initHandlerThread();
        }
        this.mCurrentInfo = info;
        this.mAPKConfig = this.mHwAPPQoEResourceManger.getAPKScenceConfig(scenceId);
        if (this.mAPKConfig == null) {
            HwAPPQoEUtils.logD(TAG, "mAPKConfig == null");
            stopMonitor();
            return;
        }
        this.isMonitoring = true;
        this.mScenceId = scenceId;
        this.mCurrentUID = uid;
        if (this.mLearningManager != null) {
            if (this.mLearningManager.getUserTypeByAppId(info.mAppId) == 1) {
                HwAPPQoEUtils.logD(TAG, " HwAPKQoEQualityMonitor is a COMMON user");
                this.mPeriodTime = this.mAPKConfig.mAppPeriod * 1000 * 2;
            } else {
                this.mPeriodTime = this.mAPKConfig.mAppPeriod * 1000;
                HwAPPQoEUtils.logD(TAG, " HwAPKQoEQualityMonitor is a USER_TYPE_RADICAL user");
            }
        }
        this.mLocalHandler.sendEmptyMessage(1);
    }

    public void stopMonitor() {
        String str = TAG;
        HwAPPQoEUtils.logD(str, " HwAPKQoEQualityMonitor stop monitor isMonitoring = " + this.isMonitoring);
        if (this.isMonitoring) {
            this.isMonitoring = false;
            this.mIsNoRxTime = 0;
            this.mLocalHandler.removeMessages(1);
            this.mLocalHandler.removeMessages(2);
            release();
        }
    }

    private void initHandlerThread() {
        HandlerThread handlerThread = new HandlerThread("HwAPKQoEQualityMonitor Thread");
        handlerThread.start();
        this.mLocalHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        int[] result = HwAPKQoEQualityMonitor.this.mHwHidataJniAdapter.sendQoECmd(23, HwAPKQoEQualityMonitor.this.mCurrentUID);
                        RssiPacketCountInfo otaInfo = HwAPKQoEQualityMonitor.this.getOTAInfo();
                        if (result != null && result.length >= 10) {
                            HwAPKTcpInfo unused = HwAPKQoEQualityMonitor.this.mLastHwAPKTcpInfo = new HwAPKTcpInfo(result, otaInfo);
                            sendEmptyMessageDelayed(2, (long) HwAPKQoEQualityMonitor.this.mPeriodTime);
                            break;
                        } else {
                            sendEmptyMessageDelayed(1, (long) HwAPKQoEQualityMonitor.this.mPeriodTime);
                            break;
                        }
                    case 2:
                        int[] result2 = HwAPKQoEQualityMonitor.this.mHwHidataJniAdapter.sendQoECmd(23, HwAPKQoEQualityMonitor.this.mCurrentUID);
                        RssiPacketCountInfo otaInfo2 = HwAPKQoEQualityMonitor.this.getOTAInfo();
                        if (result2 != null && result2.length >= 10) {
                            HwAPKTcpInfo unused2 = HwAPKQoEQualityMonitor.this.mCurrentHwAPKTcpInfo = new HwAPKTcpInfo(result2, otaInfo2);
                            HwAPKQoEQualityMonitor.this.isAPKStallThisPeriod(HwAPKQoEQualityMonitor.this.mLastHwAPKTcpInfo, HwAPKQoEQualityMonitor.this.mCurrentHwAPKTcpInfo);
                            HwAPKTcpInfo unused3 = HwAPKQoEQualityMonitor.this.mLastHwAPKTcpInfo = HwAPKQoEQualityMonitor.this.mCurrentHwAPKTcpInfo;
                        }
                        sendEmptyMessageDelayed(2, (long) HwAPKQoEQualityMonitor.this.mPeriodTime);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void release() {
        if (this.mLocalHandler != null) {
            Looper looper = this.mLocalHandler.getLooper();
            if (looper != null && looper != Looper.getMainLooper()) {
                looper.quitSafely();
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x01da A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0226  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x0232 A[SYNTHETIC, Splitter:B:98:0x0232] */
    public void isAPKStallThisPeriod(HwAPKTcpInfo initInfo, HwAPKTcpInfo curInfo) {
        int periodRTT;
        double tr;
        int periodTxBad;
        double otaRate;
        int rx;
        int result;
        int times;
        int result2;
        int rx2;
        HwAPKTcpInfo hwAPKTcpInfo = initInfo;
        HwAPKTcpInfo hwAPKTcpInfo2 = curInfo;
        WifiInfo info = this.mWifiManager.getConnectionInfo();
        if (info != null) {
            int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(info.getFrequency(), info.getRssi());
            int periodRTTPacket = hwAPKTcpInfo2.rttPackteNum - hwAPKTcpInfo.rttPackteNum;
            if (periodRTTPacket > 0) {
                periodRTT = (hwAPKTcpInfo2.rttTime - hwAPKTcpInfo.rttTime) / periodRTTPacket;
            } else {
                periodRTT = 0;
            }
            int periodRTT2 = periodRTT;
            WifiInfo wifiInfo = info;
            int periodTcpTxPacket = hwAPKTcpInfo2.tcpTxPackte - hwAPKTcpInfo.tcpTxPackte;
            int periodTcpRxPacket = hwAPKTcpInfo2.tcpRxPackte - hwAPKTcpInfo.tcpRxPackte;
            int periodTcpRsPacket = hwAPKTcpInfo2.tcpReSendPackte - hwAPKTcpInfo.tcpReSendPackte;
            int periodTxGood = hwAPKTcpInfo2.txGood - hwAPKTcpInfo.txGood;
            int periodTxBad2 = hwAPKTcpInfo2.txBad - hwAPKTcpInfo.txBad;
            if (((double) periodTcpTxPacket) > 0.0d) {
                tr = ((double) periodTcpRsPacket) / ((double) periodTcpTxPacket);
            } else {
                tr = 0.0d;
            }
            int tx = periodTcpTxPacket;
            int rx3 = periodTcpRxPacket;
            double aveRtt = (double) periodRTT2;
            if (((double) periodTxGood) > 0.0d) {
                periodTxBad = periodTxBad2;
                otaRate = ((double) periodTxBad2) / ((double) periodTxGood);
            } else {
                periodTxBad = periodTxBad2;
                otaRate = 0.0d;
            }
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Period Quality: period = ");
            sb.append(this.mPeriodTime);
            sb.append(" rtt = ");
            sb.append(periodRTT2);
            sb.append(" periodRTTPacket = ");
            sb.append(periodRTTPacket);
            sb.append(" tcpRsRate = ");
            sb.append(tr);
            sb.append(" otaRate = ");
            sb.append(otaRate);
            sb.append(" Txpacket = ");
            sb.append(periodTcpTxPacket);
            sb.append(" periodTcpRxPacket = ");
            sb.append(periodTcpRxPacket);
            sb.append(" RsPacket = ");
            sb.append(periodTcpRsPacket);
            sb.append(" txGood = ");
            sb.append(periodTxGood);
            sb.append(" txBad = ");
            int periodTxBad3 = periodTxBad;
            sb.append(periodTxBad3);
            int periodTxBad4 = periodTxBad3;
            sb.append(" rssiLevel = ");
            sb.append(rssiLevel);
            HwAPPQoEUtils.logD(str, sb.toString());
            if (rssiLevel == 3) {
                rx = rx3;
            } else if (rssiLevel == 4) {
                rx = rx3;
            } else {
                if (rssiLevel <= 2) {
                    if (tx >= 3) {
                        rx2 = rx3;
                        if (rx2 <= 100) {
                            if (aveRtt > 0.0d && aveRtt < 800.0d && periodRTTPacket >= 2) {
                                HwAPPQoEUtils.logD(TAG, "weak rssi but aveRtt is good");
                                result2 = 106;
                                rx = rx2;
                                result = result2;
                                if (tx > 0) {
                                }
                                if (rx > 0) {
                                }
                                sendMessageToSTM(result);
                                int result3 = result;
                                synchronized (this.mLock) {
                                }
                            } else if (aveRtt > 1500.0d && periodRTTPacket >= 2) {
                                HwAPPQoEUtils.logD(TAG, "weak rssi rtt bad");
                                result2 = 107;
                                rx = rx2;
                                result = result2;
                                if (tx > 0) {
                                }
                                if (rx > 0) {
                                }
                                sendMessageToSTM(result);
                                int result32 = result;
                                synchronized (this.mLock) {
                                }
                            } else if (otaRate <= 0.20000000298023224d || tx <= 20) {
                                if (tr >= 0.20000000298023224d) {
                                    HwAPPQoEUtils.logD(TAG, "weak rssi tcp bad");
                                    result2 = 107;
                                    rx = rx2;
                                    result = result2;
                                    if (tx > 0 || rx != 0 || result == 107) {
                                        if (rx > 0) {
                                            this.mSystemTotalRxpackte = 0;
                                            this.mIsNoRxTime = 0;
                                        }
                                        sendMessageToSTM(result);
                                    } else {
                                        double d = otaRate;
                                        if (rssiLevel > 2) {
                                            times = 3;
                                        } else {
                                            times = 1;
                                        }
                                        if (this.mIsNoRxTime >= times) {
                                            HwAPPQoEUtils.logD(TAG, "no rx apk bad");
                                            result = 107;
                                            sendMessageToSTM(107);
                                            this.mIsNoRxTime = 0;
                                        } else if (this.mIsNoRxTime == 0) {
                                            isSystemTcpDataNoRx(true);
                                            this.mIsNoRxTime++;
                                        } else if (isSystemTcpDataNoRx(false)) {
                                            HwAPPQoEUtils.logD(TAG, "system data no rx too");
                                            this.mIsNoRxTime++;
                                        }
                                    }
                                    int result322 = result;
                                    synchronized (this.mLock) {
                                        try {
                                            this.mAPPQoEInfo.netType = 800;
                                            this.mAPPQoEInfo.rtt = periodRTT2;
                                            this.mAPPQoEInfo.txPacket = periodTcpTxPacket;
                                            this.mAPPQoEInfo.rxPacket = periodTcpRxPacket;
                                            this.mAPPQoEInfo.rsPacket = periodTcpRsPacket;
                                            this.mAPPQoEInfo.para1 = periodTxGood;
                                            this.mAPPQoEInfo.para2 = periodTxBad4;
                                            this.mAPPQoEInfo.para3 = periodRTTPacket;
                                            this.mAPPQoEInfo.para4 = result322;
                                            this.mAPPQoEInfo.rssi = rssiLevel;
                                            return;
                                        } catch (Throwable th) {
                                            th = th;
                                            throw th;
                                        }
                                    }
                                }
                                rx = rx2;
                            } else {
                                HwAPPQoEUtils.logD(TAG, "weak rssi ota bad");
                                result2 = 107;
                                rx = rx2;
                                result = result2;
                                if (tx > 0) {
                                }
                                if (rx > 0) {
                                }
                                sendMessageToSTM(result);
                                int result3222 = result;
                                synchronized (this.mLock) {
                                }
                            }
                        }
                    } else {
                        rx2 = rx3;
                    }
                    if (aveRtt <= 1500.0d || periodRTTPacket < 2) {
                        if (rx2 >= 1) {
                            HwAPPQoEUtils.logD(TAG, "apk good");
                            result2 = 106;
                            rx = rx2;
                            result = result2;
                            if (tx > 0) {
                            }
                            if (rx > 0) {
                            }
                            sendMessageToSTM(result);
                            int result32222 = result;
                            synchronized (this.mLock) {
                            }
                        }
                        rx = rx2;
                    } else {
                        HwAPPQoEUtils.logD(TAG, "very bad rtt apk bad");
                        result2 = 107;
                        rx = rx2;
                        result = result2;
                        if (tx > 0) {
                        }
                        if (rx > 0) {
                        }
                        sendMessageToSTM(result);
                        int result322222 = result;
                        synchronized (this.mLock) {
                        }
                    }
                } else {
                    rx = rx3;
                }
                result = 108;
                if (tx > 0) {
                }
                if (rx > 0) {
                }
                sendMessageToSTM(result);
                int result3222222 = result;
                synchronized (this.mLock) {
                }
            }
            if (aveRtt <= 1500.0d || periodRTTPacket < 3) {
                if (tr >= 0.30000001192092896d && tx >= 20 && rx <= 100) {
                    if (aveRtt <= 0.0d || aveRtt >= 800.0d || periodRTTPacket < 3) {
                        HwAPPQoEUtils.logD(TAG, "STRONG_RSSI tcp bad");
                        result2 = 107;
                    } else {
                        HwAPPQoEUtils.logD(TAG, "tcp is bad but rtt is good ");
                        result2 = 106;
                    }
                    result = result2;
                    if (tx > 0) {
                    }
                    if (rx > 0) {
                    }
                    sendMessageToSTM(result);
                    int result32222222 = result;
                    synchronized (this.mLock) {
                    }
                }
                result = 108;
                if (tx > 0) {
                }
                if (rx > 0) {
                }
                sendMessageToSTM(result);
                int result322222222 = result;
                synchronized (this.mLock) {
                }
            } else {
                result2 = 107;
                result = result2;
                if (tx > 0) {
                }
                if (rx > 0) {
                }
                sendMessageToSTM(result);
                int result3222222222 = result;
                synchronized (this.mLock) {
                }
            }
        }
    }

    private void sendMessageToSTM(int action) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(action, this.mCurrentInfo));
    }

    private boolean isSystemTcpDataNoRx(boolean isInit) {
        int[] result = this.mHwHidataJniAdapter.sendQoECmd(15, 0);
        if (result == null || result.length < 10) {
            return false;
        }
        if (isInit) {
            HwAPPQoEUtils.logD(TAG, "isSystemTcpDataNoRx init");
            this.mSystemTotalRxpackte = result[7];
            return false;
        }
        int rxPackets = result[7] - this.mSystemTotalRxpackte;
        String str = TAG;
        HwAPPQoEUtils.logD(str, "isSystemTcpDataNoRx rxPackets = " + rxPackets + " result[7] = " + result[7] + " mSystemTotalRxpackte = " + this.mSystemTotalRxpackte);
        this.mSystemTotalRxpackte = result[7];
        if (rxPackets > 0) {
            return false;
        }
        return true;
    }

    public RssiPacketCountInfo getOTAInfo() {
        return this.mHwWifiBoost.getOTAInfo();
    }

    public HwAPPChrExcpInfo getAPPQoEInfo() {
        HwAPPChrExcpInfo hwAPPChrExcpInfo;
        synchronized (this.mLock) {
            String str = TAG;
            HwAPPQoEUtils.logD(str, "curAPPQoEInfo:" + this.mAPPQoEInfo.toString());
            hwAPPChrExcpInfo = this.mAPPQoEInfo;
        }
        return hwAPPChrExcpInfo;
    }
}
