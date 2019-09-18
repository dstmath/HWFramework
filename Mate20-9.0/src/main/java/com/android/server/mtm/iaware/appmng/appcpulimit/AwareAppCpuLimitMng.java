package com.android.server.mtm.iaware.appmng.appcpulimit;

import android.app.ActivityManager;
import android.app.IProcessObserver;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.huawei.android.app.HwActivityManager;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppCpuLimitMng {
    private static final String CAMERA_ITEM_NAME = "camera_cpulimit_duration";
    private static final int DEFAULT_UNCPULIMIT_DURATION = 3000;
    private static final String FEATURE_NAME = "appmng_feature";
    private static final String ITEM_CONFIG_NAME = "cpulimit";
    private static final String ITEM_NAME = "cpulimit_duration";
    private static final String KEY_BUNDLE_PACKAGENAME = "pkgName";
    private static final String KEY_BUNDLE_PID = "pid";
    private static final String KEY_BUNDLE_UID = "uid";
    private static final int MSG_ACTIVITY_STARTING = 1;
    private static final int MSG_APP_SHOW_INPUTMETHOD = 6;
    private static final int MSG_APP_SLIPPING = 4;
    private static final int MSG_APP_SLIP_END = 5;
    private static final int MSG_FG_ACTIVITIES_CHANGED = 2;
    private static final int MSG_UNCPULIMIT_DURATION = 3;
    private static final String PACKAGE_CAMERA = "com.huawei.camera";
    private static final int SLIP_UNCPULIMIT_DURATION = 1000;
    private static final String TAG = "AwareAppCpuLimitMng";
    private static AwareAppCpuLimitMng mAwareAppCpuLimitMng = null;
    private static AtomicBoolean mCpuLimitEnabled = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public static boolean mEnabled = false;
    /* access modifiers changed from: private */
    public AtomicBoolean isCpuLimited;
    private AppCpuLimitCallBackHandler mCallBackHandler;
    private int mCameraCpuLimitDuration;
    private int mCpuLimitDuration;
    private AppCpuLimitObserver mCpuLimitObserver;
    private CpuLimitSceneRecognize mCpuLimitSceneRecognize;
    /* access modifiers changed from: private */
    public String mCurPkgName;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private AtomicBoolean mIsInitialized;
    /* access modifiers changed from: private */
    public AtomicBoolean mIsMultiWin;

    private class AppCpuLimitCallBackHandler extends IMWThirdpartyCallback.Stub {
        private AppCpuLimitCallBackHandler() {
        }

        public void onModeChanged(boolean aMWStatus) {
            AwareAppCpuLimitMng.this.mIsMultiWin.set(aMWStatus);
            if (!AwareAppCpuLimitMng.this.mIsMultiWin.get()) {
                String unused = AwareAppCpuLimitMng.this.mCurPkgName = "";
            }
            AwareLog.d(AwareAppCpuLimitMng.TAG, "mIsMultiWin:" + AwareAppCpuLimitMng.this.mIsMultiWin.get());
        }

        public void onZoneChanged() {
        }

        public void onSizeChanged() {
        }
    }

    class AppCpuLimitObserver extends IProcessObserver.Stub {
        AppCpuLimitObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities && AwareAppCpuLimitMng.mEnabled) {
                Message msg = AwareAppCpuLimitMng.this.mHandler.obtainMessage();
                msg.what = 2;
                Bundle data = msg.getData();
                data.putInt("uid", uid);
                data.putInt("pid", pid);
                AwareAppCpuLimitMng.this.mHandler.sendMessage(msg);
            }
        }

        public void onProcessDied(int pid, int uid) {
            AwareAppCpuLimitMng.this.handleToRemovePids(uid, pid);
        }
    }

    private class AwareAppCpuLimitMngHanlder extends Handler {
        private AwareAppCpuLimitMngHanlder() {
        }

        public void handleMessage(Message msg) {
            if (AwareAppCpuLimitMng.mEnabled) {
                switch (msg.what) {
                    case 1:
                        String unused = AwareAppCpuLimitMng.this.mCurPkgName = msg.getData().getString("pkgName");
                        AwareAppCpuLimitMng.this.handleToCpuLimit(1);
                        break;
                    case 2:
                        Bundle data = msg.getData();
                        int uid = data.getInt("uid");
                        AwareAppCpuLimitMng.this.handleToRemovePids(uid, data.getInt("pid"));
                        String unused2 = AwareAppCpuLimitMng.this.mCurPkgName = InnerUtils.getPackageNameByUid(uid);
                        break;
                    case 3:
                        AwareAppCpuLimitMng.this.handleToUnCpuLimit();
                        break;
                    case 4:
                        if (!AwareAppCpuLimitMng.this.mIsMultiWin.get()) {
                            AwareAppCpuLimitMng.this.handleToCpuLimit(4);
                            break;
                        }
                        break;
                    case 5:
                        if (AwareAppCpuLimitMng.this.isCpuLimited.get()) {
                            AwareAppCpuLimitMng.this.mHandler.removeMessages(3);
                            AwareAppCpuLimitMng.this.sendDelayMsg(3, 1000);
                            break;
                        }
                        break;
                }
            }
        }
    }

    private class CpuLimitSceneRecognize implements AwareSceneRecognize.IAwareSceneRecCallback {
        private CpuLimitSceneRecognize() {
        }

        public void onStateChanged(int sceneType, int eventType, String pkgName) {
            Message msg = AwareAppCpuLimitMng.this.mHandler.obtainMessage();
            msg.what = 0;
            if (eventType == 1) {
                if (sceneType == 2) {
                    msg.what = 4;
                } else if (sceneType == 4) {
                    msg.what = 1;
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

    public static synchronized AwareAppCpuLimitMng getInstance() {
        AwareAppCpuLimitMng awareAppCpuLimitMng;
        synchronized (AwareAppCpuLimitMng.class) {
            if (mAwareAppCpuLimitMng == null) {
                mAwareAppCpuLimitMng = new AwareAppCpuLimitMng();
            }
            awareAppCpuLimitMng = mAwareAppCpuLimitMng;
        }
        return awareAppCpuLimitMng;
    }

    private AwareAppCpuLimitMng() {
        this.mCpuLimitObserver = new AppCpuLimitObserver();
        this.isCpuLimited = new AtomicBoolean(false);
        this.mIsMultiWin = new AtomicBoolean(false);
        this.mHandler = null;
        this.mCurPkgName = "";
        this.mCpuLimitDuration = 3000;
        this.mCameraCpuLimitDuration = 3000;
        this.mIsInitialized = new AtomicBoolean(false);
        this.mCpuLimitSceneRecognize = new CpuLimitSceneRecognize();
        this.mCallBackHandler = new AppCpuLimitCallBackHandler();
        this.mHandler = new AwareAppCpuLimitMngHanlder();
    }

    public static boolean isCpuLimitEnabled() {
        return mCpuLimitEnabled.get();
    }

    private AwareConfig getAwareCustConfig(String featureName, String configName) {
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
            AwareLog.e(TAG, "CpuFeature getAwareCustConfig RemoteException");
        }
        return configList;
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            registerAwareSceneRecognize();
            MultiTaskManagerService mMtmService = MultiTaskManagerService.self();
            if (mMtmService != null) {
                DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.APP_CPULIMIT, mMtmService.context());
                AwareAppDefaultCpuLimit.getInstance().init(mMtmService.context());
            }
            registerObserver();
            HwActivityManager.registerThirdPartyCallBack(this.mCallBackHandler);
            AwareConfig configList = getAwareCustConfig(FEATURE_NAME, ITEM_CONFIG_NAME);
            if (configList != null) {
                mCpuLimitEnabled.set(true);
                for (AwareConfig.Item item : configList.getConfigList()) {
                    if (item == null) {
                        AwareLog.w(TAG, "getAwareCustConfig failed, item is empty");
                    } else {
                        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
                        if (subItemList != null) {
                            boolean isCameraDurationConfiged = false;
                            for (AwareConfig.SubItem subItem : subItemList) {
                                if (subItem != null) {
                                    if (ITEM_NAME.equals(subItem.getName())) {
                                        try {
                                            this.mCpuLimitDuration = Integer.parseInt(subItem.getValue());
                                        } catch (NumberFormatException e) {
                                            AwareLog.e(TAG, "mCpuLimitDuration is not an Integer!");
                                        }
                                    } else if (CAMERA_ITEM_NAME.equals(subItem.getName())) {
                                        try {
                                            this.mCameraCpuLimitDuration = Integer.parseInt(subItem.getValue());
                                            isCameraDurationConfiged = true;
                                        } catch (NumberFormatException e2) {
                                            AwareLog.e(TAG, "mCameraCpuLimitDuration is not an Integer!");
                                        }
                                    } else {
                                        AwareLog.w(TAG, "cpulimit custconfig got undefined subItem");
                                    }
                                }
                            }
                            if (!isCameraDurationConfiged) {
                                this.mCameraCpuLimitDuration = this.mCpuLimitDuration;
                            }
                        }
                    }
                }
            }
            this.mIsInitialized.set(true);
        }
    }

    private synchronized void deInitialize() {
        if (this.mIsInitialized.get()) {
            unregisterAwareSceneRecognize();
            AwareAppDefaultCpuLimit.getInstance().deInitDefaultFree();
            HwActivityManager.unregisterThirdPartyCallBack(this.mCallBackHandler);
            unregisterObserver();
            this.mCpuLimitDuration = 3000;
            this.mCameraCpuLimitDuration = 3000;
            this.mIsInitialized.set(false);
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
        mEnabled = true;
        if (mAwareAppCpuLimitMng != null) {
            mAwareAppCpuLimitMng.initialize();
            if (!isCpuLimitEnabled()) {
                mEnabled = false;
                mAwareAppCpuLimitMng.deInitialize();
            }
        }
    }

    public static void disable() {
        mEnabled = false;
        if (mAwareAppCpuLimitMng != null) {
            mAwareAppCpuLimitMng.deInitialize();
        }
    }

    /* access modifiers changed from: private */
    public void sendDelayMsg(int msgType, int duration) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = msgType;
        this.mHandler.sendMessageDelayed(msg, (long) duration);
    }

    /* access modifiers changed from: private */
    public void handleToCpuLimit(int msgType) {
        AwareAppDefaultCpuLimit.getInstance().doLimitCpu(this.mCurPkgName, AppMngConstant.AppCpuLimitSource.CPULIMIT);
        if (!this.isCpuLimited.get()) {
            this.isCpuLimited.set(true);
        } else {
            this.mHandler.removeMessages(3);
        }
        if (msgType == 4) {
            return;
        }
        if ("com.huawei.camera".equals(this.mCurPkgName)) {
            sendDelayMsg(3, this.mCameraCpuLimitDuration);
        } else {
            sendDelayMsg(3, this.mCpuLimitDuration);
        }
    }

    /* access modifiers changed from: private */
    public void handleToUnCpuLimit() {
        if (this.isCpuLimited.get()) {
            AwareAppDefaultCpuLimit.getInstance().doUnLimitCPU();
            this.isCpuLimited.set(false);
        }
    }

    /* access modifiers changed from: private */
    public void handleToRemovePids(int uid, int pid) {
        if (this.isCpuLimited.get()) {
            AwareAppDefaultCpuLimit.getInstance().doRemoveCpuPids(uid, pid);
        }
    }

    public void report(int eventId, Bundle bundleArgs) {
        if (mEnabled && bundleArgs != null) {
            if (!this.mIsInitialized.get()) {
                initialize();
            }
            if (eventId != 34) {
                switch (eventId) {
                    case 1:
                    case 2:
                        int callerPid = bundleArgs.getInt("callPid");
                        int targetUid = bundleArgs.getInt("tgtUid");
                        if (targetUid >= 0 && !AwareAppDefaultCpuLimit.getInstance().isPidLimited(callerPid)) {
                            if (targetUid == 1000) {
                                handleToRemovePids(targetUid, AwareAppAssociate.getInstance().getPidByNameAndUid(bundleArgs.getString("tgtProcName"), targetUid));
                                break;
                            } else {
                                handleToRemovePids(targetUid, 0);
                                break;
                            }
                        }
                }
            } else {
                handleToCpuLimit(6);
            }
        }
    }

    private void registerObserver() {
        try {
            ActivityManager.getService().registerProcessObserver(this.mCpuLimitObserver);
        } catch (RemoteException e) {
            AwareLog.w(TAG, "register process observer failed");
        }
    }

    private void unregisterObserver() {
        try {
            ActivityManager.getService().unregisterProcessObserver(this.mCpuLimitObserver);
        } catch (RemoteException e) {
            AwareLog.w(TAG, "unregister process observer failed");
        }
    }
}
