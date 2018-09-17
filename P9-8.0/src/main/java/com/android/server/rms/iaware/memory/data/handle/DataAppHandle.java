package com.android.server.rms.iaware.memory.data.handle;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.emcom.SmartcareConstants;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.feature.MemoryFeature2;
import com.android.server.rms.iaware.memory.action.GpuCompressAction;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.rms.iaware.memory.utils.PrereadUtils;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMServiceImpl;
import java.util.ArrayList;
import java.util.List;

public class DataAppHandle extends AbsDataHandle {
    private static final long ACTIVITY_START_TIMEOUT = 5000;
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String TAG = "AwareMem_AppHandle";
    private static DataAppHandle sDataHandle;
    private long mAcvityLaunchBeginTimestamp = 0;
    private String mFgPkgName = null;
    private HwActivityManagerService mHwAMS = HwActivityManagerService.self();
    private boolean mIsActivityLaunching = false;
    private boolean mIsInBigMemoryMode = false;
    private String mLastPrereadPkg = null;
    private TrimMemoryHandler mTrimHandler = new TrimMemoryHandler(this, null);

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

        /* synthetic */ TrimMemoryHandler(DataAppHandle this$0, TrimMemoryHandler -this1) {
            this();
        }

        private TrimMemoryHandler() {
            this.mLastPid = Integer.valueOf(0);
            this.mLastPkgName = null;
            this.waitForOnTrimPids = new ArrayList();
        }

        private void interuptTrimMemoryForPid(int curPid) {
            synchronized (this.waitForOnTrimPids) {
                for (int i = this.waitForOnTrimPids.size() - 1; i >= 0; i--) {
                    OnTrimTask tsk = (OnTrimTask) this.waitForOnTrimPids.get(i);
                    if (tsk.mPid == curPid) {
                        AwareLog.i(DataAppHandle.TAG, "interuptTrimMemoryForPid! curPid:" + curPid + " tPid:" + tsk.mPid);
                        this.waitForOnTrimPids.remove(i);
                    }
                }
            }
        }

        private void doTrimMemory(int curPid, String pkgName) {
            if (DataAppHandle.this.mHwAMS != null) {
                if (!DataAppHandle.this.mHwAMS.isLauncher(pkgName)) {
                    interuptTrimMemoryForPid(curPid);
                }
                if (this.mLastPkgName != null && this.mLastPid.intValue() != 0 && !DataAppHandle.SYSTEM_UI.equals(this.mLastPkgName) && !DataAppHandle.this.mHwAMS.isLauncher(this.mLastPkgName) && this.mLastPid.intValue() != curPid) {
                    synchronized (this.waitForOnTrimPids) {
                        this.waitForOnTrimPids.add(new OnTrimTask(this.mLastPid.intValue(), SystemClock.elapsedRealtime()));
                    }
                    Message message = Message.obtain();
                    message.arg1 = this.mLastPid.intValue();
                    message.what = 100;
                    sendMessageDelayed(message, 3000);
                }
            }
        }

        private void saveLastActivityInfo(int pid, String pkgName) {
            this.mLastPkgName = pkgName;
            this.mLastPid = Integer.valueOf(pid);
        }

