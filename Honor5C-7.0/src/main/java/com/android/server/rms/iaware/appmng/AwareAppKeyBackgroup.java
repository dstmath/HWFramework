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
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.display.Utils;
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.pgmng.plug.PGSdk.Sink;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppKeyBackgroup {
    private static final String[] APPTYPESTRING = null;
    private static boolean DEBUG = false;
    private static final long DECAY_TIME = 5000;
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    private static final int MSG_APP_PROCESSDIED = 2;
    private static final int MSG_PGSDK_INIT = 3;
    private static final int MSG_REMOVE_DECAY_STATE = 1;
    private static final long PGSDK_REINIT_TIME = 2000;
    private static final int PID_INVALID = -1;
    private static final String[] STATESTRING = null;
    public static final int STATE_ALL = 100;
    public static final int STATE_AUDIO_IN = 1;
    public static final int STATE_AUDIO_OUT = 2;
    public static final int STATE_GPS = 3;
    public static final int STATE_IMEMAIL = 99;
    public static final int STATE_KEY_BG = 0;
    public static final int STATE_KEY_BG_INVALID = -1;
    public static final int STATE_SENSOR = 4;
    private static final int STATE_SIZE = 0;
    public static final int STATE_UPLOAD_DL = 5;
    private static final String TAG = "AwareAppKeyBackgroup";
    private static final int TYPE_SIZE = 0;
    private static AwareAppKeyBackgroup sInstance;
    private AppKeyHandler mAppKeyHandler;
    private final ArrayMap<IAwareStateCallback, ArraySet<Integer>> mCallbacks;
    private Context mContext;
    private final SparseArray<SensorRecord> mHistorySensorRecords;
    private HwActivityManagerService mHwAMS;
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
            if (msg.what == AwareAppKeyBackgroup.STATE_GPS) {
                AwareAppKeyBackgroup.this.doInitialize();
            } else if (AwareAppKeyBackgroup.this.mIsInitialized.get()) {
                DecayInfo decayInfo = msg.obj instanceof DecayInfo ? (DecayInfo) msg.obj : null;
                switch (msg.what) {
                    case AwareAppKeyBackgroup.STATE_AUDIO_IN /*1*/:
                        if (decayInfo != null) {
                            if (AwareAppKeyBackgroup.DEBUG) {
                                AwareLog.d(AwareAppKeyBackgroup.TAG, "Update state " + decayInfo.getStateType() + " uid : " + decayInfo.getUid());
                            }
                            AwareAppKeyBackgroup.this.updateSceneState(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                            AwareAppKeyBackgroup.this.removeDecayInfo(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                            break;
                        }
                    case AwareAppKeyBackgroup.STATE_AUDIO_OUT /*2*/:
                        int pid = msg.arg1;
                        int uid = msg.arg2;
                        AwareAppKeyBackgroup.this.updateSceneArrayProcessDied(uid, pid);
                        if (AwareAppKeyBackgroup.this.getStateEventDecayInfosSize() > 0) {
                            for (int i = AwareAppKeyBackgroup.STATE_AUDIO_IN; i < AwareAppKeyBackgroup.STATE_SIZE; i += AwareAppKeyBackgroup.STATE_AUDIO_IN) {
                                int i2;
                                AwareAppKeyBackgroup awareAppKeyBackgroup = AwareAppKeyBackgroup.this;
                                if (i == AwareAppKeyBackgroup.STATE_AUDIO_OUT) {
                                    i2 = pid;
                                } else {
                                    i2 = AwareAppKeyBackgroup.STATE_SIZE;
                                }
                                awareAppKeyBackgroup.removeDecayForProcessDied(i, AwareAppKeyBackgroup.STATE_AUDIO_OUT, i2, null, uid);
                            }
                            break;
                        }
                        break;
                }
            }
        }
    }

    class AwareABGProcessObserver extends Stub {
        AwareABGProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
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
                    observerMsg.what = AwareAppKeyBackgroup.STATE_AUDIO_OUT;
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
        private final ArrayMap<Integer, Integer> mHandles;
        private int mUid;

        public SensorRecord(int uid, int handle) {
            this.mHandles = new ArrayMap();
            this.mUid = uid;
            addSensor(handle);
        }

        public boolean hasSensor() {
            return this.mHandles.size() > 0;
        }

        public void addSensor(int handle) {
            if (!hasSensor()) {
                AwareAppKeyBackgroup.this.updateAppSensorState(true, this.mUid, AwareAppKeyBackgroup.STATE_SIZE);
            }
            Integer count = (Integer) this.mHandles.get(Integer.valueOf(handle));
            if (count == null) {
                this.mHandles.put(Integer.valueOf(handle), Integer.valueOf(AwareAppKeyBackgroup.STATE_AUDIO_IN));
            } else {
                this.mHandles.put(Integer.valueOf(handle), Integer.valueOf(count.intValue() + AwareAppKeyBackgroup.STATE_AUDIO_IN));
            }
            if (AwareAppKeyBackgroup.DEBUG) {
                AwareLog.i(AwareAppKeyBackgroup.TAG, "addSensor,mHandles:" + this.mHandles);
            }
        }

        public void removeSensor(Integer handle) {
            Integer count = (Integer) this.mHandles.get(handle);
            if (count != null) {
                int value = count.intValue() + AwareAppKeyBackgroup.STATE_KEY_BG_INVALID;
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
                AwareAppKeyBackgroup.this.updateAppSensorState(false, this.mUid, AwareAppKeyBackgroup.STATE_SIZE);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.<clinit>():void");
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
        return pid == Process.myPid() || uid == 0 || uid == IOTController.TYPE_MASTER;
    }

    private int getAppTypeFromHabit(int uid) {
        if (this.mContext == null) {
            return STATE_KEY_BG_INVALID;
        }
        if (this.mPM == null) {
            this.mPM = this.mContext.getPackageManager();
            if (this.mPM == null) {
                AwareLog.e(TAG, "Failed to get PackageManager");
                return STATE_KEY_BG_INVALID;
            }
        }
        String[] pkgNames = this.mPM.getPackagesForUid(uid);
        if (pkgNames == null) {
            AwareLog.e(TAG, "Failed to get package name for uid: " + uid);
            return STATE_KEY_BG_INVALID;
        }
        int length = pkgNames.length;
        for (int i = STATE_SIZE; i < length; i += STATE_AUDIO_IN) {
            int type = IAwareHabitUtils.getAppTypeForAppMng(pkgNames[i]);
            if (type != STATE_KEY_BG_INVALID) {
                return type;
            }
        }
        return STATE_KEY_BG_INVALID;
    }

    private boolean isNaviOrSportApp(int uid) {
        int type = getAppTypeFromHabit(uid);
        if (DEBUG) {
            AwareLog.d(TAG, "getAppTypeFromHabit uid " + uid + " type : " + type);
        }
        switch (type) {
            case STATE_SIZE /*0*/:
            case STATE_AUDIO_IN /*1*/:
            case STATE_SENSOR /*4*/:
            case STATE_UPLOAD_DL /*5*/:
            case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
            case LifeCycleStateMachine.TIME_OUT /*7*/:
            case ByteUtil.LONG_SIZE /*8*/:
            case HwGnssLogHandlerMsgID.UPDATESVSTATUS /*9*/:
            case AwareAppMngDFX.APPLICATION_STARTTYPE_COLD /*10*/:
            case AwareAppMngDFX.APPLICATION_STARTTYPE_TOTAL /*11*/:
            case Utils.MAXINUM_TEMPERATURE /*255*/:
                return false;
            case STATE_AUDIO_OUT /*2*/:
            case STATE_GPS /*3*/:
                return true;
            default:
                return true;
        }
    }

    private boolean shouldFilter(int stateType, int uid) {
        return (stateType == STATE_GPS || stateType == STATE_SENSOR) && !isNaviOrSportApp(uid);
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

    private AwareAppKeyBackgroup() {
        this.mIsInitialized = new AtomicBoolean(false);
        this.mLastSetting = new AtomicBoolean(false);
        this.mIsInitializing = new AtomicBoolean(false);
        this.mPGSdk = null;
        this.mContext = null;
        this.mHwAMS = null;
        this.mPM = null;
        this.mProcessObserver = new AwareABGProcessObserver();
        this.mScenePidArray = new ArrayList();
        this.mSceneUidArray = new ArrayList();
        this.mScenePkgArray = new ArrayList();
        this.mKeyBackgroupPids = new ArraySet();
        this.mKeyBackgroupUids = new ArraySet();
        this.mKeyBackgroupPkgs = new ArraySet();
        this.mStateEventDecayInfos = new ArrayList();
        this.mCallbacks = new ArrayMap();
        this.mHistorySensorRecords = new SparseArray();
        this.mStateRecognitionListener = new Sink() {
            public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
                boolean z = true;
                if (!AwareAppKeyBackgroup.this.mIsInitialized.get() || !AwareAppKeyBackgroup.this.checkCallingPermission() || pid == Process.myPid() || AwareAppKeyBackgroup.this.shouldFilter(stateType, uid)) {
                    return;
                }
                if (stateType == AwareAppKeyBackgroup.STATE_SENSOR) {
                    AwareAppKeyBackgroup awareAppKeyBackgroup = AwareAppKeyBackgroup.this;
                    if (eventType != AwareAppKeyBackgroup.STATE_AUDIO_IN) {
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
                if (states != null && (states.contains(Integer.valueOf(stateType)) || states.contains(Integer.valueOf(STATE_ALL)))) {
                    callback.onStateChanged(stateType, eventType, pid == STATE_KEY_BG_INVALID ? STATE_SIZE : pid, uid);
                }
            }
        }
    }

    private void initialize(Context context) {
        this.mLastSetting.set(true);
        if (!this.mIsInitialized.get() && !this.mIsInitializing.get()) {
            this.mContext = context;
            registerProcessObserver();
            if (this.mAppKeyHandler.hasMessages(STATE_GPS)) {
                this.mAppKeyHandler.removeMessages(STATE_GPS);
            }
            this.mAppKeyHandler.sendEmptyMessage(STATE_GPS);
        }
    }

    private void doInitialize() {
        this.mIsInitializing.set(true);
        synchronized (this) {
            if (this.mScenePidArray.isEmpty()) {
                for (int i = STATE_SIZE; i < STATE_SIZE; i += STATE_AUDIO_IN) {
                    this.mScenePidArray.add(new ArraySet());
                    this.mSceneUidArray.add(new ArraySet());
                    this.mScenePkgArray.add(new ArraySet());
                }
            }
            if (!ensureInitialize()) {
                if (this.mAppKeyHandler.hasMessages(STATE_GPS)) {
                    this.mAppKeyHandler.removeMessages(STATE_GPS);
                }
                this.mAppKeyHandler.sendEmptyMessageDelayed(STATE_GPS, PGSDK_REINIT_TIME);
            }
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
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, STATE_AUDIO_IN);
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, STATE_AUDIO_OUT);
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, STATE_GPS);
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, STATE_SENSOR);
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, STATE_UPLOAD_DL);
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
        this.mLastSetting.set(false);
        if (!this.mIsInitialized.get()) {
            this.mAppKeyHandler.removeMessages(STATE_GPS);
        } else if (this.mPGSdk != null) {
            try {
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, STATE_AUDIO_IN);
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, STATE_AUDIO_OUT);
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, STATE_GPS);
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, STATE_SENSOR);
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, STATE_UPLOAD_DL);
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
                this.mAppKeyHandler.removeCallbacksAndMessages(null);
                this.mIsInitialized.set(false);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "PG Exception e: deinitialize pgsdk error!");
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
            for (ProcessInfo procInfo : procs) {
                if (procInfo != null) {
                    int pid = procInfo.mPid;
                    int uid = procInfo.mUid;
                    ArrayList<String> packages = procInfo.mPackageName;
                    for (int i = STATE_AUDIO_IN; i <= STATE_UPLOAD_DL; i += STATE_AUDIO_IN) {
                        if (!shouldFilter(i, uid)) {
                            boolean state;
                            if (i <= STATE_AUDIO_OUT) {
                                state = false;
                                try {
                                    state = this.mPGSdk.checkStateByPid(context, pid, i);
                                } catch (RemoteException e) {
                                    AwareLog.e(TAG, "checkStateByPid occur exception.");
                                }
                                if (state) {
                                    updateSceneState(i, STATE_AUDIO_IN, pid, null, uid);
                                }
                            } else if (i == STATE_SENSOR) {
                                initAppSensorState(context, uid);
                            } else {
                                for (String pkg : packages) {
                                    state = false;
                                    try {
                                        state = this.mPGSdk.checkStateByPkg(context, pkg, i);
                                    } catch (RemoteException e2) {
                                        AwareLog.e(TAG, "checkStateByPkg occur exception.");
                                    }
                                    if (state) {
                                        updateSceneState(i, STATE_AUDIO_IN, STATE_SIZE, null, uid);
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
            sendDecayMesssage(STATE_AUDIO_IN, addDecayInfo(stateType, eventType, pid, pkg, uid), DECAY_TIME);
            return true;
        }
    }

    private boolean isNeedDecay(int stateType, int eventType) {
        return stateType == STATE_AUDIO_OUT && eventType == STATE_AUDIO_OUT;
    }

    private void checkStateChangedDecay(int stateType, int eventType, int pid, String pkg, int uid) {
        if (isNeedCheckDecay(stateType, eventType)) {
            DecayInfo decayInfo = removeDecayInfo(stateType, STATE_AUDIO_OUT, pid, pkg, uid);
            if (decayInfo != null) {
                if (DEBUG) {
                    AwareLog.d(TAG, "checkStateChangedDecay start has message 1 ? " + this.mAppKeyHandler.hasMessages(STATE_AUDIO_IN, decayInfo) + " decayinfo" + decayInfo + " size " + getStateEventDecayInfosSize());
                }
                this.mAppKeyHandler.removeMessages(STATE_AUDIO_IN, decayInfo);
            }
        }
    }

    private boolean isNeedCheckDecay(int stateType, int eventType) {
        return stateType == STATE_AUDIO_OUT && eventType == STATE_AUDIO_IN && getStateEventDecayInfosSize() > 0;
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
        for (ProcessInfo info : procList) {
            if (info != null && uid == info.mUid) {
                procs.add(info);
            }
        }
        return procs;
    }

    private boolean isAppAlive(Context cxt, int uid) {
        Map<Integer, AwareProcessBaseInfo> baseInfos = null;
        if (cxt == null) {
            return false;
        }
        if (this.mHwAMS != null) {
            baseInfos = this.mHwAMS.getAllProcessBaseInfo();
        }
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

    private void removeDecayForProcessDied(int stateType, int eventType, int pid, String pkg, int uid) {
        if (!isNeedDecay(stateType, eventType) || !existDecayInfo(stateType, eventType, pid, pkg, uid)) {
            return;
        }
        if (pid != 0 || !isAppAlive(this.mContext, uid)) {
            DecayInfo decayInfo = removeDecayInfo(stateType, eventType, pid, pkg, uid);
            if (decayInfo != null) {
                updateSceneState(decayInfo.getStateType(), decayInfo.getEventType(), decayInfo.getPid(), decayInfo.getPkg(), decayInfo.getUid());
                this.mAppKeyHandler.removeMessages(STATE_AUDIO_IN, decayInfo);
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

    public int getKeyBackgroupTypeInternal(int pid, int uid, List<String> pkgs) {
        if (!this.mIsInitialized.get()) {
            return STATE_KEY_BG_INVALID;
        }
        synchronized (this) {
            int keyBackgroupTypeByPidLocked;
            if (this.mKeyBackgroupPids.contains(Integer.valueOf(pid))) {
                keyBackgroupTypeByPidLocked = getKeyBackgroupTypeByPidLocked(pid);
                return keyBackgroupTypeByPidLocked;
            } else if (this.mKeyBackgroupUids.contains(Integer.valueOf(uid))) {
                keyBackgroupTypeByPidLocked = getKeyBackgroupTypeByUidLocked(uid);
                return keyBackgroupTypeByPidLocked;
            } else if (pkgs == null) {
                return STATE_KEY_BG_INVALID;
            } else {
                keyBackgroupTypeByPidLocked = getKeyBackgroupTypeByPkgsLocked(pkgs);
                return keyBackgroupTypeByPidLocked;
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

    public boolean checkKeyBackgroupByState(int state, int pid, int uid) {
        if (!this.mIsInitialized.get() || state < STATE_AUDIO_IN || state >= STATE_SIZE) {
            return false;
        }
        synchronized (this) {
            if (((ArraySet) this.mSceneUidArray.get(state)).contains(Integer.valueOf(uid))) {
                return true;
            } else if (((ArraySet) this.mScenePidArray.get(state)).contains(Integer.valueOf(pid))) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void updateSceneArrayProcessDied(int uid, int pid) {
        synchronized (this) {
            boolean appAlive = isAppAlive(this.mContext, uid);
            for (int i = STATE_AUDIO_IN; i < STATE_SIZE; i += STATE_AUDIO_IN) {
                if (((ArraySet) this.mScenePidArray.get(i)).contains(Integer.valueOf(pid))) {
                    ((ArraySet) this.mSceneUidArray.get(i)).remove(Integer.valueOf(uid));
                    ((ArraySet) this.mScenePidArray.get(i)).remove(Integer.valueOf(pid));
                    this.mKeyBackgroupUids.remove(Integer.valueOf(pid));
                } else if (!(uid == 0 || appAlive)) {
                    ((ArraySet) this.mSceneUidArray.get(i)).remove(Integer.valueOf(uid));
                }
            }
            updateKbgUidsArrayLocked();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateScenePidArrayForInvalidPid(int uid) {
        if (DEBUG) {
            AwareLog.d(TAG, "updateScenePidArrayForInvalidPid uid " + uid);
        }
        List<ProcessInfo> procs = getProcessesByUid(uid);
        if (procs != null && !procs.isEmpty()) {
            loop0:
            for (ProcessInfo info : procs) {
                if (info != null) {
                    if (DEBUG) {
                        AwareLog.d(TAG, "updateScenePidArrayForInvalidPid pid " + info.mPid);
                    }
                    synchronized (this) {
                        int i = STATE_AUDIO_IN;
                        while (true) {
                            if (i >= STATE_SIZE) {
                                break;
                            }
                            ((ArraySet) this.mScenePidArray.get(i)).remove(Integer.valueOf(info.mPid));
                            i += STATE_AUDIO_IN;
                        }
                    }
                }
            }
        }
    }

    private void updateKbgUidsArrayLocked() {
        this.mKeyBackgroupUids.clear();
        for (int i = STATE_SIZE; i < STATE_SIZE; i += STATE_AUDIO_IN) {
            this.mKeyBackgroupUids.addAll((ArraySet) this.mSceneUidArray.get(i));
        }
    }

    private void updateSceneArrayLocked(int stateType, int eventType, int pid, String pkg, int uid) {
        if (eventType == STATE_AUDIO_IN) {
            if (pid != 0) {
                ((ArraySet) this.mScenePidArray.get(stateType)).add(Integer.valueOf(pid));
            }
            if (uid != 0) {
                ((ArraySet) this.mSceneUidArray.get(stateType)).add(Integer.valueOf(uid));
            }
            if (pkg != null && !pkg.isEmpty()) {
                ((ArraySet) this.mScenePkgArray.get(stateType)).add(pkg);
            }
        } else if (eventType == STATE_AUDIO_OUT) {
            if (pid != 0) {
                ((ArraySet) this.mScenePidArray.get(stateType)).remove(Integer.valueOf(pid));
            }
            if (uid != 0) {
                ((ArraySet) this.mSceneUidArray.get(stateType)).remove(Integer.valueOf(uid));
            }
            if (pkg != null && !pkg.isEmpty()) {
                ((ArraySet) this.mScenePkgArray.get(stateType)).remove(pkg);
            }
        }
    }

    private void updateSceneState(int stateType, int eventType, int pid, String pkg, int uid) {
        if (DEBUG) {
            AwareLog.d(TAG, "state type: " + stateType + " eventType:" + eventType + " pid:" + pid + " uid:" + uid + " pkg:" + pkg);
        }
        if (stateType >= STATE_AUDIO_IN && stateType < STATE_SIZE) {
            int realpid = pid;
            if (pid == STATE_KEY_BG_INVALID) {
                updateScenePidArrayForInvalidPid(uid);
                realpid = STATE_SIZE;
            }
            notifyStateChange(stateType, eventType, realpid, uid);
            synchronized (this) {
                updateSceneArrayLocked(stateType, eventType, realpid, pkg, uid);
                if (eventType == STATE_AUDIO_IN && !this.mKeyBackgroupUids.contains(Integer.valueOf(uid))) {
                    notifyStateChange(STATE_SIZE, eventType, realpid, uid);
                }
                this.mKeyBackgroupPids.clear();
                this.mKeyBackgroupUids.clear();
                this.mKeyBackgroupPkgs.clear();
                for (int i = STATE_SIZE; i < STATE_SIZE; i += STATE_AUDIO_IN) {
                    this.mKeyBackgroupPids.addAll((ArraySet) this.mScenePidArray.get(i));
                    this.mKeyBackgroupUids.addAll((ArraySet) this.mSceneUidArray.get(i));
                    this.mKeyBackgroupPkgs.addAll((ArraySet) this.mScenePkgArray.get(i));
                }
                if (eventType == STATE_AUDIO_OUT) {
                    if (!this.mKeyBackgroupUids.contains(Integer.valueOf(uid))) {
                        notifyStateChange(STATE_SIZE, eventType, realpid, uid);
                    }
                }
                if (DEBUG) {
                    AwareLog.d(TAG, "stateChanged " + stateToString(stateType) + " mPids:" + this.mScenePidArray.get(stateType));
                    AwareLog.d(TAG, "stateChanged " + stateToString(stateType) + " mUids:" + this.mSceneUidArray.get(stateType));
                    AwareLog.d(TAG, "stateChanged " + stateToString(stateType) + " mPkgs:" + this.mScenePkgArray.get(stateType));
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dump(PrintWriter pw) {
        if (pw != null) {
            if (this.mIsInitialized.get()) {
                pw.println("dump Important State Apps start --------");
                synchronized (this) {
                    int i = STATE_AUDIO_IN;
                    while (true) {
                        if (i < STATE_SIZE) {
                            pw.println("State[" + stateToString(i) + "] Pids:" + this.mScenePidArray.get(i));
                            pw.println("State[" + stateToString(i) + "] Uids:" + this.mSceneUidArray.get(i));
                            pw.println("State[" + stateToString(i) + "] Pkgs:" + this.mScenePkgArray.get(i));
                            i += STATE_AUDIO_IN;
                        }
                    }
                }
                pw.println("dump Important State Apps end-----------");
                return;
            }
            pw.println("KeyBackGroup feature not enabled.");
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
        if (pw != null && pkg != null) {
            if (this.mPGSdk == null || !this.mIsInitialized.get()) {
                pw.println("KeyBackGroup feature not enabled.");
                return;
            }
            int type = STATE_SIZE;
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
        if (((ArraySet) this.mScenePidArray.get(STATE_AUDIO_OUT)).contains(Integer.valueOf(pid))) {
            return STATE_AUDIO_OUT;
        }
        if (((ArraySet) this.mScenePidArray.get(STATE_AUDIO_IN)).contains(Integer.valueOf(pid))) {
            return STATE_AUDIO_IN;
        }
        if (((ArraySet) this.mScenePidArray.get(STATE_GPS)).contains(Integer.valueOf(pid))) {
            return STATE_GPS;
        }
        if (((ArraySet) this.mScenePidArray.get(STATE_UPLOAD_DL)).contains(Integer.valueOf(pid))) {
            return STATE_UPLOAD_DL;
        }
        if (((ArraySet) this.mScenePidArray.get(STATE_SENSOR)).contains(Integer.valueOf(pid))) {
            return STATE_SENSOR;
        }
        return STATE_KEY_BG_INVALID;
    }

    private int getKeyBackgroupTypeByUidLocked(int uid) {
        if (((ArraySet) this.mSceneUidArray.get(STATE_AUDIO_OUT)).contains(Integer.valueOf(uid))) {
            return STATE_AUDIO_OUT;
        }
        if (((ArraySet) this.mSceneUidArray.get(STATE_AUDIO_IN)).contains(Integer.valueOf(uid))) {
            return STATE_AUDIO_IN;
        }
        if (((ArraySet) this.mSceneUidArray.get(STATE_GPS)).contains(Integer.valueOf(uid))) {
            return STATE_GPS;
        }
        if (((ArraySet) this.mSceneUidArray.get(STATE_UPLOAD_DL)).contains(Integer.valueOf(uid))) {
            return STATE_UPLOAD_DL;
        }
        if (((ArraySet) this.mSceneUidArray.get(STATE_SENSOR)).contains(Integer.valueOf(uid))) {
            return STATE_SENSOR;
        }
        return STATE_KEY_BG_INVALID;
    }

    private int getKeyBackgroupTypeByPkgsLocked(List<String> pkgs) {
        for (String pkg : pkgs) {
            if (((ArraySet) this.mScenePkgArray.get(STATE_AUDIO_OUT)).contains(pkg)) {
                return STATE_AUDIO_OUT;
            }
            if (((ArraySet) this.mScenePkgArray.get(STATE_AUDIO_IN)).contains(pkg)) {
                return STATE_AUDIO_IN;
            }
            if (((ArraySet) this.mScenePkgArray.get(STATE_GPS)).contains(pkg)) {
                return STATE_GPS;
            }
            if (((ArraySet) this.mScenePkgArray.get(STATE_UPLOAD_DL)).contains(pkg)) {
                return STATE_UPLOAD_DL;
            }
            if (((ArraySet) this.mScenePkgArray.get(STATE_SENSOR)).contains(pkg)) {
                return STATE_SENSOR;
            }
        }
        return STATE_KEY_BG_INVALID;
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
                return;
            }
            try {
                Map<String, String> sensorMap = this.mPGSdk.getSensorInfoByUid(context, uid);
                if (sensorMap != null) {
                    for (Entry entry : sensorMap.entrySet()) {
                        int sensor = Integer.parseInt((String) entry.getKey());
                        int count = Integer.parseInt((String) entry.getValue());
                        for (int i = STATE_AUDIO_IN; i <= count; i += STATE_AUDIO_IN) {
                            handleSensorEvent(uid, sensor, true);
                        }
                    }
                    if (DEBUG) {
                        AwareLog.d(TAG, "getSensorInfoByUid sensor handles " + sensorMap);
                    }
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "error, PG crash!");
            } catch (NumberFormatException e2) {
                AwareLog.e(TAG, "integer parse error!");
            }
        }
    }

    private void updateAppSensorState(boolean sensorStart, int uid, int pid) {
        if (DEBUG) {
            AwareLog.i(TAG, "updateAppSensorState :" + uid + " pid " + pid);
        }
        updateSceneState(STATE_SENSOR, sensorStart ? STATE_AUDIO_IN : STATE_AUDIO_OUT, pid, null, uid);
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
