package com.android.server.rms.algorithm;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.IUserSwitchObserver;
import android.app.IUserSwitchObserver.Stub;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.LogIAware;
import android.rms.iaware.StatisticsData;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.algorithm.AwareUserHabitAlgorithm.HabitProtectListChangeListener;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONException;
import org.json.JSONObject;

public class AwareUserHabit {
    private static final int ACTIVITIES_CHANGED_MESSAGE_DELAY = 300;
    private static final String APP_STATUS = "app_status";
    private static final String DEFAULT_PKG_NAME = "com.huawei.android.launcher";
    private static final int FOREGROUND_ACTIVITIES_CHANGED = 1;
    private static final int HIGH_DEVICE_BUSINESS_NUM = 5;
    private static final int HIGH_DEVICE_EMAIL_NUM = 2;
    private static final int HIGH_DEVICE_IM_NUM = 3;
    private static final int LOAD_APPTYPE_MESSAGE_DELAY = 20000;
    private static final int LOW_DEVICE_BUSINESS_NUM = 3;
    private static final int LOW_DEVICE_EMAIL_NUM = 1;
    private static final int LOW_DEVICE_IM_NUM = 2;
    private static final long ONE_MINUTE = 60000;
    private static final String REPORT_DYNAMIC_TOPN_KEY = "dynamicTopN";
    private static final int REPORT_MESSAGE = 2;
    private static final String REPORT_SMALL_SAMPLE_LIST_KEY = "SmallSampleList";
    private static final String REPORT_SORT_KEY = "predictSorted";
    private static final String REPORT_SWITCHOFF_KEY = "predictV2Off";
    private static final int SCREEN_STATE_CHANGED = 4;
    private static final long SREEN_OFF_SORT_THRESHOLD = 1800000;
    private static final long STAY_IN_BACKGROUND_ONE_DAYS = 86400000;
    private static final String TAG = "AwareUserHabit";
    private static final int TOPN = 5;
    private static final int UID_VALUE = 10000;
    private static final int UNINSTALL_CHECK_HABIT_PROTECT_LIST = 3;
    private static final String UNINSTALL_PKGNAME = "uninstall_pkgName";
    public static final int USERHABIT_INSTALL_APP = 1;
    public static final String USERHABIT_INSTALL_APP_UPDATE = "install_app_update";
    public static final String USERHABIT_PACKAGE_NAME = "package_name";
    public static final int USERHABIT_PRECOG_INITIALIZED = 6;
    public static final int USERHABIT_SCREEN_OFF = 7;
    public static final int USERHABIT_TRAIN_COMPLETED = 3;
    public static final String USERHABIT_UID = "uid";
    public static final int USERHABIT_UNINSTALL_APP = 2;
    public static final int USERHABIT_UPDATE_CONFIG = 4;
    public static final int USERHABIT_USER_SWITCH = 5;
    private static final String VISIBLE_ADJ_TYPE = "visible";
    private static AwareUserHabit mAwareUserHabit = null;
    private static boolean mEnabled = false;
    private String mAppMngLastPkgName = DEFAULT_PKG_NAME;
    private final AtomicInteger mAppTypeLoadCount = new AtomicInteger(2);
    private AwareHSMListHandler mAwareHSMListHandler = null;
    private AwareUserHabitAlgorithm mAwareUserHabitAlgorithm = null;
    private AwareUserHabitRadar mAwareUserHabitRadar = null;
    private Context mContext = null;
    private String mCurPkgName = DEFAULT_PKG_NAME;
    private int mDynamicTopN = 5;
    private int mEmailCount = 1;
    private long mFeatureStartTime = 0;
    final Handler mHandler = new UserHabitHandler(BackgroundThread.get().getLooper());
    private long mHighEndDeviceStayInBgTime = 432000000;
    private HwActivityManagerService mHwAMS = null;
    private int mImCount = 2;
    private boolean mIsFirstTime = true;
    private boolean mIsLowEnd = false;
    private boolean mIsNewAlgorithmSwitched = false;
    private String mLastAppPkgName = DEFAULT_PKG_NAME;
    private long mLastSortTime = 0;
    private boolean mLauncherIsVisible = false;
    private long mLowEndDeviceStayInBgTime = 172800000;
    private int mLruCount = 1;
    private int mMostUsedCount = 1;
    private final Object mPredictLock = new Object();
    private final AtomicLong mPredictResult = new AtomicLong(0);
    private final AtomicLong mPredictTimes = new AtomicLong(0);
    private UserHabitProcessObserver mProcessObserver = new UserHabitProcessObserver();
    private final AtomicInteger mTrainTimes = new AtomicInteger(0);
    private final AtomicInteger mUserId = new AtomicInteger(0);
    private IUserSwitchObserver mUserSwitchObserver = new Stub() {
        public void onUserSwitching(int newUserId, IRemoteCallback reply) {
            if (reply != null) {
                try {
                    reply.sendResult(null);
                } catch (RemoteException e) {
                    AwareLog.e(AwareUserHabit.TAG, "RemoteException onUserSwitching");
                }
            }
        }

        public void onUserSwitchComplete(int newUserId) throws RemoteException {
            AwareUserHabit.this.mUserId.set(newUserId);
            AwareUserHabit.this.setUserId(newUserId);
            if (newUserId != 0) {
                AwareUserHabit.this.mIsNewAlgorithmSwitched = false;
            }
            Message msg = AwareUserHabit.this.mHandler.obtainMessage();
            msg.what = 2;
            msg.arg1 = 5;
            AwareUserHabit.this.mHandler.sendMessage(msg);
        }

        public void onForegroundProfileSwitch(int newProfileId) {
        }

        public void onLockedBootComplete(int newUserId) {
        }
    };
    private LinkedHashMap<String, Integer> mUserTrackListVer2 = new LinkedHashMap();

