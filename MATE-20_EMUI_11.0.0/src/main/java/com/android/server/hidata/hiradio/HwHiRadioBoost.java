package com.android.server.hidata.hiradio;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.huawei.android.bastet.IBastetManager;
import huawei.android.net.hwmplink.MpLinkCommonUtils;

public class HwHiRadioBoost {
    private static final String BASTET_SERVICE = "BastetService";
    public static final int BSR_ACCELERATE_ENABLED = 2;
    public static final int DATA_ACCELERATE_ENABLED = 1;
    public static final int DATA_SEND_TO_HIDATA_HIRADIO_RESULT = 1;
    public static final int HIDATA_APPSTATE_NOTIFY_DSBOOSTER = 305;
    public static final int HIREADIO_BOOST_ACTION_BG_OR_FG = 512;
    public static final int HIREADIO_BOOST_ACTION_ENABLE = 256;
    public static final int NO_ACTION_ENABLED = 0;
    private static final String TAG = "HwHiRadioBoost";
    private static HwHiRadioBoost mHwHiRadioBoost;
    private IHwCommBoosterServiceManager bm = null;
    private IBinder mBastetService;
    private Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.server.hidata.hiradio.HwHiRadioBoost.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            HwArbitrationCommonUtils.logE(HwHiRadioBoost.TAG, false, "Bastet service has died!", new Object[0]);
            synchronized (HwHiRadioBoost.this) {
                if (HwHiRadioBoost.this.mBastetService != null) {
                    HwHiRadioBoost.this.mBastetService.unlinkToDeath(this, 0);
                    HwHiRadioBoost.this.mBastetService = null;
                    HwHiRadioBoost.this.mIBastetManager = null;
                }
            }
        }
    };
    private IHiRadioBoostCallback mHiRadioBoostCallback;
    private IBastetManager mIBastetManager;
    private IHwCommBoosterCallback mIHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class com.android.server.hidata.hiradio.HwHiRadioBoost.AnonymousClass2 */

        public void callBack(int type, Bundle b) throws RemoteException {
            int uid;
            int scene;
            int result;
            int reason;
            HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, false, "HwCommBoosterCallback, type = %{public}d", Integer.valueOf(type));
            if (b != null) {
                if (type != 1) {
                    HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, false, "unhandled type:%{public}d", Integer.valueOf(type));
                    return;
                }
                HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, false, "DATA_SEND_TO_HIDATA_HIRADIO_RESULT", new Object[0]);
                synchronized (HwHiRadioBoost.this.mLock) {
                    uid = b.getInt("appUid");
                    scene = b.getInt("scene");
                    result = b.getInt("result");
                    reason = b.getInt("reason");
                }
                HwArbitrationCommonUtils.logD(HwHiRadioBoost.TAG, false, "IHwCommBoosterCallback:uid %{public}d, scene %{public}d, result %{public}d, reason %{public}d", Integer.valueOf(uid), Integer.valueOf(scene), Integer.valueOf(result), Integer.valueOf(reason));
                if (HwHiRadioBoost.this.mHiRadioBoostCallback != null) {
                    HwHiRadioBoost.this.mHiRadioBoostCallback.LTEto3GResult(uid, result, reason);
                }
            }
        }
    };
    private final Object mLock = new Object();

    public static HwHiRadioBoost createInstance(Context context) {
        if (mHwHiRadioBoost == null) {
            mHwHiRadioBoost = new HwHiRadioBoost(context);
        }
        return mHwHiRadioBoost;
    }

    private HwHiRadioBoost(Context context) {
        this.mContext = context;
        HwArbitrationCommonUtils.logD(TAG, false, "init mHwHiRadioBoost completed, isConnected = %{public}s", String.valueOf(getBastetService()));
    }

    private boolean getBastetService() {
        synchronized (this) {
            if (this.mBastetService == null) {
                this.mBastetService = ServiceManager.getService(BASTET_SERVICE);
                if (this.mBastetService == null) {
                    HwArbitrationCommonUtils.logE(TAG, false, "Failed to get bastet service!", new Object[0]);
                    return false;
                }
                try {
                    this.mBastetService.linkToDeath(this.mDeathRecipient, 0);
                    this.mIBastetManager = IBastetManager.Stub.asInterface(this.mBastetService);
                } catch (RemoteException e) {
                    HwArbitrationCommonUtils.logE(TAG, false, "Exception happened in function getBastetService", new Object[0]);
                }
            }
            return true;
        }
    }

    private boolean isDataAccelerateEnabled(int actions) {
        return (actions & 1) == 1;
    }

    private boolean isBSRAccelerateEnabled(int actions) {
        return (actions & 2) == 2;
    }

    public void startOptimizeActionsForApp(HwAPPStateInfo AppInfo, int actions) {
        if (AppInfo == null || actions <= 0) {
            HwArbitrationCommonUtils.logE(TAG, false, "Enter startOptimizeActionsForApp: AppInfo is null, actions is %{public}d", Integer.valueOf(actions));
            return;
        }
        HwArbitrationCommonUtils.logE(TAG, false, "startOptimizeActionsForApp Enter: AppInfo.uid is %{public}dAppInfo.mAppState is %{public}d actions is %{public}d", Integer.valueOf(AppInfo.mAppUID), Integer.valueOf(AppInfo.mAppState), Integer.valueOf(actions));
        if (isDataAccelerateEnabled(actions) || isBSRAccelerateEnabled(actions)) {
            configDataAccelerate(AppInfo.mAppUID, true, true, (actions & 1) | (actions & 2));
        }
    }

    public void stopOptimizedActionsForApp(HwAPPStateInfo AppInfo, boolean fgBgState, int actions) {
        if (AppInfo == null || actions < 0) {
            HwArbitrationCommonUtils.logE(TAG, false, "Enter stopOptimizedActionsForApp: AppInfo is null, actions is %{public}d", Integer.valueOf(actions));
            return;
        }
        HwArbitrationCommonUtils.logE(TAG, false, "stopOptimizedActionsForApp Enter: AppInfo.uid is %{public}dAppInfo.mAppState is %{public}d actions is %{public}d", Integer.valueOf(AppInfo.mAppUID), Integer.valueOf(AppInfo.mAppState), Integer.valueOf(actions));
        if (isDataAccelerateEnabled(actions) || isBSRAccelerateEnabled(actions)) {
            configDataAccelerate(AppInfo.mAppUID, fgBgState, false, (actions & 1) | (actions & 2));
        }
    }

    private void configDataAccelerate(int uid, boolean fgBgState, boolean enable, int action) {
        HwArbitrationCommonUtils.logE(TAG, false, "configDataAccelerate Enter, uid is %{public}d action is %{public}d", Integer.valueOf(uid), Integer.valueOf(action));
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
                    HwArbitrationCommonUtils.logD(TAG, false, "configDataAccelerate success", new Object[0]);
                }
            }
        } catch (RemoteException e) {
            HwArbitrationCommonUtils.logD(TAG, false, "Exception happened in function configDataAccelerate", new Object[0]);
        }
    }

    private void registerBoosterCallback() {
        HwArbitrationCommonUtils.logD(TAG, false, "registerBoosterCallback enter", new Object[0]);
        IHwCommBoosterServiceManager iHwCommBoosterServiceManager = this.bm;
        if (iHwCommBoosterServiceManager != null) {
            int ret = iHwCommBoosterServiceManager.registerCallBack("com.android.server.hidata.hiradio", this.mIHwCommBoosterCallback);
            if (ret != 0) {
                HwArbitrationCommonUtils.logD(TAG, false, "registerBoosterCallback:registerCallBack hiradio failed, ret=%{public}d", Integer.valueOf(ret));
                return;
            }
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "registerBoosterCallback:null HwCommBoosterServiceManager", new Object[0]);
    }

    public void initCommBoosterManager() {
        this.bm = HwFrameworkFactory.getHwCommBoosterServiceManager();
        registerBoosterCallback();
        HwArbitrationCommonUtils.logD(TAG, false, "initCommBoosterManager completed", new Object[0]);
    }

    public void registerHiRadioCallback(IHiRadioBoostCallback callback) {
        this.mHiRadioBoostCallback = callback;
    }

    public void BrainAppStateNotifyDSBooster(HwAPPStateInfo appInfo, int scene, int status) {
        if (appInfo != null) {
            if (!MpLinkCommonUtils.isMpLinkEnabled(this.mContext)) {
                HwArbitrationCommonUtils.logD(TAG, false, "WLAN+ off", new Object[0]);
                return;
            }
            HwArbitrationCommonUtils.logD(TAG, false, "on BrainAppStateNotifyDSBooster: uid = %{public}d, type = %{public}d, scene = %{public}d, status = %{public}d", Integer.valueOf(appInfo.mAppUID), Integer.valueOf(appInfo.mScenceId), Integer.valueOf(scene), Integer.valueOf(status));
            if (this.bm == null) {
                HwArbitrationCommonUtils.logD(TAG, false, "BrainAppStateNotifyDSBooster:null HwCommBoosterServiceManager", new Object[0]);
                return;
            }
            Bundle data = new Bundle();
            data.putInt("appUid", appInfo.mAppUID);
            data.putInt("type", appInfo.mScenceId);
            data.putInt("scene", scene);
            data.putInt("status", status);
            int ret = this.bm.reportBoosterPara("com.android.server.hidata.hiradio", (int) HIDATA_APPSTATE_NOTIFY_DSBOOSTER, data);
            if (ret != 0) {
                HwArbitrationCommonUtils.logD(TAG, false, "reportBoosterPara failed, ret=%{public}d", Integer.valueOf(ret));
            }
        }
    }
}
