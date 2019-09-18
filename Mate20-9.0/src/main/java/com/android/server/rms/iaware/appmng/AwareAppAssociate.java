package com.android.server.rms.iaware.appmng;

import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.IProcessObserver;
import android.app.IUserSwitchObserver;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.LruCache;
import android.util.SparseArray;
import com.android.internal.app.ProcessMap;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.MemInfoReader;
import com.android.server.am.HwActivityManagerService;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessWindowInfo;
import com.android.server.mtm.iaware.appmng.appclean.CrashClean;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.AppCleanupFeature;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.view.HwWindowManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AwareAppAssociate {
    public static final int ASSOC_DECAY_MIN_TIME = 120000;
    public static final int ASSOC_REPORT_MIN_TIME = 60000;
    public static final int CLEAN_LEVEL = 0;
    private static final int CONSTANT_MORE_PREVIOUS_DISABLE = 0;
    private static final int CONSTANT_MORE_PREVIOUS_LEVEL1 = 1;
    private static final int CONSTANT_MORE_PREVIOUS_LEVEL2 = 2;
    private static final int COUNT_PROTECT_DEFAULT = 1;
    private static final int COUNT_PROTECT_MORE = 2;
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    public static final int FIRST_START_TIMES = 1;
    private static final int FIVE_SECONDS = 5000;
    private static final String INTERNALAPP_PKGNAME = "com.huawei.android.internal.app";
    private static final int LOW_MEM_THRESHOLD = 3072;
    private static final int MSG_CHECK_RECENT_FORE = 3;
    private static final int MSG_CLEAN = 2;
    private static final int MSG_CLEAR_BAKUP_VISWIN = 4;
    private static final int MSG_INIT = 1;
    public static final int MS_TO_SEC = 1000;
    private static final int ONE_SECOND = 1000;
    private static final int RECENT_TIME_INTERVAL = 10000;
    private static boolean RECORD = false;
    private static final long REINIT_TIME = 2000;
    public static final int RESTART_MAX_INTERVAL = 300;
    public static final int RESTART_MAX_TIMES = 30;
    private static final int SMCS_APP_WIDGET_SERVICE_GET_BY_USERID = 2;
    private static final String SYSTEM = "system";
    private static final String SYSTEM_UI_PKGNAME = "com.android.systemui";
    private static final String TAG = "RMS.AwareAppAssociate";
    private static final int VISIBLEWINDOWS_ADD_WINDOW = 4;
    private static final int VISIBLEWINDOWS_CACHE_CHANGE_MODE = 3;
    private static final int VISIBLEWINDOWS_CACHE_CLR = 2;
    private static final int VISIBLEWINDOWS_CACHE_DEL = 1;
    private static final int VISIBLEWINDOWS_CACHE_UPDATE = 0;
    private static final int VISIBLEWINDOWS_REMOVE_WINDOW = 5;
    private static final int WIDGET_INVISIBLE = 0;
    private static final int WIDGET_VISIBLE = 1;
    private static AwareAppAssociate mAwareAppAssociate = null;
    private static boolean mEnabled = false;
    private final AwareAppLruBase mAmsPrevBase;
    private final ArrayMap<Integer, AssocPidRecord> mAssocRecordMap;
    /* access modifiers changed from: private */
    public ArrayMap<Integer, ProcessData> mBgRecentForcePids;
    private final ArraySet<IAwareVisibleCallback> mCallbacks;
    /* access modifiers changed from: private */
    public int mCurSwitchUser;
    /* access modifiers changed from: private */
    public int mCurUserId;
    /* access modifiers changed from: private */
    public ArrayMap<Integer, Integer> mForePids;
    /* access modifiers changed from: private */
    public AppAssocHandler mHandler;
    private ArrayList<String> mHomePackageList;
    private int mHomeProcessPid;
    private int mHomeProcessUid;
    private HwActivityManagerService mHwAMS;
    private AtomicBoolean mIsInitialized;
    private LruCache<Integer, AwareAppLruBase> mLruCache;
    private int mMorePreviousLevel;
    /* access modifiers changed from: private */
    public MultiTaskManagerService mMtmService;
    private int mMyPid;
    private final AwareAppLruBase mPrevNonHomeBase;
    private final ProcessMap<AssocBaseRecord> mProcInfoMap;
    private Map<Integer, Map<String, Map<String, LaunchData>>> mProcLaunchMap;
    private final ArrayMap<Integer, AssocBaseRecord> mProcPidMap;
    private final ArrayMap<String, ArraySet<Integer>> mProcPkgMap;
    private final ArrayMap<Integer, ArraySet<Integer>> mProcUidMap;
    private IProcessObserver mProcessObserver;
    private final AwareAppLruBase mRecentTaskPrevBase;
    private boolean mScreenOff;
    IUserSwitchObserver mUserSwitchObserver;
    private ArraySet<Integer> mVisWinDurScreenOff;
    private ArrayMap<Integer, AwareProcessWindowInfo> mVisibleWindows;
    private ArrayMap<Integer, AwareProcessWindowInfo> mVisibleWindowsCache;
    private ArrayMap<Integer, ArrayMap<Integer, Widget>> mWidgets;

    private class AppAssocHandler extends Handler {
        public AppAssocHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (AwareAppAssociate.DEBUG) {
                AwareLog.e(AwareAppAssociate.TAG, "handleMessage message " + msg.what);
            }
            if (msg.what == 1) {
                AwareAppAssociate.this.initialize();
            } else if (msg.what == 2) {
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
                    if (AwareAppAssociate.DEBUG) {
                        AwareLog.i(AwareAppAssociate.TAG, "Pkg:" + pkg + " will be cleaned due to high-freq-restart of proc:" + proc);
                    }
                    crashClean.clean();
                } catch (NumberFormatException e) {
                    AwareLog.e(AwareAppAssociate.TAG, "MSG_CLEAN, userId format error!");
                }
            } else {
                if (msg.what == 3) {
                    AwareAppAssociate.this.checkRecentForce();
                } else if (msg.what == 4) {
                    AwareAppAssociate.this.clearRemoveVisWinDurScreenOff();
                }
            }
        }
    }

    private static final class AssocBaseRecord {
        public boolean isStrong = true;
        public HashSet<String> mComponents = new HashSet<>();
        public long miniTime;
        public int pid;
        public ArraySet<String> pkgList = new ArraySet<>();
        public String processName;
        public int uid;

        public AssocBaseRecord(String name, int uid2, int pid2) {
            this.processName = name;
            this.uid = uid2;
            this.pid = pid2;
            this.miniTime = SystemClock.elapsedRealtime();
        }
    }

    private final class AssocPidRecord {
        public final ProcessMap<AssocBaseRecord> mAssocBindService = new ProcessMap<>();
        public final ProcessMap<AssocBaseRecord> mAssocProvider = new ProcessMap<>();
        public int pid;
        public String processName;
        public int uid;

        public AssocPidRecord(int pid2, int uid2, String name) {
            this.pid = pid2;
            this.uid = uid2;
            this.processName = name;
        }

        public ProcessMap<AssocBaseRecord> getMap(int type) {
            switch (type) {
                case 1:
                case 3:
                    return this.mAssocBindService;
                case 2:
                    return this.mAssocProvider;
                default:
                    return null;
            }
        }

        public boolean isEmpty() {
            return this.mAssocBindService.getMap().isEmpty() && this.mAssocProvider.getMap().isEmpty();
        }

        public int size() {
            return this.mAssocBindService.getMap().size() + this.mAssocProvider.getMap().size();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Pid:");
            sb.append(this.pid);
            sb.append(",Uid:");
            sb.append(this.uid);
            sb.append(",ProcessName:");
            sb.append(this.processName);
            sb.append("\n");
            String sameUid = AwareAppAssociate.this.sameUid(this.pid);
            if (sameUid != null) {
                sb.append(sameUid);
            }
            int NP = this.mAssocBindService.getMap().size();
            boolean flag = true;
            int i = 0;
            while (i < NP) {
                SparseArray<AssocBaseRecord> brs = (SparseArray) this.mAssocBindService.getMap().valueAt(i);
                int NB = brs.size();
                boolean flag2 = flag;
                int j = 0;
                while (j < NB) {
                    AssocBaseRecord br = brs.valueAt(j);
                    if (flag2) {
                        sb.append("    [BindService] depend on:\n");
                        flag2 = false;
                    }
                    Iterator<String> it = br.mComponents.iterator();
                    while (it.hasNext()) {
                        sb.append("        Pid:");
                        sb.append(br.pid);
                        sb.append(",Uid:");
                        sb.append(br.uid);
                        sb.append(",ProcessName:");
                        sb.append(br.processName);
                        sb.append(",Time:");
                        sb.append(SystemClock.elapsedRealtime() - br.miniTime);
                        sb.append(",Component:");
                        sb.append(it.next());
                        sb.append("\n");
                        j = j;
                    }
                    j++;
                }
                i++;
                flag = flag2;
            }
            int NP2 = this.mAssocProvider.getMap().size();
            boolean flag3 = true;
            int i2 = 0;
            while (i2 < NP2) {
                SparseArray<AssocBaseRecord> brs2 = (SparseArray) this.mAssocProvider.getMap().valueAt(i2);
                int NB2 = brs2.size();
                boolean flag4 = flag3;
                for (int j2 = 0; j2 < NB2; j2++) {
                    AssocBaseRecord br2 = brs2.valueAt(j2);
                    if (flag4) {
                        sb.append("    [Provider] depend on:\n");
                        flag4 = false;
                    }
                    Iterator<String> it2 = br2.mComponents.iterator();
                    while (it2.hasNext()) {
                        String component = it2.next();
                        String sameUid2 = sameUid;
                        int i3 = NP2;
                        if (SystemClock.elapsedRealtime() - br2.miniTime < 120000) {
                            sb.append("        Pid:");
                            sb.append(br2.pid);
                            sb.append(",Uid:");
                            sb.append(br2.uid);
                            sb.append(",ProcessName:");
                            sb.append(br2.processName);
                            sb.append(",Time:");
                            sb.append(SystemClock.elapsedRealtime() - br2.miniTime);
                            sb.append(",Component:");
                            sb.append(component);
                            sb.append(",Strong:");
                            sb.append(br2.isStrong);
                            sb.append("\n");
                        }
                        sameUid = sameUid2;
                        NP2 = i3;
                    }
                    int i4 = NP2;
                }
                int i5 = NP2;
                i2++;
                flag3 = flag4;
            }
            int i6 = NP2;
            return sb.toString();
        }
    }

    public interface IAwareVisibleCallback {
        void onVisibleWindowsChanged(int i, int i2, int i3);
    }

    private static class LaunchData {
        private long mFirstTime;
        private int mLaunchTimes;

        private LaunchData(int launchTimes, long firstTime) {
            this.mLaunchTimes = launchTimes;
            this.mFirstTime = firstTime;
        }

        /* access modifiers changed from: private */
        public LaunchData increase() {
            this.mLaunchTimes++;
            return this;
        }

        /* access modifiers changed from: private */
        public long getFirstTime() {
            return this.mFirstTime;
        }

        /* access modifiers changed from: private */
        public int getLaunchTimes() {
            return this.mLaunchTimes;
        }
    }

    private static class ProcessData {
        /* access modifiers changed from: private */
        public long mTimeStamp;
        /* access modifiers changed from: private */
        public int mUid;

        private ProcessData(int uid, long timeStamp) {
            this.mUid = uid;
            this.mTimeStamp = timeStamp;
        }
    }

    private static final class Widget {
        int appWidgetId;
        boolean isVisible = false;
        String pkgName = "";

        public Widget(int appWidgetId2, String pkgName2, boolean isVisible2) {
            this.appWidgetId = appWidgetId2;
            this.pkgName = pkgName2;
            this.isVisible = isVisible2;
        }
    }

    /* access modifiers changed from: private */
    public void checkRecentForce() {
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
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.d(TAG, "register process observer failed");
        }
    }

    private void unregisterProcessObserver() {
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.d(TAG, "unregister process observer failed");
        }
    }

    /* JADX WARNING: type inference failed for: r2v2, types: [java.util.Map] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private LinkedHashMap<Integer, AwareAppLruBase> getActivityLruCache() {
        LinkedHashMap<Integer, AwareAppLruBase> lru = null;
        synchronized (this.mLruCache) {
            ? snapshot = this.mLruCache.snapshot();
            if (snapshot instanceof LinkedHashMap) {
                lru = snapshot;
            }
        }
        return lru;
    }

    private boolean updateActivityLruCache(int pid, int uid) {
        int i = pid;
        int i2 = uid;
        long timeNow = SystemClock.elapsedRealtime();
        synchronized (this.mLruCache) {
            if (this.mLruCache.size() == 0) {
                this.mLruCache.put(Integer.valueOf(uid), new AwareAppLruBase(i, i2, timeNow));
                return false;
            }
            LinkedHashMap<Integer, AwareAppLruBase> lru = getActivityLruCache();
            if (lru == null) {
                return false;
            }
            List<Integer> list = new ArrayList<>(lru.keySet());
            if (list.size() < 1) {
                return false;
            }
            int prevUid = list.get(list.size() - 1).intValue();
            AwareAppLruBase lruBase = lru.get(Integer.valueOf(prevUid));
            if (lruBase == null) {
                return false;
            }
            if (prevUid == i2) {
                this.mLruCache.put(Integer.valueOf(prevUid), new AwareAppLruBase(i, prevUid, lruBase.mTime));
                return false;
            }
            LinkedHashMap<Integer, AwareAppLruBase> linkedHashMap = lru;
            int prevUid2 = prevUid;
            AwareAppLruBase lruBase2 = lruBase;
            if (isSystemDialogProc(lruBase.mPid, prevUid, lruBase.mTime, timeNow)) {
                this.mLruCache.remove(Integer.valueOf(prevUid2));
            } else {
                this.mLruCache.put(Integer.valueOf(prevUid2), new AwareAppLruBase(lruBase2.mPid, prevUid2, timeNow));
            }
            this.mLruCache.put(Integer.valueOf(uid), new AwareAppLruBase(i, i2, timeNow));
            return true;
        }
    }

    private void updatePrevApp(int pid, int uid) {
        if (updateActivityLruCache(pid, uid)) {
            LinkedHashMap<Integer, AwareAppLruBase> lru = getActivityLruCache();
            if (lru != null) {
                List<Integer> list = new ArrayList<>(lru.keySet());
                int listSize = list.size();
                if (listSize >= 2) {
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
        }
    }

    /* access modifiers changed from: private */
    public void updatePreviousAppInfo(int pid, int uid, boolean foregroundActivities, Map<Integer, Integer> forePids) {
        if (this.mHwAMS != null) {
            if (!foregroundActivities) {
                if (forePids != null) {
                    if (!forePids.containsValue(Integer.valueOf(uid)) && pid != this.mHomeProcessPid) {
                        this.mRecentTaskPrevBase.setValue(pid, uid, SystemClock.elapsedRealtime());
                    }
                    for (Map.Entry<Integer, Integer> m : forePids.entrySet()) {
                        Integer forePid = m.getKey();
                        if (isForgroundPid(forePid.intValue())) {
                            updatePrevApp(forePid.intValue(), m.getValue().intValue());
                            return;
                        }
                    }
                }
            } else if (isForgroundPid(pid)) {
                updatePrevApp(pid, uid);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0038, code lost:
        return false;
     */
    private boolean isSystemDialogProc(int pid, int uid, long prevActTime, long curTime) {
        if (UserHandle.getAppId(uid) != 1000) {
            return false;
        }
        synchronized (this) {
            AssocBaseRecord br = this.mProcPidMap.get(Integer.valueOf(pid));
            if (br != null) {
                if (br.pkgList != null) {
                    if (br.pkgList.size() != 1) {
                        return false;
                    }
                    if (br.pkgList.contains(INTERNALAPP_PKGNAME)) {
                        return true;
                    }
                    return false;
                }
            }
        }
    }

    private boolean isForgroundPid(int pid) {
        if (this.mHwAMS.getProcessBaseInfo(pid).mCurAdj == 0) {
            return true;
        }
        return false;
    }

    public static boolean isDealAsPkgUid(int uid) {
        int appId = UserHandle.getAppId(uid);
        return appId >= 1000 && appId <= 1001;
    }

    private AwareAppAssociate() {
        this.mMyPid = Process.myPid();
        this.mIsInitialized = new AtomicBoolean(false);
        this.mHandler = null;
        this.mForePids = new ArrayMap<>();
        this.mLruCache = new LruCache<>(6);
        this.mMorePreviousLevel = 0;
        this.mProcLaunchMap = new HashMap();
        this.mPrevNonHomeBase = new AwareAppLruBase();
        this.mRecentTaskPrevBase = new AwareAppLruBase();
        this.mAmsPrevBase = new AwareAppLruBase();
        this.mCurUserId = 0;
        this.mCurSwitchUser = 0;
        this.mVisibleWindows = new ArrayMap<>();
        this.mVisibleWindowsCache = new ArrayMap<>();
        this.mWidgets = new ArrayMap<>();
        this.mHomeProcessPid = 0;
        this.mHomeProcessUid = 0;
        this.mHomePackageList = new ArrayList<>();
        this.mAssocRecordMap = new ArrayMap<>();
        this.mProcInfoMap = new ProcessMap<>();
        this.mProcPidMap = new ArrayMap<>();
        this.mProcUidMap = new ArrayMap<>();
        this.mProcPkgMap = new ArrayMap<>();
        this.mCallbacks = new ArraySet<>();
        this.mVisWinDurScreenOff = new ArraySet<>();
        this.mScreenOff = false;
        this.mBgRecentForcePids = new ArrayMap<>();
        this.mProcessObserver = new IProcessObserver.Stub() {
            public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
                if (AwareAppAssociate.DEBUG) {
                    AwareLog.i(AwareAppAssociate.TAG, "Pid:" + pid + ",Uid:" + uid + " come to foreground." + foregroundActivities);
                }
                ArrayMap<Integer, Integer> forePidsBak = new ArrayMap<>();
                synchronized (AwareAppAssociate.this.mForePids) {
                    if (foregroundActivities) {
                        try {
                            AwareAppAssociate.this.mForePids.put(Integer.valueOf(pid), Integer.valueOf(uid));
                            forePidsBak.putAll(AwareAppAssociate.this.mForePids);
                        } catch (Throwable th) {
                            while (true) {
                                throw th;
                            }
                        }
                    } else {
                        AwareAppAssociate.this.mForePids.remove(Integer.valueOf(pid));
                        forePidsBak.putAll(AwareAppAssociate.this.mForePids);
                    }
                }
                synchronized (AwareAppAssociate.this.mBgRecentForcePids) {
                    if (foregroundActivities) {
                        try {
                            AwareAppAssociate.this.mBgRecentForcePids.remove(Integer.valueOf(pid));
                        } catch (Throwable th2) {
                            while (true) {
                                throw th2;
                            }
                        }
                    } else {
                        AwareAppAssociate.this.mBgRecentForcePids.put(Integer.valueOf(pid), new ProcessData(uid, SystemClock.elapsedRealtime()));
                        if (AwareAppAssociate.this.mHandler != null) {
                            AwareAppAssociate.this.mHandler.sendEmptyMessageDelayed(3, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                        }
                    }
                }
                AwareAppAssociate.this.updatePreviousAppInfo(pid, uid, foregroundActivities, forePidsBak);
                AwareIntelligentRecg.getInstance().onForegroundActivitiesChanged(pid, uid, foregroundActivities);
            }

            public void onProcessDied(int pid, int uid) {
                synchronized (AwareAppAssociate.this.mForePids) {
                    AwareAppAssociate.this.mForePids.remove(Integer.valueOf(pid));
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
                AwareAppAssociate.this.removeDiedRecordProc(uid, pid);
                HwActivityManager.reportProcessDied(pid);
                AwareIntelligentRecg.getInstance().onProcessDied(pid, uid);
            }
        };
        this.mUserSwitchObserver = new IUserSwitchObserver.Stub() {
            public void onUserSwitching(int newUserId, IRemoteCallback reply) {
                if (reply != null) {
                    try {
                        reply.sendResult(null);
                        int unused = AwareAppAssociate.this.mCurSwitchUser = newUserId;
                    } catch (RemoteException e) {
                        AwareLog.e(AwareAppAssociate.TAG, "RemoteException onUserSwitching");
                    }
                }
            }

            public void onUserSwitchComplete(int newUserId) throws RemoteException {
                long startTime = System.currentTimeMillis();
                AwareAppAssociate.this.checkAndInitWidgetObj(newUserId);
                int unused = AwareAppAssociate.this.mCurUserId = newUserId;
                int unused2 = AwareAppAssociate.this.mCurSwitchUser = newUserId;
                AwareAppAssociate.this.updateWidgets(AwareAppAssociate.this.mCurUserId);
                AwareIntelligentRecg.getInstance().initUserSwitch(newUserId);
                AwareFakeActivityRecg.self().initUserSwitch(newUserId);
                if (newUserId == 0) {
                    AwareIntelligentRecg.getInstance().updateWidget(AwareAppAssociate.this.getWidgetsPkg(newUserId), null);
                }
                AwareLog.i(AwareAppAssociate.TAG, "onUserSwitchComplete cost: " + (System.currentTimeMillis() - startTime));
            }

            public void onForegroundProfileSwitch(int newProfileId) {
            }

            public void onLockedBootComplete(int newUserId) {
            }
        };
        this.mHwAMS = HwActivityManagerService.self();
        this.mHandler = new AppAssocHandler(BackgroundThread.get().getLooper());
        this.mMorePreviousLevel = decideMorePreviousLevel();
    }

    public static synchronized AwareAppAssociate getInstance() {
        AwareAppAssociate awareAppAssociate;
        synchronized (AwareAppAssociate.class) {
            if (mAwareAppAssociate == null) {
                mAwareAppAssociate = new AwareAppAssociate();
            }
            awareAppAssociate = mAwareAppAssociate;
        }
        return awareAppAssociate;
    }

    public void getVisibleWindowsInRestriction(Set<Integer> windowPids) {
        if (mEnabled && windowPids != null) {
            synchronized (this.mVisibleWindows) {
                for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                    AwareProcessWindowInfo winInfo = window.getValue();
                    if (winInfo.mInRestriction && !winInfo.isEvil()) {
                        windowPids.add(window.getKey());
                    }
                }
            }
            if (DEBUG) {
                AwareLog.d(TAG, "WindowPids in restriction:" + windowPids);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x007f A[SYNTHETIC] */
    public void getVisibleWindows(Set<Integer> windowPids, Set<Integer> evilPids) {
        boolean allowedWindow;
        if (mEnabled && windowPids != null) {
            synchronized (this.mVisibleWindows) {
                for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                    AwareProcessWindowInfo winInfo = window.getValue();
                    if (winInfo.mMode != 0) {
                        if (winInfo.mMode != 3) {
                            allowedWindow = false;
                            AwareLog.i(TAG, "[getVisibleWindows]:" + window.getKey() + " [allowedWindow]:" + allowedWindow + " isEvil:" + winInfo.isEvil());
                            if (!allowedWindow && !winInfo.isEvil()) {
                                windowPids.add(window.getKey());
                            } else if (evilPids == null) {
                                evilPids.add(window.getKey());
                            }
                        }
                    }
                    allowedWindow = true;
                    AwareLog.i(TAG, "[getVisibleWindows]:" + window.getKey() + " [allowedWindow]:" + allowedWindow + " isEvil:" + winInfo.isEvil());
                    if (!allowedWindow) {
                    }
                    if (evilPids == null) {
                    }
                }
            }
            synchronized (this.mVisWinDurScreenOff) {
                if (!this.mVisWinDurScreenOff.isEmpty()) {
                    windowPids.addAll(this.mVisWinDurScreenOff);
                }
            }
            if (DEBUG) {
                AwareLog.d(TAG, "WindowPids:" + windowPids + ", evilPids:" + evilPids);
            }
            if (RECORD) {
                recordWindowDetail(windowPids);
            }
        }
    }

    public boolean isVisibleWindows(int userid, String pkg) {
        if (!mEnabled || pkg == null) {
            return true;
        }
        synchronized (this.mVisibleWindows) {
            for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                AwareProcessWindowInfo winInfo = window.getValue();
                boolean allowedWindow = isAllowedAlertWindowOps(winInfo);
                AwareLog.i(TAG, "[isVisibleWindows]:" + window.getKey() + " pkg:" + pkg + " [allowedWindow]:" + allowedWindow + " isEvil:" + winInfo.isEvil());
                if (pkg.equals(winInfo.mPkg) && ((userid == -1 || userid == UserHandle.getUserId(winInfo.mUid)) && allowedWindow && !winInfo.isEvil())) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean hasWindow(int uid) {
        synchronized (this.mVisibleWindows) {
            for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                if (uid == window.getValue().mUid) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isAllowedAlertWindowOps(AwareProcessWindowInfo winInfo) {
        return winInfo.mMode == 0 || winInfo.mMode == 3;
    }

    public boolean isEvilAlertWindow(int window, int code) {
        boolean result;
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mVisibleWindows) {
            AwareProcessWindowInfo winInfo = this.mVisibleWindows.get(Integer.valueOf(window));
            if (winInfo == null || (isAllowedAlertWindowOps(winInfo) && !winInfo.isEvil(code))) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public void updateWidgets(int userId) {
        if (DEBUG) {
            AwareLog.i(TAG, "updateWidgets, userId: " + userId);
        }
        IBinder service = ServiceManager.getService("appwidget");
        if (service != null) {
            ArrayMap<Integer, Widget> widgets = new ArrayMap<>();
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInt(2);
            data.writeInt(userId);
            try {
                service.transact(1599297111, data, reply, 0);
                int size = reply.readInt();
                if (DEBUG) {
                    AwareLog.i(TAG, "updateWidgets, transact finish, widgets size: " + size);
                }
                for (int i = 0; i < size; i++) {
                    int id = reply.readInt();
                    String pkg = reply.readString();
                    boolean visibleB = true;
                    if (reply.readInt() != 1) {
                        visibleB = false;
                    }
                    if (pkg != null && pkg.length() > 0) {
                        widgets.put(Integer.valueOf(id), new Widget(id, pkg, visibleB));
                    }
                    if (DEBUG) {
                        AwareLog.i(TAG, "updateWidgets, widget: " + id + ", " + pkg + ", " + visibleB);
                    }
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "getWidgetsPkg, transact error!");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
            synchronized (this.mWidgets) {
                this.mWidgets.put(Integer.valueOf(userId), widgets);
            }
        }
    }

    public Set<String> getWidgetsPkg() {
        return getWidgetsPkg(this.mCurUserId);
    }

    public Set<String> getWidgetsPkg(int userId) {
        if (!mEnabled) {
            return null;
        }
        ArraySet<String> widgets = new ArraySet<>();
        synchronized (this.mWidgets) {
            ArrayMap<Integer, Widget> widgetMap = this.mWidgets.get(Integer.valueOf(userId));
            if (widgetMap != null) {
                for (Map.Entry<Integer, Widget> entry : widgetMap.entrySet()) {
                    Widget widget = entry.getValue();
                    if (widget.isVisible) {
                        widgets.add(widget.pkgName);
                    }
                    if (DEBUG) {
                        AwareLog.i(TAG, "getWidgetsPkg:" + widget.appWidgetId + ", " + widget.pkgName + ", " + widget.isVisible);
                    }
                }
            }
        }
        return widgets;
    }

    public void getForeGroundApp(Set<Integer> forePids) {
        if (mEnabled && forePids != null) {
            synchronized (this.mForePids) {
                forePids.addAll(this.mForePids.keySet());
            }
        }
    }

    public boolean isForeGroundApp(int uid) {
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mForePids) {
            for (Map.Entry<Integer, Integer> map : this.mForePids.entrySet()) {
                if (uid == map.getValue().intValue()) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isRecentFgApp(int uid) {
        if (!mEnabled) {
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

    public void getAssocListForPid(int pid, Set<Integer> strong) {
        if (mEnabled && pid > 0 && strong != null) {
            getStrongAssoc(pid, strong);
            if (DEBUG) {
                AwareLog.i(TAG, "[" + pid + "]strongList:" + strong);
            }
            if (RECORD) {
                recordAssocDetail(pid);
            }
        }
    }

    public void getAssocClientListForPid(int pid, Set<Integer> strong) {
        if (mEnabled && pid > 0 && strong != null) {
            getStrongAssocClient(pid, strong);
            if (DEBUG) {
                AwareLog.i(TAG, "[" + pid + "]strongList:" + strong);
            }
            if (RECORD) {
                recordAssocDetail(pid);
            }
        }
    }

    private void getStrongAssocClient(int pid, Set<Integer> strong) {
        if (pid > 0 && strong != null) {
            synchronized (this) {
                for (Map.Entry<Integer, AssocPidRecord> map : this.mAssocRecordMap.entrySet()) {
                    int clientPid = map.getKey().intValue();
                    AssocPidRecord record = map.getValue();
                    int NP = record.mAssocBindService.getMap().size();
                    for (int i = 0; i < NP; i++) {
                        SparseArray<AssocBaseRecord> brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                        int NB = brs.size();
                        for (int j = 0; j < NB; j++) {
                            AssocBaseRecord br = brs.valueAt(j);
                            if (br != null && br.pid == pid) {
                                strong.add(Integer.valueOf(clientPid));
                            }
                        }
                    }
                }
            }
        }
    }

    public void getAssocClientListForUid(int uid, Set<String> strong) {
        if (mEnabled && uid > 0 && strong != null) {
            synchronized (this) {
                for (Map.Entry<Integer, AssocPidRecord> map : this.mAssocRecordMap.entrySet()) {
                    AssocPidRecord record = map.getValue();
                    if (UserHandle.getAppId(record.uid) >= 10000) {
                        int NP = record.mAssocBindService.getMap().size();
                        boolean bfound = false;
                        int i = 0;
                        while (true) {
                            if (i >= NP) {
                                break;
                            }
                            SparseArray<AssocBaseRecord> brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                            int NB = brs.size();
                            int j = 0;
                            while (true) {
                                if (j >= NB) {
                                    break;
                                }
                                AssocBaseRecord br = brs.valueAt(j);
                                if (br != null && br.uid == uid) {
                                    strong.addAll(getPackageNameForUid(record.uid, record.pid));
                                    bfound = true;
                                    break;
                                }
                                j++;
                            }
                            if (bfound) {
                                break;
                            }
                            i++;
                        }
                    }
                }
            }
            if (DEBUG) {
                AwareLog.i(TAG, "[" + uid + "]strongList:" + strong);
            }
        }
    }

    public void report(int eventId, Bundle bundleArgs) {
        int callerPid;
        int i = eventId;
        Bundle bundle = bundleArgs;
        if (!mEnabled) {
            if (DEBUG) {
                AwareLog.d(TAG, "AwareAppAssociate feature disabled!");
            }
            return;
        }
        if (DEBUG) {
            AwareLog.d(TAG, "eventId: " + i);
        }
        if (bundle != null) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            switch (i) {
                case 1:
                case 2:
                    callerPid = bundle.getInt("callPid");
                    addProcessRelation(callerPid, bundle.getInt("callUid"), bundle.getString("callProcName"), bundle.getInt("tgtUid"), bundle.getString("tgtProcName"), bundle.getString("compName"), i);
                    break;
                case 3:
                    callerPid = bundle.getInt("callPid");
                    removeProcessRelation(callerPid, bundle.getInt("callUid"), bundle.getString("callProcName"), bundle.getInt("tgtUid"), bundle.getString("tgtProcName"), bundle.getString("compName"), i);
                    break;
                case 4:
                    updateProcessRelation(bundle.getInt("callPid"), bundle.getInt("callUid"), bundle.getString("callProcName"), bundle.getStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                    break;
                case 5:
                    addWidget(bundle.getInt("userid"), bundle.getInt("widgetId", -1), bundle.getString("widget"), bundle.getBundle("widgetOpt"));
                    break;
                case 6:
                    removeWidget(bundle.getInt("userid"), bundle.getInt("widgetId", -1), bundle.getString("widget"));
                    break;
                case 7:
                    clearWidget();
                    break;
                case 8:
                    addWindow(bundle.getInt("window"), bundle.getInt("windowmode"), bundle.getInt("hashcode"), bundle.getInt("width"), bundle.getInt("height"), bundle.getFloat("alpha"), bundle.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME), bundle.getInt("uid"));
                    break;
                case 9:
                    removeWindow(bundle.getInt("window"), bundle.getInt("hashcode"));
                    break;
                case 10:
                    updateWindowOps(bundle.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                    break;
                case 11:
                    reportHome(bundle.getInt("pid"), bundle.getInt("tgtUid"), bundle.getStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                    break;
                case 12:
                    reportPrevInfo(bundle.getInt("pid"), bundle.getInt("tgtUid"));
                    break;
                default:
                    switch (i) {
                        case 24:
                            updateWidgetOptions(bundle.getInt("userid"), bundle.getInt("widgetId", -1), bundle.getString("widget"), bundle.getBundle("widgetOpt"));
                            break;
                        case 25:
                            AwareIntelligentRecg.getInstance().addScreenRecord(bundle.getInt("callUid"), bundle.getInt("callPid"));
                            break;
                        case 26:
                            AwareIntelligentRecg.getInstance().removeScreenRecord(bundle.getInt("callUid"), bundle.getInt("callPid"));
                            break;
                        case 27:
                            updateWindow(bundle.getInt("window"), bundle.getInt("windowmode"), bundle.getInt("hashcode"), bundle.getInt("width"), bundle.getInt("height"), bundle.getFloat("alpha"));
                            break;
                        default:
                            switch (i) {
                                case 30:
                                    AwareIntelligentRecg.getInstance().addCamera(bundle.getInt("callUid"));
                                    break;
                                case 31:
                                    AwareIntelligentRecg.getInstance().removeCamera(bundle.getInt("callUid"));
                                    break;
                                case 32:
                                    updateWidgetFlush(bundle.getInt("userid"), bundle.getString("widget"));
                                    break;
                                case 33:
                                    AwareIntelligentRecg.getInstance().reportGoogleConn(bundle.getBoolean("gms_conn"));
                                    break;
                                default:
                                    if (DEBUG) {
                                        AwareLog.e(TAG, "Unknown EventID: " + i);
                                        break;
                                    }
                                    break;
                            }
                    }
            }
        }
    }

    private void getStrongAssoc(int pid, Set<Integer> strong) {
        Set<Integer> set = strong;
        if (pid > 0 && set != null) {
            synchronized (this) {
                long curElapse = SystemClock.elapsedRealtime();
                AssocPidRecord record = this.mAssocRecordMap.get(Integer.valueOf(pid));
                if (record != null) {
                    int NP = record.mAssocBindService.getMap().size();
                    AssocBaseRecord br = null;
                    int targetPid = 0;
                    int i = 0;
                    while (i < NP) {
                        SparseArray<AssocBaseRecord> brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                        int NB = brs.size();
                        int targetPid2 = targetPid;
                        for (int j = 0; j < NB; j++) {
                            br = brs.valueAt(j);
                            targetPid2 = br.pid;
                            if (targetPid2 != 0) {
                                set.add(Integer.valueOf(targetPid2));
                            }
                        }
                        i++;
                        targetPid = targetPid2;
                    }
                    int NP2 = record.mAssocProvider.getMap().size();
                    int targetPid3 = targetPid;
                    int i2 = 0;
                    while (i2 < NP2) {
                        SparseArray<AssocBaseRecord> brs2 = (SparseArray) record.mAssocProvider.getMap().valueAt(i2);
                        int NB2 = brs2.size();
                        AssocBaseRecord br2 = br;
                        int targetPid4 = targetPid3;
                        for (int j2 = 0; j2 < NB2; j2++) {
                            br2 = brs2.valueAt(j2);
                            targetPid4 = br2.pid;
                            if (targetPid4 != 0 && br2.isStrong && curElapse - br2.miniTime < 120000) {
                                set.add(Integer.valueOf(targetPid4));
                            }
                        }
                        i2++;
                        targetPid3 = targetPid4;
                        br = br2;
                    }
                }
            }
        }
    }

    public void getAssocProvider(int pid, Set<Integer> assocProvider) {
        Set<Integer> set = assocProvider;
        if (pid > 0 && set != null) {
            synchronized (this) {
                long curElapse = SystemClock.elapsedRealtime();
                AssocPidRecord record = this.mAssocRecordMap.get(Integer.valueOf(pid));
                if (record != null) {
                    AssocBaseRecord br = null;
                    int NP = record.mAssocProvider.getMap().size();
                    int i = 0;
                    while (i < NP) {
                        SparseArray<AssocBaseRecord> brs = (SparseArray) record.mAssocProvider.getMap().valueAt(i);
                        int NB = brs.size();
                        AssocBaseRecord br2 = br;
                        for (int j = 0; j < NB; j++) {
                            br2 = brs.valueAt(j);
                            int targetPid = br2.pid;
                            if (targetPid != 0 && br2.isStrong && curElapse - br2.miniTime < 120000) {
                                set.add(Integer.valueOf(targetPid));
                            }
                        }
                        i++;
                        br = br2;
                    }
                }
            }
        }
    }

    private void addWidget(int userId, int widgetId, String pkgName, Bundle options) {
        if (pkgName != null) {
            if (DEBUG) {
                AwareLog.i(TAG, "addWidget, userId:" + userId + ", widgetId: " + widgetId + ", pkg:" + pkgName + ", vis: " + isWidgetVisible(options));
            }
            synchronized (this.mWidgets) {
                checkAndInitWidgetObj(userId);
                if (!this.mWidgets.get(Integer.valueOf(userId)).containsKey(Integer.valueOf(widgetId))) {
                    this.mWidgets.get(Integer.valueOf(userId)).put(Integer.valueOf(widgetId), new Widget(widgetId, pkgName, isWidgetVisible(options)));
                }
            }
            if (userId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(userId), pkgName);
            }
        }
    }

    private void removeWidget(int userId, int widgetId, String pkgName) {
        if (pkgName != null) {
            if (DEBUG) {
                AwareLog.i(TAG, "removeWidget, userId:" + userId + ", widgetId: " + widgetId + ", pkg:" + pkgName);
            }
            synchronized (this.mWidgets) {
                checkAndInitWidgetObj(userId);
                this.mWidgets.get(Integer.valueOf(userId)).remove(Integer.valueOf(widgetId));
            }
            if (userId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(userId), pkgName);
            }
        }
    }

    private void updateWidgetOptions(int userId, int widgetId, String pkgName, Bundle options) {
        if (widgetId >= 0 && pkgName != null) {
            if (DEBUG) {
                AwareLog.i(TAG, "updateWidgetOptions, userId:" + userId + ", widgetId: " + widgetId + ", pkg:" + pkgName + ", options: " + options);
            }
            boolean visible = isWidgetVisible(options);
            synchronized (this.mWidgets) {
                checkAndInitWidgetObj(userId);
                ArrayMap<Integer, Widget> widgetMap = this.mWidgets.get(Integer.valueOf(userId));
                if (widgetMap.get(Integer.valueOf(widgetId)) != null) {
                    widgetMap.get(Integer.valueOf(widgetId)).isVisible = visible;
                } else {
                    widgetMap.put(Integer.valueOf(widgetId), new Widget(widgetId, pkgName, visible));
                }
                if (userId == 0) {
                    AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(userId), pkgName);
                }
            }
        }
    }

    private void updateWidgetFlush(int userId, String pkgName) {
        AwareIntelligentRecg.getInstance().widgetTrigUpdate(pkgName);
    }

    public boolean isWidgetVisible(Bundle options) {
        if (options == null) {
            return false;
        }
        int maxHeight = options.getInt("appWidgetMaxHeight");
        int maxWidth = options.getInt("appWidgetMaxWidth");
        int minHeight = options.getInt("appWidgetMinHeight");
        int minWidth = options.getInt("appWidgetMinWidth");
        if (maxHeight == 0 && maxWidth == 0 && minHeight == 0 && minWidth == 0) {
            return false;
        }
        return true;
    }

    private void clearWidget() {
        if (DEBUG) {
            AwareLog.d(TAG, "clearWidget");
        }
        synchronized (this.mWidgets) {
            for (Map.Entry<Integer, ArrayMap<Integer, Widget>> m : this.mWidgets.entrySet()) {
                ArrayMap<Integer, Widget> userWdigets = m.getValue();
                if (userWdigets != null) {
                    userWdigets.clear();
                }
            }
        }
        AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(0), null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00de  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0116  */
    private void initVisibleWindows() {
        boolean z;
        AwareProcessWindowInfo winInfo;
        boolean isEvil;
        AwareProcessWindowInfo winInfo2;
        List<Bundle> windowsList = HwWindowManager.getVisibleWindows(24);
        if (windowsList == null) {
            AwareLog.w(TAG, "Catch null when initVisibleWindows.");
            return;
        }
        synchronized (this.mVisibleWindows) {
            this.mVisibleWindows.clear();
            updateVisibleWindowsCache(2, -1, -1, null, -1, -1, false);
            for (Bundle windowState : windowsList) {
                int window = windowState.getInt("window_pid");
                int mode = windowState.getInt("window_value");
                int code = windowState.getInt("window_state");
                int width = windowState.getInt("window_width");
                int height = windowState.getInt("window_height");
                float alpha = windowState.getFloat("window_alpha");
                boolean z2 = windowState.getBoolean("window_hidden");
                String pkg = windowState.getString("window_package");
                int uid = windowState.getInt("window_uid");
                if (DEBUG) {
                    AwareLog.i(TAG, "initVisibleWindows pid:" + window + " mode:" + mode + " code:" + code + " width:" + width + " height:" + height);
                }
                if (!(width == AwareProcessWindowInfo.getMinWindowWidth() || height == AwareProcessWindowInfo.getMinWindowHeight())) {
                    if (alpha != GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                        z = false;
                        boolean isEvil2 = z;
                        winInfo = this.mVisibleWindows.get(Integer.valueOf(window));
                        if (winInfo != null) {
                            AwareProcessWindowInfo winInfo3 = new AwareProcessWindowInfo(mode, pkg, uid);
                            this.mVisibleWindows.put(Integer.valueOf(window), winInfo3);
                            AwareProcessWindowInfo winInfo4 = winInfo3;
                            String str = pkg;
                            int i = height;
                            int i2 = width;
                            updateVisibleWindowsCache(0, window, mode, pkg, uid, -1, false);
                            isEvil = isEvil2;
                            if (!isEvil) {
                                notifyVisibleWindowsChange(2, window, mode);
                            }
                            winInfo2 = winInfo4;
                        } else {
                            String str2 = pkg;
                            int i3 = height;
                            int i4 = width;
                            isEvil = isEvil2;
                            winInfo2 = winInfo;
                        }
                        winInfo2.addWindow(Integer.valueOf(code), isEvil);
                        AwareProcessWindowInfo awareProcessWindowInfo = winInfo2;
                        int intValue = Integer.valueOf(code).intValue();
                        boolean z3 = isEvil;
                        updateVisibleWindowsCache(4, window, -1, null, -1, intValue, isEvil);
                    }
                }
                z = true;
                boolean isEvil22 = z;
                winInfo = this.mVisibleWindows.get(Integer.valueOf(window));
                if (winInfo != null) {
                }
                winInfo2.addWindow(Integer.valueOf(code), isEvil);
                AwareProcessWindowInfo awareProcessWindowInfo2 = winInfo2;
                int intValue2 = Integer.valueOf(code).intValue();
                boolean z32 = isEvil;
                updateVisibleWindowsCache(4, window, -1, null, -1, intValue2, isEvil);
            }
        }
    }

    private void deinitVisibleWindows() {
        synchronized (this.mVisibleWindows) {
            this.mVisibleWindows.clear();
            notifyVisibleWindowsChange(0, -1, -1);
            updateVisibleWindowsCache(2, -1, -1, null, -1, -1, false);
        }
    }

    private void addWindow(int window, int mode, int code, int width, int height, float alpha, String pkg, int uid) {
        ArrayMap<Integer, AwareProcessWindowInfo> arrayMap;
        boolean isEvil;
        int i = window;
        int i2 = mode;
        int i3 = code;
        int i4 = width;
        int i5 = height;
        float f = alpha;
        AwareLog.i(TAG, "[addWindow]:" + i + " [mode]:" + i2 + " [code]:" + i3 + " width:" + i4 + " height:" + i5 + " alpha:" + f);
        if (i > 0) {
            ArrayMap<Integer, AwareProcessWindowInfo> arrayMap2 = this.mVisibleWindows;
            synchronized (arrayMap2) {
                try {
                    AwareProcessWindowInfo winInfo = this.mVisibleWindows.get(Integer.valueOf(window));
                    boolean isEvil2 = (i4 <= AwareProcessWindowInfo.getMinWindowWidth() && i4 > 0) || (i5 <= AwareProcessWindowInfo.getMinWindowHeight() && i5 > 0) || f == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                    if (winInfo == null) {
                        String str = pkg;
                        winInfo = new AwareProcessWindowInfo(i2, str, uid);
                        this.mVisibleWindows.put(Integer.valueOf(window), winInfo);
                        boolean isEvil3 = isEvil2;
                        arrayMap = arrayMap2;
                        try {
                            updateVisibleWindowsCache(0, i, i2, str, uid, -1, false);
                            isEvil = isEvil3;
                            if (!isEvil) {
                                notifyVisibleWindowsChange(2, i, i2);
                            }
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    } else {
                        arrayMap = arrayMap2;
                        isEvil = isEvil2;
                    }
                    winInfo.addWindow(Integer.valueOf(code), isEvil);
                    updateVisibleWindowsCache(4, i, -1, null, -1, Integer.valueOf(code).intValue(), isEvil);
                    AwareLog.i(TAG, "[addWindow]:" + i + " [mode]:" + i2 + " [code]:" + i3 + " isEvil:" + isEvil);
                    if (DEBUG) {
                        AwareLog.i(TAG, "[addVisibleWindows]:" + i + " [mode]:" + i2 + " [code]:" + i3);
                    }
                } catch (Throwable th2) {
                    th = th2;
                    arrayMap = arrayMap2;
                    throw th;
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0086, code lost:
        if (r11 == false) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x008a, code lost:
        if (r9.mScreenOff == false) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x008c, code lost:
        if (r12 != false) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008e, code lost:
        r1 = r9.mVisWinDurScreenOff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0090, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r9.mVisWinDurScreenOff.add(java.lang.Integer.valueOf(r10));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x009a, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a1, code lost:
        if (DEBUG == false) goto L_0x00c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a3, code lost:
        android.rms.iaware.AwareLog.d(TAG, "[removeVisibleWindows]:" + r10 + " [code]:" + r16);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00c4, code lost:
        r2 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c6, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00cc, code lost:
        r0 = th;
     */
    private void removeWindow(int window, int code) {
        int i = window;
        if (i > 0) {
            boolean removed = false;
            synchronized (this.mVisibleWindows) {
                try {
                    AwareProcessWindowInfo winInfo = this.mVisibleWindows.get(Integer.valueOf(i));
                    if (winInfo == null) {
                        this.mVisibleWindows.remove(Integer.valueOf(i));
                        updateVisibleWindowsCache(1, Integer.valueOf(i).intValue(), -1, null, -1, -1, false);
                        return;
                    }
                    boolean isEvil = winInfo.isEvil();
                    winInfo.removeWindow(Integer.valueOf(code));
                    updateVisibleWindowsCache(5, Integer.valueOf(i).intValue(), -1, null, -1, Integer.valueOf(code).intValue(), false);
                    if (winInfo.mWindows.size() == 0) {
                        this.mVisibleWindows.remove(Integer.valueOf(i));
                        updateVisibleWindowsCache(1, Integer.valueOf(i).intValue(), -1, null, -1, -1, false);
                        if (!isEvil) {
                            notifyVisibleWindowsChange(1, i, -1);
                        }
                        removed = true;
                    }
                } catch (Throwable th) {
                    th = th;
                    int i2 = code;
                    while (true) {
                        throw th;
                    }
                }
            }
        }
    }

    private void updateWindowOpsList() {
        synchronized (this.mVisibleWindows) {
            for (Map.Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                AwareProcessWindowInfo winInfo = window.getValue();
                int mode = ((AppOpsManager) this.mMtmService.context().getSystemService("appops")).checkOpNoThrow(24, winInfo.mUid, winInfo.mPkg);
                winInfo.mInRestriction = isInRestriction(winInfo.mMode, mode);
                winInfo.mMode = mode;
                updateVisibleWindowsCache(3, window.getKey().intValue(), mode, null, -1, -1, false);
            }
        }
    }

    private boolean isInRestriction(int oldmode, int newmode) {
        return (oldmode == 0 || oldmode == 3) && newmode == 1;
    }

    private void updateWindowOps(String pkgName) {
        String str = pkgName;
        if (this.mMtmService != null) {
            if (DEBUG) {
                AwareLog.d(TAG, "updateWindowOps pkg:" + str);
            }
            if (str == null) {
                updateWindowOpsList();
                return;
            }
            synchronized (this) {
                synchronized (this.mVisibleWindows) {
                    Iterator<Map.Entry<Integer, AwareProcessWindowInfo>> it = this.mVisibleWindows.entrySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Map.Entry next = it.next();
                        int pid = ((Integer) next.getKey()).intValue();
                        AwareProcessWindowInfo winInfo = (AwareProcessWindowInfo) next.getValue();
                        AssocBaseRecord record = this.mProcPidMap.get(Integer.valueOf(pid));
                        if (!(record == null || record.pkgList == null)) {
                            if (winInfo != null) {
                                if (record.pkgList.contains(str)) {
                                    AppOpsManager mAppOps = (AppOpsManager) this.mMtmService.context().getSystemService("appops");
                                    int mode = mAppOps.checkOpNoThrow(24, record.uid, str);
                                    winInfo.mMode = mode;
                                    AppOpsManager appOpsManager = mAppOps;
                                    int mode2 = mode;
                                    updateVisibleWindowsCache(3, pid, mode, null, -1, -1, false);
                                    if (!winInfo.isEvil()) {
                                        notifyVisibleWindowsChange(2, pid, mode2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00d6  */
    private void updateWindow(int window, int mode, int code, int width, int height, float alpha) {
        ArrayMap<Integer, AwareProcessWindowInfo> arrayMap;
        boolean z;
        boolean isEvil;
        boolean isEvil2;
        int i = window;
        int i2 = mode;
        int i3 = code;
        int i4 = width;
        int i5 = height;
        float f = alpha;
        AwareLog.i(TAG, "[updateWindow]:" + i + " [mode]:" + i2 + " [code]:" + i3 + " width:" + i4 + " height:" + i5 + " alpha:" + f);
        if (i > 0) {
            ArrayMap<Integer, AwareProcessWindowInfo> arrayMap2 = this.mVisibleWindows;
            synchronized (arrayMap2) {
                try {
                    AwareProcessWindowInfo winInfo = this.mVisibleWindows.get(Integer.valueOf(window));
                    if (i4 > AwareProcessWindowInfo.getMinWindowWidth() && i5 > AwareProcessWindowInfo.getMinWindowHeight()) {
                        if (f != GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                            z = false;
                            isEvil = z;
                            if (winInfo != null || !winInfo.containsWindow(i3)) {
                                isEvil2 = isEvil;
                                arrayMap = arrayMap2;
                            } else {
                                winInfo.addWindow(Integer.valueOf(code), isEvil);
                                isEvil2 = isEvil;
                                arrayMap = arrayMap2;
                                try {
                                    updateVisibleWindowsCache(4, i, -1, null, -1, Integer.valueOf(code).intValue(), isEvil2);
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            }
                            AwareLog.i(TAG, "[updateWindow]:" + i + " [mode]:" + i2 + " [code]:" + i3 + " isEvil:" + isEvil2);
                            if (DEBUG) {
                                AwareLog.i(TAG, "[updateWindow]:" + i + " [mode]:" + i2 + " [code]:" + i3);
                            }
                        }
                    }
                    z = true;
                    isEvil = z;
                    if (winInfo != null) {
                    }
                    isEvil2 = isEvil;
                    arrayMap = arrayMap2;
                    AwareLog.i(TAG, "[updateWindow]:" + i + " [mode]:" + i2 + " [code]:" + i3 + " isEvil:" + isEvil2);
                    if (DEBUG) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    arrayMap = arrayMap2;
                    throw th;
                }
            }
        }
    }

    private void reportHome(int pid, int uid, ArrayList<String> pkgname) {
        this.mHomeProcessPid = pid;
        this.mHomeProcessUid = uid;
        synchronized (this.mHomePackageList) {
            this.mHomePackageList.clear();
            if (pkgname != null && pkgname.size() > 0) {
                this.mHomePackageList.addAll(pkgname);
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
        return new AwareAppLruBase(this.mRecentTaskPrevBase.mPid, this.mRecentTaskPrevBase.mUid, this.mRecentTaskPrevBase.mTime);
    }

    public AwareAppLruBase getPreviousAppInfo() {
        return new AwareAppLruBase(this.mPrevNonHomeBase.mPid, this.mPrevNonHomeBase.mUid, this.mPrevNonHomeBase.mTime);
    }

    public AwareAppLruBase getPreviousByAmsInfo() {
        return new AwareAppLruBase(this.mAmsPrevBase.mPid, this.mAmsPrevBase.mUid, this.mAmsPrevBase.mTime);
    }

    private boolean checkType(int type) {
        switch (type) {
            case 1:
                return true;
            case 2:
                return true;
            case 3:
                return true;
            default:
                return false;
        }
    }

    private String typeToString(int type) {
        switch (type) {
            case 1:
                return "ADD_ASSOC_BINDSERVICE";
            case 2:
                return "ADD_ASSOC_PROVIDER";
            case 3:
                return "DEL_ASSOC_BINDSERVICE";
            case 4:
                return "APP_ASSOC_PROCESSUPDATE";
            default:
                return "[Error type]" + type;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00e9, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x011a, code lost:
        return;
     */
    private void addProcessRelation(int callerPid, int callerUid, String callerName, int targetUid, String targetName, String comp, int type) {
        if (checkType(type)) {
            if (callerUid == targetUid) {
                if (DEBUG) {
                    AwareLog.i(TAG, typeToString(type) + " in the same UID.Pass.");
                }
            } else if (callerPid <= 0 || callerUid <= 0 || targetUid <= 0) {
                if (DEBUG) {
                    AwareLog.i(TAG, typeToString(type) + " with wrong pid or uid");
                }
            } else if (callerName == null || targetName == null) {
                if (DEBUG) {
                    AwareLog.i(TAG, typeToString(type) + " with wrong callerName or targetName");
                }
            } else {
                if (comp == null) {
                    comp = "NULL";
                }
                String comp2 = comp;
                if (DEBUG) {
                    AwareLog.i(TAG, typeToString(type) + ". Caller[Pid:" + callerPid + "][Uid:" + callerUid + "][Name:" + callerName + "] Target[Uid:" + targetUid + "][pName:" + targetName + "][hash:" + comp2 + "]");
                }
                int targetPid = 0;
                if (targetUid != 1000 || !targetName.equals(SYSTEM)) {
                    synchronized (this) {
                        AssocBaseRecord br = (AssocBaseRecord) this.mProcInfoMap.get(targetName, targetUid);
                        if (br != null) {
                            targetPid = br.pid;
                        }
                        AssocPidRecord pidRecord = this.mAssocRecordMap.get(Integer.valueOf(callerPid));
                        if (pidRecord == null) {
                            AssocPidRecord pidRecord2 = new AssocPidRecord(callerPid, callerUid, callerName);
                            AssocBaseRecord baseRecord = new AssocBaseRecord(targetName, targetUid, targetPid);
                            baseRecord.mComponents.add(comp2);
                            ProcessMap<AssocBaseRecord> relations = pidRecord2.getMap(type);
                            if (relations != null) {
                                relations.put(targetName, targetUid, baseRecord);
                                this.mAssocRecordMap.put(Integer.valueOf(callerPid), pidRecord2);
                            } else if (DEBUG) {
                                AwareLog.e(TAG, "Error type:" + type);
                            }
                        } else {
                            ProcessMap<AssocBaseRecord> relations2 = pidRecord.getMap(type);
                            if (relations2 != null) {
                                AssocBaseRecord baseRecord2 = (AssocBaseRecord) relations2.get(targetName, targetUid);
                                if (baseRecord2 == null) {
                                    AssocBaseRecord baseRecord3 = new AssocBaseRecord(targetName, targetUid, targetPid);
                                    baseRecord3.mComponents.add(comp2);
                                    relations2.put(targetName, targetUid, baseRecord3);
                                    return;
                                }
                                baseRecord2.miniTime = SystemClock.elapsedRealtime();
                                baseRecord2.isStrong = true;
                                baseRecord2.mComponents.add(comp2);
                            } else if (DEBUG) {
                                AwareLog.e(TAG, "Error type:" + type);
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00c8, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00fb, code lost:
        return;
     */
    private void removeProcessRelation(int callerPid, int callerUid, String callerName, int targetUid, String targetName, String comp, int type) {
        if (checkType(type)) {
            if (callerUid == targetUid) {
                if (DEBUG) {
                    AwareLog.i(TAG, typeToString(type) + " in the same UID.Pass.");
                }
            } else if (callerPid <= 0 || callerUid <= 0 || targetUid <= 0) {
                if (DEBUG) {
                    AwareLog.i(TAG, typeToString(type) + " with wrong pid or uid");
                }
            } else if (targetName == null) {
                if (DEBUG) {
                    AwareLog.i(TAG, typeToString(type) + " with wrong targetName");
                }
            } else {
                if (DEBUG) {
                    AwareLog.i(TAG, typeToString(type) + ". Caller[Pid:" + callerPid + "] target[" + targetUid + ":" + targetName + ":" + comp + "]");
                }
                if (comp == null) {
                    comp = "NULL";
                }
                String comp2 = comp;
                synchronized (this) {
                    AssocPidRecord pr = this.mAssocRecordMap.get(Integer.valueOf(callerPid));
                    if (pr != null) {
                        ProcessMap<AssocBaseRecord> relations = pr.getMap(type);
                        if (relations != null) {
                            AssocBaseRecord br = (AssocBaseRecord) relations.get(targetName, targetUid);
                            if (br != null && br.mComponents.contains(comp2)) {
                                br.mComponents.remove(comp2);
                                if (br.mComponents.isEmpty()) {
                                    relations.remove(targetName, targetUid);
                                    if (pr.isEmpty()) {
                                        this.mAssocRecordMap.remove(Integer.valueOf(pr.pid));
                                    }
                                }
                            }
                        } else if (DEBUG) {
                            AwareLog.e(TAG, "Error type:" + type);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeDiedProcessRelation(int pid, int uid) {
        if (pid <= 0 || uid <= 0) {
            if (DEBUG) {
                AwareLog.i(TAG, "removeDiedProcessRelation with wrong pid or uid");
            }
            return;
        }
        if (DEBUG) {
            AwareLog.i(TAG, "remove died. Pid:" + pid + " Uid:" + uid);
        }
        synchronized (this) {
            AssocBaseRecord br = this.mProcPidMap.remove(Integer.valueOf(pid));
            if (br != null) {
                this.mProcInfoMap.remove(br.processName, br.uid);
                if (br.pkgList != null) {
                    Iterator<String> it = br.pkgList.iterator();
                    while (it.hasNext()) {
                        String pkg = it.next();
                        synchronized (this.mProcPkgMap) {
                            ArraySet<Integer> pids = this.mProcPkgMap.get(pkg);
                            if (pids != null && pids.contains(Integer.valueOf(pid))) {
                                pids.remove(Integer.valueOf(pid));
                                if (pids.isEmpty()) {
                                    this.mProcPkgMap.remove(pkg);
                                }
                            }
                        }
                    }
                }
            }
            ArraySet<Integer> pids2 = this.mProcUidMap.get(Integer.valueOf(uid));
            if (pids2 != null && pids2.contains(Integer.valueOf(pid))) {
                pids2.remove(Integer.valueOf(pid));
                if (pids2.isEmpty()) {
                    this.mProcUidMap.remove(Integer.valueOf(uid));
                }
            }
            Iterator<Map.Entry<Integer, AssocPidRecord>> it2 = this.mAssocRecordMap.entrySet().iterator();
            while (it2.hasNext()) {
                AssocPidRecord record = (AssocPidRecord) it2.next().getValue();
                if (record.pid == pid) {
                    it2.remove();
                } else {
                    if (br != null) {
                        record.mAssocBindService.remove(br.processName, br.uid);
                        record.mAssocProvider.remove(br.processName, br.uid);
                    }
                    if (record.isEmpty()) {
                        it2.remove();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeDiedRecordProc(int uid, int pid) {
        if (uid <= 0) {
            AwareLog.i(TAG, "removeDiedRecodrProc with wrong pid or uid");
        } else {
            AwareIntelligentRecg.getInstance().removeDiedScreenProc(uid, pid);
        }
    }

    private void updateProcLaunchData(int uid, String proc, ArrayList<String> pkgList) {
        if (AppCleanupFeature.isAppCleanEnable() && UserHandle.getAppId(uid) >= 10000 && !UserHandle.isIsolated(uid)) {
            if (DEBUG) {
                AwareLog.i(TAG, "updateProcLaunchData, proc: " + proc + ", uid: " + uid + ", pkgList: " + pkgList);
            }
            synchronized (this.mProcLaunchMap) {
                int userId = UserHandle.getUserId(uid);
                Map<String, Map<String, LaunchData>> pkgMap = this.mProcLaunchMap.get(Integer.valueOf(userId));
                if (pkgMap == null) {
                    pkgMap = new HashMap<>();
                    this.mProcLaunchMap.put(Integer.valueOf(userId), pkgMap);
                }
                Iterator<String> it = pkgList.iterator();
                while (it.hasNext()) {
                    String pkg = it.next();
                    if (pkg != null) {
                        Map<String, LaunchData> procMap = pkgMap.get(pkg);
                        if (procMap == null) {
                            procMap = new HashMap<>();
                            pkgMap.put(pkg, procMap);
                        }
                        LaunchData launchData = procMap.get(proc);
                        if (launchData != null) {
                            procMap.put(proc, launchData.increase());
                            if (DEBUG) {
                                AwareLog.i(TAG, "updateProcLaunchData, pkg: " + pkg + ", launcTimes: " + launchData.getLaunchTimes());
                            }
                            if (launchData.getLaunchTimes() >= 30) {
                                if (SystemClock.elapsedRealtime() - launchData.getFirstTime() <= HwArbitrationDEFS.DelayTimeMillisB) {
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
                            }
                        } else {
                            LaunchData launchData2 = new LaunchData(1, SystemClock.elapsedRealtime());
                            procMap.put(proc, launchData2);
                            if (DEBUG) {
                                AwareLog.i(TAG, "updateProcLaunchData, pkg: " + pkg + ", launcTimes: " + launchData2.getLaunchTimes());
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateProcessRelation(int pid, int uid, String name, ArrayList<String> pkgList) {
        if (pid <= 0 || uid <= 0) {
            if (DEBUG) {
                AwareLog.i(TAG, "updateProcessRelation with wrong pid or uid");
            }
        } else if (name == null || pkgList == null) {
            if (DEBUG) {
                AwareLog.i(TAG, "updateProcessRelation with wrong name");
            }
        } else {
            if (DEBUG) {
                AwareLog.i(TAG, "update relation. Pid:" + pid + " Uid:" + uid + ",ProcessName:" + name);
            }
            updateProcLaunchData(uid, name, pkgList);
            synchronized (this) {
                Iterator<Map.Entry<Integer, AssocPidRecord>> it = this.mAssocRecordMap.entrySet().iterator();
                while (it.hasNext()) {
                    AssocPidRecord record = (AssocPidRecord) it.next().getValue();
                    if (record.pid == pid) {
                        it.remove();
                    } else {
                        AssocBaseRecord br = (AssocBaseRecord) record.mAssocBindService.get(name, uid);
                        if (br != null) {
                            br.pid = pid;
                        }
                        AssocBaseRecord br2 = (AssocBaseRecord) record.mAssocProvider.get(name, uid);
                        if (br2 != null) {
                            br2.pid = pid;
                        }
                    }
                }
                AssocBaseRecord br3 = this.mProcPidMap.get(Integer.valueOf(pid));
                if (br3 == null) {
                    AssocBaseRecord br4 = new AssocBaseRecord(name, uid, pid);
                    br4.pkgList.addAll(pkgList);
                    this.mProcPidMap.put(Integer.valueOf(pid), br4);
                } else {
                    br3.processName = name;
                    br3.uid = uid;
                    br3.pid = pid;
                    br3.pkgList.addAll(pkgList);
                }
                AssocBaseRecord br5 = (AssocBaseRecord) this.mProcInfoMap.get(name, uid);
                if (br5 == null) {
                    this.mProcInfoMap.put(name, uid, new AssocBaseRecord(name, uid, pid));
                } else {
                    br5.pid = pid;
                }
                ArraySet<Integer> pids = this.mProcUidMap.get(Integer.valueOf(uid));
                if (pids == null) {
                    ArraySet arraySet = new ArraySet();
                    arraySet.add(Integer.valueOf(pid));
                    this.mProcUidMap.put(Integer.valueOf(uid), arraySet);
                } else {
                    pids.add(Integer.valueOf(pid));
                }
                int listSize = pkgList.size();
                for (int i = 0; i < listSize; i++) {
                    String pkg = pkgList.get(i);
                    synchronized (this.mProcPkgMap) {
                        ArraySet<Integer> pids2 = this.mProcPkgMap.get(pkg);
                        if (pids2 == null) {
                            ArraySet arraySet2 = new ArraySet();
                            arraySet2.add(Integer.valueOf(pid));
                            this.mProcPkgMap.put(pkg, arraySet2);
                        } else {
                            pids2.add(Integer.valueOf(pid));
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkAndInitWidgetObj(int userId) {
        synchronized (this.mWidgets) {
            if (this.mWidgets.get(Integer.valueOf(userId)) == null) {
                this.mWidgets.put(Integer.valueOf(userId), new ArrayMap());
            }
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
            UserInfo currentUser = ActivityManagerNative.getDefault().getCurrentUser();
            if (currentUser != null) {
                checkAndInitWidgetObj(currentUser.id);
                this.mCurUserId = currentUser.id;
                this.mCurSwitchUser = currentUser.id;
            }
            ActivityManagerNative.getDefault().registerUserSwitchObserver(this.mUserSwitchObserver, TAG);
        } catch (RemoteException e) {
            AwareLog.d(TAG, "Activity manager not running, initSwitchUser error!");
        }
    }

    private void deInitSwitchUser() {
        try {
            ActivityManagerNative.getDefault().unregisterUserSwitchObserver(this.mUserSwitchObserver);
        } catch (RemoteException e) {
            AwareLog.d(TAG, "Activity manager not running, deInitSwitchUser error!");
        }
    }

    /* access modifiers changed from: private */
    public void initialize() {
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
            if (this.mMtmService != null) {
                initAssoc();
                registerProcessObserver();
                this.mIsInitialized.set(true);
            } else if (DEBUG) {
                AwareLog.w(TAG, "MultiTaskManagerService has not been started.");
            }
        }
    }

    private boolean isUserUnlocked() {
        if (this.mMtmService == null) {
            return false;
        }
        UserManager userManager = (UserManager) this.mMtmService.context().getSystemService("user");
        if (userManager == null) {
            return false;
        }
        return userManager.isUserUnlocked();
    }

    private synchronized void deInitialize() {
        if (this.mIsInitialized.get()) {
            unregisterProcessObserver();
            if (this.mMtmService != null) {
                this.mMtmService = null;
            }
            HwActivityManager.reportAssocDisable();
            deinitVisibleWindows();
            clearWidget();
            deinitAssoc();
            this.mIsInitialized.set(false);
        }
    }

    private void initAssoc() {
        if (this.mHwAMS != null) {
            synchronized (this) {
                AssocBaseRecord br = new AssocBaseRecord(SYSTEM, 1000, this.mMyPid);
                this.mProcPidMap.put(Integer.valueOf(this.mMyPid), br);
                this.mProcInfoMap.put(SYSTEM, 1000, br);
                ArraySet<Integer> pids = new ArraySet<>();
                pids.add(Integer.valueOf(this.mMyPid));
                this.mProcUidMap.put(1000, pids);
            }
            initSwitchUser();
            initVisibleWindows();
            updateWidgets(this.mCurUserId);
            if (this.mCurUserId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(this.mCurUserId), null);
            }
            ArrayMap<Integer, Integer> forePids = new ArrayMap<>();
            this.mHwAMS.reportAssocEnable(forePids);
            AwareIntelligentRecg.getInstance().onFgPidInfosInit(forePids);
            synchronized (this.mForePids) {
                this.mForePids.clear();
                this.mForePids.putAll(forePids);
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
        synchronized (this) {
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

    public void getPidsByUid(int uid, Set<Integer> pids) {
        if (mEnabled && uid > 0 && pids != null) {
            synchronized (this) {
                ArraySet<Integer> procPids = this.mProcUidMap.get(Integer.valueOf(uid));
                if (procPids != null) {
                    pids.addAll(procPids);
                }
            }
        }
    }

    public int getPidByNameAndUid(String procName, int uid) {
        if (!mEnabled || uid <= 0 || procName == null || procName.isEmpty()) {
            return -1;
        }
        synchronized (this) {
            AssocBaseRecord br = (AssocBaseRecord) this.mProcInfoMap.get(procName, uid);
            if (br == null) {
                return -1;
            }
            int i = br.pid;
            return i;
        }
    }

    /* access modifiers changed from: private */
    public String sameUid(int pid) {
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        synchronized (this) {
            AssocBaseRecord br = this.mProcPidMap.get(Integer.valueOf(pid));
            if (br == null) {
                return null;
            }
            ArraySet<Integer> pids = this.mProcUidMap.get(Integer.valueOf(br.uid));
            if (pids == null) {
                return null;
            }
            Iterator<Integer> it = pids.iterator();
            while (it.hasNext()) {
                int tmp = it.next().intValue();
                if (tmp != pid) {
                    if (flag) {
                        sb.append("    [SameUID] depend on:\n");
                        flag = false;
                    }
                    AssocBaseRecord br2 = this.mProcPidMap.get(Integer.valueOf(tmp));
                    if (br2 != null) {
                        sb.append("        Pid:");
                        sb.append(br2.pid);
                        sb.append(",Uid:");
                        sb.append(br2.uid);
                        sb.append(",ProcessName:");
                        sb.append(br2.processName);
                        sb.append("\n");
                    }
                }
            }
            return sb.toString();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0020, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005f, code lost:
        return r0;
     */
    private Set<String> getPackageNameForUid(int uid, int pidForUid) {
        ArraySet<String> pkgList = new ArraySet<>();
        synchronized (this) {
            if (pidForUid != 0) {
                try {
                    AssocBaseRecord br = this.mProcPidMap.get(Integer.valueOf(pidForUid));
                    if (!(br == null || br.pkgList == null)) {
                        pkgList.addAll(br.pkgList);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                ArraySet<Integer> pids = this.mProcUidMap.get(Integer.valueOf(uid));
                if (pids != null) {
                    if (!pids.isEmpty()) {
                        Iterator<Integer> it = pids.iterator();
                        while (it.hasNext()) {
                            AssocBaseRecord br2 = this.mProcPidMap.get(it.next());
                            if (!(br2 == null || br2.pkgList == null)) {
                                pkgList.addAll(br2.pkgList);
                            }
                        }
                        return pkgList;
                    }
                }
            }
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            synchronized (this) {
                int listSize = this.mAssocRecordMap.size();
                for (int s = 0; s < listSize; s++) {
                    AssocPidRecord record = this.mAssocRecordMap.valueAt(s);
                    if (record != null) {
                        pw.println(record);
                    }
                }
            }
            dumpWidget(pw);
            dumpVisibleWindow(pw);
        }
    }

    public void dumpFore(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            ArraySet<Integer> tmp = new ArraySet<>();
            synchronized (this.mForePids) {
                tmp.addAll(this.mForePids.keySet());
            }
            Iterator<Integer> it = tmp.iterator();
            while (it.hasNext()) {
                dumpPid(it.next().intValue(), pw);
            }
        }
    }

    public void dumpRecentFore(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            ArraySet<Integer> tmp = new ArraySet<>();
            synchronized (this.mBgRecentForcePids) {
                tmp.addAll(this.mBgRecentForcePids.keySet());
            }
            Iterator<Integer> it = tmp.iterator();
            while (it.hasNext()) {
                dumpPid(it.next().intValue(), pw);
            }
        }
    }

    public void dumpPkgProc(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
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
                        for (Map.Entry<String, Map<String, LaunchData>> pkgEntry : pkgMap.entrySet()) {
                            pw.println("    pkg: " + pkgEntry.getKey());
                            Map<String, LaunchData> procMap = pkgEntry.getValue();
                            if (procMap != null) {
                                for (Map.Entry<String, LaunchData> procEntry : procMap.entrySet()) {
                                    LaunchData lData = procEntry.getValue();
                                    if (lData != null) {
                                        pw.println("      proc: " + procEntry.getKey() + ", launchTime: " + lData.getLaunchTimes());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void dumpPid(int pid, PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            synchronized (this) {
                AssocPidRecord record = this.mAssocRecordMap.get(Integer.valueOf(pid));
                if (record != null) {
                    pw.println(record);
                } else {
                    AssocBaseRecord br = this.mProcPidMap.get(Integer.valueOf(pid));
                    if (br != null) {
                        pw.println("Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                    }
                    pw.println(sameUid(pid));
                }
            }
        }
    }

    public void dumpVisibleWindow(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            ArraySet<Integer> windows = new ArraySet<>();
            ArraySet<Integer> windowsEvil = new ArraySet<>();
            getVisibleWindows(windows, windowsEvil);
            boolean flag = true;
            pw.println("");
            synchronized (this) {
                Iterator<Integer> it = windows.iterator();
                while (it.hasNext()) {
                    AssocBaseRecord br = this.mProcPidMap.get(Integer.valueOf(it.next().intValue()));
                    if (br != null) {
                        if (flag) {
                            pw.println("[WindowList] :");
                            flag = false;
                        }
                        pw.println("    Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName + ",PkgList:" + br.pkgList);
                    }
                }
                boolean flag2 = true;
                Iterator<Integer> it2 = windowsEvil.iterator();
                while (it2.hasNext()) {
                    AssocBaseRecord br2 = this.mProcPidMap.get(Integer.valueOf(it2.next().intValue()));
                    if (br2 != null) {
                        if (flag2) {
                            pw.println("[WindowEvilList] :");
                            flag2 = false;
                        }
                        pw.println("    Pid:" + br2.pid + ",Uid:" + br2.uid + ",ProcessName:" + br2.processName + ",PkgList:" + br2.pkgList);
                    }
                }
            }
            ArraySet<Integer> windowsClean = new ArraySet<>();
            getVisibleWindowsInRestriction(windowsClean);
            pw.println("[WindowList in restriction] :" + windowsClean);
        }
    }

    public void dumpWidget(PrintWriter pw) {
        if (pw != null) {
            if (!mEnabled) {
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
            if (!mEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            synchronized (this) {
                AssocBaseRecord br = this.mProcPidMap.get(Integer.valueOf(this.mHomeProcessPid));
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
            if (!mEnabled) {
                pw.println("AwareAppAssociate feature disabled.");
                return;
            }
            int pid = 0;
            Set<String> pkgList = getPackageNameForUid(this.mPrevNonHomeBase.mUid, isDealAsPkgUid(this.mPrevNonHomeBase.mUid) ? this.mPrevNonHomeBase.mPid : 0);
            if (this.mPrevNonHomeBase.mUid == 0) {
                eclipseTime = " none";
            } else {
                eclipseTime = " " + ((SystemClock.elapsedRealtime() - this.mPrevNonHomeBase.mTime) / 1000);
            }
            pw.println("[Prev Non Home] Uid:" + this.mPrevNonHomeBase.mUid + ",pid:" + this.mPrevNonHomeBase.mPid + ",pkg:" + pkgList + ",eclipse(s):" + eclipseTime);
            boolean isRecentTaskShow = false;
            synchronized (this.mForePids) {
                if (this.mForePids.isEmpty()) {
                    isRecentTaskShow = true;
                }
            }
            if (isRecentTaskShow) {
                Set<String> pkgList2 = getPackageNameForUid(this.mRecentTaskPrevBase.mUid, isDealAsPkgUid(this.mRecentTaskPrevBase.mUid) ? this.mRecentTaskPrevBase.mPid : 0);
                pw.println("[Prev Recent Task] Uid:" + this.mRecentTaskPrevBase.mUid + ",pid:" + this.mRecentTaskPrevBase.mPid + ",pkg:" + pkgList2);
            } else {
                pw.println("[Prev Recent Task] Uid: None");
            }
            if (isDealAsPkgUid(this.mAmsPrevBase.mUid)) {
                pid = this.mAmsPrevBase.mPid;
            }
            Set<String> pkgList3 = getPackageNameForUid(this.mAmsPrevBase.mUid, pid);
            if (this.mAmsPrevBase.mUid == 0) {
                eclipseTime2 = " none";
            } else {
                eclipseTime2 = " " + ((SystemClock.elapsedRealtime() - this.mAmsPrevBase.mTime) / 1000);
            }
            pw.println("[Prev By Ams] Uid:" + this.mAmsPrevBase.mUid + ",pid:" + this.mAmsPrevBase.mPid + ",pkg:" + pkgList3 + ",eclipse(s):" + eclipseTime2);
        }
    }

    public void dumpRecord(PrintWriter pw) {
        ArraySet<Integer> windows;
        int bindSizeAll;
        int bindSize;
        ArraySet<Integer> windowsEvil;
        ArraySet<Integer> pids;
        int curpidsize;
        int curcompsize;
        int curpiduidsize;
        int pidsize;
        PrintWriter printWriter = pw;
        if (printWriter != null) {
            if (!mEnabled) {
                printWriter.println("AwareAppAssociate feature disabled.");
                return;
            }
            Set<String> widgets = getWidgetsPkg();
            printWriter.println("Widget Size: " + widgets.size());
            ArraySet<Integer> windows2 = new ArraySet<>();
            ArraySet<Integer> windowsEvil2 = new ArraySet<>();
            getVisibleWindows(windows2, windowsEvil2);
            printWriter.println("Window Size: " + windows2.size() + ", EvilWindow Size: " + windowsEvil2.size());
            synchronized (this) {
                try {
                    int pidSize = this.mAssocRecordMap.size();
                    int compSize = 0;
                    int compSize2 = 0;
                    int s = 0;
                    while (s < pidSize) {
                        int providerSize = 0;
                        int providerSizeAll = 0;
                        int sameuid = 0;
                        try {
                            AssocPidRecord record = this.mAssocRecordMap.valueAt(s);
                            Set<String> widgets2 = widgets;
                            try {
                                int NP = record.mAssocBindService.getMap().size();
                                windows = windows2;
                                bindSizeAll = 0;
                                bindSize = 0;
                                int i = 0;
                                while (i < NP) {
                                    int NP2 = NP;
                                    try {
                                        SparseArray<AssocBaseRecord> brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                                        ArraySet<Integer> windowsEvil3 = windowsEvil2;
                                        int NB = brs.size();
                                        bindSize += NB;
                                        int bindSizeAll2 = bindSizeAll;
                                        int j = 0;
                                        while (j < NB) {
                                            bindSizeAll2 += brs.valueAt(j).mComponents.size();
                                            j++;
                                            brs = brs;
                                            NB = NB;
                                        }
                                        i++;
                                        NP = NP2;
                                        bindSizeAll = bindSizeAll2;
                                        windowsEvil2 = windowsEvil3;
                                    } catch (Throwable th) {
                                        th = th;
                                        int i2 = compSize2;
                                        int pidsize2 = compSize;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                                int i3 = NP;
                                windowsEvil = windowsEvil2;
                                int NP3 = record.mAssocProvider.getMap().size();
                                int i4 = 0;
                                while (i4 < NP3) {
                                    SparseArray<AssocBaseRecord> brs2 = (SparseArray) record.mAssocProvider.getMap().valueAt(i4);
                                    int NP4 = NP3;
                                    int NB2 = brs2.size();
                                    providerSize += NB2;
                                    int providerSizeAll2 = providerSizeAll;
                                    int j2 = 0;
                                    while (j2 < NB2) {
                                        providerSizeAll2 += brs2.valueAt(j2).mComponents.size();
                                        j2++;
                                        NB2 = NB2;
                                        brs2 = brs2;
                                    }
                                    i4++;
                                    providerSizeAll = providerSizeAll2;
                                    NP3 = NP4;
                                }
                                pids = this.mProcUidMap.get(Integer.valueOf(record.uid));
                                if (pids != null) {
                                    sameuid = pids.size() - 1;
                                }
                                curpidsize = bindSize + providerSize;
                                curcompsize = bindSizeAll + providerSizeAll;
                                curpiduidsize = curpidsize + sameuid;
                                compSize += curcompsize;
                                pidsize = compSize2 + curpidsize;
                            } catch (Throwable th2) {
                                th = th2;
                                ArraySet<Integer> arraySet = windows2;
                                ArraySet<Integer> arraySet2 = windowsEvil2;
                                int i5 = compSize2;
                                int pidsize3 = compSize;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                            try {
                                StringBuilder sb = new StringBuilder();
                                ArraySet<Integer> arraySet3 = pids;
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
                                sb.append(sameuid);
                                sb.append("]pids:[");
                                sb.append(curpidsize);
                                sb.append("]comps:[");
                                sb.append(curcompsize);
                                sb.append("]piduids:[");
                                sb.append(curpiduidsize);
                                sb.append("]");
                                printWriter.println(sb.toString());
                                s++;
                                widgets = widgets2;
                                windows2 = windows;
                                windowsEvil2 = windowsEvil;
                                compSize2 = pidsize;
                            } catch (Throwable th3) {
                                th = th3;
                                int i6 = compSize;
                                int i7 = pidsize;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            Set<String> set = widgets;
                            ArraySet<Integer> arraySet4 = windows2;
                            ArraySet<Integer> arraySet5 = windowsEvil2;
                            int i8 = compSize2;
                            int pidsize4 = compSize;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    Set<String> set2 = widgets;
                    ArraySet<Integer> arraySet6 = windows2;
                    ArraySet<Integer> arraySet7 = windowsEvil2;
                    printWriter.println("PidRecord Size: " + pidSize + " " + compSize2 + " " + compSize);
                } catch (Throwable th5) {
                    th = th5;
                    Set<String> set3 = widgets;
                    ArraySet<Integer> arraySet8 = windows2;
                    ArraySet<Integer> arraySet9 = windowsEvil2;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
    }

    private void recordWindowDetail(Set<Integer> list) {
        if (list != null && !list.isEmpty()) {
            synchronized (this) {
                for (Integer intValue : list) {
                    AssocBaseRecord br = this.mProcPidMap.get(Integer.valueOf(intValue.intValue()));
                    if (br != null) {
                        AwareLog.i(TAG, "[Window]Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                    }
                }
            }
        }
    }

    private void recordAssocDetail(int pid) {
        synchronized (this) {
            AssocPidRecord record = this.mAssocRecordMap.get(Integer.valueOf(pid));
            if (record != null) {
                AwareLog.i(TAG, "" + record);
            } else {
                AssocBaseRecord br = this.mProcPidMap.get(Integer.valueOf(pid));
                if (br != null) {
                    AwareLog.i(TAG, "Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                }
                AwareLog.i(TAG, "" + sameUid(pid));
            }
        }
    }

    public static void enable() {
        mEnabled = true;
        if (mAwareAppAssociate != null) {
            mAwareAppAssociate.initialize();
        }
    }

    public static void disable() {
        mEnabled = false;
        if (mAwareAppAssociate != null) {
            mAwareAppAssociate.deInitialize();
        }
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    public static void enableRecord() {
        RECORD = true;
    }

    public static void disableRecord() {
        RECORD = false;
    }

    public boolean isPkgHasProc(String pkg) {
        boolean z = true;
        if (!mEnabled) {
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
        this.mScreenOff = screenOff;
        if (screenOff && this.mHandler != null) {
            this.mHandler.removeMessages(4);
        }
    }

    /* access modifiers changed from: private */
    public void clearRemoveVisWinDurScreenOff() {
        synchronized (this.mVisWinDurScreenOff) {
            if (!this.mVisWinDurScreenOff.isEmpty()) {
                this.mVisWinDurScreenOff.clear();
            }
        }
    }

    public void checkBakUpVisWin() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(4);
            this.mHandler.sendEmptyMessageDelayed(4, 5000);
        }
    }

    private void updateVisibleWindowsCache(int type, int pid, int mode, String pkg, int uid, int code, boolean evil) {
        switch (type) {
            case 0:
                AwareProcessWindowInfo winInfoCache = new AwareProcessWindowInfo(mode, pkg, uid);
                synchronized (this.mVisibleWindowsCache) {
                    this.mVisibleWindowsCache.put(Integer.valueOf(pid), winInfoCache);
                }
                return;
            case 1:
                synchronized (this.mVisibleWindowsCache) {
                    this.mVisibleWindowsCache.remove(Integer.valueOf(pid));
                }
                return;
            case 2:
                synchronized (this.mVisibleWindowsCache) {
                    this.mVisibleWindowsCache.clear();
                }
                return;
            case 3:
                synchronized (this.mVisibleWindowsCache) {
                    AwareProcessWindowInfo winInfo = this.mVisibleWindowsCache.get(Integer.valueOf(pid));
                    if (winInfo != null) {
                        winInfo.mMode = mode;
                    }
                }
                return;
            case 4:
                synchronized (this.mVisibleWindowsCache) {
                    AwareProcessWindowInfo winInfo2 = this.mVisibleWindowsCache.get(Integer.valueOf(pid));
                    if (winInfo2 != null) {
                        winInfo2.addWindow(Integer.valueOf(code), evil);
                    }
                }
                return;
            case 5:
                synchronized (this.mVisibleWindowsCache) {
                    AwareProcessWindowInfo winInfo3 = this.mVisibleWindowsCache.get(Integer.valueOf(pid));
                    if (winInfo3 != null) {
                        winInfo3.removeWindow(Integer.valueOf(code));
                    }
                }
                return;
            default:
                return;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0030, code lost:
        return false;
     */
    public boolean isVisibleWindow(int pid) {
        boolean allowedWindow;
        if (!mEnabled) {
            return false;
        }
        synchronized (this.mVisibleWindowsCache) {
            AwareProcessWindowInfo winInfo = this.mVisibleWindowsCache.get(Integer.valueOf(pid));
            if (winInfo != null) {
                if (winInfo.mMode != 0) {
                    if (winInfo.mMode != 3) {
                        allowedWindow = false;
                        if (allowedWindow && !winInfo.isEvil()) {
                            return true;
                        }
                    }
                }
                allowedWindow = true;
                return true;
            }
        }
    }

    public Set<AwareAppLruBase> getPreviousAppOpt() {
        Set<AwareAppLruBase> previousApp = new ArraySet<>();
        ArraySet<Integer> foregroundPids = new ArraySet<>();
        synchronized (this.mForePids) {
            foregroundPids.addAll(this.mForePids.keySet());
        }
        int previousCount = checkIfNeedMorePreviousNow(foregroundPids) ? 2 : 1;
        LinkedHashMap<Integer, AwareAppLruBase> lruCache = getActivityLruCache();
        ListIterator<Map.Entry<Integer, AwareAppLruBase>> iter = new ArrayList(lruCache.entrySet()).listIterator(lruCache.size());
        synchronized (this) {
            while (true) {
                if (!iter.hasPrevious()) {
                    break;
                }
                Map.Entry<Integer, AwareAppLruBase> entry = iter.previous();
                if (previousCount <= 0) {
                    break;
                }
                AwareAppLruBase app = entry.getValue();
                AssocBaseRecord br = this.mProcPidMap.get(Integer.valueOf(app.mPid));
                if (!foregroundPids.contains(Integer.valueOf(app.mPid))) {
                    if (br == null || br.pkgList == null || !br.pkgList.contains("com.android.systemui")) {
                        if (app.mPid != this.mHomeProcessPid) {
                            previousApp.add(app);
                            int previousCount2 = previousCount - 1;
                            if (this.mMorePreviousLevel == 2 && br != null && br.pkgList != null && !br.pkgList.contains(AwareIntelligentRecg.getInstance().getDefaultSmsPackage())) {
                                previousCount2--;
                            }
                        }
                    }
                }
            }
        }
        return previousApp;
    }

    private boolean checkIfNeedMorePreviousNow(ArraySet<Integer> foregroundPids) {
        if (this.mMorePreviousLevel == 0 || this.mMorePreviousLevel > 2) {
            return false;
        }
        boolean needMorePrevious = false;
        synchronized (this) {
            Iterator<Integer> it = foregroundPids.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Integer forePid = it.next();
                if (forePid != null) {
                    if (this.mHomeProcessPid == forePid.intValue()) {
                        needMorePrevious = true;
                        break;
                    }
                    AssocBaseRecord br = this.mProcPidMap.get(forePid);
                    if (br == null) {
                        continue;
                    } else if (br.pkgList != null) {
                        if (br.pkgList.contains("com.android.systemui")) {
                            needMorePrevious = true;
                            break;
                        }
                    }
                }
            }
        }
        return needMorePrevious;
    }

    public void setMorePreviousLevel(int levelValue) {
        this.mMorePreviousLevel = levelValue;
    }

    private int decideMorePreviousLevel() {
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        if (minfo.getTotalSize() / MemoryConstant.MB_SIZE > 3072) {
            return 1;
        }
        return 0;
    }
}
