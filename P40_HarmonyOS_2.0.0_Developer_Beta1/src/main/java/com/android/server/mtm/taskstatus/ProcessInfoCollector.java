package com.android.server.mtm.taskstatus;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wm.HwStartWindowRecord;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProcessInfoCollector {
    private static final int CAPACITY = 10;
    private static final Object LOCK = new Object();
    private static final int MAX_TASKS = 100;
    private static final String TAG = "ProcessInfoCollector";
    private static ProcessInfoCollector sProcessInfoCollector = null;
    private boolean isOpenDebugLog = false;
    boolean isOpenInfoLog = false;
    private AwareBroadcastPolicy mAwareBrPolicy = null;
    private final HashMap<Integer, AwareProcessInfo> mAwareProcMap = new HashMap<>();
    private ProcessInfo mCacheInfo = new ProcessInfo(0, 0);
    private HwActivityManagerService mHwAms = null;
    private int mIndex = 0;
    private ProcessInfo[] mKilledProcList = new ProcessInfo[10];
    private final Object mLock = new Object();
    private final HashMap<Integer, ProcessInfo> mProcMap = new HashMap<>();

    private ProcessInfoCollector() {
        init();
    }

    public static ProcessInfoCollector getInstance() {
        ProcessInfoCollector processInfoCollector;
        synchronized (LOCK) {
            if (sProcessInfoCollector == null) {
                sProcessInfoCollector = new ProcessInfoCollector();
            }
            processInfoCollector = sProcessInfoCollector;
        }
        return processInfoCollector;
    }

    private void init() {
        this.mHwAms = HwActivityManagerService.self();
        if (this.mHwAms == null) {
            SlogEx.e(TAG, "init failed to get HwAMS handler");
        }
    }

    public void recordKilledProcess(ProcessInfo info) {
        synchronized (this.mLock) {
            info.mKilledTime = SystemClock.uptimeMillis();
            if (this.mIndex >= 10) {
                this.mIndex = 0;
            }
            this.mKilledProcList[this.mIndex] = info;
            this.mIndex++;
        }
    }

    public void removeKilledProcess(int pid) {
        ProcessInfo info;
        if (pid < 0) {
            SlogEx.e(TAG, "removeKilledProcess: proc should not less than zero.");
        }
        synchronized (this.mProcMap) {
            info = this.mProcMap.remove(Integer.valueOf(pid));
            this.mAwareProcMap.remove(Integer.valueOf(pid));
        }
        if (info != null && info.mProcessName != null && info.mPackageName != null && !info.mPackageName.isEmpty() && info.mProcessName.equals(info.mPackageName.get(0))) {
            HwStartWindowRecord.getInstance().removeStartWindowApp(Integer.valueOf(info.mAppUid));
        }
    }

    public ProcessInfo getProcessInfo(int pid) {
        ProcessInfo resInfo = null;
        if (pid < 0) {
            SlogEx.e(TAG, "getProcessInfo: proc should not less than zero.");
            return null;
        }
        synchronized (this.mProcMap) {
            ProcessInfo info = this.mProcMap.get(Integer.valueOf(pid));
            if (info == null) {
                SlogEx.e(TAG, "getProcessInfo: failed to find this proc.");
            } else {
                resInfo = new ProcessInfo(0, 0);
                ProcessInfo.copyProcessInfo(info, resInfo);
            }
        }
        return resInfo;
    }

    public AwareProcessInfo getAwareProcessInfo(int pid) {
        AwareProcessInfo info;
        if (pid < 0) {
            SlogEx.e(TAG, "getProcessInfo: proc should not less than zero.");
            return null;
        }
        synchronized (this.mProcMap) {
            info = this.mAwareProcMap.get(Integer.valueOf(pid));
        }
        return info;
    }

    public ArrayList<ProcessInfo> getProcessInfosFromPackage(String packageName, int userId) {
        ArrayList<ProcessInfo> procList = new ArrayList<>();
        if (userId < 0 || packageName == null) {
            return procList;
        }
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, ProcessInfo> entry : this.mProcMap.entrySet()) {
                ProcessInfo info = entry.getValue();
                if (info != null) {
                    ArrayList<String> packageNames = info.mPackageName;
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            if (packageName.equals(packageNames.get(0))) {
                                if (userId == UserHandleEx.getUserId(info.mUid)) {
                                    ProcessInfo copyInfo = new ProcessInfo(0, 0);
                                    ProcessInfo.copyProcessInfo(info, copyInfo);
                                    procList.add(copyInfo);
                                }
                            }
                        }
                    }
                }
            }
        }
        return procList;
    }

    public ArrayList<AwareProcessInfo> getAwareProcessInfosFromPackage(String packageName, int userId) {
        ArrayList<AwareProcessInfo> procList = new ArrayList<>();
        if (packageName == null) {
            return procList;
        }
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, AwareProcessInfo> entry : this.mAwareProcMap.entrySet()) {
                AwareProcessInfo info = entry.getValue();
                if (info != null) {
                    ArrayList<String> packageNames = info.procProcInfo.mPackageName;
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            if (packageName.equals(packageNames.get(0))) {
                                if (userId == -1 || userId == UserHandleEx.getUserId(info.procProcInfo.mUid)) {
                                    procList.add(info);
                                }
                            }
                        }
                    }
                }
            }
        }
        return procList;
    }

    public ArrayList<ProcessInfo> getProcessInfosFromPackageMap(ArrayMap<String, Integer> packMap) {
        ArrayList<ProcessInfo> procList = new ArrayList<>();
        if (packMap == null) {
            return procList;
        }
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, ProcessInfo> entry : this.mProcMap.entrySet()) {
                ProcessInfo info = entry.getValue();
                if (info != null) {
                    ArrayList<String> packageNames = info.mPackageName;
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            Integer pid = packMap.get(packageNames.get(0));
                            if (pid != null) {
                                if (pid.intValue() == UserHandleEx.getUserId(info.mUid)) {
                                    ProcessInfo copyInfo = new ProcessInfo(0, 0);
                                    ProcessInfo.copyProcessInfo(info, copyInfo);
                                    procList.add(copyInfo);
                                }
                            }
                        }
                    }
                }
            }
        }
        return procList;
    }

    public void dumpPackageTask(ArrayList<ProcessInfo> processInfos, PrintWriter pw) {
        if (pw == null) {
            SlogEx.e(TAG, "dump PrintWriter pw is null");
        } else if (processInfos != null && !processInfos.isEmpty()) {
            pw.println("  Package/Task Processes Information dump: ");
            Iterator<ProcessInfo> it = processInfos.iterator();
            while (it.hasNext()) {
                pw.println(System.lineSeparator() + "  Running Process information: ");
                printProcInfo(it.next(), pw);
            }
        }
    }

    public ArrayList<ProcessInfo> getProcessInfosFromTask(int taskId, int userId) {
        HwActivityManagerService hwActivityManagerService;
        ArrayList<ProcessInfo> resProcList = new ArrayList<>();
        if (userId < 0 || taskId < 0 || (hwActivityManagerService = this.mHwAms) == null) {
            return resProcList;
        }
        String packageName = null;
        Iterator<ActivityManager.RecentTaskInfo> iter = hwActivityManagerService.getRecentTasksList(100, 2, userId).iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            ActivityManager.RecentTaskInfo taskInfo = iter.next();
            if (taskInfo.persistentId == taskId) {
                Intent intent = new Intent(taskInfo.baseIntent);
                if (taskInfo.origActivity != null) {
                    intent.setComponent(taskInfo.origActivity);
                }
                if (intent.getComponent() != null) {
                    packageName = intent.getComponent().getPackageName();
                }
            }
        }
        if (packageName != null) {
            return getProcessInfosFromPackage(packageName, userId);
        }
        return resProcList;
    }

    public SparseSet getPidsFromUid(int uid, int userId) {
        SparseSet pids = new SparseSet();
        if (userId < 0 || uid < 0) {
            return pids;
        }
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, ProcessInfo> entry : this.mProcMap.entrySet()) {
                ProcessInfo info = entry.getValue();
                if (info != null) {
                    if (info.mAppUid == uid || info.mUid == uid) {
                        if (userId == UserHandleEx.getUserId(info.mUid)) {
                            pids.add(info.mPid);
                        }
                    }
                }
            }
        }
        return pids;
    }

    public ArrayList<ProcessInfo> getProcessInfoList() {
        ArrayList<ProcessInfo> procList;
        synchronized (this.mProcMap) {
            procList = new ArrayList<>(this.mProcMap.size());
            for (Map.Entry<Integer, ProcessInfo> entry : this.mProcMap.entrySet()) {
                ProcessInfo copyInfo = new ProcessInfo(0, 0);
                ProcessInfo.copyProcessInfo(entry.getValue(), copyInfo);
                procList.add(copyInfo);
            }
        }
        return procList;
    }

    public ArrayList<AwareProcessInfo> getAwareProcessInfoList() {
        ArrayList<AwareProcessInfo> procList;
        synchronized (this.mProcMap) {
            procList = new ArrayList<>(this.mAwareProcMap.size());
            for (Map.Entry<Integer, AwareProcessInfo> entry : this.mAwareProcMap.entrySet()) {
                procList.add(entry.getValue());
            }
        }
        return procList;
    }

    public void recordProcessInfo(int pid, int uid) {
        ProcessInfo curProcessInfo;
        boolean exist = false;
        this.mCacheInfo.initialProcessInfo(pid, uid);
        if (HwActivityManager.getProcessRecordFromMTM(this.mCacheInfo)) {
            synchronized (this.mProcMap) {
                curProcessInfo = this.mProcMap.get(Integer.valueOf(pid));
                if (curProcessInfo == null) {
                    curProcessInfo = new ProcessInfo(pid, uid);
                    this.mCacheInfo.mCreatedTime = SystemClock.elapsedRealtime();
                } else {
                    this.mCacheInfo.mCreatedTime = curProcessInfo.mCreatedTime;
                    exist = true;
                }
                if (!ProcessInfo.copyProcessInfo(this.mCacheInfo, curProcessInfo)) {
                    SlogEx.e(TAG, "recordProcessInfo: source or target object is null.");
                    return;
                }
                curProcessInfo.mCount++;
                if (!exist) {
                    this.mProcMap.put(Integer.valueOf(pid), curProcessInfo);
                    AwareProcessInfo awareProcessInfo = new AwareProcessInfo(pid, curProcessInfo);
                    this.mAwareProcMap.put(Integer.valueOf(pid), awareProcessInfo);
                    if (getAwareBrPolicy() != null) {
                        this.mAwareBrPolicy.updateProcessBrPolicy(awareProcessInfo, -1);
                    }
                }
            }
            if (!exist) {
                reportToRms(curProcessInfo);
            }
        } else if (this.isOpenDebugLog) {
            SlogEx.e(TAG, "recordProcessInfo  failed to get process record");
        }
    }

    public void enableDebug() {
        this.isOpenDebugLog = true;
        this.isOpenInfoLog = true;
    }

    public void disableDebug() {
        this.isOpenDebugLog = false;
        this.isOpenInfoLog = false;
    }

    public void dump(PrintWriter pw) {
        if (pw == null) {
            SlogEx.e(TAG, "dump PrintWriter pw is null");
            return;
        }
        pw.println("  Process Information Collector dump :");
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, ProcessInfo> entry : this.mProcMap.entrySet()) {
                pw.println(System.lineSeparator() + "  Running Process information :");
                printProcInfo(entry.getValue(), pw);
            }
        }
        synchronized (this.mLock) {
            for (int i = 0; i < this.mIndex; i++) {
                ProcessInfo info = this.mKilledProcList[i];
                pw.println(System.lineSeparator() + "  Killed Process information " + i + ":");
                printProcInfo(info, pw);
            }
        }
    }

    private void reportToRms(ProcessInfo info) {
        HwSysResManager resManager;
        if (info != null && (resManager = HwSysResManager.getInstance()) != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            Bundle args = new Bundle();
            args.putInt("callPid", info.mPid);
            args.putInt("callUid", info.mUid);
            args.putString("callProcName", info.mProcessName);
            ArrayList<String> pkgs = new ArrayList<>();
            pkgs.addAll(info.mPackageName);
            args.putStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME, pkgs);
            args.putInt("relationType", 4);
            resManager.reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args));
        }
    }

    private void printProcInfo(ProcessInfo info, PrintWriter pw) {
        pw.println("  pid =  " + info.mPid + ", uid = " + info.mUid + ", count = " + info.mCount);
        StringBuilder sb = new StringBuilder();
        sb.append("  ProcessName = ");
        sb.append(info.mProcessName);
        pw.println(sb.toString());
        pw.println("  Group = " + info.mCurSchedGroup + " (-1:default, 0:backgroud, 5:top visible, 6:perceptible)");
        pw.println("  oom_Adj = " + info.mCurAdj + ", AdjType = " + info.mAdjType);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  package type = ");
        sb2.append(info.mType);
        sb2.append(" (1:SYSTEM_SERVER, 2:SYSTEM_APP , 3:HW_INSTALL, 4:THIRDPARTY)");
        pw.println(sb2.toString());
        pw.println("  LRU = " + info.mLru + " ( The first entry in the list is the least recently used)");
        pw.println("  FG Activities = " + info.mForegroundActivities + ", FG Services = " + info.mForegroundServices + ", Force FG =" + info.mForceToForeground);
        Iterator it = info.mPackageName.iterator();
        while (it.hasNext()) {
            pw.println("  package name = " + ((String) it.next()));
        }
        if (info.mCreatedTime > 0) {
            Date dat = new Date(info.mCreatedTime);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(dat);
            String sb3 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(gc.getTime());
            pw.println("  created time = " + info.mCreatedTime + ", data:" + sb3);
        }
        if (info.mKilledTime > 0) {
            Date dat2 = new Date(info.mKilledTime);
            GregorianCalendar gc2 = new GregorianCalendar();
            gc2.setTime(dat2);
            String sb4 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(gc2.getTime());
            pw.println("  Killed time = " + info.mKilledTime + ", data:" + sb4);
        }
    }

    public void setAwareProcessState(int pid, int uid, int state) {
        synchronized (this.mProcMap) {
            AwareProcessInfo info = this.mAwareProcMap.get(Integer.valueOf(pid));
            if (info == null) {
                SlogEx.e(TAG, "setAwareProcessState: fail to set state! pid: " + pid + ", uid: " + uid + ", state: " + state);
                return;
            }
            if (state != -1) {
                info.setState(state);
            }
            if (getAwareBrPolicy() != null) {
                this.mAwareBrPolicy.updateProcessBrPolicy(info, state);
            }
        }
    }

    public void setAwareProcessStateByUid(int pid, int uid, int state) {
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, AwareProcessInfo> entry : this.mAwareProcMap.entrySet()) {
                AwareProcessInfo info = entry.getValue();
                if (info != null) {
                    if (info.procProcInfo.mUid == uid) {
                        if (state != -1) {
                            info.setState(state);
                        }
                        if (getAwareBrPolicy() != null) {
                            this.mAwareBrPolicy.updateProcessBrPolicy(info, state);
                        }
                    }
                }
            }
        }
    }

    public void resetAwareProcessStatePgRestart() {
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, AwareProcessInfo> entry : this.mAwareProcMap.entrySet()) {
                AwareProcessInfo info = entry.getValue();
                if (info != null) {
                    if (info.getState() == 1) {
                        info.setState(2);
                        if (getAwareBrPolicy() != null) {
                            this.mAwareBrPolicy.updateProcessBrPolicy(info, 2);
                        }
                    }
                }
            }
        }
    }

    private AwareBroadcastPolicy getAwareBrPolicy() {
        if (this.mAwareBrPolicy == null && MultiTaskManagerService.self() != null) {
            this.mAwareBrPolicy = MultiTaskManagerService.self().getAwareBrPolicy();
        }
        return this.mAwareBrPolicy;
    }
}
