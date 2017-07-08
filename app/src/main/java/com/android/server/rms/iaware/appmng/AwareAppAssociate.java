package com.android.server.rms.iaware.appmng;

import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.app.IUserSwitchObserver;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
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
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.PrintWriter;
import java.util.ArrayList;
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
    private static boolean DEBUG = false;
    private static final String INTERNALAPP_PKGNAME = "com.huawei.android.internal.app";
    private static final int MSG_INIT = 1;
    private static final int ONE_SECOND = 1000;
    private static boolean RECORD = false;
    private static final long REINIT_TIME = 2000;
    private static final String SYSTEM = "system";
    private static final String TAG = "RMS.AwareAppAssociate";
    private static final int TRANSACTION_getVisibleWindows = 1005;
    private static AwareAppAssociate mAwareAppAssociate;
    private static boolean mEnabled;
    private final AwareAppLruBase mAmsPrevBase;
    private final ArrayMap<Integer, AssocPidRecord> mAssocRecordMap;
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
    private final ArrayMap<Integer, AssocBaseRecord> mProcPidMap;
    private final ArrayMap<String, ArraySet<Integer>> mProcPkgMap;
    private final ArrayMap<Integer, ArraySet<Integer>> mProcUidMap;
    private IProcessObserver mProcessObserver;
    private final AwareAppLruBase mRecentTaskPrevBase;
    IUserSwitchObserver mUserSwitchObserver;
    private ArrayMap<Integer, Integer> mVisibleWindows;
    private ArrayMap<Integer, ArraySet<String>> mWidgets;

    private class AppAssocHandler extends Handler {
        public AppAssocHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (AwareAppAssociate.DEBUG) {
                AwareLog.e(AwareAppAssociate.TAG, "handleMessage message " + msg.what);
            }
            if (msg.what == AwareAppAssociate.MSG_INIT) {
                AwareAppAssociate.this.initialize();
            }
        }
    }

    private static final class AssocBaseRecord {
        public boolean isStrong;
        public HashSet<String> mComponents;
        public long miniTime;
        public int pid;
        public ArraySet<String> pkgList;
        public String processName;
        public int uid;

        public AssocBaseRecord(String name, int uid, int pid) {
            this.pkgList = new ArraySet();
            this.mComponents = new HashSet();
            this.isStrong = true;
            this.processName = name;
            this.uid = uid;
            this.pid = pid;
            this.miniTime = SystemClock.elapsedRealtime();
        }
    }

    private final class AssocPidRecord {
        public final ProcessMap<AssocBaseRecord> mAssocBindService;
        public final ProcessMap<AssocBaseRecord> mAssocProvider;
        public int pid;
        public String processName;
        public int uid;

        public AssocPidRecord(int pid, int uid, String name) {
            this.mAssocBindService = new ProcessMap();
            this.mAssocProvider = new ProcessMap();
            this.pid = pid;
            this.uid = uid;
            this.processName = name;
        }

        public ProcessMap<AssocBaseRecord> getMap(int type) {
            switch (type) {
                case AwareAppAssociate.MSG_INIT /*1*/:
                case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                    return this.mAssocBindService;
                case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
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
            int j;
            StringBuilder sb = new StringBuilder();
            sb.append("Pid:").append(this.pid).append(",Uid:").append(this.uid).append(",ProcessName:").append(this.processName).append("\n");
            String sameUid = AwareAppAssociate.this.sameUid(this.pid);
            if (sameUid != null) {
                sb.append(sameUid);
            }
            int NP = this.mAssocBindService.getMap().size();
            boolean flag = true;
            for (i = 0; i < NP; i += AwareAppAssociate.MSG_INIT) {
                SparseArray<AssocBaseRecord> brs = (SparseArray) this.mAssocBindService.getMap().valueAt(i);
                int NB = brs.size();
                for (j = 0; j < NB; j += AwareAppAssociate.MSG_INIT) {
                    AssocBaseRecord br = (AssocBaseRecord) brs.valueAt(j);
                    if (flag) {
                        sb.append("    [BindService] depend on:\n");
                        flag = false;
                    }
                    for (String component : br.mComponents) {
                        sb.append("        Pid:").append(br.pid).append(",Uid:").append(br.uid).append(",ProcessName:").append(br.processName).append(",Time:").append(SystemClock.elapsedRealtime() - br.miniTime).append(",Component:").append(component).append("\n");
                    }
                }
            }
            NP = this.mAssocProvider.getMap().size();
            flag = true;
            for (i = 0; i < NP; i += AwareAppAssociate.MSG_INIT) {
                brs = (SparseArray) this.mAssocProvider.getMap().valueAt(i);
                NB = brs.size();
                for (j = 0; j < NB; j += AwareAppAssociate.MSG_INIT) {
                    br = (AssocBaseRecord) brs.valueAt(j);
                    if (flag) {
                        sb.append("    [Provider] depend on:\n");
                        flag = false;
                    }
                    for (String component2 : br.mComponents) {
                        if (SystemClock.elapsedRealtime() - br.miniTime < 120000) {
                            sb.append("        Pid:").append(br.pid).append(",Uid:").append(br.uid).append(",ProcessName:").append(br.processName).append(",Time:").append(SystemClock.elapsedRealtime() - br.miniTime).append(",Component:").append(component2).append(",Strong:").append(br.isStrong).append("\n");
                        }
                    }
                }
            }
            return sb.toString();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.appmng.AwareAppAssociate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.appmng.AwareAppAssociate.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.appmng.AwareAppAssociate.<clinit>():void");
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
            if (list.size() < MSG_INIT) {
                return false;
            }
            int prevUid = ((Integer) list.get(list.size() - 1)).intValue();
            AwareAppLruBase lruBase = (AwareAppLruBase) lru.get(Integer.valueOf(prevUid));
            if (lruBase == null) {
                return false;
            }
            if (prevUid == uid) {
                this.mLruCache.put(Integer.valueOf(prevUid), new AwareAppLruBase(pid, prevUid, lruBase.mTime));
                return false;
            }
            if (isSystemDialogProc(lruBase.mPid, prevUid, lruBase.mTime, timeNow)) {
                this.mLruCache.remove(Integer.valueOf(prevUid));
            } else {
                this.mLruCache.put(Integer.valueOf(prevUid), new AwareAppLruBase(lruBase.mPid, prevUid, timeNow));
            }
            this.mLruCache.put(Integer.valueOf(uid), new AwareAppLruBase(pid, uid, timeNow));
            return true;
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

    private boolean isSystemDialogProc(int pid, int uid, long prevActTime, long curTime) {
        if (uid != ONE_SECOND) {
            return false;
        }
        synchronized (this) {
            AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pid));
            if (br == null || br.pkgList == null) {
                return false;
            } else if (br.pkgList.size() != MSG_INIT) {
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
        return appId >= ONE_SECOND && appId <= IOTController.TYPE_SLAVE;
    }

    private AwareAppAssociate() {
        this.mMyPid = Process.myPid();
        this.mIsInitialized = new AtomicBoolean(false);
        this.mHandler = null;
        this.mForePids = new ArrayMap();
        this.mLruCache = new LruCache(4);
        this.mPrevNonHomeBase = new AwareAppLruBase();
        this.mRecentTaskPrevBase = new AwareAppLruBase();
        this.mAmsPrevBase = new AwareAppLruBase();
        this.mCurUserId = 0;
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
                AwareAppAssociate.this.updatePreviousAppInfo(pid, uid, foregroundActivities, forePidsBak);
            }

            public void onProcessStateChanged(int pid, int uid, int procState) {
            }

            public void onProcessDied(int pid, int uid) {
                synchronized (AwareAppAssociate.this.mForePids) {
                    AwareAppAssociate.this.mForePids.remove(Integer.valueOf(pid));
                }
                AwareAppAssociate.this.removeDiedProcessRelation(pid, uid);
                if (AwareAppAssociate.this.mHwAMS != null) {
                    AwareAppAssociate.this.mHwAMS.reportProcessDied(pid);
                }
            }
        };
        this.mUserSwitchObserver = new IUserSwitchObserver.Stub() {
            public void onUserSwitching(int newUserId, IRemoteCallback reply) {
            }

            public void onUserSwitchComplete(int newUserId) throws RemoteException {
                AwareAppAssociate.this.checkAndInitWidgetObj(newUserId);
                AwareAppAssociate.this.mCurUserId = newUserId;
            }

            public void onForegroundProfileSwitch(int newProfileId) {
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

    public void getVisibleWindows(Set<Integer> windowPids) {
        if (mEnabled && windowPids != null) {
            synchronized (this.mVisibleWindows) {
                for (Entry<Integer, Integer> window : this.mVisibleWindows.entrySet()) {
                    int mode = ((Integer) window.getValue()).intValue();
                    if (mode == 0 || mode == 3) {
                        windowPids.add((Integer) window.getKey());
                    }
                }
            }
            if (DEBUG) {
                AwareLog.d(TAG, "WindowPids:" + windowPids);
            }
            if (RECORD) {
                recordWindowDetail(windowPids);
            }
        }
    }

    public Set<String> getWidgetsPkg() {
        if (!mEnabled) {
            return null;
        }
        ArraySet<String> widgets = new ArraySet();
        synchronized (this.mWidgets) {
            checkAndInitWidgetObj(this.mCurUserId);
            widgets.addAll((ArraySet) this.mWidgets.get(Integer.valueOf(this.mCurUserId)));
        }
        return widgets;
    }

    public void getWidgets(Set<Integer> widgetPids) {
        if (mEnabled && widgetPids != null) {
            ArraySet<String> widgets = new ArraySet();
            synchronized (this.mWidgets) {
                checkAndInitWidgetObj(this.mCurUserId);
                widgets.addAll((ArraySet) this.mWidgets.get(Integer.valueOf(this.mCurUserId)));
            }
            synchronized (this) {
                for (String widget : widgets) {
                    if (!(widget == null || widget.isEmpty())) {
                        if (DEBUG) {
                            AwareLog.d(TAG, "Widget:" + widget);
                        }
                        ArraySet<Integer> pids = (ArraySet) this.mProcPkgMap.get(widget);
                        if (pids != null) {
                            widgetPids.addAll(pids);
                        }
                    }
                }
            }
            if (DEBUG) {
                AwareLog.d(TAG, "WidgetPids:" + widgetPids);
            }
            if (RECORD) {
                recordWidgetDetail(widgetPids);
            }
        }
    }

    public void getForeGroundApp(Set<Integer> forePids) {
        if (mEnabled && forePids != null) {
            synchronized (this.mForePids) {
                forePids.addAll(this.mForePids.keySet());
            }
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
                    for (int i = 0; i < NP; i += MSG_INIT) {
                        SparseArray<AssocBaseRecord> brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                        int NB = brs.size();
                        for (int j = 0; j < NB; j += MSG_INIT) {
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
                    case MSG_INIT /*1*/:
                    case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                        addProcessRelation(bundleArgs.getInt("callPid"), bundleArgs.getInt("callUid"), bundleArgs.getString("callProcName"), bundleArgs.getInt("tgtUid"), bundleArgs.getString("tgtProcName"), bundleArgs.getString("compName"), eventId);
                        break;
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        removeProcessRelation(bundleArgs.getInt("callPid"), bundleArgs.getInt("callUid"), bundleArgs.getString("callProcName"), bundleArgs.getInt("tgtUid"), bundleArgs.getString("tgtProcName"), bundleArgs.getString("compName"), eventId);
                        break;
                    case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                        updateProcessRelation(bundleArgs.getInt("callPid"), bundleArgs.getInt("callUid"), bundleArgs.getString("callProcName"), bundleArgs.getStringArrayList("pkgname"));
                        break;
                    case LifeCycleStateMachine.LOGOUT /*5*/:
                        addWidget(bundleArgs.getInt("userid"), bundleArgs.getString("widget"));
                        break;
                    case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                        removeWidget(bundleArgs.getInt("userid"), bundleArgs.getString("widget"));
                        break;
                    case LifeCycleStateMachine.TIME_OUT /*7*/:
                        clearWidget();
                        break;
                    case ByteUtil.LONG_SIZE /*8*/:
                        addWindow(bundleArgs.getInt("window"), bundleArgs.getInt("windowmode"));
                        break;
                    case HwGnssLogHandlerMsgID.UPDATESVSTATUS /*9*/:
                        removeWindow(bundleArgs.getInt("window"));
                        break;
                    case AwareAppMngDFX.APPLICATION_STARTTYPE_COLD /*10*/:
                        updateWindowOps(bundleArgs.getString("pkgname"));
                        break;
                    case AwareAppMngDFX.APPLICATION_STARTTYPE_TOTAL /*11*/:
                        reportHome(bundleArgs.getInt(ProcessStopShrinker.PID_KEY), bundleArgs.getInt("tgtUid"), bundleArgs.getStringArrayList("pkgname"));
                        break;
                    case HwGnssLogHandlerMsgID.UPDATESETPOSMODE /*12*/:
                        reportPrevInfo(bundleArgs.getInt(ProcessStopShrinker.PID_KEY), bundleArgs.getInt("tgtUid"));
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
                for (i = 0; i < NP; i += MSG_INIT) {
                    brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                    NB = brs.size();
                    for (j = 0; j < NB; j += MSG_INIT) {
                        targetPid = ((AssocBaseRecord) brs.valueAt(j)).pid;
                        if (targetPid != 0) {
                            strong.add(Integer.valueOf(targetPid));
                        }
                    }
                }
                NP = record.mAssocProvider.getMap().size();
                for (i = 0; i < NP; i += MSG_INIT) {
                    brs = (SparseArray) record.mAssocProvider.getMap().valueAt(i);
                    NB = brs.size();
                    for (j = 0; j < NB; j += MSG_INIT) {
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

    private void initWidget() {
        if (this.mMtmService != null) {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(this.mMtmService.context());
            if (widgetManager != null) {
                List<AppWidgetProviderInfo> infos = widgetManager.getInstalledProviders();
                ArraySet<String> widgets = new ArraySet();
                long oldId = Binder.clearCallingIdentity();
                for (AppWidgetProviderInfo info : infos) {
                    if (!(info == null || info.getProfile() == null || info.provider == null)) {
                        int userId = this.mCurUserId;
                        String pkg = info.provider.getPackageName();
                        if (widgetManager.isBoundWidgetPackage(pkg, userId)) {
                            widgets.add(pkg);
                            if (DEBUG) {
                                AwareLog.d(TAG, "Widget:" + pkg);
                            }
                        }
                    }
                }
                Binder.restoreCallingIdentity(oldId);
                synchronized (this.mWidgets) {
                    checkAndInitWidgetObj(this.mCurUserId);
                    ((ArraySet) this.mWidgets.get(Integer.valueOf(this.mCurUserId))).clear();
                    ((ArraySet) this.mWidgets.get(Integer.valueOf(this.mCurUserId))).addAll(widgets);
                }
            }
        }
    }

    private void addWidget(int userId, String pkgName) {
        if (pkgName != null) {
            if (DEBUG) {
                AwareLog.d(TAG, "addWidget, userId:" + userId + "pkg:" + pkgName);
            }
            synchronized (this.mWidgets) {
                checkAndInitWidgetObj(userId);
                ((ArraySet) this.mWidgets.get(Integer.valueOf(userId))).add(pkgName);
            }
        }
    }

    private void removeWidget(int userId, String pkgName) {
        if (pkgName != null) {
            if (DEBUG) {
                AwareLog.d(TAG, "removeWidget:" + pkgName);
            }
            synchronized (this.mWidgets) {
                checkAndInitWidgetObj(userId);
                ((ArraySet) this.mWidgets.get(Integer.valueOf(userId))).remove(pkgName);
            }
        }
    }

    private void clearWidget() {
        if (DEBUG) {
            AwareLog.d(TAG, "clearWidget");
        }
        synchronized (this.mWidgets) {
            for (Entry<Integer, ArraySet<String>> m : this.mWidgets.entrySet()) {
                ArraySet<String> userWdigets = (ArraySet) m.getValue();
                if (userWdigets != null) {
                    userWdigets.clear();
                }
            }
        }
    }

    private void initVisibleWindows() {
        Parcel parcel = null;
        Parcel parcel2 = null;
        try {
            IBinder windowManager = ServiceManager.getService("window");
            if (windowManager == null) {
                AwareLog.e(TAG, "[ERROR]Connect to window Service failed.");
                return;
            }
            parcel = Parcel.obtain();
            parcel2 = Parcel.obtain();
            parcel.writeInterfaceToken("android.view.IWindowManager");
            windowManager.transact(TRANSACTION_getVisibleWindows, parcel, parcel2, 0);
            synchronized (this.mVisibleWindows) {
                this.mVisibleWindows.clear();
                int size = parcel2.readInt();
                for (int i = 0; i < size; i += MSG_INIT) {
                    this.mVisibleWindows.put(Integer.valueOf(parcel2.readInt()), Integer.valueOf(parcel2.readInt()));
                }
            }
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (RemoteException e) {
            try {
                AwareLog.e(TAG, "[ERROR]Catch RemoteException when initVisibleWindows.");
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
            } catch (Throwable th) {
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
            }
        }
    }

    private void deinitVisibleWindows() {
        synchronized (this.mVisibleWindows) {
            this.mVisibleWindows.clear();
        }
    }

    private void addWindow(int window, int mode) {
        if (window > 0) {
            synchronized (this.mVisibleWindows) {
                this.mVisibleWindows.put(Integer.valueOf(window), Integer.valueOf(mode));
            }
            if (DEBUG) {
                AwareLog.e(TAG, "[addVisibleWindows]:" + window + " [mode]:" + mode);
            }
        }
    }

    private void removeWindow(int window) {
        if (window > 0) {
            synchronized (this.mVisibleWindows) {
                this.mVisibleWindows.remove(Integer.valueOf(window));
            }
            if (DEBUG) {
                AwareLog.d(TAG, "[removeVisibleWindows]:" + window);
            }
        }
    }

    private void updateWindowOps(String pkgName) {
        if (pkgName != null) {
            synchronized (this) {
                synchronized (this.mVisibleWindows) {
                    for (Entry<Integer, Integer> window : this.mVisibleWindows.entrySet()) {
                        AssocBaseRecord record = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(((Integer) window.getKey()).intValue()));
                        if (record != null && record.pkgList != null && record.pkgList.contains(pkgName)) {
                            window.setValue(Integer.valueOf(((AppOpsManager) this.mMtmService.context().getSystemService("appops")).checkOpNoThrow(24, record.uid, pkgName)));
                            break;
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
            case MSG_INIT /*1*/:
                return true;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                return true;
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                return true;
            default:
                return false;
        }
    }

    private String typeToString(int type) {
        switch (type) {
            case MSG_INIT /*1*/:
                return "ADD_ASSOC_BINDSERVICE";
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                return "ADD_ASSOC_PROVIDER";
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                return "DEL_ASSOC_BINDSERVICE";
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                return "APP_ASSOC_PROCESSUPDATE";
            default:
                return "[Error type]" + type;
        }
    }

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
            if (targetUid != ONE_SECOND || !targetName.equals(SYSTEM)) {
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
                        if (relations == null) {
                            if (DEBUG) {
                                AwareLog.e(TAG, "Error type:" + type);
                            }
                            return;
                        }
                        relations.put(targetName, targetUid, baseRecord);
                        this.mAssocRecordMap.put(Integer.valueOf(callerPid), pidRecord);
                        return;
                    }
                    relations = pidRecord.getMap(type);
                    if (relations == null) {
                        if (DEBUG) {
                            AwareLog.e(TAG, "Error type:" + type);
                        }
                        return;
                    }
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
                    return;
                }
            }
        }
    }

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
                if (relations == null) {
                    if (DEBUG) {
                        AwareLog.e(TAG, "Error type:" + type);
                    }
                    return;
                }
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

    private void updateProcessRelation(int pid, int uid, String name, ArrayList<String> pkgList) {
        Throwable th;
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
            synchronized (this) {
                AssocBaseRecord br;
                AssocBaseRecord br2;
                Iterator<Entry<Integer, AssocPidRecord>> it = this.mAssocRecordMap.entrySet().iterator();
                while (it.hasNext()) {
                    AssocPidRecord record = (AssocPidRecord) ((Entry) it.next()).getValue();
                    if (record.pid == pid) {
                        it.remove();
                    } else {
                        try {
                            br = (AssocBaseRecord) record.mAssocBindService.get(name, uid);
                            if (br != null) {
                                br.pid = pid;
                            }
                            br = (AssocBaseRecord) record.mAssocProvider.get(name, uid);
                            if (br != null) {
                                br.pid = pid;
                            }
                        } catch (Throwable th2) {
                            th = th2;
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
                    } catch (Throwable th3) {
                        th = th3;
                        br = br2;
                        throw th;
                    }
                }
                br.processName = name;
                br.uid = uid;
                br.pid = pid;
                br.pkgList.addAll(pkgList);
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
                for (String pkg : pkgList) {
                    pids = (ArraySet) this.mProcPkgMap.get(pkg);
                    if (pids == null) {
                        pids = new ArraySet();
                        pids.add(Integer.valueOf(pid));
                        this.mProcPkgMap.put(pkg, pids);
                    } else {
                        pids.add(Integer.valueOf(pid));
                    }
                }
            }
        }
    }

    private void checkAndInitWidgetObj(int userId) {
        synchronized (this.mWidgets) {
            if (this.mWidgets.get(Integer.valueOf(userId)) == null) {
                this.mWidgets.put(Integer.valueOf(userId), new ArraySet());
            }
        }
    }

    public int getCurUserId() {
        return this.mCurUserId;
    }

    private void initSwitchUser() {
        try {
            UserInfo currentUser = ActivityManagerNative.getDefault().getCurrentUser();
            checkAndInitWidgetObj(currentUser.id);
            this.mCurUserId = currentUser.id;
            ActivityManagerNative.getDefault().registerUserSwitchObserver(this.mUserSwitchObserver);
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
            if (this.mHandler.hasMessages(MSG_INIT)) {
                this.mHandler.removeMessages(MSG_INIT);
            }
            this.mHandler.sendEmptyMessageDelayed(MSG_INIT, REINIT_TIME);
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
                AssocBaseRecord br = new AssocBaseRecord(SYSTEM, ONE_SECOND, this.mMyPid);
                this.mProcPidMap.put(Integer.valueOf(this.mMyPid), br);
                this.mProcInfoMap.put(SYSTEM, ONE_SECOND, br);
                ArraySet<Integer> pids = new ArraySet();
                pids.add(Integer.valueOf(this.mMyPid));
                this.mProcUidMap.put(Integer.valueOf(ONE_SECOND), pids);
            }
            initSwitchUser();
            initVisibleWindows();
            initWidget();
            ArrayMap<Integer, Integer> forePids = new ArrayMap();
            this.mHwAMS.reportAssocEnable(forePids);
            synchronized (this.mForePids) {
                this.mForePids.clear();
                this.mForePids.putAll(forePids);
            }
        }
    }

    private void deinitAssoc() {
        synchronized (this.mForePids) {
            this.mForePids.clear();
        }
        synchronized (this) {
            this.mAssocRecordMap.clear();
            this.mProcInfoMap.getMap().clear();
            this.mProcPidMap.clear();
            this.mProcUidMap.clear();
            this.mProcPkgMap.clear();
        }
        deInitSwitchUser();
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
                        sb.append("        Pid:").append(br.pid).append(",Uid:").append(br.uid).append(",ProcessName:").append(br.processName).append("\n");
                    }
                }
            }
            return sb.toString();
        }
    }

    private Set<String> getPackageNameForUid(int uid, int pidForUid) {
        ArraySet<String> pkgList = new ArraySet();
        synchronized (this) {
            AssocBaseRecord br;
            if (pidForUid != 0) {
                br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pidForUid));
                if (!(br == null || br.pkgList == null)) {
                    pkgList.addAll(br.pkgList);
                }
                return pkgList;
            }
            ArraySet<Integer> pids = (ArraySet) this.mProcUidMap.get(Integer.valueOf(uid));
            if (pids == null || pids.isEmpty()) {
                return pkgList;
            }
            for (Integer pid : pids) {
                br = (AssocBaseRecord) this.mProcPidMap.get(pid);
                if (!(br == null || br.pkgList == null)) {
                    pkgList.addAll(br.pkgList);
                }
            }
            return pkgList;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dump(PrintWriter pw) {
        if (pw != null) {
            if (mEnabled) {
                synchronized (this) {
                    int s = 0;
                    while (true) {
                        if (s < this.mAssocRecordMap.size()) {
                            AssocPidRecord record = (AssocPidRecord) this.mAssocRecordMap.valueAt(s);
                            if (record != null) {
                                pw.println(record);
                            }
                            s += MSG_INIT;
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
                getVisibleWindows(windows);
                boolean flag = true;
                pw.println(AppHibernateCst.INVALID_PKG);
                synchronized (this) {
                    for (Integer intValue : windows) {
                        AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(intValue.intValue()));
                        if (br != null) {
                            if (flag) {
                                pw.println("[WindowList] :");
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
                ArraySet<Integer> widgets = new ArraySet();
                getWidgets(widgets);
                boolean flag = true;
                pw.println(AppHibernateCst.INVALID_PKG);
                synchronized (this) {
                    for (Integer intValue : widgets) {
                        AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(intValue.intValue()));
                        if (br != null) {
                            if (flag) {
                                pw.println("[Widgets] :");
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
                String eclipseTime = AppHibernateCst.INVALID_PKG;
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
                ArraySet<Integer> widgets = new ArraySet();
                getWidgets(widgets);
                pw.println("Widget Size: " + widgets.size());
                ArraySet<Integer> windows = new ArraySet();
                getVisibleWindows(windows);
                pw.println("Window Size: " + windows.size());
                synchronized (this) {
                    pidSize = this.mAssocRecordMap.size();
                    for (int s = 0; s < pidSize; s += MSG_INIT) {
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
                        for (i = 0; i < NP; i += MSG_INIT) {
                            brs = (SparseArray) record.mAssocBindService.getMap().valueAt(i);
                            NB = brs.size();
                            bindSize += NB;
                            for (j = 0; j < NB; j += MSG_INIT) {
                                bindSizeAll += ((AssocBaseRecord) brs.valueAt(j)).mComponents.size();
                            }
                        }
                        NP = record.mAssocProvider.getMap().size();
                        for (i = 0; i < NP; i += MSG_INIT) {
                            brs = (SparseArray) record.mAssocProvider.getMap().valueAt(i);
                            NB = brs.size();
                            providerSize += NB;
                            for (j = 0; j < NB; j += MSG_INIT) {
                                providerSizeAll += ((AssocBaseRecord) brs.valueAt(j)).mComponents.size();
                            }
                        }
                        ArraySet<Integer> pids = (ArraySet) this.mProcUidMap.get(Integer.valueOf(record.uid));
                        if (pids != null) {
                            sameuid = pids.size() - 1;
                        }
                        int curpidsize = bindSize + providerSize;
                        int curcompsize = bindSizeAll + providerSizeAll;
                        int curpiduidsize = curpidsize + sameuid;
                        pidsize += curpidsize;
                        compSize += curcompsize;
                        int i2 = record.uid;
                        pw.println("[" + r0 + "][" + record.processName + "]: " + "bind[" + bindSize + "-" + bindSizeAll + "]" + "provider[" + providerSize + "-" + providerSizeAll + "]" + "SameUID[" + sameuid + "]" + "pids:[" + curpidsize + "]" + "comps:[" + curcompsize + "]" + "piduids:[" + curpiduidsize + "]");
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

    private void recordWidgetDetail(Set<Integer> list) {
        if (list != null && !list.isEmpty()) {
            synchronized (this) {
                for (Integer intValue : list) {
                    AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(intValue.intValue()));
                    if (br != null) {
                        AwareLog.i(TAG, "[Widget]Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                    }
                }
            }
        }
    }

    private void recordAssocDetail(int pid) {
        synchronized (this) {
            AssocPidRecord record = (AssocPidRecord) this.mAssocRecordMap.get(Integer.valueOf(pid));
            if (record != null) {
                AwareLog.i(TAG, AppHibernateCst.INVALID_PKG + record);
            } else {
                AssocBaseRecord br = (AssocBaseRecord) this.mProcPidMap.get(Integer.valueOf(pid));
                if (br != null) {
                    AwareLog.i(TAG, "Pid:" + br.pid + ",Uid:" + br.uid + ",ProcessName:" + br.processName);
                }
                AwareLog.i(TAG, AppHibernateCst.INVALID_PKG + sameUid(pid));
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
}
