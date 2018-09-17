package com.android.server.mtm.iaware.appmng.appstart;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartReason;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.webkit.WebViewZygote;
import com.android.server.PPPOEStateMachine;
import com.android.server.am.ActivityRecord;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.ProcessRecord;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupDataMgr;
import com.android.server.mtm.iaware.appmng.policy.AppStartPolicy;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCache;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.srms.AppStartupFeature;
import com.android.server.rms.iaware.srms.SRMSDumpRadar;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class AwareAppStartupPolicy {
    private static final /* synthetic */ int[] -android-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues = null;
    private static final String ACTION_APPSTARTUP_CHANGED = "com.huawei.android.APPSTARTUP_CHANGED";
    private static final String ACTION_APPSTARTUP_RECORD = "com.huawei.android.hsm.APPSTARTUP_RECORD";
    private static final long DELAY_MILLS_NOTIFY_PG = 5000;
    private static final long DELAY_MILLS_REMOVE_RESTART_DATA = 1200000;
    private static final long DELAY_PRINT_LOG = 1000;
    private static final int HASHCODE_INIT_VALUE = 17;
    private static final int HASHCODE_MULTI_VALUE = 31;
    private static final String HSM_PACKAGE_NAME = "com.huawei.systemmanager";
    private static final String HW_LAUNCHER_WIDGET_UPDATE = "com.huawei.android.launcher.action.GET_WIDGET";
    private static final String IGNORE = "NA";
    private static final int MAX_LOG_MERGER = 10;
    private static boolean MM_HOTA_AUTOSTART_HAS_DONE = false;
    private static final int MSG_CODE_INIT_STARTUP_SETTING = 5;
    private static final int MSG_CODE_NOTIFY_CHANGED_TO_PG = 4;
    private static final int MSG_CODE_NOTIFY_PROCESS_START = 6;
    private static final int MSG_CODE_POLICY_INIT = 1;
    private static final int MSG_CODE_REMOVE_RESTART_DATA = 7;
    private static final int MSG_CODE_REPORT_RECORD_TO_HSM = 3;
    private static final int MSG_CODE_REPORT_RECORD_TO_LOG = 8;
    private static final int MSG_CODE_WRITE_STARTUP_SETTING = 2;
    private static final String PG_PACKAGE_NAME = "com.huawei.powergenie";
    private static final String PKG_PREFIX = "com.";
    private static final String PREVENT_SEPERATE = "#";
    private static final int REPORT_HSM_INTERVAL = 30000;
    private static final int REPORT_HSM_MAX = 30;
    private static final String TAG = "AwareAppStartupPolicy";
    private static final String TAG_SIMPLE = "AppStart";
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    private static final StringBuilder mSbLog = new StringBuilder(256);
    private static final StringBuilder mSbLogMerge = new StringBuilder(256);
    private static AwareAppStartupPolicy sInstance = null;
    private boolean DEBUG_COST = false;
    private boolean DEBUG_DETAIL = false;
    private final RestartData mAllowData = new RestartData(null, null, 0, 0, null);
    private Context mContext = null;
    private AppStartupDataMgr mDataMgr = new AppStartupDataMgr();
    private int mForegroundAppLevel = 2;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    private ArrayMap<HsmRecord, Integer> mHsmRecordList = new ArrayMap();
    private AtomicBoolean mInitFinished = new AtomicBoolean(false);
    private boolean mIsAbroadArea = false;
    private boolean mIsUpgrade = false;
    private long mLastRemoveMsgTime = 0;
    private ArrayMap<AppStartLogRecord, ArraySet<String>> mLogRecordMap = new ArrayMap();
    private AtomicBoolean mPolicyInited = new AtomicBoolean(false);
    private ArrayMap<String, RestartData> mRestartDataList = new ArrayMap();
    private boolean mSubUserAppCtrl = true;

    private static class AppStartLogRecord {
        public String action;
        public String alw;
        public String caller;
        public int callerUid;
        public AppStartSource req;

        /* synthetic */ AppStartLogRecord(AppStartSource requestSource, String alwDetail, int callerUid, String callerApp, String action, AppStartLogRecord -this5) {
            this(requestSource, alwDetail, callerUid, callerApp, action);
        }

        private AppStartLogRecord(AppStartSource requestSource, String alwDetail, int callerUid, String callerApp, String action) {
            this.req = requestSource;
            this.alw = alwDetail;
            this.callerUid = callerUid;
            this.caller = callerApp;
            this.action = action;
        }

        public int hashCode() {
            return ((((((((this.req.ordinal() + 527) * 31) + this.alw.hashCode()) * 31) + this.callerUid) * 31) + this.caller.hashCode()) * 31) + this.action.hashCode();
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == null || ((obj instanceof AppStartLogRecord) ^ 1) != 0) {
                return false;
            }
            AppStartLogRecord o = (AppStartLogRecord) obj;
            if (this.req.equals(o.req) && this.alw.equals(o.alw) && this.callerUid == o.callerUid && this.caller.equals(o.caller)) {
                z = this.action.equals(o.action);
            }
            return z;
        }
    }

    private enum CacheType {
        STARTUP_SETTING
    }

    private static class HsmRecord {
        private boolean autoStart;
        private String callerType;
        private int callerUid;
        private String pkg;
        private boolean result;
        private long timeStamp;

        /* synthetic */ HsmRecord(String pkgName, String type, int uid, boolean res, boolean selfStart, HsmRecord -this5) {
            this(pkgName, type, uid, res, selfStart);
        }

        private HsmRecord(String pkgName, String type, int uid, boolean res, boolean selfStart) {
            this.pkg = pkgName;
            this.callerType = type;
            this.callerUid = uid;
            this.result = res;
            this.autoStart = selfStart;
            this.timeStamp = System.currentTimeMillis();
        }

        public int hashCode() {
            int i;
            int i2 = 1;
            int hashCode = (this.callerType != null ? this.callerType.hashCode() : 0) + (this.pkg != null ? this.pkg.hashCode() : 0);
            int i3 = this.callerUid;
            if (this.result) {
                i = 1;
            } else {
                i = 0;
            }
            i = 1 << i;
            if (!this.autoStart) {
                i2 = 0;
            }
            return ((i + i2) * i3) + hashCode;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == null || ((obj instanceof HsmRecord) ^ 1) != 0) {
                return false;
            }
            HsmRecord o = (HsmRecord) obj;
            if (Objects.equals(this.pkg, o.pkg) && Objects.equals(this.callerType, o.callerType) && this.callerUid == o.callerUid && this.result == o.result && this.autoStart == o.autoStart) {
                z = true;
            }
            return z;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("HsmRecord: {").append(this.pkg);
            sb.append(" type:").append(this.callerType);
            sb.append(" uid:").append(this.callerUid);
            sb.append(" res:").append(this.result);
            sb.append(" auto:").append(this.autoStart);
            sb.append(" time:").append(this.timeStamp).append('}');
            return sb.toString();
        }
    }

    private static class RestartData {
        private int alwType;
        private boolean dirty;
        private String pkg;
        private int policyType;
        private String reason;
        private long timeStamp;

        /* synthetic */ RestartData(String pkgName, String rsn, int allowtype, int policy, RestartData -this4) {
            this(pkgName, rsn, allowtype, policy);
        }

        private RestartData(String pkgName, String rsn, int allowtype, int policy) {
            this.pkg = pkgName;
            this.reason = rsn;
            this.alwType = allowtype;
            this.policyType = policy;
            this.timeStamp = SystemClock.elapsedRealtime();
            this.dirty = true;
        }

        public String toString() {
            return "RestartData: {" + this.pkg + " reason:" + this.reason + " policy:" + this.policyType + "}";
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        public boolean getDirty() {
            return this.dirty;
        }

        public void set(String reason, int alwType, int policyType) {
            this.reason = reason;
            this.alwType = alwType;
            this.policyType = policyType;
        }
    }

    private class StartupPolicyHandler extends Handler {
        public StartupPolicyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AwareAppStartupPolicy.this.policyInit();
                    return;
                case 2:
                    AwareAppStartupPolicy.this.writeCacheFile(CacheType.STARTUP_SETTING);
                    return;
                case 3:
                    AwareAppStartupPolicy.this.startRecordService();
                    return;
                case 4:
                    AwareAppStartupPolicy.this.startNotifyChangedService();
                    return;
                case 5:
                    AwareAppStartupPolicy.this.initStartupSetting();
                    return;
                case 6:
                    AwareAppStartupPolicy.this.reportProcessStart(msg);
                    return;
                case 7:
                    AwareAppStartupPolicy.this.periodRemoveRestartData();
                    return;
                case 8:
                    AwareAppStartupPolicy.this.printMergeReqLog();
                    return;
                default:
                    return;
            }
        }
    }

    private static /* synthetic */ int[] -getandroid-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues() {
        if (-android-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues != null) {
            return -android-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues;
        }
        int[] iArr = new int[AppStartSource.values().length];
        try {
            iArr[AppStartSource.ACCOUNT_SYNC.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppStartSource.ALARM.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppStartSource.BIND_SERVICE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppStartSource.JOB_SCHEDULE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppStartSource.PROVIDER.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppStartSource.SCHEDULE_RESTART.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[AppStartSource.START_SERVICE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[AppStartSource.SYSTEM_BROADCAST.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[AppStartSource.THIRD_ACTIVITY.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[AppStartSource.THIRD_BROADCAST.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        -android-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues = iArr;
        return iArr;
    }

    private AwareAppStartupPolicy(Context context, HandlerThread mtmThread) {
        this.mContext = context;
        if (AppStartupFeature.isAppStartupEnabled()) {
            init();
            return;
        }
        this.mHandler = new StartupPolicyHandler(mtmThread.getLooper());
        this.mHandler.sendEmptyMessage(5);
    }

    public static synchronized AwareAppStartupPolicy getInstance(Context context, HandlerThread mtmThread) {
        AwareAppStartupPolicy awareAppStartupPolicy;
        synchronized (AwareAppStartupPolicy.class) {
            if (!(sInstance != null || context == null || mtmThread == null)) {
                sInstance = new AwareAppStartupPolicy(context, mtmThread);
            }
            awareAppStartupPolicy = sInstance;
        }
        return awareAppStartupPolicy;
    }

    public static synchronized AwareAppStartupPolicy self() {
        AwareAppStartupPolicy awareAppStartupPolicy;
        synchronized (AwareAppStartupPolicy.class) {
            awareAppStartupPolicy = sInstance;
        }
        return awareAppStartupPolicy;
    }

    private void init() {
        if (this.mHandlerThread == null) {
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mHandler = new StartupPolicyHandler(this.mHandlerThread.getLooper());
        }
        this.mHandler.sendEmptyMessage(1);
    }

    private void unInit() {
        if (this.mPolicyInited.get()) {
            setInitState(false);
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(3);
            AwareIntelligentRecg.getInstance().appStartDisable();
            this.mDataMgr.unInitData();
        }
    }

    private void setInitState(boolean finished) {
        this.mPolicyInited.set(finished);
        if (finished) {
            this.mInitFinished.set(true);
        }
    }

    private void setAppStartupSyncFlag(boolean needSync) {
        SystemProperties.set("persist.sys.appstart.sync", needSync ? StorageUtils.SDCARD_ROMOUNTED_STATE : StorageUtils.SDCARD_RWMOUNTED_STATE);
        if (this.DEBUG_DETAIL) {
            AwareLog.e(TAG, "setAppStartupSyncFlag needSync=" + needSync);
        }
    }

    private void policyInit() {
        long start = System.nanoTime();
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null && pm.isUpgrade()) {
            this.mIsUpgrade = true;
        }
        AwareLog.i(TAG, "policyInit mIsUpgrade=" + this.mIsUpgrade);
        this.mIsAbroadArea = AwareDefaultConfigList.isAbroadArea();
        DecisionMaker.getInstance().updateRule(AppMngFeature.APP_START, this.mContext);
        this.mDataMgr.initData(this.mContext);
        AwareIntelligentRecg.getInstance().appStartEnable(this.mDataMgr, this.mContext);
        setInitState(true);
        AwareLog.i(TAG, "policyInit finished, cost=" + ((System.nanoTime() - start) / 1000));
    }

    private void initStartupSetting() {
        long start = System.nanoTime();
        this.mDataMgr.initStartupSetting();
        setInitState(true);
        AwareLog.i(TAG, "initStartupSetting finished, cost=" + ((System.nanoTime() - start) / 1000));
    }

    public void initSystemUidCache() {
        this.mDataMgr.initSystemUidCache(this.mContext);
    }

    private boolean isPolicyEnabled() {
        return this.mPolicyInited.get() ? AppStartupFeature.isAppStartupEnabled() : false;
    }

    private boolean isSubUser() {
        return AwareAppAssociate.getInstance().getCurSwitchUser() != 0;
    }

    private boolean isSubUserAppControl(int uid, AppStartSource requestSource) {
        boolean z = true;
        boolean subUser = isSubUser();
        if (!this.mSubUserAppCtrl) {
            return subUser;
        }
        if (!subUser) {
            return false;
        }
        if (AppStartSource.SCHEDULE_RESTART.equals(requestSource)) {
            return true;
        }
        if (UserHandle.getUserId(uid) == 0) {
            z = false;
        }
        return z;
    }

    public HwAppStartupSetting getAppStartupSetting(String pkgName) {
        if (this.mInitFinished.get()) {
            return this.mDataMgr.getAppStartupSetting(pkgName);
        }
        return null;
    }

    public List<HwAppStartupSetting> retrieveAppStartupSettings(List<String> pkgList, HwAppStartupSettingFilter filter) {
        if (!this.mInitFinished.get()) {
            return null;
        }
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "retrieveAppStartupSettings pkgList=" + pkgList + ", filter=" + filter);
        }
        return this.mDataMgr.retrieveAppStartupSettings(pkgList, filter);
    }

    public List<String> retrieveAppStartupPackages(List<String> pkgList, int[] policy, int[] modifier, int[] show) {
        if (!this.mInitFinished.get()) {
            return null;
        }
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "retrieveAppStartupPackages pkgList=" + pkgList + ", policy=" + Arrays.toString(policy) + ", modifier=" + Arrays.toString(modifier) + ", show=" + Arrays.toString(show));
        }
        List<HwAppStartupSetting> settingList = this.mDataMgr.retrieveAppStartupSettings(pkgList, new HwAppStartupSettingFilter().setPolicy(policy).setShow(show).setModifier(modifier));
        List<String> list = new ArrayList();
        for (HwAppStartupSetting item : settingList) {
            list.add(item.getPackageName());
        }
        return list;
    }

    public boolean updateAppStartupSettings(List<HwAppStartupSetting> settingList, boolean clearFirst) {
        if (this.mInitFinished.get()) {
            if (this.DEBUG_DETAIL) {
                AwareLog.i(TAG, "updateAppStartupSettings clearFirst=" + clearFirst + ", settingList=" + settingList);
            }
            if (!this.mDataMgr.updateAppStartupSettings(settingList, clearFirst)) {
                return false;
            }
            scheduleFastWriteCache(CacheType.STARTUP_SETTING);
            sendAppStartupChangedMsg();
            return true;
        }
        AwareLog.i(TAG, "updateAppStartupSettings policy is not finish init");
        return false;
    }

    public boolean removeAppStartupSetting(String pkgName) {
        if (!this.mInitFinished.get()) {
            return false;
        }
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "removeAppStartupSetting pkgName=" + pkgName);
        }
        if (!this.mDataMgr.removeAppStartupSetting(pkgName)) {
            return false;
        }
        scheduleFastWriteCache(CacheType.STARTUP_SETTING);
        sendAppStartupChangedMsg();
        return true;
    }

    public boolean updateCloudPolicy(String filePath) {
        if (!this.mInitFinished.get()) {
            return false;
        }
        AwareLog.i(TAG, "updateCloudPolicy filePath=" + filePath);
        DecisionMaker.getInstance().updateRule(null, this.mContext);
        AwareIntelligentRecg.getInstance().updateCloudData();
        if (isPolicyEnabled()) {
            this.mDataMgr.updateCloudData();
        }
        return true;
    }

    public void notifyProcessStart(String pkgName, String process, String hostingType, int pid, int uid) {
        if (!isPolicyEnabled() || isSubUser()) {
            return;
        }
        if ((AppStartupFeature.isBetaUser() || !this.mDataMgr.isSpecialCaller(uid)) && !this.mIsAbroadArea) {
            Bundle bundle = new Bundle();
            bundle.putString("PACKAGE", pkgName);
            bundle.putString("HOST_TYPE", hostingType);
            Message msg = this.mHandler.obtainMessage(6);
            msg.setData(bundle);
            this.mHandler.sendMessage(msg);
        }
    }

    public int getBigdataThreshold(boolean beta) {
        return this.mDataMgr.getBigdataThreshold(beta);
    }

    public void setEnable(boolean enable) {
        if (enable) {
            init();
        } else {
            unInit();
        }
    }

    private AppStartSource getReceiverReqSource(boolean isSpecCaller, boolean isAlarmFlag, int callerUid, ApplicationInfo applicationInfo, int unPercetibleAlarm) {
        if (isSpecCaller) {
            return isAlarmFlag ? AppStartSource.ALARM : AppStartSource.SYSTEM_BROADCAST;
        } else if (!isAlarmFlag) {
            return AppStartSource.THIRD_BROADCAST;
        } else {
            if (callerUid == applicationInfo.uid) {
                return AppStartSource.ALARM;
            }
            if (this.mDataMgr.isSystemBaseApp(applicationInfo)) {
                return AppStartSource.ALARM;
            }
            if (unPercetibleAlarm != 0) {
                return AppStartSource.ALARM;
            }
            return AppStartSource.THIRD_BROADCAST;
        }
    }

    private AppStartSource getServiceReqSource(ServiceInfo servInfo, Intent service, boolean isSrvBind, int hwFlag, int callerUid, int unPercetibleAlarm) {
        AppStartSource requestResource = AppStartSource.BIND_SERVICE;
        if ((hwFlag & 32) != 0) {
            requestResource = AppStartSource.JOB_SCHEDULE;
            service.setHwFlags(hwFlag & -33);
            return requestResource;
        } else if ((hwFlag & 64) != 0) {
            return AppStartSource.ACCOUNT_SYNC;
        } else {
            if (isSrvBind) {
                return requestResource;
            }
            if ((hwFlag & 256) == 0) {
                return AppStartSource.START_SERVICE;
            }
            if (servInfo.applicationInfo.uid == callerUid) {
                return AppStartSource.ALARM;
            }
            if (unPercetibleAlarm != 0) {
                return AppStartSource.ALARM;
            }
            return AppStartSource.START_SERVICE;
        }
    }

    private AppStartSource getActivityReqSource(int uid, int callerUid, int hwFlag) {
        AppStartSource requestResource = AppStartSource.THIRD_ACTIVITY;
        if ((hwFlag & 256) == 0) {
            return requestResource;
        }
        if (uid == callerUid) {
            return AppStartSource.ALARM;
        }
        if ((hwFlag & 2048) == 0) {
            return AppStartSource.ALARM;
        }
        return requestResource;
    }

    public boolean shouldPreventSendReceiver(Intent intent, ResolveInfo resolveInfo, int callPid, int callerUid, ProcessRecord targetApp, ProcessRecord callerApp) {
        if (intent == null || resolveInfo == null) {
            return false;
        }
        if (!isPolicyEnabled()) {
            return false;
        }
        boolean z;
        long start = 0;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        int callerPid = callerApp != null ? callerApp.pid : callPid;
        String action = intent.getAction();
        boolean isSpecCaller = this.mDataMgr.isSpecialCaller(callerUid);
        int hwFlag = intent.getHwFlags();
        boolean isAlarmFlag = (hwFlag & 256) != 0;
        int unPercetibleAlarm = 2;
        if (isAlarmFlag) {
            if ((hwFlag & 512) != 0) {
                unPercetibleAlarm = 1;
            } else if ((hwFlag & 2048) != 0) {
                unPercetibleAlarm = 0;
            }
        }
        ApplicationInfo ai = resolveInfo.activityInfo.applicationInfo;
        int pkgAlwType = getPackageAllowType(resolveInfo.activityInfo.processName, ai, callerPid, callerUid, targetApp, callerApp, getReceiverReqSource(isSpecCaller, isAlarmFlag, callerUid, ai, unPercetibleAlarm), resolveInfo.activityInfo.getComponentName().flattenToShortString(), action, unPercetibleAlarm, false);
        if (this.DEBUG_COST) {
            AwareLog.i(TAG, "shouldPreventSendReceiver cost=" + ((System.nanoTime() - start) / 1000));
        }
        if (pkgAlwType <= 0) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid, ProcessRecord callerApp, int servFlag, boolean servExist, Intent service) {
        if (servInfo == null) {
            return false;
        }
        if (!isPolicyEnabled()) {
            return false;
        }
        long start = 0;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        boolean isSrvBind = servFlag == 2;
        if (servFlag == 0 || (isSrvBind && servExist)) {
            if (this.DEBUG_DETAIL) {
                AwareLog.i(TAG, "shouldPreventStartService pkg=" + servInfo.applicationInfo.packageName + ", comp=" + servInfo.name + ", callerPid=" + callerPid + ", callerUid=" + callerUid + ", servFlag=" + servFlag + ", servExist=" + servExist);
            }
            return false;
        }
        int hwFlag = 0;
        String action = null;
        if (service != null) {
            hwFlag = service.getHwFlags();
            action = service.getAction();
        }
        int unPercetibleAlarm = 2;
        if (!((hwFlag & 256) == 0 || (hwFlag & 2048) == 0)) {
            unPercetibleAlarm = 0;
        }
        AppStartSource requestResource = getServiceReqSource(servInfo, service, isSrvBind, hwFlag, callerUid, unPercetibleAlarm);
        if (!AppStartSource.ACCOUNT_SYNC.equals(requestResource) || (hwFlag & 128) == 0) {
            int pkgAlwType = getPackageAllowType(servInfo.processName, servInfo.applicationInfo, callerPid, callerUid, null, callerApp, requestResource, servInfo.getComponentName().flattenToShortString(), action, unPercetibleAlarm, false);
            if (this.DEBUG_COST) {
                AwareLog.i(TAG, "shouldPreventStartService cost=" + ((System.nanoTime() - start) / 1000));
            }
            return pkgAlwType <= 0;
        }
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "we donnot forbiden sync when user initiated");
        }
        return false;
    }

    public boolean shouldPreventStartActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record, int callerPid, int callerUid, ProcessRecord callerApp) {
        if (aInfo == null || intent == null) {
            return false;
        }
        if (!isPolicyEnabled()) {
            return false;
        }
        long start = 0;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        AppStartSource requestResource = getActivityReqSource(aInfo.applicationInfo.uid, callerUid, intent.getHwFlags());
        boolean fromRecent = false;
        if ((intent.getFlags() & HighBitsCompModeID.MODE_COLOR_ENHANCE) != 0) {
            fromRecent = true;
        }
        boolean shouldPrevent = getPackageAllowType(aInfo.processName, aInfo.applicationInfo, callerPid, callerUid, null, callerApp, requestResource, aInfo.getComponentName().flattenToShortString(), null, 2, fromRecent) <= 0;
        if (this.DEBUG_COST) {
            AwareLog.i(TAG, "shouldPreventStartActivity cost=" + ((System.nanoTime() - start) / 1000));
        }
        return shouldPrevent;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, ProcessRecord callerApp) {
        if (cpi == null) {
            return false;
        }
        if (!isPolicyEnabled()) {
            return false;
        }
        long start = 0;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        int pkgAlwType = getPackageAllowType(cpi.processName, cpi.applicationInfo, callerPid, callerUid, null, callerApp, AppStartSource.PROVIDER, cpi.getComponentName().flattenToShortString(), null, 2, false);
        if (this.DEBUG_COST) {
            AwareLog.i(TAG, "shouldPreventStartProvider cost=" + ((System.nanoTime() - start) / 1000));
        }
        return pkgAlwType <= 0;
    }

    public boolean shouldPreventRestartService(ServiceInfo sInfo, boolean realStart) {
        if (sInfo == null) {
            return false;
        }
        if (!isPolicyEnabled()) {
            return false;
        }
        int pkgAlwType;
        boolean z;
        long start = 0;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        if (realStart) {
            RestartData restartData;
            String key = sInfo.applicationInfo.packageName + "#" + sInfo.processName;
            synchronized (this.mRestartDataList) {
                restartData = (RestartData) this.mRestartDataList.remove(key);
            }
            if (restartData != null) {
                if (!(this.mIsAbroadArea || (isSubUser() ^ 1) == 0)) {
                    updateAllowedBigData(restartData.pkg, restartData.reason, restartData.alwType, restartData.policyType, true);
                }
                pkgAlwType = restartData.alwType;
            } else {
                pkgAlwType = 4;
            }
            AwareLog.i(TAG_SIMPLE, "shouldPreventRestartService remove " + key + " " + restartData);
        } else {
            AppStartSource requestSource = AppStartSource.SCHEDULE_RESTART;
            pkgAlwType = getPackageAllowType(sInfo.processName, sInfo.applicationInfo, Binder.getCallingPid(), Binder.getCallingUid(), null, null, requestSource, sInfo.getComponentName().flattenToShortString(), null, 2, false);
        }
        if (this.DEBUG_COST) {
            AwareLog.i(TAG, "shouldPreventRestartService cost=" + ((System.nanoTime() - start) / 1000));
        }
        if (pkgAlwType <= 0) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    private boolean isForbidApp(String pkg, AppStartSource requestSource, boolean isAppStop) {
        if (!this.mDataMgr.isPgCleanApp(pkg)) {
            return false;
        }
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues()[requestSource.ordinal()]) {
            case 9:
                this.mDataMgr.removePgCleanApp(pkg);
                return false;
            default:
                if (isAppStop) {
                    return true;
                }
                return false;
        }
    }

    /* JADX WARNING: Missing block: B:8:0x003f, code:
            if (r5 == null) goto L_0x000e;
     */
    /* JADX WARNING: Missing block: B:9:0x0041, code:
            if (r10 == null) goto L_0x000e;
     */
    /* JADX WARNING: Missing block: B:10:0x0043, code:
            r8 = 0;
            r11 = r10.size();
            r1 = mSbLogMerge;
            r7 = r10.iterator();
     */
    /* JADX WARNING: Missing block: B:12:0x0052, code:
            if (r7.hasNext() == false) goto L_0x000e;
     */
    /* JADX WARNING: Missing block: B:13:0x0054, code:
            r6 = (java.lang.String) r7.next();
            r3 = r8 % 10;
     */
    /* JADX WARNING: Missing block: B:14:0x005c, code:
            if (r3 != 0) goto L_0x00b6;
     */
    /* JADX WARNING: Missing block: B:15:0x005e, code:
            r1.setLength(0);
            r1.append("req:").append(r5.req.getDesc()).append(',');
            r1.append("alw:").append(r5.alw).append(',');
            r1.append("call:").append(r5.callerUid).append(',');
            r1.append(r5.caller).append(',');
            r1.append("act:").append(r5.action).append(' ');
            r1.append("{");
     */
    /* JADX WARNING: Missing block: B:16:0x00b6, code:
            if (r6 == null) goto L_0x00c5;
     */
    /* JADX WARNING: Missing block: B:18:0x00bf, code:
            if (r6.startsWith(PKG_PREFIX) == false) goto L_0x00c5;
     */
    /* JADX WARNING: Missing block: B:19:0x00c1, code:
            r6 = r6.substring(r9);
     */
    /* JADX WARNING: Missing block: B:21:0x00c7, code:
            if (r3 == 9) goto L_0x00cd;
     */
    /* JADX WARNING: Missing block: B:23:0x00cb, code:
            if (r8 != (r11 - 1)) goto L_0x00ea;
     */
    /* JADX WARNING: Missing block: B:24:0x00cd, code:
            r1.append(r6).append("}");
            android.rms.iaware.AwareLog.i(TAG_SIMPLE, r1.toString());
     */
    /* JADX WARNING: Missing block: B:25:0x00e1, code:
            r8 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:31:0x00ea, code:
            r1.append(r6).append(',');
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void printMergeReqLog() {
        int pkgPrefixStart = PKG_PREFIX.length() - 1;
        while (true) {
            synchronized (this.mLogRecordMap) {
                Iterator<Entry<AppStartLogRecord, ArraySet<String>>> it = this.mLogRecordMap.entrySet().iterator();
                if (it.hasNext()) {
                    Entry<AppStartLogRecord, ArraySet<String>> entry = (Entry) it.next();
                    AppStartLogRecord logR = (AppStartLogRecord) entry.getKey();
                    ArraySet<String> pkgSet = (ArraySet) entry.getValue();
                    it.remove();
                } else {
                    return;
                }
            }
        }
    }

    private boolean isMergeReqLog(AppStartSource requestSource, int alwType) {
        if (this.DEBUG_DETAIL || alwType > 0) {
            return false;
        }
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues()[requestSource.ordinal()]) {
            case 2:
            case 9:
                return false;
            default:
                return true;
        }
    }

    private boolean isNeedPrintCmp(AppStartSource requestSource) {
        if (this.DEBUG_DETAIL) {
            return true;
        }
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues()[requestSource.ordinal()]) {
            case 1:
            case 4:
            case 6:
            case 8:
                return false;
            default:
                return true;
        }
    }

    private void printReqLog(AppStartSource requestSource, int alwType, StringBuilder sbReason, boolean isSysApp, boolean isAppStop, int[] fgFlags, int uid, String procName, String pkg, int callerUid, ProcessRecord callerApp, String action, String compName) {
        if (isMergeReqLog(requestSource, alwType)) {
            String str;
            String alw = Integer.toString(alwType) + '[' + sbReason.toString() + ']';
            String str2 = callerApp == null ? "null" : callerApp.processName;
            if (action == null) {
                str = "null";
            } else {
                str = action;
            }
            AppStartLogRecord logR = new AppStartLogRecord(requestSource, alw, callerUid, str2, str, null);
            synchronized (this.mLogRecordMap) {
                ArraySet<String> pkgSet = (ArraySet) this.mLogRecordMap.get(logR);
                if (pkg == null) {
                    pkg = "NA";
                }
                String pkgUid = pkg + "#" + uid;
                if (pkgSet != null) {
                    pkgSet.add(pkgUid);
                } else {
                    pkgSet = new ArraySet();
                    pkgSet.add(pkgUid);
                    this.mLogRecordMap.put(logR, pkgSet);
                }
            }
            if (!this.mHandler.hasMessages(8)) {
                this.mHandler.sendEmptyMessageDelayed(8, 1000);
                return;
            }
            return;
        }
        StringBuilder buf = mSbLog;
        buf.setLength(0);
        buf.append("req:").append(requestSource.getDesc()).append(',');
        buf.append("alw:").append(alwType);
        buf.append('[').append(sbReason.toString()).append(']').append(',');
        buf.append("flg:").append(isSysApp ? 1 : 0).append(isAppStop ? 1 : 0);
        buf.append(fgFlags[0]).append(fgFlags[1]).append(fgFlags[2]).append(',');
        buf.append("proc:").append(uid).append(',');
        buf.append(procName).append(',');
        buf.append("call:").append(callerUid).append(',');
        buf.append(callerApp == null ? "null" : callerApp.processName).append(' ');
        buf.append("act:").append(action).append(',');
        if (isNeedPrintCmp(requestSource)) {
            buf.append("cmp:").append(compName);
        } else {
            buf.append("cmp:").append("NA");
        }
        AwareLog.i(TAG_SIMPLE, buf.toString());
    }

    private int getPackageAllowType(String procName, ApplicationInfo applicationInfo, int callerPid, int callerUid, ProcessRecord targetApp, ProcessRecord callerApp, AppStartSource requestSource, String compName, String action, int unPercetibleAlarm, boolean fromRecent) {
        int[] fgFlags = new int[]{-1, -1, -1};
        String pkg = applicationInfo.packageName;
        StringBuilder sbReason = new StringBuilder(3);
        HwAppStartupSetting startupSetting = getAppStartupSetting(pkg);
        boolean isSysApp = this.mDataMgr.isSystemBaseApp(applicationInfo);
        boolean existedPR = true;
        boolean reportData = true;
        HwActivityManagerService hwAMS = HwActivityManagerService.self();
        if (hwAMS != null) {
            if (targetApp == null) {
                existedPR = hwAMS.isProcessExistLocked(procName, applicationInfo.uid);
            }
            reportData = existedPR ^ 1;
        }
        boolean isAppStop = isApplicationStop(applicationInfo, existedPR);
        int policyType = getPolicyType(startupSetting, requestSource);
        int alwType = getStartupAllowType(applicationInfo, requestSource, callerApp, pkg, callerPid, callerUid, policyType, isAppStop, isSysApp, compName, action, sbReason, fgFlags, unPercetibleAlarm, fromRecent);
        boolean allow = alwType > 0;
        if (isNeedReportRecordToHSM(startupSetting, requestSource, alwType, isAppStop, isSysApp)) {
            saveAppStartupRecord(pkg, requestSource.getDesc(), callerUid, allow, isAppSelfStart(requestSource));
        }
        if (!allow || AppStartSource.SCHEDULE_RESTART.equals(requestSource)) {
            reportData = true;
        }
        updateStartupBigData(pkg, sbReason.toString(), isSysApp, reportData, alwType, policyType, requestSource, procName, applicationInfo.uid);
        if (this.DEBUG_DETAIL || (Log.HWINFO && reportData)) {
            printReqLog(requestSource, alwType, sbReason, isSysApp, isAppStop, fgFlags, applicationInfo.uid, procName, pkg, callerUid, callerApp, action, compName);
        }
        return alwType;
    }

    private int getStartupAllowType(ApplicationInfo applicationInfo, AppStartSource requestSource, ProcessRecord callerApp, String pkg, int callerPid, int callerUid, int policyType, boolean isAppStop, boolean isSysApp, String compName, String action, StringBuilder sbReason, int[] fgFlags, int unPercetibleAlarm, boolean fromRecent) {
        String reason = "";
        int alwType = 0;
        if (isForbidApp(pkg, requestSource, isAppStop)) {
            reason = AppStartReason.DEFAULT.getDesc();
        } else {
            if (isSubUserAppControl(applicationInfo.uid, requestSource)) {
                alwType = 4;
                reason = AppStartReason.DEFAULT.getDesc();
            } else if (policyType == 0 && (isNeedAutoAppStartManage(pkg, requestSource, callerUid) ^ 1) != 0) {
                alwType = 4;
                reason = AppStartReason.DEFAULT.getDesc();
            } else if (2 == policyType) {
                alwType = 3;
                reason = "U";
            } else {
                boolean[] isBtMediaBrowserCaller = new boolean[]{false};
                boolean[] isNotifyListenerCaller = new boolean[]{false};
                alwType = this.mDataMgr.getDefaultAllowedRequestType(applicationInfo, action, callerPid, callerUid, callerApp, requestSource, isAppStop, isSysApp, isBtMediaBrowserCaller, isNotifyListenerCaller, unPercetibleAlarm, fromRecent);
                if (alwType <= 0) {
                    boolean fgTarget = isAppForeground(applicationInfo.uid);
                    boolean fgCaller = isAppForeground(callerUid);
                    if (!fgCaller) {
                        fgCaller = AwareAppAssociate.getInstance().isRecentFgApp(callerUid);
                        fgFlags[2] = fgCaller ? 1 : 0;
                    }
                    fgFlags[0] = fgTarget ? 1 : 0;
                    fgFlags[1] = fgCaller ? 1 : 0;
                    AppStartPolicy policy = (AppStartPolicy) DecisionMaker.getInstance().decide(pkg, requestSource, new AwareAppStartStatusCache(applicationInfo.uid, callerUid, isSysApp, isAppStop, compName, action, fgCaller, fgTarget, applicationInfo.flags, isBtMediaBrowserCaller[0], isNotifyListenerCaller[0], unPercetibleAlarm), policyType);
                    alwType = policy.getPolicy() != 0 ? 4 : 0;
                    reason = policy.getReason();
                } else if (AppStartSource.THIRD_ACTIVITY.equals(requestSource)) {
                    reason = requestSource.getDesc() + AppStartReason.DEFAULT.getDesc();
                } else if (alwType == 1) {
                    reason = "O";
                } else if (alwType == 2) {
                    reason = "H";
                }
            }
        }
        if (isAllowAppWhenUpgrade(pkg, alwType, requestSource)) {
            alwType = 4;
            reason = AppStartReason.DEFAULT.getDesc();
        }
        if (sbReason != null) {
            sbReason.setLength(0);
            sbReason.append(reason);
        }
        return alwType;
    }

    private boolean isAllowAppWhenUpgrade(String pkgName, int alwType, AppStartSource requestSource) {
        boolean z;
        boolean allow = false;
        if (AppStartSource.THIRD_BROADCAST.equals(requestSource)) {
            z = true;
        } else {
            z = AppStartSource.SYSTEM_BROADCAST.equals(requestSource);
        }
        if (!z) {
            return false;
        }
        if (!MM_HOTA_AUTOSTART_HAS_DONE && alwType <= 0 && WECHAT_PACKAGE_NAME.equals(pkgName)) {
            if (this.mIsUpgrade) {
                AwareLog.i(TAG, "com.tencent.mm is allowed to auto start for MEDIA_MOUNTED when ota");
                allow = true;
            }
            MM_HOTA_AUTOSTART_HAS_DONE = true;
        }
        return allow;
    }

    private boolean isNeedAutoAppStartManage(String pkg, AppStartSource requestSource, int callerUid) {
        if (!this.mIsAbroadArea || this.mDataMgr.isAutoMngPkg(pkg)) {
            return true;
        }
        int appFrom = AppTypeRecoManager.getInstance().getAppWhereFrom(pkg);
        if (appFrom == 0) {
            return true;
        }
        return appFrom == 1 && isAbroadAppStartManager(requestSource) && !AwareIntelligentRecg.getInstance().isGmsCaller(callerUid);
    }

    private boolean isNeedReportRecordToHSM(HwAppStartupSetting startupSetting, AppStartSource requestSource, int alwType, boolean isAppStop, boolean isSysApp) {
        boolean z = true;
        if (isSysApp || isSubUser()) {
            return false;
        }
        if (startupSetting != null) {
            if (startupSetting.getShow(0) == 0) {
                return false;
            }
            if (startupSetting.getShow(isAppSelfStart(requestSource) ? 1 : 2) == 0) {
                return false;
            }
        }
        if (alwType <= 0) {
            return true;
        }
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues()[requestSource.ordinal()]) {
            case 3:
            case 5:
            case 7:
            case 10:
                if (!isAppStop || alwType == 1) {
                    z = false;
                } else if (alwType == 2) {
                    z = false;
                }
                return z;
            case 6:
            case 9:
                return false;
            default:
                return isAppStop;
        }
    }

    private void sendAppStartupChangedMsg() {
        this.mHandler.removeMessages(4);
        this.mHandler.sendEmptyMessageDelayed(4, DELAY_MILLS_NOTIFY_PG);
    }

    private void saveAppStartupRecord(String pkgName, String callerType, int callerUid, boolean result, boolean selfStart) {
        int size;
        int count = 1;
        boolean trimed = false;
        HsmRecord hr = new HsmRecord(pkgName, callerType, callerUid, result, selfStart, null);
        synchronized (this.mHsmRecordList) {
            Integer val = (Integer) this.mHsmRecordList.remove(hr);
            if (val != null) {
                count = val.intValue() + 1;
                trimed = true;
            }
            this.mHsmRecordList.put(hr, Integer.valueOf(count));
            size = this.mHsmRecordList.size();
        }
        if (size > 30) {
            this.mHandler.removeMessages(3);
            this.mHandler.sendEmptyMessage(3);
        } else if (!this.mHandler.hasMessages(3)) {
            this.mHandler.sendEmptyMessageDelayed(3, 30000);
        }
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "saveAppStartupRecord trimed=" + trimed + ", size=" + size + ", num=" + count + ", " + hr);
        }
    }

    private void startNotifyChangedService() {
        Intent intentService = new Intent(ACTION_APPSTARTUP_CHANGED);
        intentService.setPackage(PG_PACKAGE_NAME);
        this.mContext.sendBroadcastAsUser(intentService, UserHandle.ALL);
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "startNotifyChangedService called");
        }
    }

    /* JADX WARNING: Missing block: B:24:0x009f, code:
            r3 = new android.os.Bundle();
            r3.putStringArray("B_TARGET_PKG", r10);
            r3.putStringArray("B_CALL_TYPE", r14);
            r3.putIntArray("B_CALL_UID", r15);
            r3.putIntArray("B_RESULT", r11);
            r3.putIntArray("B_AUTO_START", r2);
            r3.putIntArray("B_COUNT", r4);
            r3.putLongArray("B_TIME_STAMP", r13);
            r9 = new android.content.Intent(ACTION_APPSTARTUP_RECORD);
            r9.setPackage(HSM_PACKAGE_NAME);
            r9.putExtras(r3);
     */
    /* JADX WARNING: Missing block: B:26:?, code:
            r20.mContext.startServiceAsUser(r9, android.os.UserHandle.CURRENT);
     */
    /* JADX WARNING: Missing block: B:32:0x0105, code:
            android.rms.iaware.AwareLog.e(TAG, "startRecordService catch IllegalStateException");
     */
    /* JADX WARNING: Missing block: B:34:0x0110, code:
            android.rms.iaware.AwareLog.e(TAG, "startRecordService catch SecurityException");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startRecordService() {
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "startRecordService called.");
        }
        synchronized (this.mHsmRecordList) {
            int size = this.mHsmRecordList.size();
            if (size <= 0) {
                return;
            }
            String[] pkgList = new String[size];
            String[] typeList = new String[size];
            int[] uidList = new int[size];
            int[] resList = new int[size];
            int[] autoList = new int[size];
            int[] countList = new int[size];
            long[] timeList = new long[size];
            for (int index = 0; index < size; index++) {
                HsmRecord hr = (HsmRecord) this.mHsmRecordList.keyAt(index);
                pkgList[index] = hr.pkg;
                typeList[index] = hr.callerType;
                uidList[index] = hr.callerUid;
                resList[index] = hr.result ? 1 : 0;
                autoList[index] = hr.autoStart ? 1 : 0;
                timeList[index] = hr.timeStamp;
                countList[index] = ((Integer) this.mHsmRecordList.valueAt(index)).intValue();
            }
            this.mHsmRecordList.clear();
        }
    }

    private void reportProcessStart(Message msg) {
        boolean iawareCtrl;
        int i;
        Bundle bundle = msg.getData();
        String pkgName = bundle.getString("PACKAGE");
        String hostingType = bundle.getString("HOST_TYPE");
        if ("activity".equals(hostingType) || AwareAppMngSort.ADJTYPE_SERVICE.equals(hostingType) || "broadcast".equals(hostingType)) {
            iawareCtrl = true;
        } else {
            iawareCtrl = "content provider".equals(hostingType);
        }
        if ("webview_service".equals(hostingType)) {
            pkgName = WebViewZygote.getPackageName();
        }
        SRMSDumpRadar instance = SRMSDumpRadar.getInstance();
        String[] strArr = new String[]{"T"};
        int[][] iArr = new int[1][];
        int[] iArr2 = new int[2];
        iArr2[0] = 1;
        if (iawareCtrl) {
            i = 0;
        } else {
            i = 1;
        }
        iArr2[1] = i;
        iArr[0] = iArr2;
        instance.updateStartupData(pkgName, strArr, iArr);
    }

    private void periodRemoveRestartData() {
        int removeCount = 0;
        long curTime = SystemClock.elapsedRealtime();
        synchronized (this.mRestartDataList) {
            for (int i = this.mRestartDataList.size() - 1; i >= 0; i--) {
                if (curTime - ((RestartData) this.mRestartDataList.valueAt(i)).timeStamp > DELAY_MILLS_REMOVE_RESTART_DATA) {
                    this.mRestartDataList.removeAt(i);
                    removeCount++;
                }
            }
        }
        this.mLastRemoveMsgTime = 0;
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "periodRemoveRestartData removeCount: " + removeCount);
        }
    }

    private void updateStartupBigData(String pkg, String reason, boolean isSysApp, boolean reportData, int alwType, int policyType, AppStartSource requestSource, String processName, int uid) {
        this.mAllowData.setDirty(true);
        if ((AppStartupFeature.isBetaUser() || !isSysApp) && !this.mIsAbroadArea && !isSubUser()) {
            if (alwType <= 0) {
                SRMSDumpRadar dumpRadar = SRMSDumpRadar.getInstance();
                if (policyType == 1) {
                    dumpRadar.updateStartupData(pkg, new String[]{"U"}, new int[]{0, 0, 1});
                } else if (policyType == 0) {
                    int[] smtFbdFmt = new int[]{0, 1};
                    dumpRadar.updateStartupData(pkg, new String[]{"I", reason}, smtFbdFmt, smtFbdFmt);
                }
            } else if (reportData) {
                if (AppStartSource.SCHEDULE_RESTART.equals(requestSource)) {
                    synchronized (this.mRestartDataList) {
                        this.mRestartDataList.put(pkg + "#" + processName, new RestartData(pkg, reason, alwType, policyType, null));
                    }
                    long curTime = SystemClock.elapsedRealtime();
                    if (curTime - this.mLastRemoveMsgTime > DELAY_MILLS_REMOVE_RESTART_DATA) {
                        this.mHandler.sendEmptyMessageDelayed(7, DELAY_MILLS_REMOVE_RESTART_DATA);
                        this.mLastRemoveMsgTime = curTime;
                    }
                } else if (!AwareIntelligentRecg.getInstance().isWebViewUid(uid)) {
                    if (AppStartSource.SYSTEM_BROADCAST.equals(requestSource) || AppStartSource.THIRD_BROADCAST.equals(requestSource)) {
                        this.mAllowData.set(reason, alwType, policyType);
                        this.mAllowData.setDirty(false);
                    }
                    updateAllowedBigData(pkg, reason, alwType, policyType, true);
                }
            }
        }
    }

    private void updateAllowedBigData(String pkg, String reason, int alwType, int policyType, boolean increase) {
        int increaseVal = increase ? 1 : -1;
        SRMSDumpRadar dumpRadar = SRMSDumpRadar.getInstance();
        String[] strArr;
        int[][] iArr;
        if (policyType == 2) {
            strArr = new String[]{"U"};
            iArr = new int[1][];
            iArr[0] = new int[]{increaseVal, 0, 0};
            dumpRadar.updateStartupData(pkg, strArr, iArr);
        } else if (policyType == 1) {
            strArr = new String[]{"U"};
            iArr = new int[1][];
            iArr[0] = new int[]{0, increaseVal, 0};
            dumpRadar.updateStartupData(pkg, strArr, iArr);
        } else if (policyType == 0) {
            int[] smtAlwFmt = new int[]{increaseVal, 0};
            int[] smtSingleAlwFmt = new int[]{increaseVal};
            if (alwType == 1) {
                dumpRadar.updateStartupData(pkg, new String[]{"I", "O"}, smtAlwFmt, smtSingleAlwFmt);
            } else if (alwType == 2) {
                dumpRadar.updateStartupData(pkg, new String[]{"I", "H"}, smtAlwFmt, smtSingleAlwFmt);
            } else {
                dumpRadar.updateStartupData(pkg, new String[]{"I", reason}, smtAlwFmt, smtAlwFmt);
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x000e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateBroadJobCtrlBigData(String pkg) {
        if (isPolicyEnabled() && !this.mAllowData.getDirty() && !isSubUser()) {
            updateAllowedBigData(pkg, this.mAllowData.reason, this.mAllowData.alwType, this.mAllowData.policyType, false);
        }
    }

    private int getPolicyType(HwAppStartupSetting startupSetting, AppStartSource requestSource) {
        if (startupSetting == null || 1 == startupSetting.getPolicy(0)) {
            return 0;
        }
        int value = startupSetting.getPolicy(isAppSelfStart(requestSource) ? 1 : 2);
        if (1 == value) {
            return 2;
        }
        if (value == 0) {
            return 1;
        }
        return 0;
    }

    private boolean isAppSelfStart(AppStartSource requestSource) {
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues()[requestSource.ordinal()]) {
            case 1:
            case 2:
            case 4:
            case 6:
            case 8:
                return true;
            default:
                return false;
        }
    }

    private boolean isAbroadAppStartManager(AppStartSource requestSource) {
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues()[requestSource.ordinal()]) {
            case 6:
            case 8:
                return true;
            default:
                return false;
        }
    }

    private void scheduleFastWriteCache(CacheType cacheType) {
        if (cacheType == CacheType.STARTUP_SETTING) {
            setAppStartupSyncFlag(true);
            this.mHandler.removeMessages(2);
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private void writeCacheFile(CacheType cacheType) {
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "writeCacheFile type=" + cacheType);
        }
        if (cacheType == CacheType.STARTUP_SETTING) {
            this.mDataMgr.flushStartupSettingToDisk();
            setAppStartupSyncFlag(false);
        }
    }

    private boolean isAppForeground(int uid) {
        if (AwareAppAssociate.isDealAsPkgUid(uid)) {
            return AwareAppAssociate.getInstance().isForeGroundApp(uid);
        }
        HwActivityManagerService hwAMS = HwActivityManagerService.self();
        if (hwAMS != null) {
            int uidState = hwAMS.iawareGetUidState(uid);
            if (uidState > this.mForegroundAppLevel || uidState == 18) {
                return false;
            }
        }
        return true;
    }

    private boolean isApplicationStop(ApplicationInfo applicationInfo, boolean existedPR) {
        if (AwareAppAssociate.isDealAsPkgUid(applicationInfo.uid)) {
            return (!existedPR ? AwareAppAssociate.getInstance().isPkgHasProc(applicationInfo.packageName) : 1) ^ 1;
        }
        HwActivityManagerService hwAMS = HwActivityManagerService.self();
        if (hwAMS != null) {
            return hwAMS.iawareGetUidProcNum(applicationInfo.uid) <= 0;
        } else {
            return false;
        }
    }

    public void reportPgClean(String pkg) {
        if (isPolicyEnabled() && this.mDataMgr.isPGForbidRestart(pkg)) {
            this.mDataMgr.addPgCleanApp(pkg);
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        if (args != null && pw != null) {
            String strBadCmd = "Bad command";
            String strSwitchOff = "App startup feature is disabled or not inited.";
            String strOn = "1";
            String strOff = "0";
            if (args.length < 3) {
                pw.println("Bad command");
            } else if (args.length == 3) {
                if ("cache".equals(args[2])) {
                    this.mDataMgr.dump(pw, args);
                } else if (isPolicyEnabled()) {
                    if ("info".equals(args[2])) {
                        pw.println("betaUser=" + AppStartupFeature.isBetaUser() + ", inited=" + this.mPolicyInited.get() + ", switch=" + AppStartupFeature.isAppStartupEnabled() + ", multiuser=" + this.mSubUserAppCtrl + ", userId=" + AwareAppAssociate.getInstance().getCurSwitchUser());
                    }
                    this.mDataMgr.dump(pw, args);
                } else {
                    pw.println("App startup feature is disabled or not inited.");
                }
            } else {
                String cmd = args[2];
                String param = args[3];
                if ("switch".equals(cmd)) {
                    if ("0".equals(param)) {
                        AppStartupFeature.setEnable(false);
                    } else if ("1".equals(param)) {
                        AppStartupFeature.setEnable(true);
                    } else {
                        pw.println("Bad command");
                    }
                } else if ("multiuser".equals(cmd)) {
                    if ("0".equals(param)) {
                        this.mSubUserAppCtrl = false;
                    } else if ("1".equals(param)) {
                        this.mSubUserAppCtrl = true;
                    } else {
                        pw.println("Bad command");
                    }
                } else {
                    dumpOthers(pw, args);
                }
            }
        }
    }

    private void dumpOthers(PrintWriter pw, String[] args) {
        String strBadCmd = "Bad command";
        String strSwitchOff = "App startup feature is disabled or not inited.";
        String strOnlyDiff = PPPOEStateMachine.PHASE_SERIALCONN;
        String strOn = "1";
        String strOff = "0";
        String cmd = args[2];
        String param = args[3];
        if (isPolicyEnabled()) {
            if ("log".equals(cmd)) {
                if ("0".equals(param)) {
                    this.DEBUG_DETAIL = false;
                } else if ("1".equals(param)) {
                    this.DEBUG_DETAIL = true;
                } else {
                    pw.println("Bad command");
                    return;
                }
            } else if ("cost".equals(cmd)) {
                if ("0".equals(param)) {
                    this.DEBUG_COST = false;
                } else if ("1".equals(param)) {
                    this.DEBUG_COST = true;
                } else {
                    pw.println("Bad command");
                    return;
                }
            } else if ("bigdata".equals(cmd)) {
                boolean clear = false;
                boolean onlyDiff = false;
                if ("0".equals(param)) {
                    clear = false;
                } else if ("1".equals(param)) {
                    clear = true;
                } else if (PPPOEStateMachine.PHASE_SERIALCONN.equals(param)) {
                    onlyDiff = true;
                } else {
                    pw.println("Bad command");
                    return;
                }
                String bigdata = SRMSDumpRadar.getInstance().saveStartupBigData(AppStartupFeature.isBetaUser(), clear, onlyDiff);
                pw.println(bigdata);
                pw.println("Total size: " + bigdata.length());
                return;
            }
            this.mDataMgr.dump(pw, args);
            return;
        }
        pw.println("App startup feature is disabled or not inited.");
    }
}
