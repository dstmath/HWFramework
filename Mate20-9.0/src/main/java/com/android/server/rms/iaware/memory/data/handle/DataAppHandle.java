package com.android.server.rms.iaware.memory.data.handle;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.ProcessRecord;
import com.android.server.gesture.GestureNavConst;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.feature.AppQuickStartFeature;
import com.android.server.rms.iaware.feature.MemoryFeature2;
import com.android.server.rms.iaware.memory.action.GpuCompressAction;
import com.android.server.rms.iaware.memory.action.ReclaimAction;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.rms.iaware.memory.utils.PrereadUtils;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import com.android.server.rms.memrepair.ProcStateStatisData;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMService;
import java.util.ArrayList;
import java.util.List;

public class DataAppHandle extends AbsDataHandle {
    private static final long ACTIVITY_START_TIMEOUT = 5000;
    private static final String LAUNCHMODE_PRELOAD = "start application";
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String TAG = "AwareMem_AppHandle";
    private static DataAppHandle sDataHandle;
    private long mAcvityLaunchBeginTimestamp = 0;
    private String mFgPkgName = null;
    private int mFgPkgUid = 0;
    /* access modifiers changed from: private */
    public HwActivityManagerService mHwAMS = HwActivityManagerService.self();
    private boolean mIsActivityLaunching = false;
    private boolean mIsInBigMemoryMode = false;
    private String mLastPrereadPkg = null;
    private LastProcInfo mLastProcInfo = new LastProcInfo();
    private TrimMemoryHandler mTrimHandler = new TrimMemoryHandler();

    private static final class LastProcInfo {
        boolean mIsEmergMem;
        long mReqMem;

        private LastProcInfo() {
        }

        public boolean isEmerg() {
            return this.mIsEmergMem;
        }

        public boolean isEmerg(int uid, int pid, String packageName, String procName) {
            this.mIsEmergMem = false;
            if (MemoryConstant.isExactKillSwitch() || MemoryConstant.isFastKillSwitch()) {
                long availableRam = MemoryReader.getInstance().getMemAvailable();
                if (availableRam <= 0) {
                    AwareLog.e(DataAppHandle.TAG, "execute faild to read availableRam =" + availableRam);
                    return false;
                }
                if (MemoryConstant.isExactKillSwitch()) {
                    long procPss = getUss(uid, pid, packageName, procName);
                    if (procPss > 0) {
                        long realReqMem = MemoryConstant.getEmergencyMemory() + procPss;
                        if (realReqMem > availableRam) {
                            this.mReqMem = realReqMem < MemoryConstant.getMiddleWater() ? realReqMem : MemoryConstant.getMiddleWater();
                            AwareLog.i(DataAppHandle.TAG, "isEmerg=" + procPss + ",procName=" + procName + ",realAvailMem=" + this.mReqMem);
                            this.mIsEmergMem = true;
                            return true;
                        }
                    }
                }
                if (!MemoryConstant.isFastKillSwitch() || availableRam >= MemoryConstant.getCriticalMemory()) {
                    return false;
                }
                AwareLog.d(DataAppHandle.TAG, "isEmerg fast kill,packageName=" + packageName + ",procName=" + procName);
                this.mIsEmergMem = true;
                this.mReqMem = 0;
                return true;
            }
            AwareLog.d(DataAppHandle.TAG, "clean switch false");
            return false;
        }

        public void reset() {
            this.mIsEmergMem = false;
            this.mReqMem = 0;
        }

        private long getUss(int uid, int pid, String packageName, String procName) {
            long curUss = 0;
            if (pid > 0) {
                long[] outUss = new long[2];
                ResourceCollector.getPssFast(pid, outUss, null);
                curUss = outUss[0] + (outUss[1] / 3);
                if (curUss == 0) {
                    curUss = MemoryReader.getPssForPid(pid);
                }
            }
            long historyUss = ProcStateStatisData.getInstance().getHistoryProcUss(uid, packageName, procName);
            AwareLog.d(DataAppHandle.TAG, "getUss=" + curUss + ",historyUss=" + historyUss + ",packageName=" + packageName + ",procName=" + procName);
            if (historyUss <= 0 || historyUss < curUss) {
                return 0;
            }
            return historyUss - curUss;
        }
    }

