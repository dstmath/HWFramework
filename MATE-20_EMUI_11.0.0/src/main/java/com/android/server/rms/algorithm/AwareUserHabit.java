package com.android.server.rms.algorithm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.AwareNRTConstant;
import android.rms.iaware.LogIAware;
import android.rms.iaware.StatisticsData;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.algorithm.AwareUserHabitAlgorithm;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.HwStartWindowCache;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.android.app.IUserSwitchObserverEx;
import com.huawei.android.os.IRemoteCallbackEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AwareUserHabit {
    private static final int ACTIVITIES_CHANGED_MESSAGE_DELAY = 300;
    private static final int ALL_DEVICE_PAY_NUM = 3;
    private static final String APP_STATUS = "app_status";
    private static final String DEFAULT_PKG_NAME = "com.huawei.android.launcher";
    private static final int FOREGROUND_ACTIVITIES_CHANGED = 1;
    private static final int HIGH_DEVICE_BUSINESS_NUM = 5;
    private static final int HIGH_DEVICE_EMAIL_NUM = 2;
    private static final int HIGH_DEVICE_IM_NUM = 3;
    public static final long INVALID_TIME = -1;
    private static final int LOAD_APPTYPE_MESSAGE_DELAY = 20000;
    private static final Object LOCK = new Object();
    private static final int LOW_DEVICE_BUSINESS_NUM = 3;
    private static final int LOW_DEVICE_EMAIL_NUM = 1;
    private static final int LOW_DEVICE_IM_NUM = 2;
    private static final int MAXNUM = 10000;
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
    private static final int TAG_FOREGROUND_ACTIVITIES_CHANGED = -1;
    private static final int TOPN = 5;
    private static final int UID_VALUE = 10000;
    private static final int UNINSTALL_CHECK_HABIT_PROTECT_LIST = 3;
    private static final String UNINSTALL_PKGNAME = "uninstall_pkgName";
    public static final int USER_HABIT_INSTALL_APP = 1;
    public static final String USER_HABIT_INSTALL_APP_UPDATE = "install_app_update";
    public static final String USER_HABIT_PACKAGE_NAME = "package_name";
    public static final int USER_HABIT_PRECOG_INITIALIZED = 6;
    public static final int USER_HABIT_SCREEN_OFF = 7;
    public static final int USER_HABIT_TRAIN_COMPLETED = 3;
    public static final String USER_HABIT_UID = "uid";
    public static final int USER_HABIT_UNINSTALL_APP = 2;
    public static final int USER_HABIT_UPDATE_CONFIG = 4;
    public static final int USER_HABIT_USER_SWITCH = 5;
    private static final String VISIBLE_ADJ_TYPE = "visible";
    private static AwareUserHabit sAwareUserHabit = null;
    private static boolean sEnabled = false;
    private String mAppMngLastPkgName = "com.huawei.android.launcher";
    private final AtomicInteger mAppTypeLoadCount = new AtomicInteger(2);
    private final Map<String, Long> mAppUseInfos = new ArrayMap();
    private AwareHsmListHandler mAwareHsmListHandler = null;
    private AwareUserHabitAlgorithm mAwareUserHabitAlgorithm = null;
    private AwareUserHabitRadar mAwareUserHabitRadar = null;
    private Context mContext = null;
    private String mCurPkgName = "com.huawei.android.launcher";
    private int mDynamicTopN = 5;
    private int mEmailCount = 1;
    private long mFeatureStartTime = 0;
    private Handler mHandler = null;
    private long mHighEndDeviceStayInBgTime = 432000000;
    private HwActivityManagerService mHwAMS = null;
    private int mImCount = 2;
    private boolean mIsFirstTime = true;
    private boolean mIsLowEnd = false;
    private boolean mIsNewAlgorithmSwitched = false;
    private String mLastAppPkgName = "com.huawei.android.launcher";
    private long mLastSortTime = 0;
    private boolean mLauncherIsVisible = false;
    private long mLowEndDeviceStayInBgTime = 172800000;
    private int mLruCount = 1;
    private int mMostUsedCount = 1;
    private final Object mOpperLock = new Object();
    private final Object mPredictLock = new Object();
    private UserHabitProcessObserver mProcessObserver = new UserHabitProcessObserver();
    private final AtomicInteger mUserId = new AtomicInteger(0);
    private IUserSwitchObserverEx mUserSwitchObserver = new IUserSwitchObserverEx() {
        /* class com.android.server.rms.algorithm.AwareUserHabit.AnonymousClass1 */

        public void onUserSwitching(int newUserId, IRemoteCallbackEx reply) {
            if (reply != null) {
                try {
                    reply.sendResult((Bundle) null);
                } catch (RemoteException e) {
                    AwareLog.e(AwareUserHabit.TAG, "RemoteException onUserSwitching");
                }
            }
        }

        public void onUserSwitchComplete(int newUserId) {
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
    };
    private final LinkedHashMap<String, Integer> mUserTrackListVer2 = new LinkedHashMap<>();

    private AwareUserHabit(Context context) {
        this.mContext = context;
        initHandler();
    }

    public static AwareUserHabit getInstance(Context context) {
        AwareUserHabit awareUserHabit;
        synchronized (LOCK) {
            if (sAwareUserHabit == null) {
                sAwareUserHabit = new AwareUserHabit(context);
                if (sEnabled) {
                    sAwareUserHabit.init();
                }
            }
            awareUserHabit = sAwareUserHabit;
        }
        return awareUserHabit;
    }

    public static AwareUserHabit getInstance() {
        AwareUserHabit awareUserHabit;
        synchronized (LOCK) {
            awareUserHabit = sAwareUserHabit;
        }
        return awareUserHabit;
    }

    public static void enable() {
        AwareLog.i(TAG, "AwareUserHabit enable is called");
        if (!sEnabled) {
            AwareUserHabit habit = getInstance();
            if (habit != null) {
                habit.init();
            } else {
                AwareLog.i(TAG, "user habit is not ready");
            }
            sEnabled = true;
        }
    }

    public static void disable() {
        AwareLog.i(TAG, "AwareUserHabit disable is called");
        if (sEnabled) {
            AwareUserHabit habit = getInstance();
            if (habit != null) {
                habit.deinit();
            } else {
                AwareLog.i(TAG, "user habit is not ready");
            }
        }
        sEnabled = false;
    }

    public boolean isEnable() {
        return sEnabled;
    }

    public void setLowEndFlag(boolean flag) {
        synchronized (this.mOpperLock) {
            this.mIsLowEnd = flag;
        }
    }

    public List<String> getLRUAppList(int lruCount) {
        return this.mAwareUserHabitAlgorithm.getForceProtectAppsFromLRU(AwareAppAssociate.getInstance().getDefaultHomePackages(), lruCount);
    }

    private void deinit() {
        unregisterObserver();
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm != null) {
            awareUserHabitAlgorithm.deinit();
            this.mIsFirstTime = true;
        }
        AwareHsmListHandler awareHsmListHandler = this.mAwareHsmListHandler;
        if (awareHsmListHandler != null) {
            awareHsmListHandler.deInit();
        }
        AppTypeRecoManager.getInstance().deinit();
        synchronized (this.mAppUseInfos) {
            this.mAppUseInfos.clear();
        }
        AwareLog.d(TAG, "AwareUserHabit deinit finished");
    }

    private void init() {
        Context context = this.mContext;
        if (context != null) {
            if (this.mAwareUserHabitAlgorithm == null) {
                this.mAwareUserHabitAlgorithm = new AwareUserHabitAlgorithm(context);
            }
            this.mHwAMS = HwActivityManagerService.self();
            this.mAwareUserHabitAlgorithm.initHabitProtectList();
            AppTypeRecoManager.getInstance().init(this.mContext);
            registerObserver();
            this.mFeatureStartTime = System.currentTimeMillis();
            if (this.mAwareUserHabitRadar == null) {
                this.mAwareUserHabitRadar = new AwareUserHabitRadar(this.mFeatureStartTime);
            }
            if (this.mAwareHsmListHandler == null) {
                this.mAwareHsmListHandler = new AwareHsmListHandler(this.mContext);
            }
            this.mAwareHsmListHandler.init();
            updateHabitConfig();
            sendLoadAppTypeMessage();
            this.mLastSortTime = System.currentTimeMillis();
            AwareLog.d(TAG, "AwareUserHabit init finished");
        }
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHandler = new UserHabitHandler(looper);
        } else {
            this.mHandler = new UserHabitHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLoadAppTypeMessage() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 2;
        msg.arg1 = 6;
        this.mHandler.sendMessageDelayed(msg, 20000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void triggerUserTrackPredict(String pkgName, Map<String, Long> lruCache, long curTime) {
        if (!this.mIsNewAlgorithmSwitched) {
            synchronized (this.mPredictLock) {
                if (this.mAwareUserHabitAlgorithm != null) {
                    this.mAppMngLastPkgName = this.mAwareUserHabitAlgorithm.getLastPkgNameExcludeLauncher(pkgName, AwareAppAssociate.getInstance().getDefaultHomePackages());
                    if (!this.mAwareUserHabitAlgorithm.containsFilterPkg(pkgName) && !this.mAwareUserHabitAlgorithm.containsFilter2Pkg(pkgName)) {
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
            LinkedHashMap<String, String> result = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : getUserTrackListNewAlgorithm().entrySet()) {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
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
        AwareHsmListHandler awareHsmListHandler = this.mAwareHsmListHandler;
        if (awareHsmListHandler == null) {
            return new ArraySet(0);
        }
        return awareHsmListHandler.getAllProtectSet();
    }

    public Set<String> getAllUnProtectApps() {
        AwareHsmListHandler awareHsmListHandler = this.mAwareHsmListHandler;
        if (awareHsmListHandler == null) {
            return new ArraySet(0);
        }
        return awareHsmListHandler.getAllUnProtectSet();
    }

    public List<String> getForceProtectApps(int num) {
        int lruCount;
        int emailCount;
        int imCount;
        int mostUsedCount;
        AwareLog.i(TAG, "getForceProtectApps is called");
        if (num <= 0 || this.mAwareHsmListHandler == null || this.mAwareUserHabitAlgorithm == null) {
            return new ArrayList(0);
        }
        long startTime = System.currentTimeMillis();
        List<String> result = new ArrayList<>();
        Set<String> filterSet = this.mAwareHsmListHandler.getUnProtectSet();
        synchronized (LOCK) {
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
        AwareLog.d(TAG, "getForceProtectApps spend time:" + (System.currentTimeMillis() - startTime) + " ms result:" + result);
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHabitConfig() {
        Map<String, Integer> config = IAwareHabitUtils.getConfigFromCMS(IAwareHabitUtils.APPMNG, IAwareHabitUtils.HABIT_CONFIG);
        if (config != null) {
            synchronized (this.mOpperLock) {
                if (config.containsKey(IAwareHabitUtils.HABIT_LRU_COUNT)) {
                    this.mLruCount = config.get(IAwareHabitUtils.HABIT_LRU_COUNT).intValue();
                }
                if (config.containsKey(IAwareHabitUtils.HABIT_EMAIL_COUNT)) {
                    this.mEmailCount = config.get(IAwareHabitUtils.HABIT_EMAIL_COUNT).intValue();
                }
                if (config.containsKey(IAwareHabitUtils.HABIT_IM_COUNT)) {
                    this.mImCount = config.get(IAwareHabitUtils.HABIT_IM_COUNT).intValue();
                }
                if (config.containsKey(IAwareHabitUtils.HABIT_MOST_USED_COUNT)) {
                    this.mMostUsedCount = config.get(IAwareHabitUtils.HABIT_MOST_USED_COUNT).intValue();
                }
                if (config.containsKey(IAwareHabitUtils.HABIT_LOW_END)) {
                    this.mLowEndDeviceStayInBgTime = ((long) config.get(IAwareHabitUtils.HABIT_LOW_END).intValue()) * 60000;
                }
                if (config.containsKey(IAwareHabitUtils.HABIT_HIGH_END)) {
                    this.mHighEndDeviceStayInBgTime = ((long) config.get(IAwareHabitUtils.HABIT_HIGH_END).intValue()) * 60000;
                }
            }
        }
    }

    public LinkedHashMap<String, Long> getLruCache() {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm != null) {
            return awareUserHabitAlgorithm.getLruCache();
        }
        return null;
    }

    public Set<String> getBackgroundApps(long duringTime) {
        if (duringTime <= 0) {
            return new ArraySet(0);
        }
        LinkedHashMap<String, Long> lru = getLruCache();
        if (lru == null) {
            return new ArraySet(0);
        }
        long duringTimeMs = 1000 * duringTime;
        ArraySet<String> result = new ArraySet<>();
        ArrayList<String> pkgList = new ArrayList<>(lru.keySet());
        long now = SystemClock.elapsedRealtime();
        for (int i = pkgList.size() - 1; i >= 0; i--) {
            String pkg = pkgList.get(i);
            if (now - lru.get(pkg).longValue() > duringTimeMs) {
                break;
            }
            result.add(pkg);
        }
        return result;
    }

    public String getLastPkgName() {
        return this.mAppMngLastPkgName;
    }

    public List<String> recognizeLongTimeRunningApps() {
        int keyPidListSize;
        ArrayMap<Integer, ArrayList<String>> pidToPkgMap;
        List<Integer> keyPidList;
        synchronized (LOCK) {
            List<Integer> keyPidList2 = new ArrayList<>();
            ArrayMap<Integer, ArrayList<String>> pidToPkgMap2 = new ArrayMap<>();
            getRunningPidInfo(keyPidList2, pidToPkgMap2);
            long stayInBackgroudTime = this.mHighEndDeviceStayInBgTime;
            if (this.mIsLowEnd) {
                stayInBackgroudTime = this.mLowEndDeviceStayInBgTime;
            }
            AwareLog.i(TAG, "stayInBackgroudTime=" + stayInBackgroudTime);
            Map<String, Long> killingPkgMap = getLongTimeRunningPkgs(keyPidList2, stayInBackgroudTime);
            if (killingPkgMap != null) {
                if (!killingPkgMap.isEmpty()) {
                    Set<String> killingPkgSet = new ArraySet<>();
                    int keyPidListSize2 = keyPidList2.size();
                    for (int i = 0; i < keyPidListSize2; i++) {
                        SparseSet strong = new SparseSet();
                        AwareAppAssociate.getInstance().getAssocListForPid(keyPidList2.get(i).intValue(), strong);
                        int strongSize = strong.size();
                        int j = 0;
                        while (j < strongSize) {
                            int pid = strong.keyAt(j);
                            if (!pidToPkgMap2.containsKey(Integer.valueOf(pid))) {
                                keyPidList = keyPidList2;
                                pidToPkgMap = pidToPkgMap2;
                                keyPidListSize = keyPidListSize2;
                            } else {
                                ArrayList<String> pkg = pidToPkgMap2.get(Integer.valueOf(pid));
                                int pkgSize = pkg.size();
                                int k = 0;
                                while (true) {
                                    keyPidList = keyPidList2;
                                    if (k >= pkgSize) {
                                        break;
                                    }
                                    String str = pkg.get(k);
                                    pkgSize = pkgSize;
                                    AwareLog.d(TAG, "strong associate app:" + str);
                                    killingPkgMap.remove(str);
                                    k++;
                                    keyPidList2 = keyPidList;
                                    pidToPkgMap2 = pidToPkgMap2;
                                    keyPidListSize2 = keyPidListSize2;
                                }
                                pidToPkgMap = pidToPkgMap2;
                                keyPidListSize = keyPidListSize2;
                            }
                            j++;
                            keyPidList2 = keyPidList;
                            pidToPkgMap2 = pidToPkgMap;
                            keyPidListSize2 = keyPidListSize;
                        }
                    }
                    for (Map.Entry<String, Long> entry : killingPkgMap.entrySet()) {
                        String pkg2 = entry.getKey();
                        if (SystemClock.elapsedRealtime() - entry.getValue().longValue() >= stayInBackgroudTime) {
                            killingPkgSet.add(pkg2);
                        }
                    }
                    int size = killingPkgSet.size();
                    if (size > 0 && this.mAwareUserHabitRadar != null) {
                        this.mAwareUserHabitRadar.insertStatisticData("habit_kill", 0, size);
                        AwareLog.d(TAG, "recognizeLongTimeRunningApps num:" + size);
                    }
                    return size > 0 ? new ArrayList(killingPkgSet) : new ArrayList(0);
                }
            }
            return new ArrayList(0);
        }
    }

    private Map<String, Long> getLongTimeRunningPkgs(List<Integer> pidList, long bgTime) {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm == null || this.mAwareHsmListHandler == null) {
            return new ArrayMap(0);
        }
        Map<String, Long> lruPkgMap = awareUserHabitAlgorithm.getLongTimePkgsFromLru(bgTime);
        if (lruPkgMap == null) {
            return new ArrayMap(0);
        }
        Map<String, Long> ltrPkgs = new ArrayMap<>();
        List<Integer> ruList = new ArrayList<>();
        Set<String> filterSet = this.mAwareHsmListHandler.getProtectSet();
        AppTypeRecoManager recomanger = AppTypeRecoManager.getInstance();
        int pidListSize = pidList.size();
        for (int i = 0; i < pidListSize; i++) {
            String pkg = InnerUtils.getAwarePkgName(pidList.get(i).intValue());
            if (filterSet.contains(pkg)) {
                AwareLog.d(TAG, "hsm filter set pkg:" + pkg);
            } else if (pkg == null || !lruPkgMap.containsKey(pkg)) {
                ruList.add(pidList.get(i));
            } else {
                int type = recomanger.getAppType(pkg);
                if (!(type == 5 || type == 2 || type == 310 || type == 301)) {
                    ltrPkgs.put(pkg, lruPkgMap.get(pkg));
                }
            }
        }
        pidList.clear();
        pidList.addAll(ruList);
        return ltrPkgs;
    }

    private void getRunningPidInfo(List<Integer> pidList, Map<Integer, ArrayList<String>> pidToPkgMap) {
        ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        int procListSize = procList.size();
        for (int i = 0; i < procListSize; i++) {
            ProcessInfo process = procList.get(i);
            if (process != null && UserHandleEx.getAppId(process.mAppUid) > 10000) {
                int pid = Integer.valueOf(process.mPid).intValue();
                ArrayList<String> plist = new ArrayList<>();
                plist.addAll(process.mPackageName);
                pidToPkgMap.put(Integer.valueOf(pid), plist);
                pidList.add(Integer.valueOf(pid));
            }
        }
    }

    public List<String> getMostFrequentUsedApp(int num, int minCount) {
        AwareLog.i(TAG, "getMostFrequentUsedApp is called");
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm == null || num <= 0) {
            return null;
        }
        return awareUserHabitAlgorithm.getMostFrequentUsedApp(num, minCount, null);
    }

    public List<String> getMostFreqAppByType(int appType, int appNum) {
        if (this.mAwareUserHabitAlgorithm == null) {
            return new ArrayList(0);
        }
        int appNumTmp = appNum;
        if (appNum == -1) {
            int i = 2;
            int i2 = 3;
            if (appType == 0) {
                if (!this.mIsLowEnd) {
                    i = 3;
                }
                appNumTmp = i;
            } else if (appType == 1) {
                if (this.mIsLowEnd) {
                    i = 1;
                }
                appNumTmp = i;
            } else if (appType == 11) {
                if (!this.mIsLowEnd) {
                    i2 = 5;
                }
                appNumTmp = i2;
            } else if (appType == 34) {
                appNumTmp = 3;
            }
        }
        if (appNumTmp <= 0) {
            return new ArrayList(0);
        }
        return this.mAwareUserHabitAlgorithm.getMostFreqAppByType(appType, appNumTmp);
    }

    public List<String> getHabitProtectList(int emailCount, int imCount) {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm;
        if (imCount <= 0 || emailCount <= 0 || (awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm) == null) {
            return new ArrayList(0);
        }
        return awareUserHabitAlgorithm.getHabitProtectList(emailCount, imCount);
    }

    public List<String> getHabitProtectListAll(int emailCount, int imCount) {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm;
        if (imCount <= 0 || emailCount <= 0 || (awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm) == null) {
            return new ArrayList(0);
        }
        return awareUserHabitAlgorithm.getHabitProtectAppsAll(emailCount, imCount);
    }

    public List<String> getTopN(int num) {
        AwareLog.d(TAG, "getTopN is called num:" + num);
        if (num <= 0) {
            return new ArrayList(0);
        }
        if (!this.mIsNewAlgorithmSwitched) {
            AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
            if (awareUserHabitAlgorithm != null) {
                return awareUserHabitAlgorithm.getTopN(num);
            }
            return null;
        }
        int numTmp = this.mDynamicTopN;
        if (numTmp > num) {
            numTmp = num;
        }
        List<String> list = new ArrayList<>(numTmp);
        synchronized (this.mUserTrackListVer2) {
            Iterator<Map.Entry<String, Integer>> it = new ArrayList<>(this.mUserTrackListVer2.entrySet()).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Map.Entry<String, Integer> entry = it.next();
                if (numTmp == 0) {
                    break;
                }
                list.add(entry.getKey());
                numTmp--;
            }
        }
        return list;
    }

    public Set<String> getFilterApp() {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm != null) {
            return awareUserHabitAlgorithm.getFilterApp();
        }
        return new ArraySet(0);
    }

    public Map<Integer, Integer> getTopList(Map<Integer, AwareProcessInfo> appProcesses) {
        AwareLog.i(TAG, "getTopList is called");
        if (appProcesses == null) {
            return new ArrayMap(0);
        }
        return getUserTopList(appProcesses);
    }

    private Map<Integer, Integer> getUserTopList(Map<Integer, AwareProcessInfo> appProcesses) {
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>();
        ArrayMap<Integer, Integer> map = new ArrayMap<>();
        sortPidByPkgs(list, appProcesses);
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            Map.Entry<Integer, Integer> entry = list.get(i);
            map.put(Integer.valueOf(entry.getKey().intValue()), Integer.valueOf(entry.getValue().intValue()));
        }
        return map;
    }

    public void reportHabitData(Bundle bdl) {
        if (bdl != null) {
            int topN = bdl.getInt(REPORT_DYNAMIC_TOPN_KEY, -1);
            if (topN >= 0) {
                this.mDynamicTopN = topN;
            }
            try {
                List<String> sortedAppList = bdl.getStringArrayList(REPORT_SORT_KEY);
                if (sortedAppList != null) {
                    setTopList(sortedAppList);
                    return;
                }
                List<String> smallSampleList = bdl.getStringArrayList(REPORT_SMALL_SAMPLE_LIST_KEY);
                if (smallSampleList != null) {
                    AwareIntelligentRecg.getInstance().setSmallSampleList(smallSampleList);
                } else if (bdl.getBoolean(REPORT_SWITCHOFF_KEY, false)) {
                    this.mIsNewAlgorithmSwitched = false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                AwareLog.e(TAG, "getStringArrayList out of bounds exception!");
            }
        }
    }

    private void setTopList(List<String> topList) {
        if (topList != null) {
            synchronized (this.mUserTrackListVer2) {
                this.mUserTrackListVer2.clear();
                int topListSize = topList.size();
                for (int i = 0; i < topListSize; i++) {
                    this.mUserTrackListVer2.put(topList.get(i), Integer.valueOf(i + 1));
                }
            }
            this.mIsNewAlgorithmSwitched = true;
        }
    }

    private Map<String, Integer> getUserTrackListNewAlgorithm() {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        synchronized (this.mUserTrackListVer2) {
            result.putAll(this.mUserTrackListVer2);
        }
        return result;
    }

    public Map<String, Integer> getAllTopList() {
        if (this.mIsNewAlgorithmSwitched) {
            return getUserTrackListNewAlgorithm();
        }
        return this.mAwareUserHabitAlgorithm.getUserTrackList();
    }

    private void sortPidByPkgs(List<Map.Entry<Integer, Integer>> list, Map<Integer, AwareProcessInfo> appProcesses) {
        Map<String, Integer> result;
        if (this.mAwareUserHabitAlgorithm != null) {
            if (this.mIsNewAlgorithmSwitched) {
                result = getUserTrackListNewAlgorithm();
            } else {
                result = this.mAwareUserHabitAlgorithm.getUserTrackList();
            }
            for (Map.Entry<Integer, AwareProcessInfo> entry : appProcesses.entrySet()) {
                AwareProcessInfo info = entry.getValue();
                if (this.mUserId.get() == UserHandleEx.getUserId(info.procProcInfo.mUid)) {
                    ArrayList<String> pkgList = info.procProcInfo.mPackageName;
                    int pid = info.procPid;
                    if (pkgList.size() != 1) {
                        sortPidByPkgsInner(pkgList, result, pid, list);
                    } else if (result.containsKey(pkgList.get(0))) {
                        list.add(new AbstractMap.SimpleEntry(Integer.valueOf(pid), result.get(pkgList.get(0))));
                    }
                }
            }
        }
    }

    private void sortPidByPkgsInner(ArrayList<String> pkgList, Map<String, Integer> result, int pid, List<Map.Entry<Integer, Integer>> list) {
        int index = -1;
        int pkgListSize = pkgList.size();
        for (int j = 0; j < pkgListSize; j++) {
            if (result.containsKey(pkgList.get(j))) {
                if (index == -1) {
                    index = result.get(pkgList.get(j)).intValue();
                } else {
                    int order = result.get(pkgList.get(j)).intValue();
                    if (order < index) {
                        index = order;
                    }
                }
            }
        }
        if (index != -1) {
            list.add(new AbstractMap.SimpleEntry(Integer.valueOf(pid), Integer.valueOf(index)));
        }
    }

    public void report(int eventId, Bundle args) {
        if (sEnabled && args != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 2;
            msg.arg1 = eventId;
            msg.setData(args);
            this.mHandler.sendMessage(msg);
        }
    }

    public void reportScreenState(int state) {
        if (state == AwareConstant.ResourceType.RESOURCE_SCREEN_OFF.ordinal()) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 4;
            msg.arg1 = 7;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public class UserHabitHandler extends Handler {
        public UserHabitHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                String pkgName = InnerUtils.getAwarePkgName(msg.arg1);
                handleActivityChangedEvent(msg, pkgName);
                if (!msg.getData().getBoolean(AwareUserHabit.APP_STATUS)) {
                    AwareIntelligentRecg.getInstance().reportAppChangeToBackground(pkgName, SystemClock.elapsedRealtime());
                }
            } else if (i == 2) {
                handleReportMessage(msg);
            } else if (i == 4) {
                handleScreenState(msg);
            }
        }

        private void handleScreenState(Message msg) {
            if (AwareUserHabit.this.mAwareUserHabitAlgorithm != null && msg.arg1 == 7 && Math.abs(System.currentTimeMillis() - AwareUserHabit.this.mLastSortTime) > AwareUserHabit.SREEN_OFF_SORT_THRESHOLD) {
                AwareUserHabit.this.mAwareUserHabitAlgorithm.sortUsageCount();
                AwareUserHabit.this.mLastSortTime = System.currentTimeMillis();
            }
        }

        private void handleActivityChangedEventInner() {
            if (AwareUserHabit.this.mIsFirstTime) {
                AwareUserHabit.this.mAwareUserHabitAlgorithm.init();
                AwareUserHabit.this.mIsFirstTime = false;
                AwareDefaultConfigList.getInstance().fillMostFrequentUsedApp(AwareUserHabit.this.getMostFrequentUsedApp(AppMngConfig.getAdjCustTopN(), -1));
                LogIAware.report(AwareNRTConstant.USERHABIT_DATA_LOAD_FINISHED_EVENT_ID, "userhabit inited");
                if (AwareUserHabit.this.mContext != null) {
                    ArrayMap arrayMap = new ArrayMap();
                    AppTypeRecoManager.getInstance().loadAppUsedInfo(AwareUserHabit.this.mContext, arrayMap, AwareUserHabit.this.mUserId.intValue(), 7);
                    synchronized (AwareUserHabit.this.mAppUseInfos) {
                        AwareUserHabit.this.mAppUseInfos.putAll(arrayMap);
                    }
                }
                AwareLog.i(AwareUserHabit.TAG, "report first data loading finished");
            }
        }

        private void handleActivityChangedEvent(Message msg, String pkgName) {
            if (AwareUserHabit.this.mAwareUserHabitAlgorithm != null && pkgName != null) {
                handleActivityChangedEventInner();
                long time = SystemClock.elapsedRealtime();
                boolean isForeground = msg.getData().getBoolean(AwareUserHabit.APP_STATUS);
                int homePkgPid = AwareAppAssociate.getInstance().getCurHomeProcessPid();
                AwareProcessBaseInfo baseInfo = AwareUserHabit.this.mHwAMS != null ? AwareUserHabit.this.mHwAMS.getProcessBaseInfo(homePkgPid) : null;
                if (isForeground) {
                    AwareUserHabit awareUserHabit = AwareUserHabit.this;
                    awareUserHabit.triggerUserTrackPredict(pkgName, awareUserHabit.getLruCache(), time);
                    AwareUserHabit.this.mAwareUserHabitAlgorithm.foregroundUpdateHabitProtectList(pkgName);
                    AwareUserHabit.this.mAwareUserHabitAlgorithm.updateAppUsage(pkgName);
                    if (!(homePkgPid == msg.arg1 || baseInfo == null || !AwareUserHabit.VISIBLE_ADJ_TYPE.equals(baseInfo.adjType))) {
                        AwareUserHabit.this.mLauncherIsVisible = true;
                    }
                    synchronized (AwareUserHabit.this.mAppUseInfos) {
                        AwareUserHabit.this.mAppUseInfos.put(pkgName, Long.valueOf(System.currentTimeMillis()));
                    }
                    time = -1;
                } else {
                    if (AwareUserHabit.this.mLauncherIsVisible && homePkgPid != msg.arg1 && baseInfo != null && baseInfo.curAdj == HwActivityManagerService.FOREGROUND_APP_ADJ) {
                        handleActivityChangedEventEx(homePkgPid, time);
                    }
                    AwareUserHabit.this.mLauncherIsVisible = false;
                    AwareUserHabit.this.mAwareUserHabitAlgorithm.backgroundActivityChangedEvent(pkgName, Long.valueOf(time));
                }
                AwareUserHabit.this.mAwareUserHabitAlgorithm.addPkgToLru(pkgName, time);
            }
        }

        private void handleActivityChangedEventEx(int homePkgPid, long time) {
            String homePkgName = InnerUtils.getAwarePkgName(homePkgPid);
            if (homePkgName != null) {
                AwareUserHabit awareUserHabit = AwareUserHabit.this;
                awareUserHabit.triggerUserTrackPredict(homePkgName, awareUserHabit.getLruCache(), time);
            }
        }

        private void handleMsgTrainCompleted(int eventId) {
            AwareUserHabit.this.mAwareUserHabitAlgorithm.reloadDataInfo();
            if (eventId == 3) {
                AwareUserHabit.this.mAwareUserHabitAlgorithm.trainedUpdateHabitProtectList();
            } else {
                AwareUserHabit.this.mAwareUserHabitAlgorithm.clearLruCache();
                AwareUserHabit.this.mAwareUserHabitAlgorithm.initHabitProtectList();
                AwareUserHabit.this.mAwareUserHabitAlgorithm.clearHabitProtectApps();
                AwareUserHabit.this.mLastSortTime = System.currentTimeMillis();
                if (AwareUserHabit.this.mContext != null) {
                    ArrayMap arrayMap = new ArrayMap();
                    AppTypeRecoManager.getInstance().loadAppUsedInfo(AwareUserHabit.this.mContext, arrayMap, AwareUserHabit.this.mUserId.intValue(), 7);
                    synchronized (AwareUserHabit.this.mAppUseInfos) {
                        AwareUserHabit.this.mAppUseInfos.clear();
                        AwareUserHabit.this.mAppUseInfos.putAll(arrayMap);
                    }
                }
            }
            AwareDefaultConfigList.getInstance().fillMostFrequentUsedApp(AwareUserHabit.this.getMostFrequentUsedApp(AppMngConfig.getAdjCustTopN(), -1));
            LogIAware.report(AwareNRTConstant.USERHABIT_DATA_LOAD_FINISHED_EVENT_ID, "train completed");
            AwareLog.i(AwareUserHabit.TAG, "report train completed and data loading finished");
        }

        private void handleMsgPrecogInit() {
            AwareLog.i(AwareUserHabit.TAG, "AwareUserHabit load precog pkg type info");
            if (AppTypeRecoManager.getInstance().loadInstalledAppTypeInfo()) {
                AwareLog.i(AwareUserHabit.TAG, "AwareUserHabit load precog pkg OK ");
            } else if (AwareUserHabit.this.mAppTypeLoadCount.get() >= 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("AwareUserHabit send load precog pkg message again mAppTypeLoadCount=");
                sb.append(AwareUserHabit.this.mAppTypeLoadCount.get() - 1);
                AwareLog.i(AwareUserHabit.TAG, sb.toString());
                AwareUserHabit.this.sendLoadAppTypeMessage();
                AwareUserHabit.this.mAppTypeLoadCount.decrementAndGet();
            } else {
                AwareLog.e(AwareUserHabit.TAG, "AwareUserHabit precog service is error");
            }
        }

        private void handleReportMessage(Message msg) {
            if (AwareUserHabit.this.mAwareUserHabitAlgorithm != null) {
                int eventId = msg.arg1;
                Bundle args = msg.getData();
                if (args != null) {
                    switch (eventId) {
                        case 1:
                            String pkgName = args.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME);
                            if (pkgName != null) {
                                AwareUserHabit.this.mAwareUserHabitAlgorithm.removeFilterPkg(pkgName);
                                return;
                            }
                            return;
                        case 2:
                            String pkgName2 = args.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME);
                            if (pkgName2 != null) {
                                AwareUserHabit.this.mAwareUserHabitAlgorithm.removePkgFromLru(pkgName2);
                                AwareUserHabit.this.mAwareUserHabitAlgorithm.addFilterPkg(pkgName2);
                                AwareUserHabit.this.mAwareUserHabitAlgorithm.uninstallUpdateHabitProtectList(pkgName2);
                                HwStartWindowCache.getInstance().clearCacheWhenUninstall(pkgName2);
                                return;
                            }
                            return;
                        case 3:
                        case 5:
                            handleMsgTrainCompleted(eventId);
                            return;
                        case 4:
                            AwareLog.i(AwareUserHabit.TAG, "reloadFilterPkg");
                            AwareUserHabit.this.mAwareUserHabitAlgorithm.reloadFilterPkg();
                            AwareUserHabit.this.updateHabitConfig();
                            return;
                        case 6:
                            handleMsgPrecogInit();
                            return;
                        default:
                            return;
                    }
                }
            }
        }
    }

    private void registerObserver() {
        AwareCallback.getInstance().registerProcessObserver(this.mProcessObserver);
        AwareCallback.getInstance().registerUserSwitchObserver(this.mUserSwitchObserver);
    }

    private void unregisterObserver() {
        AwareCallback.getInstance().unregisterProcessObserver(this.mProcessObserver);
        AwareCallback.getInstance().unregisterUserSwitchObserver(this.mUserSwitchObserver);
    }

    /* access modifiers changed from: package-private */
    public class UserHabitProcessObserver extends IProcessObserverEx {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setUserId(int userId) {
        AwareHsmListHandler awareHsmListHandler = this.mAwareHsmListHandler;
        if (awareHsmListHandler != null) {
            awareHsmListHandler.setUserId(userId);
        }
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm != null) {
            awareUserHabitAlgorithm.setUserId(userId);
        }
    }

    public void registHabitProtectListChangeListener(AwareUserHabitAlgorithm.HabitProtectListChangeListener listener) {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm != null) {
            awareUserHabitAlgorithm.registHabitProtectListChangeListener(listener);
        }
    }

    public void unregistHabitProtectListChangeListener(AwareUserHabitAlgorithm.HabitProtectListChangeListener listener) {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm != null) {
            awareUserHabitAlgorithm.unregistHabitProtectListChangeListener(listener);
        }
    }

    public List<String> queryHabitProtectAppList(int imCount, int emailCount) {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm != null) {
            return awareUserHabitAlgorithm.queryHabitProtectAppList(imCount, emailCount);
        }
        return null;
    }

    public List<String> getGCMAppList() {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm != null) {
            return awareUserHabitAlgorithm.getGCMAppsList();
        }
        return null;
    }

    public void dumpHabitProtectList(PrintWriter pw) {
        AwareUserHabitAlgorithm awareUserHabitAlgorithm = this.mAwareUserHabitAlgorithm;
        if (awareUserHabitAlgorithm != null) {
            awareUserHabitAlgorithm.dumpHabitProtectList(pw);
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        ArrayList<StatisticsData> list;
        ArrayList<StatisticsData> tempList = new ArrayList<>();
        AwareUserHabitRadar awareUserHabitRadar = this.mAwareUserHabitRadar;
        if (!(awareUserHabitRadar == null || (list = awareUserHabitRadar.getStatisticsData()) == null)) {
            tempList.addAll(list);
        }
        return tempList;
    }

    public List<String> getMostFreqAppByTypeEx(int appType, int appNum) {
        if (appNum <= 0) {
            return new ArrayList(0);
        }
        List<String> appList = getMostFreqAppByType(appType, 10000);
        if (appList == null) {
            return new ArrayList(0);
        }
        return getMostAppByUsedInfo(appNum, appList, 604800000);
    }

    public void dumpMostFreqAppByTypeEx(PrintWriter pw, String[] args) {
        if (args.length > 3) {
            try {
                int appType = Integer.parseInt(args[args.length - 3]);
                int idx = 3 - 1;
                try {
                    int appNum = Integer.parseInt(args[args.length - idx]);
                    try {
                        int dayNum = Integer.parseInt(args[args.length - (idx - 1)]);
                        List<String> appList = getMostFreqAppByType(appType, 10000);
                        if (appList != null) {
                            pw.println(getMostAppByUsedInfo(appNum, appList, ((long) dayNum) * STAY_IN_BACKGROUND_ONE_DAYS).toString());
                        }
                    } catch (NumberFormatException e) {
                        pw.println("Bad input value param");
                    }
                } catch (NumberFormatException e2) {
                    pw.println("Bad input value param");
                }
            } catch (NumberFormatException e3) {
                pw.println("Bad input value param");
            }
        }
    }

    private List<String> getMostAppByUsedInfo(int appNum, List<String> mostUsedList, long dayTimes) {
        long now = System.currentTimeMillis();
        long diffTime = now - dayTimes;
        synchronized (this.mAppUseInfos) {
            try {
                List<String> result = new ArrayList<>();
                int i = 0;
                int length = mostUsedList.size();
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    try {
                        String pkgName = mostUsedList.get(i);
                        Long switchFgTime = this.mAppUseInfos.get(pkgName);
                        if (switchFgTime != null && switchFgTime.longValue() >= diffTime) {
                            if (switchFgTime.longValue() <= now) {
                                result.add(pkgName);
                                if (result.size() < appNum) {
                                }
                            }
                        }
                        i++;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
                return result;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    public long getAppSwitchFgTime(String pkgName) {
        synchronized (this.mAppUseInfos) {
            Long switchFgTime = this.mAppUseInfos.get(pkgName);
            if (switchFgTime == null) {
                return -1;
            }
            return switchFgTime.longValue();
        }
    }
}
