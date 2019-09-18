package com.android.server.hidata.wavemapping.dataprovider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.appqoe.IHwAPPQoECallback;
import com.android.server.hidata.wavemapping.entity.HwWmpAppInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class HwWmpCallbackImpl implements IHwAPPQoECallback {
    private static final String TAG = "HwWmpCallbackImpl";
    private static HwWmpCallbackImpl mHwWmpCallbackImpl;
    private HwAPPStateInfo appInfoSsaved = new HwAPPStateInfo();
    private Handler mStateMachineHandler;

    private HwWmpCallbackImpl(Handler handler) {
        this.mStateMachineHandler = handler;
        LogUtil.i("init HwWmpCallbackImpl completed!");
    }

    public static HwWmpCallbackImpl getInstance(Handler handler) {
        if (mHwWmpCallbackImpl == null) {
            mHwWmpCallbackImpl = new HwWmpCallbackImpl(handler);
        }
        return mHwWmpCallbackImpl;
    }

    public void onAPPStateCallBack(HwAPPStateInfo appInfo, int state) {
        LogUtil.i("onAppStateCallBack appinfo:" + appInfo.toString() + ", state:" + state);
        HwWmpAppInfo mAppInfo = new HwWmpAppInfo(appInfo.mAppId, appInfo.mScenceId, appInfo.mAppUID, appInfo.mAppType, appInfo.mAppState, appInfo.mNetworkType);
        switch (state) {
            case 100:
            case 102:
            case 103:
                if (this.appInfoSsaved.mAppId == appInfo.mAppId && this.appInfoSsaved.mScenceId == appInfo.mScenceId && this.appInfoSsaved.mAppUID == appInfo.mAppUID && this.appInfoSsaved.mAppType == appInfo.mAppType && this.appInfoSsaved.mAppState == appInfo.mAppState && this.appInfoSsaved.mNetworkType != appInfo.mNetworkType) {
                    LogUtil.i(" only Network changed, code=" + appInfo.mScenceId + ", net=" + appInfo.mNetworkType);
                    Message msg = Message.obtain(this.mStateMachineHandler, 202);
                    msg.obj = mAppInfo;
                    this.mStateMachineHandler.sendMessage(msg);
                    return;
                }
                if (!((this.appInfoSsaved.mAppId == appInfo.mAppId && this.appInfoSsaved.mScenceId == appInfo.mScenceId && this.appInfoSsaved.mAppUID == appInfo.mAppUID) || appInfo.mScenceId <= 0 || -1 == this.appInfoSsaved.mAppUID)) {
                    LogUtil.i(" App or Scence changed, code=" + this.appInfoSsaved.mScenceId + ", net=" + this.appInfoSsaved.mNetworkType);
                    HwWmpAppInfo hwWmpAppInfo = new HwWmpAppInfo(this.appInfoSsaved.mAppId, this.appInfoSsaved.mScenceId, this.appInfoSsaved.mAppUID, this.appInfoSsaved.mAppType, this.appInfoSsaved.mAppState, this.appInfoSsaved.mNetworkType);
                    HwWmpAppInfo mSavedAppInfo = hwWmpAppInfo;
                    if (mSavedAppInfo.isMonitorApp()) {
                        Message msg2 = Message.obtain(this.mStateMachineHandler, 201);
                        msg2.obj = mSavedAppInfo;
                        this.mStateMachineHandler.sendMessage(msg2);
                    }
                }
                if (!mAppInfo.isMonitorApp()) {
                    LogUtil.d(" app is not in checking list, ScenceId=" + appInfo.mScenceId);
                    return;
                }
                LogUtil.i(" App or Scence Start, code=" + appInfo.mScenceId + ", net=" + appInfo.mNetworkType);
                Message msg3 = Message.obtain(this.mStateMachineHandler, 200);
                msg3.obj = mAppInfo;
                this.mStateMachineHandler.sendMessage(msg3);
                this.appInfoSsaved.copyObjectValue(appInfo);
                return;
            default:
                if (!mAppInfo.isMonitorApp()) {
                    LogUtil.d(" app is not in checking list, ScenceId=" + appInfo.mScenceId);
                    return;
                }
                LogUtil.i(" App or Scence closed, code=" + appInfo.mScenceId + ", net=" + appInfo.mNetworkType);
                Message msg4 = Message.obtain(this.mStateMachineHandler, 201);
                msg4.obj = mAppInfo;
                this.mStateMachineHandler.sendMessage(msg4);
                this.appInfoSsaved = new HwAPPStateInfo();
                return;
        }
    }

    public void onAPPQualityCallBack(HwAPPStateInfo appInfo, int experience) {
        int qoe;
        LogUtil.i("onAPPQualityCallBack");
        HwWmpAppInfo mAppInfo = new HwWmpAppInfo(appInfo.mAppId, appInfo.mScenceId, appInfo.mAppUID, appInfo.mAppType, appInfo.mAppState, appInfo.mNetworkType);
        if (!mAppInfo.isMonitorApp()) {
            LogUtil.d(" app is not in checking list, ScenceId=" + appInfo.mScenceId);
            return;
        }
        String appName = mAppInfo.getAppName();
        switch (experience) {
            case 106:
                qoe = 2;
                break;
            case 107:
                qoe = 1;
                Message msg = Message.obtain(this.mStateMachineHandler, 211);
                Bundle bundle = new Bundle();
                bundle.putString("APPNAME", appName);
                msg.setData(bundle);
                this.mStateMachineHandler.sendMessageDelayed(msg, 15000);
                break;
            default:
                qoe = -1;
                break;
        }
        if (this.appInfoSsaved.mAppId == appInfo.mAppId && this.appInfoSsaved.mScenceId == appInfo.mScenceId && this.appInfoSsaved.mAppUID == appInfo.mAppUID && this.appInfoSsaved.mAppType == appInfo.mAppType && this.appInfoSsaved.mAppState == appInfo.mAppState && this.appInfoSsaved.mNetworkType != appInfo.mNetworkType) {
            LogUtil.i(" only Network changed, code=" + appInfo.mScenceId);
            Message msg2 = Message.obtain(this.mStateMachineHandler, 202);
            msg2.obj = mAppInfo;
            this.mStateMachineHandler.sendMessage(msg2);
        }
        LogUtil.i(" QoE report, code=" + appInfo.mScenceId + " qoe=" + qoe);
        Message msg3 = Message.obtain(this.mStateMachineHandler, 210);
        Bundle bundle2 = new Bundle();
        bundle2.putInt("QOE", qoe);
        bundle2.putString("APPNAME", appName);
        msg3.setData(bundle2);
        this.mStateMachineHandler.sendMessage(msg3);
        this.appInfoSsaved.copyObjectValue(appInfo);
    }

    public void onNetworkQualityCallBack(int UID, int sense, int network, boolean isSatisfy) {
        LogUtil.d("onNetworkQualityCallBack, not used");
    }

    public void onAPPRttInfoCallBack(HwAPPStateInfo info) {
        LogUtil.d("onAPPRttInfoCallBack, not used");
    }

    public void onWifiLinkQuality(int UID, int scence, boolean isSatisfy) {
        LogUtil.i("onWifiLinkQuality, not used");
    }
}