    private final class TrimMemoryHandler extends Handler {
        private static final int MSG_COMPRESS_GPUMEMORY = 200;
        private static final int MSG_TRIM_MEMORY = 100;
        private static final int ONTRIM_DELAY_TIME = 3000;
        private Integer mLastPid;
        private String mLastPkgName;
        private List<OnTrimTask> waitForOnTrimPids;

        private class OnTrimTask {
            public int mPid;
            public long mTime;

            public OnTrimTask(int pid, long t) {
                this.mPid = pid;
                this.mTime = t;
            }
        }

        private TrimMemoryHandler() {
            this.mLastPid = 0;
            this.mLastPkgName = null;
            this.waitForOnTrimPids = new ArrayList();
        }

        private void interuptTrimMemoryForPid(int curPid) {
            synchronized (this.waitForOnTrimPids) {
                for (int i = this.waitForOnTrimPids.size() - 1; i >= 0; i--) {
                    if (this.waitForOnTrimPids.get(i).mPid == curPid) {
                        AwareLog.i(DataAppHandle.TAG, "interuptTrimMemoryForPid! curPid:" + curPid + " tPid:" + tsk.mPid);
                        this.waitForOnTrimPids.remove(i);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public void doTrimMemory(int curPid, String pkgName) {
            if (!DataAppHandle.this.isLauncher(pkgName)) {
                interuptTrimMemoryForPid(curPid);
            }
            if (this.mLastPkgName != null && this.mLastPid.intValue() != 0 && !"com.android.systemui".equals(this.mLastPkgName) && !DataAppHandle.this.isLauncher(this.mLastPkgName) && this.mLastPid.intValue() != curPid) {
                synchronized (this.waitForOnTrimPids) {
                    this.waitForOnTrimPids.add(new OnTrimTask(this.mLastPid.intValue(), SystemClock.elapsedRealtime()));
                }
                Message message = Message.obtain();
                message.arg1 = this.mLastPid.intValue();
                message.what = 100;
                sendMessageDelayed(message, 3000);
            }
        }

        /* access modifiers changed from: private */
        public void saveLastActivityInfo(int pid, String pkgName) {
            this.mLastPkgName = pkgName;
            this.mLastPid = Integer.valueOf(pid);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0036, code lost:
            if (r0 == 0) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0038, code lost:
            com.android.server.rms.iaware.memory.utils.MemoryUtils.trimMemory(com.android.server.rms.iaware.memory.data.handle.DataAppHandle.access$500(r8.this$0), java.lang.String.valueOf(r0), 40);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
            return;
         */
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                int pid = 0;
                synchronized (this.waitForOnTrimPids) {
                    if (this.waitForOnTrimPids.size() > 0) {
                        OnTrimTask ott = this.waitForOnTrimPids.get(0);
                        if (SystemClock.elapsedRealtime() - ott.mTime >= 3000) {
                            pid = ott.mPid;
                            this.waitForOnTrimPids.remove(0);
                        }
                    }
                }
            }
        }
    }

    public static DataAppHandle getInstance() {
        DataAppHandle dataAppHandle;
        synchronized (DataAppHandle.class) {
            if (sDataHandle == null) {
                sDataHandle = new DataAppHandle();
            }
            dataAppHandle = sDataHandle;
        }
        return dataAppHandle;
    }

    public int reportData(long timestamp, int event, AttrSegments attrSegments) {
        int result = -1;
        ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
        if (appInfo == null) {
            AwareLog.w(TAG, "appInfo is NULL");
            return -1;
        }
        if (event == 15001) {
            result = handleProcessBegin(timestamp, event, appInfo);
        } else if (event == 15005) {
            result = handleActivityBegin(timestamp, event, appInfo);
        } else if (event == 15010) {
            result = handleAppPrepareMem(timestamp, event, appInfo);
        } else if (event == 15013) {
            result = handleDisplayedBegin(timestamp, event, appInfo);
        } else if (event == 85003) {
            result = handleProcessExitFinish(timestamp, event, appInfo);
        } else if (event == 85005) {
            result = handleActivityFinish(timestamp, event, appInfo);
        } else if (event == 85013) {
            result = handleDisplayedFinish(timestamp, event, appInfo);
        }
        return result;
    }

    private int handleAppPrepareMem(long timestamp, int event, ArrayMap<String, String> appInfo) {
        if (!MemoryFeature2.isUpMemoryFeature.get()) {
            return -1;
        }
        return handleBigMemAppPrepare(parseInt(appInfo.get("requestMem")), appInfo, new Bundle(), timestamp, event);
    }

    private int parseInt(String intStr) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse int failed:" + intStr);
            return -1;
        }
    }

