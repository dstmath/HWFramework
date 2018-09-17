package com.android.server.wifi.wifipro;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;

public class TrafficMonitor {
    private static final int BYTES_ONE_KbPS = 128;
    public static final boolean DBG = false;
    private static final int IDLE_COUNT = 10;
    public static final int INVALID_READ_DATA_VALUE = -1;
    private static final int LIST_MAX_NUM = 1000;
    public static final double LOW_RX_TX_RATIO = 0.8d;
    private static final int MSG_CHECK_IPDATA = 100;
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
    private static final int[] TIMER = new int[]{2000, ClientHandler.DEFAULT_WIFI_ROUTE_CHECK_TIME_FIRST, ClientHandler.DEFAULT_WIFI_ROUTE_CHECK_TIME_FIRST, ClientHandler.DEFAULT_WIFI_ROUTE_CHECK_TIME_FIRST, 1000, 1000};
    public static final double VERY_LOW_RX_TX_RATIO = 0.4d;
    private Runnable mCallBack = null;
    private int mExpireTime = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            synchronized (TrafficMonitor.this.mHandler) {
                if (TrafficMonitor.this.working) {
                    switch (msg.what) {
                        case 100:
                            int repeats = msg.arg1;
                            if (repeats == 0) {
                                TrafficMonitor.this.reset();
                            } else {
                                TrafficMonitor.this.update();
                                if (TrafficMonitor.this.mExpireTime > 0) {
                                    int level = TrafficMonitor.this.transform(TrafficMonitor.this.getRxByteSpeed(2));
                                    if (repeats <= 1 || !(level == -1 || TrafficMonitor.this.mSpeedLevel == level)) {
                                        TrafficMonitor.this.mSpeedLevel = level;
                                        Log.i(TrafficMonitor.TAG, "no expired. level:" + level);
                                        TrafficMonitor.this.mHandler.post(TrafficMonitor.this.mCallBack);
                                        TrafficMonitor.this.mHandler.removeMessages(102);
                                    } else {
                                        Log.i(TrafficMonitor.TAG, "start expired. level:" + level);
                                    }
                                    TrafficMonitor.this.mHandler.sendEmptyMessageDelayed(102, (long) TrafficMonitor.this.mExpireTime);
                                } else {
                                    TrafficMonitor.this.mHandler.post(TrafficMonitor.this.mCallBack);
                                }
                            }
                            TrafficMonitor.this.mHandler.sendMessageDelayed(Message.obtain(TrafficMonitor.this.mHandler, 100, repeats + 1, 0), (long) TrafficMonitor.this.getTimerVal());
                            if (repeats % 1000 == 0) {
                                TrafficMonitor.this.cycle_clean();
                                break;
                            }
                            break;
                        case 102:
                            TrafficMonitor.this.mHandler.removeMessages(102);
                            TrafficMonitor.this.mSpeedLevel = -1;
                            TrafficMonitor.this.mHandler.post(TrafficMonitor.this.mCallBack);
                            Log.i(TrafficMonitor.TAG, "expired arrive. level:" + TrafficMonitor.this.mSpeedLevel);
                            break;
                    }
                }
            }
        }
    };
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

    public class TxRxSum {
        private static final String DEFAULT_MOBILE_IFACE = "rmnet0";
        private static final String DEFAULT_WLAN_IFACE = "wlan0";
        private static final boolean TCP_UDPSum = true;
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

        public void updateMobileTxRxSum() {
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

    public TrafficMonitor(Runnable r) {
        this.mCallBack = r;
        this.preSnap = new TxRxSum(true);
        this.rxPktLst = new ArrayList();
        this.txPktLst = new ArrayList();
        this.rxByteLst = new ArrayList();
        this.txByteLst = new ArrayList();
        this.tsLst = new ArrayList();
    }

    public void reset() {
        synchronized (this.mHandler) {
            this.preSnap = new TxRxSum(true);
            if (this.rxPktLst != null) {
                this.rxPktLst.clear();
            } else {
                this.rxPktLst = new ArrayList();
            }
            if (this.txPktLst != null) {
                this.txPktLst.clear();
            } else {
                this.txPktLst = new ArrayList();
            }
            if (this.rxByteLst != null) {
                this.rxByteLst.clear();
            } else {
                this.rxByteLst = new ArrayList();
            }
            if (this.txByteLst != null) {
                this.txByteLst.clear();
            } else {
                this.txByteLst = new ArrayList();
            }
            if (this.tsLst != null) {
                this.tsLst.clear();
            } else {
                this.tsLst = new ArrayList();
            }
        }
    }

    private void cycle_clean() {
        ArrayList<Integer> rxPktL = new ArrayList();
        ArrayList<Integer> txPktL = new ArrayList();
        ArrayList<Integer> rxByteL = new ArrayList();
        ArrayList<Integer> txByteL = new ArrayList();
        ArrayList<Long> tsL = new ArrayList();
        int count = this.tsLst.size();
        if (count > 0) {
            index = count > 5 ? count - 5 : 0;
            while (index < count) {
                rxPktL.add((Integer) this.rxPktLst.get(index));
                txPktL.add((Integer) this.txPktLst.get(index));
                rxByteL.add((Integer) this.rxByteLst.get(index));
                txByteL.add((Integer) this.txByteLst.get(index));
                tsL.add((Long) this.tsLst.get(index));
                index++;
            }
            this.rxPktLst.clear();
            this.txPktLst.clear();
            this.rxByteLst.clear();
            this.txByteLst.clear();
            this.tsLst.clear();
            int bak_sum = tsL.size();
            for (index = 0; index < bak_sum; index++) {
                this.rxPktLst.add((Integer) rxPktL.get(index));
                this.txPktLst.add((Integer) txPktL.get(index));
                this.rxByteLst.add((Integer) rxByteL.get(index));
                this.txByteLst.add((Integer) txByteL.get(index));
                this.tsLst.add((Long) tsL.get(index));
            }
            Log.i(TAG, "before Count:" + count + ",after Count:" + this.tsLst.size());
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
        Log.i(TAG, "update:rxPkts:" + this.rxPktLst.get(count - 1) + ",txPkts:" + this.txPktLst.get(count - 1) + ",rxBytes:" + this.rxByteLst.get(count - 1) + ",txBytes:" + this.txByteLst.get(count - 1));
    }

    public boolean hasTraffic() {
        int count = this.tsLst.size();
        if (count >= 3) {
            int sendSumPkts = 0;
            int recvSumPkts = 0;
            for (int i = count - 3; i < count; i++) {
                sendSumPkts += ((Integer) this.txPktLst.get(i)).intValue();
                recvSumPkts += ((Integer) this.rxPktLst.get(i)).intValue();
            }
            Log.i(TAG, "hasTraffic:,sendSumPkts:" + sendSumPkts + ",recvSumPkts:" + recvSumPkts);
            if (sendSumPkts <= 4 || recvSumPkts <= 4) {
                return false;
            }
        }
        return true;
    }

    public boolean isLowPktRatio() {
        int count = this.tsLst.size();
        int rxPkts = ((Integer) this.rxPktLst.get(count - 1)).intValue();
        int txPkts = ((Integer) this.txPktLst.get(count - 1)).intValue();
        if (count > 0 && txPkts >= 20) {
            double ratio = (((double) rxPkts) * 1.0d) / ((double) txPkts);
            Log.i(TAG, "rx/tx ratio:" + ratio);
            if (ratio < 0.8d) {
                return true;
            }
        }
        return false;
    }

    public TxRxStat getStatic(int recent_period_s) {
        TxRxStat stat;
        synchronized (this.mHandler) {
            int count = this.tsLst.size();
            long period_ms = ((long) recent_period_s) * 1000;
            int rx_pkt_sum = 0;
            int tx_pkt_sum = 0;
            int rx_bytes_sum = 0;
            int tx_bytes_sum = 0;
            long during_ms = 0;
            stat = new TxRxStat();
            if (count > 0 && recent_period_s > 0 && this.rxPktLst.size() == count && this.txPktLst.size() == count && this.rxByteLst.size() == count && this.txByteLst.size() == count) {
                for (int k = count - 1; k > 0; k--) {
                    rx_pkt_sum += ((Integer) this.rxPktLst.get(k)).intValue();
                    tx_pkt_sum += ((Integer) this.txPktLst.get(k)).intValue();
                    rx_bytes_sum += ((Integer) this.rxByteLst.get(k)).intValue();
                    tx_bytes_sum += ((Integer) this.txByteLst.get(k)).intValue();
                    during_ms += ((Long) this.tsLst.get(k)).longValue();
                    Log.i(TAG, "during_ms:" + during_ms + ",period_ms:" + period_ms);
                    if (during_ms >= period_ms) {
                        break;
                    }
                }
                double during_s = ((double) during_ms) / 1000.0d;
                double rx_speed = during_s > 0.001d ? ((double) rx_bytes_sum) / during_s : 0.0d;
                double tx_speed = during_s > 0.001d ? ((double) tx_bytes_sum) / during_s : 0.0d;
                double ratio = tx_pkt_sum > 0 ? ((double) rx_pkt_sum) / ((double) tx_pkt_sum) : 0.0d;
                stat.setMember(rx_pkt_sum, tx_pkt_sum, (int) rx_speed, (int) tx_speed, ratio);
                Log.i(TAG, "count:" + count + ",rx_sum:" + rx_pkt_sum + ",tx_sum:" + tx_pkt_sum + ",rxBytes:" + rx_bytes_sum + ",during_ms:" + during_ms + ",rx_speed:" + rx_speed + ",tx_speed:" + tx_speed + ",rto:" + ratio);
            } else {
                Log.i(TAG, "getStatic,count:" + count + ",work status:" + this.working);
            }
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
        int speed_level = transform(speed);
        Log.i(TAG, "get rxSpeed:" + speed + ",speed-level=" + speed_level);
        return speed_level;
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
            Log.i(TAG, "enableMonitor:" + enable + ",type=" + net_type);
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
            int rx_bytes_sum = 0;
            long period_ms = ((long) period_s) * 1000;
            long during_ms = 0;
            double speed = 0.0d;
            if (count > 0 && period_s > 0) {
                for (int k = count - 1; k > 0; k--) {
                    rx_bytes_sum += ((Integer) this.rxByteLst.get(k)).intValue();
                    during_ms += ((Long) this.tsLst.get(k)).longValue();
                    if (during_ms >= period_ms) {
                        break;
                    }
                }
                double during_s = ((double) during_ms) / 1000.0d;
                speed = during_s > 0.001d ? ((double) rx_bytes_sum) / during_s : 0.0d;
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
        int interval = this.mTimerLen > 0 ? this.mTimerLen : TIMER[0];
        int count = this.txPktLst.size();
        int idle_conut = 0;
        if (count > 10) {
            int i = 1;
            while (i <= 10 && ((Integer) this.txPktLst.get(count - i)).intValue() == 0) {
                idle_conut++;
                i++;
            }
        }
        if (idle_conut >= 10) {
            interval = TIMER[1];
        }
        Log.i(TAG, "gettimer:interval=" + interval);
        return interval;
    }

    public long getMobileTxBytes() {
        return TrafficStats.getMobileTxBytes();
    }

    public long getMobileRxBytes() {
        return TrafficStats.getMobileRxBytes();
    }
}
