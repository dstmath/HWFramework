package com.android.server.rms.iaware.appmng;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.dualfwk.AwareMiddleware;
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.appmng.AwareAudioFocusManager;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.android.os.SystemClockEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppKeyBackgroup {
    private static final String[] APPTYPESTRING = {"TYPE_UNKNOW", "TYPE_LAUNCHER", "TYPE_SMS", "TYPE_EMAIL", "TYPE_INPUTMETHOD", "TYPE_GAME", "TYPE_BROWSER", "TYPE_EBOOK", "TYPE_VIDEO", "TYPE_SCRLOCK", "TYPE_CLOCK", "TYPE_IM", "TYPE_MUSIC"};
    private static final String CALL_APP_PKG = ("watch".equals(SystemPropertiesEx.get("ro.build.characteristics")) ? CALL_APP_PKG_WATCH : "com.android.incallui");
    private static final String CALL_APP_PKG_PHONE = "com.android.incallui";
    private static final String CALL_APP_PKG_WATCH = "com.huawei.harmonyos.call";
    private static final long DECAY_TIME = 60000;
    private static final long DECAY_UPLOAD_DL_TIME = 10000;
    private static final long DECAY_WAIT_FOCUS_LOSS_TIME = 5000;
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    private static final int INCALLUI_UID = 0;
    private static final Object LOCK = new Object();
    private static final int MSG_APP_PROCESSDIED = 2;
    private static final int MSG_PGSDK_INIT = 3;
    private static final int MSG_REMOVE_DECAY_STATE = 1;
    private static final long PGSDK_REINIT_TIME = 2000;
    private static final int PID_INVALID = -1;
    private static final String[] STATESTRING = {"STATE_NULL", "STATE_AUDIO_IN", "STATE_AUDIO_OUT", "STATE_GPS", "STATE_SENSOR", "STATE_UPLOAD_DL"};
    public static final int STATE_ALL = 100;
    public static final int STATE_AUDIO_IN = 1;
    public static final int STATE_AUDIO_OUT = 2;
    public static final int STATE_GPS = 3;
    public static final int STATE_IMEMAIL = 99;
    public static final int STATE_KEY_BG = 0;
    public static final int STATE_KEY_BG_INVALID = -1;
    public static final int STATE_NAT_TIMEOUT = 11;
    public static final int STATE_SENSOR = 4;
    private static final int STATE_SIZE = STATESTRING.length;
    public static final int STATE_UPLOAD_DL = 5;
    private static final String TAG = "AwareAppKeyBackgroup";
    private static final int TYPE_SIZE = APPTYPESTRING.length;
    private static boolean sDebug = false;
    private static AwareAppKeyBackgroup sInstance = null;
    private AppKeyHandler mAppKeyHandler;
    private final ArraySet<Integer> mAudioCacheUids = new ArraySet<>();
    private PhoneStateListener mCallStateListener = new PhoneStateListener() {
        /* class com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.AnonymousClass1 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            if (AwareAppKeyBackgroup.sDebug) {
                AwareLog.d(AwareAppKeyBackgroup.TAG, "onCallStateChanged state :" + state);
            }
            AwareAppKeyBackgroup.this.updateCallState(state);
        }
    };
    private final ArrayMap<IAwareStateCallback, ArraySet<Integer>> mCallbacks = new ArrayMap<>();
    private Context mContext = null;
    private AwareAudioFocusObserver mFocusObserver = new AwareAudioFocusObserver();
    private final SparseArray<SensorRecord> mHistorySensorRecords = new SparseArray<>();
    private HwActivityManagerService mHwAMS = null;
    private boolean mIsAbroadArea = false;
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private AtomicBoolean mIsInitializing = new AtomicBoolean(false);
    private final SparseSet mKeyBackgroupPids = new SparseSet();
    private final ArraySet<String> mKeyBackgroupPkgs = new ArraySet<>();
    private final SparseSet mKeyBackgroupUids = new SparseSet();
    private AtomicBoolean mLastSetting = new AtomicBoolean(false);
    private final Object mLock = new Object();
    private int mNatTimeout = 0;
    private PackageManager mPM = null;
    private PowerKit mPgSdk = null;
    private AwareABGProcessObserver mProcessObserver = new AwareABGProcessObserver();
    private final ArrayList<SparseSet> mScenePidArray = new ArrayList<>();
    private final ArrayList<ArraySet<String>> mScenePkgArray = new ArrayList<>();
    private final ArrayList<SparseSet> mSceneUidArray = new ArrayList<>();
    private final List<DecayInfo> mStateEventDecayInfos = new ArrayList();
    private PowerKit.Sink mStateRecognitionListener = new PowerKit.Sink() {
        /* class com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.AnonymousClass2 */

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            if (!AwareAppKeyBackgroup.this.mIsInitialized.get() || !AwareAppKeyBackgroup.this.checkCallingPermission() || pid == Process.myPid()) {
                return;
            }
            if (stateType == 11) {
                AwareAppKeyBackgroup.this.setNatTimeout(pid);
            } else if (!AwareAppKeyBackgroup.this.shouldFilter(stateType, uid)) {
                if (stateType == 4) {
                    AwareAppKeyBackgroup awareAppKeyBackgroup = AwareAppKeyBackgroup.this;
                    boolean z = true;
                    if (eventType != 1) {
                        z = false;
                    }
                    awareAppKeyBackgroup.handleSensorEvent(uid, pid, z);
                    return;
                }
                AwareMiddleware.getInstance().sendMsgStateChange(stateType, eventType, pid, uid);
                long timeStamp = 0;
                if (AwareAppKeyBackgroup.sDebug) {
                    AwareLog.d(AwareAppKeyBackgroup.TAG, "PowerKit Sink onStateChanged");
                    timeStamp = SystemClockEx.currentTimeMicro();
                }
                if (!AwareAppKeyBackgroup.this.isStateChangedDecay(stateType, eventType, pid, pkg, uid)) {
                    AwareAppKeyBackgroup.this.updateSceneState(stateType, eventType, pid, pkg, uid);
                    if (AwareAppKeyBackgroup.sDebug) {
                        AwareLog.d(AwareAppKeyBackgroup.TAG, "Update Scene state using " + (SystemClockEx.currentTimeMicro() - timeStamp) + " us");
                    }
                }
            }
        }
    };
    private final ArraySet<String> mSysAudioInPkgsCache = new ArraySet<>();
    private final ArrayMap<Integer, AudioCacheInfo> mSystemAudioCacheInfos = new ArrayMap<>();
    private final ArraySet<String> mSystemAudioOutPkgsCache = new ArraySet<>();

    public interface IAwareStateCallback {
        void onStateChanged(int i, int i2, int i3, int i4);
    }

    /* access modifiers changed from: package-private */
    public class AudioCacheInfo {
        public int pid;
        public ArraySet<String> pkgs = new ArraySet<>();
        public int uid;

        public AudioCacheInfo(int pidTmp, int uidTmp, ArrayList<String> pkgsTmp) {
            this.pid = pidTmp;
            this.uid = uidTmp;
            if (pkgsTmp != null) {
                this.pkgs.addAll(pkgsTmp);
            }
        }

        public String toString() {
            return "[pid=" + this.pid + " uid=" + this.uid + " pkgs =" + this.pkgs + "]";
        }
    }

    /* access modifiers changed from: private */
    public static String stateToString(int state) {
        if (state < 0 || state >= STATE_SIZE) {
            return "STATE_NULL";
        }
        return STATESTRING[state];
    }

    private static String typeToString(int type) {
        if (type < 0 || type >= TYPE_SIZE) {
            return "TYPE_UNKNOW";
        }
        return APPTYPESTRING[type];
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkCallingPermission() {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        if (pid == Process.myPid() || uid == 0 || uid == 1000) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getPkgNameByUid(int uid) {
        this.mPM = this.mContext.getPackageManager();
        PackageManager packageManager = this.mPM;
        if (packageManager == null) {
            return null;
        }
        return packageManager.getNameForUid(uid);
    }

    private int getAppTypeFromHabit(int uid) {
        Context context = this.mContext;
        if (context == null) {
            return -1;
        }
        if (this.mPM == null) {
            this.mPM = context.getPackageManager();
            if (this.mPM == null) {
                AwareLog.e(TAG, "Failed to get PackageManager");
                return -1;
            }
        }
        String[] pkgNames = this.mPM.getPackagesForUid(uid);
        if (pkgNames == null) {
            AwareLog.e(TAG, "Failed to get package name for uid: " + uid);
            return -1;
        }
        for (String pkgName : pkgNames) {
            int type = AppTypeRecoManager.getInstance().getAppType(pkgName);
            if (type != -1) {
                return type;
            }
        }
        return -1;
    }

    private boolean isNaviOrSportApp(int uid) {
        int type = getAppTypeFromHabit(uid);
        if (sDebug) {
            AwareLog.d(TAG, "getAppTypeFromHabit uid " + uid + " type : " + type);
        }
        if (type == -1 || type == 2 || type == 3 || type > 255) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldFilter(int stateType, int uid) {
        return (stateType == 3 || stateType == 4) && !isNaviOrSportApp(uid);
    }

    /* access modifiers changed from: private */
    public class AwareAudioFocusObserver implements AwareAudioFocusManager.AudioFocusObserver {
        private AwareAudioFocusObserver() {
        }

        @Override // com.android.server.rms.iaware.appmng.AwareAudioFocusManager.AudioFocusObserver
        public void onFocusRelease(int uid) {
            AwareAppKeyBackgroup.this.clearAudioCache(uid);
        }

        @Override // com.android.server.rms.iaware.appmng.AwareAudioFocusManager.AudioFocusObserver
        public void onFocusPermanentLoss(int uid) {
            ArrayList<DecayInfo> decayInfos = AwareAppKeyBackgroup.this.getDecayInfos(2, 2, uid);
            if (!(decayInfos == null || decayInfos.isEmpty())) {
                int listSize = decayInfos.size();
                for (int i = 0; i < listSize; i++) {
                    DecayInfo decayInfo = decayInfos.get(i);
                    if (decayInfo != null && AwareAppKeyBackgroup.this.mAppKeyHandler.hasMessages(1, decayInfo)) {
                        AwareLog.i(AwareAppKeyBackgroup.TAG, "Aware advance handle message because focus loss uid:" + uid);
                        AwareAppKeyBackgroup.this.mAppKeyHandler.removeMessages(1, decayInfo);
                        AwareAppKeyBackgroup.this.mAppKeyHandler.sendMessage(AwareAppKeyBackgroup.this.mAppKeyHandler.obtainMessage(1, decayInfo));
                    }
                }
            }
        }
    }

    private void registerAudioFocusObserver() {
        AwareAudioFocusManager manager = AwareAudioFocusManager.getInstance();
        if (manager != null) {
            manager.registerFocusChangeObserver(this.mFocusObserver);
        } else {
            AwareLog.i(TAG, "Aware audio focus manager is null");
        }
    }

    private void unregisterAudioFocusObserver() {
        AwareAudioFocusManager manager = AwareAudioFocusManager.getInstance();
        if (manager != null) {
            manager.unregisterFocusChangeObserver();
        } else {
            AwareLog.i(TAG, "Aware audio focus manager is null");
        }
    }

    private void registerProcessObserver() {
        AwareCallback.getInstance().registerProcessObserver(this.mProcessObserver);
    }

    private void unregisterProcessObserver() {
        AwareCallback.getInstance().unregisterProcessObserver(this.mProcessObserver);
    }

    /* access modifiers changed from: package-private */
    public class AwareABGProcessObserver extends IProcessObserverEx {
        AwareABGProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (!AwareIntelligentRecg.getInstance().isRecogOptEnable()) {
                AwareLog.i(AwareAppKeyBackgroup.TAG, "Aware recog opt unalbe");
                return;
            }
            AwareAudioFocusManager manager = AwareAudioFocusManager.getInstance();
            if (manager != null) {
                manager.reportForegroundActivitiesChanged(pid, uid, foregroundActivities);
            } else {
                AwareLog.i(AwareAppKeyBackgroup.TAG, "Aware audio focus manager is null");
            }
        }

        public void onProcessDied(int pid, int uid) {
            synchronized (AwareAppKeyBackgroup.this.mLock) {
                AwareAppKeyBackgroup.this.getPkgNameByUid(uid);
                boolean isKbgPid = AwareAppKeyBackgroup.this.mKeyBackgroupPids.contains(pid);
                boolean isKbgUid = AwareAppKeyBackgroup.this.mKeyBackgroupUids.contains(uid);
                if (isKbgPid || isKbgUid) {
                    if (AwareAppKeyBackgroup.sDebug) {
                        AwareLog.d(AwareAppKeyBackgroup.TAG, "onProcessDied pid " + pid + " uid " + uid);
                    }
                    Message observerMsg = AwareAppKeyBackgroup.this.mAppKeyHandler.obtainMessage();
                    observerMsg.arg1 = pid;
                    observerMsg.arg2 = uid;
                    observerMsg.what = 2;
                    AwareAppKeyBackgroup.this.mAppKeyHandler.sendMessage(observerMsg);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerCallStateListener(Context cxt) {
        Object obj;
        if (cxt != null && (obj = cxt.getSystemService("phone")) != null && (obj instanceof TelephonyManager)) {
            ((TelephonyManager) obj).listen(this.mCallStateListener, 32);
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterCallStateListener(Context cxt) {
        Object obj;
        if (cxt != null && (obj = cxt.getSystemService("phone")) != null && (obj instanceof TelephonyManager)) {
            ((TelephonyManager) obj).listen(this.mCallStateListener, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCallState(int state) {
        if (this.mIsInitialized.get()) {
            int eventType = state == 0 ? 2 : 1;
            AwareMiddleware.getInstance().sendMsgStateChange(2, eventType, 1, 1001);
            synchronized (this.mLock) {
                updateSceneArrayLocked(2, eventType, 0, CALL_APP_PKG, 0);
            }
            resetAudioCache(2, eventType, 0);
        }
    }

    private AwareAppKeyBackgroup() {
        initHandler();
        this.mHwAMS = HwActivityManagerService.self();
    }

    public void registerStateCallback(IAwareStateCallback callback, int stateType) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                ArraySet<Integer> states = this.mCallbacks.get(callback);
                if (states == null) {
                    ArraySet<Integer> states2 = new ArraySet<>();
                    states2.add(Integer.valueOf(stateType));
                    this.mCallbacks.put(callback, states2);
                } else {
                    states.add(Integer.valueOf(stateType));
                }
            }
        }
    }

    public void unregisterStateCallback(IAwareStateCallback callback, int stateType) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                ArraySet<Integer> states = this.mCallbacks.get(callback);
                if (states != null) {
                    states.remove(Integer.valueOf(stateType));
                    if (states.size() == 0) {
                        this.mCallbacks.remove(callback);
                    }
                }
            }
        }
    }

    private void notifyStateChange(int stateType, int eventType, int pid, int uid) {
        if (sDebug) {
            AwareLog.d(TAG, "keyBackgroup onStateChanged e:" + eventType + " pid:" + pid + " uid:" + uid);
        }
        synchronized (this.mCallbacks) {
            if (!this.mCallbacks.isEmpty()) {
                for (Map.Entry<IAwareStateCallback, ArraySet<Integer>> m : this.mCallbacks.entrySet()) {
                    IAwareStateCallback callback = m.getKey();
                    ArraySet<Integer> states = m.getValue();
                    if (states != null && (states.contains(Integer.valueOf(stateType)) || states.contains(100))) {
                        callback.onStateChanged(stateType, eventType, pid == -1 ? 0 : pid, uid);
                    }
                }
            }
        }
    }

    private void initialize(Context context) {
        this.mLastSetting.set(true);
        if (!this.mIsInitialized.get() && !this.mIsInitializing.get()) {
            this.mContext = context;
            this.mIsAbroadArea = AwareDefaultConfigList.isAbroadArea();
            registerProcessObserver();
            registerCallStateListener(this.mContext);
            registerAudioFocusObserver();
            if (this.mAppKeyHandler.hasMessages(3)) {
                this.mAppKeyHandler.removeMessages(3);
            }
            this.mAppKeyHandler.sendEmptyMessage(3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doInitialize() {
        this.mIsInitializing.set(true);
        synchronized (this.mLock) {
            if (this.mScenePidArray.isEmpty()) {
                for (int i = 0; i < STATE_SIZE; i++) {
                    this.mScenePidArray.add(new SparseSet());
                    this.mSceneUidArray.add(new SparseSet());
                    this.mScenePkgArray.add(new ArraySet<>());
                }
            }
        }
        if (!ensureInitialize()) {
            if (this.mAppKeyHandler.hasMessages(3)) {
                this.mAppKeyHandler.removeMessages(3);
            }
            this.mAppKeyHandler.sendEmptyMessageDelayed(3, PGSDK_REINIT_TIME);
            return;
        }
        this.mIsInitializing.set(false);
        checkLastSetting();
    }

    private boolean ensureInitialize() {
        if (!this.mIsInitialized.get()) {
            this.mPgSdk = PowerKit.getInstance();
            PowerKit powerKit = this.mPgSdk;
            if (powerKit == null) {
                return this.mIsInitialized.get();
            }
            try {
                powerKit.enableStateEvent(this.mStateRecognitionListener, 1);
                this.mPgSdk.enableStateEvent(this.mStateRecognitionListener, 2);
                this.mPgSdk.enableStateEvent(this.mStateRecognitionListener, 3);
                this.mPgSdk.enableStateEvent(this.mStateRecognitionListener, 4);
                this.mPgSdk.enableStateEvent(this.mStateRecognitionListener, 5);
                this.mPgSdk.enableStateEvent(this.mStateRecognitionListener, 11);
                resumeAppStates(this.mContext);
                this.mIsInitialized.set(true);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "PG Exception e: initialize pgdskd error!");
            }
        }
        if (sDebug) {
            AwareLog.d(TAG, "AwareAppKeyBackgroup ensureInitialize:" + this.mIsInitialized.get());
        }
        return this.mIsInitialized.get();
    }

    private void deInitialize() {
        unregisterProcessObserver();
        unregisterCallStateListener(this.mContext);
        unregisterAudioFocusObserver();
        this.mLastSetting.set(false);
        if (this.mIsInitialized.get()) {
            PowerKit powerKit = this.mPgSdk;
            if (powerKit != null) {
                try {
                    powerKit.disableStateEvent(this.mStateRecognitionListener, 1);
                    this.mPgSdk.disableStateEvent(this.mStateRecognitionListener, 2);
                    this.mPgSdk.disableStateEvent(this.mStateRecognitionListener, 3);
                    this.mPgSdk.disableStateEvent(this.mStateRecognitionListener, 4);
                    this.mPgSdk.disableStateEvent(this.mStateRecognitionListener, 5);
                    this.mPgSdk.disableStateEvent(this.mStateRecognitionListener, 11);
                } catch (RemoteException e) {
                    AwareLog.e(TAG, "PG Exception e: deinitialize pgsdk error!");
                } catch (Throwable th) {
                    clearCache();
                    this.mAppKeyHandler.removeCallbacksAndMessages(null);
                    this.mIsInitialized.set(false);
                    throw th;
                }
                clearCache();
                this.mAppKeyHandler.removeCallbacksAndMessages(null);
                this.mIsInitialized.set(false);
                checkLastSetting();
            } else {
                return;
            }
        } else {
            this.mAppKeyHandler.removeMessages(3);
        }
        if (sDebug) {
            AwareLog.d(TAG, "PGFeature deInitialize:" + this.mIsInitialized.get());
        }
    }

    private void clearCache() {
        synchronized (this.mLock) {
            this.mScenePidArray.clear();
            this.mScenePkgArray.clear();
            this.mSceneUidArray.clear();
            this.mKeyBackgroupPids.clear();
            this.mKeyBackgroupUids.clear();
            this.mKeyBackgroupPkgs.clear();
            this.mHistorySensorRecords.clear();
        }
        synchronized (this.mStateEventDecayInfos) {
            this.mStateEventDecayInfos.clear();
        }
        synchronized (this.mAudioCacheUids) {
            this.mAudioCacheUids.clear();
        }
        synchronized (this.mSystemAudioCacheInfos) {
            this.mSystemAudioCacheInfos.clear();
        }
        synchronized (this.mSystemAudioOutPkgsCache) {
            this.mSystemAudioOutPkgsCache.clear();
        }
        synchronized (this.mSysAudioInPkgsCache) {
            this.mSysAudioInPkgsCache.clear();
        }
    }

    private void checkLastSetting() {
        if (this.mContext != null && this.mIsInitialized.get() != this.mLastSetting.get()) {
            if (this.mLastSetting.get()) {
                getInstance().initialize(this.mContext);
            } else {
                getInstance().deInitialize();
            }
        }
    }

    public static void enable(Context context) {
        if (sDebug) {
            AwareLog.d(TAG, "KeyBackGroup Feature enable!!!");
        }
        getInstance().initialize(context);
    }

    public static void disable() {
        if (sDebug) {
            AwareLog.d(TAG, "KeyBackGroup Feature disable!!!");
        }
        getInstance().deInitialize();
    }

    public static void enableDebug() {
        sDebug = true;
    }

    public static void disableDebug() {
        sDebug = false;
    }

    public static AwareAppKeyBackgroup getInstance() {
        AwareAppKeyBackgroup awareAppKeyBackgroup;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareAppKeyBackgroup();
            }
            awareAppKeyBackgroup = sInstance;
        }
        return awareAppKeyBackgroup;
    }

    private void resumeAppStates(Context context) {
        if (context != null) {
            ArrayList<ProcessInfo> procs = ProcessInfoCollector.getInstance().getProcessInfoList();
            long timeStamp = 0;
            if (sDebug) {
                timeStamp = SystemClockEx.currentTimeMicro();
            }
            int size = procs.size();
            for (int j = 0; j < size; j++) {
                ProcessInfo procInfo = procs.get(j);
                if (procInfo != null) {
                    resumeAppState(context, procInfo);
                }
            }
            if (sDebug) {
                AwareLog.d(TAG, "resumeAppStates done using " + (SystemClockEx.currentTimeMicro() - timeStamp) + " us");
            }
        }
    }

    private void resumeAppState(Context context, ProcessInfo procInfo) {
        boolean state;
        int k;
        boolean state2;
        int pid = procInfo.mPid;
        int uid = procInfo.mUid;
        ArrayList<String> packages = procInfo.mPackageName;
        for (int i = 1; i <= 5; i++) {
            if (!shouldFilter(i, uid)) {
                if (i <= 2) {
                    try {
                        state2 = this.mPgSdk.checkStateByPid(context, pid, i);
                    } catch (RemoteException e) {
                        AwareLog.e(TAG, "checkStateByPid occur exception.");
                        state2 = false;
                    }
                    if (state2) {
                        AwareMiddleware.getInstance().sendMsgStateChange(i, 1, pid, uid);
                        updateSceneState(i, 1, pid, null, uid);
                    }
                } else if (i == 4) {
                    initAppSensorState(context, uid);
                } else {
                    int size = packages.size();
                    int k2 = 0;
                    while (k2 < size) {
                        try {
                            state = this.mPgSdk.checkStateByPkg(context, packages.get(k2), i);
                        } catch (RemoteException e2) {
                            AwareLog.e(TAG, "checkStateByPkg occur exception.");
                            state = false;
                        }
                        if (state) {
                            k = k2;
                            updateSceneState(i, 1, 0, null, uid);
                        } else {
                            k = k2;
                        }
                        k2 = k + 1;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isStateChangedDecay(int stateType, int eventType, int pid, String pkg, int uid) {
        if (this.mContext == null) {
            return false;
        }
        if (!isNeedDecay(stateType, eventType)) {
            checkStateChangedDecay(stateType, eventType, pid, pkg, uid);
            return false;
        } else if (!isAppAlive(this.mContext, uid)) {
            return false;
        } else {
            long decayTime = getDecayTime(stateType, pid, pkg, uid);
            if (decayTime == 0) {
                return false;
            }
            sendDecayMesssage(1, addDecayInfo(stateType, eventType, pid, pkg, uid), decayTime);
            return true;
        }
    }

    private boolean isNeedDecay(int stateType, int eventType) {
        return (stateType == 2 || stateType == 5) && eventType == 2;
    }

    private void checkStateChangedDecay(int stateType, int eventType, int pid, String pkg, int uid) {
        DecayInfo decayInfo;
        if (isNeedCheckDecay(stateType, eventType) && (decayInfo = removeDecayInfo(stateType, 2, pid, pkg, uid)) != null) {
            if (sDebug) {
                AwareLog.d(TAG, "checkStateChangedDecay start has message 1 ? " + this.mAppKeyHandler.hasMessages(1, decayInfo) + " decayinfo" + decayInfo + " size " + getStateEventDecayInfosSize());
            }
            this.mAppKeyHandler.removeMessages(1, decayInfo);
        }
    }

    private boolean isNeedCheckDecay(int stateType, int eventType) {
        if ((stateType == 2 || stateType == 5) && eventType == 1 && getStateEventDecayInfosSize() > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<DecayInfo> getDecayInfos(int stateType, int eventType, int uid) {
        ArrayList<DecayInfo> decayInfos = null;
        synchronized (this.mStateEventDecayInfos) {
            for (DecayInfo info : this.mStateEventDecayInfos) {
                if (info.getStateType() == stateType && info.getEventType() == eventType && info.getUid() == uid) {
                    if (decayInfos == null) {
                        decayInfos = new ArrayList<>();
                    }
                    decayInfos.add(info);
                }
            }
        }
        return decayInfos;
    }

    private DecayInfo getDecayInfo(int stateType, int eventType, int pid, String pkg, int uid) {
        synchronized (this.mStateEventDecayInfos) {
            for (DecayInfo info : this.mStateEventDecayInfos) {
                if (info.getStateType() == stateType && info.getEventType() == eventType && info.getPid() == pid && info.getUid() == uid) {
                    return info;
                }
            }
            return null;
        }
    }

    private boolean existDecayInfo(int stateType, int eventType, int pid, String pkg, int uid) {
        synchronized (this.mStateEventDecayInfos) {
            for (DecayInfo info : this.mStateEventDecayInfos) {
                if (info.getStateType() == stateType && info.getEventType() == eventType && info.getPid() == pid && info.getUid() == uid) {
                    return true;
                }
            }
            return false;
        }
    }

    private DecayInfo addDecayInfo(int stateType, int eventType, int pid, String pkg, int uid) {
        DecayInfo decayInfo;
        synchronized (this.mStateEventDecayInfos) {
            decayInfo = getDecayInfo(stateType, eventType, pid, pkg, uid);
            if (decayInfo == null) {
                decayInfo = new DecayInfo(stateType, eventType, pid, pkg, uid);
                this.mStateEventDecayInfos.add(decayInfo);
            }
        }
        return decayInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DecayInfo removeDecayInfo(int stateType, int eventType, int pid, String pkg, int uid) {
        synchronized (this.mStateEventDecayInfos) {
            DecayInfo decayInfo = getDecayInfo(stateType, eventType, pid, pkg, uid);
            if (decayInfo == null) {
                return null;
            }
            this.mStateEventDecayInfos.remove(decayInfo);
            return decayInfo;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getStateEventDecayInfosSize() {
        int size;
        synchronized (this.mStateEventDecayInfos) {
            size = this.mStateEventDecayInfos.size();
        }
        return size;
    }

    private List<ProcessInfo> getProcessesByUid(int uid) {
        ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procList.isEmpty()) {
            return null;
        }
        List<ProcessInfo> procs = new ArrayList<>();
        int size = procList.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo info = procList.get(i);
            if (info != null && uid == info.mUid) {
                procs.add(info);
            }
        }
        return procs;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAudioCache(int type, int pid, int uid) {
        if (type == 2) {
            if (!AwareIntelligentRecg.getInstance().isRecogOptEnable()) {
                AwareLog.i(TAG, "Aware recog opt disable");
                clearAudioCache(uid);
                return;
            }
            AwareAudioFocusManager manager = AwareAudioFocusManager.getInstance();
            if (manager == null || manager.isAppForeground(uid) || !manager.isTransientLoss(uid)) {
                clearAudioCache(uid);
                return;
            }
            synchronized (this.mAudioCacheUids) {
                this.mAudioCacheUids.add(Integer.valueOf(uid));
                AwareLog.i(TAG, "Aware audio cache add uid:" + uid);
                addSystemAudioOutInfos(pid, uid);
            }
        }
    }

    public boolean isAudioCache(int uid) {
        boolean contains;
        if (!this.mIsInitialized.get()) {
            return false;
        }
        synchronized (this.mAudioCacheUids) {
            contains = this.mAudioCacheUids.contains(Integer.valueOf(uid));
        }
        return contains;
    }

    private boolean isAppAlive(Context cxt, int uid) {
        if (cxt == null) {
            return false;
        }
        HwActivityManagerService hwActivityManagerService = this.mHwAMS;
        Map<Integer, AwareProcessBaseInfo> baseInfos = hwActivityManagerService != null ? hwActivityManagerService.getAllProcessBaseInfo() : null;
        if (baseInfos == null || baseInfos.isEmpty()) {
            return false;
        }
        for (Map.Entry<Integer, AwareProcessBaseInfo> entry : baseInfos.entrySet()) {
            AwareProcessBaseInfo valueInfo = entry.getValue();
            if (valueInfo != null && valueInfo.copy().uid == uid) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeDecayForProcessDied(int stateType, int eventType, int pid, String pkg, int uid) {
        DecayInfo decayInfo;
        if (isNeedDecay(stateType, eventType) && existDecayInfo(stateType, eventType, pid, pkg, uid)) {
            if ((pid != 0 || !isAppAlive(this.mContext, uid)) && (decayInfo = removeDecayInfo(stateType, eventType, pid, pkg, uid)) != null) {
                updateSceneState(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                this.mAppKeyHandler.removeMessages(1, decayInfo);
            }
        }
    }

    public boolean checkIsKeyBackgroupInternal(int pid, int uid) {
        if (!this.mIsInitialized.get()) {
            return false;
        }
        synchronized (this.mLock) {
            if (this.mKeyBackgroupPids.contains(pid)) {
                return true;
            }
            if (this.mKeyBackgroupUids.contains(uid)) {
                return true;
            }
            return false;
        }
    }

    public int getKeyBackgroupTypeInternal(int pid, int uid, List<String> pkgs) {
        if (!this.mIsInitialized.get()) {
            return -1;
        }
        synchronized (this.mAudioCacheUids) {
            if (this.mAudioCacheUids.contains(Integer.valueOf(uid))) {
                return 2;
            }
        }
        synchronized (this.mLock) {
            if (this.mKeyBackgroupPids.contains(pid)) {
                return getKeyBackgroupTypeByPidLocked(pid);
            } else if (this.mKeyBackgroupUids.contains(uid)) {
                return getKeyBackgroupTypeByUidLocked(uid);
            } else if (pkgs == null) {
                return -1;
            } else {
                return getKeyBackgroupTypeByPkgsLocked(pkgs);
            }
        }
    }

    public boolean checkIsKeyBackgroup(int pid, int uid) {
        if (!this.mIsInitialized.get()) {
            return false;
        }
        if (AwareDefaultConfigList.getInstance().getKeyHabitAppList().contains(InnerUtils.getAwarePkgName(pid))) {
            return true;
        }
        return checkIsKeyBackgroupInternal(pid, uid);
    }

    public boolean checkKeyBackgroupByState(int state, int pid, int uid, List<String> pkgs) {
        if (!this.mIsInitialized.get()) {
            return false;
        }
        if (isInAudioCache(state, pid, uid, pkgs)) {
            return true;
        }
        synchronized (this.mLock) {
            if (this.mSceneUidArray.isEmpty()) {
                return false;
            }
            if (isUidInState(state, uid, pkgs)) {
                return true;
            }
            if (this.mScenePidArray.get(state).contains(pid)) {
                return true;
            }
            if (pkgs != null) {
                if (!pkgs.isEmpty()) {
                    ArraySet<String> mScenePkgArrayItem = this.mScenePkgArray.get(state);
                    if (!mScenePkgArrayItem.isEmpty()) {
                        for (String pkg : pkgs) {
                            if (pkg != null) {
                                if (mScenePkgArrayItem.contains(pkg)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            }
            return false;
        }
    }

    private boolean isUidInState(int state, int uid, List<String> pkgs) {
        if (state != 2 || !isSystemUid(uid)) {
            if (state != 1 || !isSystemUid(uid)) {
                if (this.mSceneUidArray.get(state).contains(uid)) {
                    return true;
                }
                return false;
            } else if (!this.mSceneUidArray.get(state).contains(uid) || !isInSysAudioInPkgsCache(pkgs)) {
                return false;
            } else {
                return true;
            }
        } else if (!this.mSceneUidArray.get(state).contains(uid) || !isInSystemAudioOutPkgsCache(pkgs)) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSceneArrayProcessDied(int uid, int pid) {
        synchronized (this.mLock) {
            boolean appAlive = isAppAlive(this.mContext, uid);
            int size = this.mScenePidArray.size();
            for (int i = 1; i < size; i++) {
                if (this.mScenePidArray.get(i).contains(pid)) {
                    this.mSceneUidArray.get(i).remove(uid);
                    this.mScenePidArray.get(i).remove(pid);
                    this.mKeyBackgroupUids.remove(pid);
                } else if (uid != 0 && !appAlive) {
                    this.mSceneUidArray.get(i).remove(uid);
                }
            }
            updateKbgUidsArrayLocked();
        }
    }

    private void updateScenePidArrayForInvalidPid(int uid) {
        if (sDebug) {
            AwareLog.d(TAG, "updateScenePidArrayForInvalidPid uid " + uid);
        }
        List<ProcessInfo> procs = getProcessesByUid(uid);
        if (!(procs == null || procs.isEmpty())) {
            for (ProcessInfo info : procs) {
                if (info != null) {
                    if (sDebug) {
                        AwareLog.d(TAG, "updateScenePidArrayForInvalidPid pid " + info.mPid);
                    }
                    synchronized (this.mLock) {
                        int size = this.mScenePidArray.size();
                        for (int i = 1; i < size; i++) {
                            this.mScenePidArray.get(i).remove(info.mPid);
                        }
                        this.mKeyBackgroupUids.remove(info.mPid);
                    }
                }
            }
        }
    }

    private void updateKbgUidsArrayLocked() {
        this.mKeyBackgroupUids.clear();
        int size = this.mSceneUidArray.size();
        for (int i = 0; i < size; i++) {
            this.mKeyBackgroupUids.addAll(this.mSceneUidArray.get(i));
        }
    }

    private void updateSceneArrayLocked(int stateType, int eventType, int pid, String pkg, int uid) {
        if (!this.mSceneUidArray.isEmpty()) {
            if (eventType == 1) {
                if (pid != 0) {
                    this.mScenePidArray.get(stateType).add(pid);
                }
                if (uid != 0) {
                    this.mSceneUidArray.get(stateType).add(uid);
                }
                if (pkg != null && !pkg.isEmpty()) {
                    this.mScenePkgArray.get(stateType).add(pkg);
                }
            } else if (eventType == 2) {
                if (pid != 0) {
                    this.mScenePidArray.get(stateType).remove(pid);
                }
                if (uid != 0) {
                    this.mSceneUidArray.get(stateType).remove(uid);
                }
                if (pkg != null && !pkg.isEmpty()) {
                    this.mScenePkgArray.get(stateType).remove(pkg);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSceneState(int stateType, int eventType, int pid, String pkg, int uid) {
        if (sDebug) {
            AwareLog.d(TAG, "state type: " + stateType + " eventType:" + eventType + " pid:" + pid + " uid:" + uid + " pkg:" + pkg);
        }
        if (stateType >= 1 && stateType < STATE_SIZE) {
            int realpid = pid;
            if (pid == -1) {
                updateScenePidArrayForInvalidPid(uid);
                realpid = 0;
            }
            notifyStateChange(stateType, eventType, realpid, uid);
            synchronized (this.mLock) {
                updateSceneArrayLocked(stateType, eventType, realpid, pkg, uid);
                if (eventType == 1 && !this.mKeyBackgroupUids.contains(uid)) {
                    notifyStateChange(0, eventType, realpid, uid);
                }
                this.mKeyBackgroupPids.clear();
                this.mKeyBackgroupUids.clear();
                this.mKeyBackgroupPkgs.clear();
                int size = this.mScenePidArray.size();
                for (int i = 0; i < size; i++) {
                    this.mKeyBackgroupPids.addAll(this.mScenePidArray.get(i));
                    this.mKeyBackgroupUids.addAll(this.mSceneUidArray.get(i));
                    this.mKeyBackgroupPkgs.addAll((ArraySet<? extends String>) this.mScenePkgArray.get(i));
                }
                if (eventType == 2 && !this.mKeyBackgroupUids.contains(uid)) {
                    notifyStateChange(0, eventType, realpid, uid);
                }
                resetAudioCache(stateType, eventType, uid);
                if (sDebug && this.mScenePidArray.size() > stateType) {
                    AwareLog.d(TAG, "stateChanged " + stateToString(stateType) + " mPids:" + this.mScenePidArray.get(stateType));
                    AwareLog.d(TAG, "stateChanged " + stateToString(stateType) + " mUids:" + this.mSceneUidArray.get(stateType));
                    AwareLog.d(TAG, "stateChanged " + stateToString(stateType) + " mPkgs:" + this.mScenePkgArray.get(stateType));
                }
            }
        }
    }

    private long getDecayTime(int stateType, int pid, String pkg, int uid) {
        if (stateType != 2) {
            return 10000;
        }
        if (!AwareIntelligentRecg.getInstance().isRecogOptEnable()) {
            AwareLog.i(TAG, "Aware recg opt disable");
            return 60000;
        }
        AwareAudioFocusManager manager = AwareAudioFocusManager.getInstance();
        if (manager == null) {
            AwareLog.i(TAG, "Aware audio focus manager is null");
            return 60000;
        }
        long time = 0;
        if (!manager.isTransientPlay(uid)) {
            if (manager.isAppForeground(uid)) {
                if (manager.isAppExistFocus(uid) && !manager.isPermanentLoss(uid)) {
                    time = DECAY_WAIT_FOCUS_LOSS_TIME;
                }
            } else if (!manager.isPermanentLoss(uid)) {
                time = 60000;
            }
        }
        if (sDebug) {
            AwareLog.i(TAG, "decay keep alive time:" + time + ", uid:" + uid + ", pid:" + pid + ", pkg:" + pkg);
        }
        return time;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearAudioCache(int uid) {
        synchronized (this.mAudioCacheUids) {
            this.mAudioCacheUids.remove(Integer.valueOf(uid));
        }
        synchronized (this.mSystemAudioCacheInfos) {
            Iterator<Map.Entry<Integer, AudioCacheInfo>> iter = this.mSystemAudioCacheInfos.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Integer, AudioCacheInfo> entry = iter.next();
                if (entry != null) {
                    AudioCacheInfo audioCache = entry.getValue();
                    if (audioCache != null && audioCache.uid == uid) {
                        iter.remove();
                    }
                }
            }
        }
    }

    private void resetAudioCache(int stateType, int eventType, int uid) {
        if (eventType == 1 && stateType == 2) {
            clearAudioCache(uid);
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            if (!this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            pw.println("dump Important State Apps start --------");
            synchronized (this.mLock) {
                int size = this.mScenePidArray.size();
                for (int i = 1; i < size; i++) {
                    pw.println("State[" + stateToString(i) + "] Pids:" + this.mScenePidArray.get(i));
                    pw.println("State[" + stateToString(i) + "] Uids:" + this.mSceneUidArray.get(i));
                    pw.println("State[" + stateToString(i) + "] Pkgs:" + this.mScenePkgArray.get(i));
                }
            }
            synchronized (this.mAudioCacheUids) {
                pw.println("State[AUDIO CACHE] Uids:" + this.mAudioCacheUids);
            }
            synchronized (this.mSystemAudioCacheInfos) {
                pw.println("State[SYSTEM AUDIO CACHE] Infos:" + this.mSystemAudioCacheInfos);
            }
            pw.println("nat timeout:" + this.mNatTimeout);
            if (AwareIntelligentRecg.getInstance().isRecogOptEnable()) {
                AwareAudioFocusManager.getInstance().dump(pw);
            } else {
                pw.println("dump recog opt disable-------------");
            }
            pw.println("dump Important State Apps end-----------");
            AwareMiddleware.getInstance().dumpStateCacheIdInfos(pw);
        }
    }

    public void dumpCheckStateByPid(PrintWriter pw, Context context, int state, int pid) {
        if (pw != null) {
            if (this.mPgSdk == null || !this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            pw.println("----------------------------------------");
            boolean result = false;
            try {
                result = this.mPgSdk.checkStateByPid(context, pid, state);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "dumpCheckStateByPid occur exception.");
            }
            pw.println("CheckState Pid:" + pid);
            pw.println("state:" + stateToString(state));
            pw.println("result:" + result);
            pw.println("----------------------------------------");
        }
    }

    public void dumpCheckStateByPkg(PrintWriter pw, Context context, int state, String pkg) {
        if (pw != null && pkg != null && context != null) {
            if (this.mPgSdk == null || !this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            pw.println("----------------------------------------");
            boolean result = false;
            try {
                result = this.mPgSdk.checkStateByPkg(context, pkg, state);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "dumpCheckStateByPkg occur exception.");
            }
            pw.println("CheckState Package:" + pkg);
            pw.println("state:" + stateToString(state));
            pw.println("result:" + result);
            pw.println("----------------------------------------");
        }
    }

    public void dumpCheckPkgType(PrintWriter pw, Context context, String pkg) {
        if (pw != null && pkg != null && context != null) {
            if (this.mPgSdk == null || !this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            int type = 0;
            pw.println("----------------------------------------");
            try {
                type = this.mPgSdk.getPkgType(context, pkg);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "getAppType occur exception.");
            }
            pw.println("CheckType Package:" + pkg);
            pw.println("type:" + typeToString(type));
            pw.println("----------------------------------------");
        }
    }

    public void dumpFakeEvent(PrintWriter pw, int[] dumpInfo, String pkg) {
        if (pw != null && pkg != null && dumpInfo != null && dumpInfo.length == 4) {
            int stateType = dumpInfo[0];
            int eventType = dumpInfo[1];
            int pid = dumpInfo[2];
            int uid = dumpInfo[3];
            if (this.mPgSdk == null || !this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            pw.println("----------------------------------------");
            getInstance().updateSceneState(stateType, eventType, pid, pkg, uid);
            pw.println("Send fake event success!");
            pw.println("----------------------------------------");
        }
    }

    public void dumpCheckKeyBackGroup(PrintWriter pw, int pid, int uid) {
        if (pw != null) {
            if (!this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            pw.println("dump CheckKeyBackGroup State start --------");
            pw.println("Check Pid:" + pid);
            pw.println("Check Uid:" + uid);
            boolean result = checkIsKeyBackgroup(pid, uid);
            pw.println("result:" + result);
            pw.println("dump CheckKeyBackGroup State end-----------");
        }
    }

    private int getKeyBackgroupTypeByPidLocked(int pid) {
        if (this.mScenePidArray.isEmpty()) {
            return -1;
        }
        if (this.mScenePidArray.get(2).contains(pid)) {
            return 2;
        }
        if (this.mScenePidArray.get(1).contains(pid)) {
            return 1;
        }
        if (this.mScenePidArray.get(3).contains(pid)) {
            return 3;
        }
        if (this.mScenePidArray.get(5).contains(pid)) {
            return 5;
        }
        if (this.mScenePidArray.get(4).contains(pid)) {
            return 4;
        }
        return -1;
    }

    private int getKeyBackgroupTypeByUidLocked(int uid) {
        if (this.mSceneUidArray.isEmpty()) {
            return -1;
        }
        if (this.mSceneUidArray.get(2).contains(uid)) {
            return 2;
        }
        if (this.mSceneUidArray.get(1).contains(uid)) {
            return 1;
        }
        if (this.mSceneUidArray.get(3).contains(uid)) {
            return 3;
        }
        if (this.mSceneUidArray.get(5).contains(uid)) {
            return 5;
        }
        if (this.mSceneUidArray.get(4).contains(uid)) {
            return 4;
        }
        return -1;
    }

    private int getKeyBackgroupTypeByPkgsLocked(List<String> pkgs) {
        if (this.mScenePkgArray.isEmpty()) {
            return -1;
        }
        for (String pkg : pkgs) {
            if (this.mScenePkgArray.get(2).contains(pkg)) {
                return 2;
            }
            if (this.mScenePkgArray.get(1).contains(pkg)) {
                return 1;
            }
            if (this.mScenePkgArray.get(3).contains(pkg)) {
                return 3;
            }
            if (this.mScenePkgArray.get(5).contains(pkg)) {
                return 5;
            }
            if (this.mScenePkgArray.get(4).contains(pkg)) {
                return 4;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSensorEvent(int uid, int sensor, boolean enable) {
        if (sDebug) {
            AwareLog.i(TAG, "sensor:" + sensor + " enable:" + enable + " uid:" + uid);
        }
        synchronized (this.mLock) {
            SensorRecord se = this.mHistorySensorRecords.get(uid);
            if (enable) {
                if (se == null) {
                    this.mHistorySensorRecords.put(uid, new SensorRecord(uid, sensor));
                } else {
                    se.addSensor(sensor);
                }
            } else if (se != null && se.hasSensor()) {
                se.removeSensor(Integer.valueOf(sensor));
            }
        }
    }

    private void initAppSensorState(Context context, int uid) {
        if (this.mPgSdk == null) {
            AwareLog.e(TAG, "KeyBackGroup feature not enabled.");
            return;
        }
        synchronized (this.mLock) {
            if (this.mHistorySensorRecords.get(uid) != null) {
                if (sDebug) {
                    AwareLog.d(TAG, "History Sensor Records has uid " + uid);
                }
                return;
            }
        }
        try {
            Map<String, String> sensorMap = this.mPgSdk.getSensorInfoByUid(context, uid);
            if (sensorMap != null) {
                for (Map.Entry<String, String> entry : sensorMap.entrySet()) {
                    int sensor = Integer.parseInt(entry.getKey());
                    int count = Integer.parseInt(entry.getValue());
                    for (int i = 1; i <= count; i++) {
                        handleSensorEvent(uid, sensor, true);
                    }
                }
                if (sDebug) {
                    AwareLog.d(TAG, "getSensorInfoByUid sensor handles " + sensorMap);
                }
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "error, PG crash!");
        } catch (NumberFormatException e2) {
            AwareLog.e(TAG, "integer parse error!");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAppSensorState(boolean sensorStart, int uid, int pid) {
        if (sDebug) {
            AwareLog.i(TAG, "updateAppSensorState :" + uid + " pid " + pid);
        }
        updateSceneState(4, sensorStart ? 1 : 2, pid, null, uid);
    }

    /* access modifiers changed from: private */
    public class SensorRecord {
        private final ArrayMap<Integer, Integer> mHandles = new ArrayMap<>();
        private int mUid;

        public SensorRecord(int uid, int handle) {
            this.mUid = uid;
            addSensor(handle);
        }

        public boolean hasSensor() {
            return this.mHandles.size() > 0;
        }

        public void addSensor(int handle) {
            if (!hasSensor()) {
                AwareAppKeyBackgroup.this.updateAppSensorState(true, this.mUid, 0);
            }
            Integer count = this.mHandles.get(Integer.valueOf(handle));
            if (count == null) {
                this.mHandles.put(Integer.valueOf(handle), 1);
            } else {
                this.mHandles.put(Integer.valueOf(handle), Integer.valueOf(count.intValue() + 1));
            }
            if (AwareAppKeyBackgroup.sDebug) {
                AwareLog.i(AwareAppKeyBackgroup.TAG, "addSensor,mHandles:" + this.mHandles);
            }
        }

        public void removeSensor(Integer handle) {
            Integer count = this.mHandles.get(handle);
            if (count != null) {
                int value = count.intValue() - 1;
                if (value <= 0) {
                    this.mHandles.remove(handle);
                } else {
                    this.mHandles.put(handle, Integer.valueOf(value));
                }
            }
            if (AwareAppKeyBackgroup.sDebug) {
                AwareLog.i(AwareAppKeyBackgroup.TAG, "removeSensor,mHandles:" + this.mHandles);
            }
            if (!hasSensor()) {
                AwareAppKeyBackgroup.this.updateAppSensorState(false, this.mUid, 0);
            }
        }
    }

    private void sendDecayMesssage(int message, DecayInfo decayInfo, long delayT) {
        if (this.mAppKeyHandler.hasMessages(message, decayInfo)) {
            this.mAppKeyHandler.removeMessages(message, decayInfo);
        }
        Message observerMsg = this.mAppKeyHandler.obtainMessage();
        observerMsg.what = message;
        observerMsg.obj = decayInfo;
        this.mAppKeyHandler.sendMessageDelayed(observerMsg, delayT);
        if (sDebug) {
            AwareLog.d(TAG, "sendDecayMesssage end " + message + " decayinfo " + decayInfo);
        }
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mAppKeyHandler = new AppKeyHandler(looper);
        } else {
            this.mAppKeyHandler = new AppKeyHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public class AppKeyHandler extends Handler {
        public AppKeyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 3) {
                AwareAppKeyBackgroup.this.doInitialize();
            } else if (AwareAppKeyBackgroup.this.mIsInitialized.get()) {
                DecayInfo decayInfo = msg.obj instanceof DecayInfo ? (DecayInfo) msg.obj : null;
                int i = msg.what;
                if (i != 1) {
                    if (i == 2) {
                        int pid = msg.arg1;
                        int uid = msg.arg2;
                        AwareAppKeyBackgroup.this.updateSceneArrayProcessDied(uid, pid);
                        if (AwareAppKeyBackgroup.this.getStateEventDecayInfosSize() > 0) {
                            int i2 = 1;
                            while (i2 < AwareAppKeyBackgroup.STATE_SIZE) {
                                AwareAppKeyBackgroup.this.removeDecayForProcessDied(i2, 2, i2 == 2 ? pid : 0, null, uid);
                                i2++;
                            }
                        }
                    }
                } else if (decayInfo != null) {
                    if (AwareAppKeyBackgroup.sDebug) {
                        AwareLog.d(AwareAppKeyBackgroup.TAG, "Update state " + decayInfo.getStateType() + " uid : " + decayInfo.getUid());
                    }
                    AwareAppKeyBackgroup.this.updateSceneState(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                    AwareAppKeyBackgroup.this.removeDecayInfo(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                    AwareAppKeyBackgroup.this.updateAudioCache(decayInfo.getStateType(), decayInfo.getPid(), decayInfo.getUid());
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class DecayInfo {
        private int mEventType;
        private int mPid;
        private String mPkg;
        private int mStateType;
        private int mUid;

        public DecayInfo(int stateType, int eventType, int pid, String pkg, int uid) {
            this.mStateType = stateType;
            this.mEventType = eventType;
            this.mPid = pid;
            this.mUid = uid;
            this.mPkg = pkg;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DecayInfo other = (DecayInfo) obj;
            if (other.getStateType() == this.mStateType && other.getEventType() == this.mEventType && other.getPid() == this.mPid && other.getUid() == this.mUid) {
                return true;
            }
            return false;
        }

        public int getStateType() {
            return this.mStateType;
        }

        public int getEventType() {
            return this.mEventType;
        }

        public int getPid() {
            return this.mPid;
        }

        public int getUid() {
            return this.mUid;
        }

        public String getPkg() {
            return this.mPkg;
        }

        public String toString() {
            return "{" + AwareAppKeyBackgroup.stateToString(this.mStateType) + "," + this.mPid + "," + this.mUid + "}";
        }
    }

    private boolean isPkgsInvalid(List<String> pkgs) {
        return pkgs == null || pkgs.isEmpty();
    }

    public boolean checkAudioOutInstant(int pid, int uid, List<String> pkgs) {
        boolean decayContainsUid;
        if (!this.mIsInitialized.get() || isPkgsInvalid(pkgs)) {
            return false;
        }
        boolean sceneContainsUid = false;
        boolean sceneContainsPid = false;
        boolean decayContainsUid2 = false;
        boolean decayContainsPid = false;
        synchronized (this.mLock) {
            if (this.mSceneUidArray.isEmpty()) {
                return false;
            }
            if (isUidInState(2, uid, pkgs)) {
                sceneContainsUid = true;
            }
            if (this.mScenePidArray.get(2).contains(pid)) {
                sceneContainsPid = true;
            }
        }
        if (sceneContainsUid || sceneContainsPid) {
            synchronized (this.mStateEventDecayInfos) {
                Iterator<DecayInfo> it = this.mStateEventDecayInfos.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    DecayInfo info = it.next();
                    if (info.getStateType() == 2) {
                        if (info.getEventType() == 2) {
                            int decayUid = info.getUid();
                            int decayPid = info.getPid();
                            if (decayUid == uid) {
                                decayContainsUid2 = true;
                                if (decayPid == -1) {
                                    decayContainsPid = true;
                                }
                            }
                            if (decayPid == pid) {
                                decayContainsPid = true;
                            }
                            if (decayContainsUid2 && decayContainsPid) {
                                break;
                            }
                        }
                    }
                }
            }
            if ((sceneContainsUid && !decayContainsUid2) || (sceneContainsPid && !decayContainsPid)) {
                return true;
            }
            decayContainsUid = decayContainsUid2;
        } else {
            decayContainsUid = false;
        }
        synchronized (this.mAudioCacheUids) {
            if (!decayContainsUid) {
                if (!isInAudioCache(2, pid, uid, pkgs)) {
                    return false;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNatTimeout(int natTime) {
        this.mNatTimeout = natTime;
        AwareWakeUpManager.getInstance().setIntervalOverload(natTime);
    }

    private boolean isSystemUid(int uid) {
        return UserHandleEx.getAppId(uid) == 1000;
    }

    public void updateSysAudioPkgsCache() {
        if (this.mIsInitialized.get()) {
            ArraySet<String> audioInPkgs = getSysPkgsByState(1);
            synchronized (this.mSysAudioInPkgsCache) {
                this.mSysAudioInPkgsCache.clear();
                this.mSysAudioInPkgsCache.addAll((ArraySet<? extends String>) audioInPkgs);
            }
            ArraySet<String> audioOutPkgs = getSysPkgsByState(2);
            synchronized (this.mSystemAudioOutPkgsCache) {
                this.mSystemAudioOutPkgsCache.clear();
                this.mSystemAudioOutPkgsCache.addAll((ArraySet<? extends String>) audioOutPkgs);
            }
        }
    }

    private ArraySet<String> getSysPkgsByState(int state) {
        SparseSet pidSet = new SparseSet();
        synchronized (this.mLock) {
            if (this.mScenePidArray.size() <= state) {
                return new ArraySet<>();
            }
            pidSet.addAll(this.mScenePidArray.get(state));
        }
        int[] pids = pidSet.copyKeys();
        if (pids == null) {
            return new ArraySet<>();
        }
        ArraySet<String> pkgs = new ArraySet<>();
        for (int pid : pids) {
            ProcessInfo processInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid);
            if (!(processInfo == null || !isSystemUid(processInfo.mUid) || processInfo.mPackageName == null)) {
                pkgs.addAll(processInfo.mPackageName);
            }
        }
        return pkgs;
    }

    private boolean isInSystemAudioOutPkgsCache(List<String> pkgs) {
        if (pkgs == null) {
            return false;
        }
        synchronized (this.mSystemAudioOutPkgsCache) {
            for (String pkg : pkgs) {
                if (this.mSystemAudioOutPkgsCache.contains(pkg)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void addSystemAudioOutInfos(int pid, int uid) {
        ProcessInfo processInfo;
        if (isSystemUid(uid) && (processInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid)) != null && processInfo.mPackageName != null) {
            synchronized (this.mSystemAudioCacheInfos) {
                this.mSystemAudioCacheInfos.put(Integer.valueOf(pid), new AudioCacheInfo(pid, processInfo.mUid, processInfo.mPackageName));
            }
        }
    }

    private boolean isInAudioCache(int state, int pid, int uid, List<String> pkgs) {
        if (state != 2) {
            return false;
        }
        if (isSystemUid(uid)) {
            ArraySet<String> pkgsTmp = new ArraySet<>();
            synchronized (this.mSystemAudioCacheInfos) {
                for (Map.Entry<Integer, AudioCacheInfo> entry : this.mSystemAudioCacheInfos.entrySet()) {
                    if (entry != null) {
                        if (entry.getKey().intValue() == pid) {
                            return true;
                        }
                        AudioCacheInfo audioCacheInfo = entry.getValue();
                        if (audioCacheInfo != null) {
                            pkgsTmp.addAll(audioCacheInfo.pkgs);
                        }
                    }
                }
            }
            for (String pkg : pkgs) {
                if (pkgsTmp.contains(pkg)) {
                    return true;
                }
            }
        } else {
            synchronized (this.mAudioCacheUids) {
                if (this.mAudioCacheUids.contains(Integer.valueOf(uid))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInSysAudioInPkgsCache(List<String> pkgs) {
        if (pkgs == null) {
            return false;
        }
        synchronized (this.mSysAudioInPkgsCache) {
            for (String pkg : pkgs) {
                if (pkg != null && this.mSysAudioInPkgsCache.contains(pkg)) {
                    return true;
                }
            }
            return false;
        }
    }
}
