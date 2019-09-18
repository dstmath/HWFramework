package com.android.server.hidata.histream;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.appqoe.HwAPPQoEAPKConfig;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.systemui.shared.recents.hwutil.HwRecentsTaskUtils;

class HwHiStreamQoeMonitor implements IHwHiStreamJniCallback {
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
    private static final int MSG_APP_STATE_START = 1;
    private static final int MSG_APP_STATE_STOP = 2;
    private static final int MSG_DETECT_STALL = 3;
    private static final int MSG_QUERY_TRAFFIC = 5;
    private static final int MSG_START_AI_DETECT = 6;
    private static final int MSG_START_FRAME_DETECT = 4;
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
    private HwHidataJniAdapter mHwHidataJniAdapter;
    private long mLastAudioStallTime = 0;
    private long[] mLastCellularTraffic;
    private long[] mLastWifiTraffic;
    private Handler mManagerHandler;
    private Handler mQoeMonitorHandler;
    private long mStartTime = 0;
    private int monitoringSceneId = -1;
    private int monitoringUid = -1;

    private HwHiStreamQoeMonitor(Context context, Handler handler) {
        this.mManagerHandler = handler;
        this.mHwHidataJniAdapter = HwHidataJniAdapter.getInstance();
        initQoeMonitorHandler();
        if (this.mHwHidataJniAdapter != null) {
            this.mHwHidataJniAdapter.registerHiStreamJniCallback(this);
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

    private void initQoeMonitorHandler() {
        HandlerThread handlerThread = new HandlerThread("HwHiStreamQoeMonitor_handler_thread");
        handlerThread.start();
        this.mQoeMonitorHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        HwHiStreamQoeMonitor.this.handleAppStateStart(msg);
                        return;
                    case 2:
                        HwHiStreamQoeMonitor.this.handleAppStateStop(msg);
                        return;
                    case 3:
                        HwHiStreamQoeMonitor.this.handleStallDetect(msg);
                        return;
                    case 4:
                        HwHiStreamQoeMonitor.this.handleStartFrameDetect();
                        return;
                    case 5:
                        HwHiStreamQoeMonitor.this.handleQueryTraffic();
                        return;
                    case 6:
                        HwHiStreamQoeMonitor.this.handleStartAIDetect(msg);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    /* access modifiers changed from: private */
    public void handleQueryTraffic() {
        if (-1 != this.monitoringSceneId && this.mLastWifiTraffic != null && this.mLastCellularTraffic != null && 2 <= this.mLastWifiTraffic.length && 2 <= this.mLastCellularTraffic.length) {
            long[] curWifiTraffic = getCurTraffic(800);
            long[] curCellularTraffic = getCurTraffic(801);
            if (curWifiTraffic != null && curCellularTraffic != null && 2 <= curWifiTraffic.length && 2 <= curCellularTraffic.length && this.mLastWifiTraffic != null && this.mLastCellularTraffic != null && 2 <= this.mLastWifiTraffic.length && 2 <= this.mLastCellularTraffic.length) {
                long celluarTxTraffic = 0;
                long wifiRxTraffic = (0 == curWifiTraffic[0] || 0 == this.mLastWifiTraffic[0]) ? 0 : curWifiTraffic[0] - this.mLastWifiTraffic[0];
                long cellularRxTraffic = (0 == curCellularTraffic[0] || 0 == this.mLastCellularTraffic[0]) ? 0 : curCellularTraffic[0] - this.mLastCellularTraffic[0];
                long wifiTxTraffic = (0 == curWifiTraffic[1] || 0 == this.mLastWifiTraffic[1]) ? 0 : curWifiTraffic[1] - this.mLastWifiTraffic[1];
                if (!(0 == curCellularTraffic[1] || 0 == this.mLastCellularTraffic[1])) {
                    celluarTxTraffic = curCellularTraffic[1] - this.mLastCellularTraffic[1];
                }
                HwHiStreamUtils.logD("handleQueryTraffic: RxWifi= " + wifiRxTraffic + ",Rxcellular=" + cellularRxTraffic + ",TXwifi= " + wifiTxTraffic + ",Txcellular=" + celluarTxTraffic);
                Bundle bundle = new Bundle();
                bundle.putInt("wifiRxTraffic", (int) wifiRxTraffic);
                bundle.putInt("cellularRxTraffic", (int) cellularRxTraffic);
                bundle.putInt("wifiTxTraffic", (int) wifiTxTraffic);
                bundle.putInt("celluarTxTraffic", (int) celluarTxTraffic);
                bundle.putInt("monitoringUid", this.monitoringUid);
                this.mManagerHandler.sendMessage(this.mManagerHandler.obtainMessage(11, bundle));
                this.mLastWifiTraffic = curWifiTraffic;
                this.mLastCellularTraffic = curCellularTraffic;
                this.mQoeMonitorHandler.removeMessages(5);
                this.mQoeMonitorHandler.sendEmptyMessageDelayed(5, 2000);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleStartFrameDetect() {
        if (100106 == this.monitoringSceneId) {
            this.mDetectAlgo = 1;
            this.mHwHidataJniAdapter.startFrameDetect(this.monitoringSceneId);
        }
    }

    /* access modifiers changed from: private */
    public void handleStartAIDetect(Message msg) {
        if (-1 != this.monitoringSceneId) {
            this.mHwHidataJniAdapter.sendStallDetectCmd(24, this.monitoringUid, this.monitoringSceneId, this.mAlgoPara, ((Bundle) msg.obj).getInt("userType"));
        }
    }

    /* access modifiers changed from: private */
    public void handleAppStateStart(Message msg) {
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int uid = bundle.getInt("uid");
            int appSceneId = bundle.getInt("appSceneId");
            int stallThreshold = bundle.getInt("stallThreshold");
            int userType = bundle.getInt("userType");
            HwHiStreamUtils.logD("handleAppStateStart: appSceneId = " + appSceneId + ",stallThreshold = " + stallThreshold);
            this.monitoringSceneId = appSceneId;
            this.monitoringUid = uid;
            this.mDetectAlgo = 0;
            this.mStartTime = System.currentTimeMillis();
            if (100106 == this.monitoringSceneId) {
                this.mQoeMonitorHandler.removeMessages(4);
                this.mQoeMonitorHandler.sendEmptyMessageDelayed(4, 6100);
                this.mFrameDetectThreshold = stallThreshold;
                this.mFrameDetectGeneralTH = getAppStallThreshold(1, this.monitoringSceneId);
                this.mAlgoPara = 3;
            } else {
                this.mAlgoPara = stallThreshold;
            }
            if (-1 == this.mHwHidataJniAdapter.sendStallDetectCmd(24, uid, appSceneId, this.mAlgoPara, userType)) {
                this.mQoeMonitorHandler.sendMessageDelayed(this.mQoeMonitorHandler.obtainMessage(6, bundle), 1000);
            }
            this.lastFrameReportTime = 0;
            this.mLastAudioStallTime = 0;
            this.mLastWifiTraffic = getCurTraffic(800);
            this.mLastCellularTraffic = getCurTraffic(801);
            this.mQoeMonitorHandler.removeMessages(5);
            this.mQoeMonitorHandler.sendEmptyMessageDelayed(5, 2000);
        }
    }

    /* access modifiers changed from: private */
    public void handleAppStateStop(Message msg) {
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int uid = bundle.getInt("uid");
            int appSceneId = bundle.getInt("appSceneId");
            HwHiStreamUtils.logD("handleAppStateStop: appSceneId = " + appSceneId);
            this.monitoringSceneId = -1;
            this.monitoringUid = -1;
            this.mStartTime = 0;
            if (1 == this.mDetectAlgo) {
                this.mHwHidataJniAdapter.stopFrameDetect();
            }
            this.mHwHidataJniAdapter.sendStallDetectCmd(25, uid, appSceneId, this.mAlgoPara, 0);
            this.mQoeMonitorHandler.removeMessages(5);
            this.mLastWifiTraffic = null;
            this.mLastCellularTraffic = null;
        }
    }

    public void handleStallDetect(Message msg) {
        long curTime;
        Message message = msg;
        if (message != null && message.obj != null) {
            Bundle bundle = (Bundle) message.obj;
            int stallTime = bundle.getInt("stallTime");
            int appScene = bundle.getInt("appScene");
            int algo = bundle.getInt("algo");
            int frameDetectTH = -1;
            HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
            if (this.monitoringSceneId != appScene || this.mDetectAlgo != algo) {
                Bundle bundle2 = bundle;
            } else if (mHwHiStreamNetworkMonitor == null) {
                Bundle bundle3 = bundle;
            } else if ((100106 == appScene || 100105 == appScene) && 1 == stallTime) {
                this.mManagerHandler.sendEmptyMessage(10);
            } else {
                long curTime2 = System.currentTimeMillis();
                long handoverDelay = curTime2 - mHwHiStreamNetworkMonitor.mLastHandoverTime;
                if (1 == algo) {
                    frameDetectTH = 800 == mHwHiStreamNetworkMonitor.getCurrNetworkType(this.monitoringUid) ? this.mFrameDetectThreshold : this.mFrameDetectGeneralTH;
                    if (stallTime >= frameDetectTH && 6000 <= curTime2 - this.lastFrameReportTime) {
                        this.lastFrameReportTime = curTime2;
                    } else {
                        return;
                    }
                }
                if (100105 == appScene) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("AUDIO stall interval = ");
                    Bundle bundle4 = bundle;
                    sb.append(System.currentTimeMillis() - this.mLastAudioStallTime);
                    HwHiStreamUtils.logD(sb.toString());
                    curTime = curTime2;
                    if (MemoryConstant.MIN_INTERVAL_OP_TIMEOUT < System.currentTimeMillis() - this.mLastAudioStallTime) {
                        this.mLastAudioStallTime = System.currentTimeMillis();
                        return;
                    }
                    this.mLastAudioStallTime = System.currentTimeMillis();
                } else {
                    curTime = curTime2;
                }
                HwHiStreamUtils.logD("handleStallDetect: detect stall, appScene = " + appScene + ",curAppScene=" + this.monitoringSceneId + ",algo=" + algo + ",detectResult =" + stallTime + ",frameDetectTH=" + frameDetectTH);
                if (HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME <= handoverDelay || curTime - this.mStartTime <= handoverDelay || !(100106 == appScene || 100105 == appScene)) {
                    Bundle bundle5 = new Bundle();
                    bundle5.putInt("appSceneId", this.monitoringSceneId);
                    bundle5.putInt("detectResult", (stallTime * 100) / VIDEO_STALL_REPORT_MIN_INTERVAL);
                    this.mManagerHandler.sendMessage(this.mManagerHandler.obtainMessage(9, bundle5));
                }
            }
        }
    }

    private int getAppStallThreshold(int userType, int sceneceId) {
        HwAPPQoEResourceManger mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
        int threshold = -1;
        if (mHwAPPQoEResourceManger != null) {
            HwAPPQoEAPKConfig aPKScenceConfig = mHwAPPQoEResourceManger.getAPKScenceConfig(sceneceId);
            HwAPPQoEAPKConfig apkConfig = aPKScenceConfig;
            if (aPKScenceConfig != null) {
                threshold = 1 == userType ? apkConfig.mGeneralStallTH : apkConfig.mAggressiveStallTH;
            }
        }
        if (100106 == sceneceId) {
            return -1 == threshold ? FRAME_DETECT_DEFAULT_THRESHOLD : (threshold * VIDEO_STALL_REPORT_MIN_INTERVAL) / 100;
        } else if (100501 != sceneceId && 100701 != sceneceId) {
            return -1;
        } else {
            return -1 == threshold ? DOUYIN_DEFAULT_THRESHOLD : threshold;
        }
    }

    public void onAPPStateChange(HwAPPStateInfo stateInfo, int appState) {
        if (stateInfo == null) {
            HwHiStreamUtils.logE("onAPPStateChange:stateInfo is null");
            return;
        }
        Bundle bundle = new Bundle();
        int appSceneId = stateInfo.mScenceId;
        if (100 == appState || 103 == appState) {
            int stallThreshold = getAppStallThreshold(stateInfo.mUserType, appSceneId);
            if (-1 == this.monitoringSceneId) {
                bundle.putInt("uid", stateInfo.mAppUID);
                bundle.putInt("appSceneId", appSceneId);
                bundle.putInt("stallThreshold", stallThreshold);
                bundle.putInt("userType", stateInfo.mUserType);
                this.mQoeMonitorHandler.sendMessage(this.mQoeMonitorHandler.obtainMessage(1, bundle));
            } else if (appSceneId != this.monitoringSceneId) {
                bundle.putInt("uid", this.monitoringUid);
                bundle.putInt("appSceneId", this.monitoringSceneId);
                this.mQoeMonitorHandler.sendMessage(this.mQoeMonitorHandler.obtainMessage(2, bundle));
                Bundle bundle1 = new Bundle();
                bundle1.putInt("uid", stateInfo.mAppUID);
                bundle1.putInt("appSceneId", appSceneId);
                bundle1.putInt("stallThreshold", stallThreshold);
                bundle1.putInt("userType", stateInfo.mUserType);
                this.mQoeMonitorHandler.sendMessageDelayed(this.mQoeMonitorHandler.obtainMessage(1, bundle1), 500);
            }
        } else if ((101 == appState || (104 == appState && 100106 == appSceneId)) && appSceneId == this.monitoringSceneId) {
            bundle.putInt("uid", stateInfo.mAppUID);
            bundle.putInt("appSceneId", appSceneId);
            this.mQoeMonitorHandler.sendMessage(this.mQoeMonitorHandler.obtainMessage(2, bundle));
        }
    }

    private long[] getCurTraffic(int network) {
        long[] traffic = new long[2];
        if (100106 != this.monitoringSceneId && 100105 != this.monitoringSceneId) {
            return traffic;
        }
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (mHwHiStreamNetworkMonitor == null || network != mHwHiStreamNetworkMonitor.getCurrNetworkType(this.monitoringUid)) {
            return traffic;
        }
        return this.mHwHidataJniAdapter.getCurrTotalTraffic();
    }

    public void onStallInfoReportCallback(int stallTime, int appScene, int algo, int isDouyinStart) {
        if (isDouyinStart == 0 || this.mManagerHandler == null) {
            Bundle bundle = new Bundle();
            bundle.putInt("stallTime", stallTime);
            bundle.putInt("appScene", appScene);
            bundle.putInt("algo", algo);
            this.mQoeMonitorHandler.sendMessage(this.mQoeMonitorHandler.obtainMessage(3, bundle));
            return;
        }
        this.mManagerHandler.sendEmptyMessage(14);
    }

    public int getCurTcpRxPackets(int uid) {
        int tcpRxPackets = -1;
        int[] result = this.mHwHidataJniAdapter.sendQoECmd(23, uid);
        if (result != null && result.length > 7) {
            tcpRxPackets = result[7];
        }
        HwHiStreamUtils.logD("getCurTcpRxPackets:tcpRxPackets = " + tcpRxPackets);
        return tcpRxPackets;
    }
}
