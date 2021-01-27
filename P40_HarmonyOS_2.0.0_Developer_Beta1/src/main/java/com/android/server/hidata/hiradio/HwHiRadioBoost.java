package com.android.server.hidata.hiradio;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.server.hidata.appqoe.HwAppStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.huawei.android.bastet.IBastetManager;
import huawei.android.net.hwmplink.MpLinkCommonUtils;

public class HwHiRadioBoost {
    private static final String BASTET_SERVICE = "BastetService";
    public static final int DATA_SEND_TO_HIDATA_HIRADIO_RESULT = 1;
    public static final int HIDATA_APP_STATE_NOTIFY_DSBOOSTER = 305;
    public static final int NO_ACTION_ENABLED = 0;
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwHiRadioBoost.class.getSimpleName());
    private static HwHiRadioBoost sHwHiRadioBoost;
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
                    HwHiRadioBoost.this.mHiRadioBoostCallback.lteTo3gResult(uid, result, reason);
                }
            }
        }
    };
    private final Object mLock = new Object();

    public static HwHiRadioBoost createInstance(Context context) {
        if (sHwHiRadioBoost == null) {
            sHwHiRadioBoost = new HwHiRadioBoost(context);
        }
        return sHwHiRadioBoost;
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

    public void brainAppStateNotifyDSBooster(HwAppStateInfo appInfo, int scenes, int status) {
        if (appInfo != null) {
            if (!MpLinkCommonUtils.isMpLinkEnabled(this.mContext)) {
                HwArbitrationCommonUtils.logD(TAG, false, "WLAN+ off", new Object[0]);
                return;
            }
            HwArbitrationCommonUtils.logD(TAG, false, "on BrainAppStateNotifyDSBooster: uid = %{public}d, type = %{public}d, scenes = %{public}d, status = %{public}d", Integer.valueOf(appInfo.mAppUid), Integer.valueOf(appInfo.mScenesId), Integer.valueOf(scenes), Integer.valueOf(status));
            if (this.bm == null) {
                HwArbitrationCommonUtils.logD(TAG, false, "BrainAppStateNotifyDSBooster:null HwCommBoosterServiceManager", new Object[0]);
                return;
            }
            Bundle data = new Bundle();
            data.putInt("appUid", appInfo.mAppUid);
            data.putInt("type", appInfo.mScenesId);
            data.putInt("scene", scenes);
            data.putInt("status", status);
            int ret = this.bm.reportBoosterPara("com.android.server.hidata.hiradio", (int) HIDATA_APP_STATE_NOTIFY_DSBOOSTER, data);
            if (ret != 0) {
                HwArbitrationCommonUtils.logD(TAG, false, "reportBoosterPara failed, ret=%{public}d", Integer.valueOf(ret));
            }
        }
    }
}
