package com.android.server.mtm.taskstatus;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wm.HwStartWindowRecord;
import com.huawei.android.app.HwActivityManager;
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
    private static final int MAX_TASKS = 100;
    private static final String TAG = "ProcessInfoCollector";
    private static ProcessInfoCollector mProcessInfoCollector = null;
    boolean DEBUG = false;
    boolean INFO = false;
    private HashMap<Integer, AwareProcessInfo> mAwareProcMap = new HashMap<>();
    private ProcessInfo mCacheInfo = new ProcessInfo(0, 0);
    private HwActivityManagerService mHwAMS = null;
    private AwareBroadcastPolicy mIawareBrPolicy = null;
    private int mIndex = 0;
    private ProcessInfo[] mKilledProcList = new ProcessInfo[10];
    private HashMap<Integer, ProcessInfo> mProcMap = new HashMap<>();

    private ProcessInfoCollector() {
        init();
    }

    public static synchronized ProcessInfoCollector getInstance() {
        ProcessInfoCollector processInfoCollector;
        synchronized (ProcessInfoCollector.class) {
            if (mProcessInfoCollector == null) {
                mProcessInfoCollector = new ProcessInfoCollector();
            }
            processInfoCollector = mProcessInfoCollector;
        }
        return processInfoCollector;
    }

    private void init() {
        this.mHwAMS = HwActivityManagerService.self();
        if (this.mHwAMS == null) {
            Slog.e(TAG, "init failed to get HwAMS handler");
        }
    }

    public synchronized void recordKilledProcess(ProcessInfo info) {
        info.mKilledTime = SystemClock.uptimeMillis();
        if (this.mIndex >= 10) {
            this.mIndex = 0;
        }
        this.mKilledProcList[this.mIndex] = info;
        this.mIndex++;
    }

    public void removeKilledProcess(int pid) {
        ProcessInfo info;
        if (pid < 0) {
            Slog.e(TAG, "removeKilledProcess: proc should not less than zero ");
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
        if (pid < 0) {
            Slog.e(TAG, "getProcessInfo: proc should not less than zero ");
            return null;
        }
        ProcessInfo copyInfo = null;
        synchronized (this.mProcMap) {
            ProcessInfo info = this.mProcMap.get(Integer.valueOf(pid));
            if (info == null) {
                Slog.e(TAG, "getProcessInfo: failed to find this proc ");
            } else {
                copyInfo = new ProcessInfo(0, 0);
                ProcessInfo.copyProcessInfo(info, copyInfo);
            }
        }
        return copyInfo;
    }

    public AwareProcessInfo getAwareProcessInfo(int pid) {
        AwareProcessInfo info;
        if (pid < 0) {
            Slog.e(TAG, "getProcessInfo: proc should not less than zero ");
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
                ProcessInfo info = (ProcessInfo) entry.getValue();
                if (info != null) {
                    ArrayList<String> packageNames = info.mPackageName;
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            if (packageName.equals(packageNames.get(0))) {
                                if (userId == UserHandle.getUserId(info.mUid)) {
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
                AwareProcessInfo info = (AwareProcessInfo) entry.getValue();
                if (info != null) {
                    ArrayList<String> packageNames = info.mProcInfo.mPackageName;
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            if (packageName.equals(packageNames.get(0))) {
                                if (userId == -1 || userId == UserHandle.getUserId(info.mProcInfo.mUid)) {
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
                ProcessInfo info = (ProcessInfo) entry.getValue();
                if (info != null) {
                    ArrayList<String> packageNames = info.mPackageName;
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            String procInfoPackageName = packageNames.get(0);
                            if (packMap.get(procInfoPackageName) != null) {
                                if (packMap.get(procInfoPackageName).intValue() == UserHandle.getUserId(info.mUid)) {
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
            Slog.e(TAG, "dump PrintWriter pw is null");
        } else if (processInfos != null && !processInfos.isEmpty()) {
            pw.println("  Package/Task Processes Information dump :");
            Iterator<ProcessInfo> it = processInfos.iterator();
            while (it.hasNext()) {
                pw.println("\r\n  Running Process information :");
                printProcInfo(it.next(), pw);
            }
        }
    }

    public ArrayList<ProcessInfo> getProcessInfosFromTask(int taskId, int userId) {
        ArrayList<ProcessInfo> emptyProcList = new ArrayList<>();
        if (userId < 0 || taskId < 0) {
            return emptyProcList;
        }
        String packageName = null;
        Iterator<ActivityManager.RecentTaskInfo> iter = this.mHwAMS.getRecentTasks(100, 2, userId).getList().iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            ActivityManager.RecentTaskInfo t = iter.next();
            if (t.persistentId == taskId) {
                Intent intent = new Intent(t.baseIntent);
                if (t.origActivity != null) {
                    intent.setComponent(t.origActivity);
                }
                if (intent.getComponent() != null) {
                    packageName = intent.getComponent().getPackageName();
                }
            }
        }
        if (packageName == null) {
            return emptyProcList;
        }
        return getProcessInfosFromPackage(packageName, userId);
    }

    public ArrayList<Integer> getPidsFromUid(int uid, int userId) {
        ArrayList<Integer> pids = new ArrayList<>();
        if (userId < 0 || uid < 0) {
            return pids;
        }
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, ProcessInfo> entry : this.mProcMap.entrySet()) {
                ProcessInfo info = (ProcessInfo) entry.getValue();
                if (info != null) {
                    if (info.mAppUid == uid || info.mUid == uid) {
                        if (userId == UserHandle.getUserId(info.mUid)) {
                            pids.add(Integer.valueOf(info.mPid));
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
                ProcessInfo.copyProcessInfo((ProcessInfo) entry.getValue(), copyInfo);
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
                procList.add((AwareProcessInfo) entry.getValue());
            }
        }
        return procList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0083, code lost:
        if (r1 != false) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0085, code lost:
        reportToRms(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0088, code lost:
        return;
     */
    public void recordProcessInfo(int pid, int uid) {
        boolean exist = false;
        this.mCacheInfo.initialProcessInfo(pid, uid);
        if (!HwActivityManager.getProcessRecordFromMTM(this.mCacheInfo)) {
            if (this.DEBUG) {
                Slog.e(TAG, "recordProcessInfo  failed to get process record");
            }
            return;
        }
        synchronized (this.mProcMap) {
            ProcessInfo curProcessInfo = this.mProcMap.get(Integer.valueOf(pid));
            if (curProcessInfo == null) {
                curProcessInfo = new ProcessInfo(pid, uid);
                this.mCacheInfo.mCreatedTime = SystemClock.elapsedRealtime();
            } else {
                this.mCacheInfo.mCreatedTime = curProcessInfo.mCreatedTime;
                exist = true;
            }
            if (!ProcessInfo.copyProcessInfo(this.mCacheInfo, curProcessInfo)) {
                Slog.e(TAG, "recordProcessInfo  source or target object is null");
                return;
            }
            curProcessInfo.mCount++;
            if (!exist) {
                this.mProcMap.put(Integer.valueOf(pid), curProcessInfo);
                AwareProcessInfo awareProcessInfo = new AwareProcessInfo(pid, curProcessInfo);
                this.mAwareProcMap.put(Integer.valueOf(pid), awareProcessInfo);
                if (getIawareBrPolicy() != null) {
                    this.mIawareBrPolicy.updateProcessBrPolicy(awareProcessInfo, -1);
                }
            }
        }
    }

    public void enableDebug() {
        this.DEBUG = true;
        this.INFO = true;
    }

    public void disableDebug() {
        this.DEBUG = false;
        this.INFO = false;
    }

    public void dump(PrintWriter pw) {
        if (pw == null) {
            Slog.e(TAG, "dump PrintWriter pw is null");
            return;
        }
        pw.println("  Process Information Collector dump :");
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, ProcessInfo> entry : this.mProcMap.entrySet()) {
                pw.println("\r\n  Running Process information :");
                printProcInfo((ProcessInfo) entry.getValue(), pw);
            }
        }
        synchronized (this) {
            for (int i = 0; i < this.mIndex; i++) {
                ProcessInfo Info = this.mKilledProcList[i];
                pw.println("\r\n  Killed Process information " + i + ":");
                printProcInfo(Info, pw);
            }
        }
    }

    private void reportToRms(ProcessInfo info) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC)) && info != null) {
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

    private void printProcInfo(ProcessInfo Info, PrintWriter pw) {
        pw.println("  pid =  " + Info.mPid + ", uid = " + Info.mUid + ", count = " + Info.mCount);
        StringBuilder sb = new StringBuilder();
        sb.append("  ProcessName = ");
        sb.append(Info.mProcessName);
        pw.println(sb.toString());
        pw.println("  Group = " + Info.mCurSchedGroup + " (-1:default, 0:backgroud, 5:top visible, 6:perceptible)");
        pw.println("  oom_Adj = " + Info.mCurAdj + ", AdjType = " + Info.mAdjType);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  package type = ");
        sb2.append(Info.mType);
        sb2.append(" (1:SYSTEM_SERVER, 2:SYSTEM_APP , 3:HW_INSTALL, 4:THIRDPARTY)");
        pw.println(sb2.toString());
        pw.println("  LRU = " + Info.mLru + " ( The first entry in the list is the least recently used)");
        pw.println("  FG Activities = " + Info.mForegroundActivities + ", FG Services = " + Info.mForegroundServices + ",Force FG =" + Info.mForceToForeground);
        int list_size = Info.mPackageName.size();
        for (int i = 0; i < list_size; i++) {
            pw.println("  package name = " + ((String) Info.mPackageName.get(i)));
        }
        if (Info.mCreatedTime > 0) {
            Date dat = new Date(Info.mCreatedTime);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(dat);
            String sb3 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(gc.getTime());
            pw.println("  created time = " + Info.mCreatedTime + ", data:" + sb3);
        }
        if (Info.mKilledTime > 0) {
            Date dat2 = new Date(Info.mKilledTime);
            GregorianCalendar gc2 = new GregorianCalendar();
            gc2.setTime(dat2);
            String sb4 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(gc2.getTime());
            pw.println("  Killed time = " + Info.mKilledTime + ", data:" + sb4);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004c, code lost:
        return;
     */
    public void setAwareProcessState(int pid, int uid, int state) {
        synchronized (this.mProcMap) {
            AwareProcessInfo info = this.mAwareProcMap.get(Integer.valueOf(pid));
            if (info == null) {
                Slog.e(TAG, "setAwareProcessState: fail to set state! pid: " + pid + ", uid:" + uid + ", state:" + state);
                return;
            }
            if (state != -1) {
                info.setState(state);
            }
            if (getIawareBrPolicy() != null) {
                this.mIawareBrPolicy.updateProcessBrPolicy(info, state);
            }
        }
    }

    public void setAwareProcessStateByUid(int pid, int uid, int state) {
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, AwareProcessInfo> entry : this.mAwareProcMap.entrySet()) {
                AwareProcessInfo info = (AwareProcessInfo) entry.getValue();
                if (info != null && info.mProcInfo.mUid == uid) {
                    if (state != -1) {
                        info.setState(state);
                    }
                    if (getIawareBrPolicy() != null) {
                        this.mIawareBrPolicy.updateProcessBrPolicy(info, state);
                    }
                }
            }
        }
    }

    public void resetAwareProcessStatePgRestart() {
        synchronized (this.mProcMap) {
            for (Map.Entry<Integer, AwareProcessInfo> entry : this.mAwareProcMap.entrySet()) {
                AwareProcessInfo info = (AwareProcessInfo) entry.getValue();
                if (info != null && info.getState() == 1) {
                    info.setState(2);
                    if (getIawareBrPolicy() != null) {
                        this.mIawareBrPolicy.updateProcessBrPolicy(info, 2);
                    }
                }
            }
        }
    }

    private AwareBroadcastPolicy getIawareBrPolicy() {
        if (this.mIawareBrPolicy == null && MultiTaskManagerService.self() != null) {
            this.mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
        }
        return this.mIawareBrPolicy;
    }
}
