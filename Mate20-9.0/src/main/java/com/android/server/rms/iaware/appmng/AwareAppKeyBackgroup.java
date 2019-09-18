package com.android.server.rms.iaware.appmng;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.net.HwNetworkStatsService;
import com.huawei.pgmng.plug.PGSdk;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppKeyBackgroup {
    private static final String[] APPTYPESTRING = {"TYPE_UNKNOW", "TYPE_LAUNCHER", "TYPE_SMS", "TYPE_EMAIL", "TYPE_INPUTMETHOD", "TYPE_GAME", "TYPE_BROWSER", "TYPE_EBOOK", "TYPE_VIDEO", "TYPE_SCRLOCK", "TYPE_CLOCK", "TYPE_IM", "TYPE_MUSIC"};
    private static final String CALL_APP_PKG = "com.android.incallui";
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    private static final long DECAY_TIME = 60000;
    private static final long DECAY_UPLOAD_DL_TIME = 10000;
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    private static final long LAST_CALL_START_MAX_INTERVAL = 70000;
    private static final int MSG_APP_PROCESSDIED = 2;
    private static final int MSG_PGSDK_INIT = 3;
    private static final int MSG_REMOVE_AUDIO_PREVENT_STATE = 5;
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
    /* access modifiers changed from: private */
    public static final int STATE_SIZE = STATESTRING.length;
    public static final int STATE_UPLOAD_DL = 5;
    private static final String TAG = "AwareAppKeyBackgroup";
    private static final int TYPE_SIZE = APPTYPESTRING.length;
    private static long mAudioPreventMaxTime = SystemProperties.getLong("ro.config.iaware_audiopretime", HwNetworkStatsService.UPLOAD_INTERVAL);
    private static AwareAppKeyBackgroup sInstance = null;
    /* access modifiers changed from: private */
    public AppKeyHandler mAppKeyHandler;
    /* access modifiers changed from: private */
    public final List<DecayInfo> mAudioCacheDecayInfos;
    /* access modifiers changed from: private */
    public final ArraySet<Integer> mAudioCacheUids;
    PhoneStateListener mCallStateListener;
    private final ArrayMap<IAwareStateCallback, ArraySet<Integer>> mCallbacks;
    private Context mContext;
    private final SparseArray<SensorRecord> mHistorySensorRecords;
    private HwActivityManagerService mHwAMS;
    private boolean mIsAbroadArea;
    /* access modifiers changed from: private */
    public AtomicBoolean mIsInitialized;
    private AtomicBoolean mIsInitializing;
    /* access modifiers changed from: private */
    public final ArraySet<Integer> mKeyBackgroupPids;
    private final ArraySet<String> mKeyBackgroupPkgs;
    /* access modifiers changed from: private */
    public final ArraySet<Integer> mKeyBackgroupUids;
    private long mLastCallStartTimes;
    private AtomicBoolean mLastSetting;
    private int mNatTimeout;
    private PGSdk mPGSdk;
    private PackageManager mPM;
    private AwareABGProcessObserver mProcessObserver;
    private final ArrayList<ArraySet<Integer>> mScenePidArray;
    private final ArrayList<ArraySet<String>> mScenePkgArray;
    private final ArrayList<ArraySet<Integer>> mSceneUidArray;
    private final List<DecayInfo> mStateEventDecayInfos;
    private PGSdk.Sink mStateRecognitionListener;

    private class AppKeyHandler extends Handler {
        public AppKeyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Message message = msg;
            if (message.what == 3) {
                AwareAppKeyBackgroup.this.doInitialize();
            } else if (AwareAppKeyBackgroup.this.mIsInitialized.get()) {
                DecayInfo decayInfo = message.obj instanceof DecayInfo ? (DecayInfo) message.obj : null;
                int i = message.what;
                if (i != 5) {
                    switch (i) {
                        case 1:
                            if (decayInfo != null) {
                                if (AwareAppKeyBackgroup.DEBUG) {
                                    AwareLog.d(AwareAppKeyBackgroup.TAG, "Update state " + decayInfo.getStateType() + " uid : " + decayInfo.getUid());
                                }
                                AwareAppKeyBackgroup.this.updateSceneState(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                                DecayInfo unused = AwareAppKeyBackgroup.this.removeDecayInfo(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                                AwareAppKeyBackgroup.this.updateAudioCache(decayInfo.getStateType(), decayInfo.getPid(), decayInfo.getUid());
                                break;
                            } else {
                                return;
                            }
                        case 2:
                            int pid = message.arg1;
                            int uid = message.arg2;
                            AwareAppKeyBackgroup.this.updateSceneArrayProcessDied(uid, pid);
                            if (AwareAppKeyBackgroup.this.getStateEventDecayInfosSize() > 0) {
                                int i2 = 1;
                                while (i2 < AwareAppKeyBackgroup.STATE_SIZE) {
                                    AwareAppKeyBackgroup.this.removeDecayForProcessDied(i2, 2, i2 == 2 ? pid : 0, null, uid);
                                    i2++;
                                }
                                break;
                            }
                            break;
                    }
                } else if (decayInfo != null) {
                    synchronized (AwareAppKeyBackgroup.this.mAudioCacheUids) {
                        AwareAppKeyBackgroup.this.mAudioCacheUids.remove(Integer.valueOf(decayInfo.getUid()));
                    }
                    synchronized (AwareAppKeyBackgroup.this.mAudioCacheDecayInfos) {
                        AwareAppKeyBackgroup.this.mAudioCacheDecayInfos.remove(decayInfo);
                    }
                }
            }
        }
    }

    class AwareABGProcessObserver extends IProcessObserver.Stub {
        AwareABGProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onProcessDied(int pid, int uid) {
            synchronized (AwareAppKeyBackgroup.this) {
                boolean isKbgPid = AwareAppKeyBackgroup.this.mKeyBackgroupPids.contains(Integer.valueOf(pid));
                boolean isKbgUid = AwareAppKeyBackgroup.this.mKeyBackgroupUids.contains(Integer.valueOf(uid));
                if (isKbgPid || isKbgUid) {
                    if (AwareAppKeyBackgroup.DEBUG) {
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

    static class DecayInfo {
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

        public DecayInfo(int uid) {
            this.mUid = uid;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public boolean equals(Object obj) {
            boolean z = false;
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
                z = true;
            }
            return z;
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

    public interface IAwareStateCallback {
        void onStateChanged(int i, int i2, int i3, int i4);
    }

    private class SensorRecord {
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
            if (AwareAppKeyBackgroup.DEBUG) {
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
            if (AwareAppKeyBackgroup.DEBUG != 0) {
                AwareLog.i(AwareAppKeyBackgroup.TAG, "removeSensor,mHandles:" + this.mHandles);
            }
            if (!hasSensor()) {
                AwareAppKeyBackgroup.this.updateAppSensorState(false, this.mUid, 0);
            }
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
    public boolean checkCallingPermission() {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        if (pid == Process.myPid() || uid == 0 || uid == 1000) {
            return true;
        }
        return false;
    }

    private int getAppTypeFromHabit(int uid) {
        if (this.mContext == null) {
            return -1;
        }
        if (this.mPM == null) {
            this.mPM = this.mContext.getPackageManager();
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
        if (DEBUG) {
            AwareLog.d(TAG, "getAppTypeFromHabit uid " + uid + " type : " + type);
        }
        boolean z = true;
        if (type != -1) {
            switch (type) {
                case 2:
                case 3:
                    break;
                default:
                    if (type <= 255) {
                        z = false;
                    }
                    return z;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean shouldFilter(int stateType, int uid) {
        return (stateType == 3 || stateType == 4) && !isNaviOrSportApp(uid);
    }

    private void registerProcessObserver() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.d(TAG, "AwareAppKeyBackgroup register process observer failed");
        }
    }

    private void unregisterProcessObserver() {
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.d(TAG, "AwareAppKeyBackgroup unregister process observer failed");
        }
    }

    /* access modifiers changed from: protected */
    public void registerCallStateListener(Context cxt) {
        if (cxt != null) {
            ((TelephonyManager) cxt.getSystemService("phone")).listen(this.mCallStateListener, 32);
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterCallStateListener(Context cxt) {
        if (cxt != null) {
            ((TelephonyManager) cxt.getSystemService("phone")).listen(this.mCallStateListener, 0);
        }
    }

    /* access modifiers changed from: private */
    public final void updateCallState(int state) {
        if (this.mIsInitialized.get()) {
            int eventType = state == 0 ? 2 : 1;
            synchronized (this) {
                updateSceneArrayLocked(2, eventType, 0, CALL_APP_PKG, 0);
            }
            if (eventType == 2) {
                clearAudioCache();
            }
        }
    }

    private AwareAppKeyBackgroup() {
        this.mIsInitialized = new AtomicBoolean(false);
        this.mLastSetting = new AtomicBoolean(false);
        this.mIsInitializing = new AtomicBoolean(false);
        this.mPGSdk = null;
        this.mContext = null;
        this.mHwAMS = null;
        this.mPM = null;
        this.mProcessObserver = new AwareABGProcessObserver();
        this.mIsAbroadArea = false;
        this.mLastCallStartTimes = 0;
        this.mScenePidArray = new ArrayList<>();
        this.mSceneUidArray = new ArrayList<>();
        this.mScenePkgArray = new ArrayList<>();
        this.mKeyBackgroupPids = new ArraySet<>();
        this.mKeyBackgroupUids = new ArraySet<>();
        this.mKeyBackgroupPkgs = new ArraySet<>();
        this.mStateEventDecayInfos = new ArrayList();
        this.mCallbacks = new ArrayMap<>();
        this.mHistorySensorRecords = new SparseArray<>();
        this.mAudioCacheUids = new ArraySet<>();
        this.mAudioCacheDecayInfos = new ArrayList();
        this.mNatTimeout = 0;
        this.mStateRecognitionListener = new PGSdk.Sink() {
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
                    long timeStamp = 0;
                    if (AwareAppKeyBackgroup.DEBUG) {
                        AwareLog.d(AwareAppKeyBackgroup.TAG, "PGSdk Sink onStateChanged");
                        timeStamp = SystemClock.currentTimeMicro();
                    }
                    if (!AwareAppKeyBackgroup.this.isStateChangedDecay(stateType, eventType, pid, pkg, uid)) {
                        AwareAppKeyBackgroup.this.updateSceneState(stateType, eventType, pid, pkg, uid);
                        if (AwareAppKeyBackgroup.DEBUG) {
                            AwareLog.d(AwareAppKeyBackgroup.TAG, "Update Scene state using " + (SystemClock.currentTimeMicro() - timeStamp) + " us");
                        }
                    }
                }
            }
        };
        this.mCallStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (AwareAppKeyBackgroup.DEBUG) {
                    AwareLog.d(AwareAppKeyBackgroup.TAG, "onCallStateChanged state :" + state);
                }
                AwareAppKeyBackgroup.this.updateCallState(state);
            }
        };
        this.mAppKeyHandler = new AppKeyHandler(BackgroundThread.get().getLooper());
        this.mHwAMS = HwActivityManagerService.self();
    }

    public void registerStateCallback(IAwareStateCallback callback, int stateType) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                ArraySet<Integer> states = this.mCallbacks.get(callback);
                if (states == null) {
                    ArraySet arraySet = new ArraySet();
                    arraySet.add(Integer.valueOf(stateType));
                    this.mCallbacks.put(callback, arraySet);
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
        if (DEBUG) {
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
            if (this.mAppKeyHandler.hasMessages(3)) {
                this.mAppKeyHandler.removeMessages(3);
            }
            this.mAppKeyHandler.sendEmptyMessage(3);
        }
    }

    /* access modifiers changed from: private */
    public void doInitialize() {
        this.mIsInitializing.set(true);
        synchronized (this) {
            if (this.mScenePidArray.isEmpty()) {
                for (int i = 0; i < STATE_SIZE; i++) {
                    this.mScenePidArray.add(new ArraySet());
                    this.mSceneUidArray.add(new ArraySet());
                    this.mScenePkgArray.add(new ArraySet());
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
            this.mPGSdk = PGSdk.getInstance();
            if (this.mPGSdk == null) {
                return this.mIsInitialized.get();
            }
            try {
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 1);
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 2);
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 3);
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 4);
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 5);
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 11);
                resumeAppStates(this.mContext);
                this.mIsInitialized.set(true);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "PG Exception e: initialize pgdskd error!");
            }
        }
        if (DEBUG) {
            AwareLog.d(TAG, "AwareAppKeyBackgroup ensureInitialize:" + this.mIsInitialized.get());
        }
        return this.mIsInitialized.get();
    }

    private void deInitialize() {
        unregisterProcessObserver();
        unregisterCallStateListener(this.mContext);
        this.mLastSetting.set(false);
        if (!this.mIsInitialized.get()) {
            this.mAppKeyHandler.removeMessages(3);
        } else if (this.mPGSdk != null) {
            try {
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, 1);
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, 2);
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, 3);
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, 4);
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, 5);
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, 11);
                synchronized (this) {
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
                synchronized (this.mAudioCacheDecayInfos) {
                    this.mAudioCacheDecayInfos.clear();
                }
            } catch (RemoteException e) {
                try {
                    AwareLog.e(TAG, "PG Exception e: deinitialize pgsdk error!");
                    synchronized (this) {
                        this.mScenePidArray.clear();
                        this.mScenePkgArray.clear();
                        this.mSceneUidArray.clear();
                        this.mKeyBackgroupPids.clear();
                        this.mKeyBackgroupUids.clear();
                        this.mKeyBackgroupPkgs.clear();
                        this.mHistorySensorRecords.clear();
                        synchronized (this.mStateEventDecayInfos) {
                            this.mStateEventDecayInfos.clear();
                            synchronized (this.mAudioCacheUids) {
                                this.mAudioCacheUids.clear();
                                synchronized (this.mAudioCacheDecayInfos) {
                                    this.mAudioCacheDecayInfos.clear();
                                }
                            }
                        }
                    }
                } catch (Throwable th) {
                    synchronized (this) {
                        this.mScenePidArray.clear();
                        this.mScenePkgArray.clear();
                        this.mSceneUidArray.clear();
                        this.mKeyBackgroupPids.clear();
                        this.mKeyBackgroupUids.clear();
                        this.mKeyBackgroupPkgs.clear();
                        this.mHistorySensorRecords.clear();
                        synchronized (this.mStateEventDecayInfos) {
                            this.mStateEventDecayInfos.clear();
                            synchronized (this.mAudioCacheUids) {
                                this.mAudioCacheUids.clear();
                                synchronized (this.mAudioCacheDecayInfos) {
                                    this.mAudioCacheDecayInfos.clear();
                                    this.mAppKeyHandler.removeCallbacksAndMessages(null);
                                    this.mIsInitialized.set(false);
                                    throw th;
                                }
                            }
                        }
                    }
                }
            }
            this.mAppKeyHandler.removeCallbacksAndMessages(null);
            this.mIsInitialized.set(false);
            checkLastSetting();
        } else {
            return;
        }
        if (DEBUG) {
            AwareLog.d(TAG, "PGFeature deInitialize:" + this.mIsInitialized.get());
        }
    }

    private void checkLastSetting() {
        if (!(this.mContext == null || this.mIsInitialized.get() == this.mLastSetting.get())) {
            if (this.mLastSetting.get()) {
                getInstance().initialize(this.mContext);
            } else {
                getInstance().deInitialize();
            }
        }
    }

    public static void enable(Context context) {
        if (DEBUG) {
            AwareLog.d(TAG, "KeyBackGroup Feature enable!!!");
        }
        getInstance().initialize(context);
    }

    public static void disable() {
        if (DEBUG) {
            AwareLog.d(TAG, "KeyBackGroup Feature disable!!!");
        }
        getInstance().deInitialize();
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    public static void setTimeOut(int timeout) {
        mAudioPreventMaxTime = 60000 * ((long) timeout);
    }

    public static AwareAppKeyBackgroup getInstance() {
        AwareAppKeyBackgroup awareAppKeyBackgroup;
        synchronized (AwareAppKeyBackgroup.class) {
            if (sInstance == null) {
                sInstance = new AwareAppKeyBackgroup();
            }
            awareAppKeyBackgroup = sInstance;
        }
        return awareAppKeyBackgroup;
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00ce  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00ea  */
    private void resumeAppStates(Context context) {
        ArrayList<String> packages;
        int i;
        int uid;
        boolean state;
        ArrayList<String> packages2;
        int k;
        int psize;
        int i2;
        int uid2;
        boolean state2;
        int uid3;
        ArrayList<String> packages3;
        int i3;
        Context context2 = context;
        if (context2 != null) {
            ArrayList<ProcessInfo> procs = ProcessInfoCollector.getInstance().getProcessInfoList();
            long timeStamp = 0;
            if (DEBUG) {
                timeStamp = SystemClock.currentTimeMicro();
            }
            int size = procs.size();
            int j = 0;
            while (true) {
                int size2 = size;
                if (j >= size2) {
                    break;
                }
                ProcessInfo procInfo = procs.get(j);
                if (procInfo != null) {
                    int pid = procInfo.mPid;
                    int uid4 = procInfo.mUid;
                    ArrayList<String> packages4 = procInfo.mPackageName;
                    int i4 = 1;
                    while (true) {
                        int i5 = i4;
                        if (i5 > 5) {
                            break;
                        }
                        if (shouldFilter(i5, uid4)) {
                            i = i5;
                            packages = packages4;
                            uid = uid4;
                        } else if (i5 <= 2) {
                            try {
                                state2 = this.mPGSdk.checkStateByPid(context2, pid, i5);
                            } catch (RemoteException e) {
                                AwareLog.e(TAG, "checkStateByPid occur exception.");
                                state2 = false;
                            }
                            if (state2) {
                                i3 = i5;
                                packages3 = packages4;
                                uid3 = uid4;
                                updateSceneState(i5, 1, pid, null, uid4);
                            } else {
                                i3 = i5;
                                packages3 = packages4;
                                uid3 = uid4;
                            }
                            packages = packages3;
                            i = i3;
                            uid = uid3;
                        } else {
                            ArrayList<String> packages5 = packages4;
                            int uid5 = uid4;
                            int i6 = i5;
                            if (i6 == 4) {
                                int uid6 = uid5;
                                initAppSensorState(context2, uid6);
                                uid = uid6;
                                packages = packages5;
                                i = i6;
                            } else {
                                int uid7 = uid5;
                                ArrayList<String> packages6 = packages5;
                                int psize2 = packages6.size();
                                int k2 = 0;
                                while (true) {
                                    int psize3 = psize2;
                                    if (k2 >= psize3) {
                                        break;
                                    }
                                    try {
                                        state = false;
                                        try {
                                            state = this.mPGSdk.checkStateByPkg(context2, packages6.get(k2), i6);
                                        } catch (RemoteException e2) {
                                            e = e2;
                                            RemoteException remoteException = e;
                                            AwareLog.e(TAG, "checkStateByPkg occur exception.");
                                            if (state) {
                                            }
                                            k2 = k + 1;
                                            uid7 = uid2;
                                            i6 = i2;
                                            psize2 = psize;
                                            packages6 = packages2;
                                        }
                                    } catch (RemoteException e3) {
                                        e = e3;
                                        state = false;
                                        RemoteException remoteException2 = e;
                                        AwareLog.e(TAG, "checkStateByPkg occur exception.");
                                        if (state) {
                                        }
                                        k2 = k + 1;
                                        uid7 = uid2;
                                        i6 = i2;
                                        psize2 = psize;
                                        packages6 = packages2;
                                    }
                                    if (state) {
                                        psize = psize3;
                                        k = k2;
                                        packages2 = packages6;
                                        uid2 = uid7;
                                        i2 = i6;
                                        updateSceneState(i6, 1, 0, null, uid2);
                                    } else {
                                        psize = psize3;
                                        k = k2;
                                        packages2 = packages6;
                                        uid2 = uid7;
                                        i2 = i6;
                                    }
                                    k2 = k + 1;
                                    uid7 = uid2;
                                    i6 = i2;
                                    psize2 = psize;
                                    packages6 = packages2;
                                }
                                packages = packages6;
                                uid = uid7;
                                i = i6;
                            }
                        }
                        i4 = i + 1;
                        uid4 = uid;
                        packages4 = packages;
                    }
                }
                j++;
                size = size2;
            }
            if (DEBUG) {
                AwareLog.d(TAG, "resumeAppStates done using " + (SystemClock.currentTimeMicro() - timeStamp) + " us");
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isStateChangedDecay(int stateType, int eventType, int pid, String pkg, int uid) {
        if (this.mContext == null) {
            return false;
        }
        if (!isNeedDecay(stateType, eventType)) {
            checkStateChangedDecay(stateType, eventType, pid, pkg, uid);
            return false;
        } else if (!isAppAlive(this.mContext, uid)) {
            return false;
        } else {
            sendDecayMesssage(1, addDecayInfo(stateType, eventType, pid, pkg, uid), stateType == 5 ? 10000 : 60000);
            return true;
        }
    }

    private boolean isNeedDecay(int stateType, int eventType) {
        return (stateType == 2 || stateType == 5) && eventType == 2;
    }

    private void checkStateChangedDecay(int stateType, int eventType, int pid, String pkg, int uid) {
        if (isNeedCheckDecay(stateType, eventType)) {
            DecayInfo decayInfo = removeDecayInfo(stateType, 2, pid, pkg, uid);
            if (decayInfo != null) {
                if (DEBUG) {
                    AwareLog.d(TAG, "checkStateChangedDecay start has message 1 ? " + this.mAppKeyHandler.hasMessages(1, decayInfo) + " decayinfo" + decayInfo + " size " + getStateEventDecayInfosSize());
                }
                this.mAppKeyHandler.removeMessages(1, decayInfo);
            }
        }
    }

    private boolean isNeedCheckDecay(int stateType, int eventType) {
        return (stateType == 2 || stateType == 5) && eventType == 1 && getStateEventDecayInfosSize() > 0;
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
                DecayInfo decayInfo2 = new DecayInfo(stateType, eventType, pid, pkg, uid);
                decayInfo = decayInfo2;
                this.mStateEventDecayInfos.add(decayInfo);
            }
        }
        return decayInfo;
    }

    /* access modifiers changed from: private */
    public DecayInfo removeDecayInfo(int stateType, int eventType, int pid, String pkg, int uid) {
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
    public int getStateEventDecayInfosSize() {
        int size;
        synchronized (this.mStateEventDecayInfos) {
            size = this.mStateEventDecayInfos.size();
        }
        return size;
    }

    private void updateLastCallTime(int stateType, int eventType, int pid, String pkg, int uid) {
        if (stateType == 2 && eventType == 1) {
            String topImCN = AwareIntelligentRecg.getInstance().getActTopIMCN();
            if (pkg != null && (CALL_APP_PKG.equals(pkg) || pkg.equals(topImCN))) {
                this.mLastCallStartTimes = SystemClock.elapsedRealtime();
            } else if (pid != 0) {
                ProcessInfo processInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid);
                if (processInfo != null && processInfo.mPackageName != null) {
                    if (processInfo.mPackageName.contains(topImCN) || processInfo.mPackageName.contains(CALL_APP_PKG)) {
                        this.mLastCallStartTimes = SystemClock.elapsedRealtime();
                    }
                }
            }
        }
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

    private boolean isInCalling() {
        if (checkKeyBackgroupByState(2, 0, 0, CALL_APP_PKG)) {
            return true;
        }
        ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procList.isEmpty()) {
            return false;
        }
        String topImCN = AwareIntelligentRecg.getInstance().getActTopIMCN();
        int size = procList.size();
        for (int i = 0; i < size; i++) {
            if (procList.get(i) != null && procList.get(i).mPackageName != null && procList.get(i).mPackageName.contains(topImCN) && checkKeyBackgroupByState(2, procList.get(i).mPid, procList.get(i).mUid, topImCN)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInCallingBeforOneMin() {
        if (this.mLastCallStartTimes != 0) {
            long currentTime = SystemClock.elapsedRealtime();
            if (currentTime - this.mLastCallStartTimes <= LAST_CALL_START_MAX_INTERVAL && currentTime - this.mLastCallStartTimes >= 0) {
                return true;
            }
        }
        return false;
    }

    private void removeAudioCacheMessageByUid(int uid) {
        synchronized (this.mAudioCacheUids) {
            if (this.mAudioCacheUids.contains(Integer.valueOf(uid))) {
                synchronized (this.mAudioCacheDecayInfos) {
                    Iterator<DecayInfo> iter = this.mAudioCacheDecayInfos.iterator();
                    while (iter.hasNext()) {
                        DecayInfo decayInfo = iter.next();
                        if (decayInfo != null && decayInfo.getUid() == uid) {
                            this.mAppKeyHandler.removeMessages(5, decayInfo);
                            iter.remove();
                        }
                    }
                }
            }
        }
    }

    private void setAudioCacheTimeOut(int pid, int uid) {
        removeAudioCacheMessageByUid(uid);
        DecayInfo decayInfo = new DecayInfo(uid);
        synchronized (this.mAudioCacheDecayInfos) {
            this.mAudioCacheDecayInfos.add(decayInfo);
        }
        Message message = this.mAppKeyHandler.obtainMessage();
        message.obj = decayInfo;
        message.what = 5;
        this.mAppKeyHandler.sendMessageDelayed(message, mAudioPreventMaxTime);
        synchronized (this.mAudioCacheUids) {
            this.mAudioCacheUids.add(Integer.valueOf(uid));
        }
    }

    private void clearAudioCache() {
        this.mAppKeyHandler.removeMessages(5);
        synchronized (this.mAudioCacheDecayInfos) {
            this.mAudioCacheDecayInfos.clear();
        }
        synchronized (this.mAudioCacheUids) {
            this.mAudioCacheUids.clear();
        }
    }

    /* access modifiers changed from: private */
    public void updateAudioCache(int type, int pid, int uid) {
        if (type == 2) {
            if (!isInCalling()) {
                clearAudioCache();
            } else if (isInCallingBeforOneMin()) {
                setAudioCacheTimeOut(pid, uid);
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
        Map<Integer, AwareProcessBaseInfo> baseInfos = this.mHwAMS != null ? this.mHwAMS.getAllProcessBaseInfo() : null;
        if (baseInfos == null || baseInfos.isEmpty()) {
            return false;
        }
        for (Map.Entry<Integer, AwareProcessBaseInfo> entry : baseInfos.entrySet()) {
            AwareProcessBaseInfo valueInfo = (AwareProcessBaseInfo) entry.getValue();
            if (valueInfo != null && valueInfo.copy().mUid == uid) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void removeDecayForProcessDied(int stateType, int eventType, int pid, String pkg, int uid) {
        if (isNeedDecay(stateType, eventType) && existDecayInfo(stateType, eventType, pid, pkg, uid)) {
            if (pid != 0 || !isAppAlive(this.mContext, uid)) {
                DecayInfo decayInfo = removeDecayInfo(stateType, eventType, pid, pkg, uid);
                if (decayInfo != null) {
                    updateSceneState(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                    this.mAppKeyHandler.removeMessages(1, decayInfo);
                }
            }
        }
    }

    public boolean checkIsKeyBackgroupInternal(int pid, int uid) {
        if (!this.mIsInitialized.get()) {
            return false;
        }
        synchronized (this) {
            if (this.mKeyBackgroupPids.contains(Integer.valueOf(pid))) {
                return true;
            }
            if (this.mKeyBackgroupUids.contains(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        if (r4.mKeyBackgroupPids.contains(java.lang.Integer.valueOf(r5)) == false) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        r0 = getKeyBackgroupTypeByPidLocked(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002e, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002f, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003a, code lost:
        if (r4.mKeyBackgroupUids.contains(java.lang.Integer.valueOf(r6)) == false) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003c, code lost:
        r0 = getKeyBackgroupTypeByUidLocked(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0040, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0041, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0042, code lost:
        if (r7 != null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0044, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0045, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0046, code lost:
        r0 = getKeyBackgroupTypeByPkgsLocked(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004a, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004b, code lost:
        return r0;
     */
    public int getKeyBackgroupTypeInternal(int pid, int uid, List<String> pkgs) {
        if (!this.mIsInitialized.get()) {
            return -1;
        }
        synchronized (this.mAudioCacheUids) {
            if (this.mAudioCacheUids.contains(Integer.valueOf(uid))) {
                return 2;
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

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0022, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0029, code lost:
        if (r6.mSceneUidArray.isEmpty() == false) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002b, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002c, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003d, code lost:
        if (r6.mSceneUidArray.get(r7).contains(java.lang.Integer.valueOf(r9)) == false) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003f, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0040, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0051, code lost:
        if (r6.mScenePidArray.get(r7).contains(java.lang.Integer.valueOf(r8)) == false) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0053, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0054, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0055, code lost:
        if (r10 == null) goto L_0x0084;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005b, code lost:
        if (r10.isEmpty() == false) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005e, code lost:
        r0 = r6.mScenePkgArray.get(r7);
        r2 = r10.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x006e, code lost:
        if (r2.hasNext() == false) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0070, code lost:
        r4 = r2.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0076, code lost:
        if (r4 != null) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x007d, code lost:
        if (r0.contains(r4) == false) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x007f, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0080, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0082, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0083, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0084, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0085, code lost:
        return false;
     */
    public boolean checkKeyBackgroupByState(int state, int pid, int uid, List<String> pkgs) {
        if (!this.mIsInitialized.get()) {
            return false;
        }
        synchronized (this.mAudioCacheUids) {
            if (2 == state) {
                try {
                    if (this.mAudioCacheUids.contains(Integer.valueOf(uid))) {
                        return true;
                    }
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
        }
    }

    private boolean checkKeyBackgroupByState(int state, int pid, int uid, String pkg) {
        if (!this.mIsInitialized.get()) {
            return false;
        }
        synchronized (this) {
            if (this.mSceneUidArray.isEmpty()) {
                return false;
            }
            if (this.mSceneUidArray.get(state).contains(Integer.valueOf(uid))) {
                return true;
            }
            if (this.mScenePidArray.get(state).contains(Integer.valueOf(pid))) {
                return true;
            }
            if (this.mScenePkgArray.get(state).contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void updateSceneArrayProcessDied(int uid, int pid) {
        synchronized (this) {
            boolean appAlive = isAppAlive(this.mContext, uid);
            int size = this.mScenePidArray.size();
            for (int i = 1; i < size; i++) {
                if (this.mScenePidArray.get(i).contains(Integer.valueOf(pid))) {
                    this.mSceneUidArray.get(i).remove(Integer.valueOf(uid));
                    this.mScenePidArray.get(i).remove(Integer.valueOf(pid));
                    this.mKeyBackgroupUids.remove(Integer.valueOf(pid));
                } else if (uid != 0 && !appAlive) {
                    this.mSceneUidArray.get(i).remove(Integer.valueOf(uid));
                }
            }
            updateKbgUidsArrayLocked();
        }
    }

    private void updateScenePidArrayForInvalidPid(int uid) {
        if (DEBUG) {
            AwareLog.d(TAG, "updateScenePidArrayForInvalidPid uid " + uid);
        }
        List<ProcessInfo> procs = getProcessesByUid(uid);
        if (procs != null && !procs.isEmpty()) {
            for (ProcessInfo info : procs) {
                if (info != null) {
                    if (DEBUG) {
                        AwareLog.d(TAG, "updateScenePidArrayForInvalidPid pid " + info.mPid);
                    }
                    synchronized (this) {
                        int size = this.mScenePidArray.size();
                        for (int i = 1; i < size; i++) {
                            this.mScenePidArray.get(i).remove(Integer.valueOf(info.mPid));
                        }
                        this.mKeyBackgroupUids.remove(Integer.valueOf(info.mPid));
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
                    this.mScenePidArray.get(stateType).add(Integer.valueOf(pid));
                }
                if (uid != 0) {
                    this.mSceneUidArray.get(stateType).add(Integer.valueOf(uid));
                }
                if (pkg != null && !pkg.isEmpty()) {
                    this.mScenePkgArray.get(stateType).add(pkg);
                }
            } else if (eventType == 2) {
                if (pid != 0) {
                    this.mScenePidArray.get(stateType).remove(Integer.valueOf(pid));
                }
                if (uid != 0) {
                    this.mSceneUidArray.get(stateType).remove(Integer.valueOf(uid));
                }
                if (pkg != null && !pkg.isEmpty()) {
                    this.mScenePkgArray.get(stateType).remove(pkg);
                }
            }
            updateLastCallTime(stateType, eventType, pid, pkg, uid);
        }
    }

    /* access modifiers changed from: private */
    public void updateSceneState(int stateType, int eventType, int pid, String pkg, int uid) {
        if (DEBUG) {
            AwareLog.d(TAG, "state type: " + stateType + " eventType:" + eventType + " pid:" + pid + " uid:" + uid + " pkg:" + pkg);
        }
        if (stateType >= 1 && stateType < STATE_SIZE) {
            int realpid = pid;
            if (pid == -1) {
                updateScenePidArrayForInvalidPid(uid);
                realpid = 0;
            }
            notifyStateChange(stateType, eventType, realpid, uid);
            synchronized (this) {
                updateSceneArrayLocked(stateType, eventType, realpid, pkg, uid);
                if (eventType == 1 && !this.mKeyBackgroupUids.contains(Integer.valueOf(uid))) {
                    notifyStateChange(0, eventType, realpid, uid);
                }
                this.mKeyBackgroupPids.clear();
                this.mKeyBackgroupUids.clear();
                this.mKeyBackgroupPkgs.clear();
                int size = this.mScenePidArray.size();
                for (int i = 0; i < size; i++) {
                    this.mKeyBackgroupPids.addAll(this.mScenePidArray.get(i));
                    this.mKeyBackgroupUids.addAll(this.mSceneUidArray.get(i));
                    this.mKeyBackgroupPkgs.addAll(this.mScenePkgArray.get(i));
                }
                if (eventType == 2 && !this.mKeyBackgroupUids.contains(Integer.valueOf(uid))) {
                    notifyStateChange(0, eventType, realpid, uid);
                }
                if (DEBUG && this.mScenePidArray.size() > stateType) {
                    AwareLog.d(TAG, "stateChanged " + stateToString(stateType) + " mPids:" + this.mScenePidArray.get(stateType));
                    AwareLog.d(TAG, "stateChanged " + stateToString(stateType) + " mUids:" + this.mSceneUidArray.get(stateType));
                    AwareLog.d(TAG, "stateChanged " + stateToString(stateType) + " mPkgs:" + this.mScenePkgArray.get(stateType));
                }
            }
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            if (!this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            pw.println("dump Important State Apps start --------");
            synchronized (this) {
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
            pw.println("nat timeout:" + this.mNatTimeout);
            pw.println("audio prevent max time:" + mAudioPreventMaxTime);
            pw.println("dump Important State Apps end-----------");
        }
    }

    public void dumpCheckStateByPid(PrintWriter pw, Context context, int state, int pid) {
        if (pw != null) {
            if (this.mPGSdk == null || !this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            pw.println("----------------------------------------");
            boolean result = false;
            try {
                result = this.mPGSdk.checkStateByPid(context, pid, state);
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
            if (this.mPGSdk == null || !this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            pw.println("----------------------------------------");
            boolean result = false;
            try {
                result = this.mPGSdk.checkStateByPkg(context, pkg, state);
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
            if (this.mPGSdk == null || !this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            int type = 0;
            pw.println("----------------------------------------");
            try {
                type = this.mPGSdk.getPkgType(context, pkg);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "getAppType occur exception.");
            }
            pw.println("CheckType Package:" + pkg);
            pw.println("type:" + typeToString(type));
            pw.println("----------------------------------------");
        }
    }

    public void dumpFakeEvent(PrintWriter pw, int stateType, int eventType, int pid, String pkg, int uid) {
        if (pw != null && pkg != null) {
            if (this.mPGSdk == null || !this.mIsInitialized.get()) {
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
        if (this.mScenePidArray.get(2).contains(Integer.valueOf(pid))) {
            return 2;
        }
        if (this.mScenePidArray.get(1).contains(Integer.valueOf(pid))) {
            return 1;
        }
        if (this.mScenePidArray.get(3).contains(Integer.valueOf(pid))) {
            return 3;
        }
        if (this.mScenePidArray.get(5).contains(Integer.valueOf(pid))) {
            return 5;
        }
        if (this.mScenePidArray.get(4).contains(Integer.valueOf(pid))) {
            return 4;
        }
        return -1;
    }

    private int getKeyBackgroupTypeByUidLocked(int uid) {
        if (this.mSceneUidArray.isEmpty()) {
            return -1;
        }
        if (this.mSceneUidArray.get(2).contains(Integer.valueOf(uid))) {
            return 2;
        }
        if (this.mSceneUidArray.get(1).contains(Integer.valueOf(uid))) {
            return 1;
        }
        if (this.mSceneUidArray.get(3).contains(Integer.valueOf(uid))) {
            return 3;
        }
        if (this.mSceneUidArray.get(5).contains(Integer.valueOf(uid))) {
            return 5;
        }
        if (this.mSceneUidArray.get(4).contains(Integer.valueOf(uid))) {
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
    public void handleSensorEvent(int uid, int sensor, boolean enable) {
        if (DEBUG) {
            AwareLog.i(TAG, "sensor:" + sensor + " enable:" + enable + " uid:" + uid);
        }
        synchronized (this) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0030, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r0 = r7.mPGSdk.getSensorInfoByUid(r8, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        if (r0 == null) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
        r1 = r0.entrySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0046, code lost:
        if (r1.hasNext() == false) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0048, code lost:
        r2 = r1.next();
        r3 = java.lang.Integer.parseInt((java.lang.String) r2.getKey());
        r4 = java.lang.Integer.parseInt((java.lang.String) r2.getValue());
        r6 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0064, code lost:
        if (r6 > r4) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0066, code lost:
        handleSensorEvent(r9, r3, true);
        r6 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006f, code lost:
        if (DEBUG == false) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0071, code lost:
        android.rms.iaware.AwareLog.d(TAG, "getSensorInfoByUid sensor handles " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0089, code lost:
        android.rms.iaware.AwareLog.e(TAG, "integer parse error!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0092, code lost:
        android.rms.iaware.AwareLog.e(TAG, "error, PG crash!");
     */
    private void initAppSensorState(Context context, int uid) {
        if (this.mPGSdk == null) {
            AwareLog.e(TAG, "KeyBackGroup feature not enabled.");
            return;
        }
        synchronized (this) {
            if (this.mHistorySensorRecords.get(uid) != null) {
                if (DEBUG) {
                    AwareLog.d(TAG, "History Sensor Records has uid " + uid);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateAppSensorState(boolean sensorStart, int uid, int pid) {
        if (DEBUG) {
            AwareLog.i(TAG, "updateAppSensorState :" + uid + " pid " + pid);
        }
        updateSceneState(4, sensorStart ? 1 : 2, pid, null, uid);
    }

    private void sendDecayMesssage(int message, DecayInfo decayInfo, long delayT) {
        if (this.mAppKeyHandler.hasMessages(message, decayInfo)) {
            this.mAppKeyHandler.removeMessages(message, decayInfo);
        }
        Message observerMsg = this.mAppKeyHandler.obtainMessage();
        observerMsg.what = message;
        observerMsg.obj = decayInfo;
        this.mAppKeyHandler.sendMessageDelayed(observerMsg, delayT);
        if (DEBUG) {
            AwareLog.d(TAG, "sendDecayMesssage end " + message + " decayinfo " + decayInfo);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004e, code lost:
        if (r3 != false) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0050, code lost:
        if (r4 == false) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0053, code lost:
        r14 = r16;
        r13 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0057, code lost:
        r7 = r5;
        r9 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005a, code lost:
        r8 = r1.mStateEventDecayInfos;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        monitor-enter(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r9 = r1.mStateEventDecayInfos.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0067, code lost:
        if (r9.hasNext() == false) goto L_0x009d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0069, code lost:
        r10 = r9.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0073, code lost:
        if (r10.getStateType() != 2) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0079, code lost:
        if (r10.getEventType() == 2) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x007c, code lost:
        r11 = r10.getUid();
        r12 = r10.getPid();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0086, code lost:
        if (r11 != r17) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0088, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x008a, code lost:
        if (r12 != -1) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x008c, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x008f, code lost:
        if (r12 != r16) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0091, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0092, code lost:
        if (r5 == false) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0094, code lost:
        if (r6 == false) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x009d, code lost:
        r14 = r16;
        r13 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        monitor-exit(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a2, code lost:
        if (r3 == false) goto L_0x00a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00a4, code lost:
        if (r5 == false) goto L_0x00aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00a6, code lost:
        if (r4 == false) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00a8, code lost:
        if (r6 != false) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00aa, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ab, code lost:
        r10 = isInCalling();
        r11 = r1.mAudioCacheUids;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b1, code lost:
        monitor-enter(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b2, code lost:
        if (r10 == false) goto L_0x00c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b4, code lost:
        if (r7 != false) goto L_0x00c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        if (r1.mAudioCacheUids.contains(java.lang.Integer.valueOf(r17)) == false) goto L_0x00c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c2, code lost:
        monitor-exit(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c3, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00c4, code lost:
        monitor-exit(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00c5, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00c9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00ca, code lost:
        r14 = r16;
        r13 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
        monitor-exit(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x00cf, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x00d0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x00d9, code lost:
        r0 = th;
     */
    public boolean checkAudioOutInstant(int pid, int uid, List<String> pkgs) {
        if (!this.mIsInitialized.get() || pkgs == null || pkgs.isEmpty()) {
            int i = pid;
            int i2 = uid;
            return false;
        }
        boolean sceneContainsUid = false;
        boolean sceneContainsPid = false;
        boolean decayContainsUid = false;
        boolean decayContainsPid = false;
        synchronized (this) {
            try {
                if (this.mSceneUidArray.isEmpty()) {
                    return false;
                }
                if (this.mSceneUidArray.get(2).contains(Integer.valueOf(uid))) {
                    sceneContainsUid = true;
                }
                if (this.mScenePidArray.get(2).contains(Integer.valueOf(pid))) {
                    sceneContainsPid = true;
                }
            } finally {
                th = th;
                int i3 = pid;
                int i4 = uid;
                while (true) {
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setNatTimeout(int natTime) {
        this.mNatTimeout = natTime;
        AwareWakeUpManager.getInstance().setIntervalOverload(natTime);
    }
}
