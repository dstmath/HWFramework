package com.huawei.hwwifiproservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;

public class TrafficMonitor {
    private static final int BYTES_ONE_KBPS = 128;
    public static final boolean DBG = false;
    private static final int IDLE_COUNT = 10;
    public static final int INVALID_READ_DATA_VALUE = -1;
    private static final int LIST_MAX_NUM = 1000;
    public static final double LOW_RX_TX_RATIO = 0.8d;
    private static final int MSG_CHECK_IPDATA = 100;
    private static final int MSG_SCREEN_ON = 103;
    private static final int MSG_SPEED_EXPIRED = 102;
    private static final int NO_DATA_CHK_LATENCY = 3;
    private static final int NO_DATA_REQUEST_NUM = 4;
    public static final int QOS_LEVEL_0_NOT_AVAILABLE = 0;
    public static final int QOS_LEVEL_1_VERY_POOR = 1;
    public static final int QOS_LEVEL_2_POOR = 2;
    public static final int QOS_LEVEL_3_MODERATE = 3;
    public static final int QOS_LEVEL_4_GOOD = 4;
    public static final int QOS_LEVEL_5_GREAT = 5;
    public static final int QOS_LEVEL_UNKNOWN = -1;
    public static final int RATIO_VALID_TX_NUM = 20;
    private static final int SPEED_LEVEL_1 = 0;
    private static final int SPEED_LEVEL_2 = 8192;
    private static final int SPEED_LEVEL_3 = 16384;
    private static final int SPEED_LEVEL_4 = 32768;
    private static final int SPEED_LEVEL_5 = 65536;
    private static final int SPEED_SLICE_TIME = 2;
    private static String TAG = "TrafficMonitor";
    private static final int[] TIMER = {WifiScanGenieDataBaseImpl.SCAN_GENIE_MAX_RECORD, 5000, ApInfoManager.SCAN_INTERVAL_NORMAL_3, 5000, LIST_MAX_NUM, LIST_MAX_NUM};
    public static final double VERY_LOW_RX_TX_RATIO = 0.4d;
    private Runnable mCallBack = null;
    private Context mContext;
    private int mExpireTime = 0;
    private final Handler mHandler = new Handler() {
        /* class com.huawei.hwwifiproservice.TrafficMonitor.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            synchronized (TrafficMonitor.this.mHandler) {
                if (TrafficMonitor.this.working) {
                    int i = msg.what;
                    if (i == 100) {
                        TrafficMonitor.this.checkIpData(msg);
                    } else if (i == 102) {
                        TrafficMonitor.this.mHandler.removeMessages(102);
                        TrafficMonitor.this.mSpeedLevel = -1;
                        TrafficMonitor.this.mHandler.post(TrafficMonitor.this.mCallBack);
                        String str = TrafficMonitor.TAG;
                        Log.i(str, "expired arrive. level:" + TrafficMonitor.this.mSpeedLevel);
                    } else if (i == 103) {
                        if (TrafficMonitor.this.mHandler.hasMessages(100)) {
                            TrafficMonitor.this.mHandler.removeMessages(100);
                            Message.obtain(TrafficMonitor.this.mHandler, 100, TrafficMonitor.LIST_MAX_NUM, 0).sendToTarget();
                            Log.i(TrafficMonitor.TAG, "send message MSG_CHECK_IPDATA");
                        }
                    }
                }
            }
        }
    };
    private PowerManager mPowerManager;
    private boolean mScreenOn = false;
    private int mSpeedLevel = 0;
    private int mTimerLen = 0;
    private int networkType = -1;
    private TxRxSum preSnap;
    private ArrayList<Integer> rxByteLst = null;
    private ArrayList<Integer> rxPktLst = null;
    private ArrayList<Long> tsLst = null;
    private ArrayList<Integer> txByteLst = null;
    private ArrayList<Integer> txPktLst = null;
    private boolean working = false;

    public TrafficMonitor(Runnable r, Context context) {
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mPowerManager = (PowerManager) context2.getSystemService("power");
            PowerManager powerManager = this.mPowerManager;
            if (powerManager != null) {
                this.mScreenOn = powerManager.isScreenOn();
            }
        }
        initBroadCastRevicer();
        this.mCallBack = r;
        this.preSnap = new TxRxSum(true);
        this.rxPktLst = new ArrayList<>();
        this.txPktLst = new ArrayList<>();
        this.rxByteLst = new ArrayList<>();
        this.txByteLst = new ArrayList<>();
        this.tsLst = new ArrayList<>();
    }

    private void initBroadCastRevicer() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.huawei.hwwifiproservice.TrafficMonitor.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    if (!TrafficMonitor.this.mScreenOn) {
                        TrafficMonitor.this.mScreenOn = true;
                        TrafficMonitor.this.mHandler.sendEmptyMessage(103);
                    }
                } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                    TrafficMonitor.this.mScreenOn = false;
                }
            }
        }, intentFilter);
    }

    public void reset() {
        synchronized (this.mHandler) {
            this.preSnap = new TxRxSum(true);
            if (this.rxPktLst != null) {
                this.rxPktLst.clear();
            } else {
                this.rxPktLst = new ArrayList<>();
            }
            if (this.txPktLst != null) {
                this.txPktLst.clear();
            } else {
                this.txPktLst = new ArrayList<>();
            }
            if (this.rxByteLst != null) {
                this.rxByteLst.clear();
            } else {
                this.rxByteLst = new ArrayList<>();
            }
            if (this.txByteLst != null) {
                this.txByteLst.clear();
            } else {
                this.txByteLst = new ArrayList<>();
            }
            if (this.tsLst != null) {
                this.tsLst.clear();
            } else {
                this.tsLst = new ArrayList<>();
            }
        }
    }

    private void cycleClean() {
        ArrayList<Integer> rxPktL = new ArrayList<>();
        ArrayList<Integer> txPktL = new ArrayList<>();
        ArrayList<Integer> rxByteL = new ArrayList<>();
        ArrayList<Integer> txByteL = new ArrayList<>();
        ArrayList<Long> tsL = new ArrayList<>();
        int count = this.tsLst.size();
        if (count > 0) {
            for (int index = count > 5 ? count - 5 : 0; index < count; index++) {
                rxPktL.add(this.rxPktLst.get(index));
                txPktL.add(this.txPktLst.get(index));
                rxByteL.add(this.rxByteLst.get(index));
                txByteL.add(this.txByteLst.get(index));
                tsL.add(this.tsLst.get(index));
            }
            this.rxPktLst.clear();
            this.txPktLst.clear();
            this.rxByteLst.clear();
            this.txByteLst.clear();
            this.tsLst.clear();
            int bakSum = tsL.size();
            for (int index2 = 0; index2 < bakSum; index2++) {
                this.rxPktLst.add(rxPktL.get(index2));
                this.txPktLst.add(txPktL.get(index2));
                this.rxByteLst.add(rxByteL.get(index2));
                this.txByteLst.add(txByteL.get(index2));
                this.tsLst.add(tsL.get(index2));
            }
            String str = TAG;
            Log.i(str, "before Count:" + count + ",after Count:" + this.tsLst.size());
        }
    }

    private void update() {
        TxRxSum curr = new TxRxSum(true);
        this.tsLst.add(Long.valueOf(curr.ts - this.preSnap.ts));
        this.rxPktLst.add(Integer.valueOf((int) (curr.rxPkts - this.preSnap.rxPkts)));
        this.txPktLst.add(Integer.valueOf((int) (curr.txPkts - this.preSnap.txPkts)));
        this.rxByteLst.add(Integer.valueOf((int) (curr.rxBytes - this.preSnap.rxBytes)));
        this.txByteLst.add(Integer.valueOf((int) (curr.txBytes - this.preSnap.txBytes)));
        this.preSnap = new TxRxSum(curr);
        int count = this.tsLst.size();
        String str = TAG;
        Log.i(str, "update:rxPkts:" + this.rxPktLst.get(count - 1) + ",txPkts:" + this.txPktLst.get(count - 1) + ",rxBytes:" + this.rxByteLst.get(count - 1) + ",txBytes:" + this.txByteLst.get(count - 1));
    }

    public boolean hasTraffic() {
        int count = this.tsLst.size();
        if (count < 3) {
            return true;
        }
        int sendSumPkts = 0;
        int recvSumPkts = 0;
        for (int i = count - 3; i < count; i++) {
            sendSumPkts += this.txPktLst.get(i).intValue();
            recvSumPkts += this.rxPktLst.get(i).intValue();
        }
        String str = TAG;
        Log.i(str, "hasTraffic:,sendSumPkts:" + sendSumPkts + ",recvSumPkts:" + recvSumPkts);
        if (sendSumPkts <= 4 || recvSumPkts <= 4) {
            return false;
        }
        return true;
    }

    public boolean isLowPktRatio() {
        int count = this.tsLst.size();
        int rxPkts = this.rxPktLst.get(count - 1).intValue();
        int txPkts = this.txPktLst.get(count - 1).intValue();
        if (count <= 0 || txPkts < 20) {
            return false;
        }
        double ratio = (((double) rxPkts) * 1.0d) / ((double) txPkts);
        String str = TAG;
        Log.i(str, "rx/tx ratio:" + ratio);
        if (ratio < 0.8d) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Multiple debug info for r7v10 long: [D('rxBytesSum' int), D('duringMs' long)] */
    public TxRxStat getStatic(int recent_period_s) {
        TxRxStat stat;
        double txSpeed;
        double ratio;
        int k;
        int txBytesSum;
        int rxPktSum;
        int txPktSum;
        long duringMs;
        long duringMs2;
        int txBytesSum2;
        synchronized (this.mHandler) {
            int count = this.tsLst.size();
            double ratio2 = 0.0d;
            stat = new TxRxStat();
            if (count <= 0 || recent_period_s <= 0) {
                ratio = 0;
                txSpeed = 0;
            } else {
                int rxPktSum2 = 0;
                if (this.rxPktLst.size() != count || this.txPktLst.size() != count) {
                    ratio = 0;
                    txSpeed = 0;
                } else if (this.rxByteLst.size() == count && this.txByteLst.size() == count) {
                    int txPktSum2 = 0;
                    long periodMs = ((long) recent_period_s) * 1000;
                    int rxBytesSum = 0;
                    int k2 = count - 1;
                    long duringMs3 = 0;
                    int txBytesSum3 = 0;
                    while (true) {
                        if (k2 <= 0) {
                            k = rxBytesSum;
                            txBytesSum = txBytesSum3;
                            rxPktSum = rxPktSum2;
                            txPktSum = txPktSum2;
                            duringMs = duringMs3;
                            break;
                        }
                        rxPktSum2 += this.rxPktLst.get(k2).intValue();
                        txPktSum2 += this.txPktLst.get(k2).intValue();
                        int rxBytesSum2 = rxBytesSum + this.rxByteLst.get(k2).intValue();
                        int txBytesSum4 = txBytesSum3 + this.txByteLst.get(k2).intValue();
                        long duringMs4 = duringMs3 + this.tsLst.get(k2).longValue();
                        Log.i(TAG, "during_ms:" + duringMs4 + ",period_ms:" + periodMs);
                        if (duringMs4 >= periodMs) {
                            duringMs = duringMs4;
                            rxPktSum = rxPktSum2;
                            txPktSum = txPktSum2;
                            k = rxBytesSum2;
                            txBytesSum = txBytesSum4;
                            break;
                        }
                        k2--;
                        duringMs3 = duringMs4;
                        rxBytesSum = rxBytesSum2;
                        txBytesSum3 = txBytesSum4;
                        ratio2 = ratio2;
                    }
                    double duringS = ((double) duringMs) / 1000.0d;
                    double ratio3 = 0.0d;
                    double rxSpeed = duringS > 0.001d ? ((double) k) / duringS : 0.0d;
                    double txSpeed2 = duringS > 0.001d ? ((double) txBytesSum) / duringS : 0.0d;
                    if (txPktSum > 0) {
                        duringMs2 = duringMs;
                        txBytesSum2 = txBytesSum;
                        ratio3 = ((double) rxPktSum) / ((double) txPktSum);
                    } else {
                        duringMs2 = duringMs;
                        txBytesSum2 = txBytesSum;
                    }
                    stat.setMember(rxPktSum, txPktSum, (int) rxSpeed, (int) txSpeed2, ratio3);
                    Log.i(TAG, "count:" + count + ",rx_sum:" + rxPktSum + ",tx_sum:" + txPktSum + ",rxBytes:" + k + ",during_ms:" + duringMs2 + ",rx_speed:" + rxSpeed + ",tx_speed:" + txSpeed2 + ",rto:" + ratio3);
                } else {
                    ratio = 0;
                    txSpeed = 0;
                }
            }
            Log.i(TAG, "getStatic,count:" + count + ",work status:" + this.working);
        }
        return stat;
    }

    public void setExpireTime(int latency_ms) {
        synchronized (this.mHandler) {
            this.mExpireTime = latency_ms;
            if (this.working) {
                this.mHandler.removeMessages(102);
                if (this.mExpireTime > 0) {
                    this.mHandler.sendEmptyMessageDelayed(102, (long) this.mExpireTime);
                }
            }
        }
    }

    public void setInterval(int interval_ms) {
        synchronized (this.mHandler) {
            this.mTimerLen = interval_ms;
        }
    }

    public int getRecentRxSpeedLevel(int period) {
        int speed = getRxByteSpeed(period);
        int speedLevel = transform(speed);
        String str = TAG;
        Log.i(str, "get rxSpeed:" + speed + ",speed-level=" + speedLevel);
        return speedLevel;
    }

    public void enableMonitor(boolean enable, int net_type) {
        synchronized (this.mHandler) {
            if (enable) {
                this.networkType = net_type;
                this.working = true;
                Message.obtain(this.mHandler, 100, 0, 0).sendToTarget();
            } else {
                this.working = false;
                this.networkType = -1;
                this.mHandler.removeMessages(100);
            }
            String str = TAG;
            Log.i(str, "enableMonitor:" + enable + ",type=" + net_type);
        }
    }

    public boolean isWorking() {
        boolean ret;
        synchronized (this.mHandler) {
            ret = this.working;
        }
        return ret;
    }

    public int getRxByteSpeed(int period_s) {
        int i;
        synchronized (this.mHandler) {
            int count = this.tsLst.size();
            int rxBytesSum = 0;
            long periodMs = ((long) period_s) * 1000;
            long duringMs = 0;
            double speed = 0.0d;
            if (count > 0 && period_s > 0) {
                int k = count - 1;
                while (true) {
                    if (k <= 0) {
                        break;
                    }
                    rxBytesSum += this.rxByteLst.get(k).intValue();
                    duringMs += this.tsLst.get(k).longValue();
                    if (duringMs >= periodMs) {
                        break;
                    }
                    k--;
                }
                double duringS = ((double) duringMs) / 1000.0d;
                speed = duringS > 0.001d ? ((double) rxBytesSum) / duringS : 0.0d;
            }
            i = (int) speed;
        }
        return i;
    }

    public int transform(int speed) {
        if (speed > SPEED_LEVEL_5) {
            return 5;
        }
        if (speed > SPEED_LEVEL_4) {
            return 4;
        }
        if (speed > SPEED_LEVEL_3) {
            return 3;
        }
        if (speed > SPEED_LEVEL_2) {
            return 2;
        }
        if (speed > 0) {
            return -1;
        }
        return -1;
    }

    private int getTimerVal() {
        int interval = this.mTimerLen;
        if (interval <= 0) {
            interval = TIMER[0];
        }
        int count = this.txPktLst.size();
        int idleConut = 0;
        if (count > 10) {
            int i = 1;
            while (i <= 10 && this.txPktLst.get(count - i).intValue() == 0) {
                idleConut++;
                i++;
            }
        }
        if (idleConut >= 10) {
            interval = TIMER[1];
        }
        if (!this.mScreenOn) {
            interval = TIMER[2];
        }
        String str = TAG;
        Log.i(str, "gettimer:interval=" + interval);
        return interval;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkIpData(Message msg) {
        if (msg != null) {
            int repeats = msg.arg1;
            if (repeats == 0) {
                reset();
            } else {
                update();
                if (this.mExpireTime > 0) {
                    int level = transform(getRxByteSpeed(2));
                    if (repeats <= 1 || !(level == -1 || this.mSpeedLevel == level)) {
                        this.mSpeedLevel = level;
                        String str = TAG;
                        Log.i(str, "no expired. level:" + level);
                        this.mHandler.post(this.mCallBack);
                        this.mHandler.removeMessages(102);
                    }
                    this.mHandler.sendEmptyMessageDelayed(102, (long) this.mExpireTime);
                } else {
                    this.mHandler.post(this.mCallBack);
                }
            }
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 100, repeats + 1, 0), (long) getTimerVal());
            if (repeats % LIST_MAX_NUM == 0) {
                cycleClean();
            }
        }
    }

