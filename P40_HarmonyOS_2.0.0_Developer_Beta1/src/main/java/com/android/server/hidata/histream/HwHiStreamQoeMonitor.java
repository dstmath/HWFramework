package com.android.server.hidata.histream;

import android.content.Context;
import android.os.Handler;
import com.android.server.hidata.appqoe.HwAppQoeManager;
import com.android.server.hidata.appqoe.HwAppStateInfo;

public class HwHiStreamQoeMonitor {
    private static final int AI_DETECT_START_DELAY = 1000;
    private static HwHiStreamQoeMonitor sHwHiStreamQoeMonitor;
    private Handler mManagerHandler;
    private int mMonitoringSceneId = -1;
    private int mMonitoringUid = -1;

    private HwHiStreamQoeMonitor(Context context, Handler handler) {
        this.mManagerHandler = handler;
    }

    public static HwHiStreamQoeMonitor createInstance(Context context, Handler handler) {
        if (sHwHiStreamQoeMonitor == null) {
            sHwHiStreamQoeMonitor = new HwHiStreamQoeMonitor(context, handler);
        }
        return sHwHiStreamQoeMonitor;
    }

    public static HwHiStreamQoeMonitor getInstance() {
        return sHwHiStreamQoeMonitor;
    }

    private void notifyAppStateChange(HwAppStateInfo stateInfo, int appSceneId, int appState) {
        HwAppQoeManager hwAPPQoEManager;
        if (stateInfo != null) {
            HwAppStateInfo appStateInfo = new HwAppStateInfo();
            appStateInfo.copyObjectValue(stateInfo);
            appStateInfo.mScenesId = appSceneId;
            appStateInfo.mAppState = appState;
            if ((appSceneId == 100105 || appSceneId == 100106 || appSceneId == 101101) && (hwAPPQoEManager = HwAppQoeManager.getInstance()) != null) {
                hwAPPQoEManager.notifyGameQoeCallback(appStateInfo, appState);
            }
        }
    }

    public void handleAppStateStart(HwAppStateInfo stateInfo) {
        if (stateInfo != null) {
            this.mMonitoringSceneId = stateInfo.mScenesId;
            this.mMonitoringUid = stateInfo.mAppUid;
            HwHiStreamUtils.logD(false, "handleAppStateStart, appSceneId = %{public}d,", Integer.valueOf(this.mMonitoringSceneId));
            notifyAppStateChange(stateInfo, stateInfo.mScenesId, stateInfo.mAppState);
        }
    }

    private void handleAppStateStop(int uid, int appSceneId) {
        HwHiStreamUtils.logD(false, "handleAppStateStop, appSceneId = %{public}d", Integer.valueOf(appSceneId));
        this.mMonitoringSceneId = -1;
        this.mMonitoringUid = -1;
    }

    public void onAppStateChange(HwAppStateInfo stateInfo, int appState) {
        if (stateInfo == null) {
            HwHiStreamUtils.logE(false, "onAppStateChange, stateInfo is null", new Object[0]);
            return;
        }
        int appSceneId = stateInfo.mScenesId;
        if (appState == 100 || appState == 103) {
            int i = this.mMonitoringSceneId;
            if (i == -1) {
                handleAppStateStart(stateInfo);
            } else if (appSceneId != i) {
                notifyAppStateChange(stateInfo, i, 101);
                handleAppStateStop(this.mMonitoringUid, this.mMonitoringSceneId);
                this.mManagerHandler.sendEmptyMessageDelayed(14, 1000);
            } else {
                notifyAppStateChange(stateInfo, appSceneId, stateInfo.mAppState);
            }
        } else if (appState == 101 || (appState == 104 && appSceneId == 100106)) {
            if (appSceneId == this.mMonitoringSceneId) {
                notifyAppStateChange(stateInfo, appSceneId, stateInfo.mAppState);
                handleAppStateStop(this.mMonitoringUid, this.mMonitoringSceneId);
            }
        } else if (appState != 104) {
            HwHiStreamUtils.logD(false, "onAppStateChange, other appState", new Object[0]);
        } else if (appSceneId == this.mMonitoringSceneId) {
            notifyAppStateChange(stateInfo, appSceneId, stateInfo.mAppState);
        }
    }
}
