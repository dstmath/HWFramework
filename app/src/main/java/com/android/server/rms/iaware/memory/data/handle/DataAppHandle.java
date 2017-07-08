package com.android.server.rms.iaware.memory.data.handle;

import android.os.Bundle;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.policy.MemoryExecutorServer;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.security.trustcircle.IOTController;

public class DataAppHandle extends AbsDataHandle {
    private static final long ACTIVITY_START_TIMEOUT = 5000;
    private static final long EX_CAMERA_LAUNCHE_TIME = 2000;
    private static final String TAG = "AwareMem_AppHandle";
    private static DataAppHandle sDataHandle;
    private long mAcvityLaunchBeginTimestamp;
    private long mBigMemoryFinishedTimestamp;
    private HwActivityManagerService mHwAMS;
    private boolean mIsActivityLaunching;

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
            case 85003:
                result = handleProcessExitFinish(timestamp, event, appInfo);
                break;
            case 85005:
                result = handleActivityFinish(timestamp, event, appInfo);
                break;
        }
        return result;
    }

    public boolean isActivityLaunching() {
        long endTimeStamp = SystemClock.uptimeMillis();
        if (!this.mIsActivityLaunching || endTimeStamp - this.mAcvityLaunchBeginTimestamp >= ACTIVITY_START_TIMEOUT) {
            return false;
        }
        return true;
    }

    private DataAppHandle() {
        this.mIsActivityLaunching = false;
        this.mAcvityLaunchBeginTimestamp = 0;
        this.mBigMemoryFinishedTimestamp = 0;
        this.mHwAMS = HwActivityManagerService.self();
    }

    private int handleActivityBegin(long timestamp, int event, ArrayMap<String, String> appInfo) {
        String appName;
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            BigDataStore instance = BigDataStore.getInstance();
            instance.totalStartCount++;
            appName = (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
            AwareAppMngDFX.getInstance().trackeAppStartInfo(appName, (String) appInfo.get("processName"), 11);
        }
        appName = (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
        if (this.mHwAMS.isLauncher(appName)) {
            return 0;
        }
        Bundle extras = createBundleFromAppInfo(appInfo, appName);
        AwareLog.d(TAG, "app event " + appName + " activity begin");
        setActivityLaunching(true);
        if (BigMemoryInfo.getInstance().isBigMemoryApp(appName)) {
            enableBigMemCriticalMemory();
            EventTracker.getInstance().trackEvent(IOTController.TYPE_MASTER, event, timestamp, null);
            this.mDMEServer.execute(MemoryConstant.MEM_SCENE_BIGMEM, extras, event, timestamp);
        } else {
            disableBigMemCriticalMemory();
            this.mDMEServer.stopExecute(timestamp, event);
        }
        return 0;
    }

    private int handleActivityFinish(long timestamp, int event, ArrayMap<String, String> appInfo) {
        String appName = (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
        long now = SystemClock.uptimeMillis();
        if (BigMemoryInfo.getInstance().isBigMemoryApp(appName)) {
            enableBigMemCriticalMemory();
            setActivityLaunching(false);
            this.mBigMemoryFinishedTimestamp = now;
            MemoryExecutorServer.getInstance().setPollingPeriod(MemoryConstant.getDefaultTimerPeriod());
            return 0;
        } else if (now - this.mBigMemoryFinishedTimestamp < EX_CAMERA_LAUNCHE_TIME) {
            return 0;
        } else {
            disableBigMemCriticalMemory();
            if (this.mHwAMS.isLauncher(appName)) {
                return 0;
            }
            Bundle extras = createBundleFromAppInfo(appInfo, appName);
            AwareLog.d(TAG, "app event " + appName + " activity finish");
            setActivityLaunching(false);
            this.mDMEServer.execute(MemoryConstant.MEM_SCENE_DEFAULT, extras, event, timestamp);
            return 0;
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

    private Bundle createBundleFromAppInfo(ArrayMap<String, String> appInfo, String appName) {
        Bundle extras = new Bundle();
        try {
            extras.putInt("appUid", Integer.valueOf(Integer.parseInt((String) appInfo.get("uid"))).intValue());
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "uid is not right");
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
            if (AwareConstant.CURRENT_USER_TYPE == 3 && "activity".equals(reason)) {
                BigDataStore instance = BigDataStore.getInstance();
                instance.coldStartCount++;
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

    private void enableBigMemCriticalMemory() {
        MemoryConstant.enableBigMemCriticalMemory();
        AwareLog.d(TAG, "setLowCriticalMemory, current limit is " + (MemoryConstant.getCriticalMemory() / 1024) + "MB");
    }

    private void disableBigMemCriticalMemory() {
        MemoryConstant.disableBigMemCriticalMemory();
        AwareLog.d(TAG, "resetLowCriticalMemory, current limit is " + (MemoryConstant.getCriticalMemory() / 1024) + "MB");
    }
}
