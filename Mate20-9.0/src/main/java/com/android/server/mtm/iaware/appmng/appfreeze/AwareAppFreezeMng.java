package com.android.server.mtm.iaware.appmng.appfreeze;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppFreezeMng {
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    private static final int DEFAULT_DURATION = 1500;
    private static final String KEY_BUNDLE_PACKAGENAME = "pkgName";
    private static final String KEY_BUNDLE_UID = "uid";
    private static final long MILLI_TO_NANO = 1000000;
    private static final int MIN_FREEZE_INTERVAL = 4500;
    private static final int MSG_ACTIVITY_STARTING = 2;
    private static final int MSG_APP_SLIPPING = 9;
    private static final int MSG_APP_SLIP_END = 10;
    private static final int MSG_CAMERA_SHOT = 4;
    private static final int MSG_FG_ACTIVITIES_CHANGED = 6;
    private static final int MSG_FROZEN_TIMEOUT = 3;
    private static final int MSG_GALLERY_SCALE = 11;
    private static final int MSG_INIT = 1;
    private static final int MSG_PROXIMITY_SCREEN_OFF = 7;
    private static final int MSG_SKIPPED_FRAME = 8;
    private static final String PACKAGE_CAMERA = "com.huawei.camera";
    private static final String PACKAGE_GALLERY = "com.android.gallery3d";
    private static final String PACKAGE_SYSTEMUI = "com.android.systemui";
    private static final String PROPERTIES_CONTROL_ENABLE = "persist.sys.control_evil";
    private static final String PROPERTIES_FREEZE_DELAY = "persist.sys.fast_h_delay";
    private static final String PROPERTIES_FREEZE_INVERVAL = "persist.sys.fast_h_duration";
    private static final String PROPERTIES_IAWARE_FREEZE_DELAY = "persist.sys.iaware_fast_h_delay";
    private static final String REASON_GALLERY_SCALE = "gallery scale";
    private static final String REASON_PROXIMITY_SCREEN_OFF = "proximity sceenoff";
    private static final String REASON_SKIPPED_FRAME = "skippedframe";
    private static final String REASON_START_ACTIVITY = "start activity";
    private static final String REASON_START_CAMERA = "start camera";
    private static final String REASON_START_CAMERA_SHOT = "camera freeze";
    private static final String REASON_TIMEOUT = "time out";
    private static final String TAG = "mtm.AwareAppFreezeMng";
    private static AwareAppFreezeMng mAwareAppFreezeMng = null;
    private static boolean mIsEvilEnable = true;
    private AtomicBoolean isFreeze;
    /* access modifiers changed from: private */
    public AtomicBoolean isSilde;
    private long lastFreezeTime;
    /* access modifiers changed from: private */
    public AwareNativeFreezeManager mAwareNativeFreezeManager;
    private AwareSceneRecognizeCallback mAwareSceneRecognizeCallback;
    /* access modifiers changed from: private */
    public int mCameraDelay;
    /* access modifiers changed from: private */
    public String mCurPkgName;
    /* access modifiers changed from: private */
    public int mDefaultDelay;
    /* access modifiers changed from: private */
    public boolean mEnabled;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private long mInterval;
    private AtomicBoolean mIsInitialized;
    private AtomicBoolean mIsScreenOn;
    private AtomicBoolean mIsStatusBarRevealed;
    private IProcessObserver mProcessObserver;

    /* renamed from: com.android.server.mtm.iaware.appmng.appfreeze.AwareAppFreezeMng$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_INSTALLER_MANAGER.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    private class AwareAppFreezeMngHanlder extends Handler {
        private AwareAppFreezeMngHanlder() {
        }

        public void handleMessage(Message msg) {
            if (!AwareAppFreezeMng.this.mEnabled) {
                if (AwareAppFreezeMng.DEBUG) {
                    AwareLog.d(AwareAppFreezeMng.TAG, "AwareAppFreezeMng feature disabled!");
                }
                return;
            }
            if (AwareAppFreezeMng.DEBUG) {
                AwareLog.d(AwareAppFreezeMng.TAG, "handleMessage message " + msg.what);
            }
            switch (msg.what) {
                case 1:
                    AwareAppFreezeMng.this.initialize();
                    break;
                case 2:
                    String unused = AwareAppFreezeMng.this.mCurPkgName = msg.getData().getString("pkgName");
                    if (!AwareAppFreezeMng.this.isCamera()) {
                        AwareAppFreezeMng.this.handleToFreeze(AwareAppFreezeMng.this.mDefaultDelay, AwareAppFreezeMng.REASON_START_ACTIVITY);
                        break;
                    } else {
                        AwareAppFreezeMng.this.handleToFreeze(AwareAppFreezeMng.this.mCameraDelay, AwareAppFreezeMng.REASON_START_CAMERA);
                        break;
                    }
                case 3:
                    AwareAppFreezeMng.this.handleToUnFreeze(AwareAppFreezeMng.REASON_TIMEOUT);
                    break;
                case 4:
                    AwareAppFreezeMng.this.handleToFreeze(AwareAppFreezeMng.this.mDefaultDelay, AwareAppFreezeMng.REASON_START_CAMERA_SHOT);
                    break;
                case 6:
                    String unused2 = AwareAppFreezeMng.this.mCurPkgName = InnerUtils.getPackageNameByUid(msg.getData().getInt("uid"));
                    break;
                case 7:
                    AwareAppFreezeMng.this.handleToFreeze(AwareAppFreezeMng.this.mDefaultDelay, AwareAppFreezeMng.REASON_PROXIMITY_SCREEN_OFF);
                    break;
                case 8:
                    AwareAppFreezeMng.this.handleSkippedFrameFreeze(AwareAppFreezeMng.this.mDefaultDelay, AwareAppFreezeMng.REASON_SKIPPED_FRAME);
                    break;
                case 9:
                    AwareAppFreezeMng.this.isSilde.set(true);
                    break;
                case 10:
                    AwareAppFreezeMng.this.isSilde.set(false);
                    break;
                case 11:
                    AwareAppFreezeMng.this.handleToFreeze(AwareAppFreezeMng.this.mDefaultDelay, AwareAppFreezeMng.REASON_GALLERY_SCALE);
                    break;
            }
        }
    }

    private class AwareSceneRecognizeCallback implements AwareSceneRecognize.IAwareSceneRecCallback {
        private AwareSceneRecognizeCallback() {
        }

        public void onStateChanged(int sceneType, int eventType, String pkgName) {
            Message msg = AwareAppFreezeMng.this.mHandler.obtainMessage();
            msg.what = -1;
            if (eventType == 1) {
                if (sceneType == 2) {
                    msg.what = 9;
                } else if (sceneType == 4) {
                    msg.what = 2;
                } else if (sceneType == 8) {
                    msg.what = 4;
                } else if (sceneType == 16) {
                    msg.what = 7;
                } else if (sceneType == 32) {
                    msg.what = 8;
                } else if (sceneType == 64) {
                    msg.what = 11;
                }
            } else if (sceneType == 2) {
                msg.what = 10;
            }
            if (msg.what != -1) {
                Bundle data = new Bundle();
                data.putString("pkgName", pkgName);
                msg.setData(data);
                AwareAppFreezeMng.this.mHandler.sendMessage(msg);
            }
        }
    }

    public static synchronized AwareAppFreezeMng getInstance() {
        AwareAppFreezeMng awareAppFreezeMng;
        synchronized (AwareAppFreezeMng.class) {
            if (mAwareAppFreezeMng == null) {
                mAwareAppFreezeMng = new AwareAppFreezeMng();
            }
            awareAppFreezeMng = mAwareAppFreezeMng;
        }
        return awareAppFreezeMng;
    }

    private AwareAppFreezeMng() {
        this.mEnabled = false;
        this.mIsScreenOn = new AtomicBoolean(true);
        this.mIsStatusBarRevealed = new AtomicBoolean(false);
        this.isFreeze = new AtomicBoolean(false);
        this.isSilde = new AtomicBoolean(false);
        this.mHandler = null;
        this.mCurPkgName = "";
        this.lastFreezeTime = SystemClock.elapsedRealtimeNanos();
        this.mInterval = 4500;
        this.mDefaultDelay = 1500;
        this.mCameraDelay = 1500;
        this.mIsInitialized = new AtomicBoolean(false);
        this.mAwareSceneRecognizeCallback = new AwareSceneRecognizeCallback();
        this.mProcessObserver = new IProcessObserver.Stub() {
            public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
                AwareAppFreezeMng.this.mAwareNativeFreezeManager.onFgActivitiesChanged(pid, uid, foregroundActivities);
                if (foregroundActivities && AwareAppFreezeMng.this.mEnabled) {
                    Message msg = AwareAppFreezeMng.this.mHandler.obtainMessage();
                    msg.what = 6;
                    msg.getData().putInt("uid", uid);
                    AwareAppFreezeMng.this.mHandler.sendMessage(msg);
                }
            }

            public void onProcessDied(int pid, int uid) {
            }
        };
        this.mInterval = (long) SystemProperties.getInt(PROPERTIES_FREEZE_INVERVAL, MIN_FREEZE_INTERVAL);
        mIsEvilEnable = SystemProperties.getBoolean(PROPERTIES_CONTROL_ENABLE, true);
        this.mDefaultDelay = SystemProperties.getInt(PROPERTIES_IAWARE_FREEZE_DELAY, 1500);
        this.mCameraDelay = SystemProperties.getInt(PROPERTIES_FREEZE_DELAY, 1500);
        if (this.mDefaultDelay > this.mCameraDelay) {
            this.mDefaultDelay = this.mCameraDelay;
        }
    }

    /* access modifiers changed from: private */
    public void initialize() {
        if (!this.mIsInitialized.get()) {
            this.mHandler = new AwareAppFreezeMngHanlder();
            this.mAwareNativeFreezeManager = new AwareNativeFreezeManager();
            registerAwareSceneRecognize();
            registerObserver();
            MultiTaskManagerService mMtmService = MultiTaskManagerService.self();
            if (mMtmService != null) {
                DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_FREEZE, mMtmService.context());
                AwareAppDefaultFreeze.getInstance().init(mMtmService.context());
                this.mAwareNativeFreezeManager.start(mMtmService.context());
            }
            this.mIsInitialized.set(true);
        }
    }

    private void deInitialize() {
        if (this.mIsInitialized.get()) {
            unregisterAwareSceneRecognize();
            unregisterObserver();
            AwareAppDefaultFreeze.getInstance().deInitDefaultFree();
            this.mAwareNativeFreezeManager.destroy();
            this.mIsInitialized.set(false);
        }
    }

    private void registerAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.registerStateCallback(this.mAwareSceneRecognizeCallback, 1);
        }
    }

    private void unregisterAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.unregisterStateCallback(this.mAwareSceneRecognizeCallback);
        }
    }

    public void enable() {
        if (mAwareAppFreezeMng == null) {
            AwareLog.e(TAG, "AwareAppFreezeMng enable failed");
            return;
        }
        mAwareAppFreezeMng.initialize();
        this.mEnabled = true;
    }

    public void disable() {
        if (mAwareAppFreezeMng == null) {
            AwareLog.e(TAG, "AwareAppFreezeMng disable failed");
            return;
        }
        mAwareAppFreezeMng.deInitialize();
        this.mEnabled = false;
    }

    private boolean isEvilControlEnable() {
        return this.mEnabled && mIsEvilEnable;
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    /* access modifiers changed from: private */
    public void handleToUnFreeze(String reason) {
        this.isFreeze.set(false);
    }

    /* access modifiers changed from: private */
    public void handleSkippedFrameFreeze(int duration, String reason) {
        if (this.isSilde.get()) {
            handleToFreeze(this.mDefaultDelay, REASON_SKIPPED_FRAME);
        }
    }

    /* access modifiers changed from: private */
    public void handleToFreeze(int duration, String reason) {
        if (!this.mIsScreenOn.get()) {
            AwareLog.i(TAG, "Current is Screen off now");
        } else if (this.mIsStatusBarRevealed.get()) {
            AwareLog.i(TAG, "Current status bar is revealed now");
        } else if (AwareAppAssociate.getInstance().getDefaultHomePackages().contains(this.mCurPkgName)) {
            AwareLog.i(TAG, "Current is on launcher");
        } else if ("com.android.systemui".equals(this.mCurPkgName)) {
            AwareLog.i(TAG, "Current is on system ui");
        } else {
            if (!this.isFreeze.get()) {
                long curTime = SystemClock.elapsedRealtimeNanos();
                if (curTime - this.lastFreezeTime >= this.mInterval * MILLI_TO_NANO) {
                    AppMngConstant.AppFreezeSource config = AppMngConstant.AppFreezeSource.FAST_FREEZE;
                    if (isCamera()) {
                        config = AppMngConstant.AppFreezeSource.CAMERA_FREEZE;
                    }
                    AwareAppDefaultFreeze.getInstance().doFrozen(this.mCurPkgName, config, duration, reason);
                    this.isFreeze.set(true);
                    this.lastFreezeTime = curTime;
                    this.mHandler.sendEmptyMessageDelayed(3, (long) this.mDefaultDelay);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isCamera() {
        if (this.mCurPkgName == null || !"com.huawei.camera".equals(this.mCurPkgName)) {
            return false;
        }
        return true;
    }

    private void registerObserver() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.w(TAG, "register process observer failed");
        }
    }

    private void unregisterObserver() {
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            AwareLog.w(TAG, "unregister process observer failed");
        }
    }

    public void report(int eventId) {
        if (!this.mEnabled) {
            if (DEBUG) {
                AwareLog.d(TAG, "AwareAppFreezeMng feature disabled!");
            }
            return;
        }
        if (DEBUG) {
            AwareLog.d(TAG, "resId: " + eventId);
        }
        if (!this.mIsInitialized.get()) {
            initialize();
        }
        if (eventId == 20011) {
            this.mAwareNativeFreezeManager.reportScreenEvent(true);
            this.mIsScreenOn.set(true);
        } else if (eventId == 20015) {
            this.mIsStatusBarRevealed.set(true);
        } else if (eventId == 90011) {
            this.mAwareNativeFreezeManager.reportScreenEvent(false);
            this.mIsScreenOn.set(false);
        } else if (eventId != 90015) {
            AwareLog.e(TAG, "Unknown EventID: " + eventId);
        } else {
            this.mIsStatusBarRevealed.set(false);
        }
    }

    public void report(AwareConstant.ResourceType type, CollectData data) {
        if (!this.mEnabled) {
            if (DEBUG) {
                AwareLog.d(TAG, "AwareAppFreezeMng feature disabled!");
            }
            return;
        }
        if (AnonymousClass2.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[type.ordinal()] == 1) {
            this.mAwareNativeFreezeManager.reportData(1, data);
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            if (!this.mEnabled) {
                pw.println("AwareAppFreezeMng feature disabled.");
            } else {
                AwareAppDefaultFreeze.getInstance().dump(pw);
            }
        }
    }

    public void dumpFreezeApp(PrintWriter pw, String pkg, int time) {
        if (pw != null) {
            if (!this.mEnabled) {
                pw.println("AwareAppFreezeMng feature disabled.");
            } else {
                AwareAppDefaultFreeze.getInstance().dumpFreezeApp(pw, pkg, time);
            }
        }
    }

    public boolean isEvilWindow(int window, int code, int type) {
        if (!isEvilControlEnable()) {
            AwareLog.i(TAG, "evil window control is disabled");
            return false;
        }
        boolean result = false;
        if (type == 1) {
            result = AwareAppAssociate.getInstance().isEvilAlertWindow(window, code);
        } else if (type == 2) {
            result = AwareIntelligentRecg.getInstance().isEvilToastWindow(window, code);
        }
        return result;
    }

    public void dumpFreezeBadPid(PrintWriter pw, int pid, int uid) {
        if (pw != null) {
            if (!this.mEnabled) {
                pw.println("AwareAppFreezeMng feature disabled.");
            } else {
                AwareAppDefaultFreeze.getInstance().dumpFreezeBadPid(pw, pid, uid);
            }
        }
    }
}
