package com.android.server.mtm.iaware.appmng.appstart;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.app.mtm.iaware.appmng.AppMngConstant;
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
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.webkit.WebViewZygote;
import com.android.server.am.ActivityRecord;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.ProcessRecord;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.wavemapping.chr.BuildBenefitStatisticsChrInfo;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupDataMgr;
import com.android.server.mtm.iaware.appmng.policy.AppStartPolicy;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCache;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCacheExt;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.srms.AppStartupFeature;
import com.android.server.rms.iaware.srms.BroadcastExFeature;
import com.android.server.rms.iaware.srms.SRMSDumpRadar;
import com.huawei.android.app.HwActivityManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class AwareAppStartupPolicy {
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
    private static final Set<String> WEARCALLER_ALLOWPKGS = new ArraySet();
    private static final StringBuilder mSbLog = new StringBuilder(256);
    private static final StringBuilder mSbLogMerge = new StringBuilder(256);
    private static AwareAppStartupPolicy sInstance = null;
    private boolean DEBUG_COST = false;
    private boolean DEBUG_DETAIL = false;
    private final RestartData mAllowData;
    private Context mContext = null;
    private AppStartupDataMgr mDataMgr = new AppStartupDataMgr();
    private int mForegroundAppLevel = 2;
    private int mForegroundSleepAppLevel = 5;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    private ArrayMap<HsmRecord, Integer> mHsmRecordList;
    private AtomicBoolean mInitFinished;
    private boolean mIsAbroadArea = false;
    private boolean mIsUpgrade;
    private long mLastRemoveMsgTime = 0;
    private ArrayMap<AppStartLogRecord, ArraySet<String>> mLogRecordMap;
    private AtomicBoolean mPolicyInited;
    private ArrayMap<String, RestartData> mRestartDataList;
    private boolean mSubUserAppCtrl = true;

    /* renamed from: com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource = new int[AppMngConstant.AppStartSource.values().length];

        static {
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.THIRD_ACTIVITY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.ALARM.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.SYSTEM_BROADCAST.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.JOB_SCHEDULE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.ACCOUNT_SYNC.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.SCHEDULE_RESTART.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.BIND_SERVICE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.START_SERVICE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.PROVIDER.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.THIRD_BROADCAST.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    private static class AppStartLogRecord {
        public String action;
        public String alw;
        public String caller;
        public int callerUid;
        public AppMngConstant.AppStartSource req;

        /* synthetic */ AppStartLogRecord(AppMngConstant.AppStartSource x0, String x1, int x2, String x3, String x4, AnonymousClass1 x5) {
            this(x0, x1, x2, x3, x4);
        }

        private AppStartLogRecord(AppMngConstant.AppStartSource requestSource, String alwDetail, int callerUid2, String callerApp, String action2) {
            this.req = requestSource;
            this.alw = alwDetail;
            this.callerUid = callerUid2;
            this.caller = callerApp;
            this.action = action2;
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * ((31 * ((31 * 17) + this.req.ordinal())) + this.alw.hashCode())) + this.callerUid)) + this.caller.hashCode())) + this.action.hashCode();
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == null || !(obj instanceof AppStartLogRecord)) {
                return false;
            }
            AppStartLogRecord o = (AppStartLogRecord) obj;
            if (this.req.equals(o.req) && this.alw.equals(o.alw) && this.callerUid == o.callerUid && this.caller.equals(o.caller) && this.action.equals(o.action)) {
                z = true;
            }
            return z;
        }
    }

    private enum CacheType {
        STARTUP_SETTING
    }

    private static class HsmRecord {
        /* access modifiers changed from: private */
        public boolean autoStart;
        /* access modifiers changed from: private */
        public String callerType;
        /* access modifiers changed from: private */
        public int callerUid;
        /* access modifiers changed from: private */
        public String pkg;
        /* access modifiers changed from: private */
        public boolean result;
        /* access modifiers changed from: private */
        public long timeStamp;

        /* synthetic */ HsmRecord(String x0, String x1, int x2, boolean x3, boolean x4, AnonymousClass1 x5) {
            this(x0, x1, x2, x3, x4);
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
            int i = 0;
            int hashCode = this.pkg != null ? this.pkg.hashCode() : 0;
            if (this.callerType != null) {
                i = this.callerType.hashCode();
            }
            return hashCode + i + (this.callerUid * ((true << (this.result ? 1 : 0)) + (this.autoStart ? 1 : 0)));
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == null || !(obj instanceof HsmRecord)) {
                return false;
            }
            HsmRecord o = (HsmRecord) obj;
            if (Objects.equals(this.pkg, o.pkg) && Objects.equals(this.callerType, o.callerType) && this.callerUid == o.callerUid && this.result == o.result && this.autoStart == o.autoStart) {
                z = true;
            }
            return z;
        }

        public String toString() {
            return "HsmRecord: {" + this.pkg + " type:" + this.callerType + " uid:" + this.callerUid + " res:" + this.result + " auto:" + this.autoStart + " time:" + this.timeStamp + '}';
        }
    }

    private static class RestartData {
        /* access modifiers changed from: private */
        public int alwType;
        private boolean dirty;
        /* access modifiers changed from: private */
        public String pkg;
        /* access modifiers changed from: private */
        public int policyType;
        /* access modifiers changed from: private */
        public String reason;
        /* access modifiers changed from: private */
        public long timeStamp;

        /* synthetic */ RestartData(String x0, String x1, int x2, int x3, AnonymousClass1 x4) {
            this(x0, x1, x2, x3);
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

        public void setDirty(boolean dirty2) {
            this.dirty = dirty2;
        }

        public boolean getDirty() {
            return this.dirty;
        }

        public void set(String reason2, int alwType2, int policyType2) {
            this.reason = reason2;
            this.alwType = alwType2;
            this.policyType = policyType2;
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

    static {
        WEARCALLER_ALLOWPKGS.add("com.huawei.iconnect");
    }

    private AwareAppStartupPolicy(Context context, HandlerThread mtmThread) {
        RestartData restartData = new RestartData(null, null, 0, 0, null);
        this.mAllowData = restartData;
        this.mIsUpgrade = false;
        this.mInitFinished = new AtomicBoolean(false);
        this.mPolicyInited = new AtomicBoolean(false);
        this.mRestartDataList = new ArrayMap<>();
        this.mHsmRecordList = new ArrayMap<>();
        this.mLogRecordMap = new ArrayMap<>();
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
        SystemProperties.set("persist.sys.appstart.sync", needSync ? "true" : "false");
        if (this.DEBUG_DETAIL) {
            AwareLog.e(TAG, "setAppStartupSyncFlag needSync=" + needSync);
        }
    }

    /* access modifiers changed from: private */
    public void policyInit() {
        long start = System.nanoTime();
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null && pm.isUpgrade()) {
            this.mIsUpgrade = true;
        }
        AwareLog.i(TAG, "policyInit mIsUpgrade=" + this.mIsUpgrade);
        this.mIsAbroadArea = AwareDefaultConfigList.isAbroadArea();
        DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.COMMON, this.mContext);
        DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_START, this.mContext);
        this.mDataMgr.initData(this.mContext);
        AwareIntelligentRecg.getInstance().appStartEnable(this.mDataMgr, this.mContext);
        setInitState(true);
        AwareLog.i(TAG, "policyInit finished, cost=" + ((System.nanoTime() - start) / 1000));
    }

    /* access modifiers changed from: private */
    public void initStartupSetting() {
        long start = System.nanoTime();
        this.mDataMgr.initStartupSetting();
        setInitState(true);
        AwareLog.i(TAG, "initStartupSetting finished, cost=" + ((System.nanoTime() - start) / 1000));
    }

    public void initSystemUidCache() {
        this.mDataMgr.initSystemUidCache(this.mContext);
    }

    private boolean isPolicyEnabled() {
        return this.mPolicyInited.get() && AppStartupFeature.isAppStartupEnabled();
    }

    private boolean isSubUser() {
        return AwareAppAssociate.getInstance().getCurSwitchUser() != 0;
    }

    private boolean isSubUserAppControl(int uid, AppMngConstant.AppStartSource requestSource) {
        boolean subUser = isSubUser();
        if (!this.mSubUserAppCtrl) {
            return subUser;
        }
        boolean z = false;
        if (!subUser) {
            return false;
        }
        if (AppMngConstant.AppStartSource.SCHEDULE_RESTART.equals(requestSource)) {
            return true;
        }
        if (UserHandle.getUserId(uid) != 0) {
            z = true;
        }
        return z;
    }

    public HwAppStartupSetting getAppStartupSetting(String pkgName) {
        if (!this.mInitFinished.get()) {
            return null;
        }
        return this.mDataMgr.getAppStartupSetting(pkgName);
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
        List<String> list = new ArrayList<>();
        for (HwAppStartupSetting item : settingList) {
            list.add(item.getPackageName());
        }
        return list;
    }

    public boolean updateAppStartupSettings(List<HwAppStartupSetting> settingList, boolean clearFirst) {
        if (!this.mInitFinished.get()) {
            AwareLog.i(TAG, "updateAppStartupSettings policy is not finish init");
            return false;
        }
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
        BroadcastExFeature.updateConfig();
        if (isPolicyEnabled()) {
            this.mDataMgr.updateCloudData();
        }
        return true;
    }

    public void notifyProcessStart(String pkgName, String process, String hostingType, int pid, int uid) {
        AwareIntelligentRecg.getInstance().setHwStopFlag(UserHandle.getUserId(uid), pkgName, false);
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

    private AppMngConstant.AppStartSource getReceiverReqSource(boolean isSpecCaller, boolean isAlarmFlag, int callerUid, ApplicationInfo applicationInfo, int unPercetibleAlarm) {
        if (isSpecCaller) {
            return isAlarmFlag ? AppMngConstant.AppStartSource.ALARM : AppMngConstant.AppStartSource.SYSTEM_BROADCAST;
        } else if (!isAlarmFlag) {
            return AppMngConstant.AppStartSource.THIRD_BROADCAST;
        } else {
            if (callerUid == applicationInfo.uid) {
                return AppMngConstant.AppStartSource.ALARM;
            }
            if (this.mDataMgr.isSystemBaseApp(applicationInfo)) {
                return AppMngConstant.AppStartSource.ALARM;
            }
            if (unPercetibleAlarm != 0) {
                return AppMngConstant.AppStartSource.ALARM;
            }
            return AppMngConstant.AppStartSource.THIRD_BROADCAST;
        }
    }

    private AppMngConstant.AppStartSource getServiceReqSource(ServiceInfo servInfo, Intent service, boolean isSrvBind, int hwFlag, int callerUid, int unPercetibleAlarm) {
        AppMngConstant.AppStartSource requestResource = AppMngConstant.AppStartSource.BIND_SERVICE;
        if ((hwFlag & 32) != 0) {
            AppMngConstant.AppStartSource requestResource2 = AppMngConstant.AppStartSource.JOB_SCHEDULE;
            service.setHwFlags(hwFlag & -33);
            return requestResource2;
        } else if ((hwFlag & 64) != 0) {
            return AppMngConstant.AppStartSource.ACCOUNT_SYNC;
        } else {
            if (isSrvBind) {
                return requestResource;
            }
            if ((hwFlag & 256) == 0) {
                return AppMngConstant.AppStartSource.START_SERVICE;
            }
            if (servInfo.applicationInfo.uid == callerUid) {
                return AppMngConstant.AppStartSource.ALARM;
            }
            if (unPercetibleAlarm != 0) {
                return AppMngConstant.AppStartSource.ALARM;
            }
            return AppMngConstant.AppStartSource.START_SERVICE;
        }
    }

    private AppMngConstant.AppStartSource getActivityReqSource(int uid, int callerUid, int hwFlag) {
        AppMngConstant.AppStartSource requestResource = AppMngConstant.AppStartSource.THIRD_ACTIVITY;
        if ((hwFlag & 256) == 0) {
            return requestResource;
        }
        if (uid == callerUid) {
            return AppMngConstant.AppStartSource.ALARM;
        }
        if ((hwFlag & 2048) == 0) {
            return AppMngConstant.AppStartSource.ALARM;
        }
        return requestResource;
    }

    public boolean shouldPreventSendReceiver(Intent intent, ResolveInfo resolveInfo, int callPid, int callerUid, ProcessRecord targetApp, ProcessRecord callerApp) {
        ResolveInfo resolveInfo2 = resolveInfo;
        int hwFlag = callerUid;
        ProcessRecord processRecord = callerApp;
        if (intent == null || resolveInfo2 == null) {
            return false;
        } else if (!isPolicyEnabled()) {
            return false;
        } else {
            long start = 0;
            if (this.DEBUG_COST) {
                start = System.nanoTime();
            }
            int callerPid = processRecord != null ? processRecord.pid : callPid;
            String action = intent.getAction();
            boolean isSpecCaller = this.mDataMgr.isSpecialCaller(hwFlag);
            int hwFlag2 = intent.getHwFlags();
            boolean z = true;
            boolean isAlarmFlag = (hwFlag2 & 256) != 0;
            int unPercetibleAlarm = 2;
            if (isAlarmFlag) {
                if ((hwFlag2 & 512) != 0) {
                    unPercetibleAlarm = 1;
                } else if ((hwFlag2 & 2048) != 0) {
                    unPercetibleAlarm = 0;
                } else if (hwFlag == 1000 && "android.intent.action.DATE_CHANGED".equals(action)) {
                    unPercetibleAlarm = 1;
                }
            }
            int unPercetibleAlarm2 = unPercetibleAlarm;
            ApplicationInfo ai = resolveInfo2.activityInfo.applicationInfo;
            ApplicationInfo applicationInfo = ai;
            int i = hwFlag2;
            int pkgAlwType = getPackageAllowType(resolveInfo2.activityInfo.processName, ai, callerPid, hwFlag, targetApp, callerApp, getReceiverReqSource(isSpecCaller, isAlarmFlag, hwFlag, ai, unPercetibleAlarm2), resolveInfo2.activityInfo.getComponentName().flattenToShortString(), action, unPercetibleAlarm2, false);
            if (this.DEBUG_COST) {
                AwareLog.i(TAG, "shouldPreventSendReceiver cost=" + ((System.nanoTime() - start) / 1000));
            }
            if (pkgAlwType > 0) {
                z = false;
            }
            return z;
        }
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid, ProcessRecord callerApp, int servFlag, boolean servExist, Intent service) {
        ServiceInfo serviceInfo = servInfo;
        int i = servFlag;
        boolean z = servExist;
        boolean z2 = false;
        if (serviceInfo == null || !isPolicyEnabled()) {
            return false;
        }
        long start = 0;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        boolean isSrvBind = i == 2;
        if (i == 0 || (isSrvBind && z)) {
            if (this.DEBUG_DETAIL != 0) {
                AwareLog.i(TAG, "shouldPreventStartService pkg=" + serviceInfo.applicationInfo.packageName + ", comp=" + serviceInfo.name + ", callerPid=" + callerPid + ", callerUid=" + callerUid + ", servFlag=" + i + ", servExist=" + z);
            } else {
                int i2 = callerPid;
                int i3 = callerUid;
            }
            return false;
        }
        int hwFlag = 0;
        String action = null;
        if (service != null) {
            hwFlag = service.getHwFlags();
            action = service.getAction();
        }
        int hwFlag2 = hwFlag;
        String action2 = action;
        int unPercetibleAlarm = 2;
        if (!((hwFlag2 & 256) == 0 || (hwFlag2 & 2048) == 0)) {
            unPercetibleAlarm = 0;
        }
        int unPercetibleAlarm2 = unPercetibleAlarm;
        AppMngConstant.AppStartSource requestResource = getServiceReqSource(serviceInfo, service, isSrvBind, hwFlag2, callerUid, unPercetibleAlarm2);
        if (!AppMngConstant.AppStartSource.ACCOUNT_SYNC.equals(requestResource) || (hwFlag2 & 128) == 0) {
            AppMngConstant.AppStartSource appStartSource = requestResource;
            int i4 = hwFlag2;
            int pkgAlwType = getPackageAllowType(serviceInfo.processName, serviceInfo.applicationInfo, callerPid, callerUid, null, callerApp, requestResource, servInfo.getComponentName().flattenToShortString(), action2, unPercetibleAlarm2, false);
            if (this.DEBUG_COST) {
                AwareLog.i(TAG, "shouldPreventStartService cost=" + ((System.nanoTime() - start) / 1000));
            }
            if (pkgAlwType <= 0) {
                z2 = true;
            }
            return z2;
        }
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "we donnot forbiden sync when user initiated");
        }
        return false;
    }

    public boolean shouldPreventStartActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record, int callerPid, int callerUid, ProcessRecord callerApp) {
        ActivityInfo activityInfo = aInfo;
        boolean z = false;
        if (activityInfo == null || intent == null || !isPolicyEnabled()) {
            return false;
        }
        long start = 0;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        int hwFlag = intent.getHwFlags();
        int i = callerUid;
        AppMngConstant.AppStartSource requestResource = getActivityReqSource(activityInfo.applicationInfo.uid, i, hwFlag);
        boolean fromRecent = false;
        if ((intent.getFlags() & HighBitsCompModeID.MODE_COLOR_ENHANCE) != 0) {
            fromRecent = true;
        }
        boolean fromRecent2 = fromRecent;
        int i2 = hwFlag;
        if (getPackageAllowType(activityInfo.processName, activityInfo.applicationInfo, callerPid, i, null, callerApp, requestResource, aInfo.getComponentName().flattenToShortString(), null, 2, fromRecent2) <= 0) {
            z = true;
        }
        boolean shouldPrevent = z;
        if (this.DEBUG_COST) {
            AwareLog.i(TAG, "shouldPreventStartActivity cost=" + ((System.nanoTime() - start) / 1000));
        }
        return shouldPrevent;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, ProcessRecord callerApp) {
        ProviderInfo providerInfo = cpi;
        boolean z = false;
        if (providerInfo == null || !isPolicyEnabled()) {
            return false;
        }
        long start = 0;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        int i = callerPid;
        int i2 = callerUid;
        ProcessRecord processRecord = callerApp;
        int pkgAlwType = getPackageAllowType(providerInfo.processName, providerInfo.applicationInfo, i, i2, null, processRecord, AppMngConstant.AppStartSource.PROVIDER, cpi.getComponentName().flattenToShortString(), null, 2, false);
        if (this.DEBUG_COST) {
            AwareLog.i(TAG, "shouldPreventStartProvider cost=" + ((System.nanoTime() - start) / 1000));
        }
        if (pkgAlwType <= 0) {
            z = true;
        }
        return z;
    }

    public boolean shouldPreventRestartService(ServiceInfo sInfo, boolean realStart) {
        int pkgAlwType;
        RestartData restartData;
        ServiceInfo serviceInfo = sInfo;
        boolean z = false;
        if (serviceInfo == null || !isPolicyEnabled()) {
            return false;
        }
        long start = 0;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        if (realStart) {
            String key = serviceInfo.applicationInfo.packageName + "#" + serviceInfo.processName;
            synchronized (this.mRestartDataList) {
                restartData = this.mRestartDataList.remove(key);
            }
            if (restartData != null) {
                if (!this.mIsAbroadArea && !isSubUser()) {
                    updateAllowedBigData(restartData.pkg, restartData.reason, restartData.alwType, restartData.policyType, true);
                }
                pkgAlwType = restartData.alwType;
            } else {
                pkgAlwType = 4;
            }
            AwareLog.i(TAG_SIMPLE, "shouldPreventRestartService remove " + key + " " + restartData);
        } else {
            pkgAlwType = getPackageAllowType(serviceInfo.processName, serviceInfo.applicationInfo, Binder.getCallingPid(), Binder.getCallingUid(), null, null, AppMngConstant.AppStartSource.SCHEDULE_RESTART, sInfo.getComponentName().flattenToShortString(), null, 2, false);
        }
        if (this.DEBUG_COST) {
            AwareLog.i(TAG, "shouldPreventRestartService cost=" + ((System.nanoTime() - start) / 1000));
        }
        if (pkgAlwType <= 0) {
            z = true;
        }
        return z;
    }

    private boolean isForbidApp(String pkg, AppMngConstant.AppStartSource requestSource, boolean isAppStop, ProcessRecord callerApp) {
        if (!this.mDataMgr.isPgCleanApp(pkg)) {
            return false;
        }
        if (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[requestSource.ordinal()] == 1) {
            this.mDataMgr.removePgCleanApp(pkg);
            return false;
        } else if (!isAppStop) {
            return false;
        } else {
            if (callerApp == null || callerApp.pkgList == null || callerApp.pkgList.isEmpty() || callerApp.pkgList.keyAt(0) == null || !WEARCALLER_ALLOWPKGS.contains(callerApp.pkgList.keyAt(0))) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x003b, code lost:
        r3 = 0;
        r4 = r1.size();
        r5 = mSbLogMerge;
        r6 = r1.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004a, code lost:
        if (r6.hasNext() == false) goto L_0x000a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004c, code lost:
        r7 = r6.next();
        r8 = r3 % 10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0056, code lost:
        if (r8 != 0) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0058, code lost:
        r5.setLength(0);
        r5.append("req:");
        r5.append(r0.req.getDesc());
        r5.append(',');
        r5.append("alw:");
        r5.append(r0.alw);
        r5.append(',');
        r5.append("call:");
        r5.append(r0.callerUid);
        r5.append(',');
        r5.append(r0.caller);
        r5.append(',');
        r5.append("act:");
        r5.append(r0.action);
        r5.append(' ');
        r5.append("{");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00a5, code lost:
        if (r7 == null) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00ad, code lost:
        if (r7.startsWith(PKG_PREFIX) == false) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00af, code lost:
        r7 = r7.substring(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00b5, code lost:
        if (r8 == 9) goto L_0x00c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00b9, code lost:
        if (r3 != (r4 - 1)) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00bc, code lost:
        r5.append(r7);
        r5.append(',');
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00c3, code lost:
        r5.append(r7);
        r5.append("}");
        android.rms.iaware.AwareLog.i(TAG_SIMPLE, r5.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00d5, code lost:
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0036, code lost:
        if (r0 == null) goto L_0x000a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0038, code lost:
        if (r1 != null) goto L_0x003b;
     */
    public void printMergeReqLog() {
        int pkgPrefixStart = PKG_PREFIX.length() - 1;
        while (true) {
            synchronized (this.mLogRecordMap) {
                Iterator<Map.Entry<AppStartLogRecord, ArraySet<String>>> it = this.mLogRecordMap.entrySet().iterator();
                if (it.hasNext()) {
                    Map.Entry<AppStartLogRecord, ArraySet<String>> entry = it.next();
                    AppStartLogRecord logR = entry.getKey();
                    ArraySet<String> pkgSet = entry.getValue();
                    it.remove();
                } else {
                    return;
                }
            }
        }
    }

    private boolean isMergeReqLog(AppMngConstant.AppStartSource requestSource, int alwType) {
        if (this.DEBUG_DETAIL || alwType > 0) {
            return false;
        }
        switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[requestSource.ordinal()]) {
            case 1:
            case 2:
                return false;
            default:
                return true;
        }
    }

    private boolean isNeedPrintCmp(AppMngConstant.AppStartSource requestSource) {
        if (this.DEBUG_DETAIL) {
            return true;
        }
        switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[requestSource.ordinal()]) {
            case 3:
            case 4:
            case 5:
            case 6:
                return false;
            default:
                return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x008d, code lost:
        if (r1.mHandler.hasMessages(8) != false) goto L_0x0096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x008f, code lost:
        r1.mHandler.sendEmptyMessageDelayed(8, 1000);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0096, code lost:
        r10 = r17;
        r6 = r20;
        r7 = r23;
        r11 = r25;
        r8 = r28;
        r9 = r5;
        r5 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        return;
     */
    private void printReqLog(AppMngConstant.AppStartSource requestSource, int alwType, StringBuilder sbReason, boolean isSysApp, boolean isAppStop, int[] fgFlags, int uid, String procName, String pkg, int callerUid, ProcessRecord callerApp, String action, String compName) {
        String pkg2;
        int i = uid;
        ProcessRecord processRecord = callerApp;
        String str = action;
        if (isMergeReqLog(requestSource, alwType)) {
            AppStartLogRecord logR = new AppStartLogRecord(requestSource, Integer.toString(alwType) + '[' + sbReason.toString() + ']', callerUid, processRecord == null ? "null" : processRecord.processName, str == null ? "null" : str, null);
            synchronized (this.mLogRecordMap) {
                try {
                    ArraySet<String> pkgSet = this.mLogRecordMap.get(logR);
                    if (pkg == null) {
                        pkg2 = "NA";
                    } else {
                        pkg2 = pkg;
                    }
                    try {
                        String pkgUid = pkg2 + "#" + i;
                        if (pkgSet != null) {
                            pkgSet.add(pkgUid);
                        } else {
                            ArraySet arraySet = new ArraySet();
                            arraySet.add(pkgUid);
                            this.mLogRecordMap.put(logR, arraySet);
                        }
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    String str2 = pkg;
                    throw th;
                }
            }
        } else {
            StringBuilder buf = mSbLog;
            buf.setLength(0);
            buf.append("req:");
            buf.append(requestSource.getDesc());
            buf.append(',');
            buf.append("alw:");
            buf.append(alwType);
            buf.append('[');
            buf.append(sbReason.toString());
            buf.append(']');
            buf.append(',');
            buf.append("flg:");
            buf.append(isSysApp ? 1 : 0);
            buf.append(isAppStop ? 1 : 0);
            buf.append(fgFlags[0]);
            buf.append(fgFlags[1]);
            buf.append(fgFlags[2]);
            buf.append(',');
            buf.append("proc:");
            buf.append(i);
            buf.append(',');
            buf.append(procName);
            buf.append(',');
            buf.append("call:");
            buf.append(callerUid);
            buf.append(',');
            buf.append(processRecord == null ? "null" : processRecord.processName);
            buf.append(' ');
            buf.append("act:");
            buf.append(str);
            buf.append(',');
            if (isNeedPrintCmp(requestSource)) {
                buf.append("cmp:");
                buf.append(compName);
            } else {
                String str3 = compName;
                buf.append("cmp:");
                buf.append("NA");
            }
            AwareLog.i(TAG_SIMPLE, buf.toString());
            String str4 = pkg;
        }
    }

    private int getPackageAllowType(String procName, ApplicationInfo applicationInfo, int callerPid, int callerUid, ProcessRecord targetApp, ProcessRecord callerApp, AppMngConstant.AppStartSource requestSource, String compName, String action, int unPercetibleAlarm, boolean fromRecent) {
        boolean reportData;
        AppMngConstant.AppStartSource appStartSource;
        StringBuilder sbReason;
        ApplicationInfo applicationInfo2 = applicationInfo;
        AppMngConstant.AppStartSource appStartSource2 = requestSource;
        int[] fgFlags = {-1, -1, -1};
        String pkg = applicationInfo2.packageName;
        StringBuilder sbReason2 = new StringBuilder(3);
        HwAppStartupSetting startupSetting = getAppStartupSetting(pkg);
        boolean isSysApp = this.mDataMgr.isSystemBaseApp(applicationInfo2);
        boolean existedPR = true;
        if (targetApp == null) {
            existedPR = HwActivityManager.isProcessExistLocked(procName, applicationInfo2.uid);
        } else {
            String str = procName;
        }
        boolean existedPR2 = existedPR;
        boolean z = false;
        boolean reportData2 = !existedPR2;
        boolean isAppStop = isApplicationStop(applicationInfo2, existedPR2);
        int policyType = getPolicyType(startupSetting, appStartSource2);
        boolean z2 = existedPR2;
        HwAppStartupSetting startupSetting2 = startupSetting;
        StringBuilder sbReason3 = sbReason2;
        String pkg2 = pkg;
        int alwType = getStartupAllowType(applicationInfo2, appStartSource2, callerApp, pkg, callerPid, callerUid, policyType, isAppStop, isSysApp, compName, action, sbReason3, fgFlags, unPercetibleAlarm, fromRecent);
        if (alwType > 0) {
            z = true;
        }
        boolean allow = z;
        if (isNeedReportRecordToHSM(startupSetting2, requestSource, alwType, isAppStop, isSysApp)) {
            saveAppStartupRecord(pkg2, requestSource.getDesc(), callerUid, allow, isAppSelfStart(requestSource));
        }
        if (allow) {
            appStartSource = requestSource;
            if (!AppMngConstant.AppStartSource.SCHEDULE_RESTART.equals(appStartSource)) {
                reportData = reportData2;
                sbReason = sbReason3;
                ApplicationInfo applicationInfo3 = applicationInfo;
                updateStartupBigData(pkg2, sbReason.toString(), isSysApp, reportData, alwType, policyType, appStartSource, procName, applicationInfo3.uid);
                if (!this.DEBUG_DETAIL || (Log.HWINFO && reportData)) {
                    printReqLog(appStartSource, alwType, sbReason, isSysApp, isAppStop, fgFlags, applicationInfo3.uid, procName, pkg2, callerUid, callerApp, action, compName);
                } else {
                    StringBuilder sb = sbReason;
                }
                return alwType;
            }
        } else {
            appStartSource = requestSource;
        }
        reportData = true;
        sbReason = sbReason3;
        ApplicationInfo applicationInfo32 = applicationInfo;
        updateStartupBigData(pkg2, sbReason.toString(), isSysApp, reportData, alwType, policyType, appStartSource, procName, applicationInfo32.uid);
        if (!this.DEBUG_DETAIL) {
        }
        printReqLog(appStartSource, alwType, sbReason, isSysApp, isAppStop, fgFlags, applicationInfo32.uid, procName, pkg2, callerUid, callerApp, action, compName);
        return alwType;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v1, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v3, resolved type: boolean} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00fc  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00fe  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x015f  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x016c  */
    private int getStartupAllowType(ApplicationInfo applicationInfo, AppMngConstant.AppStartSource requestSource, ProcessRecord callerApp, String pkg, int callerPid, int callerUid, int policyType, boolean isAppStop, boolean isSysApp, String compName, String action, StringBuilder sbReason, int[] fgFlags, int unPercetibleAlarm, boolean fromRecent) {
        String reason;
        AwareAppStartupPolicy awareAppStartupPolicy;
        int alwType;
        String str;
        AppMngConstant.AppStartSource appStartSource;
        StringBuilder sb;
        boolean z;
        ApplicationInfo applicationInfo2 = applicationInfo;
        AppMngConstant.AppStartSource appStartSource2 = requestSource;
        String str2 = pkg;
        int i = callerUid;
        int i2 = policyType;
        StringBuilder sb2 = sbReason;
        String reason2 = "";
        AwareAppStartStatusCacheExt statusCacheExt = new AwareAppStartStatusCacheExt();
        statusCacheExt.mAbroad = this.mIsAbroadArea;
        statusCacheExt.mGmsNeedCtrl = AwareIntelligentRecg.getInstance().isGmsAppAndNeedCtrl(str2);
        int alwType2 = 0;
        ProcessRecord processRecord = callerApp;
        if (isForbidApp(str2, appStartSource2, isAppStop, processRecord)) {
            reason = AppMngConstant.AppStartReason.DEFAULT.getDesc();
        } else if (isSubUserAppControl(applicationInfo2.uid, appStartSource2)) {
            alwType2 = 4;
            reason = AppMngConstant.AppStartReason.DEFAULT.getDesc();
        } else if (i2 == 0 && !isNeedAutoAppStartManage(str2, appStartSource2, i, statusCacheExt)) {
            alwType2 = 4;
            reason = AppMngConstant.AppStartReason.DEFAULT.getDesc();
        } else if (2 == i2) {
            alwType2 = 3;
            reason = "U";
        } else {
            boolean[] isBtMediaBrowserCaller = {false};
            boolean[] isNotifyListenerCaller = {false};
            AwareAppStartStatusCacheExt statusCacheExt2 = statusCacheExt;
            appStartSource = appStartSource2;
            alwType = this.mDataMgr.getDefaultAllowedRequestType(applicationInfo2, action, callerPid, i, processRecord, appStartSource2, isAppStop, isSysApp, isBtMediaBrowserCaller, isNotifyListenerCaller, unPercetibleAlarm, fromRecent, statusCacheExt2);
            if (alwType > 0) {
                if (AppMngConstant.AppStartSource.THIRD_ACTIVITY.equals(appStartSource)) {
                    reason2 = requestSource.getDesc() + AppMngConstant.AppStartReason.DEFAULT.getDesc();
                } else if (alwType == 1) {
                    reason2 = "O";
                } else if (alwType == 2) {
                    reason2 = "H";
                }
                str = pkg;
                int i3 = policyType;
                awareAppStartupPolicy = this;
            } else {
                int isAppForeground = isAppForeground(applicationInfo2.uid);
                int i4 = callerUid;
                boolean fgCaller = isAppForeground(i4);
                if (!fgCaller) {
                    if (AwareAppAssociate.getInstance().isRecentFgApp(i4)) {
                        int i5 = callerPid;
                    } else if (!isAppForegroundExt(appStartSource, callerPid)) {
                        z = false;
                        fgCaller = z;
                        fgFlags[2] = !fgCaller ? 1 : 0;
                    }
                    z = true;
                    fgCaller = z;
                    fgFlags[2] = !fgCaller ? 1 : 0;
                } else {
                    int i6 = callerPid;
                }
                boolean fgCaller2 = fgCaller;
                fgFlags[0] = isAppForeground;
                fgFlags[1] = fgCaller2 ? 1 : 0;
                awareAppStartupPolicy = this;
                int i7 = alwType;
                AwareAppStartStatusCache status = new AwareAppStartStatusCache(applicationInfo2.uid, i4, isSysApp, isAppStop, compName, action, fgCaller2, isAppForeground, applicationInfo2.flags, isBtMediaBrowserCaller[0], isNotifyListenerCaller[0], unPercetibleAlarm, statusCacheExt2);
                str = pkg;
                AppStartPolicy policy = (AppStartPolicy) DecisionMaker.getInstance().decide(str, appStartSource, status, policyType);
                int alwType3 = policy.getPolicy() != 0 ? 4 : 0;
                reason = policy.getReason();
                alwType = alwType3;
            }
            if (awareAppStartupPolicy.isAllowAppWhenUpgrade(str, alwType, appStartSource)) {
                alwType = 4;
                reason = AppMngConstant.AppStartReason.DEFAULT.getDesc();
            }
            String reason3 = reason;
            sb = sbReason;
            if (sb != null) {
                sb.setLength(0);
                sb.append(reason3);
            }
            return alwType;
        }
        awareAppStartupPolicy = this;
        int i8 = i2;
        str = str2;
        appStartSource = appStartSource2;
        alwType = alwType2;
        if (awareAppStartupPolicy.isAllowAppWhenUpgrade(str, alwType, appStartSource)) {
        }
        String reason32 = reason;
        sb = sbReason;
        if (sb != null) {
        }
        return alwType;
    }

    private boolean isAllowAppWhenUpgrade(String pkgName, int alwType, AppMngConstant.AppStartSource requestSource) {
        boolean allow = false;
        if (!AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(requestSource) && !AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(requestSource)) {
            return false;
        }
        String topImCN = AwareIntelligentRecg.getInstance().getActTopIMCN();
        if (!MM_HOTA_AUTOSTART_HAS_DONE && alwType <= 0 && topImCN != null && topImCN.equals(pkgName)) {
            if (this.mIsUpgrade) {
                AwareLog.i(TAG, topImCN + " is allowed to auto start for MEDIA_MOUNTED when ota");
                allow = true;
            }
            MM_HOTA_AUTOSTART_HAS_DONE = true;
        }
        return allow;
    }

    private boolean isAppForegroundExt(AppMngConstant.AppStartSource requestSource, int callerPid) {
        if (AppMngConstant.AppStartSource.THIRD_ACTIVITY.equals(requestSource)) {
            return AwareAppAssociate.getInstance().isVisibleWindow(callerPid);
        }
        return false;
    }

    private boolean isNeedAutoAppStartManage(String pkg, AppMngConstant.AppStartSource requestSource, int callerUid, AwareAppStartStatusCacheExt statusCacheExt) {
        if (!this.mIsAbroadArea) {
            return true;
        }
        statusCacheExt.mAppSrcRange = AwareIntelligentRecg.getInstance().getAppStartSpecAppSrcRangeResult(pkg);
        if (statusCacheExt.mAppSrcRange != 0 && AwareIntelligentRecg.getInstance().isGmsCaller(callerUid)) {
            return false;
        }
        return true;
    }

    private boolean isNeedReportRecordToHSM(HwAppStartupSetting startupSetting, AppMngConstant.AppStartSource requestSource, int alwType, boolean isAppStop, boolean isSysApp) {
        boolean z = false;
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
        int i = AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[requestSource.ordinal()];
        if (i != 1) {
            switch (i) {
                case 6:
                    break;
                case 7:
                case 8:
                case 9:
                case 10:
                    if (!(!isAppStop || alwType == 1 || alwType == 2)) {
                        z = true;
                    }
                    return z;
                default:
                    return isAppStop;
            }
        }
        return false;
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
            Integer val = this.mHsmRecordList.remove(hr);
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
            this.mHandler.sendEmptyMessageDelayed(3, HwArbitrationDEFS.DelayTimeMillisA);
        }
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "saveAppStartupRecord trimed=" + trimed + ", size=" + size + ", num=" + count + ", " + hr);
        }
    }

    /* access modifiers changed from: private */
    public void startNotifyChangedService() {
        Intent intentService = new Intent(ACTION_APPSTARTUP_CHANGED);
        intentService.setPackage(PG_PACKAGE_NAME);
        this.mContext.sendBroadcastAsUser(intentService, UserHandle.ALL);
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "startNotifyChangedService called");
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x007b, code lost:
        r7 = new android.os.Bundle();
        r7.putStringArray("B_TARGET_PKG", r0);
        r7.putStringArray("B_CALL_TYPE", r1);
        r7.putIntArray("B_CALL_UID", r2);
        r7.putIntArray("B_RESULT", r3);
        r7.putIntArray("B_AUTO_START", r4);
        r7.putIntArray("B_COUNT", r5);
        r7.putLongArray("B_TIME_STAMP", r6);
        r8 = new android.content.Intent(ACTION_APPSTARTUP_RECORD);
        r8.setPackage("com.huawei.systemmanager");
        r8.putExtras(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r13.mContext.startServiceAsUser(r8, android.os.UserHandle.CURRENT);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00bb, code lost:
        android.rms.iaware.AwareLog.e(TAG, "startRecordService catch IllegalStateException");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00c5, code lost:
        android.rms.iaware.AwareLog.e(TAG, "startRecordService catch SecurityException");
     */
    public void startRecordService() {
        if (this.DEBUG_DETAIL) {
            AwareLog.i(TAG, "startRecordService called.");
        }
        synchronized (this.mHsmRecordList) {
            int size = this.mHsmRecordList.size();
            if (size > 0) {
                String[] pkgList = new String[size];
                String[] typeList = new String[size];
                int[] uidList = new int[size];
                int[] resList = new int[size];
                int[] autoList = new int[size];
                int[] countList = new int[size];
                long[] timeList = new long[size];
                for (int index = 0; index < size; index++) {
                    HsmRecord hr = this.mHsmRecordList.keyAt(index);
                    pkgList[index] = hr.pkg;
                    typeList[index] = hr.callerType;
                    uidList[index] = hr.callerUid;
                    resList[index] = hr.result;
                    autoList[index] = hr.autoStart;
                    timeList[index] = hr.timeStamp;
                    countList[index] = this.mHsmRecordList.valueAt(index).intValue();
                }
                this.mHsmRecordList.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    public void reportProcessStart(Message msg) {
        Bundle bundle = msg.getData();
        String pkgName = bundle.getString("PACKAGE");
        String hostingType = bundle.getString("HOST_TYPE");
        boolean iawareCtrl = "activity".equals(hostingType) || AwareAppMngSort.ADJTYPE_SERVICE.equals(hostingType) || "broadcast".equals(hostingType) || "content provider".equals(hostingType);
        if ("webview_service".equals(hostingType)) {
            pkgName = WebViewZygote.getPackageName();
        }
        SRMSDumpRadar instance = SRMSDumpRadar.getInstance();
        String[] strArr = {"T"};
        int[][] iArr = new int[1][];
        int[] iArr2 = new int[2];
        iArr2[0] = 1;
        iArr2[1] = iawareCtrl ? 0 : 1;
        iArr[0] = iArr2;
        instance.updateStartupData(pkgName, strArr, iArr);
    }

    /* access modifiers changed from: private */
    public void periodRemoveRestartData() {
        int removeCount = 0;
        long curTime = SystemClock.elapsedRealtime();
        synchronized (this.mRestartDataList) {
            for (int i = this.mRestartDataList.size() - 1; i >= 0; i--) {
                if (curTime - this.mRestartDataList.valueAt(i).timeStamp > DELAY_MILLS_REMOVE_RESTART_DATA) {
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

    private void updateStartupBigData(String pkg, String reason, boolean isSysApp, boolean reportData, int alwType, int policyType, AppMngConstant.AppStartSource requestSource, String processName, int uid) {
        String str = pkg;
        String str2 = reason;
        int i = alwType;
        int i2 = policyType;
        AppMngConstant.AppStartSource appStartSource = requestSource;
        this.mAllowData.setDirty(true);
        if ((AppStartupFeature.isBetaUser() || !isSysApp) && !this.mIsAbroadArea && !isSubUser()) {
            if (i <= 0) {
                SRMSDumpRadar dumpRadar = SRMSDumpRadar.getInstance();
                if (i2 == 1) {
                    dumpRadar.updateStartupData(str, new String[]{"U"}, new int[]{0, 0, 1});
                } else if (i2 == 0) {
                    int[] smtFbdFmt = {0, 1};
                    dumpRadar.updateStartupData(str, new String[]{"I", str2}, smtFbdFmt, smtFbdFmt);
                }
            } else if (reportData) {
                if (AppMngConstant.AppStartSource.SCHEDULE_RESTART.equals(appStartSource)) {
                    synchronized (this.mRestartDataList) {
                        RestartData restartData = r1;
                        RestartData restartData2 = new RestartData(str, str2, i, i2, null);
                        this.mRestartDataList.put(str + "#" + processName, restartData);
                    }
                    long curTime = SystemClock.elapsedRealtime();
                    if (curTime - this.mLastRemoveMsgTime > DELAY_MILLS_REMOVE_RESTART_DATA) {
                        this.mHandler.sendEmptyMessageDelayed(7, DELAY_MILLS_REMOVE_RESTART_DATA);
                        this.mLastRemoveMsgTime = curTime;
                    }
                } else {
                    if (!AwareIntelligentRecg.getInstance().isWebViewUid(uid)) {
                        if (AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(appStartSource) || AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(appStartSource)) {
                            this.mAllowData.set(str2, i, i2);
                            this.mAllowData.setDirty(false);
                        }
                        updateAllowedBigData(str, str2, i, i2, true);
                    }
                }
            }
            int i3 = uid;
        }
    }

    private void updateAllowedBigData(String pkg, String reason, int alwType, int policyType, boolean increase) {
        int increaseVal = increase ? 1 : -1;
        SRMSDumpRadar dumpRadar = SRMSDumpRadar.getInstance();
        if (policyType == 2) {
            dumpRadar.updateStartupData(pkg, new String[]{"U"}, new int[]{increaseVal, 0, 0});
        } else if (policyType == 1) {
            dumpRadar.updateStartupData(pkg, new String[]{"U"}, new int[]{0, increaseVal, 0});
        } else if (policyType == 0) {
            int[] smtAlwFmt = {increaseVal, 0};
            int[] smtSingleAlwFmt = {increaseVal};
            if (alwType == 1) {
                dumpRadar.updateStartupData(pkg, new String[]{"I", "O"}, smtAlwFmt, smtSingleAlwFmt);
            } else if (alwType == 2) {
                dumpRadar.updateStartupData(pkg, new String[]{"I", "H"}, smtAlwFmt, smtSingleAlwFmt);
            } else {
                dumpRadar.updateStartupData(pkg, new String[]{"I", reason}, smtAlwFmt, smtAlwFmt);
            }
        }
    }

    public void updateBroadJobCtrlBigData(String pkg) {
        if (isPolicyEnabled() && !this.mAllowData.getDirty() && !isSubUser()) {
            updateAllowedBigData(pkg, this.mAllowData.reason, this.mAllowData.alwType, this.mAllowData.policyType, false);
        }
    }

    private int getPolicyType(HwAppStartupSetting startupSetting, AppMngConstant.AppStartSource requestSource) {
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

    public static boolean isAppSelfStart(AppMngConstant.AppStartSource requestSource) {
        if (requestSource == null) {
            return false;
        }
        switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[requestSource.ordinal()]) {
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
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

    /* access modifiers changed from: private */
    public void writeCacheFile(CacheType cacheType) {
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
            if (uidState <= this.mForegroundAppLevel || uidState > this.mForegroundSleepAppLevel) {
                if (uidState > this.mForegroundSleepAppLevel || uidState == 19) {
                    return false;
                }
            } else if (AwareAppAssociate.getInstance().isForeGroundApp(uid)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean isApplicationStop(ApplicationInfo applicationInfo, boolean existedPR) {
        boolean isAppStop = false;
        if (AwareAppAssociate.isDealAsPkgUid(applicationInfo.uid)) {
            if (!existedPR && !AwareAppAssociate.getInstance().isPkgHasProc(applicationInfo.packageName)) {
                isAppStop = true;
            }
            return isAppStop;
        }
        HwActivityManagerService hwAMS = HwActivityManagerService.self();
        if (hwAMS == null) {
            return false;
        }
        if (hwAMS.iawareGetUidProcNum(applicationInfo.uid) <= 0) {
            isAppStop = true;
        }
        return isAppStop;
    }

    public void reportPgClean(String pkg) {
        if (isPolicyEnabled() && this.mDataMgr.isPGForbidRestart(pkg)) {
            this.mDataMgr.addPgCleanApp(pkg);
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        if (args != null && pw != null) {
            if (args.length < 3) {
                pw.println("Bad command");
            } else if (args.length == 3) {
                if ("cache".equals(args[2])) {
                    this.mDataMgr.dump(pw, args);
                } else if (!isPolicyEnabled()) {
                    pw.println("App startup feature is disabled or not inited.");
                } else {
                    if ("info".equals(args[2])) {
                        pw.println("betaUser=" + AppStartupFeature.isBetaUser() + ", inited=" + this.mPolicyInited.get() + ", switch=" + AppStartupFeature.isAppStartupEnabled() + ", multiuser=" + this.mSubUserAppCtrl + ", userId=" + AwareAppAssociate.getInstance().getCurSwitchUser());
                    }
                    this.mDataMgr.dump(pw, args);
                }
            } else {
                String cmd = args[2];
                String param = args[3];
                if (BuildBenefitStatisticsChrInfo.E909009052_TOTALSWITCH_INT.equals(cmd)) {
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
        String cmd = args[2];
        String param = args[3];
        if (!isPolicyEnabled()) {
            pw.println("App startup feature is disabled or not inited.");
            return;
        }
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
            } else if ("2".equals(param)) {
                onlyDiff = true;
            } else {
                pw.println("Bad command");
                return;
            }
            String bigdata = SRMSDumpRadar.getInstance().saveStartupBigData(AppStartupFeature.isBetaUser(), clear, onlyDiff);
            pw.println(bigdata);
            pw.println("Total size: " + bigdata.length());
            return;
        } else if ("widgetinterval".equals(cmd)) {
            try {
                AwareIntelligentRecg.getInstance().dumpWidgetUpdateInterval(pw, Integer.parseInt(param));
                return;
            } catch (NumberFormatException e) {
                pw.println("widgetinterval value error");
                return;
            }
        }
        this.mDataMgr.dump(pw, args);
    }
}
