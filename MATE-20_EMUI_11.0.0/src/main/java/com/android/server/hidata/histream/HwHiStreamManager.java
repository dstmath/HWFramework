package com.android.server.hidata.histream;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.appqoe.IHwAPPQoECallback;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.hidata.arbitration.IHiDataCHRCallBack;
import com.android.server.hidata.wavemapping.HwWaveMappingManager;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;

public class HwHiStreamManager implements IHwHiStreamCallback, IWaveMappingCallback {
    public static final int CHANNEL_QUALITY_CHECK_TIMEOUT = 18;
    private static final int CHANNEL_QUALITY_CHECK_TIMER = 2000;
    private static final int FIRST_VIDEO_CHECK_DELAY = 4000;
    private static final int FIRST_VIDEO_RX_TCP_THRESHOLD = 300;
    public static final int MSG_APP_MONITOR = 1;
    public static final int MSG_APP_STATE_START = 14;
    public static final int MSG_CHECK_APP_FIRST_VIDEO = 16;
    public static final int MSG_CHR_HANDOVER_GET_TUP_EVENT = 4;
    public static final int MSG_CHR_HANDOVER_UPLOAD_EVENT = 5;
    public static final int MSG_CHR_STALL_EVENT_DALAY_UPLOAD = 6;
    public static final int MSG_CHR_START_COLLECT_PARA = 7;
    public static final int MSG_CHR_UPLOAD_COLLECT_PARA = 8;
    public static final int MSG_FOREGROUND_ACTIVITY_CHANGED = 13;
    public static final int MSG_MPLINK_STATE_CHANGE_EVENT = 12;
    public static final int MSG_NETWORK_CHANGE = 3;
    public static final int MSG_NOTIFY_APP_STATE_CHANGE = 2;
    public static final int MSG_NO_DATA_DETECT_EVENT = 10;
    public static final int MSG_STALL_DETECTED_EVENT = 9;
    public static final int MSG_START_FRAME_DETECT = 15;
    public static final int MSG_UPDATE_TRAFFIC_EVENT = 11;
    public static final int MSG_VIDEO_DETECT_START = 17;
    private static final int NETWORK_QUALITY_BAD = 1;
    private static final int NETWORK_QUALITY_GOOD = 0;
    private static HwHiStreamManager mHwHiStreamManager = null;
    private boolean isCheckFirstVideoDelayed = false;
    private Context mContext;
    private int mCurrUserType = 1;
    private HwHiStreamCHRManager mHwHiStreamCHRManager = null;
    private HwHiStreamContentAware mHwHiStreamContentAware = null;
    private HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = null;
    private HwHiStreamQoeMonitor mHwHiStreamQoeMonitor = null;
    private HwHistreamUserLearning mHwHistreamUserLearning = null;
    private Handler mManagerHandler;
    private IHwHistreamQoeCallback mQoeCallback;
    private boolean mQualityOngoing = false;
    private int mScene = -1;
    private int mStartTcpRxPackets = -1;

    private HwHiStreamManager(Context context) {
        this.mContext = context;
        initHistreamManagerHandler();
        this.mHwHiStreamCHRManager = HwHiStreamCHRManager.createInstance(context, this.mManagerHandler);
        this.mHwHiStreamContentAware = HwHiStreamContentAware.createInstance(context, this, this.mManagerHandler);
        this.mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.createInstance(context, this.mManagerHandler);
        this.mHwHiStreamQoeMonitor = HwHiStreamQoeMonitor.createInstance(context, this.mManagerHandler);
        this.mHwHistreamUserLearning = HwHistreamUserLearning.createInstance(context);
        HwHiStreamTraffic.createInstance(context);
        HwHiStreamUtils.logD(false, "histream create", new Object[0]);
        HwWaveMappingManager mWaveMappingManager = HwWaveMappingManager.getInstance();
        if (mWaveMappingManager != null) {
            mWaveMappingManager.registerWaveMappingCallback(this, 1);
        }
    }

