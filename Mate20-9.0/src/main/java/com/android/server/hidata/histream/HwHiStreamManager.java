package com.android.server.hidata.histream;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.appqoe.IHwAPPQoECallback;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.hidata.arbitration.IHiDataCHRCallBack;
import com.android.server.hidata.wavemapping.HwWaveMappingManager;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;

public class HwHiStreamManager implements IHwHiStreamCallback, IWaveMappingCallback {
    private static final int DOUYIN_RX_TCP_THRESHOLD = 300;
    public static final int MSG_APP_MONITOR = 1;
    public static final int MSG_CHECK_APP_FIRST_START = 15;
    public static final int MSG_CHR_HANDOVER_GET_TUP_EVENT = 4;
    public static final int MSG_CHR_HANDOVER_UPLOAD_EVENT = 5;
    public static final int MSG_CHR_STALL_EVENT_DALAY_UPLOAD = 6;
    public static final int MSG_CHR_START_COLLECT_PARA = 7;
    public static final int MSG_CHR_UPLOAD_COLLECT_PARA = 8;
    public static final int MSG_DOUYIN_DETECT_START = 14;
    public static final int MSG_FOREGROUND_ACTIVITY_CHANGED = 13;
    public static final int MSG_MPLINK_STATE_CHANGE_EVENT = 12;
    public static final int MSG_NETWORK_CHANGE = 3;
    public static final int MSG_NOTIFY_APP_STATE_CHANGE = 2;
    public static final int MSG_NO_DATA_DETECT_EVENT = 10;
    public static final int MSG_STALL_DETECTED_EVENT = 9;
    public static final int MSG_UPDATE_TRAFFIC_EVENT = 11;
    private static HwHiStreamManager mHwHiStreamManager = null;
    private Context mContext;
    private int mCurrUserType = 1;
    /* access modifiers changed from: private */
    public HwHiStreamCHRManager mHwHiStreamCHRManager = null;
    /* access modifiers changed from: private */
    public HwHiStreamContentAware mHwHiStreamContentAware = null;
    private HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = null;
    private HwHiStreamQoeMonitor mHwHiStreamQoeMonitor = null;
    private HwHistreamUserLearning mHwHistreamUserLearning = null;
    private Handler mManagerHandler;
    private IHwHistreamQoeCallback mQoeCallback;
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
        HwHiStreamUtils.logD("histream create");
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
                        HwHiStreamManager.this.mHwHiStreamCHRManager.handleStartCollectPara();
                        return;
                    case 8:
                        HwHiStreamManager.this.mHwHiStreamCHRManager.handleUploadCollectPara();
                        return;
                    case 9:
                        HwHiStreamManager.this.onStallDetectedCallback(msg);
                        return;
                    case 10:
                        HwHiStreamManager.this.mHwHiStreamContentAware.onNodataDetected();
                        return;
                    case 11:
                        HwHiStreamManager.this.onUpdateTraffic(msg);
                        return;
                    case 12:
                        if (HwHiStreamManager.this.mHwHiStreamCHRManager != null) {
                            HwHiStreamManager.this.mHwHiStreamCHRManager.onMplinkStateChange(msg);
                            return;
                        }
                        return;
                    case 13:
                        HwHiStreamManager.this.mHwHiStreamContentAware.handleForeActivityChange(msg);
                        return;
                    case 14:
                        HwHiStreamManager.this.onDouyinDetectStart();
                        return;
                    case 15:
                        HwHiStreamManager.this.onCheckAppFirstStart();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void onAPPStateChangeCallback(HwAPPStateInfo stateInfo, int appState) {
        if (stateInfo == null) {
            HwHiStreamUtils.logD("onAPPStateChangeCallback: stateInfo is null");
            return;
        }
        stateInfo.mNetworkType = this.mHwHiStreamNetworkMonitor.getCurrNetworkType(stateInfo.mAppUID);
        this.mHwHistreamUserLearning.onAPPStateChange(stateInfo, appState);
        if (100 == appState) {
            this.mCurrUserType = this.mHwHistreamUserLearning.getUserType(stateInfo);
            stateInfo.mUserType = this.mCurrUserType;
            if (100501 == stateInfo.mScenceId) {
                this.mStartTcpRxPackets = this.mHwHiStreamQoeMonitor.getCurTcpRxPackets(stateInfo.mAppUID);
            }
        }
        this.mHwHiStreamCHRManager.onCHRAppStateChange(stateInfo, appState);
        HwHiStreamUtils.logD("onAPPStateChangeCallback:" + stateInfo.toString());
        if ((3 != stateInfo.mUserType || 100105 == stateInfo.mScenceId) && HwArbitrationCommonUtils.MAINLAND_REGION && !HwArbitrationCommonUtils.isLiteProduct(this.mContext)) {
            this.mHwHiStreamQoeMonitor.onAPPStateChange(stateInfo, appState);
        }
        IHwAPPQoECallback brainCallback = getArbitrationCallback();
        if (brainCallback != null) {
            brainCallback.onAPPStateCallBack(stateInfo, appState);
        } else {
            HwHiStreamUtils.logD("onAPPStateChangeCallback: brainCallback is null");
        }
        IHwAPPQoECallback mVMCallback = getWaveMappingCallback();
        if (mVMCallback != null) {
            mVMCallback.onAPPStateCallBack(stateInfo, appState);
        } else {
            HwHiStreamUtils.logD("onAPPStateChangeCallback: mVMCallback is null");
        }
    }

