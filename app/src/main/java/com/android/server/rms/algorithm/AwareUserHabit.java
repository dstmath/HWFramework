package com.android.server.rms.algorithm;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver.Stub;
import android.app.IUserSwitchObserver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.rms.iaware.LogIAware;
import android.rms.iaware.StatisticsData;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.os.BackgroundThread;
import com.android.server.jankshield.TableJankBd;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.algorithm.AwareUserHabitAlgorithm.HabitProtectListChangeListener;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AwareUserHabit {
    private static final int ACTIVITIES_CHANGED_MESSAGE_DELAY = 300;
    private static final String APP_STATUS = "app_status";
    private static final String DEFAULT_PKG_NAME = "com.huawei.android.launcher";
    private static final int FOREGROUND_ACTIVITIES_CHANGED = 1;
    private static final int LOAD_APPTYPE_MESSAGE_DELAY = 20000;
    private static final int REPORT_MESSAGE = 2;
    private static final long STAY_IN_BACKGROUND_FIVE_DAYS = 432000000;
    private static final long STAY_IN_BACKGROUND_ONE_DAYS = 86400000;
    private static final long STAY_IN_BACKGROUND_SEVEN_DAYS = 604800000;
    private static final long STAY_IN_BACKGROUND_THREE_DAYS = 259200000;
    private static final String TAG = "AwareUserHabit";
    private static final int TOPN = 5;
    private static final int UID_VALUE = 10000;
    private static final int UNINSTALL_CHECK_HABIT_PROTECT_LIST = 3;
    private static final String UNINSTALL_PKGNAME = "uninstall_pkgName";
    public static final int USERHABIT_INSTALL_APP = 1;
    public static final String USERHABIT_INSTALL_APP_UPDATE = "install_app_update";
    public static final String USERHABIT_PACKAGE_NAME = "package_name";
    public static final int USERHABIT_PRECOG_INITIALIZED = 6;
    public static final int USERHABIT_TRAIN_COMPLETED = 3;
    public static final int USERHABIT_UNINSTALL_APP = 2;
    public static final int USERHABIT_UPDATE_CONFIG = 4;
    public static final String USERHABIT_USERID = "user_id";
    public static final int USERHABIT_USER_SWITCH = 5;
    private static final String VISIBLE_ADJ_TYPE = "visible";
    private static AwareUserHabit mAwareUserHabit;
    private static boolean mEnabled;
    private String mAppMngLastPkgName;
    private final AtomicInteger mAppTypeLoadCount;
    private AwareHSMListHandler mAwareHSMListHandler;
    private AwareUserHabitAlgorithm mAwareUserHabitAlgorithm;
    private AwareUserHabitRadar mAwareUserHabitRadar;
    private Context mContext;
    private String mCurPkgName;
    private int mEmailCount;
    private long mFeatureStartTime;
    final Handler mHandler;
    private int mImCount;
    private boolean mIsFirstTime;
    private String mLastAppPkgName;
    private boolean mLauncherIsVisible;
    private int mLruCount;
    private int mMostUsedCount;
    private final Object mPredictLock;
    private final AtomicLong mPredictResult;
    private final AtomicLong mPredictTimes;
    private UserHabitProcessObserver mProcessObserver;
    private AtomicLong mStayInBackgroudTime;
    private final AtomicInteger mTrainTimes;
    private final AtomicInteger mUserId;
    private IUserSwitchObserver mUserSwitchObserver;

    private class UserHabitHandler extends Handler {
        public UserHabitHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AwareUserHabit.USERHABIT_INSTALL_APP /*1*/:
                    handleActivityChangedEvent(msg);
                case AwareUserHabit.USERHABIT_UNINSTALL_APP /*2*/:
                    handleReportMessage(msg);
                default:
            }
        }

        private void handleActivityChangedEvent(Message msg) {
            if (AwareUserHabit.this.mAwareUserHabitAlgorithm != null) {
                if (AwareUserHabit.this.mIsFirstTime) {
                    AwareUserHabit.this.mAwareUserHabitAlgorithm.init();
                    AwareUserHabit.this.mIsFirstTime = false;
                    AwareDefaultConfigList.getInstance().fillMostFrequentUsedApp(AwareUserHabit.this.getMostFrequentUsedApp(AppMngConfig.getAdjCustTopN(), -1));
                    LogIAware.report(2032, "userhabit inited");
                    AwareLog.i(AwareUserHabit.TAG, "report first data loading finished");
                }
                String pkgName = InnerUtils.getAwarePkgName(msg.arg1);
                if (pkgName != null) {
                    long time = SystemClock.elapsedRealtime();
                    boolean isForeground = msg.getData().getBoolean(AwareUserHabit.APP_STATUS);
                    int homePkgPid = AwareAppAssociate.getInstance().getCurHomeProcessPid();
                    ProcessInfo procInfo = ProcessInfoCollector.getInstance().getProcessInfo(homePkgPid);
                    if (isForeground) {
                        AwareUserHabit.this.triggerUserTrackPredict(pkgName, AwareUserHabit.this.getLruCache(), time);
                        AwareUserHabit.this.mAwareUserHabitAlgorithm.foregroundUpdateHabitProtectList(pkgName);
                        if (!(homePkgPid == msg.arg1 || procInfo == null || !AwareUserHabit.VISIBLE_ADJ_TYPE.equals(procInfo.mAdjType))) {
                            AwareUserHabit.this.mLauncherIsVisible = true;
                        }
                    } else {
                        if (AwareUserHabit.this.mLauncherIsVisible && homePkgPid != msg.arg1 && procInfo != null && procInfo.mCurAdj == 0) {
                            String homePkgName = InnerUtils.getAwarePkgName(homePkgPid);
                            if (homePkgName != null) {
                                AwareUserHabit.this.triggerUserTrackPredict(homePkgName, AwareUserHabit.this.getLruCache(), time);
                            }
                        }
                        AwareUserHabit.this.mLauncherIsVisible = false;
                        AwareUserHabit.this.mAwareUserHabitAlgorithm.backgroundActivityChangedEvent(pkgName, Long.valueOf(time));
                    }
                    AwareUserHabit.this.mAwareUserHabitAlgorithm.addPkgToLru(pkgName, time);
                }
            }
        }

        private void handleReportMessage(Message msg) {
            if (AwareUserHabit.this.mAwareUserHabitAlgorithm != null) {
                int eventId = msg.arg1;
                Bundle args = msg.getData();
                String pkgName;
                switch (eventId) {
                    case AwareUserHabit.USERHABIT_INSTALL_APP /*1*/:
                        pkgName = args.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                        if (pkgName != null) {
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.removeFilterPkg(pkgName);
                            break;
                        }
                        break;
                    case AwareUserHabit.USERHABIT_UNINSTALL_APP /*2*/:
                        pkgName = args.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                        if (pkgName != null) {
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.removePkgFromLru(pkgName);
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.addFilterPkg(pkgName);
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.uninstallUpdateHabitProtectList(pkgName);
                            IAwareHabitUtils.removefromAppsTypeList(pkgName);
                            break;
                        }
                        break;
                    case AwareUserHabit.USERHABIT_TRAIN_COMPLETED /*3*/:
                    case AwareUserHabit.USERHABIT_USER_SWITCH /*5*/:
                        AwareUserHabit.this.mAwareUserHabitAlgorithm.reloadDataInfo();
                        if (AwareUserHabit.USERHABIT_TRAIN_COMPLETED == eventId) {
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.trainedUpdateHabitProtectList();
                            AwareUserHabit.this.mTrainTimes.incrementAndGet();
                        } else {
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.clearLruCache();
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.initHabitProtectList();
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.clearHabitProtectApps();
                        }
                        AwareDefaultConfigList.getInstance().fillMostFrequentUsedApp(AwareUserHabit.this.getMostFrequentUsedApp(AppMngConfig.getAdjCustTopN(), -1));
                        LogIAware.report(2032, "train completed");
                        AwareLog.i(AwareUserHabit.TAG, "report train completed and data loading finished");
                        break;
                    case AwareUserHabit.USERHABIT_UPDATE_CONFIG /*4*/:
                        AwareLog.i(AwareUserHabit.TAG, "reloadFilterPkg");
                        AwareUserHabit.this.mAwareUserHabitAlgorithm.reloadFilterPkg();
                        AwareUserHabit.this.updateHabitForceProtectConfig();
                        break;
                    case AwareUserHabit.USERHABIT_PRECOG_INITIALIZED /*6*/:
                        AwareLog.i(AwareUserHabit.TAG, "AwareUserHabit load precog pkg type info");
                        if (!IAwareHabitUtils.loadInstalledAppTypeInfo()) {
                            if (AwareUserHabit.this.mAppTypeLoadCount.get() < 0) {
                                AwareLog.e(AwareUserHabit.TAG, "AwareUserHabit precog service is error");
                                break;
                            }
                            AwareLog.i(AwareUserHabit.TAG, "AwareUserHabit send load precog pkg message again mAppTypeLoadCount=" + (AwareUserHabit.this.mAppTypeLoadCount.get() - 1));
                            AwareUserHabit.this.sendLoadAppTypeMessage();
                            AwareUserHabit.this.mAppTypeLoadCount.decrementAndGet();
                            break;
                        }
                        AwareLog.i(AwareUserHabit.TAG, "AwareUserHabit load precog pkg OK ");
                        break;
                }
            }
        }
    }

    class UserHabitProcessObserver extends Stub {
        UserHabitProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            Message msg = AwareUserHabit.this.mHandler.obtainMessage();
            msg.what = AwareUserHabit.USERHABIT_INSTALL_APP;
            msg.getData().putBoolean(AwareUserHabit.APP_STATUS, foregroundActivities);
            msg.arg1 = pid;
            AwareUserHabit.this.mHandler.sendMessageDelayed(msg, 300);
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }

        public void onProcessDied(int pid, int uid) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.algorithm.AwareUserHabit.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.algorithm.AwareUserHabit.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.algorithm.AwareUserHabit.<clinit>():void");
    }

    private AwareUserHabit(Context context) {
        this.mAwareUserHabitAlgorithm = null;
        this.mContext = null;
        this.mProcessObserver = new UserHabitProcessObserver();
        this.mIsFirstTime = true;
        this.mLauncherIsVisible = false;
        this.mAwareHSMListHandler = null;
        this.mCurPkgName = DEFAULT_PKG_NAME;
        this.mLastAppPkgName = DEFAULT_PKG_NAME;
        this.mAppMngLastPkgName = DEFAULT_PKG_NAME;
        this.mPredictTimes = new AtomicLong(0);
        this.mPredictResult = new AtomicLong(0);
        this.mTrainTimes = new AtomicInteger(0);
        this.mUserId = new AtomicInteger(0);
        this.mAppTypeLoadCount = new AtomicInteger(USERHABIT_UNINSTALL_APP);
        this.mPredictLock = new Object();
        this.mAwareUserHabitRadar = null;
        this.mFeatureStartTime = 0;
        this.mLruCount = USERHABIT_INSTALL_APP;
        this.mEmailCount = USERHABIT_INSTALL_APP;
        this.mImCount = USERHABIT_UNINSTALL_APP;
        this.mMostUsedCount = USERHABIT_INSTALL_APP;
        this.mStayInBackgroudTime = new AtomicLong(STAY_IN_BACKGROUND_SEVEN_DAYS);
        this.mHandler = new UserHabitHandler(BackgroundThread.get().getLooper());
        this.mUserSwitchObserver = new IUserSwitchObserver.Stub() {
            public void onUserSwitching(int newUserId, IRemoteCallback reply) {
            }

            public void onUserSwitchComplete(int newUserId) throws RemoteException {
                AwareUserHabit.this.mUserId.set(newUserId);
                AwareUserHabit.this.setUserId(newUserId);
                Message msg = AwareUserHabit.this.mHandler.obtainMessage();
                msg.what = AwareUserHabit.USERHABIT_UNINSTALL_APP;
                msg.arg1 = AwareUserHabit.USERHABIT_USER_SWITCH;
                AwareUserHabit.this.mHandler.sendMessage(msg);
            }

            public void onForegroundProfileSwitch(int newProfileId) {
            }
        };
        this.mContext = context;
    }

    public static synchronized AwareUserHabit getInstance(Context context) {
        AwareUserHabit awareUserHabit;
        synchronized (AwareUserHabit.class) {
            if (mAwareUserHabit == null) {
                mAwareUserHabit = new AwareUserHabit(context);
                if (mEnabled) {
                    mAwareUserHabit.init();
                }
            }
            awareUserHabit = mAwareUserHabit;
        }
        return awareUserHabit;
    }

    public static synchronized AwareUserHabit getInstance() {
        AwareUserHabit awareUserHabit;
        synchronized (AwareUserHabit.class) {
            awareUserHabit = mAwareUserHabit;
        }
        return awareUserHabit;
    }

    public static void enable() {
        AwareLog.i(TAG, "AwareUserHabit enable is called");
        if (!mEnabled) {
            AwareUserHabit habit = getInstance();
            if (habit != null) {
                habit.init();
            } else {
                AwareLog.i(TAG, "user habit is not ready");
            }
            mEnabled = true;
        }
    }

    public static void disable() {
        AwareLog.i(TAG, "AwareUserHabit disable is called");
        if (mEnabled) {
            AwareUserHabit habit = getInstance();
            if (habit != null) {
                habit.deinit();
            } else {
                AwareLog.i(TAG, "user habit is not ready");
            }
        }
        mEnabled = false;
    }

    public boolean isEnable() {
        return mEnabled;
    }

    public void setStayInBackgroudTime(long day) {
        this.mStayInBackgroudTime.set(STAY_IN_BACKGROUND_ONE_DAYS * day);
    }

    public List<String> getLRUAppList(int lruCount) {
        return this.mAwareUserHabitAlgorithm.getForceProtectAppsFromLRU(AwareAppAssociate.getInstance().getDefaultHomePackages(), lruCount);
    }

    private void deinit() {
        unregisterObserver();
        if (this.mAwareUserHabitAlgorithm != null) {
            this.mAwareUserHabitAlgorithm.deinit();
            this.mIsFirstTime = true;
        }
        IAwareHabitUtils.clearAppsTypeList();
        if (this.mAwareHSMListHandler != null) {
            this.mAwareHSMListHandler.deinit();
        }
        AwareLog.d(TAG, "AwareUserHabit deinit finished");
    }

    private void init() {
        if (this.mContext != null) {
            if (this.mAwareUserHabitAlgorithm == null) {
                this.mAwareUserHabitAlgorithm = new AwareUserHabitAlgorithm(this.mContext);
            }
            this.mAwareUserHabitAlgorithm.initHabitProtectList();
            registerObserver();
            this.mFeatureStartTime = System.currentTimeMillis();
            if (this.mAwareUserHabitRadar == null) {
                this.mAwareUserHabitRadar = new AwareUserHabitRadar(this.mFeatureStartTime);
            }
            if (this.mAwareHSMListHandler == null) {
                this.mAwareHSMListHandler = new AwareHSMListHandler(this.mContext);
            }
            this.mAwareHSMListHandler.init();
            updateHabitForceProtectConfig();
            sendLoadAppTypeMessage();
            AwareLog.d(TAG, "AwareUserHabit init finished");
        }
    }

    private void sendLoadAppTypeMessage() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = USERHABIT_UNINSTALL_APP;
        msg.arg1 = USERHABIT_PRECOG_INITIALIZED;
        this.mHandler.sendMessageDelayed(msg, TableJankBd.recordMAXCOUNT);
    }

    private void triggerUserTrackPredict(String pkgName, Map<String, Long> lruCache, long curTime) {
        synchronized (this.mPredictLock) {
            if (this.mAwareUserHabitAlgorithm != null) {
                this.mAppMngLastPkgName = this.mAwareUserHabitAlgorithm.getLastPkgNameExcludeLauncher(pkgName, AwareAppAssociate.getInstance().getDefaultHomePackages());
                if (!this.mAwareUserHabitAlgorithm.containsFilterPkg(pkgName)) {
                    if (!this.mLastAppPkgName.equals(pkgName)) {
                        if (this.mAwareUserHabitAlgorithm.isPredictHit(pkgName, USERHABIT_USER_SWITCH)) {
                            this.mPredictResult.incrementAndGet();
                        }
                        this.mPredictTimes.incrementAndGet();
                    }
                    this.mLastAppPkgName = pkgName;
                }
                this.mCurPkgName = pkgName;
                this.mAwareUserHabitAlgorithm.triggerUserTrackPredict(this.mLastAppPkgName, lruCache, curTime, pkgName);
            }
        }
    }

    public Map<String, String> getUserTrackAppSortDumpInfo() {
        Map<String, String> dumpInfo = null;
        synchronized (this.mPredictLock) {
            if (this.mAwareUserHabitAlgorithm != null) {
                dumpInfo = this.mAwareUserHabitAlgorithm.getUserTrackPredictDumpInfo(this.mLastAppPkgName, getLruCache(), SystemClock.elapsedRealtime(), this.mCurPkgName);
            }
        }
        return dumpInfo;
    }

    public Set<String> getAllProtectApps() {
        if (this.mAwareHSMListHandler == null) {
            return null;
        }
        return this.mAwareHSMListHandler.getAllProtectSet();
    }

    public Set<String> getAllUnProtectApps() {
        if (this.mAwareHSMListHandler == null) {
            return null;
        }
        return this.mAwareHSMListHandler.getAllUnProtectSet();
    }

    public List<String> getForceProtectApps(int num) {
        AwareLog.i(TAG, "getForceProtectApps is called");
        if (num <= 0 || this.mAwareHSMListHandler == null || this.mAwareUserHabitAlgorithm == null) {
            return null;
        }
        int lruCount;
        int emailCount;
        int imCount;
        int mostUsedCount;
        long start_time = System.currentTimeMillis();
        List<String> result = new ArrayList();
        Set<String> filterSet = this.mAwareHSMListHandler.getUnProtectSet();
        synchronized (this) {
            lruCount = this.mLruCount;
            emailCount = this.mEmailCount;
            imCount = this.mImCount;
            mostUsedCount = this.mMostUsedCount;
        }
        List<String> habitProtectList = this.mAwareUserHabitAlgorithm.getForceProtectAppsFromHabitProtect(emailCount, imCount, filterSet);
        result.addAll(habitProtectList);
        filterSet.addAll(habitProtectList);
        List<String> mostList = this.mAwareUserHabitAlgorithm.getMostFrequentUsedApp(mostUsedCount, -1, filterSet);
        if (mostList != null && mostList.size() > 0) {
            result.addAll(mostList);
            filterSet.addAll(mostList);
        }
        List<String> homePkg = AwareAppAssociate.getInstance().getDefaultHomePackages();
        List<String> lruList = this.mAwareUserHabitAlgorithm.getForceProtectAppsFromLRU(homePkg, lruCount);
        if (lruList != null && lruList.size() > 0) {
            for (String str : lruList) {
                if (!filterSet.contains(str)) {
                    result.add(str);
                }
            }
        }
        AwareLog.d(TAG, "getForceProtectApps spend time:" + (System.currentTimeMillis() - start_time) + " ms " + "result:" + result);
        return result;
    }

    private void updateHabitForceProtectConfig() {
        Map<String, Integer> config = IAwareHabitUtils.getForceProtectConfigFromCMS();
        if (config != null) {
            synchronized (this) {
                if (config.containsKey(IAwareHabitUtils.HABIT_LRU_COUNT)) {
                    this.mLruCount = ((Integer) config.get(IAwareHabitUtils.HABIT_LRU_COUNT)).intValue();
                }
                if (config.containsKey(IAwareHabitUtils.HABIT_EMAIL_COUNT)) {
                    this.mEmailCount = ((Integer) config.get(IAwareHabitUtils.HABIT_EMAIL_COUNT)).intValue();
                }
                if (config.containsKey(IAwareHabitUtils.HABIT_IM_COUNT)) {
                    this.mImCount = ((Integer) config.get(IAwareHabitUtils.HABIT_IM_COUNT)).intValue();
                }
                if (config.containsKey(IAwareHabitUtils.HABIT_MOST_USED_COUNT)) {
                    this.mMostUsedCount = ((Integer) config.get(IAwareHabitUtils.HABIT_MOST_USED_COUNT)).intValue();
                }
            }
        }
    }

    public LinkedHashMap<String, Long> getLruCache() {
        if (this.mAwareUserHabitAlgorithm != null) {
            return this.mAwareUserHabitAlgorithm.getLruCache();
        }
        return null;
    }

    public Set<String> getBackgroundApps(long duringTime) {
        if (duringTime <= 0) {
            return null;
        }
        LinkedHashMap<String, Long> lru = getLruCache();
        if (lru == null) {
            return null;
        }
        duringTime *= 1000;
        ArraySet<String> result = new ArraySet();
        ArrayList<String> pkgList = new ArrayList(lru.keySet());
        long now = SystemClock.elapsedRealtime();
        for (int i = pkgList.size() - 1; i >= 0; i--) {
            String pkg = (String) pkgList.get(i);
            if (now - ((Long) lru.get(pkg)).longValue() > duringTime) {
                break;
            }
            result.add(pkg);
        }
        return result;
    }

    public String getLastPkgName() {
        return this.mAppMngLastPkgName;
    }

    public synchronized List<String> recognizeLongTimeRunningApps() {
        AwareLog.i(TAG, "recognizeLongTimeRunningApps is called");
        List<Integer> keyPidList = new ArrayList();
        ArrayMap<Integer, ArrayList<String>> pidToPkgMap = new ArrayMap();
        getRunningPidInfo(keyPidList, pidToPkgMap);
        Map<String, Long> killingPkgMap = getLongTimeRunningPkgs(keyPidList);
        if (killingPkgMap == null || killingPkgMap.isEmpty()) {
            return null;
        }
        List<String> list;
        int oneDaysAppsNum = 0;
        int threeDaysAppsNum = 0;
        int fiveDaysAppsNum = 0;
        Set<String> killingPkgSet = new ArraySet();
        for (int i = 0; i < keyPidList.size(); i += USERHABIT_INSTALL_APP) {
            ArraySet<Integer> strong = new ArraySet();
            AwareAppAssociate.getInstance().getAssocListForPid(((Integer) keyPidList.get(i)).intValue(), strong);
            for (int j = 0; j < strong.size(); j += USERHABIT_INSTALL_APP) {
                int pid = ((Integer) strong.valueAt(j)).intValue();
                if (pidToPkgMap.containsKey(Integer.valueOf(pid))) {
                    for (String p : (ArrayList) pidToPkgMap.get(Integer.valueOf(pid))) {
                        AwareLog.d(TAG, "strong associate app:" + p);
                        killingPkgMap.remove(p);
                    }
                }
            }
        }
        long stayInBackgroudTime = this.mStayInBackgroudTime.get();
        for (Entry entry : killingPkgMap.entrySet()) {
            String pkg = (String) entry.getKey();
            long time = SystemClock.elapsedRealtime() - ((Long) entry.getValue()).longValue();
            if (time >= stayInBackgroudTime) {
                killingPkgSet.add(pkg);
            } else if (time >= STAY_IN_BACKGROUND_FIVE_DAYS) {
                fiveDaysAppsNum += USERHABIT_INSTALL_APP;
            } else if (time >= STAY_IN_BACKGROUND_THREE_DAYS) {
                threeDaysAppsNum += USERHABIT_INSTALL_APP;
            } else if (time >= STAY_IN_BACKGROUND_ONE_DAYS) {
                oneDaysAppsNum += USERHABIT_INSTALL_APP;
            }
        }
        int size = killingPkgSet.size();
        if (this.mAwareUserHabitRadar != null) {
            if (oneDaysAppsNum > 0) {
                this.mAwareUserHabitRadar.insertStatisticData("habit-one_days-kill", 0, oneDaysAppsNum);
            }
            if (threeDaysAppsNum > 0) {
                this.mAwareUserHabitRadar.insertStatisticData("habit-three_days-kill", 0, threeDaysAppsNum);
            }
            if (fiveDaysAppsNum > 0) {
                this.mAwareUserHabitRadar.insertStatisticData("habit-five_days-kill", 0, fiveDaysAppsNum);
            }
            if (size > 0) {
                this.mAwareUserHabitRadar.insertStatisticData("habit-seven_days_kill", 0, size);
            }
            AwareLog.d(TAG, "recognizeLongTimeRunningApps sevendays num:" + size + " oneDaysAppsNum:" + oneDaysAppsNum + " threeDaysAppsNum:" + threeDaysAppsNum + " fiveDaysAppsNum:" + fiveDaysAppsNum);
        }
        if (size > 0) {
            List<String> arrayList = new ArrayList(killingPkgSet);
        } else {
            list = null;
        }
        return list;
    }

    private Map<String, Long> getLongTimeRunningPkgs(List<Integer> pidList) {
        if (this.mAwareUserHabitAlgorithm == null || this.mAwareHSMListHandler == null) {
            return null;
        }
        Map<String, Long> lruPkgMap = this.mAwareUserHabitAlgorithm.getLongTimePkgsFromLru();
        if (lruPkgMap == null) {
            return null;
        }
        Map<String, Long> ltrPkgs = new ArrayMap();
        List<Integer> rulist = new ArrayList();
        Set<String> filterSet = this.mAwareHSMListHandler.getProtectSet();
        for (int i = 0; i < pidList.size(); i += USERHABIT_INSTALL_APP) {
            String pkg = InnerUtils.getAwarePkgName(((Integer) pidList.get(i)).intValue());
            if (filterSet.contains(pkg)) {
                AwareLog.d(TAG, "hsm filter set pkg:" + pkg);
            } else if (pkg == null || !lruPkgMap.containsKey(pkg)) {
                rulist.add((Integer) pidList.get(i));
            } else {
                int type = IAwareHabitUtils.getAppType(this.mContext, pkg);
                if (!(USERHABIT_USER_SWITCH == type || USERHABIT_UNINSTALL_APP == type || 310 == type)) {
                    ltrPkgs.put(pkg, (Long) lruPkgMap.get(pkg));
                }
            }
        }
        pidList.clear();
        pidList.addAll(rulist);
        return ltrPkgs;
    }

    private void getRunningPidInfo(List<Integer> pidList, Map<Integer, ArrayList<String>> pidToPkgMap) {
        for (ProcessInfo process : ProcessInfoCollector.getInstance().getProcessInfoList()) {
            if (process != null && UserHandle.getAppId(process.mUid) > UID_VALUE) {
                int pid = Integer.valueOf(process.mPid).intValue();
                ArrayList<String> plist = new ArrayList();
                plist.addAll(process.mPackageName);
                pidToPkgMap.put(Integer.valueOf(pid), plist);
                pidList.add(Integer.valueOf(pid));
            }
        }
    }

    public List<String> getMostFrequentUsedApp(int n, int minCount) {
        AwareLog.i(TAG, "getMostFrequentUsedApp is called");
        if (this.mAwareUserHabitAlgorithm == null || n <= 0) {
            return null;
        }
        return this.mAwareUserHabitAlgorithm.getMostFrequentUsedApp(n, minCount, null);
    }

    public List<String> getHabitProtectList(int emailCount, int imCount) {
        if (imCount <= 0 || emailCount <= 0 || this.mAwareUserHabitAlgorithm == null) {
            return null;
        }
        return this.mAwareUserHabitAlgorithm.getHabitProtectList(emailCount, imCount);
    }

    public List<String> getHabitProtectListAll(int emailCount, int imCount) {
        if (imCount <= 0 || emailCount <= 0 || this.mAwareUserHabitAlgorithm == null) {
            return null;
        }
        return this.mAwareUserHabitAlgorithm.getHabitProtectAppsAll(emailCount, imCount);
    }

    public List<String> getTopN(int n) {
        AwareLog.i(TAG, "getTopN is called");
        if (this.mAwareUserHabitAlgorithm == null || n <= 0) {
            return null;
        }
        return this.mAwareUserHabitAlgorithm.getTopN(n);
    }

    public Set<String> getAppListByType(int type) {
        return IAwareHabitUtils.getAppListByType(type);
    }

    public Set<String> getFilterApp() {
        if (this.mAwareUserHabitAlgorithm != null) {
            return this.mAwareUserHabitAlgorithm.getFilterApp();
        }
        return null;
    }

    public Map<Integer, Integer> getTopList(Map<Integer, AwareProcessInfo> appProcesses) {
        AwareLog.i(TAG, "getTopList is called");
        if (appProcesses == null) {
            return null;
        }
        return getUserTopList(appProcesses);
    }

    private Map<Integer, Integer> getUserTopList(Map<Integer, AwareProcessInfo> appProcesses) {
        List<Entry<Integer, Integer>> list = new ArrayList();
        ArrayMap<Integer, Integer> map = new ArrayMap();
        sortPidByPkgs(list, appProcesses);
        for (int i = 0; i < list.size(); i += USERHABIT_INSTALL_APP) {
            Entry<Integer, Integer> entry = (Entry) list.get(i);
            map.put(Integer.valueOf(((Integer) entry.getKey()).intValue()), Integer.valueOf(((Integer) entry.getValue()).intValue()));
        }
        return map;
    }

    private void sortPidByPkgs(List<Entry<Integer, Integer>> list, Map<Integer, AwareProcessInfo> appProcesses) {
        if (this.mAwareUserHabitAlgorithm != null) {
            Map<String, Integer> result = this.mAwareUserHabitAlgorithm.getUserTrackList();
            for (Entry<Integer, AwareProcessInfo> entry : appProcesses.entrySet()) {
                AwareProcessInfo info = (AwareProcessInfo) entry.getValue();
                if (this.mUserId.get() == UserHandle.getUserId(info.mProcInfo.mUid)) {
                    ArrayList<String> pkgList = info.mProcInfo.mPackageName;
                    int pid = info.mPid;
                    if (pkgList.size() != USERHABIT_INSTALL_APP) {
                        int index = -1;
                        for (int j = 0; j < pkgList.size(); j += USERHABIT_INSTALL_APP) {
                            if (result.containsKey(pkgList.get(j))) {
                                if (index == -1) {
                                    index = ((Integer) result.get(pkgList.get(j))).intValue();
                                } else {
                                    int order = ((Integer) result.get(pkgList.get(j))).intValue();
                                    if (order < index) {
                                        index = order;
                                    }
                                }
                            }
                        }
                        if (index != -1) {
                            list.add(new SimpleEntry(Integer.valueOf(pid), Integer.valueOf(index)));
                        }
                    } else if (result.containsKey(pkgList.get(0))) {
                        list.add(new SimpleEntry(Integer.valueOf(pid), result.get(pkgList.get(0))));
                    }
                }
            }
        }
    }

    public void report(int eventId, Bundle args) {
        if (mEnabled && args != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = USERHABIT_UNINSTALL_APP;
            msg.arg1 = eventId;
            msg.setData(args);
            this.mHandler.sendMessage(msg);
        }
    }

    private void registerObserver() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.d(TAG, "AwareUserHabit register process observer failed");
        }
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(this.mUserSwitchObserver);
        } catch (RemoteException e2) {
            AwareLog.e(TAG, "AwareUserHabit registerUserSwitchObserver failed!");
        }
    }

    private void unregisterObserver() {
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.d(TAG, "AwareUserHabit unregister process observer failed");
        }
        try {
            ActivityManagerNative.getDefault().unregisterUserSwitchObserver(this.mUserSwitchObserver);
        } catch (RemoteException e2) {
            AwareLog.e(TAG, "AwareUserHabit unregisterProcessObserver failed!");
        }
    }

    private void setUserId(int userId) {
        if (this.mAwareHSMListHandler != null) {
            this.mAwareHSMListHandler.setUserId(userId);
        }
        if (this.mAwareUserHabitAlgorithm != null) {
            this.mAwareUserHabitAlgorithm.setUserId(userId);
        }
    }

    public void registHabitProtectListChangeListener(HabitProtectListChangeListener listener) {
        if (this.mAwareUserHabitAlgorithm != null) {
            this.mAwareUserHabitAlgorithm.registHabitProtectListChangeListener(listener);
        }
    }

    public void unregistHabitProtectListChangeListener(HabitProtectListChangeListener listener) {
        if (this.mAwareUserHabitAlgorithm != null) {
            this.mAwareUserHabitAlgorithm.unregistHabitProtectListChangeListener(listener);
        }
    }

    public List<String> queryHabitProtectAppList() {
        if (this.mAwareUserHabitAlgorithm != null) {
            return this.mAwareUserHabitAlgorithm.queryHabitProtectAppList();
        }
        return null;
    }

    public List<String> queryHabitProtectAppList(int imCount, int emailCount) {
        if (this.mAwareUserHabitAlgorithm != null) {
            return this.mAwareUserHabitAlgorithm.queryHabitProtectAppList(imCount, emailCount);
        }
        return null;
    }

    public List<String> getGCMAppList() {
        if (this.mAwareUserHabitAlgorithm != null) {
            return this.mAwareUserHabitAlgorithm.getGCMAppsList();
        }
        return null;
    }

    public void dumpHabitProtectList(PrintWriter pw) {
        if (this.mAwareUserHabitAlgorithm != null) {
            this.mAwareUserHabitAlgorithm.dumpHabitProtectList(pw);
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        ArrayList<StatisticsData> tempList = new ArrayList();
        long now = System.currentTimeMillis();
        tempList.add(new StatisticsData(AwareUserHabitRadar.APPMNG_FEATURE_ID, USERHABIT_INSTALL_APP, "habit-predict", this.mPredictResult.intValue(), this.mPredictTimes.intValue(), this.mTrainTimes.intValue(), this.mFeatureStartTime, now));
        this.mFeatureStartTime = now;
        this.mPredictResult.set(0);
        this.mPredictTimes.set(0);
        this.mTrainTimes.set(0);
        if (this.mAwareUserHabitRadar != null) {
            ArrayList<StatisticsData> list = this.mAwareUserHabitRadar.getStatisticsData();
            if (list != null) {
                tempList.addAll(list);
            }
        }
        return tempList;
    }
}