    public static HwHiStreamManager createInstance(Context context) {
        if (mHwHiStreamManager == null) {
            mHwHiStreamManager = new HwHiStreamManager(context);
        }
        return mHwHiStreamManager;
    }

    public static HwHiStreamManager getInstance() {
        return mHwHiStreamManager;
    }

    public synchronized void registCHRCallback(IHiDataCHRCallBack callback) {
        this.mHwHiStreamCHRManager.registCHRCallback(callback);
    }

    public void registerHistreamQoeCallback(IHwHistreamQoeCallback callback) {
        this.mQoeCallback = callback;
    }

    private void initHistreamManagerHandler() {
        HandlerThread handlerThread = new HandlerThread("HwHiStreamManager_handler_thread");
        handlerThread.start();
        this.mManagerHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.hidata.histream.HwHiStreamManager.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        HwHiStreamManager.this.mHwHiStreamContentAware.handleAppMonotor();
                        return;
                    case 2:
                        HwHiStreamManager.this.mHwHiStreamContentAware.handleNotifyAppStateChange(msg);
                        return;
                    case 3:
                        HwHiStreamManager.this.handleNetworkChange(msg);
                        return;
                    case 4:
                    case 5:
                        HwHiStreamManager.this.mHwHiStreamCHRManager.handleUploadHandoverInfo(msg);
                        return;
                    case 6:
                        HwHiStreamManager.this.mHwHiStreamCHRManager.handleUploadStallEventDelay(msg);
                        return;
                    case 7:
                    case 8:
                    default:
                        return;
                    case 9:
                        HwHiStreamManager.this.onStallDetectedCallback(msg);
                        return;
                    case 10:
                        HwHiStreamManager.this.mHwHiStreamContentAware.onNodataDetected();
                        return;
                    case 11:
                        HwHiStreamManager.this.mHwHiStreamQoeMonitor.handleQueryTraffic();
                        return;
                    case 12:
                        HwHiStreamManager.this.mHwHiStreamCHRManager.onMplinkStateChange(msg);
                        return;
                    case 13:
                        HwHiStreamManager.this.mHwHiStreamContentAware.handleForeActivityChange(msg);
                        return;
                    case 14:
                        HwHiStreamManager.this.mHwHiStreamQoeMonitor.handleAppStateStart(HwHiStreamManager.this.mHwHiStreamContentAware.getCurAppInfo());
                        return;
                    case 15:
                        HwHiStreamManager.this.mHwHiStreamQoeMonitor.handleStartFrameDetect();
                        return;
                    case 16:
                        HwHiStreamManager.this.onCheckAppFirstVideo();
                        return;
                    case 17:
                        HwHiStreamManager.this.onVideoDetectStart();
                        return;
                    case 18:
                        HwHiStreamManager.this.onQueryChannelQualityTimeOut();
                        return;
                }
            }
        };
    }

    @Override // com.android.server.hidata.histream.IHwHiStreamCallback
    public void onAPPStateChangeCallback(HwAPPStateInfo stateInfo, int appState) {
        if (stateInfo == null) {
            HwHiStreamUtils.logD(false, "onAPPStateChangeCallback: stateInfo is null", new Object[0]);
            return;
        }
        stateInfo.mNetworkType = this.mHwHiStreamNetworkMonitor.getCurrNetworkType(stateInfo.mAppUID);
        this.mHwHistreamUserLearning.onAPPStateChange(stateInfo, appState);
        if (100 == appState) {
            this.mCurrUserType = this.mHwHistreamUserLearning.getUserType(stateInfo);
            stateInfo.mUserType = this.mCurrUserType;
            if (stateInfo.mScenceId == 100501) {
                this.isCheckFirstVideoDelayed = false;
                this.mStartTcpRxPackets = this.mHwHiStreamQoeMonitor.getCurTcpRxPackets(stateInfo.mAppUID);
            }
        }
        this.mHwHiStreamCHRManager.onCHRAppStateChange(stateInfo, appState);
        HwHiStreamUtils.logD(false, "onAPPStateChangeCallback:%{public}s", stateInfo.toString());
        if ((stateInfo.mUserType != 3 || stateInfo.mScenceId == 100105) && HwAPPQoEManager.isAppStartMonitor(stateInfo, this.mContext)) {
            this.mHwHiStreamQoeMonitor.onAPPStateChange(stateInfo, appState);
        }
        IHwAPPQoECallback brainCallback = getArbitrationCallback();
        if (brainCallback != null) {
            brainCallback.onAPPStateCallBack(stateInfo, appState);
        } else {
            HwHiStreamUtils.logD(false, "onAPPStateChangeCallback: brainCallback is null", new Object[0]);
        }
        IHwAPPQoECallback mVMCallback = getWaveMappingCallback();
        if (mVMCallback != null) {
            mVMCallback.onAPPStateCallBack(stateInfo, appState);
        } else {
            HwHiStreamUtils.logD(false, "onAPPStateChangeCallback: mVMCallback is null", new Object[0]);
        }
    }

    public void handleNetworkChange(Message msg) {
        if (msg != null && msg.obj != null) {
            int event = ((Bundle) msg.obj).getInt("event");
            if (event == 1) {
                HwHiStreamUtils.logD(false, "+++++++WIFI disabled ++++++", new Object[0]);
                this.mHwHistreamUserLearning.onWifiDisabled();
            } else if (event == 2) {
                HwHiStreamUtils.logD(false, "+++++++MOBILE DATA disabled ++++++", new Object[0]);
                HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
                boolean isMplink = false;
                if (curAppInfo != null) {
                    isMplink = HwArbitrationFunction.isInMPLink(this.mContext, curAppInfo.mAppUID);
                }
                this.mHwHistreamUserLearning.onMobileDataDisabled(isMplink);
            } else if (event == 3) {
                this.mHwHiStreamCHRManager.onNetworkChange();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onStallDetectedCallback(Message msg) {
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int appSceneId = bundle.getInt("appSceneId");
            int detectResult = bundle.getInt("detectResult");
            HwAPPStateInfo stateInfo = this.mHwHiStreamContentAware.getCurAPPStateInfo(appSceneId);
            if (stateInfo == null) {
                HwHiStreamUtils.logD(false, "onStallDetectedCallback: stateInfo is null", new Object[0]);
            } else if (stateInfo.mScenceId != 100701 || this.mHwHiStreamContentAware.isInPlayActivity()) {
                stateInfo.mNetworkType = this.mHwHiStreamNetworkMonitor.getCurrNetworkType(stateInfo.mAppUID);
                notifyAppStall(appSceneId, detectResult, stateInfo);
            } else {
                HwHiStreamUtils.logD(false, "onStallDetectedCallback: not in play activity", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onQueryChannelQualityTimeOut() {
        HwHiStreamUtils.logD(false, "onQueryChannelQualityTimeOut Enter", new Object[0]);
        HwAPPStateInfo appStateInfo = this.mHwHiStreamContentAware.getCurAPPStateInfo(this.mScene);
        if (appStateInfo != null) {
            notifyAppStall(this.mScene, 1, appStateInfo);
        }
        this.mQualityOngoing = false;
    }

    private void notifyAppStall(int scence, int detectResult, HwAPPStateInfo stateInfo) {
        HwHiStreamUtils.logD(false, "notifyAppStall to brain Enter", new Object[0]);
        IHwAPPQoECallback brainCallback = getArbitrationCallback();
        if (brainCallback != null) {
            brainCallback.onAPPQualityCallBack(stateInfo, 107);
        } else {
            HwHiStreamUtils.logD(false, "onStallDetectedCallback: brainCallback is null", new Object[0]);
        }
        IHwAPPQoECallback mVMCallback = getWaveMappingCallback();
        if (mVMCallback != null) {
            mVMCallback.onAPPQualityCallBack(stateInfo, 107);
        } else {
            HwHiStreamUtils.logD(false, "onStallDetectedCallback: mVMCallback is null", new Object[0]);
        }
    }

    public void onMplinkStateChange(int sceneId, int mplinkEvent, int failReason) {
        HwAPPStateInfo curAppInfo;
        Bundle bundle = new Bundle();
        if (this.mHwHiStreamNetworkMonitor != null && this.mHwHistreamUserLearning != null) {
            bundle.putInt("sceneId", sceneId);
            bundle.putInt("mplinkEvent", mplinkEvent);
            bundle.putInt("failReason", failReason);
            Handler handler = this.mManagerHandler;
            handler.sendMessage(handler.obtainMessage(12, bundle));
            if (9 == mplinkEvent && (curAppInfo = getCurStreamAppInfo()) != null && sceneId == curAppInfo.mScenceId) {
                this.mHwHistreamUserLearning.onMobileDataDisabled(true);
            }
        }
    }

    public IHwAPPQoECallback getArbitrationCallback() {
        HwAPPQoEManager hwAPPQoEManager = HwAPPQoEManager.getInstance();
        if (hwAPPQoEManager != null) {
            return hwAPPQoEManager.getAPPQoECallback(true);
        }
        return null;
    }

    public IHwAPPQoECallback getWaveMappingCallback() {
        HwAPPQoEManager hwAPPQoEManager = HwAPPQoEManager.getInstance();
        if (hwAPPQoEManager != null) {
            return hwAPPQoEManager.getAPPQoECallback(false);
        }
        return null;
    }

    public HwAPPStateInfo getCurStreamAppInfo() {
        return this.mHwHiStreamContentAware.getCurStreamAppInfo();
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingReportCallback(int reportType, String networkName, int networkType) {
        HwHiStreamUtils.logD(false, "onWaveMappingReportCallback", new Object[0]);
        HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
        if (curAppInfo != null) {
            HwHiStreamUtils.logD(false, "onWaveMappingReportCallback, app: %{public}d", Integer.valueOf(curAppInfo.mAppId));
            IHwAPPQoECallback brainCallback = getArbitrationCallback();
            if (brainCallback != null) {
                brainCallback.onAPPQualityCallBack(curAppInfo, 107);
            }
        }
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespondCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespond4BackCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVideoDetectStart() {
        HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
        if (curAppInfo != null && curAppInfo.mScenceId == 100501) {
            curAppInfo.setIsVideoStart(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCheckAppFirstVideo() {
        HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
        if (curAppInfo != null && curAppInfo.mScenceId == 100501 && curAppInfo.getIsAppFirstStart() && !curAppInfo.getIsVideoStart()) {
            boolean isStall = false;
            if (this.isCheckFirstVideoDelayed) {
                HwHiStreamUtils.logD(false, "onCheckAppFirstStart: Check Delay", new Object[0]);
                isStall = true;
            } else {
                int curRxPackets = this.mHwHiStreamQoeMonitor.getCurTcpRxPackets(curAppInfo.mAppUID);
                int i = this.mStartTcpRxPackets;
                if (i == -1 || curRxPackets == -1) {
                    this.isCheckFirstVideoDelayed = true;
                    this.mManagerHandler.sendEmptyMessageDelayed(16, 4000);
                    return;
                } else if (curRxPackets - i < 300) {
                    HwHiStreamUtils.logD(false, "onCheckAppFirstStart: Poor Network", new Object[0]);
                    isStall = true;
                } else {
                    HwHiStreamUtils.logD(false, "onCheckAppFirstStart: Not Poor Network", new Object[0]);
                }
            }
            if (isStall) {
                IHwAPPQoECallback brainCallback = getArbitrationCallback();
                if (brainCallback != null) {
                    brainCallback.onAPPQualityCallBack(curAppInfo, 107);
                }
                this.isCheckFirstVideoDelayed = false;
            }
        }
    }
}
