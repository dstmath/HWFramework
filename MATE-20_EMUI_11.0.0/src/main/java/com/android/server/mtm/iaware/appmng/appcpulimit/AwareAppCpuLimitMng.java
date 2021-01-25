package com.android.server.mtm.iaware.appmng.appcpulimit;

import android.app.mtm.iaware.appmng.AppMngConstant;
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
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.huawei.android.app.HwActivityTaskManagerAdapter;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.android.os.IMWThirdpartyCallbackEx;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppCpuLimitMng {
    private static final int CPU_LIMIT_OPT_VERSION = 5;
    private static final int DEFAULT_UN_CPU_LIMIT_DURATION = 3000;
    private static final String FEATURE_NAME = "appmng_feature";
    private static final String ITEM_CONFIG_NAME = "cpulimit";
    private static final String ITEM_NAME = "cpulimit_duration";
    private static final String KEY_BUNDLE_PACKAGE_NAME = "pkgName";
    private static final String KEY_BUNDLE_PID = "pid";
    private static final String KEY_BUNDLE_UID = "uid";
    private static final Object LOCK = new Object();
    private static final int MSG_ACTIVITY_STARTING = 1;
    private static final int MSG_APP_SHOW_INPUT_METHOD = 6;
    private static final int MSG_APP_SLIPPING = 4;
    private static final int MSG_APP_SLIP_END = 5;
    private static final int MSG_FG_ACTIVITIES_CHANGED = 2;
    private static final int MSG_UN_CPU_LIMIT_DURATION = 3;
    private static final int SLIP_UN_CPU_LIMIT_DURATION = 1000;
    private static final String TAG = "AwareAppCpuLimitMng";
    private static AwareAppCpuLimitMng sAwareAppCpuLimitMng = null;
    private static AtomicBoolean sCpuLimitEnabled = new AtomicBoolean(false);
    private static boolean sEnabled = false;
    private static int sRealVersion = 0;
    private AppCpuLimitCallBackHandler mCallBackHandler;
    private int mCpuLimitDuration;
    private AppCpuLimitObserver mCpuLimitObserver;
    private CpuLimitSceneRecognize mCpuLimitSceneRecognize;
    private String mCurPkgName;
    private AtomicBoolean mEnableCpuLimitOptSwitch;
    private Handler mHandler;
    private AtomicBoolean mIsCpuLimited;
    private AtomicBoolean mIsInitialized;
    private AtomicBoolean mIsMultiWin;

    private AwareAppCpuLimitMng() {
        this.mCpuLimitObserver = new AppCpuLimitObserver();
        this.mIsCpuLimited = new AtomicBoolean(false);
        this.mIsMultiWin = new AtomicBoolean(false);
        this.mHandler = null;
        this.mCurPkgName = "";
        this.mCpuLimitDuration = DEFAULT_UN_CPU_LIMIT_DURATION;
        this.mIsInitialized = new AtomicBoolean(false);
        this.mEnableCpuLimitOptSwitch = new AtomicBoolean(false);
        this.mCpuLimitSceneRecognize = new CpuLimitSceneRecognize();
        this.mCallBackHandler = new AppCpuLimitCallBackHandler();
        this.mHandler = new AwareAppCpuLimitMngHanlder();
    }

    public static AwareAppCpuLimitMng getInstance() {
        AwareAppCpuLimitMng awareAppCpuLimitMng;
        synchronized (LOCK) {
            if (sAwareAppCpuLimitMng == null) {
                sAwareAppCpuLimitMng = new AwareAppCpuLimitMng();
            }
            awareAppCpuLimitMng = sAwareAppCpuLimitMng;
        }
        return awareAppCpuLimitMng;
    }

    public static boolean isCpuLimitEnabled() {
        return sCpuLimitEnabled.get();
    }

    private AwareConfig getAwareCustConfig(String featureName, String configName) {
        if (featureName == null || featureName.isEmpty() || configName == null || configName.isEmpty()) {
            return null;
        }
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getCustConfig(awareService, featureName, configName);
            }
            AwareLog.e(TAG, "can not find service awareService!");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "CpuFeature getAwareCustConfig RemoteException");
            return null;
        }
    }

    public static void setVersion(int realVersion) {
        sRealVersion = realVersion;
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            registerAwareSceneRecognize();
            MultiTaskManagerService mtmService = MultiTaskManagerService.self();
            if (mtmService != null) {
                DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_CPULIMIT, mtmService.context());
                AwareAppDefaultCpuLimit.getInstance().init(mtmService.context());
            }
            registerObserver();
            HwActivityTaskManagerAdapter.registerThirdPartyCallBack(this.mCallBackHandler);
            initCpuLimitConfig();
            if (sRealVersion >= 5) {
                AwareAppDefaultCpuLimit.getInstance().initCpuLimitOpt();
                if (AwareAppDefaultCpuLimit.getInstance().getInitResult()) {
                    this.mEnableCpuLimitOptSwitch.set(true);
                }
            }
            this.mIsInitialized.set(true);
        }
    }

    private void initCpuLimitConfig() {
        List<AwareConfig.SubItem> subItemList;
        AwareConfig configList = getAwareCustConfig(FEATURE_NAME, ITEM_CONFIG_NAME);
        if (configList == null) {
            AwareLog.d(TAG, "cpulimit config list is null!");
            return;
        }
        sCpuLimitEnabled.set(true);
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (!(item == null || (subItemList = item.getSubItemList()) == null)) {
                for (AwareConfig.SubItem subItem : subItemList) {
                    if (subItem != null && ITEM_NAME.equals(subItem.getName())) {
                        try {
                            this.mCpuLimitDuration = Integer.parseInt(subItem.getValue());
                        } catch (NumberFormatException e) {
                            AwareLog.e(TAG, "mCpuLimitDuration is not an Integer!");
                        }
                    }
                }
            }
        }
    }

    private void deInitialize() {
        synchronized (LOCK) {
            if (this.mIsInitialized.get()) {
                unregisterAwareSceneRecognize();
                AwareAppDefaultCpuLimit.getInstance().deInitDefaultFree();
                HwActivityTaskManagerAdapter.unregisterThirdPartyCallBack(this.mCallBackHandler);
                unregisterObserver();
                this.mCpuLimitDuration = DEFAULT_UN_CPU_LIMIT_DURATION;
                this.mEnableCpuLimitOptSwitch.set(false);
                AwareAppDefaultCpuLimit.getInstance().setCpuLimitOptEnable(false);
                this.mIsInitialized.set(false);
            }
        }
    }

    private void registerAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.registerStateCallback(this.mCpuLimitSceneRecognize, 1);
        }
    }

    private void unregisterAwareSceneRecognize() {
        AwareSceneRecognize recognize = AwareSceneRecognize.getInstance();
        if (recognize != null) {
            recognize.unregisterStateCallback(this.mCpuLimitSceneRecognize);
        }
    }

    public static void enable() {
        sEnabled = true;
        AwareAppCpuLimitMng awareAppCpuLimitMng = sAwareAppCpuLimitMng;
        if (awareAppCpuLimitMng != null) {
            awareAppCpuLimitMng.initialize();
            if (!isCpuLimitEnabled()) {
                sEnabled = false;
                sAwareAppCpuLimitMng.deInitialize();
            }
        }
    }

    public static void disable() {
        sEnabled = false;
        AwareAppCpuLimitMng awareAppCpuLimitMng = sAwareAppCpuLimitMng;
        if (awareAppCpuLimitMng != null) {
            awareAppCpuLimitMng.deInitialize();
        }
    }

    private class AwareAppCpuLimitMngHanlder extends Handler {
        private AwareAppCpuLimitMngHanlder() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (AwareAppCpuLimitMng.sEnabled) {
                int i = msg.what;
                if (i == 1) {
                    Bundle data = msg.getData();
                    if (data != null) {
                        AwareAppCpuLimitMng.this.mCurPkgName = data.getString("pkgName");
                        AwareAppCpuLimitMng.this.handleToCpuLimit(1);
                    }
                } else if (i == 2) {
                    Bundle data2 = msg.getData();
                    int uid = data2.getInt("uid");
                    AwareAppCpuLimitMng.this.handleToRemovePids(uid, data2.getInt("pid"));
                    AwareAppCpuLimitMng.this.mCurPkgName = InnerUtils.getPackageNameByUid(uid);
                } else if (i == 3) {
                    AwareAppCpuLimitMng.this.handleToUnCpuLimit();
                } else if (i != 4) {
                    if (i == 5 && AwareAppCpuLimitMng.this.mIsCpuLimited.get()) {
                        AwareAppCpuLimitMng.this.mHandler.removeMessages(3);
                        AwareAppCpuLimitMng.this.sendDelayMsg(3, 1000);
                    }
                } else if (!AwareAppCpuLimitMng.this.mIsMultiWin.get()) {
                    AwareAppCpuLimitMng.this.handleToCpuLimit(4);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDelayMsg(int msgType, int duration) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = msgType;
        this.mHandler.sendMessageDelayed(msg, (long) duration);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleToCpuLimit(int msgType) {
        AwareAppDefaultCpuLimit.getInstance().doLimitCpu(this.mCurPkgName, AppMngConstant.AppCpuLimitSource.CPULIMIT);
        if (!this.mIsCpuLimited.get()) {
            this.mIsCpuLimited.set(true);
        } else {
            this.mHandler.removeMessages(3);
        }
        if (msgType != 4) {
            sendDelayMsg(3, this.mCpuLimitDuration);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleToUnCpuLimit() {
        if (this.mIsCpuLimited.get()) {
            AwareAppDefaultCpuLimit.getInstance().doUnLimitCpu();
            this.mIsCpuLimited.set(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleToRemovePids(int uid, int pid) {
        if (this.mIsCpuLimited.get()) {
            AwareAppDefaultCpuLimit.getInstance().doRemoveCpuPids(uid, pid, true);
        }
    }

    /* access modifiers changed from: private */
    public class CpuLimitSceneRecognize implements AwareSceneRecognize.IAwareSceneRecCallback {
        private CpuLimitSceneRecognize() {
        }

        @Override // com.android.server.rms.iaware.appmng.AwareSceneRecognize.IAwareSceneRecCallback
        public void onStateChanged(int sceneType, int eventType, String pkgName) {
            Message msg = AwareAppCpuLimitMng.this.mHandler.obtainMessage();
            msg.what = 0;
            if (eventType == 1) {
                if (sceneType == 2) {
                    msg.what = 4;
                    if (AwareAppCpuLimitMng.this.mEnableCpuLimitOptSwitch.get() && !AwareAppCpuLimitMng.this.mIsMultiWin.get()) {
                        AwareAppCpuLimitMng.this.handleToCpuLimit(4);
                        return;
                    }
                } else if (sceneType == 4) {
                    msg.what = 1;
                    if (AwareAppCpuLimitMng.this.mEnableCpuLimitOptSwitch.get()) {
                        AwareAppCpuLimitMng.this.mCurPkgName = pkgName;
                        AwareAppCpuLimitMng.this.handleToCpuLimit(1);
                        return;
                    }
                }
            } else if (sceneType == 2) {
                msg.what = 5;
            }
            if (msg.what != 0) {
                Bundle data = new Bundle();
                data.putString("pkgName", pkgName);
                msg.setData(data);
                AwareAppCpuLimitMng.this.mHandler.sendMessage(msg);
            }
        }
    }

    public void report(int eventId, Bundle bundleArgs) {
        if (sEnabled && bundleArgs != null) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            if (eventId == 1 || eventId == 2) {
                int callerPid = bundleArgs.getInt("callPid");
                int targetUid = bundleArgs.getInt("tgtUid");
                if (targetUid >= 0 && !AwareAppDefaultCpuLimit.getInstance().isPidLimited(callerPid)) {
                    if (targetUid != 1000) {
                        handleToRemovePids(targetUid, 0);
                        return;
                    }
                    handleToRemovePids(targetUid, AwareAppAssociate.getInstance().getPidByNameAndUid(bundleArgs.getString("tgtProcName"), targetUid));
                }
            } else if (eventId == 34) {
                handleToCpuLimit(6);
            }
        }
    }

    private void registerObserver() {
        AwareCallback.getInstance().registerProcessObserver(this.mCpuLimitObserver);
    }

    private void unregisterObserver() {
        AwareCallback.getInstance().unregisterProcessObserver(this.mCpuLimitObserver);
    }

    /* access modifiers changed from: package-private */
    public class AppCpuLimitObserver extends IProcessObserverEx {
        AppCpuLimitObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean fgActivities) {
            if (fgActivities && AwareAppCpuLimitMng.sEnabled) {
                Message msg = AwareAppCpuLimitMng.this.mHandler.obtainMessage();
                msg.what = 2;
                Bundle data = msg.getData();
                data.putInt("uid", uid);
                data.putInt("pid", pid);
                AwareAppCpuLimitMng.this.mHandler.sendMessage(msg);
            }
        }

        public void onProcessDied(int pid, int uid) {
            if (AwareAppCpuLimitMng.sEnabled && AwareAppCpuLimitMng.this.mIsCpuLimited.get()) {
                AwareAppDefaultCpuLimit.getInstance().doRemoveCpuPids(uid, pid, false);
            }
        }
    }

    /* access modifiers changed from: private */
    public class AppCpuLimitCallBackHandler extends IMWThirdpartyCallbackEx {
        private AppCpuLimitCallBackHandler() {
        }

        public void onModeChanged(boolean status) {
            AwareAppCpuLimitMng.this.mIsMultiWin.set(status);
            if (!AwareAppCpuLimitMng.this.mIsMultiWin.get()) {
                AwareAppCpuLimitMng.this.mCurPkgName = "";
            }
            AwareLog.d(AwareAppCpuLimitMng.TAG, "mIsMultiWin:" + AwareAppCpuLimitMng.this.mIsMultiWin.get());
        }

        public void onZoneChanged() {
        }

        public void onSizeChanged() {
        }
    }
}
