package com.android.server.mtm.iaware.appmng.appiolimit;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver.Stub;
import android.app.mtm.iaware.appmng.AppMngConstant.AppIoLimitSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize.IAwareSceneRecCallback;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppIoLimitMng {
    private static final String APP_SYSTEM_UI = "com.android.systemui";
    private static boolean DEBUG = false;
    private static final int DEFAULT_UNIOLIMIT_DURATION = 3000;
    private static final String KEY_BUNDLE_PACKAGENAME = "pkgName";
    private static final String KEY_BUNDLE_PID = "pid";
    private static final String KEY_BUNDLE_UID = "uid";
    private static final int MSG_ACTIVITY_STARTING = 1;
    private static final int MSG_APP_SLIPPING = 2;
    private static final int MSG_APP_SLIP_END = 9;
    private static final int MSG_CAMERA_SHOT = 5;
    private static final int MSG_FG_ACTIVITIES_CHANGED = 3;
    private static final int MSG_PROXIMITY_SCREEN_OFF = 6;
    private static final int MSG_PROXIMITY_SCREEN_ON = 7;
    private static final int MSG_RECOGNIZE_GAME_APP = 8;
    private static final int MSG_UNIOLIMIT_DURATION = 4;
    private static final int RECOGNIZE_GAME_DELAY_TIME = 1000;
    private static final String TAG = "AwareAppIoLimitMng";
    private static AwareAppIoLimitMng mAwareAppIoLimitMng = null;
    private static boolean mEnabled = false;
    private AtomicBoolean isGame;
    private AtomicBoolean isIOLimit;
    private String mCurPkgName;
    private Handler mHandler;
    private AppIoLimitObserver mIoLimitObserver;
    private IOLimitSceneRecognize mIoLimitSceneRecognize;
    private int mIoLimit_duration;
    private AtomicBoolean mIsInitialized;
    private AtomicBoolean mIsMultiWin;
    private AtomicBoolean mIsScreenOn;
    private AtomicBoolean mIsStatusBarRevealed;

    class AppIoLimitObserver extends Stub {
        AppIoLimitObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities && (AwareAppIoLimitMng.mEnabled ^ 1) == 0) {
                Message msg = AwareAppIoLimitMng.this.mHandler.obtainMessage();
                msg.what = 3;
                Bundle data = msg.getData();
                data.putInt("uid", uid);
                data.putInt("pid", pid);
                AwareAppIoLimitMng.this.mHandler.sendMessage(msg);
            }
        }

        public void onProcessDied(int pid, int uid) {
        }
    }

    private class AwareAppIoLimitMngHanlder extends Handler {
        /* synthetic */ AwareAppIoLimitMngHanlder(AwareAppIoLimitMng this$0, AwareAppIoLimitMngHanlder -this1) {
            this();
        }

        private AwareAppIoLimitMngHanlder() {
        }

        public void handleMessage(Message msg) {
            if (AwareAppIoLimitMng.mEnabled) {
                if (AwareAppIoLimitMng.DEBUG) {
                    AwareLog.d(AwareAppIoLimitMng.TAG, "handleMessage message " + msg.what);
                }
                switch (msg.what) {
                    case 1:
                        AwareAppIoLimitMng.this.mCurPkgName = msg.getData().getString("pkgName");
                        AwareAppIoLimitMng.this.handleToIOLimit(1);
                        break;
                    case 2:
                        if (!(AwareAppIoLimitMng.this.mIsMultiWin.get() || (AwareAppIoLimitMng.this.isGame.get() ^ 1) == 0)) {
                            AwareAppIoLimitMng.this.handleToIOLimit(2);
                            break;
                        }
                    case 3:
                        Bundle data = msg.getData();
                        int uid = data.getInt("uid");
                        AwareAppIoLimitMng.this.handleToRemovePids(uid, data.getInt("pid"));
                        AwareAppIoLimitMng.this.mCurPkgName = InnerUtils.getPackageNameByUid(uid);
                        if (!AwareAppAssociate.getInstance().getDefaultHomePackages().contains(AwareAppIoLimitMng.this.mCurPkgName) && !AwareAppIoLimitMng.APP_SYSTEM_UI.equals(AwareAppIoLimitMng.this.mCurPkgName)) {
                            if (AwareIntelligentRecg.getInstance().isAppMngSpecType(AwareAppIoLimitMng.this.mCurPkgName, 9)) {
                                AwareAppIoLimitMng.this.isGame.set(true);
                            } else {
                                AwareAppIoLimitMng.this.sendDelayMsg(8, 1000);
                            }
                            AwareLog.d(AwareAppIoLimitMng.TAG, "mCurPkgName:" + AwareAppIoLimitMng.this.mCurPkgName);
                            break;
                        }
                        AwareAppIoLimitMng.this.isGame.set(false);
                        break;
                    case 4:
                        AwareAppIoLimitMng.this.handleToUnIOLimit();
                        break;
                    case 5:
                        AwareAppIoLimitMng.this.handleToIOLimit(5);
                        break;
                    case 6:
                        AwareLog.d(AwareAppIoLimitMng.TAG, "is Proximity screen off!");
                        AwareAppIoLimitMng.this.hanldeProximityToIOLimit();
                        break;
                    case 7:
                        AwareLog.d(AwareAppIoLimitMng.TAG, "is Proximity screen on!");
                        AwareAppIoLimitMng.this.hanldeProximityToIOLimit();
                        break;
                    case 8:
                        AwareAppIoLimitMng.this.recognizeGameApp();
                        break;
                    case 9:
                        AwareAppIoLimitMng.this.handleToUnIOLimit();
                        AwareLog.d(AwareAppIoLimitMng.TAG, "slip end!");
                        break;
                }
            }
        }
    }

    private class IOLimitSceneRecognize implements IAwareSceneRecCallback {
        /* synthetic */ IOLimitSceneRecognize(AwareAppIoLimitMng this$0, IOLimitSceneRecognize -this1) {
            this();
        }

        private IOLimitSceneRecognize() {
        }

        public void onStateChanged(int sceneType, int eventType, String pkgName) {
            Message msg = AwareAppIoLimitMng.this.mHandler.obtainMessage();
            if (eventType != 1) {
                switch (sceneType) {
                    case 2:
                        msg.what = 9;
                        break;
                    case 16:
                        msg.what = 7;
                        break;
                    default:
                        msg.what = 0;
                        break;
                }
            }
            switch (sceneType) {
                case 2:
                    msg.what = 2;
                    break;
                case 4:
                    msg.what = 1;
                    break;
                case 8:
                    msg.what = 5;
                    break;
                case 16:
                    msg.what = 6;
                    break;
                default:
                    msg.what = 0;
                    break;
            }
            if (msg.what != 0) {
                Bundle data = new Bundle();
                data.putString("pkgName", pkgName);
                msg.setData(data);
                AwareAppIoLimitMng.this.mHandler.sendMessage(msg);
            }
        }
    }

    public static synchronized AwareAppIoLimitMng getInstance() {
        AwareAppIoLimitMng awareAppIoLimitMng;
        synchronized (AwareAppIoLimitMng.class) {
            if (mAwareAppIoLimitMng == null) {
                mAwareAppIoLimitMng = new AwareAppIoLimitMng();
            }
            awareAppIoLimitMng = mAwareAppIoLimitMng;
        }
        return awareAppIoLimitMng;
    }

    private AwareAppIoLimitMng() {
        this.mIoLimitObserver = new AppIoLimitObserver();
        this.mIsScreenOn = new AtomicBoolean(true);
        this.mIsStatusBarRevealed = new AtomicBoolean(false);
        this.isIOLimit = new AtomicBoolean(false);
        this.isGame = new AtomicBoolean(false);
        this.mIsMultiWin = new AtomicBoolean(false);
        this.mHandler = null;
        this.mCurPkgName = "";
        this.mIoLimit_duration = DEFAULT_UNIOLIMIT_DURATION;
        this.mIsInitialized = new AtomicBoolean(false);
        this.mIoLimitSceneRecognize = new IOLimitSceneRecognize(this, null);
        this.mHandler = new AwareAppIoLimitMngHanlder(this, null);
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            registerAwareSceneRecognize();
            MultiTaskManagerService mMtmService = MultiTaskManagerService.self();
            if (mMtmService != null) {
                DecisionMaker.getInstance().updateRule(AppMngFeature.APP_IOLIMIT, mMtmService.context());
                AwareAppDefaultIoLimit.getInstance().init(mMtmService.context());
            }
            registerObserver();
            this.mIoLimit_duration = SystemProperties.getInt("persist.sys.io_limit_delay", DEFAULT_UNIOLIMIT_DURATION);
            this.mIsInitialized.set(true);
        }
    }

    private synchronized void deInitialize() {
        if (this.mIsInitialized.get()) {
            unregisterAwareSceneRecognize();
            AwareAppDefaultIoLimit.getInstance().deInitDefaultFree();
            unregisterObserver();
            this.mIsInitialized.set(false);
        }
    }

    private void registerAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.registerStateCallback(this.mIoLimitSceneRecognize, 1);
        }
    }

    private void unregisterAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.unregisterStateCallback(this.mIoLimitSceneRecognize);
        }
    }

    public static void enable() {
        AwareLog.d(TAG, "AwareAppIoLimitMng feature enable");
        mEnabled = true;
        if (mAwareAppIoLimitMng != null) {
            mAwareAppIoLimitMng.initialize();
        }
    }

    public static void disable() {
        AwareLog.d(TAG, "AwareAppIoLimitMng feature disabled");
        mEnabled = false;
        if (mAwareAppIoLimitMng != null) {
            mAwareAppIoLimitMng.deInitialize();
        }
    }

    public static void enableDebug() {
        DEBUG = true;
        AwareAppDefaultIoLimit.enableDebug();
    }

    public static void disableDebug() {
        DEBUG = false;
        AwareAppDefaultIoLimit.disableDebug();
    }

    private void recognizeGameApp() {
        Set<Integer> fgPids = new ArraySet();
        AwareAppAssociate.getInstance().getForeGroundApp(fgPids);
        if (!fgPids.isEmpty()) {
            String pkgName = "";
            boolean gameType = false;
            for (Integer intValue : fgPids) {
                pkgName = InnerUtils.getAwarePkgName(intValue.intValue());
                if (AwareIntelligentRecg.getInstance().isAppMngSpecType(pkgName, 9)) {
                    gameType = true;
                    break;
                }
            }
            this.isGame.set(gameType);
            AwareLog.d(TAG, "pkg:" + pkgName + ",isGame:" + this.isGame.get());
        }
    }

    private void sendDelayMsg(int msgType, int duration) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = msgType;
        this.mHandler.sendMessageDelayed(msg, (long) duration);
    }

    private void handleToIOLimit(int msgType) {
        if (!this.mIsScreenOn.get()) {
            AwareLog.d(TAG, "Current is Screen off now");
        } else if (this.mIsStatusBarRevealed.get()) {
            AwareLog.d(TAG, "Current status bar is revealed now");
        } else if (AwareAppAssociate.getInstance().getDefaultHomePackages().contains(this.mCurPkgName) || APP_SYSTEM_UI.equals(this.mCurPkgName)) {
            AwareLog.d(TAG, "Current App is home or System UI:" + this.mCurPkgName);
        } else {
            if (!this.isIOLimit.get()) {
                AwareAppDefaultIoLimit.getInstance().doLimitIO(this.mCurPkgName, AppIoLimitSource.IOLIMIT);
                this.isIOLimit.set(true);
                if (msgType != 2) {
                    sendDelayMsg(4, this.mIoLimit_duration);
                }
            } else if (msgType == 1) {
                if (DEBUG) {
                    AwareLog.d(TAG, "continue io limit");
                }
                this.mHandler.removeMessages(4);
                sendDelayMsg(4, this.mIoLimit_duration);
            } else if (msgType == 2) {
                this.mHandler.removeMessages(4);
            }
        }
    }

    private void hanldeProximityToIOLimit() {
        if (!this.isIOLimit.get()) {
            AwareAppDefaultIoLimit.getInstance().doLimitIO(this.mCurPkgName, AppIoLimitSource.IOLIMIT);
            this.isIOLimit.set(true);
            sendDelayMsg(4, this.mIoLimit_duration);
        }
    }

    private void handleToUnIOLimit() {
        if (this.isIOLimit.get()) {
            AwareAppDefaultIoLimit.getInstance().doUnLimitIO();
            this.isIOLimit.set(false);
        }
    }

    private void handleToRemovePids(int uid, int pid) {
        if (this.isIOLimit.get()) {
            AwareAppDefaultIoLimit.getInstance().doRemoveIoPids(uid, pid);
        }
    }

    public void report(int eventId) {
        if (mEnabled) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            switch (eventId) {
                case 20011:
                    this.mIsScreenOn.set(true);
                    break;
                case 20015:
                    this.mIsStatusBarRevealed.set(true);
                    break;
                case 90011:
                    this.mIsScreenOn.set(false);
                    break;
                case 90015:
                    this.mIsStatusBarRevealed.set(false);
                    break;
            }
        }
    }

    public void report(int eventId, Bundle bundleArgs) {
        if (mEnabled && bundleArgs != null) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            switch (eventId) {
                case 1:
                case 2:
                    int callerUid = bundleArgs.getInt("callUid");
                    int targetUid = bundleArgs.getInt("tgtUid");
                    if (callerUid != targetUid && targetUid >= 0) {
                        handleToRemovePids(targetUid, 0);
                        break;
                    }
                case 20017:
                    handleToRemovePids(bundleArgs.getInt("callUid"), 0);
                    break;
                case 20019:
                    this.mIsMultiWin.set(bundleArgs.getBoolean("is_multiwin"));
                    if (!this.mIsMultiWin.get()) {
                        this.mCurPkgName = "";
                    }
                    AwareLog.d(TAG, "mIsMultiWin:" + this.mIsMultiWin.get());
                    break;
            }
        }
    }

    private void registerObserver() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mIoLimitObserver);
        } catch (RemoteException e) {
            AwareLog.w(TAG, "register process observer failed");
        }
    }

    private void unregisterObserver() {
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mIoLimitObserver);
        } catch (RemoteException e) {
            AwareLog.w(TAG, "unregister process observer failed");
        }
    }
}