    private int handleBigMemAppPrepare(int reqMem, ArrayMap<String, String> appInfo, Bundle extras, long timestamp, int event) {
        int reqMemKB = (reqMem & 65535) * 1024;
        if (reqMemKB <= 0) {
            AwareLog.w(TAG, "handleBigMemAppPrepare error, reqMemKB " + reqMemKB);
            return -1;
        }
        int uid = parseInt(appInfo.get("uid")) % LaserTSMService.EXCUTE_OTA_RESULT_SUCCESS;
        if (uid == MemoryConstant.getSystemCameraUid() || uid == 1000) {
            Bundle bundle = extras;
            bundle.putLong("reqMem", (long) reqMemKB);
            AwareLog.i(TAG, "handleBigMemAppPrepare reqMemKB " + reqMemKB + "kb");
            this.mDMEServer.execute(MemoryConstant.MEM_SCENE_BIGMEM, bundle, event, timestamp);
            return 0;
        }
        AwareLog.i(TAG, "invalid uid:" + uid);
        return -1;
    }

    public boolean isActivityLaunching() {
        long endTimeStamp = SystemClock.uptimeMillis();
        if (!this.mIsActivityLaunching || endTimeStamp - this.mAcvityLaunchBeginTimestamp >= ACTIVITY_START_TIMEOUT) {
            return false;
        }
        return true;
    }

    public Bundle createBundleFromAppInfo() {
        return createBundleFromAppInfo(this.mFgPkgUid, this.mFgPkgName);
    }

    private DataAppHandle() {
    }

    private void mayNeedExitSpecialScene() {
        if (this.mIsInBigMemoryMode) {
            MemoryUtils.exitSpecialSceneNotify();
            this.mIsInBigMemoryMode = false;
        }
    }

    private void mayNeedEnterSpecialScene(String appName, String activityName) {
        if (!MemoryConstant.CAMERA_PACKAGE_NAME.equals(appName)) {
            if (BigMemoryInfo.getInstance().isBigMemoryApp(activityName)) {
                this.mIsInBigMemoryMode = true;
                MemoryUtils.enterSpecialSceneNotify(MemoryConstant.getCameraPowerUPMemory(), 16746243, 1);
            } else {
                mayNeedExitSpecialScene();
            }
        }
    }

    private void interruptReclaimIfNeeded(String packageName, int pid) {
        if (MemoryConstant.getCameraPreloadSwitch() == 1 && MemoryConstant.CAMERA_PACKAGE_NAME.equals(packageName)) {
            MemoryUtils.reclaimProcessAll(pid, true);
        }
    }

