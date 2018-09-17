package com.android.server.rms.iaware.appmng;

import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
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
import com.android.server.am.HwActivityManagerService;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessWindowInfo;
import com.android.server.mtm.iaware.appmng.appclean.CrashClean;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.AppCleanupFeature;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AwareAppAssociate {
    public static final int ASSOC_DECAY_MIN_TIME = 120000;
    public static final int ASSOC_REPORT_MIN_TIME = 60000;
    public static final int CLEAN_LEVEL = 0;
    private static boolean DEBUG = false;
    public static final int FIRST_START_TIMES = 1;
    private static final int FIVE_SECONDS = 5000;
    private static final String INTERNALAPP_PKGNAME = "com.huawei.android.internal.app";
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
    private static final String TAG = "RMS.AwareAppAssociate";
    private static final int TRANSACTION_getVisibleWindows = 1005;
    private static final int WIDGET_INVISIBLE = 0;
    private static final int WIDGET_VISIBLE = 1;
    private static AwareAppAssociate mAwareAppAssociate = null;
    private static boolean mEnabled = false;
    private final AwareAppLruBase mAmsPrevBase;
    private final ArrayMap<Integer, AssocPidRecord> mAssocRecordMap;
    private ArrayMap<Integer, ProcessData> mBgRecentForcePids;
    private final ArraySet<IAwareVisibleCallback> mCallbacks;
    private int mCurSwitchUser;
    private int mCurUserId;
    private ArrayMap<Integer, Integer> mForePids;
    private AppAssocHandler mHandler;
    private ArrayList<String> mHomePackageList;
    private int mHomeProcessPid;
    private int mHomeProcessUid;
    private HwActivityManagerService mHwAMS;
    private AtomicBoolean mIsInitialized;
    private LruCache<Integer, AwareAppLruBase> mLruCache;
    private MultiTaskManagerService mMtmService;
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
                HashMap<String, String> cleanMsg = msg.obj;
                String pkg = (String) cleanMsg.get(HwGpsPowerTracker.DEL_PKG);
                String proc = (String) cleanMsg.get("proc");
                try {
                    int userId = Integer.parseInt((String) cleanMsg.get("userId"));
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
        public HashSet<String> mComponents = new HashSet();
        public long miniTime;
        public int pid;
        public ArraySet<String> pkgList = new ArraySet();
        public String processName;
        public int uid;

        public AssocBaseRecord(String name, int uid, int pid) {
            this.processName = name;
            this.uid = uid;
            this.pid = pid;
            this.miniTime = SystemClock.elapsedRealtime();
        }
    }

    private final class AssocPidRecord {
        public final ProcessMap<AssocBaseRecord> mAssocBindService = new ProcessMap();
        public final ProcessMap<AssocBaseRecord> mAssocProvider = new ProcessMap();
        public int pid;
        public String processName;
        public int uid;

        public AssocPidRecord(int pid, int uid, String name) {
            this.pid = pid;
            this.uid = uid;
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
            if (this.mAssocBindService.getMap().isEmpty()) {
                return this.mAssocProvider.getMap().isEmpty();
            }
            return false;
        }

        public int size() {
            return this.mAssocBindService.getMap().size() + this.mAssocProvider.getMap().size();
        }

        public String toString() {
            int i;
            SparseArray<AssocBaseRecord> brs;
            int NB;
            int j;
            AssocBaseRecord br;
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
            for (i = 0; i < NP; i++) {
                brs = (SparseArray) this.mAssocBindService.getMap().valueAt(i);
                NB = brs.size();
                for (j = 0; j < NB; j++) {
                    br = (AssocBaseRecord) brs.valueAt(j);
                    if (flag) {
                        sb.append("    [BindService] depend on:\n");
                        flag = false;
                    }
                    for (String component : br.mComponents) {
                        sb.append("        Pid:");
                        sb.append(br.pid);
                        sb.append(",Uid:");
                        sb.append(br.uid);
                        sb.append(",ProcessName:");
                        sb.append(br.processName);
                        sb.append(",Time:");
                        sb.append(SystemClock.elapsedRealtime() - br.miniTime);
                        sb.append(",Component:");
                        sb.append(component);
                        sb.append("\n");
                    }
                }
            }
            NP = this.mAssocProvider.getMap().size();
            flag = true;
            for (i = 0; i < NP; i++) {
                brs = (SparseArray) this.mAssocProvider.getMap().valueAt(i);
                NB = brs.size();
                for (j = 0; j < NB; j++) {
                    br = (AssocBaseRecord) brs.valueAt(j);
                    if (flag) {
                        sb.append("    [Provider] depend on:\n");
                        flag = false;
                    }
                    for (String component2 : br.mComponents) {
                        if (SystemClock.elapsedRealtime() - br.miniTime < 120000) {
                            sb.append("        Pid:");
                            sb.append(br.pid);
                            sb.append(",Uid:");
                            sb.append(br.uid);
                            sb.append(",ProcessName:");
                            sb.append(br.processName);
                            sb.append(",Time:");
                            sb.append(SystemClock.elapsedRealtime() - br.miniTime);
                            sb.append(",Component:");
                            sb.append(component2);
                            sb.append(",Strong:");
                            sb.append(br.isStrong);
                            sb.append("\n");
                        }
                    }
                }
            }
            return sb.toString();
        }
    }

    public interface IAwareVisibleCallback {
        void onVisibleWindowsChanged(int i, int i2, int i3);
    }

    private static class LaunchData {
        private long mFirstTime;
        private int mLaunchTimes;

        /* synthetic */ LaunchData(int launchTimes, long firstTime, LaunchData -this2) {
            this(launchTimes, firstTime);
        }

        private LaunchData(int launchTimes, long firstTime) {
            this.mLaunchTimes = launchTimes;
            this.mFirstTime = firstTime;
        }

        private LaunchData increase() {
            this.mLaunchTimes++;
            return this;
        }

        private long getFirstTime() {
            return this.mFirstTime;
        }

        private int getLaunchTimes() {
            return this.mLaunchTimes;
        }
    }

    private static class ProcessData {
        private long mTimeStamp;
        private int mUid;

        /* synthetic */ ProcessData(int uid, long timeStamp, ProcessData -this2) {
            this(uid, timeStamp);
        }

        private ProcessData(int uid, long timeStamp) {
            this.mUid = uid;
            this.mTimeStamp = timeStamp;
        }
    }

    private static final class Widget {
        int appWidgetId;
        boolean isVisible = false;
        String pkgName = "";

        public Widget(int appWidgetId, String pkgName, boolean isVisible) {
            this.appWidgetId = appWidgetId;
            this.pkgName = pkgName;
            this.isVisible = isVisible;
        }
    }

    private void checkRecentForce() {
        int removeCount = 0;
        long curTime = SystemClock.elapsedRealtime();
        synchronized (this.mBgRecentForcePids) {
            for (int i = this.mBgRecentForcePids.size() - 1; i >= 0; i--) {
                if (curTime - ((ProcessData) this.mBgRecentForcePids.valueAt(i)).mTimeStamp > MemoryConstant.MIN_INTERVAL_OP_TIMEOUT) {
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
            List<Integer> list = new ArrayList(lru.keySet());
            if (list.size() < 1) {
                return false;
            }
            int prevUid = ((Integer) list.get(list.size() - 1)).intValue();
            AwareAppLruBase lruBase = (AwareAppLruBase) lru.get(Integer.valueOf(prevUid));
            if (lruBase == null) {
                return false;
            } else if (prevUid == uid) {
                this.mLruCache.put(Integer.valueOf(prevUid), new AwareAppLruBase(pid, prevUid, lruBase.mTime));
                return false;
            } else {
                if (isSystemDialogProc(lruBase.mPid, prevUid, lruBase.mTime, timeNow)) {
                    this.mLruCache.remove(Integer.valueOf(prevUid));
                } else {
                    this.mLruCache.put(Integer.valueOf(prevUid), new AwareAppLruBase(lruBase.mPid, prevUid, timeNow));
                }
                this.mLruCache.put(Integer.valueOf(uid), new AwareAppLruBase(pid, uid, timeNow));
                return true;
            }
        }
    }

    private void updatePrevApp(int pid, int uid) {
        if (updateActivityLruCache(pid, uid)) {
            LinkedHashMap<Integer, AwareAppLruBase> lru = getActivityLruCache();
            if (lru != null) {
                List<Integer> list = new ArrayList(lru.keySet());
                int listSize = list.size();
                if (listSize >= 2) {
                    int prevUid = ((Integer) list.get(listSize - 2)).intValue();
                    if (prevUid != this.mHomeProcessUid) {
                        AwareAppLruBase.copyLruBaseInfo((AwareAppLruBase) lru.get(Integer.valueOf(prevUid)), this.mPrevNonHomeBase);
                    } else if (listSize < 3) {
                        this.mPrevNonHomeBase.setInitValue();
                    } else {
                        AwareAppLruBase.copyLruBaseInfo((AwareAppLruBase) lru.get(Integer.valueOf(((Integer) list.get(listSize - 3)).intValue())), this.mPrevNonHomeBase);
                    }
                }
            }
        }
    }

    private void updatePreviousAppInfo(int pid, int uid, boolean foregroundActivities, Map<Integer, Integer> forePids) {
        if (this.mHwAMS != null) {
            if (foregroundActivities) {
                if (isForgroundPid(pid)) {
                    updatePrevApp(pid, uid);
                }
            } else if (forePids != null) {
                if (!(forePids.containsValue(Integer.valueOf(uid)) || pid == this.mHomeProcessPid)) {
                    this.mRecentTaskPrevBase.setValue(pid, uid, SystemClock.elapsedRealtime());
                }
                for (Entry<Integer, Integer> m : forePids.entrySet()) {
                    Integer forePid = (Integer) m.getKey();
                    if (isForgroundPid(forePid.intValue())) {
                        updatePrevApp(forePid.intValue(), ((Integer) m.getValue()).intValue());
                        return;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x001b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSystemDialogProc(int pid, int uid, long prevActTime, long curTime) {
        if (uid != 1000) {
            return false;
        }
        synchronized (this) {
            AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pid));
            if (br == null || br.pkgList == null) {
            } else if (br.pkgList.size() != 1) {
                return false;
            } else if (br.pkgList.contains(INTERNALAPP_PKGNAME)) {
                return true;
            } else {
                return false;
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
        this.mForePids = new ArrayMap();
        this.mLruCache = new LruCache(4);
        this.mProcLaunchMap = new HashMap();
        this.mPrevNonHomeBase = new AwareAppLruBase();
        this.mRecentTaskPrevBase = new AwareAppLruBase();
        this.mAmsPrevBase = new AwareAppLruBase();
        this.mCurUserId = 0;
        this.mCurSwitchUser = 0;
        this.mVisibleWindows = new ArrayMap();
        this.mWidgets = new ArrayMap();
        this.mHomeProcessPid = 0;
        this.mHomeProcessUid = 0;
        this.mHomePackageList = new ArrayList();
        this.mAssocRecordMap = new ArrayMap();
        this.mProcInfoMap = new ProcessMap();
        this.mProcPidMap = new ArrayMap();
        this.mProcUidMap = new ArrayMap();
        this.mProcPkgMap = new ArrayMap();
        this.mCallbacks = new ArraySet();
        this.mVisWinDurScreenOff = new ArraySet();
        this.mScreenOff = false;
        this.mBgRecentForcePids = new ArrayMap();
        this.mProcessObserver = new Stub() {
            public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
                if (AwareAppAssociate.DEBUG) {
                    AwareLog.i(AwareAppAssociate.TAG, "Pid:" + pid + ",Uid:" + uid + " come to foreground." + foregroundActivities);
                }
                ArrayMap<Integer, Integer> forePidsBak = new ArrayMap();
                synchronized (AwareAppAssociate.this.mForePids) {
                    if (foregroundActivities) {
                        AwareAppAssociate.this.mForePids.put(Integer.valueOf(pid), Integer.valueOf(uid));
                        forePidsBak.putAll(AwareAppAssociate.this.mForePids);
                    } else {
                        AwareAppAssociate.this.mForePids.remove(Integer.valueOf(pid));
                        forePidsBak.putAll(AwareAppAssociate.this.mForePids);
                    }
                }
                synchronized (AwareAppAssociate.this.mBgRecentForcePids) {
                    if (foregroundActivities) {
                        AwareAppAssociate.this.mBgRecentForcePids.remove(Integer.valueOf(pid));
                    } else {
                        AwareAppAssociate.this.mBgRecentForcePids.put(Integer.valueOf(pid), new ProcessData(uid, SystemClock.elapsedRealtime(), null));
                        if (AwareAppAssociate.this.mHandler != null) {
                            AwareAppAssociate.this.mHandler.sendEmptyMessageDelayed(3, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                        }
                    }
                }
                AwareAppAssociate.this.updatePreviousAppInfo(pid, uid, foregroundActivities, forePidsBak);
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
                if (AwareAppAssociate.this.mHwAMS != null) {
                    AwareAppAssociate.this.mHwAMS.reportProcessDied(pid);
                }
            }
        };
        this.mUserSwitchObserver = new IUserSwitchObserver.Stub() {
            public void onUserSwitching(int newUserId, IRemoteCallback reply) {
                if (reply != null) {
                    try {
                        reply.sendResult(null);
                        AwareAppAssociate.this.mCurSwitchUser = newUserId;
                    } catch (RemoteException e) {
                        AwareLog.e(AwareAppAssociate.TAG, "RemoteException onUserSwitching");
                    }
                }
            }

            public void onUserSwitchComplete(int newUserId) throws RemoteException {
                AwareAppAssociate.this.checkAndInitWidgetObj(newUserId);
                AwareAppAssociate.this.mCurUserId = newUserId;
                AwareAppAssociate.this.mCurSwitchUser = newUserId;
                AwareAppAssociate.this.updateWidgets(AwareAppAssociate.this.mCurUserId);
                AwareIntelligentRecg.getInstance().initUserSwitch(newUserId);
                AwareFakeActivityRecg.self().initUserSwitch(newUserId);
                if (newUserId == 0) {
                    AwareIntelligentRecg.getInstance().updateWidget(AwareAppAssociate.this.getWidgetsPkg(newUserId));
                }
            }

            public void onForegroundProfileSwitch(int newProfileId) {
            }

            public void onLockedBootComplete(int newUserId) {
            }
        };
        this.mHwAMS = HwActivityManagerService.self();
        this.mHandler = new AppAssocHandler(BackgroundThread.get().getLooper());
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

    public void getVisibleWindows(Set<Integer> windowPids, Set<Integer> evilPids) {
        if (mEnabled && windowPids != null) {
            synchronized (this.mVisibleWindows) {
                for (Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                    AwareProcessWindowInfo winInfo = (AwareProcessWindowInfo) window.getValue();
                    boolean allowedWindow = winInfo.mMode == 0 || winInfo.mMode == 3;
                    if (allowedWindow && (winInfo.isEvil() ^ 1) != 0) {
                        windowPids.add((Integer) window.getKey());
                    } else if (evilPids != null) {
                        evilPids.add((Integer) window.getKey());
                    } else {
                        continue;
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

    private void updateWidgets(int userId) {
        if (DEBUG) {
            AwareLog.i(TAG, "updateWidgets, userId: " + userId);
        }
        IBinder service = ServiceManager.getService("appwidget");
        if (service != null) {
            ArrayMap<Integer, Widget> widgets = new ArrayMap();
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
                    boolean visibleB = reply.readInt() == 1;
                    if (pkg != null && pkg.length() > 0) {
                        widgets.put(Integer.valueOf(id), new Widget(id, pkg, visibleB));
                    }
                    if (DEBUG) {
                        AwareLog.i(TAG, "updateWidgets, widget: " + id + ", " + pkg + ", " + visibleB);
                    }
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "getWidgetsPkg, transact error!");
            } finally {
                reply.recycle();
                data.recycle();
            }
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
        ArraySet<String> widgets = new ArraySet();
        synchronized (this.mWidgets) {
            ArrayMap<Integer, Widget> widgetMap = (ArrayMap) this.mWidgets.get(Integer.valueOf(userId));
            if (widgetMap != null) {
                for (Entry<Integer, Widget> entry : widgetMap.entrySet()) {
                    Widget widget = (Widget) entry.getValue();
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
            for (Entry<Integer, Integer> map : this.mForePids.entrySet()) {
                if (uid == ((Integer) map.getValue()).intValue()) {
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
            for (Entry<Integer, ProcessData> map : this.mBgRecentForcePids.entrySet()) {
                ProcessData data = (ProcessData) map.getValue();
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
                for (Entry<Integer, AssocPidRecord> map : this.mAssocRecordMap.entrySet()) {
                    int clientPid = ((Integer) map.getKey()).intValue();
                    AssocPidRecord record = (AssocPidRecord) map.getValue();
                    int NP = record.mAssocBindService.getMap().size();
                    for (int i = 0; i < NP; i++) {
                        SparseArray<AssocBaseRecord> brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                        int NB = brs.size();
                        for (int j = 0; j < NB; j++) {
                            AssocBaseRecord br = (AssocBaseRecord) brs.valueAt(j);
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
                for (Entry<Integer, AssocPidRecord> map : this.mAssocRecordMap.entrySet()) {
                    AssocPidRecord record = (AssocPidRecord) map.getValue();
                    if (record.uid >= 10000) {
                        boolean bfound = false;
                        int NP = record.mAssocBindService.getMap().size();
                        for (int i = 0; i < NP; i++) {
                            SparseArray<AssocBaseRecord> brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                            int NB = brs.size();
                            for (int j = 0; j < NB; j++) {
                                AssocBaseRecord br = (AssocBaseRecord) brs.valueAt(j);
                                if (br != null && br.uid == uid) {
                                    strong.addAll(getPackageNameForUid(record.uid, record.pid));
                                    bfound = true;
                                    break;
                                }
                            }
                            if (bfound) {
                                break;
                            }
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
        if (mEnabled) {
            if (DEBUG) {
                AwareLog.d(TAG, "eventId: " + eventId);
            }
            if (bundleArgs != null) {
                if (!this.mIsInitialized.get()) {
                    initialize();
                }
                switch (eventId) {
                    case 1:
                    case 2:
                        addProcessRelation(bundleArgs.getInt("callPid"), bundleArgs.getInt("callUid"), bundleArgs.getString("callProcName"), bundleArgs.getInt("tgtUid"), bundleArgs.getString("tgtProcName"), bundleArgs.getString("compName"), eventId);
                        break;
                    case 3:
                        removeProcessRelation(bundleArgs.getInt("callPid"), bundleArgs.getInt("callUid"), bundleArgs.getString("callProcName"), bundleArgs.getInt("tgtUid"), bundleArgs.getString("tgtProcName"), bundleArgs.getString("compName"), eventId);
                        break;
                    case 4:
                        updateProcessRelation(bundleArgs.getInt("callPid"), bundleArgs.getInt("callUid"), bundleArgs.getString("callProcName"), bundleArgs.getStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                        break;
                    case 5:
                        addWidget(bundleArgs.getInt("userid"), bundleArgs.getInt("widgetId", -1), bundleArgs.getString("widget"), bundleArgs.getBundle("widgetOpt"));
                        break;
                    case 6:
                        removeWidget(bundleArgs.getInt("userid"), bundleArgs.getInt("widgetId", -1), bundleArgs.getString("widget"));
                        break;
                    case 7:
                        clearWidget();
                        break;
                    case 8:
                        addWindow(bundleArgs.getInt("window"), bundleArgs.getInt("windowmode"), bundleArgs.getInt("hashcode"), bundleArgs.getInt("width"), bundleArgs.getInt("height"));
                        break;
                    case 9:
                        removeWindow(bundleArgs.getInt("window"), bundleArgs.getInt("hashcode"));
                        break;
                    case 10:
                        updateWindowOps(bundleArgs.getString(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                        break;
                    case 11:
                        ArrayList<String> pkgHome = bundleArgs.getStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME);
                        reportHome(bundleArgs.getInt("pid"), bundleArgs.getInt("tgtUid"), pkgHome);
                        break;
                    case 12:
                        reportPrevInfo(bundleArgs.getInt("pid"), bundleArgs.getInt("tgtUid"));
                        break;
                    case 24:
                        updateWidgetOptions(bundleArgs.getInt("userid"), bundleArgs.getInt("widgetId", -1), bundleArgs.getString("widget"), bundleArgs.getBundle("widgetOpt"));
                        break;
                    default:
                        AwareLog.e(TAG, "Unknown EventID: " + eventId);
                        break;
                }
                return;
            }
            return;
        }
        if (DEBUG) {
            AwareLog.d(TAG, "AwareAppAssociate feature disabled!");
        }
    }

    private void getStrongAssoc(int pid, Set<Integer> strong) {
        if (pid > 0 && strong != null) {
            synchronized (this) {
                long curElapse = SystemClock.elapsedRealtime();
                AssocPidRecord record = (AssocPidRecord) this.mAssocRecordMap.get(Integer.valueOf(pid));
                if (record == null) {
                    return;
                }
                int i;
                SparseArray<AssocBaseRecord> brs;
                int NB;
                int j;
                int targetPid;
                int NP = record.mAssocBindService.getMap().size();
                for (i = 0; i < NP; i++) {
                    brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                    NB = brs.size();
                    for (j = 0; j < NB; j++) {
                        targetPid = ((AssocBaseRecord) brs.valueAt(j)).pid;
                        if (targetPid != 0) {
                            strong.add(Integer.valueOf(targetPid));
                        }
                    }
                }
                NP = record.mAssocProvider.getMap().size();
                for (i = 0; i < NP; i++) {
                    brs = (SparseArray) record.mAssocProvider.getMap().valueAt(i);
                    NB = brs.size();
                    for (j = 0; j < NB; j++) {
                        AssocBaseRecord br = (AssocBaseRecord) brs.valueAt(j);
                        targetPid = br.pid;
                        if (targetPid != 0 && br.isStrong && curElapse - br.miniTime < 120000) {
                            strong.add(Integer.valueOf(targetPid));
                        }
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
                if (!((ArrayMap) this.mWidgets.get(Integer.valueOf(userId))).containsKey(Integer.valueOf(widgetId))) {
                    ((ArrayMap) this.mWidgets.get(Integer.valueOf(userId))).put(Integer.valueOf(widgetId), new Widget(widgetId, pkgName, isWidgetVisible(options)));
                }
            }
            if (userId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(userId));
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
                ((ArrayMap) this.mWidgets.get(Integer.valueOf(userId))).remove(Integer.valueOf(widgetId));
            }
            if (userId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(userId));
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
                ArrayMap<Integer, Widget> widgetMap = (ArrayMap) this.mWidgets.get(Integer.valueOf(userId));
                if (widgetMap.get(Integer.valueOf(widgetId)) != null) {
                    ((Widget) widgetMap.get(Integer.valueOf(widgetId))).isVisible = visible;
                } else {
                    widgetMap.put(Integer.valueOf(widgetId), new Widget(widgetId, pkgName, visible));
                }
                if (userId == 0) {
                    AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(userId));
                }
            }
        }
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
            for (Entry<Integer, ArrayMap<Integer, Widget>> m : this.mWidgets.entrySet()) {
                ArrayMap<Integer, Widget> userWdigets = (ArrayMap) m.getValue();
                if (userWdigets != null) {
                    userWdigets.clear();
                }
            }
        }
        AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(0));
    }

    private void initVisibleWindows() {
        Parcel data = null;
        Parcel reply = null;
        try {
            IBinder windowManager = ServiceManager.getService("window");
            if (windowManager == null) {
                AwareLog.e(TAG, "[ERROR]Connect to window Service failed.");
                return;
            }
            data = Parcel.obtain();
            reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            windowManager.transact(1005, data, reply, 0);
            synchronized (this.mVisibleWindows) {
                this.mVisibleWindows.clear();
                int size = reply.readInt();
                for (int i = 0; i < size; i++) {
                    boolean z;
                    int window = reply.readInt();
                    int mode = reply.readInt();
                    int code = reply.readInt();
                    int width = reply.readInt();
                    int height = reply.readInt();
                    if (DEBUG) {
                        AwareLog.i(TAG, "initVisibleWindows pid:" + window + " mode:" + mode + " code:" + code + " width:" + width + " height:" + height);
                    }
                    AwareProcessWindowInfo winInfo = (AwareProcessWindowInfo) this.mVisibleWindows.get(Integer.valueOf(window));
                    if (winInfo == null) {
                        winInfo = new AwareProcessWindowInfo(mode, width, height);
                        this.mVisibleWindows.put(Integer.valueOf(window), winInfo);
                        if (!(width == 1 && height == 1)) {
                            notifyVisibleWindowsChange(2, window, mode);
                        }
                    }
                    Integer valueOf = Integer.valueOf(code);
                    if (width < 0 || height < 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    winInfo.addWindow(valueOf, z);
                }
            }
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (RemoteException e) {
            try {
                AwareLog.e(TAG, "[ERROR]Catch RemoteException when initVisibleWindows.");
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
            } catch (Throwable th) {
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
            }
        }
    }

    private void deinitVisibleWindows() {
        synchronized (this.mVisibleWindows) {
            this.mVisibleWindows.clear();
            notifyVisibleWindowsChange(0, -1, -1);
        }
    }

    private void addWindow(int window, int mode, int code, int width, int height) {
        boolean z = true;
        if (window > 0) {
            synchronized (this.mVisibleWindows) {
                AwareProcessWindowInfo winInfo = (AwareProcessWindowInfo) this.mVisibleWindows.get(Integer.valueOf(window));
                if (winInfo == null) {
                    winInfo = new AwareProcessWindowInfo(mode, width, height);
                    this.mVisibleWindows.put(Integer.valueOf(window), winInfo);
                    if (!(width == 1 && height == 1)) {
                        notifyVisibleWindowsChange(2, window, mode);
                    }
                }
                Integer valueOf = Integer.valueOf(code);
                if (width >= 0 && height >= 0) {
                    z = false;
                }
                winInfo.addWindow(valueOf, z);
            }
            if (DEBUG) {
                AwareLog.i(TAG, "[addVisibleWindows]:" + window + " [mode]:" + mode + " [code]:" + code);
            }
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0046, code:
            if (r1 == false) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:20:0x004a, code:
            if (r6.mScreenOff == false) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:22:0x004e, code:
            if ((r0 ^ 1) == 0) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:23:0x0050, code:
            r4 = r6.mVisWinDurScreenOff;
     */
    /* JADX WARNING: Missing block: B:24:0x0052, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:26:?, code:
            r6.mVisWinDurScreenOff.add(java.lang.Integer.valueOf(r7));
     */
    /* JADX WARNING: Missing block: B:27:0x005c, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:29:0x005f, code:
            if (DEBUG == false) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:30:0x0061, code:
            android.rms.iaware.AwareLog.d(TAG, "[removeVisibleWindows]:" + r7 + " [code]:" + r8);
     */
    /* JADX WARNING: Missing block: B:31:0x0086, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void removeWindow(int window, int code) {
        if (window > 0) {
            boolean removed = false;
            synchronized (this.mVisibleWindows) {
                AwareProcessWindowInfo winInfo = (AwareProcessWindowInfo) this.mVisibleWindows.get(Integer.valueOf(window));
                if (winInfo == null) {
                    this.mVisibleWindows.remove(Integer.valueOf(window));
                    return;
                }
                boolean isEvil = winInfo.isEvil();
                winInfo.removeWindow(Integer.valueOf(code));
                if (winInfo.mWindows.size() == 0) {
                    this.mVisibleWindows.remove(Integer.valueOf(window));
                    if (!isEvil) {
                        notifyVisibleWindowsChange(1, window, -1);
                    }
                    removed = true;
                }
            }
        }
    }

    private void updateWindowOps(String pkgName) {
        if (pkgName != null && this.mMtmService != null) {
            synchronized (this) {
                synchronized (this.mVisibleWindows) {
                    for (Entry<Integer, AwareProcessWindowInfo> window : this.mVisibleWindows.entrySet()) {
                        int pid = ((Integer) window.getKey()).intValue();
                        AwareProcessWindowInfo winInfo = (AwareProcessWindowInfo) window.getValue();
                        AssocBaseRecord record = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pid));
                        if (record != null && record.pkgList != null && winInfo != null && record.pkgList.contains(pkgName)) {
                            int mode = ((AppOpsManager) this.mMtmService.context().getSystemService("appops")).checkOpNoThrow(24, record.uid, pkgName);
                            winInfo.mMode = mode;
                            if (!winInfo.isEvil()) {
                                notifyVisibleWindowsChange(2, pid, mode);
                            }
                        }
                    }
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
        ArrayList<String> pkgs = new ArrayList();
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

    /* JADX WARNING: Missing block: B:44:0x014c, code:
            return;
     */
    /* JADX WARNING: Missing block: B:56:0x0180, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addProcessRelation(int callerPid, int callerUid, String callerName, int targetUid, String targetName, String comp, int type) {
        if (!checkType(type)) {
            return;
        }
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
            if (DEBUG) {
                AwareLog.i(TAG, typeToString(type) + ". Caller[Pid:" + callerPid + "][Uid:" + callerUid + "][Name:" + callerName + "]" + " Target[Uid:" + targetUid + "][pName:" + targetName + "][hash:" + comp + "]");
            }
            int targetPid = 0;
            if (targetUid != 1000 || !targetName.equals(SYSTEM)) {
                synchronized (this) {
                    AssocBaseRecord br = (AssocBaseRecord) this.mProcInfoMap.get(targetName, targetUid);
                    if (br != null) {
                        targetPid = br.pid;
                    }
                    AssocPidRecord pidRecord = (AssocPidRecord) this.mAssocRecordMap.get(Integer.valueOf(callerPid));
                    AssocBaseRecord baseRecord;
                    ProcessMap<AssocBaseRecord> relations;
                    if (pidRecord == null) {
                        pidRecord = new AssocPidRecord(callerPid, callerUid, callerName);
                        baseRecord = new AssocBaseRecord(targetName, targetUid, targetPid);
                        baseRecord.mComponents.add(comp);
                        relations = pidRecord.getMap(type);
                        if (relations != null) {
                            relations.put(targetName, targetUid, baseRecord);
                            this.mAssocRecordMap.put(Integer.valueOf(callerPid), pidRecord);
                        } else if (DEBUG) {
                            AwareLog.e(TAG, "Error type:" + type);
                        }
                    } else {
                        relations = pidRecord.getMap(type);
                        if (relations != null) {
                            baseRecord = (AssocBaseRecord) relations.get(targetName, targetUid);
                            if (baseRecord == null) {
                                baseRecord = new AssocBaseRecord(targetName, targetUid, targetPid);
                                baseRecord.mComponents.add(comp);
                                relations.put(targetName, targetUid, baseRecord);
                                return;
                            }
                            baseRecord.miniTime = SystemClock.elapsedRealtime();
                            baseRecord.isStrong = true;
                            baseRecord.mComponents.add(comp);
                        } else if (DEBUG) {
                            AwareLog.e(TAG, "Error type:" + type);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:39:0x0104, code:
            return;
     */
    /* JADX WARNING: Missing block: B:51:0x0137, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void removeProcessRelation(int callerPid, int callerUid, String callerName, int targetUid, String targetName, String comp, int type) {
        if (!checkType(type)) {
            return;
        }
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
            synchronized (this) {
                AssocPidRecord pr = (AssocPidRecord) this.mAssocRecordMap.get(Integer.valueOf(callerPid));
                if (pr == null) {
                    return;
                }
                ProcessMap<AssocBaseRecord> relations = pr.getMap(type);
                if (relations != null) {
                    AssocBaseRecord br = (AssocBaseRecord) relations.get(targetName, targetUid);
                    if (br != null && br.mComponents.contains(comp)) {
                        br.mComponents.remove(comp);
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

    private void removeDiedProcessRelation(int pid, int uid) {
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
            ArraySet<Integer> pids;
            AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.remove(Integer.valueOf(pid));
            if (br != null) {
                this.mProcInfoMap.remove(br.processName, br.uid);
                if (br.pkgList != null) {
                    for (String pkg : br.pkgList) {
                        synchronized (this.mProcPkgMap) {
                            pids = (ArraySet) this.mProcPkgMap.get(pkg);
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
            pids = (ArraySet) this.mProcUidMap.get(Integer.valueOf(uid));
            if (pids != null && pids.contains(Integer.valueOf(pid))) {
                pids.remove(Integer.valueOf(pid));
                if (pids.isEmpty()) {
                    this.mProcUidMap.remove(Integer.valueOf(uid));
                }
            }
            Iterator<Entry<Integer, AssocPidRecord>> it = this.mAssocRecordMap.entrySet().iterator();
            while (it.hasNext()) {
                AssocPidRecord record = (AssocPidRecord) ((Entry) it.next()).getValue();
                if (record.pid == pid) {
                    it.remove();
                } else {
                    if (br != null) {
                        record.mAssocBindService.remove(br.processName, br.uid);
                        record.mAssocProvider.remove(br.processName, br.uid);
                    }
                    if (record.isEmpty()) {
                        it.remove();
                    }
                }
            }
        }
    }

    private void updateProcLaunchData(int uid, String proc, ArrayList<String> pkgList) {
        if (AppCleanupFeature.isAppCleanEnable() && UserHandle.getAppId(uid) >= 10000 && !UserHandle.isIsolated(uid)) {
            if (DEBUG) {
                AwareLog.i(TAG, "updateProcLaunchData, proc: " + proc + ", uid: " + uid + ", pkgList: " + pkgList);
            }
            synchronized (this.mProcLaunchMap) {
                int userId = UserHandle.getUserId(uid);
                Map<String, Map<String, LaunchData>> pkgMap = (Map) this.mProcLaunchMap.get(Integer.valueOf(userId));
                if (pkgMap == null) {
                    pkgMap = new HashMap();
                    this.mProcLaunchMap.put(Integer.valueOf(userId), pkgMap);
                }
                for (String pkg : pkgList) {
                    if (pkg != null) {
                        Map<String, LaunchData> procMap = (Map) pkgMap.get(pkg);
                        if (procMap == null) {
                            procMap = new HashMap();
                            pkgMap.put(pkg, procMap);
                        }
                        LaunchData launchData = (LaunchData) procMap.get(proc);
                        if (launchData != null) {
                            procMap.put(proc, launchData.increase());
                            if (DEBUG) {
                                AwareLog.i(TAG, "updateProcLaunchData, pkg: " + pkg + ", launcTimes: " + launchData.getLaunchTimes());
                            }
                            if (launchData.getLaunchTimes() >= 30) {
                                if (SystemClock.elapsedRealtime() - launchData.getFirstTime() <= 300000) {
                                    Map<String, String> cleanMsg = new HashMap();
                                    cleanMsg.put("proc", proc);
                                    cleanMsg.put(HwGpsPowerTracker.DEL_PKG, pkg);
                                    cleanMsg.put("userId", "" + userId);
                                    Message msg = this.mHandler.obtainMessage();
                                    msg.what = 2;
                                    msg.obj = cleanMsg;
                                    this.mHandler.sendMessage(msg);
                                }
                                pkgMap.remove(pkg);
                            }
                        } else {
                            launchData = new LaunchData(1, SystemClock.elapsedRealtime(), null);
                            procMap.put(proc, launchData);
                            if (DEBUG) {
                                AwareLog.i(TAG, "updateProcLaunchData, pkg: " + pkg + ", launcTimes: " + launchData.getLaunchTimes());
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:66:0x0160, code:
            r5 = r5 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateProcessRelation(int pid, int uid, String name, ArrayList<String> pkgList) {
        Throwable th;
        if (pid <= 0 || uid <= 0) {
            if (DEBUG) {
                AwareLog.i(TAG, "updateProcessRelation with wrong pid or uid");
            }
            return;
        } else if (name == null || pkgList == null) {
            if (DEBUG) {
                AwareLog.i(TAG, "updateProcessRelation with wrong name");
            }
            return;
        } else {
            if (DEBUG) {
                AwareLog.i(TAG, "update relation. Pid:" + pid + " Uid:" + uid + ",ProcessName:" + name);
            }
            updateProcLaunchData(uid, name, pkgList);
            synchronized (this) {
                AssocBaseRecord br;
                AssocBaseRecord br2;
                Iterator<Entry<Integer, AssocPidRecord>> it = this.mAssocRecordMap.entrySet().iterator();
                while (it.hasNext()) {
                    AssocPidRecord record = (AssocPidRecord) ((Entry) it.next()).getValue();
                    if (record.pid == pid) {
                        it.remove();
                    } else {
                        br = (AssocBaseRecord) record.mAssocBindService.get(name, uid);
                        if (br != null) {
                            br.pid = pid;
                        }
                        br = (AssocBaseRecord) record.mAssocProvider.get(name, uid);
                        if (br != null) {
                            br.pid = pid;
                        }
                    }
                }
                br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pid));
                if (br == null) {
                    br2 = new AssocBaseRecord(name, uid, pid);
                    try {
                        br2.pkgList.addAll(pkgList);
                        this.mProcPidMap.put(Integer.valueOf(pid), br2);
                        br = br2;
                    } catch (Throwable th2) {
                        th = th2;
                        br = br2;
                        throw th;
                    }
                }
                br.processName = name;
                br.uid = uid;
                br.pid = pid;
                br.pkgList.addAll(pkgList);
                try {
                    br = (AssocBaseRecord) this.mProcInfoMap.get(name, uid);
                    if (br == null) {
                        br2 = new AssocBaseRecord(name, uid, pid);
                        this.mProcInfoMap.put(name, uid, br2);
                        br = br2;
                    } else {
                        br.pid = pid;
                    }
                    ArraySet<Integer> pids = (ArraySet) this.mProcUidMap.get(Integer.valueOf(uid));
                    if (pids == null) {
                        pids = new ArraySet();
                        pids.add(Integer.valueOf(pid));
                        this.mProcUidMap.put(Integer.valueOf(uid), pids);
                    } else {
                        pids.add(Integer.valueOf(pid));
                    }
                    int i = 0;
                    int listSize = pkgList.size();
                    while (i < listSize) {
                        String pkg = (String) pkgList.get(i);
                        synchronized (this.mProcPkgMap) {
                            try {
                                pids = (ArraySet) this.mProcPkgMap.get(pkg);
                                if (pids == null) {
                                    ArraySet<Integer> pids2 = new ArraySet();
                                    try {
                                        pids2.add(Integer.valueOf(pid));
                                        this.mProcPkgMap.put(pkg, pids2);
                                        pids = pids2;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        pids = pids2;
                                    }
                                } else {
                                    pids.add(Integer.valueOf(pid));
                                }
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                    }
                    return;
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
            }
        }
        throw th;
    }

    private void checkAndInitWidgetObj(int userId) {
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

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            if (this.mMtmService == null) {
                this.mMtmService = MultiTaskManagerService.self();
            }
            if (isUserUnlocked()) {
                if (this.mMtmService != null) {
                    initAssoc();
                    registerProcessObserver();
                    this.mIsInitialized.set(true);
                } else if (DEBUG) {
                    AwareLog.w(TAG, "MultiTaskManagerService has not been started.");
                }
                return;
            }
            if (this.mHandler.hasMessages(1)) {
                this.mHandler.removeMessages(1);
            }
            this.mHandler.sendEmptyMessageDelayed(1, REINIT_TIME);
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
            if (this.mHwAMS != null) {
                this.mHwAMS.reportAssocDisable();
            }
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
                ArraySet<Integer> pids = new ArraySet();
                pids.add(Integer.valueOf(this.mMyPid));
                this.mProcUidMap.put(Integer.valueOf(1000), pids);
            }
            initSwitchUser();
            initVisibleWindows();
            updateWidgets(this.mCurUserId);
            if (this.mCurUserId == 0) {
                AwareIntelligentRecg.getInstance().updateWidget(getWidgetsPkg(this.mCurUserId));
            }
            ArrayMap<Integer, Integer> forePids = new ArrayMap();
            this.mHwAMS.reportAssocEnable(forePids);
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
                ArraySet<Integer> procPids = (ArraySet) this.mProcUidMap.get(Integer.valueOf(uid));
                if (procPids != null) {
                    pids.addAll(procPids);
                }
            }
        }
    }

    private String sameUid(int pid) {
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        synchronized (this) {
            AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pid));
            if (br == null) {
                return null;
            }
            ArraySet<Integer> pids = (ArraySet) this.mProcUidMap.get(Integer.valueOf(br.uid));
            if (pids == null) {
                return null;
            }
            for (Integer intValue : pids) {
                int tmp = intValue.intValue();
                if (tmp != pid) {
                    if (flag) {
                        sb.append("    [SameUID] depend on:\n");
                        flag = false;
                    }
                    br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(tmp));
                    if (br != null) {
                        sb.append("        Pid:");
                        sb.append(br.pid);
                        sb.append(",Uid:");
                        sb.append(br.uid);
                        sb.append(",ProcessName:");
                        sb.append(br.processName);
                        sb.append("\n");
                    }
                }
            }
            return sb.toString();
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0020, code:
            return r4;
     */
    /* JADX WARNING: Missing block: B:17:0x0036, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Set<String> getPackageNameForUid(int uid, int pidForUid) {
        ArraySet<String> pkgList = new ArraySet();
        synchronized (this) {
            AssocBaseRecord br;
            if (pidForUid != 0) {
                br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pidForUid));
                if (!(br == null || br.pkgList == null)) {
                    pkgList.addAll(br.pkgList);
                }
            } else {
                ArraySet<Integer> pids = (ArraySet) this.mProcUidMap.get(Integer.valueOf(uid));
                if (pids == null || pids.isEmpty()) {
                } else {
                    for (Integer pid : pids) {
                        br = (AssocBaseRecord) this.mProcPidMap.get(pid);
                        if (!(br == null || br.pkgList == null)) {
                            pkgList.addAll(br.pkgList);
                        }
                    }
                    return pkgList;
                }
            }
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                synchronized (this) {
                    int listSize = this.mAssocRecordMap.size();
                    for (int s = 0; s < listSize; s++) {
                        AssocPidRecord record = (AssocPidRecord) this.mAssocRecordMap.valueAt(s);
                        if (record != null) {
                            pw.println(record);
                        }
                    }
                }
                dumpWidget(pw);
                dumpVisibleWindow(pw);
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    public void dumpFore(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                ArraySet<Integer> tmp = new ArraySet();
                synchronized (this.mForePids) {
                    tmp.addAll(this.mForePids.keySet());
                }
                for (Integer intValue : tmp) {
                    dumpPid(intValue.intValue(), pw);
                }
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    public void dumpRecentFore(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                ArraySet<Integer> tmp = new ArraySet();
                synchronized (this.mBgRecentForcePids) {
                    tmp.addAll(this.mBgRecentForcePids.keySet());
                }
                for (Integer intValue : tmp) {
                    dumpPid(intValue.intValue(), pw);
                }
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    public void dumpPkgProc(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                synchronized (this.mProcPkgMap) {
                    for (String pkg : this.mProcPkgMap.keySet()) {
                        pw.println(pkg + ":" + this.mProcPkgMap.get(pkg));
                    }
                }
                pw.println("proc launch data:");
                synchronized (this.mProcLaunchMap) {
                    for (Entry<Integer, Map<String, Map<String, LaunchData>>> uidEntry : this.mProcLaunchMap.entrySet()) {
                        pw.println("  userId: " + uidEntry.getKey());
                        Map<String, Map<String, LaunchData>> pkgMap = (Map) uidEntry.getValue();
                        if (pkgMap != null) {
                            for (Entry<String, Map<String, LaunchData>> pkgEntry : pkgMap.entrySet()) {
                                pw.println("    pkg: " + ((String) pkgEntry.getKey()));
                                Map<String, LaunchData> procMap = (Map) pkgEntry.getValue();
                                if (procMap != null) {
                                    for (Entry<String, LaunchData> procEntry : procMap.entrySet()) {
                                        LaunchData lData = (LaunchData) procEntry.getValue();
                                        if (lData != null) {
                                            pw.println("      proc: " + ((String) procEntry.getKey()) + ", launchTime: " + lData.getLaunchTimes());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    public void dumpPid(int pid, PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                synchronized (this) {
                    AssocPidRecord record = (AssocPidRecord) this.mAssocRecordMap.get(Integer.valueOf(pid));
                    if (record != null) {
                        pw.println(record);
                    } else {
                        AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pid));
                        if (br != null) {
                            pw.println("Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                        }
                        pw.println(sameUid(pid));
                    }
                }
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    public void dumpVisibleWindow(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                ArraySet<Integer> windows = new ArraySet();
                ArraySet<Integer> windowsEvil = new ArraySet();
                getVisibleWindows(windows, windowsEvil);
                boolean flag = true;
                pw.println("");
                synchronized (this) {
                    AssocBaseRecord br;
                    for (Integer intValue : windows) {
                        br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(intValue.intValue()));
                        if (br != null) {
                            if (flag) {
                                pw.println("[WindowList] :");
                                flag = false;
                            }
                            pw.println("    Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName + ",PkgList:" + br.pkgList);
                        }
                    }
                    for (Integer intValue2 : windowsEvil) {
                        br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(intValue2.intValue()));
                        if (br != null) {
                            if (flag) {
                                pw.println("[WindowEvilList] :");
                                flag = false;
                            }
                            pw.println("    Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName + ",PkgList:" + br.pkgList);
                        }
                    }
                }
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    public void dumpWidget(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                Set<String> widgets = getWidgetsPkg();
                pw.println("[Widgets] : " + widgets.size());
                for (String w : widgets) {
                    pw.println("    " + w);
                }
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    public void dumpHome(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                synchronized (this) {
                    AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(this.mHomeProcessPid));
                    if (br != null) {
                        pw.println("[Home]Pid:" + this.mHomeProcessPid + ",Uid:" + br.uid + ",ProcessName:" + br.processName + ",pkg:" + br.pkgList);
                    }
                }
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    public void dumpPrev(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                Set<String> pkgList = getPackageNameForUid(this.mPrevNonHomeBase.mUid, isDealAsPkgUid(this.mPrevNonHomeBase.mUid) ? this.mPrevNonHomeBase.mPid : 0);
                String eclipseTime = "";
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
                    pw.println("[Prev Recent Task] Uid:" + this.mRecentTaskPrevBase.mUid + ",pid:" + this.mRecentTaskPrevBase.mPid + ",pkg:" + getPackageNameForUid(this.mRecentTaskPrevBase.mUid, isDealAsPkgUid(this.mRecentTaskPrevBase.mUid) ? this.mRecentTaskPrevBase.mPid : 0));
                } else {
                    pw.println("[Prev Recent Task] Uid: None");
                }
                pkgList = getPackageNameForUid(this.mAmsPrevBase.mUid, isDealAsPkgUid(this.mAmsPrevBase.mUid) ? this.mAmsPrevBase.mPid : 0);
                if (this.mAmsPrevBase.mUid == 0) {
                    eclipseTime = " none";
                } else {
                    eclipseTime = " " + ((SystemClock.elapsedRealtime() - this.mAmsPrevBase.mTime) / 1000);
                }
                pw.println("[Prev By Ams] Uid:" + this.mAmsPrevBase.mUid + ",pid:" + this.mAmsPrevBase.mPid + ",pkg:" + pkgList + ",eclipse(s):" + eclipseTime);
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    public void dumpRecord(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                int pidSize;
                int pidsize = 0;
                int compSize = 0;
                pw.println("Widget Size: " + getWidgetsPkg().size());
                ArraySet<Integer> windows = new ArraySet();
                ArraySet<Integer> windowsEvil = new ArraySet();
                getVisibleWindows(windows, windowsEvil);
                pw.println("Window Size: " + windows.size() + ", EvilWindow Size: " + windowsEvil.size());
                synchronized (this) {
                    pidSize = this.mAssocRecordMap.size();
                    for (int s = 0; s < pidSize; s++) {
                        int i;
                        SparseArray<AssocBaseRecord> brs;
                        int NB;
                        int j;
                        int bindSize = 0;
                        int bindSizeAll = 0;
                        int providerSize = 0;
                        int providerSizeAll = 0;
                        int sameuid = 0;
                        AssocPidRecord record = (AssocPidRecord) this.mAssocRecordMap.valueAt(s);
                        int NP = record.mAssocBindService.getMap().size();
                        for (i = 0; i < NP; i++) {
                            brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                            NB = brs.size();
                            bindSize += NB;
                            for (j = 0; j < NB; j++) {
                                bindSizeAll += ((AssocBaseRecord) brs.valueAt(j)).mComponents.size();
                            }
                        }
                        NP = record.mAssocProvider.getMap().size();
                        for (i = 0; i < NP; i++) {
                            brs = (SparseArray) record.mAssocProvider.getMap().valueAt(i);
                            NB = brs.size();
                            providerSize += NB;
                            for (j = 0; j < NB; j++) {
                                providerSizeAll += ((AssocBaseRecord) brs.valueAt(j)).mComponents.size();
                            }
                        }
                        ArraySet<Integer> pids = (ArraySet) this.mProcUidMap.get(Integer.valueOf(record.uid));
                        if (pids != null) {
                            sameuid = pids.size() - 1;
                        }
                        int curpidsize = bindSize + providerSize;
                        int curcompsize = bindSizeAll + providerSizeAll;
                        pidsize += curpidsize;
                        compSize += curcompsize;
                        pw.println("[" + record.uid + "][" + record.processName + "]: " + "bind[" + bindSize + "-" + bindSizeAll + "]" + "provider[" + providerSize + "-" + providerSizeAll + "]" + "SameUID[" + sameuid + "]" + "pids:[" + curpidsize + "]" + "comps:[" + curcompsize + "]" + "piduids:[" + (curpidsize + sameuid) + "]");
                    }
                }
                pw.println("PidRecord Size: " + pidSize + " " + pidsize + " " + compSize);
                return;
            }
            pw.println("AwareAppAssociate feature disabled.");
        }
    }

    private void recordWindowDetail(Set<Integer> list) {
        if (list != null && !list.isEmpty()) {
            synchronized (this) {
                for (Integer intValue : list) {
                    AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(intValue.intValue()));
                    if (br != null) {
                        AwareLog.i(TAG, "[Window]Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                    }
                }
            }
        }
    }

    private void recordAssocDetail(int pid) {
        synchronized (this) {
            AssocPidRecord record = (AssocPidRecord) this.mAssocRecordMap.get(Integer.valueOf(pid));
            if (record != null) {
                AwareLog.i(TAG, "" + record);
            } else {
                AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pid));
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
            if (this.mCallbacks.isEmpty()) {
                return;
            }
            int callbackSize = this.mCallbacks.size();
            for (int i = 0; i < callbackSize; i++) {
                ((IAwareVisibleCallback) this.mCallbacks.valueAt(i)).onVisibleWindowsChanged(type, window, mode);
            }
        }
    }

    public void screenStateChange(boolean screenOff) {
        this.mScreenOff = screenOff;
        if (screenOff && this.mHandler != null) {
            this.mHandler.removeMessages(4);
        }
    }

    private void clearRemoveVisWinDurScreenOff() {
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
}
