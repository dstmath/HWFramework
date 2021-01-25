package com.android.server.hidata.histream;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.appqoe.HwAPPQoEAPKConfig;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;

/* access modifiers changed from: package-private */
public class HwHiStreamQoeMonitor implements IHwHiStreamJniCallback {
    private static final int AI_DETECT_START_DELAY = 1000;
    private static final int ALGO_PARA_100MS_40 = 0;
    private static final int ALGO_PARA_100MS_80 = 1;
    private static final int ALGO_PARA_2S = 3;
    private static final int AUDIO_STALL_REPORT_MIN_INTERVAL = 10000;
    private static final int CMD_QUERY_HIDATA_INFO = 23;
    private static final int CMD_START_STALL_MONITOR = 24;
    private static final int CMD_STOP_AI_ALGO = 26;
    private static final int CMD_STOP_STALL_MONITOR = 25;
    private static final int DETECT_ALGO_FRAME_DETECT = 1;
    private static final int DETECT_ALGO_UDP_AI = 0;
    private static final int DETECT_RESULT_NO_DATA = 1;
    private static final int DOUYIN_DEFAULT_THRESHOLD = 286339651;
    private static final int FRAME_DETECT_DEFAULT_THRESHOLD = 1800;
    private static final int FRAME_DETECT_START_DELAY = 6100;
    private static final int HICURE_REPORT_NO_RX_COUNT = 3;
    private static final int QUERY_TRAFFIC_INTERVAL = 2000;
    private static final int STALL_DETECT_AFTER_HANDOVER_DELAY = 20000;
    private static final int TCP_RX_PACKETS_POSISTION = 7;
    private static final int VIDEO_STALL_REPORT_MIN_INTERVAL = 6000;
    private static HwHiStreamQoeMonitor mHwHiStreamQoeMonitor;
    private long lastFrameReportTime = 0;
    private int mAlgoPara = 3;
    private int mDetectAlgo = 0;
    private int mFrameDetectGeneralTH = FRAME_DETECT_DEFAULT_THRESHOLD;
    private int mFrameDetectThreshold = FRAME_DETECT_DEFAULT_THRESHOLD;
    private HwHiStreamUdpMonitor mHwHiStreamUdpMonitor;
    private HwHidataJniAdapter mHwHidataJniAdapter;
    private long mLastAudioStallTime = 0;
    private long[] mLastCellularTraffic;
    private long[] mLastWifiTraffic;
    private Handler mManagerHandler;
    private long mStartTime = 0;
    private int monitoringSceneId = -1;
    private int monitoringUid = -1;

    private HwHiStreamQoeMonitor(Context context, Handler handler) {
        this.mManagerHandler = handler;
        this.mHwHidataJniAdapter = HwHidataJniAdapter.getInstance();
        this.mHwHiStreamUdpMonitor = HwHiStreamUdpMonitor.getInstance(context, handler);
        HwHidataJniAdapter hwHidataJniAdapter = this.mHwHidataJniAdapter;
        if (hwHidataJniAdapter != null) {
            hwHidataJniAdapter.registerHiStreamJniCallback(this);
        }
    }

    public static HwHiStreamQoeMonitor createInstance(Context context, Handler handler) {
        if (mHwHiStreamQoeMonitor == null) {
            mHwHiStreamQoeMonitor = new HwHiStreamQoeMonitor(context, handler);
        }
        return mHwHiStreamQoeMonitor;
    }