    private int handleActivityBegin(long timestamp, int event, ArrayMap<String, String> appInfo) {
        ArrayMap<String, String> arrayMap = appInfo;
        String appName = arrayMap.get("packageName");
        String activityName = arrayMap.get("activityName");
        String procName = arrayMap.get("processName");
        int uid = -1;
        int pid = -1;
        try {
            uid = Integer.parseInt(arrayMap.get("uid"));
            pid = Integer.parseInt(arrayMap.get("pid"));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "uid is not right");
        }
        if (isFirstBooting(appName)) {
            AwareLog.d(TAG, "booting " + appName + "," + activityName + " activity begin");
            return 0;
        }
        this.mFgPkgUid = uid;
        this.mFgPkgName = appName;
        interruptReclaimIfNeeded(appName, pid);
        dispatchPrereadMsg(appName, uid);
        MemoryConstant.resetTotalAPIRequestMemory();
        if (isLauncher(appName)) {
            mayNeedExitSpecialScene();
            return 0;
        }
        if (MemoryConstant.isKernCompressEnable()) {
            MemoryUtils.rccPause();
        }
        if (isInCallActivity(appName)) {
            return 0;
        }
        if (MemoryFeature2.isUpMemoryFeature.get() && MemoryConstant.getConfigGmcSwitch() != 0 && uid >= 0) {
            GpuCompressAction.removeUidFromGMCMap(uid);
        }
        Bundle extras = createBundleFromAppInfo(uid, appName);
        if (BigMemoryInfo.getInstance().isBigMemoryApp(activityName)) {
            extras.putString("appName", activityName);
        }
        AwareLog.d(TAG, "app event " + appName + " activity begin");
        setActivityLaunching(true);
        if (BigMemoryInfo.getInstance().isBigMemoryApp(appName) || BigMemoryInfo.getInstance().isBigMemoryApp(activityName)) {
            MemoryConstant.enableBigMemCriticalMemory();
            EventTracker.getInstance().trackEvent(1000, event, timestamp, null);
            this.mDMEServer.execute(MemoryConstant.MEM_SCENE_BIGMEM, extras, event, timestamp);
        } else {
            MemoryConstant.disableBigMemCriticalMemory();
            int i = event;
            this.mDMEServer.stopExecute(timestamp, i);
            if (this.mLastProcInfo.isEmerg(uid, pid, appName, procName)) {
                extras.putLong("appMem", this.mLastProcInfo.mReqMem);
                extras.putBoolean("immediate", true);
                this.mDMEServer.execute(MemoryConstant.MEM_SCENE_DEFAULT, extras, i, timestamp);
            }
        }
        mayNeedEnterSpecialScene(appName, activityName);
        return 0;
    }

    private void dispatchPrereadMsg(String appName, int uid) {
        if (appName != null && !appName.equals(this.mLastPrereadPkg)) {
            this.mLastPrereadPkg = appName;
            if (!"com.android.systemui".equals(appName) && this.mHwAMS != null && !isLauncher(appName)) {
                if (MemoryConstant.getCameraPrereadFileMap().containsKey(appName)) {
                    PrereadUtils.getInstance();
                    PrereadUtils.sendPrereadMsg(appName);
                } else if (!AppQuickStartFeature.isExactPrereadFeatureEnable() && this.mHwAMS.numOfPidWithActivity(uid) == 0) {
                    PrereadUtils.getInstance();
                    if (PrereadUtils.addPkgFilesIfNecessary(appName)) {
                        PrereadUtils.getInstance();
                        PrereadUtils.sendPrereadMsg(appName);
                    }
                }
            }
        }
    }

