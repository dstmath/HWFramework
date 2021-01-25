package com.android.server.mtm.iaware.appmng.appiolimit;

import android.app.AppOpsManager;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.app.AppOpsManagerExt;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.HwActivityTaskManagerAdapter;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.android.os.IMWThirdpartyCallbackEx;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppIoLimitMng {
    private static final String APP_SYSTEM_UI = "com.android.systemui";
    private static final int CAMERA_UNIOLIMIT_DURATION = 5000;
    private static final int DEFAULT_UNIOLIMIT_DURATION = 3000;
    private static final String FEATURE_NAME = "appmng_feature";
    private static final String ITEM_CONFIG_NAME = "camera_iolimit";
    private static final String ITEM_NAME = "camera_iolimit_duration";
    private static final String KEY_BUNDLE_PACKAGENAME = "pkgName";
    private static final String KEY_BUNDLE_PID = "pid";
    private static final String KEY_BUNDLE_UID = "uid";
    private static final Object LOCK = new Object();
    private static final int MAX_UNIOLIMIT_DURATION = 10000;
    private static final int MSG_ACTIVITY_STARTING = 1;
    private static final int MSG_APP_SLIPPING = 2;
    private static final int MSG_APP_SLIP_END = 9;
    private static final int MSG_CAMERA_SHOT = 5;
    private static final int MSG_FG_ACTIVITIES_CHANGED = 3;
    private static final int MSG_PROXIMITY_SCREEN_OFF = 6;
    private static final int MSG_PROXIMITY_SCREEN_ON = 7;
    private static final int MSG_RECOGNIZE_GAME_APP = 8;
    private static final int MSG_THIRD_CAMERA_OPEN = 10;
    private static final int MSG_UNIOLIMIT_DURATION = 4;
    private static final String PACKAGE_CAMERA = "com.huawei.camera";
    private static final int RECOGNIZE_GAME_DELAY_TIME = 1000;
    private static final String TAG = "AwareAppIoLimitMng";
    private static final String THIRD_ITEM_NAME = "third_camera_iolimit_duration";
    private static AwareAppIoLimitMng sAwareAppIoLimitMng = null;
    private static AtomicBoolean sCameraEnhanced = new AtomicBoolean(false);
    private static boolean sDebug = false;
    private static boolean sEnabled = false;
    private static AtomicBoolean sThirdCameraEnhanced = new AtomicBoolean(false);
    private AppOpsManager mAppOps;
    private AppOpsListener mAppOpsListener;
    private AppIoLimitCallBackHandler mCallBackHandler;
    private int mCameraIoLimitDuration;
    private String mCurPkgName;
    private Handler mHandler;
    private int mIoLimitDuration;
    private AppIoLimitObserver mIoLimitObserver;
    private IOLimitSceneRecognize mIoLimitSceneRecognize;
    private AtomicBoolean mIsGame;
    private AtomicBoolean mIsIOLimit;
    private AtomicBoolean mIsInitialized;
    private AtomicBoolean mIsMultiWin;
    private AtomicBoolean mIsScreenOn;
    private AtomicBoolean mIsStatusBarRevealed;
    private int mThirdCameraIoLimitDuration;

    public static AwareAppIoLimitMng getInstance() {
        AwareAppIoLimitMng awareAppIoLimitMng;
        synchronized (LOCK) {
            if (sAwareAppIoLimitMng == null) {
                sAwareAppIoLimitMng = new AwareAppIoLimitMng();
            }
            awareAppIoLimitMng = sAwareAppIoLimitMng;
        }
        return awareAppIoLimitMng;
    }

    private AwareAppIoLimitMng() {
        this.mIoLimitObserver = new AppIoLimitObserver();
        this.mIsScreenOn = new AtomicBoolean(true);
        this.mIsStatusBarRevealed = new AtomicBoolean(false);
        this.mIsIOLimit = new AtomicBoolean(false);
        this.mIsGame = new AtomicBoolean(false);
        this.mIsMultiWin = new AtomicBoolean(false);
        this.mHandler = null;
        this.mCurPkgName = "";
        this.mIoLimitDuration = DEFAULT_UNIOLIMIT_DURATION;
        this.mCameraIoLimitDuration = CAMERA_UNIOLIMIT_DURATION;
        this.mThirdCameraIoLimitDuration = CAMERA_UNIOLIMIT_DURATION;
        this.mIsInitialized = new AtomicBoolean(false);
        this.mIoLimitSceneRecognize = new IOLimitSceneRecognize();
        this.mCallBackHandler = new AppIoLimitCallBackHandler();
        this.mAppOpsListener = null;
        this.mAppOps = null;
        this.mHandler = new AwareAppIoLimitMngHanlder();
    }

    public static boolean isCameraEnhanced() {
        return sCameraEnhanced.get();
    }

    private AwareConfig getConfig(String featureName, String configName) {
        if (featureName == null || featureName.isEmpty() || configName == null || configName.isEmpty()) {
            return null;
        }
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                return IAwareCMSManager.getCustConfig(awareservice, featureName, configName);
            }
            AwareLog.e(TAG, "can not find service awareservice!");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "IOFeature getConfig RemoteException");
            return null;
        }
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
            HwActivityTaskManagerAdapter.registerThirdPartyCallBack(this.mCallBackHandler);
            this.mIoLimitDuration = SystemPropertiesEx.getInt("persist.sys.io_limit_delay", (int) DEFAULT_UNIOLIMIT_DURATION);
            AwareConfig configList = getConfig(FEATURE_NAME, ITEM_CONFIG_NAME);
            if (configList != null) {
                for (AwareConfig.Item item : configList.getConfigList()) {
                    if (item == null) {
                        AwareLog.w(TAG, "getConfig failed, item is empty");
                    } else {
                        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
                        if (subItemList != null) {
                            for (AwareConfig.SubItem subItem : subItemList) {
                                if (!(subItem == null || subItem.getName() == null)) {
                                    if (subItem.getName().equals(ITEM_NAME)) {
                                        try {
                                            this.mCameraIoLimitDuration = checkValidDuration(Integer.parseInt(subItem.getValue()));
                                        } catch (NumberFormatException e) {
                                            AwareLog.e(TAG, "camera_iolimit_duration is not an Integer!");
                                        }
                                    } else if (subItem.getName().equals(THIRD_ITEM_NAME)) {
                                        try {
                                            this.mThirdCameraIoLimitDuration = checkValidDuration(Integer.parseInt(subItem.getValue()));
                                            sThirdCameraEnhanced.set(true);
                                        } catch (NumberFormatException e2) {
                                            AwareLog.e(TAG, "third_camera_iolimit_duration is not an Integer!");
                                        }
                                    } else {
                                        AwareLog.w(TAG, "unknown item:" + subItem.getName());
                                    }
                                }
                            }
                        }
                    }
                }
                sCameraEnhanced.set(true);
                if (sCameraEnhanced.get() && sThirdCameraEnhanced.get()) {
                    registerOpCamera();
                }
            }
            this.mIsInitialized.set(true);
        }
    }

    private int checkValidDuration(int value) {
        if (value >= CAMERA_UNIOLIMIT_DURATION && value <= 10000) {
            return value;
        }
        AwareLog.w(TAG, "invalid camera io limit duration:" + value + ", set to default value:" + CAMERA_UNIOLIMIT_DURATION);
        return CAMERA_UNIOLIMIT_DURATION;
    }

    private void registerOpCamera() {
        Context context = MemoryConstant.getContext();
        if (context != null) {
            Object obj = context.getSystemService("appops");
            if (obj instanceof AppOpsManager) {
                this.mAppOps = (AppOpsManager) obj;
            }
            if (this.mAppOps != null) {
                this.mAppOpsListener = new AppOpsListener();
                AppOpsManagerExt.startWatchingActive(this.mAppOps, new int[]{26}, this.mAppOpsListener);
            }
        }
    }

    private void unregisterOpCamera() {
        AppOpsListener appOpsListener;
        AppOpsManager appOpsManager = this.mAppOps;
        if (appOpsManager != null && (appOpsListener = this.mAppOpsListener) != null) {
            AppOpsManagerExt.stopWatchingActive(appOpsManager, appOpsListener);
        }
    }

    private synchronized void deInitialize() {
        if (this.mIsInitialized.get()) {
            unregisterAwareSceneRecognize();
            AwareAppDefaultIoLimit.getInstance().deInitDefaultFree();
            HwActivityTaskManagerAdapter.unregisterThirdPartyCallBack(this.mCallBackHandler);
            unregisterObserver();
            unregisterOpCamera();
            sCameraEnhanced.set(false);
            sThirdCameraEnhanced.set(false);
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
        sEnabled = true;
        AwareAppIoLimitMng awareAppIoLimitMng = sAwareAppIoLimitMng;
        if (awareAppIoLimitMng != null) {
            awareAppIoLimitMng.initialize();
        }
    }

    public static void disable() {
        AwareLog.d(TAG, "AwareAppIoLimitMng feature disabled");
        sEnabled = false;
        AwareAppIoLimitMng awareAppIoLimitMng = sAwareAppIoLimitMng;
        if (awareAppIoLimitMng != null) {
            awareAppIoLimitMng.deInitialize();
        }
    }

    public static void enableDebug() {
        sDebug = true;
        AwareAppDefaultIoLimit.enableDebug();
    }

    public static void disableDebug() {
        sDebug = false;
        AwareAppDefaultIoLimit.disableDebug();
    }

    private class AwareAppIoLimitMngHanlder extends Handler {
        private AwareAppIoLimitMngHanlder() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (AwareAppIoLimitMng.sEnabled) {
                if (AwareAppIoLimitMng.sDebug) {
                    AwareLog.d(AwareAppIoLimitMng.TAG, "handleMessage message " + msg.what);
                }
                switch (msg.what) {
                    case 1:
                        Bundle data = msg.getData();
                        AwareAppIoLimitMng.this.mCurPkgName = data.getString("pkgName");
                        AwareAppIoLimitMng.this.handleToIOLimit(1);
                        return;
                    case 2:
                        if (!AwareAppIoLimitMng.this.mIsMultiWin.get() && !AwareAppIoLimitMng.this.mIsGame.get()) {
                            AwareAppIoLimitMng.this.handleToIOLimit(2);
                            return;
                        }
                        return;
                    case 3:
                        AwareAppIoLimitMng.this.handleFgActivityChanged(msg);
                        return;
                    case 4:
                        AwareAppIoLimitMng.this.handleToUnIOLimit();
                        return;
                    case 5:
                        AwareAppIoLimitMng.this.handleToIOLimit(5);
                        return;
                    case 6:
                        AwareLog.d(AwareAppIoLimitMng.TAG, "is Proximity screen off!");
                        AwareAppIoLimitMng.this.hanldeProximityToIOLimit();
                        return;
                    case 7:
                        AwareLog.d(AwareAppIoLimitMng.TAG, "is Proximity screen on!");
                        AwareAppIoLimitMng.this.hanldeProximityToIOLimit();
                        return;
                    case 8:
                        AwareAppIoLimitMng.this.recognizeGameApp();
                        return;
                    case 9:
                        AwareAppIoLimitMng.this.handleToUnIOLimit();
                        AwareLog.d(AwareAppIoLimitMng.TAG, "slip end!");
                        return;
                    default:
                        return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFgActivityChanged(Message msg) {
        Bundle data = msg.getData();
        int uid = data.getInt("uid");
        handleToRemovePids(uid, data.getInt("pid"));
        this.mCurPkgName = InnerUtils.getPackageNameByUid(uid);
        if (AwareAppAssociate.getInstance().getDefaultHomePackages().contains(this.mCurPkgName) || "com.android.systemui".equals(this.mCurPkgName)) {
            this.mIsGame.set(false);
            return;
        }
        if (AwareIntelligentRecg.getInstance().isAppMngSpecType(this.mCurPkgName, 9)) {
            this.mIsGame.set(true);
        } else {
            sendDelayMsg(8, 1000);
        }
        AwareLog.d(TAG, "mCurPkgName:" + this.mCurPkgName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recognizeGameApp() {
        SparseSet fgPids = new SparseSet();
        AwareAppAssociate.getInstance().getForeGroundApp(fgPids);
        if (!fgPids.isEmpty()) {
            String pkgName = "";
            boolean gameType = false;
            int i = fgPids.size() - 1;
            while (true) {
                if (i < 0) {
                    break;
                }
                pkgName = InnerUtils.getAwarePkgName(fgPids.keyAt(i));
                if (AwareIntelligentRecg.getInstance().isAppMngSpecType(pkgName, 9)) {
                    gameType = true;
                    break;
                }
                i--;
            }
            this.mIsGame.set(gameType);
            AwareLog.d(TAG, "pkg:" + pkgName + ",mIsGame:" + this.mIsGame.get());
        }
    }

    private void sendDelayMsg(int msgType, int duration) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = msgType;
        this.mHandler.sendMessageDelayed(msg, (long) duration);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleToIOLimit(int msgType) {
        if (!this.mIsScreenOn.get()) {
            AwareLog.d(TAG, "Current is Screen off now");
        } else if (this.mIsStatusBarRevealed.get()) {
            AwareLog.d(TAG, "Current status bar is revealed now");
        } else if (AwareAppAssociate.getInstance().getDefaultHomePackages().contains(this.mCurPkgName) || "com.android.systemui".equals(this.mCurPkgName)) {
            AwareLog.d(TAG, "Current App is home or System UI:" + this.mCurPkgName);
        } else if (!this.mIsIOLimit.get()) {
            AppMngConstant.AppIoLimitSource config = AppMngConstant.AppIoLimitSource.IOLIMIT;
            int duration = this.mIoLimitDuration;
            AwareLog.d(TAG, "isCameraEnhanced() = " + isCameraEnhanced());
            boolean isCamera = false;
            if (isCameraEnhanced()) {
                if ("com.huawei.camera".equals(this.mCurPkgName)) {
                    config = AppMngConstant.AppIoLimitSource.CAMERA_IOLIMIT;
                    duration = this.mCameraIoLimitDuration;
                    isCamera = true;
                } else if (msgType == 10) {
                    config = AppMngConstant.AppIoLimitSource.CAMERA_IOLIMIT;
                    duration = this.mThirdCameraIoLimitDuration;
                    isCamera = true;
                    AwareLog.d(TAG, "third party camera io limit, duration:" + duration);
                } else {
                    AwareLog.d(TAG, "default io limit, duration:" + duration);
                }
            }
            AwareAppDefaultIoLimit.getInstance().doLimitIO(this.mCurPkgName, config, isCamera);
            this.mIsIOLimit.set(true);
            if (msgType != 2) {
                sendDelayMsg(4, duration);
            }
        } else if (msgType == 10) {
            AwareLog.d(TAG, "third party camera io limit, duration:" + this.mThirdCameraIoLimitDuration);
            AppMngConstant.AppIoLimitSource config2 = AppMngConstant.AppIoLimitSource.CAMERA_IOLIMIT;
            this.mHandler.removeMessages(4);
            AwareAppDefaultIoLimit.getInstance().doLimitIO(this.mCurPkgName, config2, true);
            this.mIsIOLimit.set(true);
            sendDelayMsg(4, this.mThirdCameraIoLimitDuration);
        } else if (msgType == 1) {
            if (sDebug) {
                AwareLog.d(TAG, "continue io limit");
            }
            this.mHandler.removeMessages(4);
            sendDelayMsg(4, this.mIoLimitDuration);
        } else if (msgType == 2) {
            this.mHandler.removeMessages(4);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hanldeProximityToIOLimit() {
        if (!this.mIsIOLimit.get()) {
            AwareAppDefaultIoLimit.getInstance().doLimitIO(this.mCurPkgName, AppMngConstant.AppIoLimitSource.IOLIMIT, "com.huawei.camera".equals(this.mCurPkgName));
            this.mIsIOLimit.set(true);
            sendDelayMsg(4, this.mIoLimitDuration);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleToUnIOLimit() {
        if (this.mIsIOLimit.get()) {
            AwareAppDefaultIoLimit.getInstance().doUnLimitIO();
            this.mIsIOLimit.set(false);
        }
    }

    private void handleToRemovePids(int uid, int pid) {
        if (this.mIsIOLimit.get()) {
            AwareAppDefaultIoLimit.getInstance().doRemoveIoPids(uid, pid);
        }
    }

    /* access modifiers changed from: private */
    public class IOLimitSceneRecognize implements AwareSceneRecognize.IAwareSceneRecCallback {
        private IOLimitSceneRecognize() {
        }

        @Override // com.android.server.rms.iaware.appmng.AwareSceneRecognize.IAwareSceneRecCallback
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

    public void report(int eventId) {
        if (sEnabled) {
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
        if (sEnabled && bundleArgs != null) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            if (eventId == 1 || eventId == 2) {
                int callerUid = bundleArgs.getInt("callUid");
                int targetUid = bundleArgs.getInt("tgtUid");
                if (callerUid != targetUid && targetUid >= 0) {
                    handleToRemovePids(targetUid, 0);
                }
            } else if (eventId == 20017) {
                handleToRemovePids(bundleArgs.getInt("callUid"), 0);
            }
        }
    }

    private void registerObserver() {
        AwareCallback.getInstance().registerProcessObserver(this.mIoLimitObserver);
    }

    private void unregisterObserver() {
        AwareCallback.getInstance().unregisterProcessObserver(this.mIoLimitObserver);
    }

    /* access modifiers changed from: package-private */
    public class AppIoLimitObserver extends IProcessObserverEx {
        AppIoLimitObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities && AwareAppIoLimitMng.sEnabled) {
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

    /* access modifiers changed from: private */
    public class AppIoLimitCallBackHandler extends IMWThirdpartyCallbackEx {
        private AppIoLimitCallBackHandler() {
        }

        public void onModeChanged(boolean aMWStatus) {
            AwareAppIoLimitMng.this.mIsMultiWin.set(aMWStatus);
            if (!AwareAppIoLimitMng.this.mIsMultiWin.get()) {
                AwareAppIoLimitMng.this.mCurPkgName = "";
            }
            AwareLog.d(AwareAppIoLimitMng.TAG, "mIsMultiWin:" + AwareAppIoLimitMng.this.mIsMultiWin.get());
        }

        public void onZoneChanged() {
        }

        public void onSizeChanged() {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCameraOpen(String packageName) {
        if (sCameraEnhanced.get() && sThirdCameraEnhanced.get() && packageName != null && !"com.huawei.camera".equals(packageName)) {
            this.mCurPkgName = packageName;
            handleToIOLimit(10);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCurrentActivityName() {
        ActivityInfo activityInfo = HwActivityTaskManager.getLastResumedActivity();
        if (activityInfo == null || ActivityInfoEx.getComponentName(activityInfo) == null) {
            return "";
        }
        return ActivityInfoEx.getComponentName(activityInfo).getClassName();
    }

    /* access modifiers changed from: package-private */
    public class AppOpsListener extends AppOpsManagerExt.OnOpActiveChangedListenerEx {
        AppOpsListener() {
        }

        public void onOpActiveChanged(int code, int uid, String packageName, boolean active) {
            if (active) {
                AwareLog.d(AwareAppIoLimitMng.TAG, "open camera activity change:" + packageName + ", activity:" + AwareAppIoLimitMng.this.getCurrentActivityName());
                AwareAppIoLimitMng.this.processCameraOpen(packageName);
            }
        }
    }
}
