package com.android.server.rms.iaware.memory.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.huawei.android.app.AppOpsManagerExt;
import com.huawei.android.app.HwActivityTaskManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BigMemoryInfo {
    private static final Object STATIC_LOCK = new Object();
    private static final String TAG = "AwareMem_BigMemConfig";
    private static BigMemoryInfo sBigMemoryInfo;
    private static int sTotalActivityNum = 0;
    private int mActivitiesSize = 0;
    private Map<String, Long> mActivityBeginTimeMap = new ConcurrentHashMap();
    private ActivityMemInfoHandler mActivityHandler = null;
    private int mActivityMaxMem = 0;
    private Map<String, ActivityMemInfo> mActivityMemInfoMap = new ConcurrentHashMap();
    private String mAppNameForCallingCamera = "";
    private AppOpsManager mAppOps = null;
    private AppOpsListener mAppOpsListener = null;
    private int mCameraMaxMem = 0;
    private int mCameraMemChangeThreshold = 0;
    private int mCameraMemChangeThresholdSd = 0;
    private int mCameraMinMem = 0;
    private Set<String> mCameraServerProcs = new HashSet();
    private volatile boolean mIsActivityBegin = false;
    private volatile boolean mIsBigMemFeatureOn = false;
    private volatile boolean mIsCameraClosed = false;
    private volatile boolean mIsDynamicBigMemory = true;
    private int mOpenCameraDelayTime = 0;
    private String mUsingCameraActivity = "";
    private final ArrayMap<String, Long> memoryRequestMap = new ArrayMap<>();
    private String recentActivity = null;

    private BigMemoryInfo() {
        initParas();
    }

    public static BigMemoryInfo getInstance() {
        BigMemoryInfo bigMemoryInfo;
        synchronized (STATIC_LOCK) {
            if (sBigMemoryInfo == null) {
                sBigMemoryInfo = new BigMemoryInfo();
            }
            bigMemoryInfo = sBigMemoryInfo;
        }
        return bigMemoryInfo;
    }

    public boolean checkConfigParas() {
        if (this.mActivityMaxMem == 0 || this.mActivitiesSize == 0 || this.mCameraMaxMem == 0 || this.mCameraMinMem == 0 || this.mOpenCameraDelayTime == 0 || this.mCameraMemChangeThresholdSd == 0 || this.mCameraMemChangeThreshold == 0 || this.mCameraServerProcs.isEmpty()) {
            return false;
        }
        return true;
    }

    private void initParas() {
        this.mActivityHandler = new ActivityMemInfoHandler();
        Context context = MemoryConstant.getContext();
        if (context != null && context.getSystemService("appops") != null) {
            Object obj = context.getSystemService("appops");
            if (obj instanceof AppOpsManager) {
                this.mAppOps = (AppOpsManager) obj;
            }
            this.mAppOpsListener = new AppOpsListener();
            AppOpsManager appOpsManager = this.mAppOps;
            if (appOpsManager != null) {
                AppOpsManagerExt.startWatchingActive(appOpsManager, new int[]{26}, this.mAppOpsListener);
            }
        }
    }

    public boolean isBigMemoryApp(String appName) {
        synchronized (this.memoryRequestMap) {
            if (appName != null) {
                if (!this.memoryRequestMap.isEmpty()) {
                    return this.memoryRequestMap.containsKey(appName);
                }
            }
            return false;
        }
    }

    public boolean isBigMemoryApp(int uid, String activityName) {
        if (activityName == null) {
            return false;
        }
        String activityNameTemp = cutActivityNameIfNeed(activityName);
        if (activityNameTemp.equals(BigMemoryConstant.CAMERA_OPEN_ACTIVITY_NAME)) {
            activityNameTemp = "com.huawei.camera";
        }
        Map<String, ActivityMemInfo> map = this.mActivityMemInfoMap;
        ActivityMemInfo activityMemInfo = map.get(uid + activityNameTemp);
        if (activityMemInfo == null || !activityMemInfo.isLearned()) {
            return false;
        }
        return true;
    }

    public long getAppLaunchRequestMemory(String appName) {
        synchronized (this.memoryRequestMap) {
            if (appName != null) {
                if (!this.memoryRequestMap.isEmpty()) {
                    if (this.memoryRequestMap.containsKey(appName)) {
                        return this.memoryRequestMap.get(appName).longValue();
                    }
                }
            }
            return 0;
        }
    }

    public long getAppLaunchRequestMemory(int uid, String activityName) {
        if (uid <= 0 || activityName == null) {
            return 0;
        }
        Map<String, ActivityMemInfo> map = this.mActivityMemInfoMap;
        ActivityMemInfo activityMemInfo = map.get(uid + activityName);
        if (activityMemInfo != null) {
            return (long) activityMemInfo.getMemSize();
        }
        return 0;
    }

    public void resetLaunchMemConfig() {
        synchronized (this.memoryRequestMap) {
            this.memoryRequestMap.clear();
        }
    }

    public void setRequestMemForLaunch(String appName, long launchRequestMem) {
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "setRequestMemForLaunch appname is " + appName);
        }
        if (appName != null) {
            synchronized (this.memoryRequestMap) {
                this.memoryRequestMap.put(appName, Long.valueOf(launchRequestMem));
            }
        }
    }

    public void removeRequestMemForLaunch(String appName) {
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "removeRequestMemForLaunch appname is " + appName);
        }
        synchronized (this.memoryRequestMap) {
            if (appName != null) {
                this.memoryRequestMap.remove(appName);
            }
        }
    }

    public boolean isMemoryRequestMapEmpty() {
        synchronized (this.memoryRequestMap) {
            if (this.memoryRequestMap.isEmpty()) {
                return true;
            }
            return false;
        }
    }

    public boolean isDynamicBigMemory() {
        return this.mIsDynamicBigMemory;
    }

    public void setDynamicBigMemory(boolean isDynamicBigMemory) {
        this.mIsDynamicBigMemory = isDynamicBigMemory;
    }

    /* access modifiers changed from: private */
    public class ActivityMemInfoHandler extends Handler {
        private ActivityMemInfoHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case BigMemoryConstant.MSG_LEARNING_MEMORY_INFO /* 101 */:
                    BigMemoryInfo.this.learnCameraMemInfo(bundle);
                    return;
                case BigMemoryConstant.MSG_OPEN_CAMERA_INFO /* 102 */:
                    BigMemoryInfo.this.handleCameraOpen(bundle);
                    return;
                case BigMemoryConstant.MSG_ACTIVITY_BEGIN_INFO /* 103 */:
                    BigMemoryInfo.this.handleActivityBegin(bundle);
                    return;
                case 104:
                    BigMemoryInfo.this.handleActivityFinish(bundle);
                    return;
                case 105:
                    BigMemoryInfo.this.handleRemoveUidFromBigMemMap(bundle);
                    return;
                default:
                    return;
            }
        }
    }

    private boolean isMaxLimit(String activityName, int uid) {
        Map<String, ActivityMemInfo> map = this.mActivityMemInfoMap;
        ActivityMemInfo activityMemInfo = map.get(uid + activityName);
        if (!(activityMemInfo == null || !activityMemInfo.isLearned()) || sTotalActivityNum < this.mActivitiesSize) {
            return false;
        }
        AwareLog.w(TAG, "the total activity is more than the max activity size");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void learnCameraMemInfo(Bundle bundle) {
        if (bundle == null) {
            AwareLog.w(TAG, "learnCameraMemInfo bundle is null");
            return;
        }
        String packageName = bundle.getString("packageName");
        String activityName = bundle.getString("activityName");
        int uid = bundle.getInt("uid");
        if (!this.mIsCameraClosed) {
            int cameraTotalMemInfo = 0;
            for (String cameraServerProc : this.mCameraServerProcs) {
                if (BigMemoryConstant.CAMERA_SERVER.equals(cameraServerProc)) {
                }
                int temp = (int) ResourceCollector.getPss(MemoryUtils.getNativeRelatedPid(cameraServerProc), (long[]) null, (long[]) null);
                if (!checkMemInValid(temp, cameraTotalMemInfo)) {
                    cameraTotalMemInfo += temp;
                } else {
                    return;
                }
            }
            int totalMemSize = getMemSizeValid(cameraTotalMemInfo, uid, activityName, packageName);
            if (totalMemSize > 0) {
                Map<String, ActivityMemInfo> map = this.mActivityMemInfoMap;
                ActivityMemInfo activityMemInfo = map.get(uid + activityName);
                if (activityMemInfo == null) {
                    activityMemInfo = new ActivityMemInfo(activityName, uid);
                    Map<String, ActivityMemInfo> map2 = this.mActivityMemInfoMap;
                    map2.put(uid + activityName, activityMemInfo);
                }
                List<Integer> activityMemList = activityMemInfo.getMemList();
                activityMemList.add(Integer.valueOf(totalMemSize));
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "learnCameraMemInfo the totalMemSize of the time is :" + totalMemSize);
                }
                if (activityMemList.size() == 5) {
                    saveValidMem(activityMemList, activityMemInfo);
                    activityMemList.clear();
                }
            }
        }
    }

    private boolean checkMemInValid(int processMem, int totalMem) {
        if (processMem > 0 && totalMem + processMem > 0) {
            return false;
        }
        return true;
    }

    private int getMemSizeValid(int cameraTotalMemInfo, int uid, String activityName, String packageName) {
        if (cameraTotalMemInfo < this.mCameraMinMem || cameraTotalMemInfo > this.mCameraMaxMem) {
            AwareLog.w(TAG, "getMemSizeValid camera process mem is invalid ,cameraMinMem=" + this.mCameraMinMem);
            return 0;
        }
        int totalMemSize = getAppTotalMemInfo(uid, activityName, packageName) + cameraTotalMemInfo + ((int) MemoryConstant.getBigMemoryAppCriticalMemory());
        if (totalMemSize <= this.mActivityMaxMem && totalMemSize > 0) {
            return totalMemSize;
        }
        AwareLog.w(TAG, "getMemSizeValid the total mem is invalid ,cameraTotalMemInfo =" + totalMemSize);
        return 0;
    }

    private void saveValidMem(List<Integer> list, ActivityMemInfo activityMemInfo) {
        Collections.sort(list);
        long average = 0;
        List<Integer> tempList = new ArrayList<>();
        int listSize = list.size();
        for (int i = 1; i < listSize - 1; i++) {
            average += (long) list.get(i).intValue();
            tempList.add(list.get(i));
        }
        int tempListSize = tempList.size();
        if (tempListSize > 0) {
            long average2 = average / ((long) tempListSize);
            if (!activityMemInfo.isLearned() || Math.abs(((long) activityMemInfo.getMemSize()) - average2) >= ((long) this.mCameraMemChangeThreshold)) {
                long variance = 0;
                for (Integer num : tempList) {
                    variance += (long) Math.pow((double) (((long) num.intValue()) - average2), 2.0d);
                }
                if (variance > 0) {
                    long variance2 = (long) Math.sqrt((double) (variance / ((long) tempListSize)));
                    if (variance2 <= ((long) this.mCameraMemChangeThresholdSd)) {
                        int validMem = (int) average2;
                        activityMemInfo.setMemSize(validMem);
                        activityMemInfo.setNeedUpdate(true);
                        if (activityMemInfo.getName() != null) {
                            Map<String, ActivityMemInfo> map = this.mActivityMemInfoMap;
                            map.put(activityMemInfo.getUid() + activityMemInfo.getName(), activityMemInfo);
                            if (AwareLog.getDebugLogSwitch()) {
                                AwareLog.d(TAG, "saveValidMem validMem = " + validMem + ",sd = " + variance2 + ",MemChangeThresholdSd = " + this.mCameraMemChangeThresholdSd);
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ActivityMemInfo {
        private volatile boolean mIsLearned = false;
        private String mName = null;
        private volatile boolean mNeedUpdate = false;
        private int mUid = 0;
        private List<Integer> memList = new ArrayList();
        private int memSize;

        ActivityMemInfo(String name, int uid) {
            this.mName = name;
            this.mUid = uid;
        }

        public int getMemSize() {
            return this.memSize;
        }

        public void setMemSize(int memSize2) {
            this.memSize = memSize2;
        }

        public boolean isNeedUpdate() {
            return this.mNeedUpdate;
        }

        public void setNeedUpdate(boolean needUpdate) {
            this.mNeedUpdate = needUpdate;
        }

        public String getName() {
            return this.mName;
        }

        public void setName(String name) {
            this.mName = name;
        }

        public List<Integer> getMemList() {
            return this.memList;
        }

        public void setMemList(List<Integer> memList2) {
            this.memList = memList2;
        }

        public boolean isLearned() {
            return this.mIsLearned;
        }

        public void setLearned(boolean isLeanred) {
            this.mIsLearned = isLeanred;
        }

        public int getUid() {
            return this.mUid;
        }
    }

    private void saveActivityMem(int uid, String activityName) {
        ActivityMemInfo activityMemInfo = this.mActivityMemInfoMap.get(uid + activityName);
        if (activityMemInfo != null) {
            if (activityMemInfo.isNeedUpdate()) {
                if (!activityMemInfo.isLearned()) {
                    sTotalActivityNum++;
                }
                activityMemInfo.setLearned(true);
                activityMemInfo.setNeedUpdate(false);
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "the activity " + activityName + " has learned");
                    return;
                }
                return;
            }
            AwareLog.d(TAG, "the activity memory has not updated");
        }
    }

    private int getAppTotalMemInfo(int uid, String activityName, String appName) {
        int totalMem = 0;
        if (!"com.huawei.camera".equals(activityName) || !"com.huawei.camera".equals(appName)) {
            return 0;
        }
        SparseSet pids = new SparseSet();
        AwareAppAssociate.getInstance().getPidsByUid(uid, pids);
        for (int i = pids.size() - 1; i >= 0; i--) {
            totalMem += (int) ResourceCollector.getPss(pids.keyAt(i), (long[]) null, (long[]) null);
        }
        if (totalMem < 0) {
            return 0;
        }
        return totalMem;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCurrentActivityName() {
        ActivityInfo activityInfo = HwActivityTaskManager.getLastResumedActivity();
        if (activityInfo == null || ActivityInfoEx.getComponentName(activityInfo) == null) {
            return "";
        }
        return ActivityInfoEx.getComponentName(activityInfo).getClassName();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCameraOpen(String packageName, int uid, String activityName) {
        AwareLog.d(TAG, "processCameraOpen open camera event");
        Message message = Message.obtain();
        message.setData(getBundle(packageName, uid, activityName));
        message.what = BigMemoryConstant.MSG_OPEN_CAMERA_INFO;
        this.mActivityHandler.removeMessages(BigMemoryConstant.MSG_OPEN_CAMERA_INFO);
        this.mActivityHandler.sendMessage(message);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCameraOpen(Bundle bundle) {
        if (bundle == null) {
            AwareLog.w(TAG, "handleCameraOpen bundle is null");
            return;
        }
        String packageName = bundle.getString("packageName");
        if (packageName == null || "".equals(packageName)) {
            AwareLog.w(TAG, "handleCameraOpen packageName is null or empty");
            return;
        }
        String activityName = bundle.getString("activityName");
        if (activityName == null || "".equals(activityName)) {
            AwareLog.w(TAG, "handleCameraOpen activityName is null or empty");
            return;
        }
        int uid = bundle.getInt("uid");
        if (uid <= 0) {
            AwareLog.w(TAG, "handleCameraOpen uid <=0");
            return;
        }
        this.mIsCameraClosed = false;
        if (isMaxLimit(activityName, uid)) {
            AwareLog.d(TAG, "handleCameraOpen the total activity is more than the limit");
        } else if (checkOverTime(activityName)) {
            AwareLog.d(TAG, "handleCameraOpen from activity begin to open camera overtime");
        } else {
            this.mAppNameForCallingCamera = cutActivityNameIfNeed(packageName);
            String activityNameTemp = cutActivityNameIfNeed(activityName);
            if (!activityNameTemp.equals(this.mUsingCameraActivity)) {
                AwareLog.d(TAG, "handleCameraOpen the activity name is different");
                return;
            }
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "processCameraOpen pkgName:" + packageName + " uid:" + uid + " actName:" + activityNameTemp);
            }
            processCameraMemInfo(packageName, uid, activityNameTemp);
        }
    }

    private Bundle getBundle(String packageName, int uid, String activityName) {
        Bundle bundle = new Bundle();
        bundle.putString("packageName", packageName);
        bundle.putString("activityName", activityName);
        bundle.putInt("uid", uid);
        return bundle;
    }

    private void processCameraMemInfo(String packageName, int uid, String activityName) {
        Message message = Message.obtain();
        message.setData(getBundle(packageName, uid, activityName));
        message.what = BigMemoryConstant.MSG_LEARNING_MEMORY_INFO;
        this.mActivityHandler.removeMessages(BigMemoryConstant.MSG_LEARNING_MEMORY_INFO);
        this.mActivityHandler.sendMessageDelayed(message, (long) this.mOpenCameraDelayTime);
    }

    private boolean checkOverTime(String activityName) {
        String activityNameTemp = cutActivityNameIfNeed(activityName);
        if ("".equals(activityNameTemp) || this.mActivityBeginTimeMap.get(activityNameTemp) == null) {
            return false;
        }
        long beginTime = this.mActivityBeginTimeMap.get(activityNameTemp).longValue();
        if (beginTime != 0) {
            long duringTime = System.currentTimeMillis() - beginTime;
            if (duringTime > 500) {
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "the time between the activity and open camera: " + duringTime);
                }
                this.mActivityBeginTimeMap.remove(activityNameTemp);
                return true;
            }
        }
        this.mActivityBeginTimeMap.remove(activityNameTemp);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCameraClose(String appName, int uid) {
        this.mIsCameraClosed = true;
    }

    public void showActivityMemInfo() {
        for (Map.Entry<String, ActivityMemInfo> entry : this.mActivityMemInfoMap.entrySet()) {
            ActivityMemInfo activityMemInfo = entry.getValue();
            if (activityMemInfo.isLearned()) {
                int uid = activityMemInfo.getUid();
                String activityName = activityMemInfo.getName();
                int memSize = activityMemInfo.getMemSize();
                List<Integer> list = activityMemInfo.getMemList();
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "uid=" + uid + " activityName=" + activityName + " memSize=" + memSize + " list=" + list);
                }
            }
        }
    }

    public void removeUidFromBigMemMap(int uninstallApkUid, String pkgName) {
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "removeUidFromBigMemMap pkgName = " + pkgName + ",uninstallApkUid = " + uninstallApkUid);
        }
        if (!this.mIsBigMemFeatureOn) {
            AwareLog.d(TAG, "removeUidFromBigMemMap switch off for BigMemoryPolicy");
        } else if (uninstallApkUid <= 0 || pkgName == null) {
            AwareLog.w(TAG, "removeUidFromBigMemMap paras error");
        } else {
            Bundle bundle = new Bundle();
            bundle.putInt("uid", uninstallApkUid);
            Message message = Message.obtain();
            message.setData(bundle);
            message.what = 105;
            this.mActivityHandler.removeMessages(105);
            this.mActivityHandler.sendMessage(message);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemoveUidFromBigMemMap(Bundle bundle) {
        if (bundle == null) {
            AwareLog.w(TAG, "handleRemoveUidFromBigMemMap bundle is null");
            return;
        }
        int uninstallApkUid = bundle.getInt("uid");
        if (uninstallApkUid <= 0) {
            AwareLog.w(TAG, "handleRemoveUidFromBigMemMap uid is error");
            return;
        }
        Map<String, ActivityMemInfo> map = this.mActivityMemInfoMap;
        if (map != null) {
            Iterator<Map.Entry<String, ActivityMemInfo>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue().getUid() == uninstallApkUid) {
                    it.remove();
                    sTotalActivityNum--;
                }
            }
        }
    }

    public void processActivityBegin(String activityName) {
        if (!this.mIsBigMemFeatureOn) {
            AwareLog.d(TAG, "switch off for BigMemoryPolicy");
        } else if (activityName != null) {
            Bundle bundle = new Bundle();
            bundle.putString("activityName", activityName);
            Message message = Message.obtain();
            message.setData(bundle);
            message.what = BigMemoryConstant.MSG_ACTIVITY_BEGIN_INFO;
            this.mActivityHandler.removeMessages(BigMemoryConstant.MSG_ACTIVITY_BEGIN_INFO);
            this.mActivityHandler.sendMessage(message);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleActivityBegin(Bundle bundle) {
        this.mIsActivityBegin = true;
        this.mAppNameForCallingCamera = "";
        if (bundle == null) {
            AwareLog.w(TAG, "handleActivityBegin bundle is null");
            return;
        }
        String activityNameTemp = bundle.getString("activityName");
        if (activityNameTemp == null) {
            AwareLog.w(TAG, "activityName is null");
            return;
        }
        debugForBetaUser(BigMemoryConstant.BETA_ACTIVITY_BEGIN, activityNameTemp);
        String activityNameTemp2 = cutActivityNameIfNeed(activityNameTemp);
        this.mUsingCameraActivity = activityNameTemp2;
        this.mActivityBeginTimeMap.put(activityNameTemp2, Long.valueOf(System.currentTimeMillis()));
    }

    public void processActivityFinish(int uid, String appName, String activityName) {
        if (!this.mIsBigMemFeatureOn) {
            AwareLog.d(TAG, "switch off for BigMemoryPolicy");
        } else if (!checkParameter(appName, activityName, uid)) {
            AwareLog.w(TAG, "processActivityFinish paras error");
            this.mIsActivityBegin = false;
        } else {
            Bundle bundle = getBundle(appName, uid, activityName);
            Message message = Message.obtain();
            message.setData(bundle);
            message.what = 104;
            this.mActivityHandler.removeMessages(104);
            this.mActivityHandler.sendMessage(message);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleActivityFinish(Bundle bundle) {
        if (bundle == null) {
            AwareLog.w(TAG, "handleActivityFinish bundle is null");
            return;
        }
        String packageName = bundle.getString("packageName");
        String activityName = bundle.getString("activityName");
        int uid = bundle.getInt("uid");
        if (!checkParameter(packageName, activityName, uid)) {
            AwareLog.w(TAG, "handleActivityFinish paras error");
            return;
        }
        debugForBetaUser(BigMemoryConstant.BETA_ACTIVITY_FINISH, activityName);
        String activityName2 = cutActivityNameIfNeed(activityName);
        if (this.mIsActivityBegin) {
            if (checkSameApp(this.mAppNameForCallingCamera, packageName)) {
                boolean isHuaWeiCamera = "com.huawei.camera".equals(this.mUsingCameraActivity);
                if (activityName2.equals(this.mUsingCameraActivity) || isHuaWeiCamera) {
                    saveActivityMem(uid, this.mUsingCameraActivity);
                }
            }
            this.mUsingCameraActivity = "";
            this.mAppNameForCallingCamera = "";
            this.mIsActivityBegin = false;
        }
        this.mActivityBeginTimeMap.remove(activityName2);
    }

    private void debugForBetaUser(String activityEvent, String activityName) {
        if (AwareConstant.CURRENT_USER_TYPE == 3 && AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "activityEvent:" + activityEvent + ",activityName:" + activityName);
        }
    }

    private boolean checkSameApp(String callCameraAppName, String appName) {
        if (!callCameraAppName.equals(appName)) {
            return false;
        }
        return true;
    }

    private boolean checkParameter(String appName, String activityName, int uid) {
        if (appName == null || activityName == null || uid <= 0) {
            return false;
        }
        return true;
    }

    public String cutActivityNameIfNeed(String activityName) {
        if (activityName == null) {
            return "";
        }
        if (activityName.length() > 132) {
            return activityName.substring(0, BigMemoryConstant.ACTIVITY_NAME_MAX_LEN);
        }
        return activityName;
    }

    public void insertDataForDumpSys(int uid, int memSize, String activityName) {
        if (uid > 0 && memSize > 0 && activityName != null) {
            if (sTotalActivityNum >= this.mActivitiesSize) {
                AwareLog.w(TAG, "the total activity is more than the max activity size");
                return;
            }
            String activityNameTemp = cutActivityNameIfNeed(activityName);
            Map<String, ActivityMemInfo> map = this.mActivityMemInfoMap;
            if (map.get(uid + activityNameTemp) == null) {
                ActivityMemInfo activityMemInfo = new ActivityMemInfo(activityNameTemp, uid);
                activityMemInfo.setMemSize(memSize);
                activityMemInfo.setLearned(true);
                activityMemInfo.setNeedUpdate(true);
                Map<String, ActivityMemInfo> map2 = this.mActivityMemInfoMap;
                map2.put(uid + activityNameTemp, activityMemInfo);
                sTotalActivityNum = sTotalActivityNum + 1;
            }
        }
    }

    public boolean isBigMemFeatureOn() {
        return this.mIsBigMemFeatureOn;
    }

    public void setBigMemFeatureOn(boolean bigMemFeatureOn) {
        this.mIsBigMemFeatureOn = bigMemFeatureOn;
    }

    public void setActivityMaxMem(int activityMaxMem) {
        this.mActivityMaxMem = activityMaxMem;
    }

    public int getActivitiesSize() {
        return this.mActivitiesSize;
    }

    public void setActivitiesSize(int activitiesSize) {
        this.mActivitiesSize = activitiesSize;
    }

    public void setCameraMaxMem(int cameraMaxMem) {
        this.mCameraMaxMem = cameraMaxMem;
    }

    public void setCameraMinMem(int cameraMinMem) {
        this.mCameraMinMem = cameraMinMem;
    }

    public void setCameraMemChangeThresholdSd(int cameraMemChangeThresholdSd) {
        this.mCameraMemChangeThresholdSd = cameraMemChangeThresholdSd;
    }

    public void setCameraMemChangeThreshold(int cameraMemChangeThreshold) {
        this.mCameraMemChangeThreshold = cameraMemChangeThreshold;
    }

    public void setOpenCameraDelayTime(int openCameraDelayTime) {
        this.mOpenCameraDelayTime = openCameraDelayTime;
    }

    public int getCameraMaxMem() {
        return this.mCameraMaxMem;
    }

    public int getCameraMinMem() {
        return this.mCameraMinMem;
    }

    public void addCameraServerProcs(String cameraServerName) {
        this.mCameraServerProcs.add(cameraServerName);
    }

    /* access modifiers changed from: package-private */
    public class AppOpsListener extends AppOpsManagerExt.OnOpActiveChangedListenerEx {
        AppOpsListener() {
        }

        public void onOpActiveChanged(int code, int uid, String packageName, boolean active) {
            if (!BigMemoryInfo.this.mIsBigMemFeatureOn) {
                return;
            }
            if (active) {
                BigMemoryInfo.this.processCameraOpen(packageName, uid, BigMemoryInfo.this.getCurrentActivityName());
                return;
            }
            BigMemoryInfo.this.processCameraClose(packageName, uid);
        }
    }
}