    public static HwHiStreamQoeMonitor getInstance() {
        return mHwHiStreamQoeMonitor;
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00de  */
    public void handleQueryTraffic() {
        long[] jArr;
        long[] jArr2;
        long[] jArr3;
        long[] jArr4;
        long cellularRxTraffic;
        long wifiTxTraffic;
        HwHiStreamCHRManager mHwHiStreamCHRManager;
        if (-1 != this.monitoringSceneId && (jArr = this.mLastWifiTraffic) != null && (jArr2 = this.mLastCellularTraffic) != null && 2 <= jArr.length && 2 <= jArr2.length) {
            long[] curWifiTraffic = getCurTraffic(800);
            long[] curCellularTraffic = getCurTraffic(801);
            if (curWifiTraffic != null && curCellularTraffic != null && 2 <= curWifiTraffic.length && 2 <= curCellularTraffic.length && (jArr3 = this.mLastWifiTraffic) != null && (jArr4 = this.mLastCellularTraffic) != null && 2 <= jArr3.length && 2 <= jArr4.length) {
                long celluarTxTraffic = 0;
                long wifiRxTraffic = (0 == curWifiTraffic[0] || 0 == jArr3[0]) ? 0 : curWifiTraffic[0] - jArr3[0];
                if (0 != curCellularTraffic[0]) {
                    long[] jArr5 = this.mLastCellularTraffic;
                    if (0 != jArr5[0]) {
                        cellularRxTraffic = curCellularTraffic[0] - jArr5[0];
                        if (0 != curWifiTraffic[1]) {
                            long[] jArr6 = this.mLastWifiTraffic;
                            if (0 != jArr6[1]) {
                                wifiTxTraffic = curWifiTraffic[1] - jArr6[1];
                                if (0 != curCellularTraffic[1]) {
                                    long[] jArr7 = this.mLastCellularTraffic;
                                    if (0 != jArr7[1]) {
                                        celluarTxTraffic = curCellularTraffic[1] - jArr7[1];
                                    }
                                }
                                if (isVoipApp(this.monitoringSceneId)) {
                                    HwHiStreamUtils.logD(false, "handleQueryTraffic: RxWifi= %{public}s,Rxcellular=%{public}s, TXwifi= %{public}s, Txcellular=%{public}s", String.valueOf(wifiRxTraffic), String.valueOf(cellularRxTraffic), String.valueOf(wifiTxTraffic), String.valueOf(celluarTxTraffic));
                                }
                                mHwHiStreamCHRManager = HwHiStreamCHRManager.getInstance();
                                if (mHwHiStreamCHRManager != null) {
                                    mHwHiStreamCHRManager.onUpdateQuality((int) wifiRxTraffic, (int) cellularRxTraffic, (int) wifiTxTraffic, (int) celluarTxTraffic, this.monitoringUid);
                                }
                                this.mLastWifiTraffic = curWifiTraffic;
                                this.mLastCellularTraffic = curCellularTraffic;
                                this.mManagerHandler.removeMessages(11);
                                this.mManagerHandler.sendEmptyMessageDelayed(11, 2000);
                            }
                        }
                        wifiTxTraffic = 0;
                        if (0 != curCellularTraffic[1]) {
                        }
                        if (isVoipApp(this.monitoringSceneId)) {
                        }
                        mHwHiStreamCHRManager = HwHiStreamCHRManager.getInstance();
                        if (mHwHiStreamCHRManager != null) {
                        }
                        this.mLastWifiTraffic = curWifiTraffic;
                        this.mLastCellularTraffic = curCellularTraffic;
                        this.mManagerHandler.removeMessages(11);
                        this.mManagerHandler.sendEmptyMessageDelayed(11, 2000);
                    }
                }
                cellularRxTraffic = 0;
                if (0 != curWifiTraffic[1]) {
                }
                wifiTxTraffic = 0;
                if (0 != curCellularTraffic[1]) {
                }
                if (isVoipApp(this.monitoringSceneId)) {
                }
                mHwHiStreamCHRManager = HwHiStreamCHRManager.getInstance();
                if (mHwHiStreamCHRManager != null) {
                }
                this.mLastWifiTraffic = curWifiTraffic;
                this.mLastCellularTraffic = curCellularTraffic;
                this.mManagerHandler.removeMessages(11);
                this.mManagerHandler.sendEmptyMessageDelayed(11, 2000);
            }
        }
    }

    public void handleStartFrameDetect() {
        int i = this.monitoringSceneId;
        if (i == 100106) {
            this.mDetectAlgo = 1;
            this.mHwHidataJniAdapter.startFrameDetect(i);
        }
    }

    private void notifyAppStateChange(HwAPPStateInfo stateInfo, int appSceneId, int appState) {
        HwAPPQoEManager hwAPPQoEManager;
        if (stateInfo != null) {
            HwAPPStateInfo appStateInfo = new HwAPPStateInfo();
            appStateInfo.copyObjectValue(stateInfo);
            appStateInfo.mScenceId = appSceneId;
            appStateInfo.mAppState = appState;
            if ((appSceneId == 100105 || appSceneId == 100106 || appSceneId == 101101) && (hwAPPQoEManager = HwAPPQoEManager.getInstance()) != null) {
                hwAPPQoEManager.notifyGameQoeCallback(appStateInfo, appState);
            }
        }
    }

    public void handleAppStateStart(HwAPPStateInfo stateInfo) {
        if (stateInfo != null) {
            this.monitoringSceneId = stateInfo.mScenceId;
            this.monitoringUid = stateInfo.mAppUID;
            this.mDetectAlgo = 0;
            this.mStartTime = System.currentTimeMillis();
            int stallThreshold = getAppStallThreshold(stateInfo.mUserType, stateInfo.mScenceId);
            HwHiStreamUtils.logD(false, "handleAppStateStart: appSceneId = %{public}d,stallThreshold = %{public}d", Integer.valueOf(this.monitoringSceneId), Integer.valueOf(stallThreshold));
            if (this.monitoringSceneId == 100106) {
                this.mManagerHandler.removeMessages(15);
                this.mManagerHandler.sendEmptyMessageDelayed(15, 6100);
                this.mFrameDetectThreshold = stallThreshold;
                this.mFrameDetectGeneralTH = getAppStallThreshold(1, this.monitoringSceneId);
                this.mAlgoPara = 3;
                this.mHwHiStreamUdpMonitor.startMonitor(this.monitoringUid, this.monitoringSceneId);
            } else {
                this.mAlgoPara = stallThreshold;
            }
            notifyAppStateChange(stateInfo, stateInfo.mScenceId, stateInfo.mAppState);
            this.mHwHidataJniAdapter.sendStallDetectCmd(24, this.monitoringUid, this.monitoringSceneId, this.mAlgoPara, stateInfo.mUserType);
            this.lastFrameReportTime = 0;
            this.mLastAudioStallTime = 0;
            this.mLastWifiTraffic = getCurTraffic(800);
            this.mLastCellularTraffic = getCurTraffic(801);
            this.mManagerHandler.removeMessages(11);
            this.mManagerHandler.sendEmptyMessageDelayed(11, 2000);
        }
    }

    private void handleAppStateStop(int uid, int appSceneId) {
        HwHiStreamUtils.logD(false, "handleAppStateStop: appSceneId = %{public}d", Integer.valueOf(appSceneId));
        this.monitoringSceneId = -1;
        this.monitoringUid = -1;
        this.mStartTime = 0;
        if (this.mDetectAlgo == 1) {
            this.mHwHidataJniAdapter.stopFrameDetect();
        }
        this.mHwHidataJniAdapter.sendStallDetectCmd(25, uid, appSceneId, this.mAlgoPara, 0);
        this.mManagerHandler.removeMessages(11);
        this.mLastWifiTraffic = null;
        this.mLastCellularTraffic = null;
        this.mHwHiStreamUdpMonitor.stopMonitor();
    }

    private int getAppStallThreshold(int userType, int sceneceId) {
        HwAPPQoEAPKConfig apkConfig;
        HwAPPQoEResourceManger mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
        int threshold = -1;
        if (!(mHwAPPQoEResourceManger == null || (apkConfig = mHwAPPQoEResourceManger.getAPKScenceConfig(sceneceId)) == null)) {
            threshold = 1 == userType ? apkConfig.mGeneralStallTH : apkConfig.mAggressiveStallTH;
        }
        if (100106 == sceneceId) {
            return -1 == threshold ? FRAME_DETECT_DEFAULT_THRESHOLD : (threshold * VIDEO_STALL_REPORT_MIN_INTERVAL) / 100;
        } else if (isShortVideoApp(sceneceId)) {
            return -1 == threshold ? DOUYIN_DEFAULT_THRESHOLD : threshold;
        } else {
            HwHiStreamUtils.logD(false, "ignore the threshold", new Object[0]);
            return -1;
        }
    }

    public void onAPPStateChange(HwAPPStateInfo stateInfo, int appState) {
        if (stateInfo == null) {
            HwHiStreamUtils.logE(false, "onAPPStateChange:stateInfo is null", new Object[0]);
            return;
        }
        new Bundle();
        int appSceneId = stateInfo.mScenceId;
        if (100 == appState || 103 == appState) {
            int i = this.monitoringSceneId;
            if (i == -1) {
                handleAppStateStart(stateInfo);
            } else if (appSceneId != i) {
                notifyAppStateChange(stateInfo, i, 101);
                handleAppStateStop(this.monitoringUid, this.monitoringSceneId);
                this.mManagerHandler.sendEmptyMessageDelayed(14, 1000);
            } else {
                notifyAppStateChange(stateInfo, appSceneId, stateInfo.mAppState);
            }
        } else if (101 == appState || (104 == appState && 100106 == appSceneId)) {
            if (appSceneId == this.monitoringSceneId) {
                notifyAppStateChange(stateInfo, appSceneId, stateInfo.mAppState);
                handleAppStateStop(this.monitoringUid, this.monitoringSceneId);
            }
        } else if (appState != 104) {
            HwHiStreamUtils.logD(false, "onAPPStateChange:other appState", new Object[0]);
        } else if (appSceneId == this.monitoringSceneId) {
            notifyAppStateChange(stateInfo, appSceneId, stateInfo.mAppState);
        }
    }

    private long[] getCurTraffic(int network) {
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor;
        long[] traffic = new long[2];
        int i = this.monitoringSceneId;
        if ((100106 == i || 100105 == i) && (mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance()) != null && network == mHwHiStreamNetworkMonitor.getCurrNetworkType(this.monitoringUid)) {
            return this.mHwHidataJniAdapter.getCurrTotalTraffic();
        }
        return traffic;
    }

    @Override // com.android.server.hidata.histream.IHwHiStreamJniCallback
    public void onStallInfoReportCallback(int stallTime, int appScene, int algo, boolean isVideoStart) {
        if (appScene != this.monitoringSceneId) {
            HwHiStreamUtils.logD(false, "App not match, appScene = %{public}d,curAppScene = %{public}d", Integer.valueOf(appScene), Integer.valueOf(this.monitoringSceneId));
        } else if (isVoipApp(appScene) && stallTime == 1) {
            this.mManagerHandler.sendEmptyMessage(10);
        } else if (appScene != 100501 || !isVideoStart) {
            boolean isStall = true;
            switch (appScene) {
                case HwAPPQoEUtils.SCENE_AUDIO /* 100105 */:
                    isStall = isAudioStall();
                    break;
                case HwAPPQoEUtils.SCENE_VIDEO /* 100106 */:
                    isStall = isVideoStall(stallTime, algo);
                    break;
            }
            if (isVoipApp(appScene) && isInHandoverProtect()) {
                isStall = false;
            }
            if (isStall) {
                HwHiStreamUtils.logD(false, "detect stall, appScene = %{public}d", Integer.valueOf(this.monitoringSceneId));
                Bundle bundle = new Bundle();
                bundle.putInt("appSceneId", appScene);
                bundle.putInt("detectResult", (stallTime * 100) / VIDEO_STALL_REPORT_MIN_INTERVAL);
                Handler handler = this.mManagerHandler;
                handler.sendMessage(handler.obtainMessage(9, bundle));
            }
        } else {
            this.mManagerHandler.sendEmptyMessage(17);
        }
    }

    private boolean isShortVideoApp(int sceneceId) {
        if (sceneceId == 100501 || sceneceId == 100901 || sceneceId == 100701) {
            return true;
        }
        return false;
    }

    private boolean isVoipApp(int sceneceId) {
        if (sceneceId == 100106 || sceneceId == 100105) {
            return true;
        }
        return false;
    }

    private boolean isVideoStall(int stallTime, int algo) {
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (mHwHiStreamNetworkMonitor == null || algo != this.mDetectAlgo) {
            HwHiStreamUtils.logD(false, "isVideoStall:algo mismatch,ignore", new Object[0]);
            return false;
        }
        long curTime = System.currentTimeMillis();
        int frameDetectThreshold = mHwHiStreamNetworkMonitor.getCurrNetworkType(this.monitoringUid) == 800 ? this.mFrameDetectThreshold : this.mFrameDetectGeneralTH;
        if (stallTime <= frameDetectThreshold || curTime - this.lastFrameReportTime < 6000) {
            return false;
        }
        this.lastFrameReportTime = curTime;
        HwHiStreamUtils.logD(false, "isVideoStall: wechat video detect stall, frameDetectTH = %{public}d", Integer.valueOf(frameDetectThreshold));
        return true;
    }

    private boolean isAudioStall() {
        long curTime = System.currentTimeMillis();
        long stallInterval = curTime - this.mLastAudioStallTime;
        boolean isStall = false;
        if (stallInterval < HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM) {
            isStall = true;
        }
        HwHiStreamUtils.logD(false, "isAudioStall:stallInterval = %{public}d", Long.valueOf(stallInterval));
        this.mLastAudioStallTime = curTime;
        return isStall;
    }

    private boolean isInHandoverProtect() {
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (mHwHiStreamNetworkMonitor == null) {
            return false;
        }
        long curTime = System.currentTimeMillis();
        long handoverDelay = curTime - mHwHiStreamNetworkMonitor.mLastHandoverTime;
        if (handoverDelay >= 20000 || handoverDelay >= curTime - this.mStartTime) {
            return false;
        }
        HwHiStreamUtils.logD(false, "handleStallDetect: handover protect,ignore stall", new Object[0]);
        return true;
    }

    public int getCurTcpRxPackets(int uid) {
        int tcpRxPackets = -1;
        int[] result = this.mHwHidataJniAdapter.sendQoECmd(23, uid);
        if (result != null && result.length > 7) {
            tcpRxPackets = result[7];
        }
        HwHiStreamUtils.logD(false, "getCurTcpRxPackets:tcpRxPackets = %{public}d", Integer.valueOf(tcpRxPackets));
        return tcpRxPackets;
    }
}
