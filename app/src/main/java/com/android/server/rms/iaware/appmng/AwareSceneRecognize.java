package com.android.server.rms.iaware.appmng;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.util.ArrayMap;
import com.android.internal.os.BackgroundThread;
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.data.content.AttrSegments.Builder;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AwareSceneRecognize {
    private static boolean DEBUG = false;
    private static final int MSG_INIT = 1;
    private static final int MSG_STOP_ACTIVITY = 104;
    private static final int MSG_STOP_FLING = 102;
    private static final int MSG_STOP_SCROLL = 101;
    private static final long REINIT_TIME = 2000;
    public static final int SCENE_RECONGNIZE_DEFAULT = 1;
    public static final int SCENE_RECONGNIZE_EVENT_BEGIN = 1;
    public static final int SCENE_RECONGNIZE_EVENT_END = 0;
    public static final int SCENE_RECONGNIZE_SLIPPING = 2;
    public static final int SCENE_RECONGNIZE_START_ACTIVITY = 8;
    public static final int SCENE_RECONGNIZE_START_APP = 4;
    private static final long START_TIME_OUT = 3000;
    private static final long STOP_SCROLL_DELAY = 100;
    private static final String TAG = "RMS.AwareSceneRecognize";
    private static AwareSceneRecognize mAwareSceneRecognize;
    private static boolean mEnabled;
    private final ArrayMap<IAwareSceneRecCallback, Integer> mCallbacks;
    private Handler mHandler;
    private AtomicBoolean mIsActivityStarting;
    private AtomicBoolean mIsAppStarting;
    private AtomicBoolean mIsFling;
    private AtomicBoolean mIsInitialized;
    private AtomicBoolean mIsScroll;
    private String mLastStartPkg;

    public interface IAwareSceneRecCallback {
        void onStateChanged(int i, int i2, String str);
    }

    private class SceneRecHandler extends Handler {
        public SceneRecHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (AwareSceneRecognize.DEBUG) {
                AwareLog.d(AwareSceneRecognize.TAG, "handleMessage message " + msg.what + ", isScroll=" + AwareSceneRecognize.this.mIsScroll.get() + ", isFling=" + AwareSceneRecognize.this.mIsFling.get());
            }
            switch (msg.what) {
                case AwareSceneRecognize.SCENE_RECONGNIZE_EVENT_BEGIN /*1*/:
                    AwareSceneRecognize.this.initialize();
                    break;
                case AwareSceneRecognize.MSG_STOP_SCROLL /*101*/:
                    if (!AwareSceneRecognize.this.mIsFling.get() && AwareSceneRecognize.this.mIsScroll.get()) {
                        AwareSceneRecognize.this.mIsScroll.set(false);
                        AwareSceneRecognize.this.notifyStateChange(AwareSceneRecognize.SCENE_RECONGNIZE_SLIPPING, AwareSceneRecognize.SCENE_RECONGNIZE_EVENT_END, null);
                        break;
                    }
                case AwareSceneRecognize.MSG_STOP_FLING /*102*/:
                    AwareSceneRecognize.this.mIsFling.set(false);
                    if (!AwareSceneRecognize.this.mIsScroll.get()) {
                        AwareSceneRecognize.this.notifyStateChange(AwareSceneRecognize.SCENE_RECONGNIZE_SLIPPING, AwareSceneRecognize.SCENE_RECONGNIZE_EVENT_END, null);
                        break;
                    }
                    break;
                case AwareSceneRecognize.MSG_STOP_ACTIVITY /*104*/:
                    AwareSceneRecognize.this.handleActivityStartingFinish();
                    break;
                case 15005:
                    Bundle data = msg.getData();
                    if (data != null) {
                        AwareSceneRecognize.this.handleStartActivity(data.getString(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY));
                        break;
                    }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.appmng.AwareSceneRecognize.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.appmng.AwareSceneRecognize.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.appmng.AwareSceneRecognize.<clinit>():void");
    }

    private AwareSceneRecognize() {
        this.mLastStartPkg = AppHibernateCst.INVALID_PKG;
        this.mIsInitialized = new AtomicBoolean(false);
        this.mIsScroll = new AtomicBoolean(false);
        this.mIsFling = new AtomicBoolean(false);
        this.mIsActivityStarting = new AtomicBoolean(false);
        this.mIsAppStarting = new AtomicBoolean(false);
        this.mCallbacks = new ArrayMap();
        this.mHandler = new SceneRecHandler(BackgroundThread.get().getLooper());
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            if (isUserUnlocked()) {
                this.mIsInitialized.set(true);
                return;
            }
            if (this.mHandler.hasMessages(SCENE_RECONGNIZE_EVENT_BEGIN)) {
                this.mHandler.removeMessages(SCENE_RECONGNIZE_EVENT_BEGIN);
            }
            this.mHandler.sendEmptyMessageDelayed(SCENE_RECONGNIZE_EVENT_BEGIN, REINIT_TIME);
            if (DEBUG) {
                AwareLog.d(TAG, "MultiTaskManagerService has not been started or User is locked.");
            }
        }
    }

    private boolean isUserUnlocked() {
        MultiTaskManagerService mtmService = MultiTaskManagerService.self();
        if (mtmService == null) {
            return false;
        }
        UserManager userManager = (UserManager) mtmService.context().getSystemService("user");
        if (userManager == null) {
            return false;
        }
        return userManager.isUserUnlocked();
    }

    private synchronized void deInitialize() {
        if (this.mIsInitialized.get()) {
            synchronized (this.mCallbacks) {
                this.mCallbacks.clear();
            }
            this.mIsInitialized.set(false);
        }
    }

    public static synchronized AwareSceneRecognize getInstance() {
        AwareSceneRecognize awareSceneRecognize;
        synchronized (AwareSceneRecognize.class) {
            if (mAwareSceneRecognize == null) {
                mAwareSceneRecognize = new AwareSceneRecognize();
            }
            awareSceneRecognize = mAwareSceneRecognize;
        }
        return awareSceneRecognize;
    }

    public void registerStateCallback(IAwareSceneRecCallback callback, int stateType) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                Integer states = (Integer) this.mCallbacks.get(callback);
                if (states == null) {
                    this.mCallbacks.put(callback, Integer.valueOf(stateType));
                } else {
                    this.mCallbacks.put(callback, Integer.valueOf(states.intValue() | stateType));
                }
            }
        }
    }

    public void unregisterStateCallback(IAwareSceneRecCallback callback) {
        if (callback != null) {
            synchronized (this.mCallbacks) {
                this.mCallbacks.remove(callback);
            }
        }
    }

    private void notifyStateChange(int sceneType, int eventType, String pkgName) {
        AwareLog.w(TAG, "SceneRec sceneType :" + sceneType + ", eventType:" + eventType + ", pkg=" + pkgName);
        synchronized (this.mCallbacks) {
            if (this.mCallbacks.isEmpty()) {
                return;
            }
            for (Entry<IAwareSceneRecCallback, Integer> m : this.mCallbacks.entrySet()) {
                IAwareSceneRecCallback callback = (IAwareSceneRecCallback) m.getKey();
                int state = ((Integer) m.getValue()).intValue();
                if (SCENE_RECONGNIZE_EVENT_BEGIN == state || (state & sceneType) != 0) {
                    callback.onStateChanged(sceneType, eventType, pkgName);
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
                    case HwGnssLogHandlerMsgID.PERMISSIONERR /*13*/:
                        handleStartScroll();
                        break;
                    case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
                        handleStopScroll();
                        break;
                    case HwGnssLogHandlerMsgID.ADDGEOFENCESTATUS /*15*/:
                        handleFling(bundleArgs.getInt("scroll_duration"));
                        break;
                    case HwSecDiagnoseConstant.BIT_SU /*16*/:
                        if (!bundleArgs.getBoolean("start_or_stop_app")) {
                            this.mHandler.sendEmptyMessage(MSG_STOP_ACTIVITY);
                            break;
                        }
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
            AwareLog.d(TAG, "AwareSceneRecognize feature disabled!");
        }
    }

    public void reportActivityStart(CollectData data) {
        if (mEnabled) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            if (data != null) {
                String eventData = data.getData();
                Builder builder = new Builder();
                builder.addCollectData(eventData);
                AttrSegments attrSegments = builder.build();
                if (attrSegments.isValid()) {
                    ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
                    if (appInfo == null) {
                        AwareLog.d(TAG, "appInfo is NULL");
                        return;
                    }
                    int eventId = attrSegments.getEvent().intValue();
                    if (15005 == eventId) {
                        String pkgName = (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY);
                        Message msg = this.mHandler.obtainMessage();
                        msg.what = eventId;
                        Bundle bundle = new Bundle();
                        bundle.putString(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, pkgName);
                        msg.setData(bundle);
                        this.mHandler.sendMessage(msg);
                    }
                    return;
                }
                AwareLog.e(TAG, "Invalid collectData, or event");
                return;
            }
            return;
        }
        if (DEBUG) {
            AwareLog.d(TAG, "AwareSceneRecognize feature disabled!");
        }
    }

    private void sendMessage(int what, long delay) {
        this.mHandler.sendEmptyMessageDelayed(what, delay);
    }

    private void handleStartScroll() {
        if (!this.mIsScroll.get()) {
            this.mIsScroll.set(true);
            if (!this.mIsFling.get()) {
                notifyStateChange(SCENE_RECONGNIZE_SLIPPING, SCENE_RECONGNIZE_EVENT_BEGIN, null);
            }
            this.mIsFling.set(false);
            this.mHandler.removeMessages(MSG_STOP_FLING);
        }
    }

    private void handleStopScroll() {
        sendMessage(MSG_STOP_SCROLL, STOP_SCROLL_DELAY);
    }

    private void handleFling(int duration) {
        this.mIsFling.set(true);
        if (this.mIsScroll.get()) {
            this.mIsScroll.set(false);
        }
        sendMessage(MSG_STOP_FLING, (long) duration);
    }

    private void handleStartActivity(String pkgName) {
        if (pkgName == null || AppHibernateCst.INVALID_PKG.equals(pkgName.trim())) {
            if (DEBUG) {
                AwareLog.d(TAG, "current start pkgName is null or empty");
            }
            return;
        }
        AwareAppAssociate assc = AwareAppAssociate.getInstance();
        if (assc == null) {
            AwareLog.d(TAG, "the aware assoc is not started");
            return;
        }
        List<String> homePkg = assc.getDefaultHomePackages();
        if (this.mLastStartPkg.equals(pkgName) && homePkg.contains(pkgName)) {
            if (DEBUG) {
                AwareLog.d(TAG, "current is home, no need restart home process, so filter it");
            }
            return;
        }
        this.mIsActivityStarting.set(true);
        if (this.mLastStartPkg.equals(pkgName)) {
            notifyStateChange(SCENE_RECONGNIZE_START_ACTIVITY, SCENE_RECONGNIZE_EVENT_BEGIN, pkgName);
        } else {
            notifyStateChange(SCENE_RECONGNIZE_START_APP, SCENE_RECONGNIZE_EVENT_BEGIN, pkgName);
            this.mIsAppStarting.set(true);
        }
        if (this.mHandler.hasMessages(MSG_STOP_ACTIVITY)) {
            this.mHandler.removeMessages(MSG_STOP_ACTIVITY);
        }
        this.mHandler.sendEmptyMessageDelayed(MSG_STOP_ACTIVITY, START_TIME_OUT);
        this.mLastStartPkg = pkgName;
    }

    private void handleActivityStartingFinish() {
        if (this.mIsActivityStarting.get()) {
            notifyStateChange(this.mIsAppStarting.get() ? SCENE_RECONGNIZE_START_APP : SCENE_RECONGNIZE_START_ACTIVITY, SCENE_RECONGNIZE_EVENT_END, null);
            this.mIsActivityStarting.set(false);
        }
        this.mIsAppStarting.set(false);
        if (this.mHandler.hasMessages(MSG_STOP_ACTIVITY)) {
            this.mHandler.removeMessages(MSG_STOP_ACTIVITY);
        }
    }

    public static void enable() {
        mEnabled = true;
        if (mAwareSceneRecognize != null) {
            mAwareSceneRecognize.initialize();
        }
    }

    public static void disable() {
        mEnabled = false;
        if (mAwareSceneRecognize != null) {
            mAwareSceneRecognize.deInitialize();
        }
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }
}
