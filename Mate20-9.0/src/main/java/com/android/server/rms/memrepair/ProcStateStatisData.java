package com.android.server.rms.memrepair;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcStateStatisData {
    private static final String BACKGROUND = "background";
    private static final int BACKGROUND_STATE = 1;
    public static final int MAX_COUNT = 50;
    public static final int MAX_INTERVAL = 30;
    public static final int MIN_COUNT = 6;
    public static final int MIN_INTERVAL = 2;
    public static final String SEPERATOR_CHAR = "|";
    private static final String TAG = "AwareMem_PSSData";
    private static final String TOP = "top";
    private static final int TOP_STATE = 0;
    private static ProcStateStatisData mProcStateStatisData;
    private final BroadcastReceiver br = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            AwareLog.d(ProcStateStatisData.TAG, "onReceive...");
            if (intent != null && "android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                Uri data = intent.getData();
                if (intent.getData() != null) {
                    String packageName = data.getSchemeSpecificPart();
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    AwareLog.d(ProcStateStatisData.TAG, "packageName=" + packageName + "uid=" + uid);
                    synchronized (ProcStateStatisData.this.mHistoryMap) {
                        ProcStateStatisData.this.mHistoryMap.remove(uid + "|" + packageName);
                    }
                }
            }
        }
    };
    private int customProcessState = 19;
    private int[] mCollectCounts = {6, 6};
    private Context mContext;
    private long mDropThreshold = 0;
    private AtomicBoolean mEnabled = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public Map<String, HistoryProcInfo> mHistoryMap = new ArrayMap();
    private long[] mIntervalTime = {120000, 900000};
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public Map<String, ProcMemValueInfo> mProcMemValueMap = new ArrayMap();
    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }

        public void onProcessDied(int pid, int uid) {
            AwareLog.d(ProcStateStatisData.TAG, "onProcessDied pid = " + pid + ", uid = " + uid);
            StringBuilder sb = new StringBuilder();
            sb.append(uid);
            sb.append("|");
            sb.append(pid);
            String uidAndPid = sb.toString();
            synchronized (ProcStateStatisData.this.mLock) {
                ProcStateStatisData.this.mPssListMap.remove(uidAndPid);
            }
            synchronized (ProcStateStatisData.this.mVssLock) {
                ProcStateStatisData.this.mVssListMap.remove(uidAndPid);
                AwareLog.d(ProcStateStatisData.TAG, "VSS onProcessDied pid = " + pid + ", uid = " + uid);
            }
            synchronized (ProcStateStatisData.this.mProcMemValueMap) {
                ProcStateStatisData.this.mProcMemValueMap.remove(uidAndPid);
            }
        }
    };
    /* access modifiers changed from: private */
    public Map<String, List<ProcStateData>> mPssListMap = new ArrayMap();
    private long[] mTestIntervalTime = {2000, 15000};
    /* access modifiers changed from: private */
    public Map<String, List<ProcStateData>> mVssListMap = new ArrayMap();
    /* access modifiers changed from: private */
    public final Object mVssLock = new Object();
    private String[] stateStatus = {TOP, "background"};

    private static final class HistoryProcInfo {
        static final int FIRST_AVG_TIMES = 50;
        static final int FIRST_MIN_TIMES = 5;
        int mAvgTimes;
        long mAvgUss;
        int mMinTimes = 1;
        long mMinUss;
        String mProcName;

        public HistoryProcInfo(String procName, long uss) {
            this.mProcName = procName;
            this.mAvgUss = uss;
            this.mMinUss = uss;
        }

        public void avgUss(long uss) {
            if (this.mMinUss > uss) {
                this.mMinUss = uss;
                this.mAvgUss = ((this.mAvgUss * ((long) this.mMinTimes)) + uss) / ((long) (this.mMinTimes + 1));
                this.mMinTimes++;
            }
            this.mAvgTimes++;
            if (this.mAvgTimes >= 50 && this.mAvgTimes - 50 >= 5) {
                this.mAvgTimes = 50;
                if (this.mAvgUss >= this.mMinUss * 2) {
                    this.mMinUss += (this.mAvgUss - this.mMinUss) / 5;
                }
            }
        }

        public String getProcName() {
            return this.mProcName;
        }

        public long getUss() {
            if (this.mAvgTimes >= 5) {
                return this.mMinUss;
            }
            return 0;
        }
    }

    private static final class ProcMemValueInfo {
        long mPss;
        long mSwapPss;
        long mUss;

        public ProcMemValueInfo(long pss, long uss, long swapPss) {
            this.mPss = pss;
            this.mUss = uss;
            this.mSwapPss = swapPss;
        }

        public long getPss() {
            return this.mPss;
        }

        public long getUss() {
            return this.mUss;
        }

        public long getSwapPss() {
            return this.mSwapPss;
        }
    }

    private ProcStateStatisData() {
    }

    public static ProcStateStatisData getInstance() {
        ProcStateStatisData procStateStatisData;
        synchronized (ProcStateStatisData.class) {
            if (mProcStateStatisData == null) {
                mProcStateStatisData = new ProcStateStatisData();
            }
            procStateStatisData = mProcStateStatisData;
        }
        return procStateStatisData;
    }

    public void setEnable(boolean enable) {
        AwareLog.d(TAG, "enable=" + enable);
        if (!this.mEnabled.get() && enable) {
            registerProcessObserver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            if (this.mContext != null) {
                this.mContext.registerReceiver(this.br, filter);
            }
        }
        if (this.mEnabled.get() && !enable) {
            unregisterProcessObserver();
            if (this.mContext != null) {
                try {
                    this.mContext.unregisterReceiver(this.br);
                } catch (IllegalArgumentException e) {
                    AwareLog.w(TAG, "Receiver not registered");
                }
            }
        }
        this.mEnabled.set(enable);
    }

    public void updateConfig(int minFgCount, int minBgCount, long fgInterval, long bgInterval) {
        AwareLog.d(TAG, "fgCount=" + minFgCount + ",bgCount" + minBgCount + ",fgInterval=" + fgInterval + ",bgInterval=" + bgInterval);
        this.mCollectCounts[0] = minFgCount;
        this.mCollectCounts[1] = minBgCount;
        this.mIntervalTime[0] = fgInterval;
        this.mIntervalTime[1] = bgInterval;
    }

    public int getMinCount(int procState) {
        if (isValidProcState(procState)) {
            return this.mCollectCounts[procState];
        }
        return 0;
    }

    public long getProcPss(int uid, int pid) {
        String procKey = uid + "|" + pid;
        synchronized (this.mProcMemValueMap) {
            ProcMemValueInfo procMemValueInfo = this.mProcMemValueMap.get(procKey);
            if (procMemValueInfo == null) {
                return 0;
            }
            long pss = procMemValueInfo.getPss();
            return pss;
        }
    }

    public long getProcUss(int uid, int pid) {
        String procKey = uid + "|" + pid;
        synchronized (this.mProcMemValueMap) {
            ProcMemValueInfo procMemValueInfo = this.mProcMemValueMap.get(procKey);
            if (procMemValueInfo == null) {
                return 0;
            }
            long uss = procMemValueInfo.getUss() + (procMemValueInfo.getSwapPss() / 3);
            return uss;
        }
    }

    public long getHistoryProcUss(int uid, String packageName, String procName) {
        String procKey = uid + "|" + packageName;
        synchronized (this.mHistoryMap) {
            HistoryProcInfo historyProcInfo = this.mHistoryMap.get(procKey);
            if (historyProcInfo == null) {
                return 0;
            }
            if (!historyProcInfo.getProcName().equals(procName)) {
                return 0;
            }
            AwareLog.d(TAG, "proc=" + historyProcInfo.getProcName() + ";Uss=" + historyProcInfo.getUss());
            long uss = historyProcInfo.getUss();
            return uss;
        }
    }

    public void addPssToMap(String pkgName, String procName, int uid, int pid, int procState, long pss, long uss, long swapPss, boolean test) {
        Map<String, ProcMemValueInfo> map;
        Object obj;
        long intervalTime;
        StringBuilder sb;
        long j;
        long intervalTime2;
        boolean isExist;
        Iterator<ProcStateData> it;
        String str = procName;
        int i = uid;
        int i2 = pid;
        int i3 = procState;
        long j2 = pss;
        boolean z = test;
        if (!this.mEnabled.get()) {
            AwareLog.d(TAG, "not enabled");
            return;
        }
        long now = SystemClock.uptimeMillis();
        if (str == null) {
            AwareLog.d(TAG, "addPssToMap: procName is null!");
            return;
        }
        AwareLog.d(TAG, "procName=" + str + ";uid=" + i + ";pid=" + i2 + ";procState=" + i3 + ";pss=" + j2 + ";test=" + z);
        if (i3 == 2 || i3 == 11) {
            this.customProcessState = 0;
        } else {
            this.customProcessState = 1;
        }
        if (isValidProcState(this.customProcessState)) {
            String procKey = i + "|" + i2;
            long intervalTime3 = z ? this.mTestIntervalTime[this.customProcessState] : this.mIntervalTime[this.customProcessState];
            Map<String, ProcMemValueInfo> map2 = this.mProcMemValueMap;
            synchronized (map2) {
                try {
                    r1 = r1;
                    long intervalTime4 = intervalTime3;
                    String procKey2 = procKey;
                    map = map2;
                    try {
                        ProcMemValueInfo procMemValueInfo = new ProcMemValueInfo(j2, uss, swapPss);
                        this.mProcMemValueMap.put(procKey2, procMemValueInfo);
                        Object obj2 = this.mLock;
                        synchronized (obj2) {
                            try {
                                if (this.mPssListMap.containsKey(procKey2)) {
                                    try {
                                        AwareLog.d(TAG, "addPssToMap=" + procKey2);
                                        List<ProcStateData> procStateDataList = this.mPssListMap.get(procKey2);
                                        Iterator<ProcStateData> it2 = procStateDataList.iterator();
                                        boolean isExist2 = false;
                                        while (it2.hasNext()) {
                                            try {
                                                try {
                                                    ProcStateData procStateData = it2.next();
                                                    if (procStateData.getState() != this.customProcessState || !str.equals(procStateData.getProcName())) {
                                                        obj = obj2;
                                                        it = it2;
                                                        isExist2 = isExist2;
                                                    } else {
                                                        AwareLog.d(TAG, "processState=" + this.customProcessState);
                                                        boolean isExist3 = isExist2;
                                                        ProcStateData procStateData2 = procStateData;
                                                        obj = obj2;
                                                        it = it2;
                                                        try {
                                                            procStateData.addMemToList(j2, now, intervalTime4, this.mCollectCounts[this.customProcessState]);
                                                            isExist2 = true;
                                                        } catch (Throwable th) {
                                                            th = th;
                                                            boolean z2 = isExist3;
                                                            throw th;
                                                        }
                                                    }
                                                    j2 = pss;
                                                    obj2 = obj;
                                                    it2 = it;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    obj = obj2;
                                                    boolean z3 = isExist2;
                                                    long j3 = intervalTime4;
                                                    throw th;
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                obj = obj2;
                                                long j4 = intervalTime4;
                                                boolean z4 = isExist2;
                                                throw th;
                                            }
                                        }
                                        boolean isExist4 = isExist2;
                                        obj = obj2;
                                        if (!isExist4) {
                                            try {
                                                AwareLog.d(TAG, "processState=" + this.customProcessState + "exist=" + isExist4);
                                                ProcStateData procStateData3 = new ProcStateData(i2, str, this.customProcessState);
                                                isExist = isExist4;
                                                ProcStateData procStateData4 = procStateData3;
                                                try {
                                                    procStateData3.addMemToList(pss, now, intervalTime4, this.mCollectCounts[this.customProcessState]);
                                                    procStateDataList.add(procStateData4);
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    long j5 = intervalTime4;
                                                    boolean z5 = isExist;
                                                }
                                            } catch (Throwable th5) {
                                                th = th5;
                                                long j6 = intervalTime4;
                                                boolean z6 = isExist4;
                                                throw th;
                                            }
                                        } else {
                                            isExist = isExist4;
                                        }
                                        intervalTime = intervalTime4;
                                        boolean z7 = isExist;
                                    } catch (Throwable th6) {
                                        th = th6;
                                        obj = obj2;
                                        throw th;
                                    }
                                } else {
                                    obj = obj2;
                                    try {
                                        sb = new StringBuilder();
                                        sb.append("else addPssToMap=");
                                        sb.append(procKey2);
                                        sb.append(";pss=");
                                        j = pss;
                                        sb.append(j);
                                        sb.append(";interval=");
                                        intervalTime2 = intervalTime4;
                                    } catch (Throwable th7) {
                                        th = th7;
                                        long j7 = intervalTime4;
                                        throw th;
                                    }
                                    try {
                                        sb.append(intervalTime2);
                                        AwareLog.d(TAG, sb.toString());
                                        List<ProcStateData> procStateDataList2 = new ArrayList<>();
                                        ProcStateData procStateData5 = new ProcStateData(i2, str, this.customProcessState);
                                        ProcStateData procStateData6 = procStateData5;
                                        intervalTime = intervalTime2;
                                        try {
                                            procStateData5.addMemToList(j, now, intervalTime2, this.mCollectCounts[this.customProcessState]);
                                            procStateDataList2.add(procStateData6);
                                            this.mPssListMap.put(procKey2, procStateDataList2);
                                        } catch (Throwable th8) {
                                            th = th8;
                                            throw th;
                                        }
                                    } catch (Throwable th9) {
                                        th = th9;
                                        long j8 = intervalTime2;
                                        throw th;
                                    }
                                }
                                String str2 = pkgName;
                                String str3 = str;
                                int i4 = i;
                                addToHistoryMap(str2, str3, i4, uss, swapPss);
                                addVssToMap(str2, str3, i4, i2, procKey2, this.customProcessState, intervalTime);
                            } catch (Throwable th10) {
                                th = th10;
                                obj = obj2;
                                long j72 = intervalTime4;
                                throw th;
                            }
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        long j9 = intervalTime4;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th12) {
                                th = th12;
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th13) {
                    th = th13;
                    long j10 = intervalTime3;
                    String str4 = procKey;
                    map = map2;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v0, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v3, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v6, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v8, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v9, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v10, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v11, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v12, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v13, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v14, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r20v15, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v7, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r4v9 */
    /* JADX WARNING: type inference failed for: r4v11 */
    /* JADX WARNING: type inference failed for: r4v15 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void addVssToMap(String pkgName, String procName, int uid, int pid, String procKey, int customProcessState2, long intervalTime) {
        String str;
        List<ProcStateData> procStateDataList;
        Iterator<ProcStateData> it;
        ? r4;
        String str2 = procName;
        int i = pid;
        String str3 = procKey;
        int i2 = customProcessState2;
        if (this.mDropThreshold > 0) {
            long vss = MemoryCollector.getVSS(pid);
            if (vss > 0) {
                long now = SystemClock.uptimeMillis();
                Object obj = this.mVssLock;
                synchronized (obj) {
                    try {
                        if (this.mVssListMap.containsKey(str3)) {
                            try {
                                AwareLog.d(TAG, "addVssToMap=" + str3);
                                List<ProcStateData> procStateDataList2 = this.mVssListMap.get(str3);
                                Iterator<ProcStateData> it2 = procStateDataList2.iterator();
                                String str4 = null;
                                String str5 = obj;
                                while (it2.hasNext()) {
                                    try {
                                        ProcStateData procStateData = it2.next();
                                        if (procStateData.getState() != i2 || !str2.equals(procStateData.getProcName())) {
                                            procStateDataList = procStateDataList2;
                                            r4 = str4;
                                            it = it2;
                                            str = str5;
                                        } else {
                                            StringBuilder sb = new StringBuilder();
                                            str = str4;
                                            try {
                                                sb.append("processState=");
                                                sb.append(i2);
                                                AwareLog.d(TAG, sb.toString());
                                                ProcStateData procStateData2 = procStateData;
                                                procStateDataList = procStateDataList2;
                                                str3 = str;
                                                it = it2;
                                                str = str5;
                                                procStateData.addMemToList(vss, now, intervalTime, this.mCollectCounts[i2]);
                                                r4 = 1;
                                            } catch (Throwable th) {
                                                th = th;
                                                String str6 = str3;
                                                String str7 = procKey;
                                                throw th;
                                            }
                                        }
                                        str4 = r4;
                                        it2 = it;
                                        str5 = str;
                                        procStateDataList2 = procStateDataList;
                                        str3 = procKey;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        str = str5;
                                        String str8 = str4;
                                        String str9 = procKey;
                                        throw th;
                                    }
                                }
                                List<ProcStateData> procStateDataList3 = procStateDataList2;
                                boolean isExist = str4;
                                str = str5;
                                if (!isExist) {
                                    AwareLog.d(TAG, "processState=" + i2 + "exist=" + isExist);
                                    if (vss < this.mDropThreshold) {
                                        AwareLog.d(TAG, "Not over DropThreshold, vss=" + vss);
                                    } else {
                                        ProcStateData procStateData3 = new ProcStateData(i, str2, i2, false);
                                        procStateData3.addMemToList(vss, now, intervalTime, this.mCollectCounts[i2]);
                                        procStateDataList3.add(procStateData3);
                                    }
                                }
                                boolean z = isExist;
                            } catch (Throwable th3) {
                                th = th3;
                                str = obj;
                                String str10 = str3;
                                throw th;
                            }
                        } else {
                            str = obj;
                            try {
                                if (vss < this.mDropThreshold) {
                                    AwareLog.d(TAG, "Not over DropThreshold, vss=" + vss);
                                } else {
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append("else addVssToMap=");
                                    String str11 = procKey;
                                    try {
                                        sb2.append(str11);
                                        sb2.append(";vss=");
                                        sb2.append(vss);
                                        sb2.append(";interval=");
                                        sb2.append(intervalTime);
                                        AwareLog.d(TAG, sb2.toString());
                                        List<ProcStateData> procStateDataList4 = new ArrayList<>();
                                        ProcStateData procStateData4 = new ProcStateData(i, str2, i2, false);
                                        String str12 = str11;
                                        procStateData4.addMemToList(vss, now, intervalTime, this.mCollectCounts[i2]);
                                        procStateDataList4.add(procStateData4);
                                        this.mVssListMap.put(str12, procStateDataList4);
                                    } catch (Throwable th4) {
                                        th = th4;
                                        throw th;
                                    }
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                String str72 = procKey;
                                throw th;
                            }
                        }
                        String str13 = procKey;
                    } catch (Throwable th6) {
                        th = th6;
                        String str14 = str3;
                        str = obj;
                        throw th;
                    }
                }
            } else {
                String str15 = str3;
            }
        }
    }

    public Map<String, List<ProcStateData>> getVssListMap() {
        AwareLog.d(TAG, "enter getPssListMap...");
        Map<String, List<ProcStateData>> cloneMap = new ArrayMap<>();
        synchronized (this.mVssLock) {
            cloneMap.putAll(this.mVssListMap);
        }
        return cloneMap;
    }

    public void updateDropThreshold(long value) {
        if (value > 0) {
            this.mDropThreshold = value;
        }
    }

    private void addToHistoryMap(String pkgName, String procName, int uid, long uss, long swapPss) {
        if (pkgName != null && procName != null && this.customProcessState == 0 && !procName.contains(":")) {
            synchronized (this.mHistoryMap) {
                String historyMapKey = uid + "|" + pkgName;
                HistoryProcInfo historyProcInfo = this.mHistoryMap.get(historyMapKey);
                long realUss = uss + swapPss;
                if (historyProcInfo == null) {
                    AwareLog.d(TAG, "historyMap procName=" + procName + ";uss=" + uss);
                    this.mHistoryMap.put(historyMapKey, new HistoryProcInfo(procName, realUss));
                } else {
                    historyProcInfo.avgUss(realUss);
                }
            }
        }
    }

    public Map<String, List<ProcStateData>> getPssListMap() {
        AwareLog.d(TAG, "enter getPssListMap...");
        Map<String, List<ProcStateData>> cloneMap = new ArrayMap<>();
        synchronized (this.mLock) {
            cloneMap.putAll(this.mPssListMap);
        }
        return cloneMap;
    }

    public boolean isValidProcState(int procState) {
        return procState >= 0 && procState < this.stateStatus.length;
    }

    public boolean isForgroundState(int procState) {
        return procState == 0;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    private void registerProcessObserver() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.w(TAG, "register process observer failed");
        }
    }

    private void unregisterProcessObserver() {
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
            synchronized (this.mLock) {
                this.mPssListMap.clear();
            }
            synchronized (this.mVssLock) {
                this.mVssListMap.clear();
            }
        } catch (RemoteException e) {
            AwareLog.w(TAG, "unregister process observer failed");
        }
    }
}
