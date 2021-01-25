package com.android.server.mtm.iaware.appmng.appiolimit;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.SystemUnremoveUidCache;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.feature.AppIoLimitFeature;
import com.android.server.rms.iaware.feature.IoLimitFeature;
import com.android.server.rms.iaware.feature.IoLimitGroup;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.os.UserHandleEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppDefaultIoLimit extends CleanSource {
    private static final long EXPIRETIME = 600;
    private static final String IOLIMITED_BAD = "app_iolimit_bad";
    private static final int LEVEL = 0;
    private static final Object LOCK = new Object();
    private static final String PACKAGE_CAMERA = "com.huawei.camera";
    private static final String TAG = "AwareAppDefaultIoLimit";
    private static AwareAppDefaultIoLimit sAwareAppDefaultIoLimit = null;
    private static boolean sDebug = false;
    private Map<Integer, Integer> mCurHeavyIOPids = new ArrayMap();
    private Map<Integer, Integer> mCurIOPids = new ArrayMap();
    private SparseSet mCurIolimitUids = new SparseSet();
    private Map<Integer, Integer> mHeavyIOPids = new ArrayMap();
    private Map<Integer, Integer> mIOPids = new ArrayMap();
    private SparseSet mIolimitUids = new SparseSet();
    private final AtomicBoolean mIsExecutorAlive = new AtomicBoolean(false);
    private PackageManager mPm = null;
    private Set<String> mSysUnremoveBadPkgs = new ArraySet();
    private SystemUnremoveUidCache mSystemUnremoveUidCache;
    private long mUpdateTime = 0;

    public static AwareAppDefaultIoLimit getInstance() {
        AwareAppDefaultIoLimit awareAppDefaultIoLimit;
        synchronized (LOCK) {
            if (sAwareAppDefaultIoLimit == null) {
                sAwareAppDefaultIoLimit = new AwareAppDefaultIoLimit();
            }
            awareAppDefaultIoLimit = sAwareAppDefaultIoLimit;
        }
        return awareAppDefaultIoLimit;
    }

    private AwareAppDefaultIoLimit() {
    }

    public void init(Context ctx) {
        if (ctx != null) {
            this.mPm = ctx.getPackageManager();
            this.mSystemUnremoveUidCache = SystemUnremoveUidCache.getInstance(ctx);
            initSystemUnremoveBadList();
        }
    }

    public void deInitDefaultFree() {
        this.mPm = null;
    }

    private void initSystemUnremoveBadList() {
        ArrayList<String> badList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_IOLIMIT.getDesc(), IOLIMITED_BAD);
        if (badList != null) {
            this.mSysUnremoveBadPkgs.clear();
            this.mSysUnremoveBadPkgs.addAll(badList);
        }
    }

    private boolean isDownloadUpApp(int pid, int uid, List<String> packageName) {
        return AwareAppKeyBackgroup.getInstance().checkKeyBackgroupByState(5, pid, uid, packageName);
    }

    public void doLimitIO(String pkgName, AppMngConstant.AppIoLimitSource config, boolean isCamera) {
        if (this.mIsExecutorAlive.compareAndSet(false, true)) {
            new Thread(new Runnable(pkgName, config, isCamera) {
                /* class com.android.server.mtm.iaware.appmng.appiolimit.$$Lambda$AwareAppDefaultIoLimit$_CYgcXUeCwOoiG0ydA4ep2IKjDQ */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ AppMngConstant.AppIoLimitSource f$2;
                private final /* synthetic */ boolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    AwareAppDefaultIoLimit.this.lambda$doLimitIO$0$AwareAppDefaultIoLimit(this.f$1, this.f$2, this.f$3);
                }
            }, "iaware.iolimit").start();
        }
    }

    public /* synthetic */ void lambda$doLimitIO$0$AwareAppDefaultIoLimit(String pkgName, AppMngConstant.AppIoLimitSource config, boolean isCamera) {
        CleanSource.setSchedPriority();
        if (pkgName == null) {
            AwareLog.w(TAG, "The input params is invalid");
            return;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos = getAllIOLimitApp(config, isCamera);
        if (awareProcessBlockInfos == null || awareProcessBlockInfos.isEmpty()) {
            AwareLog.w(TAG, "no pid need to limit");
            return;
        }
        if (sDebug) {
            AwareLog.d(TAG, "currPkg:" + pkgName);
        }
        synchronized (LOCK) {
            long curTime = SystemClock.elapsedRealtime() / 1000;
            if (checkNeedDiffIoLimit(curTime)) {
                this.mCurIOPids.clear();
                this.mCurHeavyIOPids.clear();
                this.mCurIolimitUids.clear();
                doIOBlockGroup(awareProcessBlockInfos, pkgName, this.mCurIOPids, this.mCurHeavyIOPids, this.mCurIolimitUids);
                doDifferentialIoLimit();
            } else {
                this.mIOPids.clear();
                this.mHeavyIOPids.clear();
                this.mIolimitUids.clear();
                doIOBlockGroup(awareProcessBlockInfos, pkgName, this.mIOPids, this.mHeavyIOPids, this.mIolimitUids);
                doLightIoLimit(true, this.mIOPids);
                doHeavyIoLimit(true, this.mHeavyIOPids);
                this.mUpdateTime = curTime;
            }
        }
        this.mIsExecutorAlive.set(false);
    }

    private boolean checkNeedDiffIoLimit(long curTime) {
        if (!AppIoLimitFeature.isFeatureEnable()) {
            return false;
        }
        long j = this.mUpdateTime;
        if (j == 0 || curTime - j >= EXPIRETIME || curTime - j < 0) {
            return false;
        }
        return true;
    }

    private void doDifferentialIoLimit() {
        ArraySet<Integer> removeLightPids = getDeletePids(this.mIOPids, this.mCurIOPids);
        ArraySet<Integer> removeHeavyPids = getDeletePids(this.mHeavyIOPids, this.mCurHeavyIOPids);
        SparseSet removeIOPids = new SparseSet();
        removeIOPids.addAll(removeLightPids);
        removeIOPids.addAll(removeHeavyPids);
        Map<Integer, Integer> addLightIOPids = getAddIOPids(this.mIOPids, this.mCurIOPids);
        Map<Integer, Integer> addHeavyIOPids = getAddIOPids(this.mHeavyIOPids, this.mCurHeavyIOPids);
        restorePidsSets();
        IoLimitFeature.getInstance().removeIoLimitTaskList(removeIOPids);
        doLightIoLimit(false, addLightIOPids);
        doHeavyIoLimit(false, addHeavyIOPids);
        if (sDebug) {
            AwareLog.d(TAG, "Base count: " + ((this.mIOPids.keySet().size() * 2) + (this.mHeavyIOPids.keySet().size() * 2)));
            AwareLog.d(TAG, "Opt count: " + (addLightIOPids.keySet().size() + addHeavyIOPids.keySet().size() + removeIOPids.size()));
            AwareLog.d(TAG, "remove IO Pids: " + removeIOPids.toString());
            AwareLog.d(TAG, "add IO pids: Light IO pids: " + addLightIOPids.toString() + ", Heavy IO pids: " + addHeavyIOPids.toString());
        }
    }

    private ArraySet<Integer> getDeletePids(Map<Integer, Integer> ioPids, Map<Integer, Integer> curIoPids) {
        ArraySet<Integer> deletePids = new ArraySet<>();
        deletePids.addAll(ioPids.keySet());
        deletePids.removeAll(curIoPids.keySet());
        return deletePids;
    }

    private Map<Integer, Integer> getAddIOPids(Map<Integer, Integer> ioPids, Map<Integer, Integer> curIoPids) {
        Map<Integer, Integer> addIOPids = new ArrayMap<>();
        for (Map.Entry<Integer, Integer> entry : curIoPids.entrySet()) {
            if (!ioPids.keySet().contains(entry.getKey())) {
                addIOPids.put(entry.getKey(), entry.getValue());
            }
        }
        return addIOPids;
    }

    private void restorePidsSets() {
        this.mIOPids.clear();
        this.mHeavyIOPids.clear();
        this.mIolimitUids.clear();
        this.mIOPids.putAll(this.mCurIOPids);
        this.mHeavyIOPids.putAll(this.mCurHeavyIOPids);
        this.mIolimitUids.addAll(this.mCurIolimitUids);
    }

    private void doIOBlockGroup(List<AwareProcessBlockInfo> awareProcessBlockInfos, String pkgName, Map<Integer, Integer> iopids, Map<Integer, Integer> heavyIopids, SparseSet iolimitUids) {
        List<AwareProcessInfo> infos;
        SparseSet filterPids = getAssocPidsByUid(getUidByPackageName(pkgName));
        for (AwareProcessBlockInfo processInfo : awareProcessBlockInfos) {
            if (processInfo.procCleanType == ProcessCleaner.CleanType.IO_LIMIT && ((!pkgName.equals(processInfo.procPackageName) || UserHandleEx.getUserId(processInfo.procUid) != AwareAppAssociate.getInstance().getCurUserId()) && (infos = processInfo.procProcessList) != null && !infos.isEmpty())) {
                doIOGroup(infos, filterPids, iopids, heavyIopids, iolimitUids);
                printLimitedApp(infos, processInfo.procPackageName);
            }
        }
    }

    private void printLimitedApp(List<AwareProcessInfo> infos, String pkgName) {
        if (!(infos == null || infos.isEmpty() || !sDebug)) {
            StringBuffer sb = new StringBuffer();
            for (AwareProcessInfo info : infos) {
                sb.append(info.procPid);
                sb.append(" ");
            }
            AwareLog.d(TAG, "IO Limit App:" + pkgName + ",pids:" + sb.toString());
        }
    }

    private void doIOGroup(List<AwareProcessInfo> infos, SparseSet filterPids, Map<Integer, Integer> ioPids, Map<Integer, Integer> heavyIoPids, SparseSet iolimitUids) {
        if (!(infos == null || infos.isEmpty())) {
            for (AwareProcessInfo info : infos) {
                iolimitUids.add(info.procProcInfo.mUid);
                if (!filterPids.contains(info.procPid)) {
                    if (isDownloadUpApp(info.procProcInfo.mPid, info.procProcInfo.mUid, info.procProcInfo.mPackageName)) {
                        heavyIoPids.put(Integer.valueOf(info.procPid), Integer.valueOf(info.procProcInfo.mUid));
                    } else {
                        ioPids.put(Integer.valueOf(info.procPid), Integer.valueOf(info.procProcInfo.mUid));
                    }
                }
            }
        }
    }

    private void doLightIoLimit(boolean removeAll, Map<Integer, Integer> ioPids) {
        if (!this.mIOPids.isEmpty()) {
            IoLimitFeature.getInstance().setIoLimitTaskList(IoLimitGroup.LIGHT, ioPids, removeAll);
            IoLimitFeature.getInstance().enable(IoLimitGroup.LIGHT);
            AwareLog.i(TAG, "do Limit light IO pids:" + ioPids.toString());
        }
    }

    private void doHeavyIoLimit(boolean removeAll, Map<Integer, Integer> ioHeavyPids) {
        if (!this.mHeavyIOPids.isEmpty()) {
            IoLimitFeature.getInstance().setIoLimitTaskList(IoLimitGroup.HEAVY, ioHeavyPids, removeAll);
            IoLimitFeature.getInstance().enable(IoLimitGroup.HEAVY);
            AwareLog.i(TAG, "do Limit heavy IO pids:" + ioHeavyPids.toString());
        }
    }

    private List<AwareProcessBlockInfo> getAllIOLimitApp(AppMngConstant.AppIoLimitSource config, boolean isCamera) {
        List<AwareProcessInfo> awareProcListBeforeFiler = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (awareProcListBeforeFiler == null || awareProcListBeforeFiler.isEmpty()) {
            AwareLog.d(TAG, "no pid exit in mtm");
            return null;
        }
        List<AwareProcessInfo> awareProcList = new ArrayList<>(awareProcListBeforeFiler.size());
        for (AwareProcessInfo awareProc : awareProcListBeforeFiler) {
            if (!(awareProc == null || awareProc.procProcInfo == null)) {
                if ((isCamera && AwareAppIoLimitMng.isCameraEnhanced()) || !isUnRemoveApp(awareProc.procProcInfo)) {
                    awareProcList.add(awareProc);
                }
            }
        }
        if (awareProcList.isEmpty()) {
            AwareLog.w(TAG, "no pid need to app filter");
            return null;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos = DecisionMaker.getInstance().decideAll(awareProcList, 0, AppMngConstant.AppMngFeature.APP_IOLIMIT, (AppMngConstant.EnumWithDesc) config);
        if (!(awareProcessBlockInfos == null || awareProcessBlockInfos.isEmpty())) {
            return mergeBlock(awareProcessBlockInfos);
        }
        AwareLog.w(TAG, "no pid need to io limit");
        return null;
    }

    private boolean isUnRemoveApp(ProcessInfo procInfo) {
        int uid = procInfo.mAppUid % 100000;
        if (isSystemUnRemoveBadPkg(procInfo)) {
            return false;
        }
        if (uid > 0 && uid <= 10000) {
            return true;
        }
        SystemUnremoveUidCache systemUnremoveUidCache = this.mSystemUnremoveUidCache;
        if (systemUnremoveUidCache == null || !systemUnremoveUidCache.checkUidExist(uid)) {
            return false;
        }
        return true;
    }

    private boolean isSystemUnRemoveBadPkg(ProcessInfo procInfo) {
        if (!this.mSysUnremoveBadPkgs.isEmpty() && procInfo.mPackageName != null && !procInfo.mPackageName.isEmpty()) {
            int size = procInfo.mPackageName.size();
            for (int i = 0; i < size; i++) {
                if (this.mSysUnremoveBadPkgs.contains((String) procInfo.mPackageName.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getUidByPackageName(String pkgName) {
        if (pkgName == null) {
            return 0;
        }
        try {
            if (pkgName.isEmpty() || this.mPm == null) {
                return 0;
            }
            return PackageManagerExt.getApplicationInfoAsUser(this.mPm, pkgName, 1, AwareAppAssociate.getInstance().getCurUserId()).uid;
        } catch (PackageManager.NameNotFoundException e) {
            AwareLog.w(TAG, "get application info failed");
            return 0;
        }
    }

    private SparseSet getAssocPidsByUid(int uid) {
        SparseSet assocPids = new SparseSet();
        SparseSet pids = ProcessInfoCollector.getInstance().getPidsFromUid(uid, UserHandleEx.getUserId(uid));
        if (pids.isEmpty()) {
            if (sDebug) {
                AwareLog.w(TAG, "get assoc pids is empty");
            }
            return assocPids;
        }
        assocPids.addAll(pids);
        int size = pids.size();
        for (int i = 0; i < size; i++) {
            int pid = pids.keyAt(i);
            SparseSet strongPids = new SparseSet();
            AwareAppAssociate.getInstance().getAssocListForPid(pid, strongPids);
            if (!strongPids.isEmpty()) {
                assocPids.addAll(strongPids);
            }
        }
        if (sDebug) {
            AwareLog.i(TAG, "get assoc pids by uid:" + uid + ", pids:" + pids + ", assocPids:" + assocPids);
        }
        return assocPids;
    }

    public static void enableDebug() {
        AwareLog.d(TAG, "enableDebug");
        sDebug = true;
    }

    public static void disableDebug() {
        AwareLog.d(TAG, "disableDebug");
        sDebug = false;
    }

    public void doUnLimitIO() {
        IoLimitFeature.getInstance().disable(IoLimitGroup.LIGHT);
        IoLimitFeature.getInstance().disable(IoLimitGroup.HEAVY);
        AwareLog.i(TAG, "do IO UnLimit ok");
    }

    public void doRemoveIoPids(int uid, int pid) {
        synchronized (LOCK) {
            if (this.mIolimitUids.contains(uid)) {
                if (pid <= 0 || this.mIOPids.containsKey(Integer.valueOf(pid)) || this.mHeavyIOPids.containsKey(Integer.valueOf(pid))) {
                    SparseSet ioLimitPids = getAssocPidsByUid(uid);
                    if (!ioLimitPids.isEmpty()) {
                        IoLimitFeature.getInstance().removeIoLimitTaskList(ioLimitPids);
                        this.mIolimitUids.remove(uid);
                        updateCachePids(ioLimitPids);
                        AwareLog.i(TAG, "doRemoveIoPids pids:" + ioLimitPids.toString());
                    }
                }
            }
        }
    }

    private void updateCachePids(SparseSet ioLimitPids) {
        int size = ioLimitPids.size();
        for (int i = 0; i < size; i++) {
            int pid = ioLimitPids.keyAt(i);
            this.mIOPids.remove(Integer.valueOf(pid));
            this.mHeavyIOPids.remove(Integer.valueOf(pid));
        }
    }

    public void dumpLightIolimit(PrintWriter pw) {
        synchronized (LOCK) {
            pw.println("Light IOlimit Pids: " + this.mIOPids.toString());
        }
    }

    public void dumpHeavyIolimit(PrintWriter pw) {
        synchronized (LOCK) {
            pw.println("Heavy IOlimit Pids: " + this.mHeavyIOPids.toString());
        }
    }
}
