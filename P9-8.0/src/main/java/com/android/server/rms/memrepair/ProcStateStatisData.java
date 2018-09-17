package com.android.server.rms.memrepair;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private int customProcessState = 18;
    private int[] mCollectCounts = new int[]{6, 6};
    private boolean mEnabled = false;
    private long[] mIntervalTime = new long[]{120000, 900000};
    private final Object mLock = new Object();
    private IProcessObserver mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }

        public void onProcessDied(int pid, int uid) {
            AwareLog.d(ProcStateStatisData.TAG, "onProcessDied pid = " + pid + ", uid = " + uid);
            String uidAndPid = uid + ProcStateStatisData.SEPERATOR_CHAR + pid;
            synchronized (ProcStateStatisData.this.mLock) {
                ProcStateStatisData.this.mPssListMap.remove(uidAndPid);
            }
        }
    };
    private Map<String, List<ProcStateData>> mPssListMap = new ArrayMap();
    private long[] mTestIntervalTime = new long[]{2000, 15000};
    private String[] stateStatus = new String[]{TOP, "background"};

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
        this.mEnabled = enable;
        if (enable) {
            registerProcessObserver();
        } else {
            unregisterProcessObserver();
        }
    }

    public void updateConfig(int minFgCount, int minBgCount, long fgInterval, long bgInterval) {
        AwareLog.d(TAG, "fgCount=" + minFgCount + ",bgCount" + minBgCount + ",fgInterval=" + fgInterval + ",bgInterval=" + bgInterval);
        this.mCollectCounts[0] = minFgCount;
        this.mCollectCounts[1] = minBgCount;
        this.mIntervalTime[0] = fgInterval;
        this.mIntervalTime[1] = bgInterval;
    }

    public int getMinCount(int procState) {
        return isValidProcState(procState) ? this.mCollectCounts[procState] : 0;
    }

    public void addPssToMap(String procName, int uid, int pid, int procState, long pss, long now, boolean test) {
        if (!this.mEnabled) {
            AwareLog.d(TAG, "not enabled");
        } else if (procName == null) {
            AwareLog.d(TAG, "addPssToMap: procName is null!");
        } else {
            AwareLog.d(TAG, "procName=" + procName + ";uid=" + uid + ";pid=" + pid + ";procState=" + procState + ";pss=" + pss + ";test=" + test);
            if (procState == 2 || procState == 5) {
                this.customProcessState = 0;
            } else {
                this.customProcessState = 1;
            }
            if (isValidProcState(this.customProcessState)) {
                String procKey = uid + SEPERATOR_CHAR + pid;
                long intervalTime = test ? this.mTestIntervalTime[this.customProcessState] : this.mIntervalTime[this.customProcessState];
                synchronized (this.mLock) {
                    List<ProcStateData> procStateDataList;
                    ProcStateData procStateData;
                    if (this.mPssListMap.containsKey(procKey)) {
                        AwareLog.d(TAG, "addPssToMap=" + procKey);
                        procStateDataList = (List) this.mPssListMap.get(procKey);
                        boolean isExist = false;
                        for (ProcStateData procStateData2 : procStateDataList) {
                            if (procStateData2.getState() == this.customProcessState) {
                                if (procName.equals(procStateData2.getProcName())) {
                                    AwareLog.d(TAG, "processState=" + this.customProcessState);
                                    procStateData2.addPssToList(pss, now, intervalTime, this.mCollectCounts[this.customProcessState]);
                                    isExist = true;
                                }
                            }
                        }
                        if (!isExist) {
                            AwareLog.d(TAG, "processState=" + this.customProcessState + "exist=" + isExist);
                            procStateData2 = new ProcStateData(pid, procName, this.customProcessState);
                            procStateData2.addPssToList(pss, now, intervalTime, this.mCollectCounts[this.customProcessState]);
                            procStateDataList.add(procStateData2);
                        }
                    } else {
                        AwareLog.d(TAG, "else addPssToMap=" + procKey + ";pss=" + pss + ";interval=" + intervalTime);
                        procStateDataList = new ArrayList();
                        procStateData2 = new ProcStateData(pid, procName, this.customProcessState);
                        procStateData2.addPssToList(pss, now, intervalTime, this.mCollectCounts[this.customProcessState]);
                        procStateDataList.add(procStateData2);
                        this.mPssListMap.put(procKey, procStateDataList);
                    }
                }
            }
        }
    }

    public Map<String, List<ProcStateData>> getPssListMap() {
        AwareLog.d(TAG, "enter getPssListMap...");
        Map<String, List<ProcStateData>> cloneMap = new ArrayMap();
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
        } catch (RemoteException e) {
            AwareLog.w(TAG, "unregister process observer failed");
        }
    }
}