    private class UserHabitHandler extends Handler {
        public UserHabitHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String pkgName = InnerUtils.getAwarePkgName(msg.arg1);
                    handleActivityChangedEvent(msg, pkgName);
                    if (!msg.getData().getBoolean(AwareUserHabit.APP_STATUS)) {
                        AwareIntelligentRecg.getInstance().reportAppChangeToBackground(pkgName, SystemClock.elapsedRealtime());
                        return;
                    }
                    return;
                case 2:
                    handleReportMessage(msg);
                    return;
                case 4:
                    handleScreenState(msg);
                    return;
                default:
                    return;
            }
        }

        private void handleScreenState(Message msg) {
            if (AwareUserHabit.this.mAwareUserHabitAlgorithm != null && msg.arg1 == 7 && Math.abs(System.currentTimeMillis() - AwareUserHabit.this.mLastSortTime) > 1800000) {
                AwareUserHabit.this.mAwareUserHabitAlgorithm.sortUsageCount();
                AwareUserHabit.this.mLastSortTime = System.currentTimeMillis();
            }
        }

        private void handleActivityChangedEvent(Message msg, String pkgName) {
            if (AwareUserHabit.this.mAwareUserHabitAlgorithm != null && pkgName != null) {
                if (AwareUserHabit.this.mIsFirstTime) {
                    AwareUserHabit.this.mAwareUserHabitAlgorithm.init();
                    AwareUserHabit.this.mIsFirstTime = false;
                    AwareDefaultConfigList.getInstance().fillMostFrequentUsedApp(AwareUserHabit.this.getMostFrequentUsedApp(AppMngConfig.getAdjCustTopN(), -1));
                    LogIAware.report(2032, "userhabit inited");
                    AwareLog.i(AwareUserHabit.TAG, "report first data loading finished");
                }
                long time = SystemClock.elapsedRealtime();
                boolean isForeground = msg.getData().getBoolean(AwareUserHabit.APP_STATUS);
                int homePkgPid = AwareAppAssociate.getInstance().getCurHomeProcessPid();
                AwareProcessBaseInfo baseInfo = AwareUserHabit.this.mHwAMS != null ? AwareUserHabit.this.mHwAMS.getProcessBaseInfo(homePkgPid) : null;
                if (isForeground) {
                    AwareUserHabit.this.triggerUserTrackPredict(pkgName, AwareUserHabit.this.getLruCache(), time);
                    AwareUserHabit.this.mAwareUserHabitAlgorithm.foregroundUpdateHabitProtectList(pkgName);
                    AwareUserHabit.this.mAwareUserHabitAlgorithm.updateAppUsage(pkgName);
                    if (!(homePkgPid == msg.arg1 || baseInfo == null || !AwareUserHabit.VISIBLE_ADJ_TYPE.equals(baseInfo.mAdjType))) {
                        AwareUserHabit.this.mLauncherIsVisible = true;
                    }
                } else {
                    if (AwareUserHabit.this.mLauncherIsVisible && homePkgPid != msg.arg1 && baseInfo != null && baseInfo.mCurAdj == 0) {
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

        private void handleReportMessage(Message msg) {
            if (AwareUserHabit.this.mAwareUserHabitAlgorithm != null) {
                int eventId = msg.arg1;
                Bundle args = msg.getData();
                String pkgName;
                switch (eventId) {
                    case 1:
                        pkgName = args.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                        if (pkgName != null) {
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.removeFilterPkg(pkgName);
                            break;
                        }
                        break;
                    case 2:
                        pkgName = args.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
                        if (pkgName != null) {
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.removePkgFromLru(pkgName);
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.addFilterPkg(pkgName);
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.uninstallUpdateHabitProtectList(pkgName);
                            break;
                        }
                        break;
                    case 3:
                    case 5:
                        AwareUserHabit.this.mAwareUserHabitAlgorithm.reloadDataInfo();
                        if (3 == eventId) {
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.trainedUpdateHabitProtectList();
                            AwareUserHabit.this.mTrainTimes.incrementAndGet();
                        } else {
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.clearLruCache();
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.initHabitProtectList();
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.clearHabitProtectApps();
                            AwareUserHabit.this.mLastSortTime = System.currentTimeMillis();
                        }
                        AwareDefaultConfigList.getInstance().fillMostFrequentUsedApp(AwareUserHabit.this.getMostFrequentUsedApp(AppMngConfig.getAdjCustTopN(), -1));
                        LogIAware.report(2032, "train completed");
                        AwareLog.i(AwareUserHabit.TAG, "report train completed and data loading finished");
                        break;
                    case 4:
                        AwareLog.i(AwareUserHabit.TAG, "reloadFilterPkg");
                        AwareUserHabit.this.mAwareUserHabitAlgorithm.reloadFilterPkg();
                        AwareUserHabit.this.updateHabitConfig();
                        break;
                    case 6:
                        AwareLog.i(AwareUserHabit.TAG, "AwareUserHabit load precog pkg type info");
                        if (!AppTypeRecoManager.getInstance().loadInstalledAppTypeInfo()) {
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

    class UserHabitProcessObserver extends IProcessObserver.Stub {
        UserHabitProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            Message msg = AwareUserHabit.this.mHandler.obtainMessage();
            msg.what = 1;
            msg.getData().putBoolean(AwareUserHabit.APP_STATUS, foregroundActivities);
            msg.arg1 = pid;
            AwareUserHabit.this.mHandler.sendMessageDelayed(msg, 300);
        }

        public void onProcessDied(int pid, int uid) {
        }
    }

    private AwareUserHabit(Context context) {
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

    public void setLowEndFlag(boolean flag) {
        synchronized (this) {
            this.mIsLowEnd = flag;
        }
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
        if (this.mAwareHSMListHandler != null) {
            this.mAwareHSMListHandler.deinit();
        }
        AppTypeRecoManager.getInstance().deinit();
        AwareLog.d(TAG, "AwareUserHabit deinit finished");
    }

    private void init() {
        if (this.mContext != null) {
            if (this.mAwareUserHabitAlgorithm == null) {
                this.mAwareUserHabitAlgorithm = new AwareUserHabitAlgorithm(this.mContext);
            }
            this.mHwAMS = HwActivityManagerService.self();
            this.mAwareUserHabitAlgorithm.initHabitProtectList();
            AppTypeRecoManager.getInstance().init(this.mContext);
            registerObserver();
            this.mFeatureStartTime = System.currentTimeMillis();
            if (this.mAwareUserHabitRadar == null) {
                this.mAwareUserHabitRadar = new AwareUserHabitRadar(this.mFeatureStartTime);
            }
            if (this.mAwareHSMListHandler == null) {
                this.mAwareHSMListHandler = new AwareHSMListHandler(this.mContext);
            }
            this.mAwareHSMListHandler.init();
            updateHabitConfig();
            sendLoadAppTypeMessage();
            this.mLastSortTime = System.currentTimeMillis();
            AwareLog.d(TAG, "AwareUserHabit init finished");
        }
    }

    private void sendLoadAppTypeMessage() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 2;
        msg.arg1 = 6;
        this.mHandler.sendMessageDelayed(msg, 20000);
    }

    private void triggerUserTrackPredict(String pkgName, Map<String, Long> lruCache, long curTime) {
        if (!this.mIsNewAlgorithmSwitched) {
            synchronized (this.mPredictLock) {
                if (this.mAwareUserHabitAlgorithm != null) {
                    this.mAppMngLastPkgName = this.mAwareUserHabitAlgorithm.getLastPkgNameExcludeLauncher(pkgName, AwareAppAssociate.getInstance().getDefaultHomePackages());
                    if (!(this.mAwareUserHabitAlgorithm.containsFilterPkg(pkgName) || (this.mAwareUserHabitAlgorithm.containsFilter2Pkg(pkgName) ^ 1) == 0)) {
                        if (!this.mLastAppPkgName.equals(pkgName)) {
                            if (this.mAwareUserHabitAlgorithm.isPredictHit(pkgName, 5)) {
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
    }

    public Map<String, String> getUserTrackAppSortDumpInfo() {
        if (this.mIsNewAlgorithmSwitched) {
            LinkedHashMap<String, String> result = new LinkedHashMap();
            for (Entry<String, Integer> entry : getUserTrackListVer2().entrySet()) {
                result.put((String) entry.getKey(), String.valueOf(entry.getValue()));
            }
            return result;
        }
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
        List<String> lruList = this.mAwareUserHabitAlgorithm.getForceProtectAppsFromLRU(AwareAppAssociate.getInstance().getDefaultHomePackages(), lruCount);
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

    private void updateHabitConfig() {
        Map<String, Integer> config = IAwareHabitUtils.getConfigFromCMS(IAwareHabitUtils.APPMNG, IAwareHabitUtils.HABIT_CONFIG);
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
                if (config.containsKey(IAwareHabitUtils.HABIT_LOW_END)) {
                    this.mLowEndDeviceStayInBgTime = ((long) ((Integer) config.get(IAwareHabitUtils.HABIT_LOW_END)).intValue()) * 60000;
                }
                if (config.containsKey(IAwareHabitUtils.HABIT_HIGH_END)) {
                    this.mHighEndDeviceStayInBgTime = ((long) ((Integer) config.get(IAwareHabitUtils.HABIT_HIGH_END)).intValue()) * 60000;
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

    /* JADX WARNING: Missing block: B:45:0x0171, code:
            return r26;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized List<String> recognizeLongTimeRunningApps() {
        AwareLog.i(TAG, "recognizeLongTimeRunningApps is called");
        List<Integer> keyPidList = new ArrayList();
        ArrayMap<Integer, ArrayList<String>> pidToPkgMap = new ArrayMap();
        getRunningPidInfo(keyPidList, pidToPkgMap);
        long stayInBackgroudTime = this.mHighEndDeviceStayInBgTime;
        if (this.mIsLowEnd) {
            stayInBackgroudTime = this.mLowEndDeviceStayInBgTime;
        }
        AwareLog.i(TAG, "stayInBackgroudTime=" + stayInBackgroudTime);
        Map<String, Long> killingPkgMap = getLongTimeRunningPkgs(keyPidList, stayInBackgroudTime);
        if (killingPkgMap == null || killingPkgMap.isEmpty()) {
            return null;
        }
        Set<String> killingPkgSet = new ArraySet();
        int keyPidListSize = keyPidList.size();
        for (int i = 0; i < keyPidListSize; i++) {
            ArraySet<Integer> strong = new ArraySet();
            AwareAppAssociate.getInstance().getAssocListForPid(((Integer) keyPidList.get(i)).intValue(), strong);
            int strongSize = strong.size();
            for (int j = 0; j < strongSize; j++) {
                int pid = ((Integer) strong.valueAt(j)).intValue();
                if (pidToPkgMap.containsKey(Integer.valueOf(pid))) {
                    ArrayList<String> pkg = (ArrayList) pidToPkgMap.get(Integer.valueOf(pid));
                    int pkgSize = pkg.size();
                    for (int k = 0; k < pkgSize; k++) {
                        String p = (String) pkg.get(k);
                        AwareLog.d(TAG, "strong associate app:" + p);
                        killingPkgMap.remove(p);
                    }
                }
            }
        }
        for (Entry entry : killingPkgMap.entrySet()) {
            String pkg2 = (String) entry.getKey();
            if (SystemClock.elapsedRealtime() - ((Long) entry.getValue()).longValue() >= stayInBackgroudTime) {
                killingPkgSet.add(pkg2);
            }
        }
        int size = killingPkgSet.size();
        if (size > 0 && this.mAwareUserHabitRadar != null) {
            this.mAwareUserHabitRadar.insertStatisticData("habit_kill", 0, size);
            AwareLog.d(TAG, "recognizeLongTimeRunningApps  num:" + size);
        }
        if (size > 0) {
            List<String> arrayList = new ArrayList(killingPkgSet);
        } else {
            List<String> list = null;
        }
    }

    private Map<String, Long> getLongTimeRunningPkgs(List<Integer> pidList, long bgtime) {
        if (this.mAwareUserHabitAlgorithm == null || this.mAwareHSMListHandler == null) {
            return null;
        }
        Map<String, Long> lruPkgMap = this.mAwareUserHabitAlgorithm.getLongTimePkgsFromLru(bgtime);
        if (lruPkgMap == null) {
            return null;
        }
        Map<String, Long> ltrPkgs = new ArrayMap();
        List<Integer> rulist = new ArrayList();
        Set<String> filterSet = this.mAwareHSMListHandler.getProtectSet();
        AppTypeRecoManager recomanger = AppTypeRecoManager.getInstance();
        int pidListSize = pidList.size();
        for (int i = 0; i < pidListSize; i++) {
            String pkg = InnerUtils.getAwarePkgName(((Integer) pidList.get(i)).intValue());
            if (filterSet.contains(pkg)) {
                AwareLog.d(TAG, "hsm filter set pkg:" + pkg);
            } else if (pkg == null || !lruPkgMap.containsKey(pkg)) {
                rulist.add((Integer) pidList.get(i));
            } else {
                int type = recomanger.getAppType(pkg);
                if (!(5 == type || 2 == type || 310 == type || MemoryConstant.MSG_BOOST_SIGKILL_SWITCH == type)) {
                    ltrPkgs.put(pkg, (Long) lruPkgMap.get(pkg));
                }
            }
        }
        pidList.clear();
        pidList.addAll(rulist);
        return ltrPkgs;
    }

    private void getRunningPidInfo(List<Integer> pidList, Map<Integer, ArrayList<String>> pidToPkgMap) {
        ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        int procListSize = procList.size();
        for (int i = 0; i < procListSize; i++) {
            ProcessInfo process = (ProcessInfo) procList.get(i);
            if (process != null && UserHandle.getAppId(process.mAppUid) > 10000) {
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

    public List<String> getMostFreqAppByType(int appType, int appNum) {
        if (this.mAwareUserHabitAlgorithm == null) {
            return null;
        }
        if (appNum == -1) {
            if (appType == 0) {
                appNum = this.mIsLowEnd ? 2 : 3;
            } else if (1 == appType) {
                appNum = this.mIsLowEnd ? 1 : 2;
            } else if (11 == appType) {
                appNum = this.mIsLowEnd ? 3 : 5;
            }
        }
        if (appNum <= 0) {
            return null;
        }
        return this.mAwareUserHabitAlgorithm.getMostFreqAppByType(appType, appNum);
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
        AwareLog.d(TAG, "getTopN is called n:" + n);
        if (n <= 0) {
            return null;
        }
        List<String> list = null;
        if (this.mIsNewAlgorithmSwitched) {
            if (this.mDynamicTopN <= n) {
                n = this.mDynamicTopN;
            }
            list = new ArrayList(n);
            synchronized (this.mUserTrackListVer2) {
                for (Entry<String, Integer> entry : new ArrayList(this.mUserTrackListVer2.entrySet())) {
                    if (n == 0) {
                        break;
                    }
                    list.add((String) entry.getKey());
                    n--;
                }
            }
        } else if (this.mAwareUserHabitAlgorithm != null) {
            list = this.mAwareUserHabitAlgorithm.getTopN(n);
        }
        return list;
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
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            Entry<Integer, Integer> entry = (Entry) list.get(i);
            map.put(Integer.valueOf(((Integer) entry.getKey()).intValue()), Integer.valueOf(((Integer) entry.getValue()).intValue()));
        }
        return map;
    }

    public void reportHabitData(Bundle bdl) {
        if (bdl != null) {
            int topN = bdl.getInt(REPORT_DYNAMIC_TOPN_KEY, -1);
            if (topN >= 0) {
                this.mDynamicTopN = topN;
            }
            List<String> sortedAppList = bdl.getStringArrayList(REPORT_SORT_KEY);
            if (sortedAppList != null) {
                setTopList(sortedAppList);
                return;
            }
            List<String> smallSampleList = bdl.getStringArrayList(REPORT_SMALL_SAMPLE_LIST_KEY);
            if (smallSampleList != null) {
                AwareIntelligentRecg.getInstance().setSmallSampleList(smallSampleList);
                return;
            }
            if (bdl.getBoolean(REPORT_SWITCHOFF_KEY, false)) {
                this.mIsNewAlgorithmSwitched = false;
            }
        }
    }

    private void setTopList(List<String> topList) {
        if (topList != null) {
            synchronized (this.mUserTrackListVer2) {
                this.mUserTrackListVer2.clear();
                int topListSize = topList.size();
                for (int i = 0; i < topListSize; i++) {
                    this.mUserTrackListVer2.put((String) topList.get(i), Integer.valueOf(i + 1));
                }
            }
            this.mIsNewAlgorithmSwitched = true;
        }
    }

    private Map<String, Integer> getUserTrackListVer2() {
        LinkedHashMap<String, Integer> result = new LinkedHashMap();
        synchronized (this.mUserTrackListVer2) {
            result.putAll(this.mUserTrackListVer2);
        }
        return result;
    }

    public Map<String, Integer> getAllTopList() {
        if (this.mIsNewAlgorithmSwitched) {
            return getUserTrackListVer2();
        }
        return this.mAwareUserHabitAlgorithm.getUserTrackList();
    }

    private void sortPidByPkgs(List<Entry<Integer, Integer>> list, Map<Integer, AwareProcessInfo> appProcesses) {
        if (this.mAwareUserHabitAlgorithm != null) {
            Map<String, Integer> result;
            if (this.mIsNewAlgorithmSwitched) {
                result = getUserTrackListVer2();
            } else {
                result = this.mAwareUserHabitAlgorithm.getUserTrackList();
            }
            for (Entry<Integer, AwareProcessInfo> entry : appProcesses.entrySet()) {
                AwareProcessInfo info = (AwareProcessInfo) entry.getValue();
                if (this.mUserId.get() == UserHandle.getUserId(info.mProcInfo.mUid)) {
                    ArrayList<String> pkgList = info.mProcInfo.mPackageName;
                    int pid = info.mPid;
                    if (pkgList.size() != 1) {
                        int index = -1;
                        int pkgListSize = pkgList.size();
                        for (int j = 0; j < pkgListSize; j++) {
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
            msg.what = 2;
            msg.arg1 = eventId;
            msg.setData(args);
            this.mHandler.sendMessage(msg);
        }
    }

    public void reportScreenState(int state) {
        if (state == ResourceType.RESOURCE_SCREEN_OFF.ordinal()) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 4;
            msg.arg1 = 7;
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
            ActivityManagerNative.getDefault().registerUserSwitchObserver(this.mUserSwitchObserver, TAG);
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
        if (this.mAwareUserHabitRadar != null) {
            ArrayList<StatisticsData> list = this.mAwareUserHabitRadar.getStatisticsData();
            if (list != null) {
                tempList.addAll(list);
            }
        }
        return tempList;
    }

    public String getHabitDfxData(boolean isClear) {
        if (this.mIsNewAlgorithmSwitched) {
            return null;
        }
        float accuracy = 0.0f;
        if (this.mPredictTimes.get() > 0) {
            try {
                accuracy = Float.parseFloat(String.format(Locale.US, "%.4f", new Object[]{Float.valueOf(this.mPredictResult.floatValue() / ((float) this.mPredictTimes.get()))}));
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "getHabitDfxData occur NumberFormatException");
            }
        }
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("accuracy", (double) accuracy);
            jsonObj.put("hitTimes", this.mPredictResult.get());
            jsonObj.put("predictTimes", this.mPredictTimes.get());
            jsonObj.put("trainTimes", this.mTrainTimes.get());
        } catch (JSONException e2) {
            AwareLog.e(TAG, "getHabitDfxData json error!");
        }
        if (isClear) {
            this.mPredictResult.set(0);
            this.mPredictTimes.set(0);
            this.mTrainTimes.set(0);
        }
        return jsonObj.toString();
    }
}