    public void handleNetworkChange(Message msg) {
        if (msg != null && msg.obj != null) {
            switch (((Bundle) msg.obj).getInt("event")) {
                case 1:
                    HwHiStreamUtils.logD("+++++++WIFI disabled ++++++");
                    this.mHwHistreamUserLearning.onWifiDisabled();
                    break;
                case 2:
                    HwHiStreamUtils.logD("+++++++MOBILE DATA disabled ++++++");
                    HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
                    boolean isMplink = false;
                    if (curAppInfo != null) {
                        isMplink = HwArbitrationFunction.isInMPLink(this.mContext, curAppInfo.mAppUID);
                    }
                    this.mHwHistreamUserLearning.onMobileDataDisabled(isMplink);
                    break;
                case 3:
                    this.mHwHiStreamCHRManager.onNetworkChange();
                    break;
            }
        }
    }

    /* access modifiers changed from: private */
    public void onStallDetectedCallback(Message msg) {
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int appSceneId = bundle.getInt("appSceneId");
            int detectResult = bundle.getInt("detectResult");
            HwAPPStateInfo stateInfo = this.mHwHiStreamContentAware.getCurAPPStateInfo(appSceneId);
            if (stateInfo == null) {
                HwHiStreamUtils.logD("onStallDetectedCallback: stateInfo is null");
            } else if (100701 != stateInfo.mScenceId || this.mHwHiStreamContentAware.isInPlayActivity()) {
                stateInfo.mNetworkType = this.mHwHiStreamNetworkMonitor.getCurrNetworkType(stateInfo.mAppUID);
                IHwAPPQoECallback brainCallback = getArbitrationCallback();
                if (brainCallback != null) {
                    brainCallback.onAPPQualityCallBack(stateInfo, 107);
                } else {
                    HwHiStreamUtils.logD("onStallDetectedCallback: brainCallback is null");
                }
                IHwAPPQoECallback mVMCallback = getWaveMappingCallback();
                if (mVMCallback != null) {
                    mVMCallback.onAPPQualityCallBack(stateInfo, 107);
                } else {
                    HwHiStreamUtils.logD("onStallDetectedCallback: mVMCallback is null");
                }
                this.mHwHiStreamCHRManager.onStallDetect(appSceneId, detectResult, this.mQoeCallback);
            } else {
                HwHiStreamUtils.logD("onStallDetectedCallback: not in play activity");
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUpdateTraffic(Message msg) {
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            this.mHwHiStreamCHRManager.onUpdateQuality(bundle.getInt("wifiRxTraffic"), bundle.getInt("cellularRxTraffic"), bundle.getInt("wifiTxTraffic"), bundle.getInt("celluarTxTraffic"), bundle.getInt("monitoringUid"));
        }
    }

    public void onMplinkStateChange(int sceneId, int mplinkEvent, int failReason) {
        Bundle bundle = new Bundle();
        if (this.mHwHiStreamNetworkMonitor != null && this.mHwHistreamUserLearning != null) {
            bundle.putInt("sceneId", sceneId);
            bundle.putInt("mplinkEvent", mplinkEvent);
            bundle.putInt("failReason", failReason);
            this.mManagerHandler.sendMessage(this.mManagerHandler.obtainMessage(12, bundle));
            if (9 == mplinkEvent) {
                HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
                if (curAppInfo != null && sceneId == curAppInfo.mScenceId) {
                    if (2 == curAppInfo.mUserType) {
                        this.mHwHistreamUserLearning.setAPUserType(this.mHwHiStreamNetworkMonitor.getCurBSSID(), sceneId, 1);
                    } else if (1 == curAppInfo.mUserType) {
                        this.mHwHistreamUserLearning.setAPUserType(this.mHwHiStreamNetworkMonitor.getCurBSSID(), sceneId, 3);
                    }
                }
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

    /* access modifiers changed from: private */
    public void onDouyinDetectStart() {
        HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
        if (curAppInfo != null && 100501 == curAppInfo.mScenceId) {
            curAppInfo.mIsVideoStart = true;
        }
    }

    /* access modifiers changed from: private */
    public void onCheckAppFirstStart() {
        HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
        if (curAppInfo != null && 100501 == curAppInfo.mScenceId && true == curAppInfo.mIsAppFirstStart && !curAppInfo.mIsVideoStart) {
            boolean isPoorNetwork = false;
            int curRxPackets = this.mHwHiStreamQoeMonitor.getCurTcpRxPackets(curAppInfo.mAppUID);
            if (!(-1 == this.mStartTcpRxPackets || -1 == curRxPackets || curRxPackets - this.mStartTcpRxPackets >= 300)) {
                isPoorNetwork = true;
            }
            HwHiStreamUtils.logD("onCheckAppFirstStart: isPoorNetwork = " + isPoorNetwork);
            if (isPoorNetwork) {
                IHwAPPQoECallback brainCallback = getArbitrationCallback();
                if (brainCallback != null) {
                    brainCallback.onAPPQualityCallBack(curAppInfo, 107);
                }
                this.mHwHiStreamCHRManager.onStallDetect(curAppInfo.mScenceId, 107, this.mQoeCallback);
            }
        }
    }

    public void onWaveMappingReportCallback(int reportType, String networkName, int networkType) {
        HwHiStreamUtils.logD("onWaveMappingReportCallback");
        HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
        if (curAppInfo != null) {
            HwHiStreamUtils.logD("onWaveMappingReportCallback, app: " + curAppInfo.mAppId);
            IHwAPPQoECallback brainCallback = getArbitrationCallback();
            if (brainCallback != null) {
                brainCallback.onAPPQualityCallBack(curAppInfo, 107);
            }
        }
    }

    public void onWaveMappingRespondCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
    }

    public void onWaveMappingRespond4BackCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
    }
}