    private int handleActivityFinish(long timestamp, int event, ArrayMap<String, String> appInfo) {
        String appName = appInfo.get("packageName");
        String activityName = appInfo.get("activityName");
        int uid = -1;
        try {
            uid = Integer.parseInt(appInfo.get("uid"));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "uid is not right");
        }
        if (isFirstBooting(appName)) {
            AwareLog.d(TAG, "booting " + appName + "," + activityName + " activity finish");
            return 0;
        }
        handleActivityFinishDFX(appInfo, appName);
        if (MemoryFeature2.isUpMemoryFeature.get() && MemoryConstant.getConfigGmcSwitch() != 0) {
            int pid = 0;
            try {
                pid = Integer.parseInt(appInfo.get("pid"));
            } catch (NumberFormatException e2) {
                AwareLog.e(TAG, "pid is not right");
            }
            if (pid != 0) {
                this.mTrimHandler.doTrimMemory(pid, appName);
                this.mTrimHandler.saveLastActivityInfo(pid, appName);
            }
            if (uid >= 0 && !isLauncher(appName)) {
                GpuCompressAction.removeUidFromGMCMap(uid);
            }
        }
        if (BigMemoryInfo.getInstance().isBigMemoryApp(appName) || BigMemoryInfo.getInstance().isBigMemoryApp(activityName)) {
            MemoryConstant.enableBigMemCriticalMemory();
        } else {
            MemoryConstant.disableBigMemCriticalMemory();
            mayNeedExitSpecialScene();
        }
        if (isLauncher(appName)) {
            SysLoadManager.getInstance().enterLauncher();
            return 0;
        } else if (isInCallActivity(appName)) {
            return 0;
        } else {
            Bundle extras = createBundleFromAppInfo(uid, appName);
            AwareLog.d(TAG, "app event " + appName + " activity finish");
            setActivityLaunching(false);
            if (!this.mLastProcInfo.isEmerg()) {
                this.mDMEServer.execute(MemoryConstant.MEM_SCENE_DEFAULT, extras, event, timestamp);
            }
            this.mLastProcInfo.reset();
            return 0;
        }
    }

    private void handleActivityFinishDFX(ArrayMap<String, String> appInfo, String appName) {
        if (AwareConstant.CURRENT_USER_TYPE == 3 && appName != null) {
            String processName = appInfo.get("processName");
            if (processName != null) {
                AwareAppMngDFX.getInstance().trackeAppStartInfo(appName, processName, 11);
                if (!"com.android.systemui".equals(appName) && !isLauncher(appName) && !appName.equals(this.mFgPkgName)) {
                    BigDataStore.getInstance().warmLaunch++;
                    AwareLog.d(TAG, "pkg: " + appName + ", warmLaunch: " + BigDataStore.getInstance().warmLaunch);
                }
                this.mFgPkgName = appName;
                AwareLog.d(TAG, "fgPkg: " + this.mFgPkgName);
            }
        }
    }

    private int handleProcessBegin(long timestamp, int event, ArrayMap<String, String> appInfo) {
        AwareLog.d(TAG, "app event process launch begin");
        String launchMode = appInfo.get("launchMode");
        String packageName = appInfo.get("packageName");
        if (MemoryConstant.getCameraPreloadSwitch() == 1 && MemoryConstant.CAMERA_PACKAGE_NAME.equals(packageName) && LAUNCHMODE_PRELOAD.equals(launchMode)) {
            try {
                String processName = appInfo.get("processName");
                int uid = Integer.parseInt(appInfo.get("uid"));
                if (!(processName == null || this.mHwAMS == null)) {
                    ProcessRecord app = this.mHwAMS.getProcessRecord(processName, uid, true);
                    if (app != null) {
                        ReclaimAction.reclaimProcessAll(this.mTrimHandler, app);
                    }
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "handleProcessBegin get uid number format err");
            }
        }
        return traceProcess(true, launchMode, appInfo, timestamp);
    }

    private int handleProcessExitFinish(long timestamp, int event, ArrayMap<String, String> appInfo) {
        AwareLog.d(TAG, "app event process exit finish");
        return traceProcess(false, appInfo.get("exitMode"), appInfo, timestamp);
    }

    private int handleDisplayedBegin(long timestamp, int event, ArrayMap<String, String> appInfo) {
        AwareLog.d(TAG, "handleDisplayedBegin");
        if (!MemoryFeature2.isUpMemoryFeature.get() || AwareConstant.CURRENT_USER_TYPE != 3) {
            return -1;
        }
        try {
            String activityName = appInfo.get("activityName");
            int pid = Integer.parseInt(appInfo.get("pid"));
            if (activityName == null) {
                return -1;
            }
            AwareLog.d(TAG, "activity displayed time : " + activityName + " , " + pid + ", 0");
            MemoryUtils.sendActivityDisplayedTime(activityName, pid, 0);
            return 0;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "handleDisplayedBegin get pid or time failed");
            return -1;
        }
    }

    private int handleDisplayedFinish(long timestamp, int event, ArrayMap<String, String> appInfo) {
        int thisIntTime;
        AwareLog.d(TAG, "handleDisplayedFinish");
        if (!MemoryFeature2.isUpMemoryFeature.get() || AwareConstant.CURRENT_USER_TYPE != 3) {
            return -1;
        }
        try {
            String activityName = appInfo.get("activityName");
            int pid = Integer.parseInt(appInfo.get("pid"));
            long thisTime = Long.parseLong(appInfo.get("displayedTime"));
            if (activityName == null) {
                return -1;
            }
            if (thisTime < 0) {
                thisIntTime = 0;
            } else if (thisTime > 2147483647L) {
                thisIntTime = Integer.MAX_VALUE;
            } else {
                thisIntTime = (int) thisTime;
            }
            AwareLog.d(TAG, "activity displayed time : " + activityName + " , " + pid + ", " + thisIntTime);
            MemoryUtils.sendActivityDisplayedTime(activityName, pid, thisIntTime);
            return 0;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "handleDisplayedFinish get pid or time failed");
            return -1;
        }
    }

    private Bundle createBundleFromAppInfo(int uid, String appName) {
        Bundle extras = new Bundle();
        if (uid >= 0) {
            extras.putInt("appUid", uid);
        }
        extras.putString("appName", appName);
        return extras;
    }

    private int traceProcess(boolean launched, String reason, ArrayMap<String, String> appInfo, long timestamp) {
        ArrayMap<String, String> arrayMap = appInfo;
        if (TextUtils.isEmpty(reason)) {
            return -1;
        }
        try {
            String packageName = arrayMap.get("packageName");
            String processName = arrayMap.get("processName");
            Integer uid = Integer.valueOf(Integer.parseInt(arrayMap.get("uid")));
            PackageTracker tracker = PackageTracker.getInstance();
            if (launched) {
                tracker.addStartRecord(reason, packageName, uid.intValue(), processName, timestamp);
                if (AwareConstant.CURRENT_USER_TYPE == 3) {
                    try {
                        if ("activity".equals(reason) && packageName != null && processName != null) {
                            try {
                                this.mFgPkgName = packageName;
                                BigDataStore.getInstance().coldLaunch++;
                                AwareLog.d(TAG, "pkg: " + packageName + ", coldLaunch: " + BigDataStore.getInstance().coldLaunch);
                                AwareAppMngDFX.getInstance().trackeAppStartInfo(packageName, processName, 10);
                            } catch (NumberFormatException e) {
                                AwareLog.e(TAG, "failed to get uid");
                                return -1;
                            }
                        }
                    } catch (NumberFormatException e2) {
                        AwareLog.e(TAG, "failed to get uid");
                        return -1;
                    }
                } else {
                    String str = reason;
                }
            } else {
                tracker.addExitRecord(reason, packageName, uid.intValue(), processName, timestamp);
            }
            return 0;
        } catch (NumberFormatException e3) {
            String str2 = reason;
            AwareLog.e(TAG, "failed to get uid");
            return -1;
        }
    }

    private void setActivityLaunching(boolean status) {
        if (status) {
            this.mIsActivityLaunching = true;
            this.mAcvityLaunchBeginTimestamp = SystemClock.uptimeMillis();
            return;
        }
        this.mIsActivityLaunching = false;
    }

    private boolean isFirstBooting(String appName) {
        if (!this.mDMEServer.isFirstBooting()) {
            return false;
        }
        if (!isLauncher(appName)) {
            return true;
        }
        this.mDMEServer.firstBootingFinish();
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isLauncher(String packageName) {
        if (Process.myUid() != 1000 || packageName == null || packageName.trim().isEmpty()) {
            return false;
        }
        if (!GestureNavConst.DEFAULT_LAUNCHER_PACKAGE.equals(packageName) && !AwareAppAssociate.getInstance().getDefaultHomePackages().contains(packageName)) {
            return false;
        }
        return true;
    }

    static boolean isInCallActivity(String pkgName) {
        return "com.android.incallui".equals(pkgName);
    }
}
