package com.android.server.hidata.hiradio;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.huawei.android.bastet.IBastetManager;
import huawei.android.net.hwmplink.MpLinkCommonUtils;

public class HwHiRadioBoost {
    private static final int APP_STATE_PERIOD_UPDATE_TIMER = 300000;
    private static final String BASTET_SERVICE = "BastetService";
    public static final int BSR_ACCELERATE_ENABLED = 2;
    public static final int DATA_ACCELERATE_ENABLED = 1;
    private static final int DATA_SEND_TO_HIDATA_HIRADIO_RESULT = 1;
    private static final int HIDATA_APPSTATE_NOTIFY_DSBOOSTER = 305;
    public static final int HIREADIO_BOOST_ACTION_BG_OR_FG = 512;
    public static final int HIREADIO_BOOST_ACTION_ENABLE = 256;
    private static final int MSG_APP_STALL = 1;
    private static final int MSG_APP_STATE_CHANGE = 0;
    private static final int MSG_PERIOD_UPDATE_APP_STATE = 2;
    public static final int NO_ACTION_ENABLED = 0;
    private static final long STALL_TIME_INTERNAL = 10000;
    private static final String TAG = "HwHiRadioBoost";
    private static HwHiRadioBoost mHwHiRadioBoost;
    /* access modifiers changed from: private */
    public Bundle appState = null;
    private IHwCommBoosterServiceManager bm = null;
    /* access modifiers changed from: private */
    public long currentStallTime = -1;
    /* access modifiers changed from: private */
    public long lastStallTime = -1;
    /* access modifiers changed from: private */
    public IBinder mBastetService;
    private Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        public void binderDied() {
            HwArbitrationCommonUtils.logE(HwHiRadioBoost.TAG, "Bastet service has died!");
            synchronized (HwHiRadioBoost.this) {
                if (HwHiRadioBoost.this.mBastetService != null) {
                    HwHiRadioBoost.this.mBastetService.unlinkToDeath(this, 0);
                    IBinder unused = HwHiRadioBoost.this.mBastetService = null;
                    IBastetManager unused2 = HwHiRadioBoost.this.mIBastetManager = null;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public IHiRadioBoostCallback mHiRadioBoostCallback;
    /* access modifiers changed from: private */
    public Handler mHiRadioHandler;
    /* access modifiers changed from: private */
    public IBastetManager mIBastetManager;
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        public void callBack(int type, Bundle b) throws RemoteException {
            int uid;
            int scene;
            int result;
            int reason;
            HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, "HwCommBoosterCallback, type = " + type);
            if (b != null) {
                boolean z = true;
                if (type != 1) {
                    HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, "unhandled type:" + type);
                } else {
                    HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, "DATA_SEND_TO_HIDATA_HIRADIO_RESULT");
                    synchronized (HwHiRadioBoost.this.mLock) {
                        uid = b.getInt("appUid");
                        scene = b.getInt(MemoryConstant.MEM_POLICY_SCENE);
                        result = b.getInt("result");
                        reason = b.getInt("reason");
                    }
                    HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, "IHwCommBoosterCallback:uid " + uid + ", scene " + scene + ", result " + result + ", reason " + reason);
                    if (HwHiRadioBoost.this.mHiRadioBoostCallback != null) {
                        HwHiRadioBoost.this.mHiRadioBoostCallback.LTEto3GResult(uid, result, reason);
                    }
                    HwHiRadioBoost hwHiRadioBoost = HwHiRadioBoost.this;
                    if (result != 0) {
                        z = false;
                    }
                    hwHiRadioBoost.setSwitchTo3GFlag(z);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    /* access modifiers changed from: private */
    public boolean switchTo3GFlag = false;

    public static HwHiRadioBoost createInstance(Context context) {
        if (mHwHiRadioBoost == null) {
            mHwHiRadioBoost = new HwHiRadioBoost(context);
        }
        return mHwHiRadioBoost;
    }

    private HwHiRadioBoost(Context context) {
        this.mContext = context;
        boolean isConnected = getBastetService();
        HwArbitrationCommonUtils.logD(TAG, "init mHwHiRadioBoost completed, isConnected = " + isConnected);
        initHiRadio4gSwitchto3gHandler();
    }

    private boolean getBastetService() {
        synchronized (this) {
            if (this.mBastetService == null) {
                this.mBastetService = ServiceManager.getService(BASTET_SERVICE);
                if (this.mBastetService == null) {
                    HwArbitrationCommonUtils.logE(TAG, "Failed to get bastet service!");
                    return false;
                }
                try {
                    this.mBastetService.linkToDeath(this.mDeathRecipient, 0);
                    this.mIBastetManager = IBastetManager.Stub.asInterface(this.mBastetService);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private boolean isDataAccelerateEnabled(int actions) {
        return (actions & 1) == 1;
    }

    private boolean isBSRAccelerateEnabled(int actions) {
        return (actions & 2) == 2;
    }

    public void startOptimizeActionsForApp(HwAPPStateInfo AppInfo, int actions) {
        if (AppInfo == null || actions <= 0) {
            HwArbitrationCommonUtils.logE(TAG, "Enter startOptimizeActionsForApp: AppInfo is null, actions is " + actions);
            return;
        }
        HwArbitrationCommonUtils.logE(TAG, "startOptimizeActionsForApp Enter: AppInfo.uid is " + AppInfo.mAppUID + "AppInfo.mAppState is " + AppInfo.mAppState + "actions is " + actions);
        if (isDataAccelerateEnabled(actions) || isBSRAccelerateEnabled(actions)) {
            configDataAccelerate(AppInfo.mAppUID, true, true, (actions & 1) | (actions & 2));
        }
    }

    public void stopOptimizedActionsForApp(HwAPPStateInfo AppInfo, boolean fgBgState, int actions) {
        if (AppInfo == null || actions < 0) {
            HwArbitrationCommonUtils.logE(TAG, "Enter stopOptimizedActionsForApp: AppInfo is null, actions is " + actions);
            return;
        }
        HwArbitrationCommonUtils.logE(TAG, "stopOptimizedActionsForApp Enter: AppInfo.uid is " + AppInfo.mAppUID + "AppInfo.mAppState is " + AppInfo.mAppState + "actions is " + actions);
        if (isDataAccelerateEnabled(actions) || isBSRAccelerateEnabled(actions)) {
            configDataAccelerate(AppInfo.mAppUID, fgBgState, false, (actions & 1) | (actions & 2));
        }
    }

    private void configDataAccelerate(int uid, boolean fgBgState, boolean enable, int action) {
        HwArbitrationCommonUtils.logE(TAG, "configDataAccelerate Enter, uid is " + uid + "action is " + action);
        if (enable) {
            action += 256;
        }
        if (!fgBgState) {
            action += 512;
        }
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null && this.mIBastetManager.configDataAccelerate(uid, action) == 0) {
                    HwArbitrationCommonUtils.logD(TAG, "configDataAccelerate success");
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void registerBoosterCallback() {
        HwArbitrationCommonUtils.logD(TAG, "registerBoosterCallback enter");
        if (this.bm != null) {
            int ret = this.bm.registerCallBack("com.android.server.hidata.hiradio", this.mIHwCommBoosterCallback);
            if (ret != 0) {
                HwArbitrationCommonUtils.logD(TAG, "registerBoosterCallback:registerCallBack hiradio failed, ret=" + ret);
                return;
            }
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, "registerBoosterCallback:null HwCommBoosterServiceManager");
    }

    public void initCommBoosterManager() {
        this.bm = HwFrameworkFactory.getHwCommBoosterServiceManager();
        registerBoosterCallback();
        HwArbitrationCommonUtils.logD(TAG, "initCommBoosterManager completed");
    }

    public void registerHiRadioCallback(IHiRadioBoostCallback callback) {
        this.mHiRadioBoostCallback = callback;
    }

    public void BrainAppStateNotifyDSBooster(HwAPPStateInfo appInfo, int scene, int status) {
        if (appInfo != null && this.mHiRadioHandler != null) {
            if (!MpLinkCommonUtils.isMpLinkEnabled(this.mContext)) {
                HwArbitrationCommonUtils.logD(TAG, "WLAN+ off");
                return;
            }
            HwArbitrationCommonUtils.logD(TAG, "on BrainAppStateNotifyDSBooster: uid = " + appInfo.mAppUID + ", type = " + appInfo.mScenceId + ", scene = " + scene + ", status = " + status);
            Bundle data = new Bundle();
            data.putInt("appUid", appInfo.mAppUID);
            data.putInt(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, appInfo.mScenceId);
            data.putInt(MemoryConstant.MEM_POLICY_SCENE, scene);
            data.putInt("status", status);
            if (scene == 1) {
                this.mHiRadioHandler.sendMessage(this.mHiRadioHandler.obtainMessage(1, data));
            } else {
                this.mHiRadioHandler.sendMessage(this.mHiRadioHandler.obtainMessage(0, data));
            }
        }
    }

    private void initHiRadio4gSwitchto3gHandler() {
        HandlerThread handlerThread = new HandlerThread("HwHiRadio4gSwitchto3g_handler_thread");
        handlerThread.start();
        this.mHiRadioHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, "MSG_APP_STATE_CHANGE");
                        HwHiRadioBoost.this.handleAPPStateChange(msg);
                        return;
                    case 1:
                        HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, "MSG_APP_STALL");
                        long unused = HwHiRadioBoost.this.currentStallTime = SystemClock.elapsedRealtime();
                        if (-1 == HwHiRadioBoost.this.lastStallTime || HwHiRadioBoost.this.currentStallTime - HwHiRadioBoost.this.lastStallTime > 10000) {
                            HwHiRadioBoost.this.handleAPPStall(msg, 1);
                            long unused2 = HwHiRadioBoost.this.lastStallTime = HwHiRadioBoost.this.currentStallTime;
                        } else {
                            HwHiRadioBoost.this.handleAPPStall(msg, 2);
                            long unused3 = HwHiRadioBoost.this.lastStallTime = -1;
                        }
                        HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, "time between two consecutive stall:" + (HwHiRadioBoost.this.currentStallTime - HwHiRadioBoost.this.lastStallTime));
                        return;
                    case 2:
                        if (HwHiRadioBoost.this.switchTo3GFlag) {
                            HwHiRadioBoost.this.mHiRadioHandler.sendMessageDelayed(HwHiRadioBoost.this.mHiRadioHandler.obtainMessage(2), HwArbitrationDEFS.DelayTimeMillisB);
                            HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, "MSG_PERIOD_UPDATE_APP_STATE");
                        }
                        HwHiRadioBoost.this.NotifyDSBooster(HwHiRadioBoost.this.appState);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    /* access modifiers changed from: private */
    public void handleAPPStateChange(Message msg) {
        HwArbitrationCommonUtils.logD(TAG, "enter handleAPPStateChange");
        if (msg != null && msg.obj != null) {
            Bundle data = (Bundle) msg.obj;
            this.appState = data;
            if (this.mHiRadioHandler.hasMessages(2)) {
                HwArbitrationCommonUtils.logD(TAG, "restart MSG_PERIOD_UPDATE_APP_STATE");
                this.mHiRadioHandler.removeMessages(2);
                this.mHiRadioHandler.sendEmptyMessageDelayed(2, HwArbitrationDEFS.DelayTimeMillisB);
            }
            NotifyDSBooster(data);
        }
    }

    /* access modifiers changed from: private */
    public void handleAPPStall(Message msg, int stallGrade) {
        if (msg != null && msg.obj != null) {
            Bundle data = (Bundle) msg.obj;
            data.putInt(MemoryConstant.MEM_POLICY_SCENE, stallGrade);
            NotifyDSBooster(data);
        }
    }

    /* access modifiers changed from: private */
    public void NotifyDSBooster(Bundle bundle) {
        HwArbitrationCommonUtils.logD(TAG, "enter NotifyDSBooster");
        if (bundle == null) {
            HwArbitrationCommonUtils.logD(TAG, "bundle is null");
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, "appUid:" + bundle.getInt("appUid") + ", type:" + bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE) + ", scene:" + bundle.getInt(MemoryConstant.MEM_POLICY_SCENE) + ", status:" + bundle.getInt("status"));
        if (this.bm == null) {
            HwArbitrationCommonUtils.logD(TAG, "BrainAppStateNotifyDSBooster:null HwCommBoosterServiceManager");
            return;
        }
        int ret = this.bm.reportBoosterPara("com.android.server.hidata.hiradio", 305, bundle);
        if (ret != 0) {
            HwArbitrationCommonUtils.logD(TAG, "reportBoosterPara failed, ret=" + ret);
        }
    }

    public void setSwitchTo3GFlag(boolean is3G) {
        this.switchTo3GFlag = is3G;
        HwArbitrationCommonUtils.logD(TAG, "switchTo3GFlag:" + this.switchTo3GFlag);
        if (this.mHiRadioHandler == null) {
            return;
        }
        if (this.switchTo3GFlag) {
            this.mHiRadioHandler.sendEmptyMessageDelayed(2, HwArbitrationDEFS.DelayTimeMillisB);
            HwArbitrationCommonUtils.logD(TAG, "MSG_PERIOD_UPDATE_APP_STATE");
            return;
        }
        this.mHiRadioHandler.removeMessages(2);
        HwArbitrationCommonUtils.logD(TAG, "remove MSG_PERIOD_UPDATE_APP_STATE");
    }
}
