package com.android.server.hidata.wavemapping.dataprovider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.server.hidata.appqoe.HwAppStateInfo;
import com.android.server.hidata.appqoe.IHwAppQoeCallback;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.hidata.wavemapping.entity.HwWmpAppInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class HwWmpCallbackImpl implements IHwAppQoeCallback {
    private static final String KEY_APP_NAME = "APPNAME";
    private static final String KEY_QOE = "QOE";
    private static final String TAG = "HwWmpCallbackImpl";
    private static HwWmpCallbackImpl mHwWmpCallbackImpl;
    private HwAppStateInfo appInfoSsaved = new HwAppStateInfo();
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

    @Override // com.android.server.hidata.appqoe.IHwAppQoeCallback
    public void onAppStateCallBack(HwAppStateInfo appInfo, int state) {
        LogUtil.i(false, "onAppStateCallBack appinfo:%{public}s, state:%{public}d", appInfo.toString(), Integer.valueOf(state));
        HwWmpAppInfo mAppInfo = new HwWmpAppInfo(appInfo.mAppId, appInfo.mScenesId, appInfo.mAppUid, appInfo.mAppType, appInfo.mAppState, appInfo.mNetworkType);
        if (state == 100 || state == 102 || state == 103) {
            updateAppState(appInfo, mAppInfo);
        } else if (!mAppInfo.isMonitorApp()) {
            LogUtil.d(false, " app is not in checking list, ScenesId=%{public}d", Integer.valueOf(appInfo.mScenesId));
        } else {
            LogUtil.i(false, " App or Scenes closed, code=%{public}d, net=%{public}d", Integer.valueOf(appInfo.mScenesId), Integer.valueOf(appInfo.mNetworkType));
            Message msg = Message.obtain(this.mStateMachineHandler, 201);
            msg.obj = mAppInfo;
            this.mStateMachineHandler.sendMessage(msg);
            this.appInfoSsaved = new HwAppStateInfo();
        }
    }

    private void updateAppState(HwAppStateInfo appInfo, HwWmpAppInfo mAppInfo) {
        if (this.appInfoSsaved.mAppId == appInfo.mAppId && this.appInfoSsaved.mScenesId == appInfo.mScenesId && this.appInfoSsaved.mAppUid == appInfo.mAppUid && this.appInfoSsaved.mAppType == appInfo.mAppType && this.appInfoSsaved.mAppState == appInfo.mAppState && this.appInfoSsaved.mNetworkType != appInfo.mNetworkType) {
            LogUtil.i(false, " only Network changed, code=%{public}d, net=%{public}d", Integer.valueOf(appInfo.mScenesId), Integer.valueOf(appInfo.mNetworkType));
            Message msg = Message.obtain(this.mStateMachineHandler, 202);
            msg.obj = mAppInfo;
            this.mStateMachineHandler.sendMessage(msg);
            return;
        }
        if (!((this.appInfoSsaved.mAppId == appInfo.mAppId && this.appInfoSsaved.mScenesId == appInfo.mScenesId && this.appInfoSsaved.mAppUid == appInfo.mAppUid) || appInfo.mScenesId <= 0 || this.appInfoSsaved.mAppUid == -1)) {
            LogUtil.i(false, " App or Scenes changed, code=%{public}d, net=%{public}d", Integer.valueOf(this.appInfoSsaved.mScenesId), Integer.valueOf(this.appInfoSsaved.mNetworkType));
            HwWmpAppInfo mSavedAppInfo = new HwWmpAppInfo(this.appInfoSsaved.mAppId, this.appInfoSsaved.mScenesId, this.appInfoSsaved.mAppUid, this.appInfoSsaved.mAppType, this.appInfoSsaved.mAppState, this.appInfoSsaved.mNetworkType);
            if (mSavedAppInfo.isMonitorApp()) {
                Message msg2 = Message.obtain(this.mStateMachineHandler, 201);
                msg2.obj = mSavedAppInfo;
                this.mStateMachineHandler.sendMessage(msg2);
            }
        }
        if (!mAppInfo.isMonitorApp()) {
            LogUtil.d(false, " app is not in checking list, ScenesId=%{public}d", Integer.valueOf(appInfo.mScenesId));
            return;
        }
        LogUtil.i(false, " App or Scenes Start, code=%{public}d, net=%{public}d", Integer.valueOf(appInfo.mScenesId), Integer.valueOf(appInfo.mNetworkType));
        Message msg3 = Message.obtain(this.mStateMachineHandler, 200);
        msg3.obj = mAppInfo;
        this.mStateMachineHandler.sendMessage(msg3);
        this.appInfoSsaved.copyObjectValue(appInfo);
    }

    @Override // com.android.server.hidata.appqoe.IHwAppQoeCallback
    public void onAppQualityCallBack(HwAppStateInfo appInfo, int experience) {
        int qoe;
        LogUtil.i(false, "onAPPQualityCallBack", new Object[0]);
        HwWmpAppInfo mAppInfo = new HwWmpAppInfo(appInfo.mAppId, appInfo.mScenesId, appInfo.mAppUid, appInfo.mAppType, appInfo.mAppState, appInfo.mNetworkType);
        if (!mAppInfo.isMonitorApp()) {
            LogUtil.d(false, " app is not in checking list, ScenesId=%{public}d", Integer.valueOf(appInfo.mScenesId));
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
            this.mStateMachineHandler.sendMessageDelayed(msg, 15000);
        }
        if (this.appInfoSsaved.mAppId == appInfo.mAppId && this.appInfoSsaved.mScenesId == appInfo.mScenesId && this.appInfoSsaved.mAppUid == appInfo.mAppUid && this.appInfoSsaved.mAppType == appInfo.mAppType && this.appInfoSsaved.mAppState == appInfo.mAppState && this.appInfoSsaved.mNetworkType != appInfo.mNetworkType) {
            LogUtil.i(false, " only Network changed, code=%{public}d", Integer.valueOf(appInfo.mScenesId));
            Message msg2 = Message.obtain(this.mStateMachineHandler, 202);
            msg2.obj = mAppInfo;
            this.mStateMachineHandler.sendMessage(msg2);
        }
        LogUtil.i(false, " QoE report, code=%{public}d qoe=%{public}d", Integer.valueOf(appInfo.mScenesId), Integer.valueOf(qoe));
        Message msg3 = Message.obtain(this.mStateMachineHandler, (int) WMStateCons.MSG_APP_QOE_EVENT);
        Bundle bundle2 = new Bundle();
        bundle2.putInt(KEY_QOE, qoe);
        bundle2.putString(KEY_APP_NAME, appName);
        msg3.setData(bundle2);
        this.mStateMachineHandler.sendMessage(msg3);
        this.appInfoSsaved.copyObjectValue(appInfo);
    }

    @Override // com.android.server.hidata.appqoe.IHwAppQoeCallback
    public void onAppRttInfoCallBack(HwAppStateInfo info) {
        LogUtil.d(false, "onAPPRttInfoCallBack, not used", new Object[0]);
    }
}
