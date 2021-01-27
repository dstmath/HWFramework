package com.huawei.hwwifiproservice;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwUidTcpMonitor {
    private static final String DNS_SEPORATOR = "/";
    private static final int MAX_UID_DNS_SIZE = 8;
    private static final int MAX_UID_SIZE = 20;
    private static final String MOBILE_UID_TAG = "mobile UID state:";
    private static final String TAG = "HwUidTcpMonitor";
    private static final String TCP_SEPORATOR = "\t";
    private static String TCP_STAT_PATH = "proc/net/wifipro_tcp_stat";
    public static final String[] TCP_WEB_STAT_ITEM = {"UID", "WEBSENDSEGS", "WEBRESENDSEGS", "WEBRECVSEGS", "WEBRTTDURATION", "WEBRTTSEGS"};
    private static final String UID_COUNT_SEPORATOR = "-";
    private static final String WLAN_UID_TAG = "wlan UID state:";
    private static HwUidTcpMonitor hwUidTcpMonitor = null;
    private Context mContext = null;
    private Handler mHandler;
    private int mLastDnsFailedCnt = -1;
    private int mLastTopUid = 0;
    private int mLastUidDnsFailedCnt = -1;
    private final Object mTcpStatisticsLock = new Object();
    private HashMap<Integer, UidTcpStatInfo> mUidTcpStatInfo = new HashMap<>();
    private AtomicBoolean mWifiMonitorEnabled = new AtomicBoolean(false);

    public HwUidTcpMonitor(Context context) {
        this.mContext = context;
    }

    public static synchronized HwUidTcpMonitor getInstance(Context context) {
        HwUidTcpMonitor hwUidTcpMonitor2;
        synchronized (HwUidTcpMonitor.class) {
            if (hwUidTcpMonitor == null) {
                hwUidTcpMonitor = new HwUidTcpMonitor(context);
            }
            hwUidTcpMonitor2 = hwUidTcpMonitor;
        }
        return hwUidTcpMonitor2;
    }

    class TcpStatisticsRun implements Runnable {
        TcpStatisticsRun() {
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (TcpStatisticsRun.class) {
                HwUidTcpMonitor.this.logI("TcpStatisticsRun run");
                HwUidTcpMonitor.this.readTcpStatistics();
            }
        }
    }

    private Integer[] getSelectedColumnIdx(String columnNames) {
        Integer[] selectedColumnIdx = new Integer[TCP_WEB_STAT_ITEM.length];
        String[] cols = columnNames.split(TCP_SEPORATOR);
        for (int i = 0; i < TCP_WEB_STAT_ITEM.length; i++) {
            selectedColumnIdx[i] = -1;
            String selectedColumnName = TCP_WEB_STAT_ITEM[i];
            int j = 0;
            while (true) {
                if (j >= cols.length) {
                    break;
                } else if (selectedColumnName.equals(cols[j])) {
                    selectedColumnIdx[i] = Integer.valueOf(j);
                    break;
                } else {
                    j++;
                }
            }
        }
        return selectedColumnIdx;
    }

    private int getOldestUidUpdated(Map<Integer, UidTcpStatInfo> uidTcpStatInfo) {
        int oldestUid = -1;
        long oldestUpdatedTime = Long.MAX_VALUE;
        for (Map.Entry<Integer, UidTcpStatInfo> entry : uidTcpStatInfo.entrySet()) {
            Integer currKey = entry.getKey();
            UidTcpStatInfo tmp = entry.getValue();
            if (tmp != null && tmp.mLastUpdateTime < oldestUpdatedTime) {
                oldestUpdatedTime = tmp.mLastUpdateTime;
                oldestUid = currKey.intValue();
            }
        }
        return oldestUid;
    }

    private void checkWifiInternetCapability(int txCnt, int rxCnt, int reTxCnt) {
        int currDnsFailedCnt = HwSelfCureUtils.getCurrentDnsFailedCounter();
        int i = this.mLastDnsFailedCnt;
        int deltaDnsFailed = i >= 0 ? currDnsFailedCnt - i : 0;
        if (HwSelfCureEngine.getInstance() != null) {
            HwSelfCureEngine.getInstance().notifyTcpStatResults(txCnt, rxCnt, reTxCnt, deltaDnsFailed);
        }
        this.mLastDnsFailedCnt = currDnsFailedCnt;
    }

    /* JADX INFO: Multiple debug info for r19v4 int: [D('rttSeg' int), D('deltaTxCnt' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x01e5 A[SYNTHETIC, Splitter:B:108:0x01e5] */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x0241  */
    private void parseWlanUidTcpStatistics(List<String> tcpStatLines) {
        int topUid;
        Throwable th;
        boolean isNotifyByTcp;
        int topUid2;
        int sndSeg;
        int resndSeg;
        int rcvSeg;
        int rttDur;
        int rttSeg;
        int topUid3;
        int tcpStatLinesSize;
        int resndSeg2;
        int topUid4;
        boolean isNotifyByTcp2;
        int sndSeg2;
        UidTcpStatInfo lastUidTcpStatInfo;
        HwAutoConnectManager autoConnectManager;
        boolean isNotifyByTcp3;
        int i;
        boolean isNotifyByTcp4;
        List<String> list = tcpStatLines;
        HwAutoConnectManager autoConnectManager2 = HwAutoConnectManager.getInstance();
        if (autoConnectManager2 != null) {
            topUid = autoConnectManager2.getCurrentTopUid();
        } else {
            topUid = -1;
        }
        int wlanUidIdx = getWlanUidStatLineNumber(tcpStatLines);
        if (wlanUidIdx == -1) {
            return;
        }
        if (tcpStatLines.size() > wlanUidIdx + 2) {
            Integer[] selectedColumnIdx = getSelectedColumnIdx(list.get(wlanUidIdx + 1));
            synchronized (this.mTcpStatisticsLock) {
                int deltaTxCnt = 0;
                int deltaRxCnt = 0;
                int deltaReTxCnt = 0;
                try {
                    int tcpStatLinesSize2 = tcpStatLines.size();
                    boolean isNotifyByTcp5 = false;
                    int i2 = wlanUidIdx + 2;
                    while (true) {
                        if (i2 >= tcpStatLinesSize2) {
                            isNotifyByTcp = isNotifyByTcp5;
                            topUid2 = topUid;
                            break;
                        }
                        try {
                            if (!list.get(i2).startsWith("custom ip")) {
                                if (list.get(i2).length() == 0) {
                                    isNotifyByTcp = isNotifyByTcp5;
                                    topUid2 = topUid;
                                    break;
                                } else if (list.get(i2).startsWith(MOBILE_UID_TAG)) {
                                    isNotifyByTcp = isNotifyByTcp5;
                                    topUid2 = topUid;
                                    break;
                                } else {
                                    String[] tcpStatValues = list.get(i2).split(TCP_SEPORATOR);
                                    Integer[] selectedTcpStatsValues = new Integer[selectedColumnIdx.length];
                                    int j = 0;
                                    while (j < selectedTcpStatsValues.length) {
                                        try {
                                            selectedTcpStatsValues[j] = 0;
                                            if (selectedColumnIdx[j].intValue() >= 0) {
                                                isNotifyByTcp4 = isNotifyByTcp5;
                                                try {
                                                    if (selectedColumnIdx[j].intValue() < tcpStatValues.length) {
                                                        try {
                                                            selectedTcpStatsValues[j] = Integer.valueOf(Integer.parseInt(tcpStatValues[selectedColumnIdx[j].intValue()]));
                                                        } catch (NumberFormatException e) {
                                                            Log.e(TAG, "parseWlanUidTcpStatistics NumberFormatException rcv!");
                                                            return;
                                                        }
                                                    }
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    while (true) {
                                                        try {
                                                            break;
                                                        } catch (Throwable th3) {
                                                            th = th3;
                                                        }
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                isNotifyByTcp4 = isNotifyByTcp5;
                                            }
                                            j++;
                                            selectedTcpStatsValues = selectedTcpStatsValues;
                                            isNotifyByTcp5 = isNotifyByTcp4;
                                        } catch (Throwable th4) {
                                            th = th4;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    }
                                    int uid = -1;
                                    try {
                                        if (selectedTcpStatsValues.length > 0 && selectedTcpStatsValues[0].intValue() > 0) {
                                            uid = selectedTcpStatsValues[0].intValue();
                                        }
                                        if (selectedTcpStatsValues.length > 1) {
                                            sndSeg = selectedTcpStatsValues[1].intValue();
                                        } else {
                                            sndSeg = 0;
                                        }
                                        if (selectedTcpStatsValues.length > 2) {
                                            resndSeg = selectedTcpStatsValues[2].intValue();
                                        } else {
                                            resndSeg = 0;
                                        }
                                        if (selectedTcpStatsValues.length > 3) {
                                            rcvSeg = selectedTcpStatsValues[3].intValue();
                                        } else {
                                            rcvSeg = 0;
                                        }
                                        if (selectedTcpStatsValues.length > 4) {
                                            try {
                                                rttDur = selectedTcpStatsValues[4].intValue();
                                            } catch (Throwable th5) {
                                                th = th5;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        } else {
                                            rttDur = 0;
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                    try {
                                        if (selectedTcpStatsValues.length > 5) {
                                            try {
                                                rttSeg = selectedTcpStatsValues[5].intValue();
                                            } catch (Throwable th7) {
                                                th = th7;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        } else {
                                            rttSeg = 0;
                                        }
                                        int deltaTxCnt2 = deltaTxCnt + sndSeg;
                                        int deltaRxCnt2 = deltaRxCnt + rcvSeg;
                                        int deltaReTxCnt2 = deltaReTxCnt + resndSeg;
                                        if (uid == -1 || topUid != uid) {
                                            resndSeg2 = resndSeg;
                                            sndSeg2 = sndSeg;
                                            topUid3 = topUid;
                                            tcpStatLinesSize = tcpStatLinesSize2;
                                            topUid4 = rcvSeg;
                                        } else {
                                            try {
                                                HwWifiConnectivityMonitor monitor = HwWifiConnectivityMonitor.getInstance();
                                                if (monitor != null) {
                                                    resndSeg2 = resndSeg;
                                                    sndSeg2 = sndSeg;
                                                    topUid3 = topUid;
                                                    tcpStatLinesSize = tcpStatLinesSize2;
                                                    topUid4 = rcvSeg;
                                                    try {
                                                        isNotifyByTcp2 = monitor.notifyTopUidTcpInfo(topUid, sndSeg, rcvSeg, resndSeg, rttDur, rttSeg);
                                                        lastUidTcpStatInfo = this.mUidTcpStatInfo.get(Integer.valueOf(uid));
                                                        if (lastUidTcpStatInfo == null) {
                                                            try {
                                                                lastUidTcpStatInfo.setSendSegs(lastUidTcpStatInfo.getSendSegs() + ((long) sndSeg2));
                                                                try {
                                                                    lastUidTcpStatInfo.setResendSegs(lastUidTcpStatInfo.getResendSegs() + ((long) resndSeg2));
                                                                    lastUidTcpStatInfo.setRcvSegs(lastUidTcpStatInfo.getRcvSegs() + ((long) topUid4));
                                                                    lastUidTcpStatInfo.setRttDuration(lastUidTcpStatInfo.getRttDuration() + ((long) rttDur));
                                                                    lastUidTcpStatInfo.setRttSegs(lastUidTcpStatInfo.getRttSegs() + ((long) rttSeg));
                                                                    lastUidTcpStatInfo.setLastUpdateTime(System.currentTimeMillis());
                                                                    logI("parseWlanUidTcpStatistics lastUidTcpStatInfo = " + lastUidTcpStatInfo);
                                                                    i = i2;
                                                                    autoConnectManager = autoConnectManager2;
                                                                    isNotifyByTcp3 = isNotifyByTcp2;
                                                                } catch (Throwable th8) {
                                                                    th = th8;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            } catch (Throwable th9) {
                                                                th = th9;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                throw th;
                                                            }
                                                        } else {
                                                            try {
                                                                if (this.mUidTcpStatInfo.size() == 20) {
                                                                    int oldestUid = getOldestUidUpdated(this.mUidTcpStatInfo);
                                                                    this.mUidTcpStatInfo.remove(Integer.valueOf(oldestUid));
                                                                    logI("parseWlanUidTcpStatistics rm oldestUid = " + oldestUid + ", current size = " + this.mUidTcpStatInfo.size());
                                                                }
                                                                if (uid == -1 || this.mUidTcpStatInfo.size() >= 20) {
                                                                    i = i2;
                                                                    autoConnectManager = autoConnectManager2;
                                                                    isNotifyByTcp3 = isNotifyByTcp2;
                                                                } else {
                                                                    long j2 = (long) resndSeg2;
                                                                    i = i2;
                                                                    isNotifyByTcp3 = isNotifyByTcp2;
                                                                    autoConnectManager = autoConnectManager2;
                                                                    try {
                                                                        UidTcpStatInfo newUidTcpStatInfo = new UidTcpStatInfo(uid, (long) sndSeg2, j2, (long) topUid4, (long) rttDur, (long) rttSeg);
                                                                        if (autoConnectManager != null) {
                                                                            newUidTcpStatInfo.mPacketName = autoConnectManager.getCurrentPackageName();
                                                                        }
                                                                        newUidTcpStatInfo.mLastUpdateTime = System.currentTimeMillis();
                                                                        this.mUidTcpStatInfo.put(Integer.valueOf(uid), newUidTcpStatInfo);
                                                                        logI("parseWlanUidTcpStatistics newUidTcpStatInfo = " + newUidTcpStatInfo);
                                                                    } catch (Throwable th10) {
                                                                        th = th10;
                                                                        while (true) {
                                                                            break;
                                                                        }
                                                                        throw th;
                                                                    }
                                                                }
                                                            } catch (Throwable th11) {
                                                                th = th11;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                throw th;
                                                            }
                                                        }
                                                        i2 = i + 1;
                                                        list = tcpStatLines;
                                                        deltaTxCnt = deltaTxCnt2;
                                                        wlanUidIdx = wlanUidIdx;
                                                        selectedColumnIdx = selectedColumnIdx;
                                                        deltaRxCnt = deltaRxCnt2;
                                                        deltaReTxCnt = deltaReTxCnt2;
                                                        tcpStatLinesSize2 = tcpStatLinesSize;
                                                        topUid = topUid3;
                                                        isNotifyByTcp5 = isNotifyByTcp3;
                                                        autoConnectManager2 = autoConnectManager;
                                                    } catch (Throwable th12) {
                                                        th = th12;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                } else {
                                                    resndSeg2 = resndSeg;
                                                    sndSeg2 = sndSeg;
                                                    topUid3 = topUid;
                                                    tcpStatLinesSize = tcpStatLinesSize2;
                                                    topUid4 = rcvSeg;
                                                }
                                            } catch (Throwable th13) {
                                                th = th13;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        }
                                        isNotifyByTcp2 = isNotifyByTcp5;
                                        try {
                                            lastUidTcpStatInfo = this.mUidTcpStatInfo.get(Integer.valueOf(uid));
                                            if (lastUidTcpStatInfo == null) {
                                            }
                                            i2 = i + 1;
                                            list = tcpStatLines;
                                            deltaTxCnt = deltaTxCnt2;
                                            wlanUidIdx = wlanUidIdx;
                                            selectedColumnIdx = selectedColumnIdx;
                                            deltaRxCnt = deltaRxCnt2;
                                            deltaReTxCnt = deltaReTxCnt2;
                                            tcpStatLinesSize2 = tcpStatLinesSize;
                                            topUid = topUid3;
                                            isNotifyByTcp5 = isNotifyByTcp3;
                                            autoConnectManager2 = autoConnectManager;
                                        } catch (Throwable th14) {
                                            th = th14;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th15) {
                                        th = th15;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                            } else {
                                isNotifyByTcp = isNotifyByTcp5;
                                topUid2 = topUid;
                                break;
                            }
                        } catch (Throwable th16) {
                            th = th16;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    try {
                        parseWlanUidDnsStatistics(topUid2, isNotifyByTcp);
                    } catch (Throwable th17) {
                        th = th17;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } catch (Throwable th18) {
                    th = th18;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
    }

    private void parseWlanUidDnsStatistics(int topUid, boolean isNotifyByTcp) {
        HwWifiConnectivityMonitor monitor;
        String dnsFailCountStr = SystemProperties.get("hw.wifipro.uid_dns_fail_count", "0");
        if (dnsFailCountStr != null) {
            String[] uidCountArray = dnsFailCountStr.split(DNS_SEPORATOR);
            if (uidCountArray.length <= 8) {
                String[] strArr = new String[2];
                int deltaDnsFailCnt = 0;
                int length = uidCountArray.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String[] uidAndCount = uidCountArray[i].split(UID_COUNT_SEPORATOR, 2);
                    if (uidAndCount.length == 2) {
                        try {
                            int newUid = Integer.parseInt(uidAndCount[0]);
                            int newDnsFailCount = Integer.parseInt(uidAndCount[1]);
                            if (newUid != topUid) {
                                i++;
                            } else {
                                int i2 = this.mLastTopUid;
                                if (topUid != i2 || i2 == 0) {
                                    this.mLastTopUid = topUid;
                                    deltaDnsFailCnt = 0;
                                } else {
                                    deltaDnsFailCnt = newDnsFailCount - this.mLastUidDnsFailedCnt;
                                }
                                this.mLastUidDnsFailedCnt = newDnsFailCount;
                            }
                        } catch (NumberFormatException e) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                if (deltaDnsFailCnt > 0 && !isNotifyByTcp && (monitor = HwWifiConnectivityMonitor.getInstance()) != null) {
                    monitor.notifyTopUidDnsInfo(topUid, deltaDnsFailCnt);
                }
            }
        }
    }

    public static List<String> getFileResult(String fileName) {
        List<String> result = new ArrayList<>();
        FileInputStream fileStream = null;
        BufferedReader dr = null;
        try {
            FileInputStream fileStream2 = new FileInputStream(fileName);
            BufferedReader dr2 = new BufferedReader(new InputStreamReader(fileStream2, "US-ASCII"));
            for (String line = dr2.readLine(); line != null; line = dr2.readLine()) {
                String line2 = line.trim();
                if (!line2.equals("")) {
                    result.add(line2);
                }
            }
            try {
                dr2.close();
            } catch (IOException e) {
                Log.e(TAG, "getFileResult throw IOException when close BufferedReader");
            }
            try {
                fileStream2.close();
            } catch (IOException e2) {
                Log.e(TAG, "getFileResult throw IOException when close FileInputStream");
            }
        } catch (FileNotFoundException e3) {
            Log.e(TAG, "getFileResult throw FileNotFoundException");
            if (0 != 0) {
                try {
                    dr.close();
                } catch (IOException e4) {
                    Log.e(TAG, "getFileResult throw IOException when close BufferedReader");
                }
            }
            if (0 != 0) {
                fileStream.close();
            }
        } catch (IOException e5) {
            Log.e(TAG, "getFileResult throw IOException");
            if (0 != 0) {
                try {
                    dr.close();
                } catch (IOException e6) {
                    Log.e(TAG, "getFileResult throw IOException when close BufferedReader");
                }
            }
            if (0 != 0) {
                fileStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    dr.close();
                } catch (IOException e7) {
                    Log.e(TAG, "getFileResult throw IOException when close BufferedReader");
                }
            }
            if (0 != 0) {
                try {
                    fileStream.close();
                } catch (IOException e8) {
                    Log.e(TAG, "getFileResult throw IOException when close FileInputStream");
                }
            }
            throw th;
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readTcpStatistics() {
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 79, (Bundle) null);
    }

    public void parseTcpStatLines(List<String> data) {
        if (data != null && data.size() != 0) {
            parseWlanUidTcpStatistics(data);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0012, code lost:
        r1 = th;
     */
    public synchronized HashMap<Integer, UidTcpStatInfo> getUidTcpStatistics() {
        HashMap<Integer, UidTcpStatInfo> cloneMap;
        synchronized (this.mTcpStatisticsLock) {
            cloneMap = (HashMap) this.mUidTcpStatInfo.clone();
        }
        return cloneMap;
        while (true) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0022, code lost:
        r1 = th;
     */
    public synchronized boolean isAppAccessInternet(int appUid) {
        boolean z;
        synchronized (this.mTcpStatisticsLock) {
            UidTcpStatInfo matchedUid = null;
            if (appUid != -1) {
                try {
                    matchedUid = this.mUidTcpStatInfo.get(Integer.valueOf(appUid));
                } catch (Throwable th) {
                    th = th;
                    while (true) {
                        throw th;
                    }
                }
            }
            z = matchedUid != null;
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0028, code lost:
        r1 = th;
     */
    public synchronized long getRttDuration(int appUid, int wifiState) {
        synchronized (this.mTcpStatisticsLock) {
            UidTcpStatInfo matchedUid = null;
            if (appUid != -1) {
                try {
                    matchedUid = this.mUidTcpStatInfo.get(Integer.valueOf(appUid));
                } catch (Throwable th) {
                    th = th;
                    while (true) {
                        throw th;
                    }
                }
            }
            if (matchedUid == null) {
                return 0;
            }
            return matchedUid.mRttDuration;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0028, code lost:
        r1 = th;
     */
    public synchronized long getRttSegs(int appUid, int wifiState) {
        synchronized (this.mTcpStatisticsLock) {
            UidTcpStatInfo matchedUid = null;
            if (appUid != -1) {
                try {
                    matchedUid = this.mUidTcpStatInfo.get(Integer.valueOf(appUid));
                } catch (Throwable th) {
                    th = th;
                    while (true) {
                        throw th;
                    }
                }
            }
            if (matchedUid == null) {
                return 0;
            }
            return matchedUid.mRttSegs;
        }
    }

    public synchronized void updateUidTcpStatistics() {
        if (this.mWifiMonitorEnabled.get()) {
            new Thread(new TcpStatisticsRun()).start();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        r1 = th;
     */
    public synchronized void notifyWifiMonitorEnabled(boolean enabled) {
        this.mWifiMonitorEnabled.set(enabled);
        if (!this.mWifiMonitorEnabled.get()) {
            synchronized (this.mTcpStatisticsLock) {
                this.mUidTcpStatInfo.clear();
                this.mLastDnsFailedCnt = -1;
            }
        }
        return;
        while (true) {
        }
    }

    private int getWlanUidStatLineNumber(List<String> tcpStatLines) {
        int linesSize = tcpStatLines.size();
        for (int i = 0; i < linesSize; i++) {
            if (tcpStatLines.get(i).startsWith(WLAN_UID_TAG)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public static class UidTcpStatInfo {
        private long mLastUpdateTime = 0;
        private String mPacketName = "";
        private long mRcvSegs;
        private long mResendSegs;
        private long mRttDuration;
        private long mRttSegs;
        private long mSendSegs;
        private int mUid;

        public UidTcpStatInfo(int uid, long sendSegs, long resendSegs, long rcvSegs, long rttDuration, long rttSegs) {
            this.mUid = uid;
            this.mSendSegs = sendSegs;
            this.mResendSegs = resendSegs;
            this.mRcvSegs = rcvSegs;
            this.mRttDuration = rttDuration;
            this.mRttSegs = rttSegs;
        }

        public String getPacketName() {
            return this.mPacketName;
        }

        public void setPacketName(String packetName) {
            this.mPacketName = packetName;
        }

        public long getLastUpdateTime() {
            return this.mLastUpdateTime;
        }

        public void setLastUpdateTime(long lastUpdateTime) {
            this.mLastUpdateTime = lastUpdateTime;
        }

        public int getUid() {
            return this.mUid;
        }

        public void setUid(int uid) {
            this.mUid = uid;
        }

        public long getSendSegs() {
            return this.mSendSegs;
        }

        public void setSendSegs(long sendSegs) {
            this.mSendSegs = sendSegs;
        }

        public long getResendSegs() {
            return this.mResendSegs;
        }

        public void setResendSegs(long resendSegs) {
            this.mResendSegs = resendSegs;
        }

        public long getRcvSegs() {
            return this.mRcvSegs;
        }

        public void setRcvSegs(long rcvSegs) {
            this.mRcvSegs = rcvSegs;
        }

        public long getRttDuration() {
            return this.mRttDuration;
        }

        public void setRttDuration(long rttDuration) {
            this.mRttDuration = rttDuration;
        }

        public long getRttSegs() {
            return this.mRttSegs;
        }

        public void setRttSegs(long rttSegs) {
            this.mRttSegs = rttSegs;
        }

        public String toString() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("[");
            sbuf.append(" mUid = " + getUid());
            sbuf.append(" mPacketName = ");
            sbuf.append(getPacketName());
            sbuf.append(" mSendSegs = ");
            sbuf.append(getSendSegs());
            sbuf.append(" mResendSegs = " + getResendSegs());
            sbuf.append(" mRcvSegs = " + getRcvSegs());
            sbuf.append(" ]");
            return sbuf.toString();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logI(String msg) {
        Log.i(TAG, msg);
    }
}
