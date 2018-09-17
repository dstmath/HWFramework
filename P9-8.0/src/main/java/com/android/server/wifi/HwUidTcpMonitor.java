package com.android.server.wifi;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwUidTcpMonitor {
    private static final int CMD_UPDATE_TCP_STATISTICS = 101;
    private static final int MAX_UID_SIZE = 20;
    private static final String TAG = "HwUidTcpMonitor";
    private static String TCP_STAT_PATH = "proc/net/wifipro_tcp_stat";
    public static final String[] TCP_WEB_STAT_ITEM = new String[]{HwCHRWifiUIDWebSpeed.WEB_UID, HwCHRWifiSpeedBaseChecker.WEB_SENDSEGS, HwCHRWifiSpeedBaseChecker.WEB_RESENDSEGS, HwCHRWifiSpeedBaseChecker.WEB_RECVSEGS, HwCHRWifiSpeedBaseChecker.WEB_RTT_DURATION, HwCHRWifiSpeedBaseChecker.WEB_RTT_PACKETS};
    private static HwUidTcpMonitor hwUidTcpMonitor = null;
    private String MOBILE_UID_TAG = "mobile UID state:";
    private String TCP_SEPORATOR = "\t";
    private String WLAN_UID_TAG = "wlan UID state:";
    private Context mContext = null;
    private Handler mHandler;
    private int mLastDnsFailedCnt = -1;
    private Object mTcpStatisticsLock = new Object();
    private HashMap<Integer, UidTcpStatInfo> mUidTcpStatInfo = new HashMap();
    private AtomicBoolean mWifiMonitorEnabled = new AtomicBoolean(false);

    static class UidTcpStatInfo {
        public long mLastUpdateTime = 0;
        public String mPacketName = "";
        public long mRcvSegs;
        public long mResendSegs;
        public long mRttDuration;
        public long mRttSegs;
        public long mSendSegs;
        public int mUid;

        public UidTcpStatInfo(int uid, long sendSegs, long resendSegs, long rcvSegs, long rttDuration, long rttSegs) {
            this.mUid = uid;
            this.mSendSegs = sendSegs;
            this.mResendSegs = resendSegs;
            this.mRcvSegs = rcvSegs;
            this.mRttDuration = rttDuration;
            this.mRttSegs = rttSegs;
        }

        public String toString() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("[");
            sbuf.append(" mUid = ").append(this.mUid);
            sbuf.append(" mPacketName = ").append(this.mPacketName);
            sbuf.append(" mSendSegs = ").append(this.mSendSegs);
            sbuf.append(" mResendSegs = ").append(this.mResendSegs);
            sbuf.append(" mRcvSegs = ").append(this.mRcvSegs);
            sbuf.append(" ]");
            return sbuf.toString();
        }
    }

    public HwUidTcpMonitor(Context context) {
        this.mContext = context;
        init();
    }

    public static synchronized HwUidTcpMonitor getInstance(Context context) {
        HwUidTcpMonitor hwUidTcpMonitor;
        synchronized (HwUidTcpMonitor.class) {
            if (hwUidTcpMonitor == null) {
                hwUidTcpMonitor = new HwUidTcpMonitor(context);
            }
            hwUidTcpMonitor = hwUidTcpMonitor;
        }
        return hwUidTcpMonitor;
    }

    private void init() {
        HandlerThread handlerThread = new HandlerThread("wifipro_uid_tcp_monitor_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:
                        HwUidTcpMonitor.this.readAndParseTcpStatistics();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private Integer[] getSelectedColumnIdx(String columnNames) {
        Integer[] selectedColumnIdx = new Integer[TCP_WEB_STAT_ITEM.length];
        String[] cols = columnNames.split(this.TCP_SEPORATOR);
        for (int i = 0; i < TCP_WEB_STAT_ITEM.length; i++) {
            selectedColumnIdx[i] = Integer.valueOf(-1);
            String selectedColumnName = TCP_WEB_STAT_ITEM[i];
            for (int j = 0; j < cols.length; j++) {
                if (selectedColumnName.equals(cols[j])) {
                    selectedColumnIdx[i] = Integer.valueOf(j);
                    break;
                }
            }
        }
        return selectedColumnIdx;
    }

    private int getOldestUidUpdated(Map<Integer, UidTcpStatInfo> uidTcpStatInfo) {
        int oldestUid = -1;
        long oldestUpdatedTime = Long.MAX_VALUE;
        for (Entry entry : uidTcpStatInfo.entrySet()) {
            Integer currKey = (Integer) entry.getKey();
            UidTcpStatInfo tmp = (UidTcpStatInfo) entry.getValue();
            if (tmp != null && tmp.mLastUpdateTime < oldestUpdatedTime) {
                oldestUpdatedTime = tmp.mLastUpdateTime;
                oldestUid = currKey.intValue();
            }
        }
        return oldestUid;
    }

    private void checkWifiInternetCapability(int txCnt, int rxCnt, int reTxCnt) {
        int currDnsFailedCnt = HwSelfCureUtils.getCurrentDnsFailedCounter();
        HwSelfCureEngine.getInstance().notifyTcpStatResults(txCnt, rxCnt, reTxCnt, this.mLastDnsFailedCnt >= 0 ? currDnsFailedCnt - this.mLastDnsFailedCnt : 0);
        this.mLastDnsFailedCnt = currDnsFailedCnt;
    }

    private void parseWlanUidTcpStatistics(List<String> tcpStatLines) {
        int wlanUidIdx = getWlanUidStatLineNumber(tcpStatLines);
        if (wlanUidIdx != -1 && tcpStatLines.size() > wlanUidIdx + 2) {
            Integer[] selectedColumnIdx = getSelectedColumnIdx((String) tcpStatLines.get(wlanUidIdx + 1));
            synchronized (this.mTcpStatisticsLock) {
                int deltaTxCnt = 0;
                int deltaRxCnt = 0;
                int deltaReTxCnt = 0;
                int topUid = WifiProCommonUtils.getForegroundAppUid(this.mContext);
                int i = wlanUidIdx + 2;
                while (i < tcpStatLines.size() && !((String) tcpStatLines.get(i)).startsWith("custom ip")) {
                    if (((String) tcpStatLines.get(i)).length() == 0 || ((String) tcpStatLines.get(i)).startsWith(this.MOBILE_UID_TAG)) {
                        break;
                    }
                    String[] tcpStatValues = ((String) tcpStatLines.get(i)).split(this.TCP_SEPORATOR);
                    Integer[] selectedTcpStatsValues = new Integer[selectedColumnIdx.length];
                    int j = 0;
                    while (j < selectedTcpStatsValues.length) {
                        selectedTcpStatsValues[j] = Integer.valueOf(0);
                        if (selectedColumnIdx[j].intValue() >= 0 && selectedColumnIdx[j].intValue() < tcpStatValues.length) {
                            try {
                                selectedTcpStatsValues[j] = Integer.valueOf(Integer.parseInt(tcpStatValues[selectedColumnIdx[j].intValue()]));
                            } catch (NumberFormatException e) {
                                LOGD("parseWlanUidTcpStatistics NumberFormatException rcv!");
                                return;
                            }
                        }
                        j++;
                    }
                    int uid = -1;
                    int sndSeg = 0;
                    int resndSeg = 0;
                    int rcvSeg = 0;
                    int rttDur = 0;
                    int rttSeg = 0;
                    if (selectedTcpStatsValues.length >= 0 && selectedTcpStatsValues[0].intValue() > 0) {
                        uid = selectedTcpStatsValues[0].intValue();
                    }
                    if (selectedTcpStatsValues.length >= 1) {
                        sndSeg = selectedTcpStatsValues[1].intValue();
                    }
                    if (selectedTcpStatsValues.length >= 2) {
                        resndSeg = selectedTcpStatsValues[2].intValue();
                    }
                    if (selectedTcpStatsValues.length >= 3) {
                        rcvSeg = selectedTcpStatsValues[3].intValue();
                    }
                    if (selectedTcpStatsValues.length >= 4) {
                        rttDur = selectedTcpStatsValues[4].intValue();
                    }
                    if (selectedTcpStatsValues.length >= 5) {
                        rttSeg = selectedTcpStatsValues[5].intValue();
                    }
                    deltaTxCnt += sndSeg;
                    deltaRxCnt += rcvSeg;
                    deltaReTxCnt += resndSeg;
                    if (uid != -1 && topUid == uid) {
                        HwWifiConnectivityMonitor monitor = HwWifiConnectivityMonitor.getInstance();
                        if (monitor != null) {
                            monitor.notifyTopUidTcpInfo(topUid, sndSeg, rcvSeg, resndSeg, rttDur, rttSeg);
                        }
                    }
                    UidTcpStatInfo lastUidTcpStatInfo = (UidTcpStatInfo) this.mUidTcpStatInfo.get(Integer.valueOf(uid));
                    if (lastUidTcpStatInfo != null) {
                        lastUidTcpStatInfo.mSendSegs += (long) sndSeg;
                        lastUidTcpStatInfo.mResendSegs += (long) resndSeg;
                        lastUidTcpStatInfo.mRcvSegs += (long) rcvSeg;
                        lastUidTcpStatInfo.mRttDuration += (long) rttDur;
                        lastUidTcpStatInfo.mRttSegs += (long) rttSeg;
                        lastUidTcpStatInfo.mLastUpdateTime = System.currentTimeMillis();
                        LOGD("parseWlanUidTcpStatistics lastUidTcpStatInfo = " + lastUidTcpStatInfo);
                    } else {
                        if (this.mUidTcpStatInfo.size() == 20) {
                            int oldestUid = getOldestUidUpdated(this.mUidTcpStatInfo);
                            this.mUidTcpStatInfo.remove(Integer.valueOf(oldestUid));
                            LOGD("parseWlanUidTcpStatistics rm oldestUid = " + oldestUid + ", current size = " + this.mUidTcpStatInfo.size());
                        }
                        if (uid != -1 && this.mUidTcpStatInfo.size() < 20) {
                            UidTcpStatInfo newUidTcpStatInfo = new UidTcpStatInfo(uid, (long) sndSeg, (long) resndSeg, (long) rcvSeg, (long) rttDur, (long) rttSeg);
                            newUidTcpStatInfo.mPacketName = WifiProCommonUtils.getPackageName(this.mContext, uid);
                            newUidTcpStatInfo.mLastUpdateTime = System.currentTimeMillis();
                            this.mUidTcpStatInfo.put(Integer.valueOf(uid), newUidTcpStatInfo);
                            LOGD("parseWlanUidTcpStatistics newUidTcpStatInfo = " + newUidTcpStatInfo);
                        }
                    }
                    i++;
                }
            }
        }
    }

    private void readAndParseTcpStatistics() {
        List<String> tcpStatLines = HwCHRWifiFile.getFileResult(TCP_STAT_PATH);
        if (tcpStatLines.size() != 0) {
            parseWlanUidTcpStatistics(tcpStatLines);
        }
    }

    public synchronized HashMap<Integer, UidTcpStatInfo> getUidTcpStatistics() {
        HashMap<Integer, UidTcpStatInfo> cloneMap;
        synchronized (this.mTcpStatisticsLock) {
            cloneMap = (HashMap) this.mUidTcpStatInfo.clone();
        }
        return cloneMap;
    }

    public synchronized boolean isAppAccessInternet(int appUid) {
        boolean z;
        synchronized (this.mTcpStatisticsLock) {
            UidTcpStatInfo uidTcpStatInfo = null;
            if (appUid != -1) {
                uidTcpStatInfo = (UidTcpStatInfo) this.mUidTcpStatInfo.get(Integer.valueOf(appUid));
            }
            z = uidTcpStatInfo != null;
        }
        return z;
    }

    public synchronized long getRttDuration(int appUid) {
        synchronized (this.mTcpStatisticsLock) {
            UidTcpStatInfo matchedUid = null;
            if (appUid != -1) {
                matchedUid = (UidTcpStatInfo) this.mUidTcpStatInfo.get(Integer.valueOf(appUid));
            }
            if (matchedUid != null) {
                long j = matchedUid.mRttDuration;
                return j;
            }
            return 0;
        }
    }

    public synchronized long getRttSegs(int appUid) {
        synchronized (this.mTcpStatisticsLock) {
            UidTcpStatInfo matchedUid = null;
            if (appUid != -1) {
                matchedUid = (UidTcpStatInfo) this.mUidTcpStatInfo.get(Integer.valueOf(appUid));
            }
            if (matchedUid != null) {
                long j = matchedUid.mRttSegs;
                return j;
            }
            return 0;
        }
    }

    public synchronized void updateUidTcpStatistics() {
        if (this.mWifiMonitorEnabled.get()) {
            this.mHandler.sendEmptyMessage(101);
        }
    }

    public synchronized void notifyWifiMonitorEnabled(boolean enabled) {
        this.mWifiMonitorEnabled.set(enabled);
        if (!this.mWifiMonitorEnabled.get()) {
            synchronized (this.mTcpStatisticsLock) {
                this.mUidTcpStatInfo.clear();
                this.mLastDnsFailedCnt = -1;
            }
        }
    }

    private int getWlanUidStatLineNumber(List<String> tcpStatLines) {
        for (int i = 0; i < tcpStatLines.size(); i++) {
            if (((String) tcpStatLines.get(i)).startsWith(this.WLAN_UID_TAG)) {
                return i;
            }
        }
        return -1;
    }

    private void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
