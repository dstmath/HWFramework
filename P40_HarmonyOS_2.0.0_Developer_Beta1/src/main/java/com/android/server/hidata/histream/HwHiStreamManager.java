package com.android.server.hidata.histream;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.android.server.hidata.appqoe.HwAppQoeManager;
import com.android.server.hidata.appqoe.HwAppStateInfo;
import com.android.server.hidata.appqoe.IHwAppQoeCallback;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;

public class HwHiStreamManager implements IHwHiStreamCallback, IWaveMappingCallback {
    public static final int MSG_APP_MONITOR = 1;
    public static final int MSG_APP_STATE_START = 14;
    public static final int MSG_FOREGROUND_ACTIVITY_CHANGED = 13;
    public static final int MSG_NOTIFY_APP_STATE_CHANGE = 2;
    public static final int MSG_NO_DATA_DETECT_EVENT = 10;
    private static HwHiStreamManager sHwHiStreamManager = null;
    private Context mContext;
    private HwHiStreamContentAware mHwHiStreamContentAware = null;
    private HwHiStreamQoeMonitor mHwHiStreamQoeMonitor = null;
    private Handler mManagerHandler;

    private HwHiStreamManager(Context context) {
        this.mContext = context;
        initHiStreamManagerHandler();
        this.mHwHiStreamContentAware = HwHiStreamContentAware.createInstance(context, this, this.mManagerHandler);
        this.mHwHiStreamQoeMonitor = HwHiStreamQoeMonitor.createInstance(context, this.mManagerHandler);
        HwHiStreamUtils.logD(false, "HwHiStreamManager init success", new Object[0]);
    }

    public static HwHiStreamManager createInstance(Context context) {
        if (sHwHiStreamManager == null) {
            sHwHiStreamManager = new HwHiStreamManager(context);
        }
        return sHwHiStreamManager;
    }

    public static HwHiStreamManager getInstance() {
        return sHwHiStreamManager;
    }

    private void initHiStreamManagerHandler() {
        HandlerThread handlerThread = new HandlerThread("HwHiStreamManager_handler_thread");
        handlerThread.start();
        this.mManagerHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.hidata.histream.HwHiStreamManager.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    HwHiStreamManager.this.mHwHiStreamContentAware.handleAppMonitor();
                } else if (i == 2) {
                    HwHiStreamManager.this.mHwHiStreamContentAware.handleNotifyAppStateChange(msg);
                } else if (i == 10) {
                    HwHiStreamManager.this.mHwHiStreamContentAware.onNoDataDetected();
                } else if (i == 13) {
                    HwHiStreamManager.this.mHwHiStreamContentAware.handleForeActivityChange(msg);
                } else if (i == 14) {
                    HwHiStreamManager.this.mHwHiStreamQoeMonitor.handleAppStateStart(HwHiStreamManager.this.mHwHiStreamContentAware.getCurAppInfo());
                }
            }
        };
    }

    @Override // com.android.server.hidata.histream.IHwHiStreamCallback
    public void onAppStateChangeCallback(HwAppStateInfo stateInfo, int appState) {
        if (stateInfo == null) {
            HwHiStreamUtils.logD(false, "onAppStateChangeCallback, stateInfo is null", new Object[0]);
            return;
        }
        HwHiStreamUtils.logD(false, "onAppStateChangeCallback, %{public}s", stateInfo.toString());
        if (stateInfo.mScenesId == 100105 && HwAppQoeManager.isAppStartMonitor(stateInfo, this.mContext)) {
            this.mHwHiStreamQoeMonitor.onAppStateChange(stateInfo, appState);
        }
        IHwAppQoeCallback brainCallback = getArbitrationCallback();
        if (brainCallback != null) {
            brainCallback.onAppStateCallBack(stateInfo, appState);
        } else {
            HwHiStreamUtils.logD(false, "onAPPStateChangeCallback: brainCallback is null", new Object[0]);
        }
        IHwAppQoeCallback mVMCallback = getWaveMappingCallback();
        if (mVMCallback != null) {
            mVMCallback.onAppStateCallBack(stateInfo, appState);
        } else {
            HwHiStreamUtils.logD(false, "onAPPStateChangeCallback: mVMCallback is null", new Object[0]);
        }
    }

    public IHwAppQoeCallback getArbitrationCallback() {
        HwAppQoeManager hwAppQoeManager = HwAppQoeManager.getInstance();
        if (hwAppQoeManager != null) {
            return hwAppQoeManager.getAppQoeCallback(true);
        }
        return null;
    }

    public IHwAppQoeCallback getWaveMappingCallback() {
        HwAppQoeManager hwAppQoeManager = HwAppQoeManager.getInstance();
        if (hwAppQoeManager != null) {
            return hwAppQoeManager.getAppQoeCallback(false);
        }
        return null;
    }

    public HwAppStateInfo getCurStreamAppInfo() {
        return this.mHwHiStreamContentAware.getCurStreamAppInfo();
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingReportCallback(int reportType, String networkName, int networkType) {
        HwHiStreamUtils.logD(false, "enter onWaveMappingReportCallback", new Object[0]);
        HwAppStateInfo curAppInfo = getCurStreamAppInfo();
        if (curAppInfo != null) {
            HwHiStreamUtils.logD(false, "onWaveMappingReportCallback, app id is %{public}d", Integer.valueOf(curAppInfo.mAppId));
            IHwAppQoeCallback brainCallback = getArbitrationCallback();
            if (brainCallback != null) {
                brainCallback.onAppQualityCallBack(curAppInfo, 107);
            }
        }
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespondCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespond4BackCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
    }
}
