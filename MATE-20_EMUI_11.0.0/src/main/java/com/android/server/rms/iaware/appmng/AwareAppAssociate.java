package com.android.server.rms.iaware.appmng;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.LruCache;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.app.ProcessMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.appclean.CrashClean;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.SystemUnremoveUidCache;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.appmng.AwareAppAssociateUtils;
import com.android.server.rms.iaware.cpu.NetManager;
import com.android.server.rms.iaware.feature.SceneRecogFeature;
import com.android.server.rms.iaware.memory.policy.CachedMemoryCleanPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.AppCleanupFeature;
import com.huawei.android.app.ActivityManagerNativeExt;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.android.app.IUserSwitchObserverEx;
import com.huawei.android.content.pm.UserInfoExAdapter;
import com.huawei.android.internal.util.MemInfoReaderExt;
import com.huawei.android.os.IRemoteCallbackEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SparseArrayEx;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AwareAppAssociate {
    public static final int ASSOC_DECAY_MIN_TIME = 120000;
    public static final int ASSOC_REPORT_MIN_TIME = 60000;
    public static final int CLEAN_LEVEL = 0;
    private static final int CONSTANT_MORE_PREVIOUS_DISABLE = 0;
    private static final int CONSTANT_MORE_PREVIOUS_LEVEL1 = 1;
    private static final int CONSTANT_MORE_PREVIOUS_LEVEL2 = 2;
    private static final int CONSTANT_MORE_PREVIOUS_LEVEL_HIGH = 4;
    private static final int COUNT_PROTECT_DEFAULT = 1;
    private static final int COUNT_PROTECT_HIGH = 4;
    private static final int COUNT_PROTECT_MORE = 2;
    public static final int FIRST_START_TIMES = 1;
    private static final int FIVE_SECONDS = 5000;
    private static final int HIGH_MEM_THRESHOLD = 6144;
    private static final int INIT_SIZE = 9;
    private static final String INTERNAL_APP_PKGNAME = "com.huawei.android.internal.app";
    public static final int INVALID_VALUE = -1;
    private static final Object LOCK = new Object();
    private static final int LOW_MEM_THRESHOLD = 3072;
    private static final int MSG_CHECK_RECENT_FORE = 3;
    private static final int MSG_CLEAN = 2;
    private static final int MSG_CLEAR_BAKUP_VISWIN = 4;
    private static final int MSG_INIT = 1;
    public static final int MS_TO_SEC = 1000;
    private static final int ONE_SECOND = 1000;
    private static final String PERMISSIOM_CONTROLLER = "com.android.permissioncontroller";
    private static final int PREVIOUS_HIGH_DEFAULT = -1;
    private static final int PREVIOUS_HIGH_DISABLE = 0;
    private static final int PREVIOUS_HIGH_ENABLE = 1;
    private static final int RECENT_TIME_INTERVAL = 10000;
    private static final long REINIT_TIME = 2000;
    public static final int RESTART_MAX_INTERVAL = 300;
    public static final int RESTART_MAX_TIMES = 30;
    private static final String SYSTEM = "system";
    private static final String SYSTEM_UI_PKGNAME = "com.android.systemui";
    private static final String TAG = "RMS.AwareAppAssociate";
    private static AwareAppAssociate sAwareAppAssociate = null;
    private static boolean sDebug = false;
    private static boolean sEnabled = false;
    private static boolean sRecord = false;
    private final AwareAppLruBase mAmsPrevBase = new AwareAppLruBase();
    private final SparseArray<AwareAppAssociateUtils.AssocPidRecord> mAssocRecordMap = new SparseArray<>();
    private AwareAppAssociateWidget mAssociateWidget = new AwareAppAssociateWidget();
    private AwareAppAssociateWindow mAssociateWindow = new AwareAppAssociateWindow();
    private final ArrayMap<Integer, ProcessData> mBgRecentForcePids = new ArrayMap<>();
    private final ArraySet<IAwareVisibleCallback> mCallbacks = new ArraySet<>();
    private int mCurSwitchUser = 0;
    private int mCurUserId = 0;
    private final SparseIntArray mForePids = new SparseIntArray();
    private AppAssocHandler mHandler = null;
    private final ArrayList<String> mHomePackageList = new ArrayList<>();
    private int mHomeProcessPid = 0;
    private int mHomeProcessUid = 0;
    private HwActivityManagerService mHwAms = HwActivityManagerService.self();
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private int mIsPreviousHighEnable = SystemPropertiesEx.getInt("persist.sys.iaware.previoushigh", -1);
    private final Object mLock = new Object();
    private final LruCache<Integer, AwareAppLruBase> mLruCache = new LruCache<>(9);
    private int mMorePreviousLevel = 0;
    private MultiTaskManagerService mMtmService;
    private int mMyPid = Process.myPid();
    private final AwareAppLruBase mPrevNonHomeBase = new AwareAppLruBase();
    private final ProcessMap<AwareAppAssociateUtils.AssocBaseRecord> mProcInfoMap = new ProcessMap<>();
    private final Map<Integer, Map<String, Map<String, LaunchData>>> mProcLaunchMap = new HashMap();
    private final SparseArray<AwareAppAssociateUtils.AssocBaseRecord> mProcPidMap = new SparseArray<>();
    private final ArrayMap<String, SparseSet> mProcPkgMap = new ArrayMap<>();
    private final SparseArray<SparseSet> mProcUidMap = new SparseArray<>();
    private IProcessObserverEx mProcessObserver = new IProcessObserverEx() {
        /* class com.android.server.rms.iaware.appmng.AwareAppAssociate.AnonymousClass2 */

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (AwareAppAssociate.sDebug) {
                AwareLog.i(AwareAppAssociate.TAG, "Pid:" + pid + ",Uid:" + uid + " come to foreground." + foregroundActivities);
            }
            SparseIntArray forePidsBak = new SparseIntArray();
            synchronized (AwareAppAssociate.this.mForePids) {
                if (foregroundActivities) {
                    AwareAppAssociate.this.mForePids.put(pid, uid);
                } else {
                    AwareAppAssociate.this.mForePids.delete(pid);
                }
                AwareAppAssociate.this.addAllForSparseIntArray(AwareAppAssociate.this.mForePids, forePidsBak);
            }
            synchronized (AwareAppAssociate.this.mBgRecentForcePids) {
                if (foregroundActivities) {
                    AwareAppAssociate.this.mBgRecentForcePids.remove(Integer.valueOf(pid));
                } else {
                    AwareAppAssociate.this.mBgRecentForcePids.put(Integer.valueOf(pid), new ProcessData(uid, SystemClock.elapsedRealtime()));
                    if (AwareAppAssociate.this.mHandler != null) {
                        AwareAppAssociate.this.mHandler.sendEmptyMessageDelayed(3, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                    }
                }
            }
            AwareAppAssociate.this.updatePreviousAppInfo(pid, uid, foregroundActivities, forePidsBak);
            AwareSwitchCleanManager.getInstance().notifyFgActivitiesChanged(pid, uid, foregroundActivities);
            AwareAppUseDataManager.getInstance().updateFgActivityChange(pid, uid, foregroundActivities);
            if (foregroundActivities && pid == AwareAppAssociate.this.mHomeProcessPid) {
                ContinuePowerDevMng.getInstance().tryPreLoadPermanentApplication();
            }
            AwareComponentPreloadManager.getInstance().updateFgActivityChange(pid, uid, foregroundActivities);
            AwareIntelligentRecg.getInstance().onForegroundActivitiesChanged(pid, uid, foregroundActivities);
            CachedMemoryCleanPolicy.getInstance().onForegroundActivitiesChanged(pid, uid, foregroundActivities);
        }

        public void onProcessDied(int pid, int uid) {
            synchronized (AwareAppAssociate.this.mForePids) {
                AwareAppAssociate.this.mForePids.delete(pid);
            }
            synchronized (AwareAppAssociate.this.mBgRecentForcePids) {
                AwareAppAssociate.this.mBgRecentForcePids.remove(Integer.valueOf(pid));
            }
            try {
                AwareAppAssociate.this.removeDiedProcessRelation(pid, uid);
            } catch (NullPointerException e) {
                AwareLog.d(AwareAppAssociate.TAG, "remove died processrelation failed caused by null pointer");
            } catch (Exception e2) {
                AwareLog.d(AwareAppAssociate.TAG, "remove died processrelation failed");
            }
            AwareAppAssociateUtils.removeDiedRecordProc(uid, pid);
            HwActivityManager.reportProcessDied(pid);
            AwareIntelligentRecg.getInstance().onProcessDied(pid, uid);
        }
    };
    private final AwareAppLruBase mRecentTaskPrevBase = new AwareAppLruBase();
    private SystemUnremoveUidCache mSystemUnremoveUidCache;
    private IUserSwitchObserverEx mUserSwitchObserver = new IUserSwitchObserverEx() {
        /* class com.android.server.rms.iaware.appmng.AwareAppAssociate.AnonymousClass1 */

        public void onUserSwitching(int newUserId, IRemoteCallbackEx reply) {
            if (reply != null) {
                try {
                    reply.sendResult((Bundle) null);
                    AwareAppAssociate.this.mCurSwitchUser = newUserId;
                } catch (RemoteException e) {
                    AwareLog.e(AwareAppAssociate.TAG, "RemoteException onUserSwitching");
                }
            }
        }

        public void onUserSwitchComplete(int newUserId) {
            long startTime = System.currentTimeMillis();
            AwareAppAssociate.this.mAssociateWidget.checkAndInitWidgetObj(newUserId);
            AwareAppAssociate.this.mCurUserId = newUserId;
            AwareAppAssociate.this.mCurSwitchUser = newUserId;
            AwareAppAssociate.this.mAssociateWidget.updateWidgets(AwareAppAssociate.this.mCurUserId);
            AwareIntelligentRecg.getInstance().initUserSwitch(newUserId);
            AwareFakeActivityRecg.self().initUserSwitch(newUserId);
            if (newUserId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(AwareAppAssociate.this.getWidgetsPkg(newUserId), null);
            }
            AwareAppUseDataManager.getInstance().initUserSwitch();
            CachedMemoryCleanPolicy.getInstance().initUserSwitch();
            AwareLog.i(AwareAppAssociate.TAG, "onUserSwitchComplete cost: " + (System.currentTimeMillis() - startTime));
        }
    };

    public interface IAwareVisibleCallback {
        void onVisibleWindowsChanged(int i, int i2, int i3);
    }

    /* access modifiers changed from: private */
    public static class ProcessData {
        private long mTimeStamp;
        private int mUid;

        private ProcessData(int uid, long timeStamp) {
            this.mUid = uid;
            this.mTimeStamp = timeStamp;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkRecentForce() {
        int removeCount = 0;
        long curTime = SystemClock.elapsedRealtime();
        synchronized (this.mBgRecentForcePids) {
            for (int i = this.mBgRecentForcePids.size() - 1; i >= 0; i--) {
                if (curTime - this.mBgRecentForcePids.valueAt(i).mTimeStamp > MemoryConstant.MIN_INTERVAL_OP_TIMEOUT) {
                    this.mBgRecentForcePids.removeAt(i);
                    removeCount++;
                }
            }
        }
        AwareLog.d(TAG, "checkRecentForce removeCount: " + removeCount);
    }

    private void registerProcessObserver() {
        AwareCallback.getInstance().registerProcessObserver(this.mProcessObserver);
    }

    private void unregisterProcessObserver() {
        AwareCallback.getInstance().unregisterProcessObserver(this.mProcessObserver);
    }

    private LinkedHashMap<Integer, AwareAppLruBase> getActivityLruCache() {
        LinkedHashMap<Integer, AwareAppLruBase> lru = null;
        synchronized (this.mLruCache) {
            Map<Integer, AwareAppLruBase> tmp = this.mLruCache.snapshot();
            if (tmp instanceof LinkedHashMap) {
                lru = (LinkedHashMap) tmp;
            }
        }
        return lru;
    }

    private boolean updateActivityLruCache(int pid, int uid) {
        long timeNow = SystemClock.elapsedRealtime();
        synchronized (this.mLruCache) {
            if (this.mLruCache.size() == 0) {
                this.mLruCache.put(Integer.valueOf(uid), new AwareAppLruBase(pid, uid, timeNow));
                return false;
            }
            LinkedHashMap<Integer, AwareAppLruBase> lru = getActivityLruCache();
            if (lru == null) {
                return false;
            }
            List<Integer> list = new ArrayList<>(lru.keySet());
            if (list.isEmpty()) {
                return false;
            }
            int prevUid = list.get(list.size() - 1).intValue();
            AwareAppLruBase lruBase = lru.get(Integer.valueOf(prevUid));
            if (lruBase == null) {
                return false;
            }
            if (prevUid == uid) {
                this.mLruCache.put(Integer.valueOf(prevUid), new AwareAppLruBase(pid, prevUid, lruBase.inactiveTime));
                return false;
            }
            if (isSystemDialogProc(lruBase.procPid, prevUid, lruBase.inactiveTime, timeNow)) {
                this.mLruCache.remove(Integer.valueOf(prevUid));
            } else {
                this.mLruCache.put(Integer.valueOf(prevUid), new AwareAppLruBase(lruBase.procPid, prevUid, timeNow));
            }
            this.mLruCache.put(Integer.valueOf(uid), new AwareAppLruBase(pid, uid, timeNow));
            return true;
        }
    }

    private void updatePrevApp(int pid, int uid) {
        LinkedHashMap<Integer, AwareAppLruBase> lru;
        List<Integer> list;
        int listSize;
        if (!AwareAppAssociateUtils.isAppLock(uid) && updateActivityLruCache(pid, uid) && (lru = getActivityLruCache()) != null && (listSize = (list = new ArrayList<>(lru.keySet())).size()) >= 2) {
            int prevUid = list.get(listSize - 2).intValue();
            if (prevUid != this.mHomeProcessUid) {
                AwareAppLruBase.copyLruBaseInfo(lru.get(Integer.valueOf(prevUid)), this.mPrevNonHomeBase);
            } else if (listSize < 3) {
                this.mPrevNonHomeBase.setInitValue();
            } else {
                AwareAppLruBase.copyLruBaseInfo(lru.get(Integer.valueOf(list.get(listSize - 3).intValue())), this.mPrevNonHomeBase);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePreviousAppInfo(int pid, int uid, boolean foregroundActivities, SparseIntArray forePids) {
        if (this.mHwAms != null) {
            if (!foregroundActivities) {
                if (forePids != null) {
                    if (forePids.indexOfValue(uid) < 0 && pid != this.mHomeProcessPid) {
                        this.mRecentTaskPrevBase.setValue(pid, uid, SystemClock.elapsedRealtime());
                    }
                    for (int i = forePids.size() - 1; i >= 0; i--) {
                        int forePid = forePids.keyAt(i);
                        if (isForegroundPid(forePid)) {
                            updatePrevApp(forePid, forePids.valueAt(i));
                            return;
                        }
                    }
                }
            } else if (isForegroundPid(pid)) {
                updatePrevApp(pid, uid);
            }
        }
    }

    private boolean isSystemDialogProc(int pid, int uid, long prevActTime, long curTime) {
        if (UserHandleEx.getAppId(uid) != 1000) {
            return false;
        }
        synchronized (this.mLock) {
            AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(pid);
            if (br != null) {
                if (br.pkgList != null) {
                    if (br.pkgList.size() != 1) {
                        return false;
                    }
                    if (br.pkgList.contains(INTERNAL_APP_PKGNAME)) {
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }
    }

    private boolean isForegroundPid(int pid) {
        if (this.mHwAms.getProcessBaseInfo(pid).curAdj == HwActivityManagerService.FOREGROUND_APP_ADJ) {
            return true;
        }
        return false;
    }

    public static boolean isDealAsPkgUid(int uid) {
        int appId = UserHandleEx.getAppId(uid);
        return appId >= 1000 && appId <= 1001;
    }

    private AwareAppAssociate() {
        initHandler();
        this.mMorePreviousLevel = decideMorePreviousLevel();
    }

    public static AwareAppAssociate getInstance() {
        AwareAppAssociate awareAppAssociate;
        synchronized (LOCK) {
            if (sAwareAppAssociate == null) {
                sAwareAppAssociate = new AwareAppAssociate();
            }
            awareAppAssociate = sAwareAppAssociate;
        }
        return awareAppAssociate;
    }

    public void getVisibleWindowsInRestriction(SparseSet windowPids) {
        if (sEnabled && windowPids != null) {
            this.mAssociateWindow.getVisibleWindowsInRestriction(windowPids);
        }
    }

    public void getVisibleWindows(SparseSet windowPids, SparseSet evilPids) {
        if (sEnabled) {
            this.mAssociateWindow.getVisibleWindows(windowPids, evilPids);
            if (sRecord) {
                recordWindowDetail(windowPids);
            }
        }
    }

    public boolean isVisibleWindows(int userId, String pkg) {
        if (!sEnabled) {
            return true;
        }
        return this.mAssociateWindow.isVisibleWindows(userId, pkg);
    }

    public boolean hasWindow(int uid) {
        return this.mAssociateWindow.hasWindow(uid);
    }

    public boolean isEvilAlertWindow(int window, int code) {
        if (!sEnabled) {
            return false;
        }
        return this.mAssociateWindow.isEvilAlertWindow(window, code);
    }

    public Set<String> getWidgetsPkg() {
        return getWidgetsPkg(this.mCurUserId);
    }

    public Set<String> getWidgetsPkg(int userId) {
        if (!sEnabled) {
            return null;
        }
        return this.mAssociateWidget.getWidgetsPkg(userId);
    }

    public void getForeGroundApp(SparseSet forePids) {
        if (sEnabled && forePids != null) {
            synchronized (this.mForePids) {
                for (int i = this.mForePids.size() - 1; i >= 0; i--) {
                    forePids.add(this.mForePids.keyAt(i));
                }
            }
        }
    }

    public boolean isForeGroundApp(int uid) {
        boolean z = false;
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mForePids) {
            if (this.mForePids.indexOfValue(uid) >= 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean isRecentFgApp(int uid) {
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mBgRecentForcePids) {
            for (Map.Entry<Integer, ProcessData> map : this.mBgRecentForcePids.entrySet()) {
                ProcessData data = map.getValue();
                if (data != null && data.mUid == uid) {
                    return true;
                }
            }
            return false;
        }
    }

    public void getAssocListForPid(int pid, SparseSet strong) {
        if (sEnabled && pid > 0 && strong != null) {
            getStrongAssoc(pid, strong);
            if (sDebug) {
                AwareLog.i(TAG, "[" + pid + "]strongList:" + strong);
            }
            if (sRecord) {
                recordAssocDetail(pid);
            }
        }
    }

    public void getAssocClientListForPid(int pid, SparseSet strong) {
        if (sEnabled && pid > 0 && strong != null) {
            getStrongAssocClient(pid, strong);
            if (sDebug) {
                AwareLog.i(TAG, "[" + pid + "]strongList:" + strong);
            }
            if (sRecord) {
                recordAssocDetail(pid);
            }
        }
    }

    private void getStrongAssocClient(int pid, SparseSet strong) {
        if (pid > 0 && strong != null) {
            synchronized (this.mLock) {
                for (int k = this.mAssocRecordMap.size() - 1; k >= 0; k--) {
                    AwareAppAssociateUtils.getStrongAssocClientLocked(this.mAssocRecordMap.valueAt(k), this.mAssocRecordMap.keyAt(k), pid, strong);
                }
            }
        }
    }

    private boolean prepareReport(int eventId, Bundle bundleArgs) {
        if (!sEnabled) {
            if (sDebug) {
                AwareLog.d(TAG, "AwareAppAssociate feature disabled!");
            }
            return false;
        }
        if (sDebug) {
            AwareLog.d(TAG, "eventId: " + eventId);
        }
        if (bundleArgs == null) {
            return false;
        }
        if (this.mIsInitialized.get()) {
            return true;
        }
        initialize();
        return true;
    }

    private void reportHome(Bundle bundleArgs) {
        try {
            reportHome(bundleArgs.getInt(SceneRecogFeature.DATA_PID), bundleArgs.getInt("tgtUid"), bundleArgs.getStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME));
        } catch (ArrayIndexOutOfBoundsException e) {
            AwareLog.e(TAG, "getStringArrayList out of bounds exception!");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0055  */
    public void report(int eventId, Bundle bundleArgs) {
        if (prepareReport(eventId, bundleArgs)) {
            switch (eventId) {
                case 1:
                case 2:
                    addProcessRelation(bundleArgs, eventId);
                    return;
                case 3:
                    removeProcessRelation(bundleArgs, eventId);
                    return;
                case 4:
                    AwareAppAssociateUtils.report(eventId, bundleArgs);
                    return;
                case 5:
                case 6:
                case 7:
                    this.mAssociateWidget.report(eventId, bundleArgs);
                    return;
                case 8:
                case 9:
                    this.mAssociateWindow.report(eventId, bundleArgs);
                    return;
                case 10:
                    updateWindowOps(bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                    return;
                case 11:
                    reportHome(bundleArgs);
                    return;
                case NetManager.MSG_NET_GAME_ENABLE /* 12 */:
                    reportPrevInfo(bundleArgs.getInt(SceneRecogFeature.DATA_PID), bundleArgs.getInt("tgtUid"));
                    return;
                default:
                    switch (eventId) {
                        case 24:
                            break;
                        case 25:
                        case 26:
                            break;
                        case 27:
                            break;
                        default:
                            switch (eventId) {
                                case 30:
                                case 31:
                                case 33:
                                    break;
                                case 32:
                                    break;
                                default:
                                    if (sDebug) {
                                        AwareLog.e(TAG, "Unknown EventID: " + eventId);
                                        return;
                                    }
                                    return;
                            }
                    }
            }
        }
    }

    private void getStrongAssoc(int pid, SparseSet strong) {
        if (pid > 0 && strong != null) {
            synchronized (this.mLock) {
                AwareAppAssociateUtils.getStrongAssocLocked(this.mAssocRecordMap, pid, strong);
            }
        }
    }

    public void getAssocProvider(int pid, SparseSet assocProvider) {
        if (pid > 0 && assocProvider != null) {
            synchronized (this.mLock) {
                AwareAppAssociateUtils.getAssocProviderLocked(this.mAssocRecordMap, pid, assocProvider);
            }
        }
    }

    public boolean isWidgetVisible(Bundle options) {
        return this.mAssociateWidget.isWidgetVisible(options);
    }

    private void reportHome(int pid, int uid, ArrayList<String> pkgName) {
        this.mHomeProcessPid = pid;
        this.mHomeProcessUid = uid;
        synchronized (this.mHomePackageList) {
            this.mHomePackageList.clear();
            if (pkgName != null && pkgName.size() > 0) {
                this.mHomePackageList.addAll(pkgName);
            }
        }
    }

    private void updateWindowOps(String pkgName) {
        if (this.mMtmService != null) {
            if (sDebug) {
                AwareLog.d(TAG, "updateWindowOps pkg:" + pkgName);
            }
            if (pkgName == null) {
                this.mAssociateWindow.updateWindowOpsList(this.mMtmService);
                return;
            }
            synchronized (this.mLock) {
                this.mAssociateWindow.updateWindowOps(pkgName, this.mMtmService, this.mProcPidMap);
            }
        }
    }

    private void reportPrevInfo(int pid, int uid) {
        this.mAmsPrevBase.setValue(pid, uid, SystemClock.elapsedRealtime());
    }

    public List<String> getDefaultHomePackages() {
        ArrayList<String> pkgs = new ArrayList<>();
        synchronized (this.mHomePackageList) {
            pkgs.addAll(this.mHomePackageList);
        }
        return pkgs;
    }

    public int getCurHomeProcessPid() {
        return this.mHomeProcessPid;
    }

    public int getCurHomeProcessUid() {
        return this.mHomeProcessUid;
    }

    public AwareAppLruBase getRecentTaskPrevInfo() {
        return new AwareAppLruBase(this.mRecentTaskPrevBase.procPid, this.mRecentTaskPrevBase.procUid, this.mRecentTaskPrevBase.inactiveTime);
    }

    public AwareAppLruBase getPreviousAppInfo() {
        return new AwareAppLruBase(this.mPrevNonHomeBase.procPid, this.mPrevNonHomeBase.procUid, this.mPrevNonHomeBase.inactiveTime);
    }

    public AwareAppLruBase getPreviousByAmsInfo() {
        return new AwareAppLruBase(this.mAmsPrevBase.procPid, this.mAmsPrevBase.procUid, this.mAmsPrevBase.inactiveTime);
    }

    private void addProcessRelationLocked(Bundle bundleArgs, String targetName, String callerName, String compName, int type) {
        int targetPid = 0;
        int callerPid = bundleArgs.getInt("callPid");
        int callerUid = bundleArgs.getInt("callUid");
        int targetUid = bundleArgs.getInt("tgtUid");
        int hwFlag = bundleArgs.getInt("hwFlag");
        AwareAppAssociateUtils.AssocBaseRecord br = (AwareAppAssociateUtils.AssocBaseRecord) this.mProcInfoMap.get(targetName, targetUid);
        if (br != null) {
            targetPid = br.pid;
        }
        AwareAppAssociateUtils.AssocPidRecord pidRecord = this.mAssocRecordMap.get(callerPid);
        if (pidRecord == null) {
            AwareAppAssociateUtils.AssocPidRecord pidRecord2 = new AwareAppAssociateUtils.AssocPidRecord(callerPid, callerUid, callerName);
            AwareAppAssociateUtils.AssocBaseRecord baseRecord = new AwareAppAssociateUtils.AssocBaseRecord(targetName, targetUid, targetPid);
            baseRecord.components.add(compName);
            addAppCompJobLock(baseRecord, compName, hwFlag);
            ProcessMap<AwareAppAssociateUtils.AssocBaseRecord> relations = pidRecord2.getMap(type).orElse(null);
            if (relations != null) {
                relations.put(targetName, targetUid, baseRecord);
                this.mAssocRecordMap.put(callerPid, pidRecord2);
            } else if (sDebug) {
                AwareLog.e(TAG, "Error type:" + type);
            }
        } else {
            ProcessMap<AwareAppAssociateUtils.AssocBaseRecord> relations2 = pidRecord.getMap(type).orElse(null);
            if (relations2 != null) {
                AwareAppAssociateUtils.AssocBaseRecord baseRecord2 = (AwareAppAssociateUtils.AssocBaseRecord) relations2.get(targetName, targetUid);
                if (baseRecord2 == null) {
                    AwareAppAssociateUtils.AssocBaseRecord baseRecord3 = new AwareAppAssociateUtils.AssocBaseRecord(targetName, targetUid, targetPid);
                    baseRecord3.components.add(compName);
                    addAppCompJobLock(baseRecord3, compName, hwFlag);
                    relations2.put(targetName, targetUid, baseRecord3);
                    return;
                }
                baseRecord2.miniTime = SystemClock.elapsedRealtime();
                baseRecord2.isStrong = true;
                baseRecord2.components.add(compName);
                addAppCompJobLock(baseRecord2, compName, hwFlag);
            } else if (sDebug) {
                AwareLog.e(TAG, "Error type:" + type);
            }
        }
    }

    private void addProcessRelation(Bundle bundleArgs, int type) {
        String compName;
        int callerPid = bundleArgs.getInt("callPid");
        int callerUid = bundleArgs.getInt("callUid");
        int targetUid = bundleArgs.getInt("tgtUid");
        if (AwareAppAssociateUtils.checkProcessRelationParams(callerUid, targetUid, callerPid, type)) {
            String targetName = bundleArgs.getString("tgtProcName");
            String callerName = bundleArgs.getString("callProcName");
            if (callerName != null && targetName != null) {
                String compName2 = bundleArgs.getString("compName");
                if (compName2 == null) {
                    compName = "NULL";
                } else {
                    compName = compName2;
                }
                if (sDebug) {
                    AwareLog.i(TAG, AwareAppAssociateUtils.typeToString(type) + ". Caller[Pid:" + callerPid + "][Uid:" + callerUid + "][Name:" + callerName + "] Target[Uid:" + targetUid + "][pName:" + targetName + "][hash:" + compName + "]");
                }
                if (targetUid != 1000 || !SYSTEM.equals(targetName)) {
                    synchronized (this.mLock) {
                        addProcessRelationLocked(bundleArgs, targetName, callerName, compName, type);
                    }
                }
            } else if (sDebug) {
                AwareLog.i(TAG, AwareAppAssociateUtils.typeToString(type) + " with wrong callerName or targetName");
            }
        }
    }

    private void removeProcessRelationLocked(Bundle bundleArgs, int type) {
        int callerPid = bundleArgs.getInt("callPid");
        String compName = bundleArgs.getString("compName");
        if (compName == null) {
            compName = "NULL";
        }
        AwareAppAssociateUtils.AssocPidRecord pr = this.mAssocRecordMap.get(callerPid);
        if (pr != null) {
            ProcessMap<AwareAppAssociateUtils.AssocBaseRecord> relations = pr.getMap(type).orElse(null);
            if (relations != null) {
                int targetUid = bundleArgs.getInt("tgtUid");
                String targetName = bundleArgs.getString("tgtProcName");
                AwareAppAssociateUtils.AssocBaseRecord br = (AwareAppAssociateUtils.AssocBaseRecord) relations.get(targetName, targetUid);
                int hwFlag = bundleArgs.getInt("hwFlag");
                if (br != null && br.components.contains(compName)) {
                    br.components.remove(compName);
                    removeAppCompJobLock(br, compName, hwFlag);
                    if (br.components.isEmpty()) {
                        relations.remove(targetName, targetUid);
                        if (pr.isEmpty()) {
                            this.mAssocRecordMap.remove(pr.pid);
                        }
                    }
                }
            } else if (sDebug) {
                AwareLog.e(TAG, "Error type:" + type);
            }
        }
    }

    private void removeProcessRelation(Bundle bundleArgs, int type) {
        int callerPid = bundleArgs.getInt("callPid");
        int callerUid = bundleArgs.getInt("callUid");
        int targetUid = bundleArgs.getInt("tgtUid");
        String targetName = bundleArgs.getString("tgtProcName");
        String compName = bundleArgs.getString("compName");
        if (AwareAppAssociateUtils.checkProcessRelationParams(callerUid, targetUid, callerPid, type)) {
            if (targetName != null) {
                if (sDebug) {
                    AwareLog.i(TAG, AwareAppAssociateUtils.typeToString(type) + ". Caller[Pid:" + callerPid + "] target[" + targetUid + ":" + targetName + ":" + compName + "]");
                }
                synchronized (this.mLock) {
                    removeProcessRelationLocked(bundleArgs, type);
                }
            } else if (sDebug) {
                AwareLog.i(TAG, AwareAppAssociateUtils.typeToString(type) + " with wrong targetName");
            }
        }
    }

    private void removeDiedProcessRelationLockedInner(String pkg, int pid) {
        SparseSet pids = this.mProcPkgMap.get(pkg);
        if (pids != null && pids.contains(pid)) {
            pids.remove(pid);
            if (pids.isEmpty()) {
                this.mProcPkgMap.remove(pkg);
            }
        }
    }

    private void removeDiedProcessRelationLocked(AwareAppAssociateUtils.AssocBaseRecord br, int pid) {
        Iterator<String> it = br.pkgList.iterator();
        while (it.hasNext()) {
            String pkg = it.next();
            synchronized (this.mProcPkgMap) {
                removeDiedProcessRelationLockedInner(pkg, pid);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeDiedProcessRelation(int pid, int uid) {
        if (pid > 0 && uid > 0) {
            if (sDebug) {
                AwareLog.i(TAG, "remove died. Pid:" + pid + " Uid:" + uid);
            }
            AwareAppAssociateUtils.AssocBaseRecord br = null;
            synchronized (this.mLock) {
                Object obj = new SparseArrayEx(this.mProcPidMap).removeReturnOld(pid);
                if (obj != null && (obj instanceof AwareAppAssociateUtils.AssocBaseRecord)) {
                    br = (AwareAppAssociateUtils.AssocBaseRecord) obj;
                    this.mProcInfoMap.remove(br.processName, br.uid);
                    if (br.pkgList != null) {
                        removeDiedProcessRelationLocked(br, pid);
                    }
                }
                SparseSet pids = this.mProcUidMap.get(uid);
                if (pids != null && pids.contains(pid)) {
                    pids.remove(pid);
                    if (pids.isEmpty()) {
                        this.mProcUidMap.remove(uid);
                    }
                }
                for (int k = this.mAssocRecordMap.size() - 1; k >= 0; k--) {
                    AwareAppAssociateUtils.AssocPidRecord record = this.mAssocRecordMap.valueAt(k);
                    if (record.pid == pid) {
                        this.mAssocRecordMap.removeAt(k);
                    } else {
                        if (br != null) {
                            record.assocBindService.remove(br.processName, br.uid);
                            record.assocProvider.remove(br.processName, br.uid);
                        }
                        if (record.isEmpty()) {
                            this.mAssocRecordMap.removeAt(k);
                        }
                    }
                }
            }
        } else if (sDebug) {
            AwareLog.i(TAG, "removeDiedProcessRelation with wrong pid or uid");
        }
    }

    /* access modifiers changed from: private */
    public static class LaunchData {
        private long mFirstTime;
        private int mLaunchTimes;

        private LaunchData(int launchTimes, long firstTime) {
            this.mLaunchTimes = launchTimes;
            this.mFirstTime = firstTime;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private LaunchData increase() {
            this.mLaunchTimes++;
            return this;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private long getFirstTime() {
            return this.mFirstTime;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getLaunchTimes() {
            return this.mLaunchTimes;
        }
    }

    private void updateProcLaunchDataLocked(Map<String, Map<String, LaunchData>> pkgMap, String proc, String pkg, int userId) {
        Map<String, LaunchData> procMap = pkgMap.get(pkg);
        if (procMap == null) {
            procMap = new HashMap();
            pkgMap.put(pkg, procMap);
        }
        LaunchData launchData = procMap.get(proc);
        if (launchData != null) {
            procMap.put(proc, launchData.increase());
            if (sDebug) {
                AwareLog.i(TAG, "updateProcLaunchData, pkg: " + pkg + ", launchTimes: " + launchData.getLaunchTimes());
            }
            if (launchData.getLaunchTimes() >= 30) {
                if (SystemClock.elapsedRealtime() - launchData.getFirstTime() <= 300000) {
                    Map<String, String> cleanMsg = new HashMap<>();
                    cleanMsg.put("proc", proc);
                    cleanMsg.put("pkg", pkg);
                    cleanMsg.put("userId", "" + userId);
                    Message msg = this.mHandler.obtainMessage();
                    msg.what = 2;
                    msg.obj = cleanMsg;
                    this.mHandler.sendMessage(msg);
                }
                pkgMap.remove(pkg);
                return;
            }
            return;
        }
        LaunchData launchData2 = new LaunchData(1, SystemClock.elapsedRealtime());
        procMap.put(proc, launchData2);
        if (sDebug) {
            AwareLog.i(TAG, "updateProcLaunchData, pkg: " + pkg + ", launchTimes: " + launchData2.getLaunchTimes());
        }
    }

    private void updateProcLaunchData(int uid, String proc, ArrayList<String> pkgList) {
        if (AppCleanupFeature.isAppCleanEnable() && UserHandleEx.getAppId(uid) >= 10000 && !UserHandleEx.isIsolated(uid)) {
            if (sDebug) {
                AwareLog.i(TAG, "updateProcLaunchData, proc: " + proc + ", uid: " + uid + ", pkgList: " + pkgList);
            }
            synchronized (this.mProcLaunchMap) {
                int userId = UserHandleEx.getUserId(uid);
                Map<String, Map<String, LaunchData>> pkgMap = this.mProcLaunchMap.get(Integer.valueOf(userId));
                if (pkgMap == null) {
                    pkgMap = new HashMap();
                    this.mProcLaunchMap.put(Integer.valueOf(userId), pkgMap);
                }
                Iterator<String> it = pkgList.iterator();
                while (it.hasNext()) {
                    String pkg = it.next();
                    if (pkg != null) {
                        updateProcLaunchDataLocked(pkgMap, proc, pkg, userId);
                    }
                }
            }
        }
    }

    private void updateProcessRelationLocked(int pid, int uid, String name, ArrayList<String> pkgList) {
        AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(pid);
        if (br == null) {
            AwareAppAssociateUtils.AssocBaseRecord br2 = new AwareAppAssociateUtils.AssocBaseRecord(name, uid, pid);
            br2.pkgList.addAll(pkgList);
            this.mProcPidMap.put(pid, br2);
        } else {
            br.processName = name;
            br.uid = uid;
            br.pid = pid;
            br.pkgList.addAll(pkgList);
        }
        AwareAppAssociateUtils.AssocBaseRecord br3 = (AwareAppAssociateUtils.AssocBaseRecord) this.mProcInfoMap.get(name, uid);
        if (br3 == null) {
            this.mProcInfoMap.put(name, uid, new AwareAppAssociateUtils.AssocBaseRecord(name, uid, pid));
        } else {
            br3.pid = pid;
        }
        SparseSet pids = this.mProcUidMap.get(uid);
        if (pids == null) {
            SparseSet pids2 = new SparseSet();
            pids2.add(pid);
            this.mProcUidMap.put(uid, pids2);
        } else {
            pids.add(pid);
        }
        int listSize = pkgList.size();
        for (int i = 0; i < listSize; i++) {
            String pkg = pkgList.get(i);
            synchronized (this.mProcPkgMap) {
                SparseSet pids3 = this.mProcPkgMap.get(pkg);
                if (pids3 == null) {
                    SparseSet pids4 = new SparseSet();
                    pids4.add(pid);
                    this.mProcPkgMap.put(pkg, pids4);
                } else {
                    pids3.add(pid);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateProcessRelation(int pid, int uid, String name, ArrayList<String> pkgList) {
        if (pid <= 0 || uid <= 0) {
            if (sDebug) {
                AwareLog.i(TAG, "updateProcessRelation with wrong pid or uid");
            }
        } else if (name != null && pkgList != null) {
            if (sDebug) {
                AwareLog.i(TAG, "update relation. Pid:" + pid + " Uid:" + uid + ",ProcessName:" + name);
            }
            updateProcLaunchData(uid, name, pkgList);
            synchronized (this.mLock) {
                for (int k = this.mAssocRecordMap.size() - 1; k >= 0; k--) {
                    AwareAppAssociateUtils.AssocPidRecord record = this.mAssocRecordMap.valueAt(k);
                    if (record.pid == pid) {
                        this.mAssocRecordMap.removeAt(k);
                    } else {
                        AwareAppAssociateUtils.AssocBaseRecord br = (AwareAppAssociateUtils.AssocBaseRecord) record.assocBindService.get(name, uid);
                        if (br != null) {
                            br.pid = pid;
                        }
                        AwareAppAssociateUtils.AssocBaseRecord br2 = (AwareAppAssociateUtils.AssocBaseRecord) record.assocProvider.get(name, uid);
                        if (br2 != null) {
                            br2.pid = pid;
                        }
                    }
                }
                updateProcessRelationLocked(pid, uid, name, pkgList);
            }
        } else if (sDebug) {
            AwareLog.i(TAG, "updateProcessRelation with wrong name");
        }
    }

    public int getCurUserId() {
        return this.mCurUserId;
    }

    public int getCurSwitchUser() {
        return this.mCurSwitchUser;
    }

    private void initSwitchUser() {
        try {
            UserInfoExAdapter currentUser = ActivityManagerNativeExt.getCurrentUser();
            if (currentUser != null) {
                this.mAssociateWidget.checkAndInitWidgetObj(currentUser.getUserId());
                this.mCurUserId = currentUser.getUserId();
                this.mCurSwitchUser = currentUser.getUserId();
            }
            AwareCallback.getInstance().registerUserSwitchObserver(this.mUserSwitchObserver);
        } catch (RemoteException e) {
            AwareLog.d(TAG, "Activity manager not running, initSwitchUser error!");
        }
    }

    private void deInitSwitchUser() {
        AwareCallback.getInstance().unregisterUserSwitchObserver(this.mUserSwitchObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initialize() {
        if (!this.mIsInitialized.get()) {
            if (this.mMtmService == null) {
                this.mMtmService = MultiTaskManagerService.self();
            }
            if (!isUserUnlocked()) {
                if (this.mHandler.hasMessages(1)) {
                    this.mHandler.removeMessages(1);
                }
                this.mHandler.sendEmptyMessageDelayed(1, REINIT_TIME);
                return;
            }
            MultiTaskManagerService multiTaskManagerService = this.mMtmService;
            if (multiTaskManagerService != null) {
                if (multiTaskManagerService.context() != null) {
                    this.mSystemUnremoveUidCache = SystemUnremoveUidCache.getInstance(this.mMtmService.context());
                }
                initAssoc();
                registerProcessObserver();
                this.mIsInitialized.set(true);
            } else if (sDebug) {
                AwareLog.w(TAG, "MultiTaskManagerService has not been started.");
            }
            FloatBallAssociate.getInstance().init();
        }
    }

    private boolean isUserUnlocked() {
        MultiTaskManagerService multiTaskManagerService = this.mMtmService;
        if (multiTaskManagerService == null) {
            return false;
        }
        Object obj = multiTaskManagerService.context().getSystemService("user");
        if (obj instanceof UserManager) {
            return ((UserManager) obj).isUserUnlocked();
        }
        return false;
    }

    private void deInitialize() {
        synchronized (LOCK) {
            if (this.mIsInitialized.get()) {
                unregisterProcessObserver();
                if (this.mMtmService != null) {
                    this.mMtmService = null;
                }
                HwActivityManager.reportAssocDisable();
                this.mAssociateWindow.deinitVisibleWindows();
                this.mAssociateWidget.clearWidget();
                deinitAssoc();
                this.mIsInitialized.set(false);
                FloatBallAssociate.getInstance().deInit();
            }
        }
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHandler = new AppAssocHandler(looper);
        } else {
            this.mHandler = new AppAssocHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public class AppAssocHandler extends Handler {
        public AppAssocHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (AwareAppAssociate.sDebug) {
                AwareLog.e(AwareAppAssociate.TAG, "handleMessage message " + msg.what);
            }
            int i = msg.what;
            if (i == 1) {
                AwareAppAssociate.this.initialize();
            } else if (i != 2) {
                if (i == 3) {
                    AwareAppAssociate.this.checkRecentForce();
                } else if (i == 4) {
                    AwareAppAssociate.this.clearRemoveVisWinDurScreenOff();
                }
            } else if (msg.obj instanceof HashMap) {
                HashMap<String, String> cleanMsg = (HashMap) msg.obj;
                String pkg = cleanMsg.get("pkg");
                String proc = cleanMsg.get("proc");
                try {
                    int userId = Integer.parseInt(cleanMsg.get("userId"));
                    if (userId < 0 || AwareAppAssociate.this.mMtmService == null) {
                        AwareLog.e(AwareAppAssociate.TAG, "MSG_CLEAN, userId or mMtmService error!");
                        return;
                    }
                    CrashClean crashClean = new CrashClean(userId, 0, pkg, AwareAppAssociate.this.mMtmService.context());
                    if (AwareAppAssociate.sDebug) {
                        AwareLog.i(AwareAppAssociate.TAG, "Pkg:" + pkg + " will be cleaned due to high-freq-restart of proc:" + proc);
                    }
                    crashClean.clean();
                } catch (NumberFormatException e) {
                    AwareLog.e(AwareAppAssociate.TAG, "MSG_CLEAN, userId format error!");
                }
            }
        }
    }

    private void initAssoc() {
        if (this.mHwAms != null) {
            synchronized (this.mLock) {
                AwareAppAssociateUtils.AssocBaseRecord br = new AwareAppAssociateUtils.AssocBaseRecord(SYSTEM, 1000, this.mMyPid);
                this.mProcPidMap.put(this.mMyPid, br);
                this.mProcInfoMap.put(SYSTEM, 1000, br);
                SparseSet pids = new SparseSet();
                pids.add(this.mMyPid);
                this.mProcUidMap.put(1000, pids);
            }
            initSwitchUser();
            this.mAssociateWindow.initVisibleWindows();
            this.mAssociateWidget.updateWidgets(this.mCurUserId);
            if (this.mCurUserId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(this.mCurUserId), null);
            }
            ArrayMap<Integer, Integer> forePids = new ArrayMap<>();
            this.mHwAms.reportAssocEnable(forePids);
            AwareIntelligentRecg.getInstance().onFgPidInfosInit(forePids);
            synchronized (this.mForePids) {
                this.mForePids.clear();
                addAllForSparseIntArray(forePids, this.mForePids);
            }
            synchronized (this.mBgRecentForcePids) {
                this.mBgRecentForcePids.clear();
            }
        }
    }

    private void deinitAssoc() {
        synchronized (this.mForePids) {
            this.mForePids.clear();
        }
        synchronized (this.mBgRecentForcePids) {
            this.mBgRecentForcePids.clear();
        }
        synchronized (this.mLock) {
            this.mAssocRecordMap.clear();
            this.mProcInfoMap.getMap().clear();
            this.mProcPidMap.clear();
            this.mProcUidMap.clear();
            synchronized (this.mProcPkgMap) {
                this.mProcPkgMap.clear();
            }
        }
        deInitSwitchUser();
    }

    public void getPidsByUid(int uid, SparseSet pids) {
        if (sEnabled && uid > 0 && pids != null) {
            synchronized (this.mLock) {
                SparseSet procPids = this.mProcUidMap.get(uid);
                if (procPids != null) {
                    pids.addAll(procPids);
                }
            }
        }
    }

    public int getPidByNameAndUid(String procName, int uid) {
        if (!sEnabled || uid <= 0 || procName == null || procName.isEmpty()) {
            return -1;
        }
        synchronized (this.mLock) {
            AwareAppAssociateUtils.AssocBaseRecord br = (AwareAppAssociateUtils.AssocBaseRecord) this.mProcInfoMap.get(procName, uid);
            if (br == null) {
                return -1;
            }
            return br.pid;
        }
    }

    private void sameUidInner(SparseSet pids, int pid, boolean flagIn, StringBuilder sb) {
        boolean flag = flagIn;
        for (int i = pids.size() - 1; i >= 0; i--) {
            int tmp = pids.keyAt(i);
            if (tmp != pid) {
                if (flag) {
                    sb.append("    [SameUID] depend on");
                    sb.append(System.lineSeparator());
                    flag = false;
                }
                AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(tmp);
                if (br != null) {
                    sb.append("        Pid:");
                    sb.append(br.pid);
                    sb.append(",Uid:");
                    sb.append(br.uid);
                    sb.append(",ProcessName:");
                    sb.append(br.processName);
                    sb.append(System.lineSeparator());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Optional<String> sameUid(int pid) {
        StringBuilder sb = new StringBuilder();
        synchronized (this.mLock) {
            AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(pid);
            if (br == null) {
                return Optional.empty();
            }
            SparseSet pids = this.mProcUidMap.get(br.uid);
            if (pids == null) {
                return Optional.empty();
            }
            sameUidInner(pids, pid, true, sb);
            return Optional.ofNullable(sb.toString());
        }
    }

    private Set<String> getPackageNameForUid(int uid, int pidForUid) {
        ArraySet<String> pkgList = new ArraySet<>();
        synchronized (this.mLock) {
            if (pidForUid != 0) {
                AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(pidForUid);
                if (!(br == null || br.pkgList == null)) {
                    pkgList.addAll(br.pkgList);
                }
                return pkgList;
            }
            SparseSet pids = this.mProcUidMap.get(uid);
            if (pids != null) {
                if (!pids.isEmpty()) {
                    for (int i = pids.size() - 1; i >= 0; i--) {
                        AwareAppAssociateUtils.AssocBaseRecord br2 = this.mProcPidMap.get(pids.keyAt(i));
                        if (!(br2 == null || br2.pkgList == null)) {
                            pkgList.addAll(br2.pkgList);
                        }
                    }
                    return pkgList;
                }
            }
            return pkgList;
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            synchronized (this.mLock) {
                int listSize = this.mAssocRecordMap.size();
                for (int s = 0; s < listSize; s++) {
                    AwareAppAssociateUtils.AssocPidRecord record = this.mAssocRecordMap.valueAt(s);
                    if (record != null) {
                        pw.println(record);
                    }
                }
            }
            dumpWidget(pw);
            dumpVisibleWindow(pw);
            pw.println("[mIsPreviousHighEnable] : " + this.mIsPreviousHighEnable);
        }
    }

    public void dumpFore(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            SparseSet tmp = new SparseSet();
            synchronized (this.mForePids) {
                for (int i = this.mForePids.size() - 1; i >= 0; i--) {
                    tmp.add(this.mForePids.keyAt(i));
                }
            }
            for (int j = tmp.size() - 1; j >= 0; j--) {
                dumpPid(tmp.keyAt(j), pw);
            }
        }
    }

    public void dumpRecentFore(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            SparseSet tmp = new SparseSet();
            synchronized (this.mBgRecentForcePids) {
                tmp.addAll(this.mBgRecentForcePids.keySet());
            }
            for (int i = tmp.size() - 1; i >= 0; i--) {
                dumpPid(tmp.keyAt(i), pw);
            }
        }
    }

    private void dumpPkgProcLocked(PrintWriter pw, Map<String, Map<String, LaunchData>> pkgMap) {
        for (Map.Entry<String, Map<String, LaunchData>> pkgEntry : pkgMap.entrySet()) {
            pw.println("    pkg: " + pkgEntry.getKey());
            Map<String, LaunchData> procMap = pkgEntry.getValue();
            if (procMap != null) {
                for (Map.Entry<String, LaunchData> procEntry : procMap.entrySet()) {
                    LaunchData launchData = procEntry.getValue();
                    if (launchData != null) {
                        pw.println("      proc: " + procEntry.getKey() + ", launchTime: " + launchData.getLaunchTimes());
                    }
                }
            }
        }
    }

    public void dumpPkgProc(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            synchronized (this.mProcPkgMap) {
                for (String pkg : this.mProcPkgMap.keySet()) {
                    pw.println(pkg + ":" + this.mProcPkgMap.get(pkg));
                }
            }
            pw.println("proc launch data:");
            synchronized (this.mProcLaunchMap) {
                for (Map.Entry<Integer, Map<String, Map<String, LaunchData>>> uidEntry : this.mProcLaunchMap.entrySet()) {
                    pw.println("  userId: " + uidEntry.getKey());
                    Map<String, Map<String, LaunchData>> pkgMap = uidEntry.getValue();
                    if (pkgMap != null) {
                        dumpPkgProcLocked(pw, pkgMap);
                    }
                }
            }
        }
    }

    public void dumpPid(int pid, PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            synchronized (this.mLock) {
                AwareAppAssociateUtils.AssocPidRecord record = this.mAssocRecordMap.get(pid);
                if (record != null) {
                    pw.println(record);
                } else {
                    AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(pid);
                    if (br != null) {
                        pw.println("Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                    }
                    pw.println(sameUid(pid).orElse(null));
                }
            }
        }
    }

    public void dumpVisibleWindow(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            SparseSet windows = new SparseSet();
            SparseSet windowsEvil = new SparseSet();
            getVisibleWindows(windows, windowsEvil);
            boolean flag = true;
            pw.println("");
            synchronized (this.mLock) {
                for (int i = windows.size() - 1; i >= 0; i--) {
                    AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(windows.keyAt(i));
                    if (br != null) {
                        if (flag) {
                            pw.println("[WindowList] :");
                            flag = false;
                        }
                        pw.println("    Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName + ",PkgList:" + br.pkgList);
                    }
                }
                boolean flag2 = true;
                for (int i2 = windowsEvil.size() - 1; i2 >= 0; i2--) {
                    AwareAppAssociateUtils.AssocBaseRecord br2 = this.mProcPidMap.get(windowsEvil.keyAt(i2));
                    if (br2 != null) {
                        if (flag2) {
                            pw.println("[WindowEvilList] :");
                            flag2 = false;
                        }
                        pw.println("    Pid:" + br2.pid + ",Uid:" + br2.uid + ",ProcessName:" + br2.processName + ",PkgList:" + br2.pkgList);
                    }
                }
            }
            SparseSet windowsClean = new SparseSet();
            getVisibleWindowsInRestriction(windowsClean);
            pw.println("[WindowList in restriction] :" + windowsClean);
        }
    }

    public void dumpWidget(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            Set<String> widgets = getWidgetsPkg();
            pw.println("[Widgets] : " + widgets.size());
            Iterator<String> it = widgets.iterator();
            while (it.hasNext()) {
                pw.println("    " + it.next());
            }
        }
    }

    public void dumpHome(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            synchronized (this.mLock) {
                AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(this.mHomeProcessPid);
                if (br != null) {
                    pw.println("[Home]Pid:" + this.mHomeProcessPid + ",Uid:" + br.uid + ",ProcessName:" + br.processName + ",pkg:" + br.pkgList);
                }
            }
        }
    }

    public void dumpPrev(PrintWriter pw) {
        String eclipseTime;
        String eclipseTime2;
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            int pid = 0;
            Set<String> pkgList = getPackageNameForUid(this.mPrevNonHomeBase.procUid, isDealAsPkgUid(this.mPrevNonHomeBase.procUid) ? this.mPrevNonHomeBase.procPid : 0);
            if (this.mPrevNonHomeBase.procUid == 0) {
                eclipseTime = " none";
            } else {
                eclipseTime = " " + ((SystemClock.elapsedRealtime() - this.mPrevNonHomeBase.inactiveTime) / 1000);
            }
            pw.println("[Prev Non Home] Uid:" + this.mPrevNonHomeBase.procUid + ",pid:" + this.mPrevNonHomeBase.procUid + ",pkg:" + pkgList + ",eclipse(s):" + eclipseTime);
            boolean isRecentTaskShow = false;
            synchronized (this.mForePids) {
                if (this.mForePids.size() == 0) {
                    isRecentTaskShow = true;
                }
            }
            if (isRecentTaskShow) {
                pw.println("[Prev Recent Task] Uid:" + this.mRecentTaskPrevBase.procUid + ",pid:" + this.mRecentTaskPrevBase.procPid + ",pkg:" + getPackageNameForUid(this.mRecentTaskPrevBase.procUid, isDealAsPkgUid(this.mRecentTaskPrevBase.procUid) ? this.mRecentTaskPrevBase.procPid : 0));
            } else {
                pw.println("[Prev Recent Task] Uid: None");
            }
            if (isDealAsPkgUid(this.mAmsPrevBase.procUid)) {
                pid = this.mAmsPrevBase.procPid;
            }
            Set<String> pkgList2 = getPackageNameForUid(this.mAmsPrevBase.procUid, pid);
            if (this.mAmsPrevBase.procUid == 0) {
                eclipseTime2 = " none";
            } else {
                eclipseTime2 = " " + ((SystemClock.elapsedRealtime() - this.mAmsPrevBase.inactiveTime) / 1000);
            }
            pw.println("[Prev By Ams] Uid:" + this.mAmsPrevBase.procUid + ",pid:" + this.mAmsPrevBase.procPid + ",pkg:" + pkgList2 + ",eclipse(s):" + eclipseTime2);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0057: APUT  (r0v1 'results' int[] A[D('results' int[])]), (0 ??[int, short, byte, char]), (r2v1 'size' int A[D('size' int)]) */
    private int[] dumpRecordLocked(AwareAppAssociateUtils.AssocPidRecord record, boolean isService) {
        int np;
        SparseArray<AwareAppAssociateUtils.AssocBaseRecord> brs;
        int[] results = new int[2];
        if (isService) {
            np = record.assocBindService.getMap().size();
        } else {
            np = record.assocProvider.getMap().size();
        }
        int size = 0;
        int sizeAll = 0;
        for (int i = 0; i < np; i++) {
            if (isService) {
                brs = (SparseArray) record.assocBindService.getMap().valueAt(i);
            } else {
                brs = (SparseArray) record.assocProvider.getMap().valueAt(i);
            }
            int nb = brs.size();
            size += nb;
            for (int j = 0; j < nb; j++) {
                sizeAll += brs.valueAt(j).components.size();
            }
        }
        results[0] = size;
        results[1] = sizeAll;
        return results;
    }

    /* JADX INFO: Multiple debug info for r1v8 int: [D('pids' com.android.server.mtm.utils.SparseSet), D('curPidSize' int)] */
    /* JADX INFO: Multiple debug info for r7v4 int: [D('windows' com.android.server.mtm.utils.SparseSet), D('curCompSize' int)] */
    /* JADX INFO: Multiple debug info for r8v2 int: [D('windowsEvil' com.android.server.mtm.utils.SparseSet), D('curPidUidSize' int)] */
    public void dumpRecord(PrintWriter pw) {
        Throwable th;
        AwareAppAssociate awareAppAssociate = this;
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            int pidSizeTmp = 0;
            int compSize = 0;
            Set<String> widgets = getWidgetsPkg();
            pw.println("Widget Size: " + widgets.size());
            SparseSet windows = new SparseSet();
            SparseSet windowsEvil = new SparseSet();
            awareAppAssociate.getVisibleWindows(windows, windowsEvil);
            pw.println("Window Size: " + windows.size() + ", EvilWindow Size: " + windowsEvil.size());
            synchronized (awareAppAssociate.mLock) {
                try {
                    int pidSize = awareAppAssociate.mAssocRecordMap.size();
                    int s = 0;
                    while (s < pidSize) {
                        AwareAppAssociateUtils.AssocPidRecord record = awareAppAssociate.mAssocRecordMap.valueAt(s);
                        int[] ret = awareAppAssociate.dumpRecordLocked(record, true);
                        int bindSize = ret[0];
                        int bindSizeAll = ret[1];
                        int[] ret2 = awareAppAssociate.dumpRecordLocked(record, false);
                        int providerSize = ret2[0];
                        int providerSizeAll = ret2[1];
                        SparseSet pids = awareAppAssociate.mProcUidMap.get(record.uid);
                        int sameUid = 0;
                        if (pids != null) {
                            try {
                                sameUid = pids.size() - 1;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        int curPidSize = bindSize + providerSize;
                        int curCompSize = bindSizeAll + providerSizeAll;
                        int curPidUidSize = curPidSize + sameUid;
                        int pidSizeTmp2 = pidSizeTmp + curPidSize;
                        int compSize2 = compSize + curCompSize;
                        try {
                            StringBuilder sb = new StringBuilder();
                            try {
                                sb.append("[");
                                sb.append(record.uid);
                                sb.append("][");
                                sb.append(record.processName);
                                sb.append("]: bind[");
                                sb.append(bindSize);
                                sb.append("-");
                                sb.append(bindSizeAll);
                                sb.append("]provider[");
                                sb.append(providerSize);
                                sb.append("-");
                                sb.append(providerSizeAll);
                                sb.append("]SameUID[");
                                sb.append(sameUid);
                                sb.append("]pids:[");
                                sb.append(curPidSize);
                                sb.append("]comps:[");
                                sb.append(curCompSize);
                                sb.append("]piduids:[");
                                sb.append(curPidUidSize);
                                sb.append("]");
                                pw.println(sb.toString());
                                s++;
                                awareAppAssociate = this;
                                windows = windows;
                                widgets = widgets;
                                windowsEvil = windowsEvil;
                                pidSizeTmp = pidSizeTmp2;
                                compSize = compSize2;
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            throw th;
                        }
                    }
                    try {
                        pw.println("PidRecord Size: " + pidSize + " " + pidSizeTmp + " " + compSize);
                    } catch (Throwable th5) {
                        th = th5;
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    throw th;
                }
            }
        }
    }

    private void recordWindowDetail(SparseSet list) {
        if (!(list == null || list.isEmpty())) {
            synchronized (this.mLock) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(list.keyAt(i));
                    if (br != null) {
                        AwareLog.i(TAG, "[Window]Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                    }
                }
            }
        }
    }

    private void recordAssocDetail(int pid) {
        synchronized (this.mLock) {
            AwareAppAssociateUtils.AssocPidRecord record = this.mAssocRecordMap.get(pid);
            if (record != null) {
                AwareLog.i(TAG, "" + record);
            } else {
                AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(pid);
                if (br != null) {
                    AwareLog.i(TAG, "Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                }
                AwareLog.i(TAG, "" + sameUid(pid).orElse(null));
            }
        }
    }

    public static void enable() {
        sEnabled = true;
        AwareAppAssociate awareAppAssociate = sAwareAppAssociate;
        if (awareAppAssociate != null) {
            awareAppAssociate.initialize();
        }
    }

    public static void disable() {
        sEnabled = false;
        AwareAppAssociate awareAppAssociate = sAwareAppAssociate;
        if (awareAppAssociate != null) {
            awareAppAssociate.deInitialize();
        }
    }

    public static void enableDebug() {
        sDebug = true;
    }

    public static void disableDebug() {
        sDebug = false;
    }

    public static void enableRecord() {
        sRecord = true;
    }

    public static void disableRecord() {
        sRecord = false;
    }

    public boolean isPkgHasProc(String pkg) {
        boolean z = true;
        if (!sEnabled) {
            return true;
        }
        synchronized (this.mProcPkgMap) {
            if (this.mProcPkgMap.get(pkg) == null) {
                z = false;
            }
        }
        return z;
    }

    public void registerVisibleCallback(IAwareVisibleCallback callback) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                if (!this.mCallbacks.contains(callback)) {
                    this.mCallbacks.add(callback);
                }
            }
        }
    }

    public void unregisterVisibleCallback(IAwareVisibleCallback callback) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                if (this.mCallbacks.contains(callback)) {
                    this.mCallbacks.remove(callback);
                }
            }
        }
    }

    public void notifyVisibleWindowsChange(int type, int window, int mode) {
        synchronized (this.mCallbacks) {
            if (!this.mCallbacks.isEmpty()) {
                int callbackSize = this.mCallbacks.size();
                for (int i = 0; i < callbackSize; i++) {
                    this.mCallbacks.valueAt(i).onVisibleWindowsChanged(type, window, mode);
                }
            }
        }
    }

    public void screenStateChange(boolean screenOff) {
        AppAssocHandler appAssocHandler;
        this.mAssociateWindow.screenStateChange(screenOff);
        if (screenOff && (appAssocHandler = this.mHandler) != null) {
            appAssocHandler.removeMessages(4);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearRemoveVisWinDurScreenOff() {
        this.mAssociateWindow.clearRemoveVisWinDurScreenOff();
    }

    public void checkBakUpVisWin() {
        AppAssocHandler appAssocHandler = this.mHandler;
        if (appAssocHandler != null) {
            appAssocHandler.removeMessages(4);
            this.mHandler.sendEmptyMessageDelayed(4, 5000);
        }
    }

    public boolean isVisibleWindow(int pid) {
        if (!sEnabled) {
            return false;
        }
        return this.mAssociateWindow.isVisibleWindow(pid);
    }

    private void getPreviousAppOptLocked(SparseSet foregroundPids, ListIterator<Map.Entry<Integer, AwareAppLruBase>> iter, Set<AwareAppLruBase> previousApp) {
        int previousCount = getPreviousCount(foregroundPids);
        while (iter.hasPrevious()) {
            Map.Entry<Integer, AwareAppLruBase> entry = iter.previous();
            if (previousCount > 0) {
                AwareAppLruBase app = entry.getValue();
                AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(app.procPid);
                if (!foregroundPids.contains(app.procPid) && ((br == null || br.pkgList == null || (!br.pkgList.contains("com.android.systemui") && !br.pkgList.contains(PERMISSIOM_CONTROLLER))) && app.procPid != this.mHomeProcessPid)) {
                    previousApp.add(app);
                    previousCount--;
                    if (this.mMorePreviousLevel == 2 && br != null && br.pkgList != null && !br.pkgList.contains(AwareIntelligentRecg.getInstance().getDefaultSmsPackage())) {
                        previousCount--;
                    }
                }
            } else {
                return;
            }
        }
    }

    public Set<AwareAppLruBase> getPreviousAppOpt() {
        Set<AwareAppLruBase> previousApp = new ArraySet<>();
        SparseSet foregroundPids = new SparseSet();
        synchronized (this.mForePids) {
            for (int i = this.mForePids.size() - 1; i >= 0; i--) {
                foregroundPids.add(this.mForePids.keyAt(i));
            }
        }
        LinkedHashMap<Integer, AwareAppLruBase> lruCache = getActivityLruCache();
        ListIterator<Map.Entry<Integer, AwareAppLruBase>> iter = new ArrayList(lruCache.entrySet()).listIterator(lruCache.size());
        synchronized (this.mLock) {
            getPreviousAppOptLocked(foregroundPids, iter, previousApp);
        }
        return previousApp;
    }

    private int getPreviousCount(SparseSet foregroundPids) {
        int i = this.mMorePreviousLevel;
        if (i == 4) {
            return 4;
        }
        if (i == 0 || i > 2) {
            return 1;
        }
        synchronized (this.mLock) {
            for (int i2 = foregroundPids.size() - 1; i2 >= 0; i2--) {
                int forePid = foregroundPids.keyAt(i2);
                if (this.mHomeProcessPid == forePid) {
                    return 2;
                }
                AwareAppAssociateUtils.AssocBaseRecord br = this.mProcPidMap.get(forePid);
                if (br != null) {
                    if (br.pkgList != null) {
                        if (br.pkgList.contains("com.android.systemui")) {
                            return 2;
                        }
                    }
                }
            }
            return 1;
        }
    }

    public void setMorePreviousLevel(int levelValue) {
        this.mMorePreviousLevel = levelValue;
    }

    private int decideMorePreviousLevel() {
        MemInfoReaderExt minfo = new MemInfoReaderExt();
        minfo.readMemInfo();
        long totalMemMb = minfo.getTotalSize() / MemoryConstant.MB_SIZE;
        if (isPreviousHighEnable() && totalMemMb > 6144) {
            return 4;
        }
        if (totalMemMb > 3072) {
            return 1;
        }
        return 2;
    }

    private boolean isPreviousHighEnable() {
        int i = this.mIsPreviousHighEnable;
        if (i != 1 && i == 0) {
            return false;
        }
        return true;
    }

    public boolean isSystemUnRemoveApp(int uid) {
        if (!sEnabled) {
            return false;
        }
        int appUid = UserHandleEx.getAppId(uid);
        if (appUid < 10000) {
            return true;
        }
        SystemUnremoveUidCache systemUnremoveUidCache = this.mSystemUnremoveUidCache;
        if (systemUnremoveUidCache == null || !systemUnremoveUidCache.checkUidExist(appUid)) {
            return false;
        }
        return true;
    }

    private void addAppCompJobLock(AwareAppAssociateUtils.AssocBaseRecord baseRecord, String comp, int hwFlag) {
        if (baseRecord != null && needRecgCompJob(baseRecord.uid, hwFlag)) {
            baseRecord.componentsJob.add(comp);
        }
    }

    private void removeAppCompJobLock(AwareAppAssociateUtils.AssocBaseRecord baseRecord, String comp, int hwFlag) {
        if (baseRecord != null && needRecgCompJob(baseRecord.uid, hwFlag)) {
            baseRecord.componentsJob.remove(comp);
        }
    }

    private boolean isJobFlag(int hwFlag) {
        return (hwFlag & 8224) != 0;
    }

    private boolean needRecgCompJob(int uid, int hwFlag) {
        if (isJobFlag(hwFlag) && !isSystemUnRemoveApp(uid)) {
            return true;
        }
        return false;
    }

    private boolean isJobDoingForUidLocked(int uid) {
        AwareAppAssociateUtils.AssocPidRecord record = this.mAssocRecordMap.get(this.mMyPid);
        if (record == null) {
            return false;
        }
        int mapSize = record.assocBindService.getMap().size();
        for (int i = 0; i < mapSize; i++) {
            SparseArray<AwareAppAssociateUtils.AssocBaseRecord> brs = (SparseArray) record.assocBindService.getMap().valueAt(i);
            int arraySize = brs.size();
            for (int j = 0; j < arraySize; j++) {
                AwareAppAssociateUtils.AssocBaseRecord br = brs.valueAt(j);
                if (br != null && br.uid == uid && !br.componentsJob.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isJobDoingForUid(int uid) {
        boolean isJobDoingForUidLocked;
        if (!sEnabled) {
            return false;
        }
        synchronized (this.mLock) {
            isJobDoingForUidLocked = isJobDoingForUidLocked(uid);
        }
        return isJobDoingForUidLocked;
    }

    private void addAllForSparseIntArray(Map<Integer, Integer> from, SparseIntArray to) {
        for (Map.Entry<Integer, Integer> map : from.entrySet()) {
            to.put(map.getKey().intValue(), map.getValue().intValue());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addAllForSparseIntArray(SparseIntArray from, SparseIntArray to) {
        for (int i = from.size() - 1; i >= 0; i--) {
            to.put(from.keyAt(i), from.valueAt(i));
        }
    }

    public static boolean isEnabled() {
        return sEnabled;
    }

    protected static boolean isDebugEnabled() {
        return sDebug;
    }
}
