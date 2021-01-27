package com.android.server.rms.iaware.memory.data.handle;

import android.iawareperf.UniPerf;
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
import com.android.server.am.ProcessRecordEx;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.appmng.ActivityEventManager;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppMngDfx;
import com.android.server.rms.iaware.feature.AppQuickStartFeature;
import com.android.server.rms.iaware.feature.MemoryFeatureEx;
import com.android.server.rms.iaware.feature.SceneRecogFeature;
import com.android.server.rms.iaware.memory.action.GpuCompressAction;
import com.android.server.rms.iaware.memory.action.ReclaimAction;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.BigMemoryConstant;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.rms.iaware.memory.utils.PrereadUtils;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import com.android.server.rms.memrepair.ProcStateStatisData;
import com.android.server.rms.memrepair.SystemAppMemRepairMng;
import com.huawei.server.rme.hyperhold.SceneProcessing;
import com.huawei.server.rme.hyperhold.Swap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DataAppHandle extends AbsDataHandle {
    private static final long ACTIVITY_START_TIMEOUT = 5000;
    private static final int COMPRESS_MEM_RATIO = 3;
    private static final int K_PER_M = 1024;
    private static final String LAUNCH_MODE_PRELOAD = "start application";
    private static final Object LOCK = new Object();
    private static final int OUT_USS_LENGTH = 2;
    private static final int REQ_MEM_MASK = 65535;
    private static final int SCAN_MODE = 30101;
    private static final String TAG = "AwareMem_AppHandle";
    private static DataAppHandle sDataHandle;
    private long mAcvityLaunchBeginTimestamp;
    private AppHandler mAppHandler;
    private BigMemoryInfo mBigMemInfo;
    private String mFgPkgName;
    private int mFgPkgUid;
    private HwActivityManagerService mHwAms;
    private boolean mIsActivityLaunching;
    private boolean mIsInBigMemoryMode;
    private boolean mIsInScanMode;
    private String mLastPrereadPkg;
    private LastProcInfo mLastProcInfo;
    private String mStartingPkgName;
    private TrimMemoryHandler mTrimHandler;

    private DataAppHandle() {
        this.mIsActivityLaunching = false;
        this.mIsInBigMemoryMode = false;
        this.mIsInScanMode = false;
        this.mAcvityLaunchBeginTimestamp = 0;
        this.mFgPkgUid = 0;
        this.mFgPkgName = null;
        this.mStartingPkgName = null;
        this.mLastPrereadPkg = null;
        this.mLastProcInfo = new LastProcInfo();
        this.mBigMemInfo = null;
        this.mBigMemInfo = BigMemoryInfo.getInstance();
        this.mHwAms = HwActivityManagerService.self();
        this.mTrimHandler = new TrimMemoryHandler();
        this.mAppHandler = new AppHandler();
    }

    public static DataAppHandle getInstance() {
        DataAppHandle dataAppHandle;
        synchronized (LOCK) {
            if (sDataHandle == null) {
                sDataHandle = new DataAppHandle();
            }
            dataAppHandle = sDataHandle;
        }
        return dataAppHandle;
    }

    @Override // com.android.server.rms.iaware.memory.data.handle.AbsDataHandle
    public int reportData(long timeStamp, int event, AttrSegments attrSegments) {
        ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
        if (appInfo == null) {
            AwareLog.w(TAG, "appInfo is null!");
            return -1;
        } else if (event == 15001) {
            return handleProcessBegin(timeStamp, event, appInfo);
        } else {
            if (event == 15005) {
                return handleActivityBegin(timeStamp, event, appInfo);
            }
            if (event == 15010) {
                return handleAppPrepareMem(timeStamp, event, appInfo);
            }
            if (event == 15013) {
                return handleDisplayedBegin(timeStamp, event, appInfo);
            }
            if (event == 15019) {
                return handleAppActivityIn(appInfo);
            }
            if (event == 85003) {
                return handleProcessExitFinish(timeStamp, event, appInfo);
            }
            if (event == 85005) {
                return handleActivityFinish(timeStamp, event, appInfo);
            }
            if (event != 85013) {
                return -1;
            }
            return handleDisplayedFinish(timeStamp, event, appInfo);
        }
    }

    private int handleAppPrepareMem(long timeStamp, int event, ArrayMap<String, String> appInfo) {
        if (!MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get()) {
            return -1;
        }
        Bundle extras = new Bundle();
        try {
            long reqMemKb = (long) ((Integer.valueOf(Integer.parseInt(appInfo.get("requestMem"))).intValue() & REQ_MEM_MASK) * 1024);
            if (reqMemKb <= 0) {
                AwareLog.w(TAG, "handleAppPrepareMem error reqMemKB " + reqMemKb);
                return -1;
            }
            Integer uid = Integer.valueOf(Integer.valueOf(Integer.parseInt(appInfo.get("uid"))).intValue() % 100000);
            if (uid.intValue() != MemoryConstant.getSystemCameraUid()) {
                if (uid.intValue() != 1000) {
                    AwareLog.i(TAG, "invalid uid:" + uid);
                    return -1;
                }
            }
            extras.putLong("reqMem", reqMemKb);
            extras.putInt("appUid", uid.intValue());
            AwareLog.i(TAG, "handleAppPrepareMem reqMemKB " + reqMemKb + "kb");
            try {
                this.mDmeServer.execute(MemoryConstant.MEM_SCENE_BIGMEM, extras, event, timeStamp);
                if (!Swap.getInstance().isSwapEnabled()) {
                    return 0;
                }
                SceneProcessing.getInstance().notifyAppReqMem(reqMemKb);
                return 0;
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "reqMem is not right");
                return -1;
            }
        } catch (NumberFormatException e2) {
            AwareLog.e(TAG, "reqMem is not right");
            return -1;
        }
    }

    public boolean isActivityLaunching() {
        long endTimeStamp = SystemClock.uptimeMillis();
        if (!this.mIsActivityLaunching || endTimeStamp - this.mAcvityLaunchBeginTimestamp >= ACTIVITY_START_TIMEOUT) {
            return false;
        }
        return true;
    }

    public Bundle createBundleFromAppInfo() {
        return createBundleFromAppInfo(this.mFgPkgUid, this.mStartingPkgName);
    }

    private Bundle createBundleFromAppInfo(int uid, String appName) {
        Bundle extras = new Bundle();
        if (uid >= 0) {
            extras.putInt("appUid", uid);
        }
        extras.putString("appName", appName);
        return extras;
    }

    private void mayNeedExitSpecialScene() {
        if (this.mIsInBigMemoryMode) {
            UniPerf.getInstance().uniPerfEvent(13250, "", new int[]{-1});
            this.mIsInBigMemoryMode = false;
        }
    }

    private void mayNeedEnterSpecialScene(int uid, String appName, String activityName) {
        if (!"com.huawei.camera".equals(appName)) {
            if (BigMemoryInfo.getInstance().isBigMemoryApp(activityName) || BigMemoryInfo.getInstance().isBigMemoryApp(uid, activityName)) {
                this.mIsInBigMemoryMode = true;
                UniPerf.getInstance().uniPerfEvent(13250, "", new int[]{0});
                return;
            }
            mayNeedExitSpecialScene();
        }
    }

    private void interruptReclaimIfNeeded(String packageName, int pid) {
        if (MemoryConstant.getCameraPreloadSwitch() == 1 && "com.huawei.camera".equals(packageName)) {
            MemoryUtils.reclaimProcessAll(pid, true);
        }
    }

    /* access modifiers changed from: private */
    public static final class ActivityInfo {
        public String activityName;
        public String appName;
        public int pid;
        public String procName;
        public int uid;

        ActivityInfo(int myUid, int myPid, String myAppName, String myProcName, String myActivityName) {
            this.uid = myUid;
            this.pid = myPid;
            this.appName = myAppName;
            this.procName = myProcName;
            this.activityName = myActivityName;
        }
    }

    private void handleActivityStartPost(long timeStamp, int event, ActivityInfo activity) {
        if (MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get() && MemoryConstant.getConfigGmcSwitch() != 0 && activity.uid >= 0) {
            GpuCompressAction.removeUidFromGmcMap(activity.uid);
        }
        Bundle extras = createBundleFromAppInfo(activity.uid, activity.appName);
        if (BigMemoryInfo.getInstance().isBigMemoryApp(activity.activityName)) {
            extras.putString("appName", activity.activityName);
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "app event " + activity.appName + " activity begin");
        }
        setActivityLaunching(true);
        String activityNameTemp = this.mBigMemInfo.cutActivityNameIfNeed(activity.activityName);
        processActivityStart(isBigMem(activityNameTemp, activity.appName, extras), extras, event, timeStamp, getBundle(activity.appName, activity.uid, activity.pid, activity.procName));
        mayNeedEnterSpecialScene(activity.uid, activity.appName, activityNameTemp);
    }

    private int handleActivityBegin(long timeStamp, int event, ArrayMap<String, String> appInfo) {
        int uid = -1;
        int pid = -1;
        try {
            uid = Integer.parseInt(appInfo.get("uid"));
            pid = Integer.parseInt(appInfo.get(SceneRecogFeature.DATA_PID));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "uid is not right");
        }
        String appName = appInfo.get("packageName");
        String activityName = appInfo.get("activityName");
        if (isFirstBooting(appName)) {
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "booting " + appName + "," + activityName + " activity begin");
            }
            return 0;
        }
        this.mFgPkgUid = uid;
        this.mStartingPkgName = appName;
        this.mAppHandler.reset();
        interruptReclaimIfNeeded(appName, pid);
        if (!AppQuickStartFeature.isExactPrereadFeatureEnable()) {
            dispatchPrereadMsg(appName, uid);
        }
        MemoryConstant.resetTotalRequestMemory();
        SystemAppMemRepairMng.getInstance().interrupt(true);
        if (isLauncher(appName)) {
            mayNeedExitSpecialScene();
            this.mAppHandler.doStartLaunch();
            return 0;
        }
        if (MemoryConstant.isKernCompressEnable() || MemoryUtils.shouldPauseGpuCompress()) {
            MemoryUtils.rccPause();
        }
        if (MemoryConstant.getPressureReclaimSwitch() == 1) {
            MemoryUtils.setBalanceSwappiness();
        }
        if (isInCallActivity(appName)) {
            return 0;
        }
        handleActivityStartPost(timeStamp, event, new ActivityInfo(uid, pid, appName, appInfo.get(MemoryConstant.MEM_NATIVE_ITEM_PROCESSNAME), activityName));
        checkSceneMode(activityName);
        return 0;
    }

    private int handleAppActivityIn(ArrayMap<String, String> appInfo) {
        checkSceneMode(appInfo.get("activityName").replaceAll(".*?/(?=[^.])|/(?=.)", ""));
        return 0;
    }

    private void checkSceneMode(String activityName) {
        if (MemoryConstant.isConfigScanModeOptSwitch()) {
            int sceneMode = ActivityEventManager.getInstance().getSceneMode(activityName);
            if (!this.mIsInScanMode && sceneMode == SCAN_MODE) {
                setCameraScanMode(SCAN_MODE);
                this.mIsInScanMode = true;
            } else if (this.mIsInScanMode && sceneMode != SCAN_MODE) {
                setCameraScanMode(-1);
                this.mIsInScanMode = false;
            }
        }
    }

    private void setCameraScanMode(int mode) {
        try {
            Object obj = Class.forName("com.huawei.hwpostcamera.HwPostCamera").newInstance();
            Method method = obj.getClass().getDeclaredMethod("nativeSetScanMode", Integer.TYPE);
            method.setAccessible(true);
            method.invoke(obj, Integer.valueOf(mode));
            AwareLog.i(TAG, "setCameraScanMode mode = " + mode);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            AwareLog.e(TAG, "setCameraScanMode failed.");
        }
    }

    private Bundle getBundle(String appName, int uid, int pid, String procName) {
        Bundle bundle = new Bundle();
        bundle.putString("appName", appName);
        bundle.putInt("uid", uid);
        bundle.putInt(SceneRecogFeature.DATA_PID, pid);
        bundle.putString("procName", procName);
        return bundle;
    }

    private void processActivityStart(boolean isBigMem, Bundle extras, int event, long timeStamp, Bundle bundle) {
        if (isBigMem) {
            if (MemoryConstant.isBigMemEnableDmeSwitchOn()) {
                this.mDmeServer.enable();
            }
            MemoryConstant.enableBigMemCriticalMemory();
            EventTracker.getInstance().trackEvent(1000, event, timeStamp, null);
            this.mDmeServer.execute(MemoryConstant.MEM_SCENE_BIGMEM, extras, event, timeStamp);
            return;
        }
        MemoryConstant.disableBigMemCriticalMemory();
        this.mDmeServer.stopExecute(timeStamp, event);
        if (bundle == null) {
            AwareLog.w(TAG, "processActivityStart bundle is null");
        } else if (this.mLastProcInfo.isEmerg(bundle.getInt("uid"), bundle.getInt(SceneRecogFeature.DATA_PID), bundle.getString("appName"), bundle.getString("procName"))) {
            extras.putLong("appMem", this.mLastProcInfo.mReqMem);
            extras.putBoolean("immediate", true);
            this.mDmeServer.execute(MemoryConstant.MEM_SCENE_DEFAULT, extras, event, timeStamp);
        }
    }

    private boolean isBigMem(String activityName, String appName, Bundle extras) {
        this.mBigMemInfo.processActivityBegin(activityName);
        if (this.mBigMemInfo.isBigMemoryApp(appName) || this.mBigMemInfo.isBigMemoryApp(activityName)) {
            return true;
        }
        if (!this.mBigMemInfo.isBigMemFeatureOn()) {
            return false;
        }
        if (extras == null) {
            AwareLog.w(TAG, "processActivityStart extras is null");
            return false;
        }
        if (!this.mBigMemInfo.isBigMemoryApp(extras.getInt("appUid"), activityName)) {
            return false;
        }
        extras.putString("appName", activityName);
        return true;
    }

    private void dispatchPrereadMsg(String appName, int uid) {
        if (appName != null && !appName.equals(this.mLastPrereadPkg)) {
            this.mLastPrereadPkg = appName;
            if (!MemoryConstant.SYSTEM_UI_PACKAGE_NAME.equals(appName) && this.mHwAms != null && !isLauncher(appName)) {
                if (MemoryConstant.getCameraPrereadFileMap().containsKey(appName)) {
                    PrereadUtils.getInstance();
                    PrereadUtils.sendPrereadMsg(appName);
                } else if (this.mHwAms.numOfPidWithActivity(uid) == 0) {
                    PrereadUtils.getInstance();
                    if (PrereadUtils.addPkgFilesIfNecessary(appName)) {
                        PrereadUtils.getInstance();
                        PrereadUtils.sendPrereadMsg(appName);
                    }
                }
            }
        }
    }

    private int handleActivityFinish(long timeStamp, int event, ArrayMap<String, String> appInfo) {
        String appName = appInfo.get("packageName");
        String activityName = appInfo.get("activityName");
        int uid = -1;
        try {
            uid = Integer.parseInt(appInfo.get("uid"));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "uid is not right");
        }
        this.mBigMemInfo.processActivityFinish(uid, appName, activityName);
        if (isFirstBooting(appName)) {
            return 0;
        }
        this.mAppHandler.reset();
        handleActivityFinishDfx(appInfo, appName);
        handleFinishMemory(appInfo, appName, uid);
        processActivityFinishBigMem(appName, activityName, uid);
        if (MemoryConstant.getPressureReclaimSwitch() == 1) {
            MemoryUtils.restoreSwappiness();
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
                this.mDmeServer.execute(MemoryConstant.MEM_SCENE_DEFAULT, extras, event, timeStamp);
            }
            this.mLastProcInfo.reset();
            return 0;
        }
    }

    private void handleFinishMemory(ArrayMap<String, String> appInfo, String appName, int uid) {
        if (MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get()) {
            int pid = 0;
            try {
                pid = Integer.parseInt(appInfo.get(SceneRecogFeature.DATA_PID));
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "pid is not right");
            }
            if (pid != 0 && MemoryConstant.isAppTrimEnabled()) {
                this.mTrimHandler.doTrimMemory(pid, appName);
                this.mTrimHandler.saveLastActivityInfo(pid, appName);
            }
            if (uid >= 0 && !isLauncher(appName) && MemoryConstant.getConfigGmcSwitch() != 0) {
                GpuCompressAction.removeUidFromGmcMap(uid);
            }
        }
    }

    private void processActivityFinishBigMem(String appName, String activityName, int uid) {
        if (this.mBigMemInfo.isBigMemoryApp(appName) || this.mBigMemInfo.isBigMemoryApp(activityName) || (this.mBigMemInfo.isBigMemFeatureOn() && this.mBigMemInfo.isBigMemoryApp(uid, activityName))) {
            MemoryConstant.enableBigMemCriticalMemory();
            return;
        }
        MemoryConstant.disableBigMemCriticalMemory();
        mayNeedExitSpecialScene();
    }

    private void handleActivityFinishDfx(ArrayMap<String, String> appInfo, String appName) {
        String processName;
        if (AwareConstant.CURRENT_USER_TYPE == 3 && appName != null && (processName = appInfo.get(MemoryConstant.MEM_NATIVE_ITEM_PROCESSNAME)) != null) {
            AwareAppMngDfx.getInstance().trackeAppStartInfo(appName, processName, 11);
            if (!MemoryConstant.SYSTEM_UI_PACKAGE_NAME.equals(appName) && !isLauncher(appName) && !appName.equals(this.mFgPkgName)) {
                BigDataStore.getInstance().warmLaunch++;
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "pkg: " + appName + ", warmLaunch: " + BigDataStore.getInstance().warmLaunch);
                }
            }
            this.mFgPkgName = appName;
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "fgPkg: " + this.mFgPkgName);
            }
        }
    }

    private int handleProcessBegin(long timeStamp, int event, ArrayMap<String, String> appInfo) {
        ProcessRecordEx app;
        AwareLog.d(TAG, "app event process launch begin");
        String launchMode = appInfo.get("launchMode");
        String packageName = appInfo.get("packageName");
        if (MemoryConstant.getCameraPreloadSwitch() == 1 && "com.huawei.camera".equals(packageName) && LAUNCH_MODE_PRELOAD.equals(launchMode)) {
            try {
                String processName = appInfo.get(MemoryConstant.MEM_NATIVE_ITEM_PROCESSNAME);
                int uid = Integer.parseInt(appInfo.get("uid"));
                if (!(processName == null || this.mHwAms == null || (app = this.mHwAms.getProcessRecordEx(processName, uid, true)) == null || app.isProcessRecordNull())) {
                    ReclaimAction.reclaimProcessAll(this.mTrimHandler, app);
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "handleProcessBegin get uid number format err");
            }
        }
        return traceProcess(true, launchMode, appInfo, timeStamp);
    }

    private int handleProcessExitFinish(long timeStamp, int event, ArrayMap<String, String> appInfo) {
        AwareLog.d(TAG, "app event process exit finish");
        return traceProcess(false, appInfo.get("exitMode"), appInfo, timeStamp);
    }

    private int handleDisplayedBegin(long timeStamp, int event, ArrayMap<String, String> appInfo) {
        AwareLog.d(TAG, "handleDisplayedBegin");
        if (!MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get() || AwareConstant.CURRENT_USER_TYPE != 3) {
            return -1;
        }
        try {
            String activityName = appInfo.get("activityName");
            int pid = Integer.parseInt(appInfo.get(SceneRecogFeature.DATA_PID));
            if (activityName == null) {
                return -1;
            }
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "activity displayed time : " + activityName + " , " + pid + ", 0");
            }
            MemoryUtils.sendActivityDisplayedTime(activityName, pid, 0);
            return 0;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "handleDisplayedBegin get pid or time failed");
            return -1;
        }
    }

    private int handleDisplayedFinish(long timeStamp, int event, ArrayMap<String, String> appInfo) {
        int thisIntTime;
        AwareLog.d(TAG, "handleDisplayedFinish");
        if (!MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get() || AwareConstant.CURRENT_USER_TYPE != 3) {
            return -1;
        }
        try {
            String activityName = appInfo.get("activityName");
            int pid = Integer.parseInt(appInfo.get(SceneRecogFeature.DATA_PID));
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
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "activity displayed time : " + activityName + " , " + pid + ", " + thisIntTime);
            }
            MemoryUtils.sendActivityDisplayedTime(activityName, pid, thisIntTime);
            return 0;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "handleDisplayedFinish get pid or time failed");
            return -1;
        }
    }

    private int traceProcess(boolean launched, String reason, ArrayMap<String, String> appInfo, long timeStamp) {
        if (TextUtils.isEmpty(reason)) {
            return -1;
        }
        try {
            String packageName = appInfo.get("packageName");
            String processName = appInfo.get(MemoryConstant.MEM_NATIVE_ITEM_PROCESSNAME);
            Integer uid = Integer.valueOf(Integer.parseInt(appInfo.get("uid")));
            PackageTracker tracker = PackageTracker.getInstance();
            if (launched) {
                tracker.addStartRecord(reason, packageName, uid.intValue(), processName, timeStamp);
                if (AwareConstant.CURRENT_USER_TYPE != 3) {
                    return 0;
                }
                try {
                    if (!BigMemoryConstant.BIG_MEM_INFO_ITEM_TAG.equals(reason) || packageName == null || processName == null) {
                        return 0;
                    }
                    try {
                        this.mFgPkgName = packageName;
                        BigDataStore.getInstance().coldLaunch++;
                        if (AwareLog.getDebugLogSwitch()) {
                            AwareLog.d(TAG, "pkg: " + packageName + ", coldLaunch: " + BigDataStore.getInstance().coldLaunch);
                        }
                        AwareAppMngDfx.getInstance().trackeAppStartInfo(packageName, processName, 10);
                        return 0;
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "failed to get uid");
                        return -1;
                    }
                } catch (NumberFormatException e2) {
                    AwareLog.e(TAG, "failed to get uid");
                    return -1;
                }
            } else {
                tracker.addExitRecord(reason, packageName, uid.intValue(), processName, timeStamp);
                return 0;
            }
        } catch (NumberFormatException e3) {
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
        if (!this.mDmeServer.isFirstBooting()) {
            return false;
        }
        if (!isLauncher(appName)) {
            return true;
        }
        this.mDmeServer.firstBootingFinish();
        return false;
    }

    /* access modifiers changed from: private */
    public final class TrimMemoryHandler extends Handler {
        private static final int MSG_TRIM_MEMORY = 100;
        private static final int ON_TRIM_DELAY_TIME = 3000;
        private int mLastPid;
        private String mLastPkgName;
        private final List<OnTrimTask> waitForOnTrimPids;

        private TrimMemoryHandler() {
            this.waitForOnTrimPids = new ArrayList();
            this.mLastPid = 0;
            this.mLastPkgName = null;
        }

        /* access modifiers changed from: private */
        public class OnTrimTask {
            public int myPid;
            public long myTime;

            OnTrimTask(int pid, long time) {
                this.myPid = pid;
                this.myTime = time;
            }
        }

        private void interuptTrimMemoryForPid(int curPid) {
            synchronized (this.waitForOnTrimPids) {
                for (int i = this.waitForOnTrimPids.size() - 1; i >= 0; i--) {
                    OnTrimTask tsk = this.waitForOnTrimPids.get(i);
                    if (tsk.myPid == curPid) {
                        if (AwareLog.getDebugLogSwitch()) {
                            AwareLog.d(DataAppHandle.TAG, "interuptTrimMemoryForPid! curPid:" + curPid + " tPid:" + tsk.myPid);
                        }
                        this.waitForOnTrimPids.remove(i);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void doTrimMemory(int curPid, String pkgName) {
            if (!DataAppHandle.this.isLauncher(pkgName)) {
                interuptTrimMemoryForPid(curPid);
            }
            String str = this.mLastPkgName;
            if (str != null && this.mLastPid != 0 && !MemoryConstant.SYSTEM_UI_PACKAGE_NAME.equals(str) && !DataAppHandle.this.isLauncher(this.mLastPkgName) && this.mLastPid != curPid) {
                synchronized (this.waitForOnTrimPids) {
                    this.waitForOnTrimPids.add(new OnTrimTask(this.mLastPid, SystemClock.elapsedRealtime()));
                }
                Message message = Message.obtain();
                message.arg1 = this.mLastPid;
                message.what = 100;
                sendMessageDelayed(message, 3000);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void saveLastActivityInfo(int pid, String pkgName) {
            this.mLastPkgName = pkgName;
            this.mLastPid = pid;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                int pid = 0;
                synchronized (this.waitForOnTrimPids) {
                    if (this.waitForOnTrimPids.size() > 0) {
                        OnTrimTask ott = this.waitForOnTrimPids.get(0);
                        if (SystemClock.elapsedRealtime() - ott.myTime >= 3000) {
                            pid = ott.myPid;
                            this.waitForOnTrimPids.remove(0);
                        }
                    } else {
                        return;
                    }
                }
                if (pid != 0) {
                    MemoryUtils.trimMemory(DataAppHandle.this.mHwAms, String.valueOf(pid), 40);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class AppHandler extends Handler {
        private static final int LAUNCH_DELAY_TIME = 3000;
        private static final int MSG_LAUNCH_DELAY = 100;

        private AppHandler() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void doStartLaunch() {
            sendEmptyMessageDelayed(100, 3000);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reset() {
            removeMessages(100);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                DataAppHandle dataAppHandle = DataAppHandle.this;
                if (dataAppHandle.isLauncher(dataAppHandle.mStartingPkgName)) {
                    MemoryConstant.disableBigMemCriticalMemory();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isLauncher(String packageName) {
        if (Process.myUid() != 1000 || packageName == null || packageName.trim().isEmpty()) {
            return false;
        }
        if (!MemoryConstant.LAUNCHER_PACKAGE_NAME.equals(packageName) && !AwareAppAssociate.getInstance().getDefaultHomePackages().contains(packageName)) {
            return false;
        }
        return true;
    }

    private static boolean isInCallActivity(String pkgName) {
        return MemoryConstant.IN_CALL_UI_PACKAGE_NAME_PHONE.equals(pkgName) || "com.huawei.ohos.call".equals(pkgName);
    }

    /* access modifiers changed from: private */
    public static final class LastProcInfo {
        boolean mIsEmergMem;
        long mReqMem;

        private LastProcInfo() {
        }

        public boolean isEmerg() {
            return this.mIsEmergMem;
        }

        public boolean isEmerg(int uid, int pid, String packageName, String procName) {
            long j;
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
                            if (realReqMem < MemoryConstant.getMiddleWater()) {
                                j = realReqMem;
                            } else {
                                j = MemoryConstant.getMiddleWater();
                            }
                            this.mReqMem = j;
                            AwareLog.i(DataAppHandle.TAG, "isEmerg=" + procPss + ",procName=" + procName + ",realAvailMem=" + this.mReqMem);
                            this.mIsEmergMem = true;
                            return true;
                        }
                    }
                }
                if (!MemoryConstant.isFastKillSwitch() || availableRam >= MemoryConstant.getCriticalMemory()) {
                    return false;
                }
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(DataAppHandle.TAG, "isEmerg fast kill,packageName=" + packageName + ",procName=" + procName);
                }
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
                ResourceCollector.getPss(pid, outUss, (long[]) null);
                curUss = outUss[0] + (outUss[1] / 3);
                if (curUss == 0) {
                    curUss = MemoryReader.getPssForPid(pid);
                }
            }
            long historyUss = ProcStateStatisData.getInstance().getHistoryProcUss(uid, packageName, procName);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(DataAppHandle.TAG, "getUss=" + curUss + ",historyUss=" + historyUss + ",packageName=" + packageName + ",procName=" + procName);
            }
            if (historyUss <= 0 || historyUss < curUss) {
                return 0;
            }
            return historyUss - curUss;
        }
    }
}
