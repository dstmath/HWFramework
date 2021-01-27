package com.android.server.rms.memrepair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.rms.iaware.AwareCallback;
import com.huawei.android.app.IProcessObserverEx;
import java.util.ArrayList;
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
    private static ProcStateStatisData sProcStateStatisData;
    private int[] mCollectCounts = {6, 6};
    private Context mContext;
    private int mCustomProcessState = 21;
    private long mDropThreshold = 0;
    private AtomicBoolean mEnabled = new AtomicBoolean(false);
    private final Map<String, HistoryProcInfo> mHistoryMap = new ArrayMap();
    private long[] mIntervalTime = {120000, 900000};
    private final Object mLock = new Object();
    private final Map<String, ProcMemValueInfo> mProcMemValueMap = new ArrayMap();
    private IProcessObserverEx mProcessObserver = new IProcessObserverEx() {
        /* class com.android.server.rms.memrepair.ProcStateStatisData.AnonymousClass2 */

        public void onForegroundActivitiesChanged(int pid, int uid, boolean isFgActivities) {
        }

        public void onProcessDied(int pid, int uid) {
            AwareLog.d(ProcStateStatisData.TAG, "onProcessDied pid = " + pid + ", uid = " + uid);
            StringBuilder sb = new StringBuilder();
            sb.append(uid);
            sb.append(ProcStateStatisData.SEPERATOR_CHAR);
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
    private Map<String, List<ProcStateData>> mPssListMap = new ArrayMap();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.rms.memrepair.ProcStateStatisData.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            AwareLog.d(ProcStateStatisData.TAG, "onReceive...");
            if (intent != null && "android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                Uri data = intent.getData();
                if (intent.getData() != null) {
                    String packageName = data.getSchemeSpecificPart();
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    AwareLog.d(ProcStateStatisData.TAG, "packageName = " + packageName + ", uid = " + uid);
                    synchronized (ProcStateStatisData.this.mHistoryMap) {
                        ProcStateStatisData.this.mHistoryMap.remove(uid + ProcStateStatisData.SEPERATOR_CHAR + packageName);
                    }
                }
            }
        }
    };
    private String[] mStateStatus = {TOP, "background"};
    private long[] mTestIntervalTime = {2000, 15000};
    private Map<String, List<ProcStateData>> mVssListMap = new ArrayMap();
    private final Object mVssLock = new Object();

    public static ProcStateStatisData getInstance() {
        ProcStateStatisData procStateStatisData;
        synchronized (ProcStateStatisData.class) {
            if (sProcStateStatisData == null) {
                sProcStateStatisData = new ProcStateStatisData();
            }
            procStateStatisData = sProcStateStatisData;
        }
        return procStateStatisData;
    }

    public void setEnable(boolean enable) {
        AwareLog.d(TAG, "enable = " + enable);
        if (!this.mEnabled.get() && enable) {
            registerProcessObserver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            Context context = this.mContext;
            if (context != null) {
                context.registerReceiver(this.mReceiver, filter);
            }
        }
        if (this.mEnabled.get() && !enable) {
            unregisterProcessObserver();
            Context context2 = this.mContext;
            if (context2 != null) {
                try {
                    context2.unregisterReceiver(this.mReceiver);
                } catch (IllegalArgumentException e) {
                    AwareLog.w(TAG, "Receiver not registered");
                }
            }
        }
        this.mEnabled.set(enable);
    }

    public void updateConfig(int minFgCount, int minBgCount, long fgInterval, long bgInterval) {
        AwareLog.d(TAG, "fgCount = " + minFgCount + ", bgCount = " + minBgCount + ", fgInterval = " + fgInterval + ", bgInterval = " + bgInterval);
        int[] iArr = this.mCollectCounts;
        iArr[0] = minFgCount;
        iArr[1] = minBgCount;
        long[] jArr = this.mIntervalTime;
        jArr[0] = fgInterval;
        jArr[1] = bgInterval;
    }

    public int getMinCount(int procState) {
        if (isValidProcState(procState)) {
            return this.mCollectCounts[procState];
        }
        return 0;
    }

    public long getProcPss(int uid, int pid) {
        String procKey = uid + SEPERATOR_CHAR + pid;
        synchronized (this.mProcMemValueMap) {
            ProcMemValueInfo procMemValueInfo = this.mProcMemValueMap.get(procKey);
            if (procMemValueInfo == null) {
                return 0;
            }
            return procMemValueInfo.getPss();
        }
    }

    public long getProcUss(int uid, int pid) {
        String procKey = uid + SEPERATOR_CHAR + pid;
        synchronized (this.mProcMemValueMap) {
            ProcMemValueInfo procMemValueInfo = this.mProcMemValueMap.get(procKey);
            if (procMemValueInfo == null) {
                return 0;
            }
            return procMemValueInfo.getUss() + (procMemValueInfo.getSwapPss() / 3);
        }
    }

    public long getHistoryProcUss(int uid, String packageName, String procName) {
        String procKey = uid + SEPERATOR_CHAR + packageName;
        synchronized (this.mHistoryMap) {
            HistoryProcInfo historyProcInfo = this.mHistoryMap.get(procKey);
            if (historyProcInfo == null) {
                return 0;
            }
            if (!historyProcInfo.getProcName().equals(procName)) {
                return 0;
            }
            AwareLog.d(TAG, "proc = " + historyProcInfo.getProcName() + "; Uss = " + historyProcInfo.getUss());
            return historyProcInfo.getUss();
        }
    }

    private void addCookedDataToMap(String procName, int pid, long pss, String procKey, long intervalTime) {
        Object obj;
        Throwable th;
        boolean isExist;
        long now = SystemClock.uptimeMillis();
        boolean isExist2 = false;
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                if (this.mPssListMap.containsKey(procKey)) {
                    AwareLog.d(TAG, "addCookedDataToMap: procKey = " + procKey);
                    List<ProcStateData> procStateDataList = this.mPssListMap.get(procKey);
                    for (ProcStateData procStateData : procStateDataList) {
                        try {
                            if (procStateData.getState() != this.mCustomProcessState) {
                                obj = obj2;
                            } else if (procName.equals(procStateData.getProcName())) {
                                AwareLog.d(TAG, "processState = " + this.mCustomProcessState);
                                obj = obj2;
                                try {
                                    procStateData.addMemToList(pss, now, intervalTime, this.mCollectCounts[this.mCustomProcessState]);
                                    isExist2 = true;
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            } else {
                                obj = obj2;
                            }
                            obj2 = obj;
                        } catch (Throwable th3) {
                            th = th3;
                            obj = obj2;
                            throw th;
                        }
                    }
                    obj = obj2;
                    if (!isExist2) {
                        try {
                            AwareLog.d(TAG, "processState = " + this.mCustomProcessState + "exist = " + isExist2);
                            ProcStateData procStateData2 = new ProcStateData(pid, procName, this.mCustomProcessState);
                            isExist = isExist2;
                            try {
                                procStateData2.addMemToList(pss, now, intervalTime, this.mCollectCounts[this.mCustomProcessState]);
                                procStateDataList.add(procStateData2);
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            throw th;
                        }
                    } else {
                        isExist = isExist2;
                    }
                } else {
                    obj = obj2;
                    AwareLog.d(TAG, "else procKey = " + procKey + "; pss = " + pss + "; interval = " + intervalTime);
                    List<ProcStateData> procStateDataList2 = new ArrayList<>();
                    ProcStateData procStateData3 = new ProcStateData(pid, procName, this.mCustomProcessState);
                    procStateData3.addMemToList(pss, now, intervalTime, this.mCollectCounts[this.mCustomProcessState]);
                    procStateDataList2.add(procStateData3);
                    this.mPssListMap.put(procKey, procStateDataList2);
                }
            } catch (Throwable th6) {
                th = th6;
                obj = obj2;
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0103, code lost:
        r0 = th;
     */
    public void addPssToMap(ProcAttribute procAttribute, long pss, long uss, long swapPss, boolean tested) {
        if (!this.mEnabled.get()) {
            AwareLog.d(TAG, "not enabled");
            return;
        }
        String pkgName = procAttribute.getPkgName();
        String procName = procAttribute.getProcName();
        int pid = procAttribute.getPid();
        int uid = procAttribute.getUid();
        int procState = procAttribute.getProcState();
        if (pkgName == null || procName == null) {
            AwareLog.w(TAG, "addPssToMap: pkgName or procName is null!");
            return;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "procName = " + procName + "; uid = " + uid + "; pid = " + pid + "; procState = " + procState + "; pss = " + pss + "; test = " + tested);
        }
        if (procState == 2 || procState == 13) {
            this.mCustomProcessState = 0;
        } else {
            this.mCustomProcessState = 1;
        }
        if (isValidProcState(this.mCustomProcessState)) {
            String procKey = uid + SEPERATOR_CHAR + pid;
            long intervalTime = tested ? this.mTestIntervalTime[this.mCustomProcessState] : this.mIntervalTime[this.mCustomProcessState];
            synchronized (this.mProcMemValueMap) {
                this.mProcMemValueMap.put(procKey, new ProcMemValueInfo(pss, uss, swapPss));
            }
            addCookedDataToMap(procName, pid, pss, procKey, intervalTime);
            addToHistoryMap(pkgName, procName, uid, uss, swapPss);
            addVssToMap(procAttribute, this.mCustomProcessState, intervalTime);
            return;
        }
        return;
        while (true) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d1  */
    private void addVssToMap(ProcAttribute procAttribute, int customProcessState, long intervalTime) {
        Object obj;
        String procKey;
        boolean isProcNameInList;
        int uid;
        int pid;
        String procKey2;
        String procName;
        int pid2 = procAttribute.getPid();
        long vss = MemoryCollector.getVss(pid2);
        if (!(vss <= 0 || this.mDropThreshold <= 0)) {
            int uid2 = procAttribute.getUid();
            String procKey3 = uid2 + SEPERATOR_CHAR + pid2;
            String procName2 = procAttribute.getProcName();
            long now = SystemClock.uptimeMillis();
            Object obj2 = this.mVssLock;
            synchronized (obj2) {
                try {
                    if (this.mVssListMap.containsKey(procKey3)) {
                        try {
                            AwareLog.d(TAG, "mVssListMap contains procKey: " + procKey3);
                            List<ProcStateData> procStateDataList = this.mVssListMap.get(procKey3);
                            boolean isExist = false;
                            for (ProcStateData procStateData : procStateDataList) {
                                try {
                                    try {
                                        if (procStateData.getState() == customProcessState) {
                                            try {
                                                if (procName2.equals(procStateData.getProcName())) {
                                                    isProcNameInList = true;
                                                    if (!isProcNameInList) {
                                                        AwareLog.d(TAG, "processState = " + customProcessState);
                                                        obj = obj2;
                                                        pid = pid2;
                                                        procKey2 = procKey3;
                                                        procName = procName2;
                                                        uid = uid2;
                                                        try {
                                                            procStateData.addMemToList(vss, now, intervalTime, this.mCollectCounts[customProcessState]);
                                                            isExist = true;
                                                        } catch (Throwable th) {
                                                            th = th;
                                                            throw th;
                                                        }
                                                    } else {
                                                        pid = pid2;
                                                        obj = obj2;
                                                        procKey2 = procKey3;
                                                        procName = procName2;
                                                        uid = uid2;
                                                    }
                                                    procName2 = procName;
                                                    obj2 = obj;
                                                    procKey3 = procKey2;
                                                    pid2 = pid;
                                                    uid2 = uid;
                                                }
                                            } catch (Throwable th2) {
                                                th = th2;
                                                obj = obj2;
                                                throw th;
                                            }
                                        }
                                        isProcNameInList = false;
                                        if (!isProcNameInList) {
                                        }
                                        procName2 = procName;
                                        obj2 = obj;
                                        procKey3 = procKey2;
                                        pid2 = pid;
                                        uid2 = uid;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        obj = obj2;
                                        throw th;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    obj = obj2;
                                    throw th;
                                }
                            }
                            obj = obj2;
                            if (!isExist) {
                                try {
                                    AwareLog.d(TAG, "processState = " + customProcessState + ", exist = " + isExist);
                                    if (vss < this.mDropThreshold) {
                                        AwareLog.d(TAG, "Not over DropThreshold, vss = " + vss);
                                        return;
                                    }
                                    try {
                                        ProcStateData procStateData2 = new ProcStateData(pid2, procName2, customProcessState, false);
                                        procStateData2.addMemToList(vss, now, intervalTime, this.mCollectCounts[customProcessState]);
                                        procStateDataList.add(procStateData2);
                                    } catch (Throwable th5) {
                                        th = th5;
                                        throw th;
                                    }
                                } catch (Throwable th6) {
                                    th = th6;
                                    throw th;
                                }
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            obj = obj2;
                            throw th;
                        }
                    } else {
                        obj = obj2;
                        try {
                            if (vss < this.mDropThreshold) {
                                try {
                                    AwareLog.d(TAG, "Not over DropThreshold, vss = " + vss);
                                } catch (Throwable th8) {
                                    th = th8;
                                    throw th;
                                }
                            } else {
                                if (AwareLog.getDebugLogSwitch()) {
                                    try {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("else addVssToMap = ");
                                        procKey = procKey3;
                                        try {
                                            sb.append(procKey);
                                            sb.append("; vss = ");
                                            sb.append(vss);
                                            sb.append("; interval = ");
                                            sb.append(intervalTime);
                                            AwareLog.d(TAG, sb.toString());
                                        } catch (Throwable th9) {
                                            th = th9;
                                            throw th;
                                        }
                                    } catch (Throwable th10) {
                                        th = th10;
                                        throw th;
                                    }
                                } else {
                                    procKey = procKey3;
                                }
                                List<ProcStateData> procStateDataLists = new ArrayList<>();
                                ProcStateData procStateData3 = new ProcStateData(pid2, procName2, customProcessState, false);
                                procStateData3.addMemToList(vss, now, intervalTime, this.mCollectCounts[customProcessState]);
                                procStateDataLists.add(procStateData3);
                                this.mVssListMap.put(procKey, procStateDataLists);
                            }
                        } catch (Throwable th11) {
                            th = th11;
                            throw th;
                        }
                    }
                } catch (Throwable th12) {
                    th = th12;
                    obj = obj2;
                    throw th;
                }
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
        if (this.mCustomProcessState == 0 && !procName.contains(":")) {
            synchronized (this.mHistoryMap) {
                String historyMapKey = uid + SEPERATOR_CHAR + pkgName;
                HistoryProcInfo historyProcInfo = this.mHistoryMap.get(historyMapKey);
                long realUss = uss + swapPss;
                if (historyProcInfo == null) {
                    AwareLog.d(TAG, "historyMap procName = " + procName + "; uss = " + uss);
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
        return procState >= 0 && procState < this.mStateStatus.length;
    }

    public boolean isForgroundState(int procState) {
        return procState == 0;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    private void registerProcessObserver() {
        AwareCallback.getInstance().registerProcessObserver(this.mProcessObserver);
    }

    private void unregisterProcessObserver() {
        AwareCallback.getInstance().unregisterProcessObserver(this.mProcessObserver);
        synchronized (this.mLock) {
            this.mPssListMap.clear();
        }
        synchronized (this.mVssLock) {
            this.mVssListMap.clear();
        }
    }

    /* access modifiers changed from: private */
    public static final class HistoryProcInfo {
        static final int FIRST_AVG_TIMES = 50;
        static final int FIRST_MIN_TIMES = 5;
        int mAvgTimes = 0;
        long mAvgUss = 0;
        int mMinTimes = 0;
        long mMinUss = 0;
        String mProcName = "";

        HistoryProcInfo(String procName, long uss) {
            this.mProcName = procName;
            this.mMinUss = uss;
            this.mAvgUss = uss;
            this.mMinTimes = 1;
        }

        public void avgUss(long uss) {
            if (this.mMinUss > uss) {
                this.mMinUss = uss;
                long j = this.mAvgUss;
                int i = this.mMinTimes;
                this.mAvgUss = ((j * ((long) i)) + uss) / ((long) (i + 1));
                this.mMinTimes = i + 1;
            }
            this.mAvgTimes++;
            int i2 = this.mAvgTimes;
            if (i2 >= 50 && i2 - 50 >= 5) {
                this.mAvgTimes = 50;
                long j2 = this.mAvgUss;
                long j3 = this.mMinUss;
                if (j2 >= 2 * j3) {
                    this.mMinUss = j3 + ((j2 - j3) / 5);
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

    /* access modifiers changed from: private */
    public static final class ProcMemValueInfo {
        long mPss = 0;
        long mSwapPss = 0;
        long mUss = 0;

        ProcMemValueInfo(long pss, long uss, long swapPss) {
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

    public static class ProcAttribute {
        private int mPid = 0;
        private String mPkgName = "";
        private String mProcName = "";
        private int mProcState = 0;
        private int mUid = 0;

        public ProcAttribute(String pkgName, String procName, int uid, int pid, int procState) {
            this.mPkgName = pkgName;
            this.mProcName = procName;
            this.mUid = uid;
            this.mPid = pid;
            this.mProcState = procState;
        }

        public String getPkgName() {
            return this.mPkgName;
        }

        public String getProcName() {
            return this.mProcName;
        }

        public int getUid() {
            return this.mUid;
        }

        public int getPid() {
            return this.mPid;
        }

        public int getProcState() {
            return this.mProcState;
        }
    }
}
