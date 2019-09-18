package com.android.server.wifi;

import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.wifi.wifipro.HwAutoConnectManager;
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
    private static final int MAX_UID_DNS_SIZE = 8;
    private static final int MAX_UID_SIZE = 20;
    private static final String TAG = "HwUidTcpMonitor";
    private static String TCP_STAT_PATH = "proc/net/wifipro_tcp_stat";
    public static final String[] TCP_WEB_STAT_ITEM = {"UID", "WEBSENDSEGS", "WEBRESENDSEGS", "WEBRECVSEGS", "WEBRTTDURATION", "WEBRTTSEGS"};
    private static HwUidTcpMonitor hwUidTcpMonitor = null;
    private String DNS_SEPORATOR = "/";
    private String MOBILE_UID_TAG = "mobile UID state:";
    private String TCP_SEPORATOR = "\t";
    private String UID_COUNT_SEPORATOR = "-";
    private String WLAN_UID_TAG = "wlan UID state:";
    private Context mContext = null;
    private Handler mHandler;
    private int mLastDnsFailedCnt = -1;
    private int mLastTopUid = 0;
    private int mLastUidDnsFailedCnt = -1;
    private Object mTcpStatisticsLock = new Object();
    private HashMap<Integer, UidTcpStatInfo> mUidTcpStatInfo = new HashMap<>();
    private AtomicBoolean mWifiMonitorEnabled = new AtomicBoolean(false);

    class TcpStatisticsRun implements Runnable {
        TcpStatisticsRun() {
        }

        public void run() {
            synchronized (TcpStatisticsRun.class) {
                Log.i(HwUidTcpMonitor.TAG, "TcpStatisticsRun run");
                HwUidTcpMonitor.this.readAndParseTcpStatistics();
            }
        }
    }

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
            return "[" + (" mUid = " + this.mUid) + (" mPacketName = " + this.mPacketName) + (" mSendSegs = " + this.mSendSegs) + (" mResendSegs = " + this.mResendSegs) + (" mRcvSegs = " + this.mRcvSegs) + " ]";
        }
    }

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

    private Integer[] getSelectedColumnIdx(String columnNames) {
        Integer[] selectedColumnIdx = new Integer[TCP_WEB_STAT_ITEM.length];
        String[] cols = columnNames.split(this.TCP_SEPORATOR);
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
        for (Map.Entry entry : uidTcpStatInfo.entrySet()) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:168:0x030e, code lost:
        r43 = r3;
        r2 = r4;
        r39 = r12;
        r42 = r14;
        r45 = r15;
        r3 = r1;
        r14 = r11;
     */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x017a  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x01d0 A[SYNTHETIC, Splitter:B:121:0x01d0] */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0214  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0110 A[SYNTHETIC, Splitter:B:60:0x0110] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0117  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x011f A[SYNTHETIC, Splitter:B:67:0x011f] */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x012f A[SYNTHETIC, Splitter:B:75:0x012f] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0137  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x013f A[SYNTHETIC, Splitter:B:83:0x013f] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0152  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x015a A[SYNTHETIC, Splitter:B:94:0x015a] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0165  */
    private void parseWlanUidTcpStatistics(List<String> tcpStatLines) {
        Object obj;
        Integer[] selectedTcpStatsValues;
        int sndSeg;
        int uid;
        int sndSeg2;
        int resndSeg;
        int rcvSeg;
        boolean isNotifyByTcp;
        int rttDur;
        int rttSeg;
        int topUid;
        Integer[] selectedColumnIdx;
        int resndSeg2;
        int i;
        int rcvSeg2;
        int topUid2;
        UidTcpStatInfo lastUidTcpStatInfo;
        HwUidTcpMonitor hwUidTcpMonitor2;
        HwAutoConnectManager autoConnectManager;
        UidTcpStatInfo newUidTcpStatInfo;
        HwWifiConnectivityMonitor monitor;
        HwUidTcpMonitor hwUidTcpMonitor3 = this;
        List<String> list = tcpStatLines;
        int topUid3 = -1;
        HwAutoConnectManager autoConnectManager2 = HwAutoConnectManager.getInstance();
        if (autoConnectManager2 != null) {
            topUid3 = autoConnectManager2.getCurrentTopUid();
        }
        int wlanUidIdx = getWlanUidStatLineNumber(tcpStatLines);
        if (wlanUidIdx == -1) {
            int i2 = wlanUidIdx;
            int i3 = topUid3;
            HwUidTcpMonitor hwUidTcpMonitor4 = hwUidTcpMonitor3;
            int i4 = i3;
        } else if (tcpStatLines.size() <= wlanUidIdx + 2) {
            HwAutoConnectManager hwAutoConnectManager = autoConnectManager2;
            int i5 = wlanUidIdx;
            int i6 = topUid3;
            HwUidTcpMonitor hwUidTcpMonitor5 = hwUidTcpMonitor3;
            int i7 = i6;
        } else {
            Integer[] selectedColumnIdx2 = hwUidTcpMonitor3.getSelectedColumnIdx(list.get(wlanUidIdx + 1));
            Object obj2 = hwUidTcpMonitor3.mTcpStatisticsLock;
            synchronized (obj2) {
                int deltaTxCnt = 0;
                int deltaRxCnt = 0;
                int deltaReTxCnt = 0;
                int i8 = wlanUidIdx + 2;
                boolean isNotifyByTcp2 = false;
                while (true) {
                    int i9 = i8;
                    try {
                        if (i9 < tcpStatLines.size()) {
                            if (!list.get(i9).startsWith("custom ip")) {
                                if (list.get(i9).length() == 0) {
                                    break;
                                } else if (list.get(i9).startsWith(hwUidTcpMonitor3.MOBILE_UID_TAG)) {
                                    break;
                                } else {
                                    String[] tcpStatValues = list.get(i9).split(hwUidTcpMonitor3.TCP_SEPORATOR);
                                    Integer[] selectedTcpStatsValues2 = new Integer[selectedColumnIdx2.length];
                                    int i10 = 0;
                                    int j = 0;
                                    while (true) {
                                        selectedTcpStatsValues = selectedTcpStatsValues2;
                                        int j2 = j;
                                        if (j2 >= selectedTcpStatsValues.length) {
                                            break;
                                        }
                                        try {
                                            selectedTcpStatsValues[j2] = Integer.valueOf(i10);
                                            if (selectedColumnIdx2[j2].intValue() >= 0 && selectedColumnIdx2[j2].intValue() < tcpStatValues.length) {
                                                selectedTcpStatsValues[j2] = Integer.valueOf(Integer.parseInt(tcpStatValues[selectedColumnIdx2[j2].intValue()]));
                                            }
                                            j = j2 + 1;
                                            selectedTcpStatsValues2 = selectedTcpStatsValues;
                                            i10 = 0;
                                            List<String> list2 = tcpStatLines;
                                        } catch (NumberFormatException e) {
                                            hwUidTcpMonitor3.LOGD("parseWlanUidTcpStatistics NumberFormatException rcv!");
                                            return;
                                        } catch (Throwable th) {
                                            th = th;
                                            HwAutoConnectManager hwAutoConnectManager2 = autoConnectManager2;
                                            int i11 = wlanUidIdx;
                                            Integer[] numArr = selectedColumnIdx2;
                                            obj = obj2;
                                            int i12 = topUid3;
                                            HwUidTcpMonitor hwUidTcpMonitor6 = hwUidTcpMonitor3;
                                            int topUid4 = i12;
                                            while (true) {
                                                try {
                                                    break;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                }
                                            }
                                            throw th;
                                        }
                                    }
                                    if (selectedTcpStatsValues.length > 0) {
                                        sndSeg = 0;
                                        if (selectedTcpStatsValues[0].intValue() > 0) {
                                            uid = selectedTcpStatsValues[0].intValue();
                                            if (selectedTcpStatsValues.length <= 1) {
                                                sndSeg2 = selectedTcpStatsValues[1].intValue();
                                            } else {
                                                sndSeg2 = sndSeg;
                                            }
                                            String[] tcpStatValues2 = tcpStatValues;
                                            if (selectedTcpStatsValues.length <= 2) {
                                                resndSeg = selectedTcpStatsValues[2].intValue();
                                            } else {
                                                resndSeg = 0;
                                            }
                                            int i13 = i9;
                                            if (selectedTcpStatsValues.length <= 3) {
                                                rcvSeg = selectedTcpStatsValues[3].intValue();
                                            } else {
                                                rcvSeg = 0;
                                            }
                                            isNotifyByTcp = isNotifyByTcp2;
                                            if (selectedTcpStatsValues.length <= 4) {
                                                try {
                                                    rttDur = selectedTcpStatsValues[4].intValue();
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                    HwAutoConnectManager hwAutoConnectManager3 = autoConnectManager2;
                                                    int i14 = wlanUidIdx;
                                                    obj = obj2;
                                                    int i122 = topUid3;
                                                    HwUidTcpMonitor hwUidTcpMonitor62 = hwUidTcpMonitor3;
                                                    int topUid42 = i122;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                rttDur = 0;
                                            }
                                            int wlanUidIdx2 = wlanUidIdx;
                                            if (selectedTcpStatsValues.length <= 5) {
                                                try {
                                                    rttSeg = selectedTcpStatsValues[5].intValue();
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    HwAutoConnectManager hwAutoConnectManager4 = autoConnectManager2;
                                                    obj = obj2;
                                                    int i1222 = topUid3;
                                                    HwUidTcpMonitor hwUidTcpMonitor622 = hwUidTcpMonitor3;
                                                    int topUid422 = i1222;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                rttSeg = 0;
                                            }
                                            int deltaTxCnt2 = deltaTxCnt + sndSeg2;
                                            int deltaRxCnt2 = deltaRxCnt + rcvSeg;
                                            int deltaReTxCnt2 = deltaReTxCnt + resndSeg;
                                            if (uid != -1 && topUid3 == uid) {
                                                monitor = HwWifiConnectivityMonitor.getInstance();
                                                if (monitor != null) {
                                                    int i15 = topUid3;
                                                    resndSeg2 = resndSeg;
                                                    String[] strArr = tcpStatValues2;
                                                    Integer[] numArr2 = selectedTcpStatsValues;
                                                    i = i13;
                                                    rcvSeg2 = rcvSeg;
                                                    topUid = topUid3;
                                                    topUid2 = rttDur;
                                                    selectedColumnIdx = selectedColumnIdx2;
                                                    try {
                                                        isNotifyByTcp2 = monitor.notifyTopUidTcpInfo(i15, sndSeg2, rcvSeg, resndSeg, rttDur, rttSeg);
                                                        lastUidTcpStatInfo = hwUidTcpMonitor3.mUidTcpStatInfo.get(Integer.valueOf(uid));
                                                        if (lastUidTcpStatInfo != null) {
                                                            try {
                                                                lastUidTcpStatInfo.mSendSegs += (long) sndSeg2;
                                                                lastUidTcpStatInfo.mResendSegs += (long) resndSeg2;
                                                                lastUidTcpStatInfo.mRcvSegs += (long) rcvSeg2;
                                                                lastUidTcpStatInfo.mRttDuration += (long) topUid2;
                                                                lastUidTcpStatInfo.mRttSegs += (long) rttSeg;
                                                                lastUidTcpStatInfo.mLastUpdateTime = System.currentTimeMillis();
                                                                hwUidTcpMonitor3.LOGD("parseWlanUidTcpStatistics lastUidTcpStatInfo = " + lastUidTcpStatInfo);
                                                                hwUidTcpMonitor2 = hwUidTcpMonitor3;
                                                                autoConnectManager = autoConnectManager2;
                                                                obj = obj2;
                                                            } catch (Throwable th5) {
                                                                th = th5;
                                                                HwUidTcpMonitor hwUidTcpMonitor7 = hwUidTcpMonitor3;
                                                                HwAutoConnectManager hwAutoConnectManager5 = autoConnectManager2;
                                                                obj = obj2;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                throw th;
                                                            }
                                                        } else {
                                                            int resndSeg3 = resndSeg2;
                                                            if (hwUidTcpMonitor3.mUidTcpStatInfo.size() == 20) {
                                                                hwUidTcpMonitor3.mUidTcpStatInfo.remove(Integer.valueOf(hwUidTcpMonitor3.getOldestUidUpdated(hwUidTcpMonitor3.mUidTcpStatInfo)));
                                                                hwUidTcpMonitor3.LOGD("parseWlanUidTcpStatistics rm oldestUid = " + oldestUid + ", current size = " + hwUidTcpMonitor3.mUidTcpStatInfo.size());
                                                            }
                                                            if (uid != -1) {
                                                                if (hwUidTcpMonitor3.mUidTcpStatInfo.size() < 20) {
                                                                    UidTcpStatInfo uidTcpStatInfo = r23;
                                                                    obj = obj2;
                                                                    int i16 = sndSeg2;
                                                                    int i17 = topUid2;
                                                                    HwAutoConnectManager autoConnectManager3 = autoConnectManager2;
                                                                    r23 = uidTcpStatInfo;
                                                                    try {
                                                                        UidTcpStatInfo uidTcpStatInfo2 = new UidTcpStatInfo(uid, (long) sndSeg2, (long) resndSeg3, (long) rcvSeg2, (long) topUid2, (long) rttSeg);
                                                                        newUidTcpStatInfo = uidTcpStatInfo;
                                                                        if (autoConnectManager3 != null) {
                                                                            autoConnectManager = autoConnectManager3;
                                                                            try {
                                                                                newUidTcpStatInfo.mPacketName = autoConnectManager.getCurrentPackageName();
                                                                            } catch (Throwable th6) {
                                                                                th = th6;
                                                                                int i18 = topUid;
                                                                            }
                                                                        } else {
                                                                            autoConnectManager = autoConnectManager3;
                                                                        }
                                                                    } catch (Throwable th7) {
                                                                        th = th7;
                                                                        HwAutoConnectManager hwAutoConnectManager6 = autoConnectManager3;
                                                                        int i19 = topUid;
                                                                        while (true) {
                                                                            break;
                                                                        }
                                                                        throw th;
                                                                    }
                                                                    try {
                                                                        newUidTcpStatInfo.mLastUpdateTime = System.currentTimeMillis();
                                                                        hwUidTcpMonitor2 = this;
                                                                        try {
                                                                            hwUidTcpMonitor2.mUidTcpStatInfo.put(Integer.valueOf(uid), newUidTcpStatInfo);
                                                                            hwUidTcpMonitor2.LOGD("parseWlanUidTcpStatistics newUidTcpStatInfo = " + newUidTcpStatInfo);
                                                                        } catch (Throwable th8) {
                                                                            th = th8;
                                                                        }
                                                                    } catch (Throwable th9) {
                                                                        th = th9;
                                                                        while (true) {
                                                                            break;
                                                                        }
                                                                        throw th;
                                                                    }
                                                                }
                                                            }
                                                            hwUidTcpMonitor2 = hwUidTcpMonitor3;
                                                            autoConnectManager = autoConnectManager2;
                                                            obj = obj2;
                                                        }
                                                        i8 = i + 1;
                                                        autoConnectManager2 = autoConnectManager;
                                                        hwUidTcpMonitor3 = hwUidTcpMonitor2;
                                                        deltaTxCnt = deltaTxCnt2;
                                                        deltaRxCnt = deltaRxCnt2;
                                                        deltaReTxCnt = deltaReTxCnt2;
                                                        wlanUidIdx = wlanUidIdx2;
                                                        selectedColumnIdx2 = selectedColumnIdx;
                                                        topUid3 = topUid;
                                                        obj2 = obj;
                                                        list = tcpStatLines;
                                                    } catch (Throwable th10) {
                                                        th = th10;
                                                        HwUidTcpMonitor hwUidTcpMonitor8 = hwUidTcpMonitor3;
                                                        HwAutoConnectManager hwAutoConnectManager7 = autoConnectManager2;
                                                        boolean z = isNotifyByTcp;
                                                        obj = obj2;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                }
                                            }
                                            topUid = topUid3;
                                            resndSeg2 = resndSeg;
                                            topUid2 = rttDur;
                                            Integer[] numArr3 = selectedTcpStatsValues;
                                            selectedColumnIdx = selectedColumnIdx2;
                                            String[] strArr2 = tcpStatValues2;
                                            i = i13;
                                            rcvSeg2 = rcvSeg;
                                            isNotifyByTcp2 = isNotifyByTcp;
                                            lastUidTcpStatInfo = hwUidTcpMonitor3.mUidTcpStatInfo.get(Integer.valueOf(uid));
                                            if (lastUidTcpStatInfo != null) {
                                            }
                                            i8 = i + 1;
                                            autoConnectManager2 = autoConnectManager;
                                            hwUidTcpMonitor3 = hwUidTcpMonitor2;
                                            deltaTxCnt = deltaTxCnt2;
                                            deltaRxCnt = deltaRxCnt2;
                                            deltaReTxCnt = deltaReTxCnt2;
                                            wlanUidIdx = wlanUidIdx2;
                                            selectedColumnIdx2 = selectedColumnIdx;
                                            topUid3 = topUid;
                                            obj2 = obj;
                                            list = tcpStatLines;
                                        }
                                    } else {
                                        sndSeg = 0;
                                    }
                                    uid = -1;
                                    if (selectedTcpStatsValues.length <= 1) {
                                    }
                                    try {
                                        String[] tcpStatValues22 = tcpStatValues;
                                        if (selectedTcpStatsValues.length <= 2) {
                                        }
                                        int i132 = i9;
                                        if (selectedTcpStatsValues.length <= 3) {
                                        }
                                        isNotifyByTcp = isNotifyByTcp2;
                                        if (selectedTcpStatsValues.length <= 4) {
                                        }
                                    } catch (Throwable th11) {
                                        th = th11;
                                        int i20 = topUid3;
                                        HwAutoConnectManager hwAutoConnectManager8 = autoConnectManager2;
                                        int i21 = wlanUidIdx;
                                        Integer[] numArr4 = selectedColumnIdx2;
                                        obj = obj2;
                                        HwUidTcpMonitor hwUidTcpMonitor9 = hwUidTcpMonitor3;
                                        boolean z2 = isNotifyByTcp2;
                                        int i22 = i20;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                    try {
                                        int wlanUidIdx22 = wlanUidIdx;
                                        if (selectedTcpStatsValues.length <= 5) {
                                        }
                                        int deltaTxCnt22 = deltaTxCnt + sndSeg2;
                                        int deltaRxCnt22 = deltaRxCnt + rcvSeg;
                                        int deltaReTxCnt22 = deltaReTxCnt + resndSeg;
                                    } catch (Throwable th12) {
                                        th = th12;
                                        int i23 = topUid3;
                                        HwAutoConnectManager hwAutoConnectManager9 = autoConnectManager2;
                                        int i24 = wlanUidIdx;
                                        Integer[] numArr5 = selectedColumnIdx2;
                                        obj = obj2;
                                        HwUidTcpMonitor hwUidTcpMonitor10 = hwUidTcpMonitor3;
                                        boolean z3 = isNotifyByTcp;
                                        int i25 = i23;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                    try {
                                        monitor = HwWifiConnectivityMonitor.getInstance();
                                        if (monitor != null) {
                                        }
                                        topUid = topUid3;
                                        resndSeg2 = resndSeg;
                                        topUid2 = rttDur;
                                        Integer[] numArr32 = selectedTcpStatsValues;
                                        selectedColumnIdx = selectedColumnIdx2;
                                        String[] strArr22 = tcpStatValues22;
                                        i = i132;
                                        rcvSeg2 = rcvSeg;
                                        isNotifyByTcp2 = isNotifyByTcp;
                                    } catch (Throwable th13) {
                                        th = th13;
                                        Integer[] numArr6 = selectedColumnIdx2;
                                        HwAutoConnectManager hwAutoConnectManager10 = autoConnectManager2;
                                        boolean z4 = isNotifyByTcp;
                                        obj = obj2;
                                        int i26 = topUid3;
                                        HwUidTcpMonitor hwUidTcpMonitor11 = hwUidTcpMonitor3;
                                        int i27 = i26;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                    try {
                                        lastUidTcpStatInfo = hwUidTcpMonitor3.mUidTcpStatInfo.get(Integer.valueOf(uid));
                                        if (lastUidTcpStatInfo != null) {
                                        }
                                        i8 = i + 1;
                                        autoConnectManager2 = autoConnectManager;
                                        hwUidTcpMonitor3 = hwUidTcpMonitor2;
                                        deltaTxCnt = deltaTxCnt22;
                                        deltaRxCnt = deltaRxCnt22;
                                        deltaReTxCnt = deltaReTxCnt22;
                                        wlanUidIdx = wlanUidIdx22;
                                        selectedColumnIdx2 = selectedColumnIdx;
                                        topUid3 = topUid;
                                        obj2 = obj;
                                        list = tcpStatLines;
                                    } catch (Throwable th14) {
                                        th = th14;
                                        HwUidTcpMonitor hwUidTcpMonitor12 = hwUidTcpMonitor3;
                                        HwAutoConnectManager hwAutoConnectManager11 = autoConnectManager2;
                                        obj = obj2;
                                        int i28 = topUid;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } catch (Throwable th15) {
                        th = th15;
                        HwAutoConnectManager hwAutoConnectManager12 = autoConnectManager2;
                        int i29 = wlanUidIdx;
                        Integer[] numArr7 = selectedColumnIdx2;
                        obj = obj2;
                        boolean z5 = isNotifyByTcp2;
                        int i30 = topUid3;
                        HwUidTcpMonitor hwUidTcpMonitor13 = hwUidTcpMonitor3;
                        int i31 = i30;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                int topUid5 = topUid3;
                Object obj3 = obj2;
                HwUidTcpMonitor hwUidTcpMonitor14 = hwUidTcpMonitor3;
                boolean isNotifyByTcp3 = isNotifyByTcp2;
                try {
                    hwUidTcpMonitor14.parseWlanUidDnsStatistics(topUid5, isNotifyByTcp3);
                } catch (Throwable th16) {
                    th = th16;
                    int i32 = topUid5;
                    boolean z6 = isNotifyByTcp3;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
    }

    private void parseWlanUidDnsStatistics(int topUid, boolean isNotifyByTcp) {
        int deltaDnsFailCnt;
        String[] strArr = new String[2];
        int deltaDnsFailCnt2 = 0;
        String dnsFailCountStr = SystemProperties.get("hw.wifipro.uid_dns_fail_count", "0");
        if (dnsFailCountStr != null) {
            String[] uidCountArray = dnsFailCountStr.split(this.DNS_SEPORATOR);
            if (uidCountArray.length <= 8) {
                int i = 0;
                while (true) {
                    if (i >= uidCountArray.length) {
                        break;
                    }
                    String[] uidAndCount = uidCountArray[i].split(this.UID_COUNT_SEPORATOR, 2);
                    if (uidAndCount.length == 2) {
                        try {
                            int newUid = Integer.parseInt(uidAndCount[0]);
                            int newDnsFailCount = Integer.parseInt(uidAndCount[1]);
                            if (newUid != topUid) {
                                i++;
                            } else {
                                if (topUid != this.mLastTopUid || this.mLastTopUid == 0) {
                                    this.mLastTopUid = topUid;
                                    deltaDnsFailCnt = 0;
                                } else {
                                    deltaDnsFailCnt = newDnsFailCount - this.mLastUidDnsFailedCnt;
                                }
                                deltaDnsFailCnt2 = deltaDnsFailCnt;
                                this.mLastUidDnsFailedCnt = newDnsFailCount;
                            }
                        } catch (NumberFormatException e) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                if (deltaDnsFailCnt2 > 0 && !isNotifyByTcp) {
                    HwWifiConnectivityMonitor monitor = HwWifiConnectivityMonitor.getInstance();
                    if (monitor != null) {
                        monitor.notifyTopUidDnsInfo(topUid, deltaDnsFailCnt2);
                    }
                }
            }
        }
    }

    public static List<String> getFileResult(String fileName) {
        List<String> result = new ArrayList<>();
        FileInputStream f = null;
        BufferedReader dr = null;
        try {
            FileInputStream f2 = new FileInputStream(fileName);
            BufferedReader dr2 = new BufferedReader(new InputStreamReader(f2, "US-ASCII"));
            String readLine = dr2.readLine();
            while (true) {
                String line = readLine;
                if (line == null) {
                    break;
                }
                String line2 = line.trim();
                if (!line2.equals("")) {
                    result.add(line2);
                }
                readLine = dr2.readLine();
            }
            dr2.close();
            f2.close();
            try {
                dr2.close();
            } catch (IOException e) {
                Log.e(TAG, "getFileResult throw IOException when close BufferedReader");
            }
            try {
                f2.close();
            } catch (IOException e2) {
                Log.e(TAG, "getFileResult throw IOException when close FileInputStream");
            }
        } catch (FileNotFoundException e3) {
            Log.e(TAG, "getFileResult throw FileNotFoundException");
            if (dr != null) {
                try {
                    dr.close();
                } catch (IOException e4) {
                    Log.e(TAG, "getFileResult throw IOException when close BufferedReader");
                }
            }
            if (f != null) {
                f.close();
            }
        } catch (IOException e5) {
            Log.e(TAG, "getFileResult throw IOException");
            if (dr != null) {
                try {
                    dr.close();
                } catch (IOException e6) {
                    Log.e(TAG, "getFileResult throw IOException when close BufferedReader");
                }
            }
            if (f != null) {
                f.close();
            }
        } catch (Throwable th) {
            if (dr != null) {
                try {
                    dr.close();
                } catch (IOException e7) {
                    Log.e(TAG, "getFileResult throw IOException when close BufferedReader");
                }
            }
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e8) {
                    Log.e(TAG, "getFileResult throw IOException when close FileInputStream");
                }
            }
            throw th;
        }
        return result;
    }

    /* access modifiers changed from: private */
    public void readAndParseTcpStatistics() {
        List<String> tcpStatLines = getFileResult(TCP_STAT_PATH);
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
            UidTcpStatInfo matchedUid = null;
            if (appUid != -1) {
                try {
                    matchedUid = this.mUidTcpStatInfo.get(Integer.valueOf(appUid));
                } catch (Throwable th) {
                    while (true) {
                    }
                    throw th;
                }
            }
            z = matchedUid != null;
        }
        return z;
    }

    public synchronized long getRttDuration(int appUid, int wifiState) {
        synchronized (this.mTcpStatisticsLock) {
            UidTcpStatInfo matchedUid = null;
            if (appUid != -1) {
                try {
                    matchedUid = this.mUidTcpStatInfo.get(Integer.valueOf(appUid));
                } catch (Throwable th) {
                    while (true) {
                    }
                    throw th;
                }
            }
            if (matchedUid == null) {
                return 0;
            }
            long j = matchedUid.mRttDuration;
            return j;
        }
    }

    public synchronized long getRttSegs(int appUid, int wifiState) {
        synchronized (this.mTcpStatisticsLock) {
            UidTcpStatInfo matchedUid = null;
            if (appUid != -1) {
                try {
                    matchedUid = this.mUidTcpStatInfo.get(Integer.valueOf(appUid));
                } catch (Throwable th) {
                    while (true) {
                    }
                    throw th;
                }
            }
            if (matchedUid == null) {
                return 0;
            }
            long j = matchedUid.mRttSegs;
            return j;
        }
    }

    public synchronized void updateUidTcpStatistics() {
        if (this.mWifiMonitorEnabled.get()) {
            new Thread(new TcpStatisticsRun()).start();
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
            if (tcpStatLines.get(i).startsWith(this.WLAN_UID_TAG)) {
                return i;
            }
        }
        return -1;
    }

    private void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
