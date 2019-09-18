package com.android.server.mtm.iaware.appmng.appiolimit;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import android.util.ArraySet;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.huawei.android.app.HwActivityManager;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppIoLimitMng {
    private static final String APP_SYSTEM_UI = "com.android.systemui";
    private static final int CAMERA_UNIOLIMIT_DURATION = 5000;
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    private static final int DEFAULT_UNIOLIMIT_DURATION = 3000;
    private static final String FEATURE_NAME = "appmng_feature";
    private static final String ITEM_CONFIG_NAME = "camera_iolimit";
    private static final String ITEM_NAME = "camera_iolimit_duration";
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
    private static final String PACKAGE_CAMERA = "com.huawei.camera";
    private static final int RECOGNIZE_GAME_DELAY_TIME = 1000;
    private static final String TAG = "AwareAppIoLimitMng";
    private static AwareAppIoLimitMng mAwareAppIoLimitMng = null;
    private static AtomicBoolean mCameraEnhanced = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public static boolean mEnabled = false;
    /* access modifiers changed from: private */
    public AtomicBoolean isGame;
    private AtomicBoolean isIOLimit;
    private AppIoLimitCallBackHandler mCallBackHandler;
    private int mCameraIoLimit_duration;
    /* access modifiers changed from: private */
    public String mCurPkgName;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private AppIoLimitObserver mIoLimitObserver;
    private IOLimitSceneRecognize mIoLimitSceneRecognize;
    private int mIoLimit_duration;
    private AtomicBoolean mIsInitialized;
    /* access modifiers changed from: private */
    public AtomicBoolean mIsMultiWin;
    private AtomicBoolean mIsScreenOn;
    private AtomicBoolean mIsStatusBarRevealed;

    private class AppIoLimitCallBackHandler extends IMWThirdpartyCallback.Stub {
        private AppIoLimitCallBackHandler() {
        }

        public void onModeChanged(boolean aMWStatus) {
            AwareAppIoLimitMng.this.mIsMultiWin.set(aMWStatus);
            if (!AwareAppIoLimitMng.this.mIsMultiWin.get()) {
                String unused = AwareAppIoLimitMng.this.mCurPkgName = "";
            }
            AwareLog.d(AwareAppIoLimitMng.TAG, "mIsMultiWin:" + AwareAppIoLimitMng.this.mIsMultiWin.get());
        }

        public void onZoneChanged() {
        }

        public void onSizeChanged() {
        }
    }

    class AppIoLimitObserver extends IProcessObserver.Stub {
        AppIoLimitObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities && AwareAppIoLimitMng.mEnabled) {
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
        private AwareAppIoLimitMngHanlder() {
        }

        public void handleMessage(Message msg) {
            if (AwareAppIoLimitMng.mEnabled) {
                if (AwareAppIoLimitMng.DEBUG) {
                    AwareLog.d(AwareAppIoLimitMng.TAG, "handleMessage message " + msg.what);
                }
                switch (msg.what) {
                    case 1:
                        String unused = AwareAppIoLimitMng.this.mCurPkgName = msg.getData().getString("pkgName");
                        AwareAppIoLimitMng.this.handleToIOLimit(1);
                        break;
                    case 2:
                        if (!AwareAppIoLimitMng.this.mIsMultiWin.get() && !AwareAppIoLimitMng.this.isGame.get()) {
                            AwareAppIoLimitMng.this.handleToIOLimit(2);
                            break;
                        }
                    case 3:
                        Bundle data = msg.getData();
                        int uid = data.getInt("uid");
                        AwareAppIoLimitMng.this.handleToRemovePids(uid, data.getInt("pid"));
                        String unused2 = AwareAppIoLimitMng.this.mCurPkgName = InnerUtils.getPackageNameByUid(uid);
                        if (!AwareAppAssociate.getInstance().getDefaultHomePackages().contains(AwareAppIoLimitMng.this.mCurPkgName) && !"com.android.systemui".equals(AwareAppIoLimitMng.this.mCurPkgName)) {
                            if (AwareIntelligentRecg.getInstance().isAppMngSpecType(AwareAppIoLimitMng.this.mCurPkgName, 9)) {
                                AwareAppIoLimitMng.this.isGame.set(true);
                            } else {
                                AwareAppIoLimitMng.this.sendDelayMsg(8, 1000);
                            }
                            AwareLog.d(AwareAppIoLimitMng.TAG, "mCurPkgName:" + AwareAppIoLimitMng.this.mCurPkgName);
                            break;
                        } else {
                            AwareAppIoLimitMng.this.isGame.set(false);
                            break;
                        }
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

    private class IOLimitSceneRecognize implements AwareSceneRecognize.IAwareSceneRecCallback {
        private IOLimitSceneRecognize() {
        }

        public void onStateChanged(int sceneType, int eventType, String pkgName) {
            Message msg = AwareAppIoLimitMng.this.mHandler.obtainMessage();
            if (eventType == 1) {
                if (sceneType == 2) {
                    msg.what = 2;
                } else if (sceneType == 4) {
                    msg.what = 1;
                } else if (sceneType == 8) {
                    msg.what = 5;
                } else if (sceneType != 16) {
                    msg.what = 0;
                } else {
                    msg.what = 6;
                }
            } else if (sceneType == 2) {
                msg.what = 9;
            } else if (sceneType != 16) {
                msg.what = 0;
            } else {
                msg.what = 7;
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
        this.mIoLimit_duration = 3000;
        this.mCameraIoLimit_duration = CAMERA_UNIOLIMIT_DURATION;
        this.mIsInitialized = new AtomicBoolean(false);
        this.mIoLimitSceneRecognize = new IOLimitSceneRecognize();
        this.mCallBackHandler = new AppIoLimitCallBackHandler();
        this.mHandler = new AwareAppIoLimitMngHanlder();
    }

    public static boolean isCameraEnhanced() {
        return mCameraEnhanced.get();
    }

    private AwareConfig getConfig(String featureName, String configName) {
        AwareConfig configList = null;
        if (featureName == null || featureName.isEmpty() || configName == null || configName.isEmpty()) {
            return null;
        }
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                configList = IAwareCMSManager.getCustConfig(awareservice, featureName, configName);
            } else {
                AwareLog.e(TAG, "can not find service awareservice!");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "IOFeature getConfig RemoteException");
        }
        return configList;
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            registerAwareSceneRecognize();
            MultiTaskManagerService mMtmService = MultiTaskManagerService.self();
            if (mMtmService != null) {
                DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_IOLIMIT, mMtmService.context());
                AwareAppDefaultIoLimit.getInstance().init(mMtmService.context());
            }
            registerObserver();
            HwActivityManager.registerThirdPartyCallBack(this.mCallBackHandler);
            this.mIoLimit_duration = SystemProperties.getInt("persist.sys.io_limit_delay", 3000);
            AwareConfig configList = getConfig(FEATURE_NAME, ITEM_CONFIG_NAME);
            if (configList != null) {
                for (AwareConfig.Item item : configList.getConfigList()) {
                    if (item == null) {
                        AwareLog.w(TAG, "getConfig failed, item is empty");
                    } else {
                        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
                        if (subItemList != null) {
                            for (AwareConfig.SubItem subItem : subItemList) {
                                if (subItem != null && ITEM_NAME.equals(subItem.getName())) {
                                    try {
                                        this.mCameraIoLimit_duration = Integer.parseInt(subItem.getValue());
                                    } catch (NumberFormatException e) {
                                        AwareLog.e(TAG, "camera_iolimit_duration is not an Integer!");
                                    }
                                }
                            }
                        }
                    }
                }
                mCameraEnhanced.set(true);
            }
            this.mIsInitialized.set(true);
        }
    }

    private synchronized void deInitialize() {
        if (this.mIsInitialized.get()) {
            unregisterAwareSceneRecognize();
            AwareAppDefaultIoLimit.getInstance().deInitDefaultFree();
            HwActivityManager.unregisterThirdPartyCallBack(this.mCallBackHandler);
            unregisterObserver();
            mCameraEnhanced.set(false);
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

    /* access modifiers changed from: private */
    public void recognizeGameApp() {
        Set<Integer> fgPids = new ArraySet<>();
        AwareAppAssociate.getInstance().getForeGroundApp(fgPids);
        if (!fgPids.isEmpty()) {
            String pkgName = "";
            boolean gameType = false;
            Iterator<Integer> it = fgPids.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                pkgName = InnerUtils.getAwarePkgName(it.next().intValue());
                if (AwareIntelligentRecg.getInstance().isAppMngSpecType(pkgName, 9)) {
                    gameType = true;
                    break;
                }
            }
            this.isGame.set(gameType);
            AwareLog.d(TAG, "pkg:" + pkgName + ",isGame:" + this.isGame.get());
        }
    }

    /* access modifiers changed from: private */
    public void sendDelayMsg(int msgType, int duration) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = msgType;
        this.mHandler.sendMessageDelayed(msg, (long) duration);
    }

    /* access modifiers changed from: private */
    public void handleToIOLimit(int msgType) {
        if (!this.mIsScreenOn.get()) {
            AwareLog.d(TAG, "Current is Screen off now");
        } else if (this.mIsStatusBarRevealed.get()) {
            AwareLog.d(TAG, "Current status bar is revealed now");
        } else if (AwareAppAssociate.getInstance().getDefaultHomePackages().contains(this.mCurPkgName) || "com.android.systemui".equals(this.mCurPkgName)) {
            AwareLog.d(TAG, "Current App is home or System UI:" + this.mCurPkgName);
        } else {
            if (!this.isIOLimit.get()) {
                AppMngConstant.AppIoLimitSource config = AppMngConstant.AppIoLimitSource.IOLIMIT;
                int duration = this.mIoLimit_duration;
                AwareLog.d(TAG, "isCameraEnhanced() = " + isCameraEnhanced());
                if ("com.huawei.camera".equals(this.mCurPkgName) && isCameraEnhanced()) {
                    config = AppMngConstant.AppIoLimitSource.CAMERA_IOLIMIT;
                    duration = this.mCameraIoLimit_duration;
                }
                AwareAppDefaultIoLimit.getInstance().doLimitIO(this.mCurPkgName, config);
                this.isIOLimit.set(true);
                if (msgType != 2) {
                    sendDelayMsg(4, duration);
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

    /* access modifiers changed from: private */
    public void hanldeProximityToIOLimit() {
        if (!this.isIOLimit.get()) {
            AwareAppDefaultIoLimit.getInstance().doLimitIO(this.mCurPkgName, AppMngConstant.AppIoLimitSource.IOLIMIT);
            this.isIOLimit.set(true);
            sendDelayMsg(4, this.mIoLimit_duration);
        }
    }

    /* access modifiers changed from: private */
    public void handleToUnIOLimit() {
        if (this.isIOLimit.get()) {
            AwareAppDefaultIoLimit.getInstance().doUnLimitIO();
            this.isIOLimit.set(false);
        }
    }

    /* access modifiers changed from: private */
    public void handleToRemovePids(int uid, int pid) {
        if (this.isIOLimit.get()) {
            AwareAppDefaultIoLimit.getInstance().doRemoveIoPids(uid, pid);
        }
    }

    public void report(int eventId) {
        if (mEnabled) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            if (eventId == 20011) {
                this.mIsScreenOn.set(true);
            } else if (eventId == 20015) {
                this.mIsStatusBarRevealed.set(true);
            } else if (eventId == 90011) {
                this.mIsScreenOn.set(false);
            } else if (eventId == 90015) {
                this.mIsStatusBarRevealed.set(false);
            }
        }
    }

    public void report(int eventId, Bundle bundleArgs) {
        if (mEnabled && bundleArgs != null) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            if (eventId != 20017) {
                switch (eventId) {
                    case 1:
                    case 2:
                        int callerUid = bundleArgs.getInt("callUid");
                        int targetUid = bundleArgs.getInt("tgtUid");
                        if (callerUid != targetUid && targetUid >= 0) {
                            handleToRemovePids(targetUid, 0);
                            break;
                        }
                }
            } else {
                handleToRemovePids(bundleArgs.getInt("callUid"), 0);
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