    public class TxRxSum {
        private static final String DEFAULT_MOBILE_IFACE = "rmnet0";
        private static final String DEFAULT_WLAN_IFACE = "wlan0";
        private static final boolean TCP_UDP_SUM = true;
        private final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
        public long rxBytes;
        public long rxPkts;
        public long ts;
        public long txBytes;
        public long txPkts;

        public TxRxSum(boolean need_update) {
            if (need_update) {
                updateMobileTxRxSum();
            }
        }

        public TxRxSum(TxRxSum sum) {
            this.txPkts = sum.txPkts;
            this.rxPkts = sum.rxPkts;
            this.txBytes = sum.txBytes;
            this.rxBytes = sum.rxBytes;
            this.ts = sum.ts;
        }

        public String toString() {
            return "{txSum=" + this.txPkts + " rxSum=" + this.rxPkts + "}";
        }

        private void updateMobileTxRxSum() {
            if (TrafficMonitor.this.networkType == 0) {
                this.txPkts = TrafficStats.getMobileTxPackets();
                this.rxPkts = TrafficStats.getMobileRxPackets();
                this.txBytes = TrafficStats.getMobileTxBytes();
                this.rxBytes = TrafficStats.getMobileRxBytes();
            } else if (TrafficMonitor.this.networkType == 1) {
                this.txPkts = TrafficStats.getTxPackets(this.WLAN_IFACE);
                this.rxPkts = TrafficStats.getRxPackets(this.WLAN_IFACE);
                this.txBytes = TrafficStats.getTxBytes(this.WLAN_IFACE);
                this.rxBytes = TrafficStats.getRxBytes(this.WLAN_IFACE);
            } else {
                Log.e(TrafficMonitor.TAG, "unknown netwotk type to check.");
            }
            this.ts = SystemClock.uptimeMillis();
        }
    }

    public static class TxRxStat {
        public long rxPkts;
        public long rx_speed;
        public double rx_tx_rto;
        public long txPkts;
        public long tx_speed;

        public TxRxStat() {
            this.txPkts = 0;
            this.rxPkts = 0;
            this.rx_speed = 0;
            this.rx_tx_rto = 0.0d;
            this.tx_speed = 0;
        }

        public TxRxStat(TxRxStat trs) {
            this.txPkts = trs.txPkts;
            this.rxPkts = trs.rxPkts;
            this.rx_speed = trs.rx_speed;
            this.rx_tx_rto = trs.rx_tx_rto;
            this.tx_speed = trs.tx_speed;
        }

        public void setMember(int rxSum, int txSum, int rxSpeed, int txSpeed, double rto) {
            this.txPkts = (long) txSum;
            this.rxPkts = (long) rxSum;
            this.rx_speed = (long) rxSpeed;
            this.tx_speed = (long) txSpeed;
            this.rx_tx_rto = rto;
        }
    }

    public long getMobileTxBytes() {
        return TrafficStats.getMobileTxBytes();
    }

    public long getMobileRxBytes() {
        return TrafficStats.getMobileRxBytes();
    }
}