        /* JADX WARNING: Missing block: B:11:0x0034, code:
            if (r1 == 0) goto L_?;
     */
        /* JADX WARNING: Missing block: B:12:0x0036, code:
            com.android.server.rms.iaware.memory.utils.MemoryUtils.trimMemory(com.android.server.rms.iaware.memory.data.handle.DataAppHandle.-get0(r8.this$0), java.lang.String.valueOf(r1), 40);
     */
        /* JADX WARNING: Missing block: B:18:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:19:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    int pid = 0;
                    synchronized (this.waitForOnTrimPids) {
                        if (this.waitForOnTrimPids.size() > 0) {
                            OnTrimTask ott = (OnTrimTask) this.waitForOnTrimPids.get(0);
                            if (SystemClock.elapsedRealtime() - ott.mTime >= 3000) {
                                pid = ott.mPid;
                                this.waitForOnTrimPids.remove(0);
                                break;
                            }
                        }
                        return;
                    }
                    break;
                default:
                    return;
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
        switch (event) {
            case 15001:
                result = handleProcessBegin(timestamp, event, appInfo);
                break;
            case 15005:
                result = handleActivityBegin(timestamp, event, appInfo);
                break;
            case 15010:
                result = handleAppPrepareMem(timestamp, event, appInfo);
                break;
            case 15013:
                result = handleDisplayedBegin(timestamp, event, appInfo);
                break;
            case 85003:
                result = handleProcessExitFinish(timestamp, event, appInfo);
                break;
            case 85005:
                result = handleActivityFinish(timestamp, event, appInfo);
                break;
            case 85013:
                result = handleDisplayedFinish(timestamp, event, appInfo);
                break;
        }
        return result;
    }

    private int handleAppPrepareMem(long timestamp, int event, ArrayMap<String, String> appInfo) {
        if (!MemoryFeature2.isUpMemoryFeature.get()) {
            return -1;
        }
        Bundle extras = new Bundle();
        try {
            int reqMemKB = (Integer.valueOf(Integer.parseInt((String) appInfo.get("requestMem"))).intValue() & 65535) * 1024;
            if (reqMemKB <= 0) {
                AwareLog.w(TAG, "handleAppPrepareMem error  reqMemKB " + reqMemKB);
                return -1;
            }
            Integer uid = Integer.valueOf(Integer.valueOf(Integer.parseInt((String) appInfo.get("uid"))).intValue() % LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS);
            int sysCameraUid = MemoryConstant.getSystemCameraUid();
            if (uid.intValue() == 0 || sysCameraUid == 0 || uid.intValue() != sysCameraUid) {
                AwareLog.i(TAG, "invalid uid:" + uid + " camerauid:" + sysCameraUid);
                return -1;
            }
            AwareLog.i(TAG, "handleAppPrepareMem reqMemKB " + reqMemKB + " KB");
            extras.putLong("reqMem", (long) reqMemKB);
            this.mDMEServer.execute(MemoryConstant.MEM_SCENE_BIGMEM, extras, event, timestamp);
            return 0;
        } catch (NumberFormatException e) {
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

    private int handleActivityBegin(long timestamp, int event, ArrayMap<String, String> appInfo) {
        String appName = (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
        String activityName = (String) appInfo.get("activityName");
        int uid = -1;
        try {
            uid = Integer.parseInt((String) appInfo.get("uid"));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "uid is not right");
        }
        dispatchPrereadMsg(appName, uid);
        if (this.mHwAMS == null || !this.mHwAMS.isLauncher(appName)) {
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
                this.mDMEServer.stopExecute(timestamp, event);
            }
            mayNeedEnterSpecialScene(appName, activityName);
            return 0;
        }
        mayNeedExitSpecialScene();
        return 0;
    }

    private void dispatchPrereadMsg(String appName, int uid) {
        if (appName != null && !appName.equals(this.mLastPrereadPkg)) {
            this.mLastPrereadPkg = appName;
            if (!(SYSTEM_UI.equals(appName) || this.mHwAMS == null || (this.mHwAMS.isLauncher(appName) ^ 1) == 0)) {
                if (MemoryConstant.getCameraPrereadFileMap().containsKey(appName)) {
                    PrereadUtils.getInstance();
                    PrereadUtils.sendPrereadMsg(appName);
                } else if (this.mHwAMS.numOfPidWithActivity(uid) == 0) {
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
        String appName = (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
        String activityName = (String) appInfo.get("activityName");
        int uid = -1;
        try {
            uid = Integer.parseInt((String) appInfo.get("uid"));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "uid is not right");
        }
        handleActivityFinishDFX(appInfo, appName);
        if (MemoryFeature2.isUpMemoryFeature.get() && MemoryConstant.getConfigGmcSwitch() != 0) {
            int pid = 0;
            try {
                pid = Integer.parseInt((String) appInfo.get("pid"));
            } catch (NumberFormatException e2) {
                AwareLog.e(TAG, "pid is not right");
            }
            if (pid != 0) {
                this.mTrimHandler.doTrimMemory(pid, appName);
                this.mTrimHandler.saveLastActivityInfo(pid, appName);
            }
            if (!(uid < 0 || this.mHwAMS == null || (this.mHwAMS.isLauncher(appName) ^ 1) == 0)) {
                GpuCompressAction.removeUidFromGMCMap(uid);
            }
        }
        if (BigMemoryInfo.getInstance().isBigMemoryApp(appName) || BigMemoryInfo.getInstance().isBigMemoryApp(activityName)) {
            MemoryConstant.enableBigMemCriticalMemory();
        } else {
            MemoryConstant.disableBigMemCriticalMemory();
            mayNeedExitSpecialScene();
        }
        if (this.mHwAMS != null && this.mHwAMS.isLauncher(appName)) {
            return 0;
        }
        Bundle extras = createBundleFromAppInfo(uid, appName);
        AwareLog.d(TAG, "app event " + appName + " activity finish");
        setActivityLaunching(false);
        this.mDMEServer.execute(MemoryConstant.MEM_SCENE_DEFAULT, extras, event, timestamp);
        return 0;
    }

    private void handleActivityFinishDFX(ArrayMap<String, String> appInfo, String appName) {
        if (AwareConstant.CURRENT_USER_TYPE == 3 && appName != null) {
            String processName = (String) appInfo.get("processName");
            if (processName != null) {
                AwareAppMngDFX.getInstance().trackeAppStartInfo(appName, processName, 11);
                if (!(SYSTEM_UI.equals(appName) || this.mHwAMS == null || (this.mHwAMS.isLauncher(appName) ^ 1) == 0 || appName.equals(this.mFgPkgName))) {
                    BigDataStore instance = BigDataStore.getInstance();
                    instance.warmLaunch++;
                    AwareLog.d(TAG, "pkg: " + appName + ", warmLaunch: " + BigDataStore.getInstance().warmLaunch);
                }
                this.mFgPkgName = appName;
                AwareLog.d(TAG, "fgPkg: " + this.mFgPkgName);
            }
        }
    }

    private int handleProcessBegin(long timestamp, int event, ArrayMap<String, String> appInfo) {
        AwareLog.d(TAG, "app event process launch begin");
        return traceProcess(true, (String) appInfo.get("launchMode"), appInfo, timestamp);
    }

    private int handleProcessExitFinish(long timestamp, int event, ArrayMap<String, String> appInfo) {
        AwareLog.d(TAG, "app event process exit finish");
        return traceProcess(false, (String) appInfo.get("exitMode"), appInfo, timestamp);
    }

    private int handleDisplayedBegin(long timestamp, int event, ArrayMap<String, String> appInfo) {
        AwareLog.d(TAG, "handleDisplayedBegin");
        if (!MemoryFeature2.isUpMemoryFeature.get() || AwareConstant.CURRENT_USER_TYPE != 3) {
            return -1;
        }
        try {
            String activityName = (String) appInfo.get("activityName");
            int pid = Integer.parseInt((String) appInfo.get("pid"));
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
        AwareLog.d(TAG, "handleDisplayedFinish");
        if (!MemoryFeature2.isUpMemoryFeature.get() || AwareConstant.CURRENT_USER_TYPE != 3) {
            return -1;
        }
        try {
            String activityName = (String) appInfo.get("activityName");
            int pid = Integer.parseInt((String) appInfo.get("pid"));
            long thisTime = Long.parseLong((String) appInfo.get("displayedTime"));
            if (activityName == null) {
                return -1;
            }
            int thisIntTime;
            if (thisTime < 0) {
                thisIntTime = 0;
            } else if (thisTime > 2147483647L) {
                thisIntTime = SmartcareConstants.INVALID;
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
        if (TextUtils.isEmpty(reason)) {
            return -1;
        }
        try {
            String packageName = (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
            String processName = (String) appInfo.get("processName");
            Integer uid = Integer.valueOf(Integer.parseInt((String) appInfo.get("uid")));
            if (AwareConstant.CURRENT_USER_TYPE == 3 && "activity".equals(reason) && packageName != null && processName != null) {
                this.mFgPkgName = packageName;
                BigDataStore instance = BigDataStore.getInstance();
                instance.coldLaunch++;
                AwareLog.d(TAG, "pkg: " + packageName + ", coldLaunch: " + BigDataStore.getInstance().coldLaunch);
                AwareAppMngDFX.getInstance().trackeAppStartInfo(packageName, processName, 10);
            }
            PackageTracker tracker = PackageTracker.getInstance();
            if (launched) {
                tracker.addStartRecord(reason, packageName, uid.intValue(), processName, timestamp);
            } else {
                tracker.addExitRecord(reason, packageName, uid.intValue(), processName, timestamp);
            }
            return 0;
        } catch (NumberFormatException e) {
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
}
