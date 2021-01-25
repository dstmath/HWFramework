package com.android.server.hidata.wavemapping.dataprovider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.appqoe.IHwAPPQoECallback;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.hidata.wavemapping.entity.HwWmpAppInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class HwWmpCallbackImpl implements IHwAPPQoECallback {
    private static final String KEY_APP_NAME = "APPNAME";
    private static final String KEY_QOE = "QOE";
    private static final String TAG = "HwWmpCallbackImpl";
    private static HwWmpCallbackImpl mHwWmpCallbackImpl;
    private HwAPPStateInfo appInfoSsaved = new HwAPPStateInfo();
    private Handler mStateMachineHandler;

    private HwWmpCallbackImpl(Handler handler) {
        this.mStateMachineHandler = handler;
        LogUtil.i(false, "init HwWmpCallbackImpl completed!", new Object[0]);
    }

    public static HwWmpCallbackImpl getInstance(Handler handler) {
        if (mHwWmpCallbackImpl == null) {
            mHwWmpCallbackImpl = new HwWmpCallbackImpl(handler);
        }
        return mHwWmpCallbackImpl;
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onAPPStateCallBack(HwAPPStateInfo appInfo, int state) {
        LogUtil.i(false, "onAppStateCallBack appinfo:%{public}s, state:%{public}d", appInfo.toString(), Integer.valueOf(state));
        HwWmpAppInfo mAppInfo = new HwWmpAppInfo(appInfo.mAppId, appInfo.mScenceId, appInfo.mAppUID, appInfo.mAppType, appInfo.mAppState, appInfo.mNetworkType);
        if (state == 100 || state == 102 || state == 103) {
            updateAppState(appInfo, mAppInfo);
        } else if (!mAppInfo.isMonitorApp()) {
            LogUtil.d(false, " app is not in checking list, ScenesId=%{public}d", Integer.valueOf(appInfo.mScenceId));
        } else {
            LogUtil.i(false, " App or Scenes closed, code=%{public}d, net=%{public}d", Integer.valueOf(appInfo.mScenceId), Integer.valueOf(appInfo.mNetworkType));
            Message msg = Message.obtain(this.mStateMachineHandler, 201);
            msg.obj = mAppInfo;
            this.mStateMachineHandler.sendMessage(msg);
            this.appInfoSsaved = new HwAPPStateInfo();
        }
    }

    private void updateAppState(HwAPPStateInfo appInfo, HwWmpAppInfo mAppInfo) {
        if (this.appInfoSsaved.mAppId == appInfo.mAppId && this.appInfoSsaved.mScenceId == appInfo.mScenceId && this.appInfoSsaved.mAppUID == appInfo.mAppUID && this.appInfoSsaved.mAppType == appInfo.mAppType && this.appInfoSsaved.mAppState == appInfo.mAppState && this.appInfoSsaved.mNetworkType != appInfo.mNetworkType) {
            LogUtil.i(false, " only Network changed, code=%{public}d, net=%{public}d", Integer.valueOf(appInfo.mScenceId), Integer.valueOf(appInfo.mNetworkType));
            Message msg = Message.obtain(this.mStateMachineHandler, 202);
            msg.obj = mAppInfo;
            this.mStateMachineHandler.sendMessage(msg);
            return;
        }
        if (!((this.appInfoSsaved.mAppId == appInfo.mAppId && this.appInfoSsaved.mScenceId == appInfo.mScenceId && this.appInfoSsaved.mAppUID == appInfo.mAppUID) || appInfo.mScenceId <= 0 || this.appInfoSsaved.mAppUID == -1)) {
            LogUtil.i(false, " App or Scenes changed, code=%{public}d, net=%{public}d", Integer.valueOf(this.appInfoSsaved.mScenceId), Integer.valueOf(this.appInfoSsaved.mNetworkType));
            HwWmpAppInfo mSavedAppInfo = new HwWmpAppInfo(this.appInfoSsaved.mAppId, this.appInfoSsaved.mScenceId, this.appInfoSsaved.mAppUID, this.appInfoSsaved.mAppType, this.appInfoSsaved.mAppState, this.appInfoSsaved.mNetworkType);
            if (mSavedAppInfo.isMonitorApp()) {
                Message msg2 = Message.obtain(this.mStateMachineHandler, 201);
                msg2.obj = mSavedAppInfo;
                this.mStateMachineHandler.sendMessage(msg2);
            }
        }
        if (!mAppInfo.isMonitorApp()) {
            LogUtil.d(false, " app is not in checking list, ScenesId=%{public}d", Integer.valueOf(appInfo.mScenceId));
            return;
        }
        LogUtil.i(false, " App or Scenes Start, code=%{public}d, net=%{public}d", Integer.valueOf(appInfo.mScenceId), Integer.valueOf(appInfo.mNetworkType));
        Message msg3 = Message.obtain(this.mStateMachineHandler, 200);
        msg3.obj = mAppInfo;
        this.mStateMachineHandler.sendMessage(msg3);
        this.appInfoSsaved.copyObjectValue(appInfo);
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onAPPQualityCallBack(HwAPPStateInfo appInfo, int experience) {
        int qoe;
        LogUtil.i(false, "onAPPQualityCallBack", new Object[0]);
        HwWmpAppInfo mAppInfo = new HwWmpAppInfo(appInfo.mAppId, appInfo.mScenceId, appInfo.mAppUID, appInfo.mAppType, appInfo.mAppState, appInfo.mNetworkType);
        if (!mAppInfo.isMonitorApp()) {
            LogUtil.d(false, " app is not in checking list, ScenesId=%{public}d", Integer.valueOf(appInfo.mScenceId));
            return;
        }
        String appName = mAppInfo.getAppName();
        if (experience == 106) {
            qoe = 2;
        } else if (experience != 107) {
            qoe = -1;
        } else {
            qoe = 1;
            Message msg = Message.obtain(this.mStateMachineHandler, (int) WMStateCons.MSG_APP_DATA_STALL);
            Bundle bundle = new Bundle();
            bundle.putString(KEY_APP_NAME, appName);
            msg.setData(bundle);
            this.mStateMachineHandler.sendMessageDelayed(msg, HwArbitrationDEFS.WIFI_RX_BYTES_THRESHOLD);
        }
        if (this.appInfoSsaved.mAppId == appInfo.mAppId && this.appInfoSsaved.mScenceId == appInfo.mScenceId && this.appInfoSsaved.mAppUID == appInfo.mAppUID && this.appInfoSsaved.mAppType == appInfo.mAppType && this.appInfoSsaved.mAppState == appInfo.mAppState && this.appInfoSsaved.mNetworkType != appInfo.mNetworkType) {
            LogUtil.i(false, " only Network changed, code=%{public}d", Integer.valueOf(appInfo.mScenceId));
            Message msg2 = Message.obtain(this.mStateMachineHandler, 202);
            msg2.obj = mAppInfo;
            this.mStateMachineHandler.sendMessage(msg2);
        }
        LogUtil.i(false, " QoE report, code=%{public}d qoe=%{public}d", Integer.valueOf(appInfo.mScenceId), Integer.valueOf(qoe));
        Message msg3 = Message.obtain(this.mStateMachineHandler, (int) WMStateCons.MSG_APP_QOE_EVENT);
        Bundle bundle2 = new Bundle();
        bundle2.putInt(KEY_QOE, qoe);
        bundle2.putString(KEY_APP_NAME, appName);
        msg3.setData(bundle2);
        this.mStateMachineHandler.sendMessage(msg3);
        this.appInfoSsaved.copyObjectValue(appInfo);
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onNetworkQualityCallBack(int uid, int scenes, int network, boolean isSatisfy) {
        LogUtil.d(false, "onNetworkQualityCallBack, not used", new Object[0]);
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onAPPRttInfoCallBack(HwAPPStateInfo info) {
        LogUtil.d(false, "onAPPRttInfoCallBack, not used", new Object[0]);
    }

    @Override // com.android.server.hidata.appqoe.IHwAPPQoECallback
    public void onWifiLinkQuality(int uid, int scenes, boolean isSatisfy) {
        LogUtil.i(false, "onWifiLinkQuality, not used", new Object[0]);
    }
}
