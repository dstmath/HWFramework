package com.android.server.rms.iaware.appmng;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver.Stub;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
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
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.pgmng.plug.PGSdk.Sink;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppKeyBackgroup {
    private static final String[] APPTYPESTRING = new String[]{"TYPE_UNKNOW", "TYPE_LAUNCHER", "TYPE_SMS", "TYPE_EMAIL", "TYPE_INPUTMETHOD", "TYPE_GAME", "TYPE_BROWSER", "TYPE_EBOOK", "TYPE_VIDEO", "TYPE_SCRLOCK", "TYPE_CLOCK", "TYPE_IM", "TYPE_MUSIC"};
    private static final String CALL_APP_PKG = "com.android.incallui";
    private static boolean DEBUG = false;
    private static final long DECAY_TIME = 60000;
    private static final long DECAY_UPLOAD_DL_TIME = 10000;
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    private static final String HUAWEI_AMAP_APP_PKG = "com.amap.android.ams";
    private static final int MSG_APP_PROCESSDIED = 2;
    private static final int MSG_ASSOCIATE_CHECK = 4;
    private static final int MSG_PGSDK_INIT = 3;
    private static final int MSG_REMOVE_DECAY_STATE = 1;
    private static final long PGSDK_REINIT_TIME = 2000;
    private static final int PID_INVALID = -1;
    private static final String[] STATESTRING = new String[]{"STATE_NULL", "STATE_AUDIO_IN", "STATE_AUDIO_OUT", "STATE_GPS", "STATE_SENSOR", "STATE_UPLOAD_DL"};
    public static final int STATE_ALL = 100;
    public static final int STATE_AUDIO_IN = 1;
    public static final int STATE_AUDIO_OUT = 2;
    public static final int STATE_GPS = 3;
    public static final int STATE_IMEMAIL = 99;
    public static final int STATE_KEY_BG = 0;
    public static final int STATE_KEY_BG_INVALID = -1;
    public static final int STATE_SENSOR = 4;
    private static final int STATE_SIZE = STATESTRING.length;
    public static final int STATE_UPLOAD_DL = 5;
    private static final String TAG = "AwareAppKeyBackgroup";
    private static final int TYPE_SIZE = APPTYPESTRING.length;
    private static final String WECHAT_APP_PKG = "com.tencent.mm";
    private static AwareAppKeyBackgroup sInstance = null;
    private int mAmapAppUid;
    private AppKeyHandler mAppKeyHandler;
    private final ArraySet<Integer> mAudioCacheUids;
    PhoneStateListener mCallStateListener;
    private final ArrayMap<IAwareStateCallback, ArraySet<Integer>> mCallbacks;
    private Context mContext;
    private final SparseArray<SensorRecord> mHistorySensorRecords;
    private HwActivityManagerService mHwAMS;
    private boolean mIsAbroadArea;
    private AtomicBoolean mIsInitialized;
    private AtomicBoolean mIsInitializing;
    private final ArraySet<Integer> mKeyBackgroupPids;
    private final ArraySet<String> mKeyBackgroupPkgs;
    private final ArraySet<Integer> mKeyBackgroupUids;
    private AtomicBoolean mLastSetting;
    private PGSdk mPGSdk;
    private PackageManager mPM;
    private AwareABGProcessObserver mProcessObserver;
    private final ArrayList<ArraySet<Integer>> mScenePidArray;
    private final ArrayList<ArraySet<String>> mScenePkgArray;
    private final ArrayList<ArraySet<Integer>> mSceneUidArray;
    private final List<DecayInfo> mStateEventDecayInfos;
    private Sink mStateRecognitionListener;

    public interface IAwareStateCallback {
        void onStateChanged(int i, int i2, int i3, int i4);
    }

    private class AppKeyHandler extends Handler {
        public AppKeyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (AwareAppKeyBackgroup.DEBUG) {
                AwareLog.e(AwareAppKeyBackgroup.TAG, "handleMessage message " + msg.what);
            }
            if (msg.what == 3) {
                AwareAppKeyBackgroup.this.doInitialize();
            } else if (AwareAppKeyBackgroup.this.mIsInitialized.get()) {
                DecayInfo decayInfo = msg.obj instanceof DecayInfo ? (DecayInfo) msg.obj : null;
                switch (msg.what) {
                    case 1:
                        if (decayInfo != null) {
                            if (AwareAppKeyBackgroup.DEBUG) {
                                AwareLog.d(AwareAppKeyBackgroup.TAG, "Update state " + decayInfo.getStateType() + " uid : " + decayInfo.getUid());
                            }
                            AwareAppKeyBackgroup.this.updateSceneState(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                            AwareAppKeyBackgroup.this.removeDecayInfo(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                            if (decayInfo.getStateType() == 2) {
                                AwareAppKeyBackgroup.this.updateAudioCache(decayInfo.getPid(), decayInfo.getUid());
                                break;
                            }
                        }
                        return;
                        break;
                    case 2:
                        int pid = msg.arg1;
                        int uid = msg.arg2;
                        AwareAppKeyBackgroup.this.updateSceneArrayProcessDied(uid, pid);
                        if (AwareAppKeyBackgroup.this.getStateEventDecayInfosSize() > 0) {
                            for (int i = 1; i < AwareAppKeyBackgroup.STATE_SIZE; i++) {
                                int i2;
                                AwareAppKeyBackgroup awareAppKeyBackgroup = AwareAppKeyBackgroup.this;
                                if (i == 2) {
                                    i2 = pid;
                                } else {
                                    i2 = 0;
                                }
                                awareAppKeyBackgroup.removeDecayForProcessDied(i, 2, i2, null, uid);
                            }
                            break;
                        }
                        break;
                    case 4:
                        if (decayInfo != null && decayInfo.getStateType() == 3) {
                            AwareAppKeyBackgroup.this.checkHuaweiAmapApp(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getUid());
                            break;
                        }
                }
            }
        }
    }

    class AwareABGProcessObserver extends Stub {
        AwareABGProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onProcessDied(int pid, int uid) {
            synchronized (AwareAppKeyBackgroup.this) {
                boolean isKbgPid = AwareAppKeyBackgroup.this.mKeyBackgroupPids.contains(Integer.valueOf(pid));
                boolean isKbgUid = AwareAppKeyBackgroup.this.mKeyBackgroupUids.contains(Integer.valueOf(uid));
                if (isKbgPid || (isKbgUid ^ 1) == 0) {
                    if (AwareAppKeyBackgroup.DEBUG) {
                        AwareLog.d(AwareAppKeyBackgroup.TAG, "onProcessDied pid " + pid + " uid " + uid);
                    }
                    Message observerMsg = AwareAppKeyBackgroup.this.mAppKeyHandler.obtainMessage();
                    observerMsg.arg1 = pid;
                    observerMsg.arg2 = uid;
                    observerMsg.what = 2;
                    AwareAppKeyBackgroup.this.mAppKeyHandler.sendMessage(observerMsg);
                    return;
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

        public int hashCode() {
            return super.hashCode();
        }

        public boolean equals(Object obj) {
            boolean z = true;
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
            if (other.getStateType() != this.mStateType || other.getEventType() != this.mEventType || other.getPid() != this.mPid) {
                z = false;
            } else if (other.getUid() != this.mUid) {
                z = false;
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

    private class SensorRecord {
        private final ArrayMap<Integer, Integer> mHandles = new ArrayMap();
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
            Integer count = (Integer) this.mHandles.get(Integer.valueOf(handle));
            if (count == null) {
                this.mHandles.put(Integer.valueOf(handle), Integer.valueOf(1));
            } else {
                this.mHandles.put(Integer.valueOf(handle), Integer.valueOf(count.intValue() + 1));
            }
            if (AwareAppKeyBackgroup.DEBUG) {
                AwareLog.i(AwareAppKeyBackgroup.TAG, "addSensor,mHandles:" + this.mHandles);
            }
        }

        public void removeSensor(Integer handle) {
            Integer count = (Integer) this.mHandles.get(handle);
            if (count != null) {
                int value = count.intValue() - 1;
                if (value <= 0) {
                    this.mHandles.remove(handle);
                } else {
                    this.mHandles.put(handle, Integer.valueOf(value));
                }
            }
            if (AwareAppKeyBackgroup.DEBUG) {
                AwareLog.i(AwareAppKeyBackgroup.TAG, "removeSensor,mHandles:" + this.mHandles);
            }
            if (!hasSensor()) {
                AwareAppKeyBackgroup.this.updateAppSensorState(false, this.mUid, 0);
            }
        }
    }

    private static String stateToString(int state) {
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

    private boolean checkCallingPermission() {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        return pid == Process.myPid() || uid == 0 || uid == 1000;
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
        boolean z = true;
        int type = getAppTypeFromHabit(uid);
        if (DEBUG) {
            AwareLog.d(TAG, "getAppTypeFromHabit uid " + uid + " type : " + type);
        }
        switch (type) {
            case -1:
            case 2:
            case 3:
                return true;
            default:
                if (type <= 255) {
                    z = false;
                }
                return z;
        }
    }

    private boolean shouldFilter(int stateType, int uid) {
        return (stateType == 3 || stateType == 4) ? isNaviOrSportApp(uid) ^ 1 : false;
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

    protected void registerCallStateListener(Context cxt) {
        if (cxt != null) {
            ((TelephonyManager) cxt.getSystemService("phone")).listen(this.mCallStateListener, 32);
        }
    }

    protected void unregisterCallStateListener(Context cxt) {
        if (cxt != null) {
            ((TelephonyManager) cxt.getSystemService("phone")).listen(this.mCallStateListener, 0);
        }
    }

    private final void updateCallState(int state) {
        if (this.mIsInitialized.get()) {
            int eventType = state == 0 ? 2 : 1;
            synchronized (this) {
                updateSceneArrayLocked(2, eventType, 0, CALL_APP_PKG, 0);
            }
            if (eventType == 2) {
                synchronized (this.mAudioCacheUids) {
                    this.mAudioCacheUids.clear();
                }
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
        this.mAmapAppUid = 0;
        this.mScenePidArray = new ArrayList();
        this.mSceneUidArray = new ArrayList();
        this.mScenePkgArray = new ArrayList();
        this.mKeyBackgroupPids = new ArraySet();
        this.mKeyBackgroupUids = new ArraySet();
        this.mKeyBackgroupPkgs = new ArraySet();
        this.mStateEventDecayInfos = new ArrayList();
        this.mCallbacks = new ArrayMap();
        this.mHistorySensorRecords = new SparseArray();
        this.mAudioCacheUids = new ArraySet();
        this.mStateRecognitionListener = new Sink() {
            public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
                boolean z = true;
                if (!AwareAppKeyBackgroup.this.mIsInitialized.get() || !AwareAppKeyBackgroup.this.checkCallingPermission() || pid == Process.myPid() || AwareAppKeyBackgroup.this.shouldFilter(stateType, uid)) {
                    return;
                }
                if (stateType == 4) {
                    AwareAppKeyBackgroup awareAppKeyBackgroup = AwareAppKeyBackgroup.this;
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
                    AwareAppKeyBackgroup.this.checkAssociateApp(stateType, eventType, pid, pkg, uid);
                    if (AwareAppKeyBackgroup.DEBUG) {
                        AwareLog.d(AwareAppKeyBackgroup.TAG, "Update Scene state using " + (SystemClock.currentTimeMicro() - timeStamp) + " us");
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
                ArraySet<Integer> states = (ArraySet) this.mCallbacks.get(callback);
                if (states == null) {
                    states = new ArraySet();
                    states.add(Integer.valueOf(stateType));
                    this.mCallbacks.put(callback, states);
                } else {
                    states.add(Integer.valueOf(stateType));
                }
            }
        }
    }

    public void unregisterStateCallback(IAwareStateCallback callback, int stateType) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                ArraySet<Integer> states = (ArraySet) this.mCallbacks.get(callback);
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
            if (this.mCallbacks.isEmpty()) {
                return;
            }
            for (Entry<IAwareStateCallback, ArraySet<Integer>> m : this.mCallbacks.entrySet()) {
                IAwareStateCallback callback = (IAwareStateCallback) m.getKey();
                ArraySet<Integer> states = (ArraySet) m.getValue();
                if (states != null && (states.contains(Integer.valueOf(stateType)) || states.contains(Integer.valueOf(100)))) {
                    callback.onStateChanged(stateType, eventType, pid == -1 ? 0 : pid, uid);
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

    private void doInitialize() {
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
        if (ensureInitialize()) {
            this.mIsInitializing.set(false);
            checkLastSetting();
            return;
        }
        if (this.mAppKeyHandler.hasMessages(3)) {
            this.mAppKeyHandler.removeMessages(3);
        }
        this.mAppKeyHandler.sendEmptyMessageDelayed(3, PGSDK_REINIT_TIME);
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
                this.mAppKeyHandler.removeCallbacksAndMessages(null);
                this.mIsInitialized.set(false);
            } catch (RemoteException e) {
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
                            this.mAppKeyHandler.removeCallbacksAndMessages(null);
                            this.mIsInitialized.set(false);
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
                            this.mAppKeyHandler.removeCallbacksAndMessages(null);
                            this.mIsInitialized.set(false);
                        }
                    }
                }
            }
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

    private void resumeAppStates(Context context) {
        if (context != null) {
            ArrayList<ProcessInfo> procs = ProcessInfoCollector.getInstance().getProcessInfoList();
            long timeStamp = 0;
            if (DEBUG) {
                timeStamp = SystemClock.currentTimeMicro();
            }
            int size = procs.size();
            for (int j = 0; j < size; j++) {
                ProcessInfo procInfo = (ProcessInfo) procs.get(j);
                if (procInfo != null) {
                    int pid = procInfo.mPid;
                    int uid = procInfo.mUid;
                    ArrayList<String> packages = procInfo.mPackageName;
                    for (int i = 1; i <= 5; i++) {
                        if (!shouldFilter(i, uid)) {
                            boolean state;
                            if (i <= 2) {
                                state = false;
                                try {
                                    state = this.mPGSdk.checkStateByPid(context, pid, i);
                                } catch (RemoteException e) {
                                    AwareLog.e(TAG, "checkStateByPid occur exception.");
                                }
                                if (state) {
                                    updateSceneState(i, 1, pid, null, uid);
                                }
                            } else if (i == 4) {
                                initAppSensorState(context, uid);
                            } else {
                                int psize = packages.size();
                                for (int k = 0; k < psize; k++) {
                                    state = false;
                                    try {
                                        state = this.mPGSdk.checkStateByPkg(context, (String) packages.get(k), i);
                                    } catch (RemoteException e2) {
                                        AwareLog.e(TAG, "checkStateByPkg occur exception.");
                                    }
                                    if (state) {
                                        updateSceneState(i, 1, 0, null, uid);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (DEBUG) {
                AwareLog.d(TAG, "resumeAppStates done using " + (SystemClock.currentTimeMicro() - timeStamp) + " us");
            }
        }
    }

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
        boolean z = true;
        if ((stateType != 2 && stateType != 5) || eventType != 1) {
            return false;
        }
        if (getStateEventDecayInfosSize() <= 0) {
            z = false;
        }
        return z;
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

    private DecayInfo removeDecayInfo(int stateType, int eventType, int pid, String pkg, int uid) {
        synchronized (this.mStateEventDecayInfos) {
            DecayInfo decayInfo = getDecayInfo(stateType, eventType, pid, pkg, uid);
            if (decayInfo != null) {
                this.mStateEventDecayInfos.remove(decayInfo);
                return decayInfo;
            }
            return null;
        }
    }

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
        List<ProcessInfo> procs = new ArrayList();
        int size = procList.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo info = (ProcessInfo) procList.get(i);
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
        int i = 0;
        int size = procList.size();
        while (i < size) {
            if (procList.get(i) != null && ((ProcessInfo) procList.get(i)).mPackageName != null && ((ProcessInfo) procList.get(i)).mPackageName.contains(WECHAT_APP_PKG) && checkKeyBackgroupByState(2, ((ProcessInfo) procList.get(i)).mPid, ((ProcessInfo) procList.get(i)).mUid, WECHAT_APP_PKG)) {
                return true;
            }
            i++;
        }
        return false;
    }

    private void updateAudioCache(int pid, int uid) {
        boolean iscalling = isInCalling();
        synchronized (this.mAudioCacheUids) {
            if (iscalling) {
                this.mAudioCacheUids.add(Integer.valueOf(uid));
            } else {
                this.mAudioCacheUids.clear();
            }
        }
    }

    public boolean isAudioCache(int uid) {
        if (!this.mIsInitialized.get()) {
            return false;
        }
        boolean contains;
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
        for (Entry entry : baseInfos.entrySet()) {
            AwareProcessBaseInfo valueInfo = (AwareProcessBaseInfo) entry.getValue();
            if (valueInfo != null && valueInfo.copy().mUid == uid) {
                return true;
            }
        }
        return false;
    }

    private void checkAssociateApp(int stateType, int eventType, int pid, String pkg, int uid) {
        if (stateType == 3) {
            sendDecayMesssage(4, new DecayInfo(stateType, eventType, pid, pkg, uid), 0);
        }
    }

    private void checkHuaweiAmapApp(int stateType, int eventType, int uid) {
        if (this.mAmapAppUid == 0) {
            this.mAmapAppUid = getUidByPkg(HUAWEI_AMAP_APP_PKG);
        }
        if (this.mAmapAppUid > 0 && this.mAmapAppUid == uid) {
            Set<String> strong = new ArraySet();
            if (eventType == 1) {
                AwareAppAssociate.getInstance().getAssocClientListForUid(uid, strong);
                if (strong.isEmpty()) {
                    return;
                }
            }
            synchronized (this) {
                updateGPSPkgArrayLocked(stateType, eventType, strong);
            }
        }
    }

    private int getUidByPkg(String pkg) {
        ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procList.isEmpty()) {
            return 0;
        }
        int size = procList.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo info = (ProcessInfo) procList.get(i);
            if (info != null && info.mPackageName != null && info.mPackageName.contains(pkg)) {
                return info.mUid;
            }
        }
        return 0;
    }

    private void updateGPSPkgArrayLocked(int stateType, int eventType, Set<String> pkgs) {
        if (DEBUG) {
            AwareLog.d(TAG, "updateGPSPkgArrayLocked eventType : " + eventType + " pkgs : " + pkgs);
        }
        if (!this.mScenePkgArray.isEmpty()) {
            if (eventType != 1) {
                ((ArraySet) this.mScenePkgArray.get(stateType)).clear();
            } else if (this.mIsAbroadArea) {
                ((ArraySet) this.mScenePkgArray.get(stateType)).addAll(pkgs);
            } else {
                for (String pkg : pkgs) {
                    if (AppTypeRecoManager.getInstance().getAppType(pkg) == 3) {
                        ((ArraySet) this.mScenePkgArray.get(stateType)).add(pkg);
                    }
                }
            }
        }
    }

    private void removeDecayForProcessDied(int stateType, int eventType, int pid, String pkg, int uid) {
        if (!isNeedDecay(stateType, eventType) || !existDecayInfo(stateType, eventType, pid, pkg, uid)) {
            return;
        }
        if (pid != 0 || (isAppAlive(this.mContext, uid) ^ 1) != 0) {
            DecayInfo decayInfo = removeDecayInfo(stateType, eventType, pid, pkg, uid);
            if (decayInfo != null) {
                updateSceneState(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                this.mAppKeyHandler.removeMessages(1, decayInfo);
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
            } else if (this.mKeyBackgroupUids.contains(Integer.valueOf(uid))) {
                return true;
            } else {
                return false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001d, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:15:0x0028, code:
            if (r4.mKeyBackgroupPids.contains(java.lang.Integer.valueOf(r5)) == false) goto L_0x0033;
     */
    /* JADX WARNING: Missing block: B:16:0x002a, code:
            r0 = getKeyBackgroupTypeByPidLocked(r5);
     */
    /* JADX WARNING: Missing block: B:17:0x002e, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:18:0x002f, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:24:0x003d, code:
            if (r4.mKeyBackgroupUids.contains(java.lang.Integer.valueOf(r6)) == false) goto L_0x0045;
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            r0 = getKeyBackgroupTypeByUidLocked(r6);
     */
    /* JADX WARNING: Missing block: B:26:0x0043, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:27:0x0044, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:28:0x0045, code:
            if (r7 != null) goto L_0x0049;
     */
    /* JADX WARNING: Missing block: B:29:0x0047, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:30:0x0048, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            r0 = getKeyBackgroupTypeByPkgsLocked(r7);
     */
    /* JADX WARNING: Missing block: B:33:0x004d, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:34:0x004e, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    /* JADX WARNING: Missing block: B:13:0x0020, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:16:0x0027, code:
            if (r8.mSceneUidArray.isEmpty() == false) goto L_0x002e;
     */
    /* JADX WARNING: Missing block: B:17:0x0029, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:18:0x002a, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:24:0x003e, code:
            if (((android.util.ArraySet) r8.mSceneUidArray.get(r9)).contains(java.lang.Integer.valueOf(r11)) == false) goto L_0x0042;
     */
    /* JADX WARNING: Missing block: B:25:0x0040, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:26:0x0041, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:29:0x0052, code:
            if (((android.util.ArraySet) r8.mScenePidArray.get(r9)).contains(java.lang.Integer.valueOf(r10)) == false) goto L_0x0056;
     */
    /* JADX WARNING: Missing block: B:30:0x0054, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:31:0x0055, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:32:0x0056, code:
            if (r12 == null) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:35:0x005c, code:
            if (r12.isEmpty() == false) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:36:0x005e, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:37:0x005f, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:39:?, code:
            r0 = (android.util.ArraySet) r8.mScenePkgArray.get(r9);
            r2 = r12.iterator();
     */
    /* JADX WARNING: Missing block: B:41:0x0070, code:
            if (r2.hasNext() == false) goto L_0x0082;
     */
    /* JADX WARNING: Missing block: B:42:0x0072, code:
            r1 = (java.lang.String) r2.next();
     */
    /* JADX WARNING: Missing block: B:43:0x0078, code:
            if (r1 == null) goto L_0x006c;
     */
    /* JADX WARNING: Missing block: B:45:0x007e, code:
            if (r0.contains(r1) == false) goto L_0x006c;
     */
    /* JADX WARNING: Missing block: B:46:0x0080, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:47:0x0081, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:48:0x0082, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:49:0x0083, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkKeyBackgroupByState(int state, int pid, int uid, List<String> pkgs) {
        if (!this.mIsInitialized.get()) {
            return false;
        }
        synchronized (this.mAudioCacheUids) {
            if (2 == state) {
                if (this.mAudioCacheUids.contains(Integer.valueOf(uid))) {
                    return true;
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
            } else if (((ArraySet) this.mSceneUidArray.get(state)).contains(Integer.valueOf(uid))) {
                return true;
            } else if (((ArraySet) this.mScenePidArray.get(state)).contains(Integer.valueOf(pid))) {
                return true;
            } else if (((ArraySet) this.mScenePkgArray.get(state)).contains(pkg)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void updateSceneArrayProcessDied(int uid, int pid) {
        synchronized (this) {
            boolean appAlive = isAppAlive(this.mContext, uid);
            int size = this.mScenePidArray.size();
            for (int i = 1; i < size; i++) {
                if (((ArraySet) this.mScenePidArray.get(i)).contains(Integer.valueOf(pid))) {
                    ((ArraySet) this.mSceneUidArray.get(i)).remove(Integer.valueOf(uid));
                    ((ArraySet) this.mScenePidArray.get(i)).remove(Integer.valueOf(pid));
                    this.mKeyBackgroupUids.remove(Integer.valueOf(pid));
                } else if (!(uid == 0 || (appAlive ^ 1) == 0)) {
                    ((ArraySet) this.mSceneUidArray.get(i)).remove(Integer.valueOf(uid));
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
                            ((ArraySet) this.mScenePidArray.get(i)).remove(Integer.valueOf(info.mPid));
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
            this.mKeyBackgroupUids.addAll((ArraySet) this.mSceneUidArray.get(i));
        }
    }

    private void updateSceneArrayLocked(int stateType, int eventType, int pid, String pkg, int uid) {
        if (!this.mSceneUidArray.isEmpty()) {
            if (eventType == 1) {
                if (pid != 0) {
                    ((ArraySet) this.mScenePidArray.get(stateType)).add(Integer.valueOf(pid));
                }
                if (uid != 0) {
                    ((ArraySet) this.mSceneUidArray.get(stateType)).add(Integer.valueOf(uid));
                }
                if (!(pkg == null || (pkg.isEmpty() ^ 1) == 0)) {
                    ((ArraySet) this.mScenePkgArray.get(stateType)).add(pkg);
                }
            } else if (eventType == 2) {
                if (pid != 0) {
                    ((ArraySet) this.mScenePidArray.get(stateType)).remove(Integer.valueOf(pid));
                }
                if (uid != 0) {
                    ((ArraySet) this.mSceneUidArray.get(stateType)).remove(Integer.valueOf(uid));
                }
                if (!(pkg == null || (pkg.isEmpty() ^ 1) == 0)) {
                    ((ArraySet) this.mScenePkgArray.get(stateType)).remove(pkg);
                }
            }
        }
    }

    private void updateSceneState(int stateType, int eventType, int pid, String pkg, int uid) {
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
                if (eventType == 1 && (this.mKeyBackgroupUids.contains(Integer.valueOf(uid)) ^ 1) != 0) {
                    notifyStateChange(0, eventType, realpid, uid);
                }
                this.mKeyBackgroupPids.clear();
                this.mKeyBackgroupUids.clear();
                this.mKeyBackgroupPkgs.clear();
                int size = this.mScenePidArray.size();
                for (int i = 0; i < size; i++) {
                    this.mKeyBackgroupPids.addAll((ArraySet) this.mScenePidArray.get(i));
                    this.mKeyBackgroupUids.addAll((ArraySet) this.mSceneUidArray.get(i));
                    this.mKeyBackgroupPkgs.addAll((ArraySet) this.mScenePkgArray.get(i));
                }
                if (eventType == 2 && (this.mKeyBackgroupUids.contains(Integer.valueOf(uid)) ^ 1) != 0) {
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
            if (this.mIsInitialized.get()) {
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
                pw.println("dump Important State Apps end-----------");
                return;
            }
            pw.println("KeyBackGroup feature not enabled.");
        }
    }

    public void dumpCheckStateByPid(PrintWriter pw, Context context, int state, int pid) {
        if (pw != null) {
            if (this.mPGSdk == null || (this.mIsInitialized.get() ^ 1) != 0) {
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
            if (this.mPGSdk == null || (this.mIsInitialized.get() ^ 1) != 0) {
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
            if (this.mPGSdk == null || (this.mIsInitialized.get() ^ 1) != 0) {
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
            if (this.mPGSdk == null || (this.mIsInitialized.get() ^ 1) != 0) {
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
            if (this.mIsInitialized.get()) {
                pw.println("dump CheckKeyBackGroup State start --------");
                pw.println("Check Pid:" + pid);
                pw.println("Check Uid:" + uid);
                pw.println("result:" + checkIsKeyBackgroup(pid, uid));
                pw.println("dump CheckKeyBackGroup State end-----------");
                return;
            }
            pw.println("KeyBackGroup feature not enabled.");
        }
    }

    private int getKeyBackgroupTypeByPidLocked(int pid) {
        if (this.mScenePidArray.isEmpty()) {
            return -1;
        }
        if (((ArraySet) this.mScenePidArray.get(2)).contains(Integer.valueOf(pid))) {
            return 2;
        }
        if (((ArraySet) this.mScenePidArray.get(1)).contains(Integer.valueOf(pid))) {
            return 1;
        }
        if (((ArraySet) this.mScenePidArray.get(3)).contains(Integer.valueOf(pid))) {
            return 3;
        }
        if (((ArraySet) this.mScenePidArray.get(5)).contains(Integer.valueOf(pid))) {
            return 5;
        }
        return ((ArraySet) this.mScenePidArray.get(4)).contains(Integer.valueOf(pid)) ? 4 : -1;
    }

    private int getKeyBackgroupTypeByUidLocked(int uid) {
        if (this.mSceneUidArray.isEmpty()) {
            return -1;
        }
        if (((ArraySet) this.mSceneUidArray.get(2)).contains(Integer.valueOf(uid))) {
            return 2;
        }
        if (((ArraySet) this.mSceneUidArray.get(1)).contains(Integer.valueOf(uid))) {
            return 1;
        }
        if (((ArraySet) this.mSceneUidArray.get(3)).contains(Integer.valueOf(uid))) {
            return 3;
        }
        if (((ArraySet) this.mSceneUidArray.get(5)).contains(Integer.valueOf(uid))) {
            return 5;
        }
        return ((ArraySet) this.mSceneUidArray.get(4)).contains(Integer.valueOf(uid)) ? 4 : -1;
    }

    private int getKeyBackgroupTypeByPkgsLocked(List<String> pkgs) {
        if (this.mScenePkgArray.isEmpty()) {
            return -1;
        }
        for (String pkg : pkgs) {
            if (((ArraySet) this.mScenePkgArray.get(2)).contains(pkg)) {
                return 2;
            }
            if (((ArraySet) this.mScenePkgArray.get(1)).contains(pkg)) {
                return 1;
            }
            if (((ArraySet) this.mScenePkgArray.get(3)).contains(pkg)) {
                return 3;
            }
            if (((ArraySet) this.mScenePkgArray.get(5)).contains(pkg)) {
                return 5;
            }
            if (((ArraySet) this.mScenePkgArray.get(4)).contains(pkg)) {
                return 4;
            }
        }
        return -1;
    }

    private void handleSensorEvent(int uid, int sensor, boolean enable) {
        if (DEBUG) {
            AwareLog.i(TAG, "sensor:" + sensor + " enable:" + enable + " uid:" + uid);
        }
        synchronized (this) {
            SensorRecord se = (SensorRecord) this.mHistorySensorRecords.get(uid);
            if (enable) {
                if (se == null) {
                    this.mHistorySensorRecords.put(uid, new SensorRecord(uid, sensor));
                } else {
                    se.addSensor(sensor);
                }
            } else if (se != null) {
                if (se.hasSensor()) {
                    se.removeSensor(Integer.valueOf(sensor));
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0036, code:
            return;
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r7 = r11.mPGSdk.getSensorInfoByUid(r12, r13);
     */
    /* JADX WARNING: Missing block: B:16:0x003e, code:
            if (r7 == null) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:17:0x0040, code:
            r5 = r7.entrySet().iterator();
     */
    /* JADX WARNING: Missing block: B:19:0x004c, code:
            if (r5.hasNext() == false) goto L_0x0075;
     */
    /* JADX WARNING: Missing block: B:20:0x004e, code:
            r3 = (java.util.Map.Entry) r5.next();
            r6 = java.lang.Integer.parseInt((java.lang.String) r3.getKey());
            r0 = java.lang.Integer.parseInt((java.lang.String) r3.getValue());
            r4 = 1;
     */
    /* JADX WARNING: Missing block: B:21:0x0069, code:
            if (r4 > r0) goto L_0x0048;
     */
    /* JADX WARNING: Missing block: B:22:0x006b, code:
            handleSensorEvent(r13, r6, true);
     */
    /* JADX WARNING: Missing block: B:23:0x006f, code:
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:29:0x0077, code:
            if (DEBUG == false) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:30:0x0079, code:
            android.rms.iaware.AwareLog.d(TAG, "getSensorInfoByUid sensor handles " + r7);
     */
    /* JADX WARNING: Missing block: B:33:0x0095, code:
            android.rms.iaware.AwareLog.e(TAG, "integer parse error!");
     */
    /* JADX WARNING: Missing block: B:35:0x00a0, code:
            android.rms.iaware.AwareLog.e(TAG, "error, PG crash!");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    private void updateAppSensorState(boolean sensorStart, int uid, int pid) {
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
}
