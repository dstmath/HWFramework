package com.android.server.wifi.hwcoex;

import android.content.Context;
import android.os.Message;
import java.util.List;

public class HiCoexCellularScore extends HiCoexScore {
    public static final int SCENE_AIRPLANE_ON = 1;
    private static final int SCENE_AIRPLANE_ON_SCORE = 2;
    public static final int SCENE_DOUBLE_LINK = 512;
    private static final int SCENE_DOUBLE_LINK_SCORE = 34;
    public static final int SCENE_LOWLATENCY = 1024;
    private static final int SCENE_LOWLATENCY_SCORE = 52;
    public static final int SCENE_NO_SERVICES = 4;
    private static final int SCENE_NO_SERVICES_SCORE = 4;
    public static final int SCENE_NSA_DATA_CONNECTED = 16;
    private static final int SCENE_NSA_DATA_CONNECTED_SCORE = 16;
    public static final int SCENE_NSA_DATA_UNCONNECTED = 8;
    private static final int SCENE_NSA_DATA_UNCONNECTED_SCORE = 4;
    public static final int SCENE_PHONE_CALLING = 2048;
    private static final int SCENE_PHONE_CALLING_SCORE = 52;
    public static final int SCENE_SA_DATA_CONNECTED = 64;
    private static final int SCENE_SA_DATA_CONNECTED_SCORE = 24;
    public static final int SCENE_SA_DATA_UNCONNECTED = 32;
    private static final int SCENE_SA_DATA_UNCONNECTED_SCORE = 22;
    public static final int SCENE_SIM_ABSENT = 2;
    private static final int SCENE_SIM_ABSENT_SCORE = 4;
    public static final int SCENE_VOWIFI_CALLING = 128;
    private static final int SCENE_VOWIFI_CALLING_SCORE = 62;
    public static final int SCENE_WIFI_DISPLAY = 256;
    private static final int SCENE_WIFI_DISPLAY_SCORE = 25;
    private static final int SCORE_DEFAULT = 2;
    private static final String TAG = "HiCoexCellularScore";
    private HiCoexReceiver mHiCoexReceiver = null;
    private boolean mIsDisplayEnabled = false;

    public HiCoexCellularScore(Context context, HiCoexReceiver receiver) {
        super(context);
        this.mHiCoexReceiver = receiver;
        loadScoreConfiguration();
    }

    public static int getSceneNo(HiCoexCellularState cellularState) {
        if (cellularState == null) {
            HiCoexUtils.logE(TAG, "getSceneNo cellularState is null");
            return 0;
        }
        int serviceState = cellularState.getState();
        if (serviceState == 3) {
            return 1;
        }
        if (serviceState == 1) {
            return 4;
        }
        if (serviceState != 0) {
            return 0;
        }
        int networkType = cellularState.getNetworkType();
        if (networkType == 2) {
            if (cellularState.getDataConnected()) {
                return 64;
            }
            return 32;
        } else if (networkType != 1) {
            return 4;
        } else {
            if (cellularState.getDataConnected()) {
                return 16;
            }
            return 8;
        }
    }

    @Override // com.android.server.wifi.hwcoex.HiCoexScore
    public void onReceiveEvent(Message msg) {
        if (msg == null) {
            HiCoexUtils.logE(TAG, "onReceiveEvent message is null");
            return;
        }
        int i = msg.what;
        if (i != 4) {
            if (i == 7) {
                this.mCurrentScene &= -513;
                return;
            } else if (i == 8) {
                handleServiceStateChanged();
                return;
            } else if (i == 18) {
                handleWifiDisplayConnected(msg);
                return;
            } else if (i != 19) {
                switch (i) {
                    case 21:
                        handleLowLatencyChanged(msg);
                        return;
                    case 22:
                        this.mCurrentScene &= -2049;
                        return;
                    case 23:
                        this.mCurrentScene |= 2048;
                        return;
                    default:
                        HiCoexUtils.logV(TAG, "HiCoexCellularScore ignore message:" + msg.what);
                        return;
                }
            }
        }
        handleNetworkChanged();
    }

    @Override // com.android.server.wifi.hwcoex.HiCoexScore
    public int getDefaultScore() {
        return 2;
    }

    private void loadScoreConfiguration() {
        this.mSceneNames = new String[]{"scene_airplane_on", "scene_sim_absent", "scene_no_services", "scene_nsa_data_unconnected", "scene_nsa_data_connected", "scene_sa_data_unconnected", "scene_sa_data_connected", "scene_vowifi_calling", "scene_wifi_display", "scene_double_link", "scene_lowlatency", "scene_phone_calling"};
        if (HiCoexUtils.isDebugEnable()) {
            this.mSceneScores = HiCoexDebugger.loadScoreConfiguration(0);
            if (this.mSceneScores != null && this.mSceneScores.length > 0) {
                return;
            }
        }
        this.mSceneScores = new int[]{2, 4, 4, 4, 16, 22, 24, SCENE_VOWIFI_CALLING_SCORE, 25, 34, 52, 52};
    }

    private void handleServiceStateChanged() {
        int sceneNo = 0;
        int sceneScore = 0;
        HiCoexReceiver hiCoexReceiver = this.mHiCoexReceiver;
        if (hiCoexReceiver == null) {
            HiCoexUtils.logE(TAG, "HiCoexReceiver is null");
            return;
        }
        List<HiCoexCellularState> hiCoexCellStates = hiCoexReceiver.getHiCoexCellStates();
        if (hiCoexCellStates == null) {
            HiCoexUtils.logE(TAG, "hiCoexCellStates is null");
            return;
        }
        for (HiCoexCellularState cellularState : hiCoexCellStates) {
            int tmpSceneNo = 0;
            int serviceState = cellularState.getState();
            if (serviceState == 3) {
                tmpSceneNo = 1;
            } else if (serviceState == 1) {
                tmpSceneNo = 4;
            } else if (serviceState == 0) {
                int networkType = cellularState.getNetworkType();
                if (networkType == 2) {
                    if (cellularState.getDataConnected()) {
                        tmpSceneNo = handleDataConnection(64);
                    } else {
                        tmpSceneNo = 32;
                    }
                } else if (networkType != 1) {
                    tmpSceneNo = 4;
                } else if (cellularState.getDataConnected()) {
                    tmpSceneNo = handleDataConnection(16);
                } else {
                    tmpSceneNo = 8;
                }
            }
            int tmpScore = getScore(tmpSceneNo);
            if (sceneNo == 0 || tmpScore > sceneScore) {
                sceneNo = tmpSceneNo;
                sceneScore = tmpScore;
            }
        }
        this.mCurrentScene = sceneNo;
    }

    private void handleNetworkChanged() {
        HiCoexReceiver hiCoexReceiver = this.mHiCoexReceiver;
        if (hiCoexReceiver == null) {
            HiCoexUtils.logE(TAG, "HiCoexReceiver is null");
        } else if (!hiCoexReceiver.isNrNetwork()) {
            HiCoexUtils.logE("handleNetworkChanged isNrNetwork:false");
        } else {
            int mainNetworkType = this.mHiCoexReceiver.getActiveNetworkType();
            int foreNetworkType = this.mHiCoexReceiver.getForegroundNetworkType();
            boolean isDoubleLink = false;
            boolean isWifiConnected = this.mHiCoexReceiver.isWifiConnected();
            boolean isCellConnected = foreNetworkType == 801 || mainNetworkType == 801;
            if (isWifiConnected && isCellConnected) {
                isDoubleLink = true;
            }
            if (isDoubleLink) {
                this.mCurrentScene |= 512;
            } else {
                this.mCurrentScene &= -513;
            }
        }
    }

    private void handleLowLatencyChanged(Message msg) {
        HiCoexReceiver hiCoexReceiver = this.mHiCoexReceiver;
        if (hiCoexReceiver == null) {
            HiCoexUtils.logE(TAG, "HiCoexReceiver is null");
        } else if (!hiCoexReceiver.isNrNetwork()) {
            HiCoexUtils.logE("handleLowLatencyChanged isNrNetwork:false");
        } else if (msg.arg1 > 0) {
            int mainNetworkType = this.mHiCoexReceiver.getActiveNetworkType();
            if (this.mHiCoexReceiver.getForegroundNetworkType() == 801 || mainNetworkType == 801) {
                this.mCurrentScene |= 1024;
            } else {
                this.mCurrentScene &= -1025;
            }
        } else {
            this.mCurrentScene &= -1025;
        }
    }

    private void handleWifiDisplayConnected(Message msg) {
        this.mIsDisplayEnabled = msg.arg1 > 0;
        HiCoexReceiver hiCoexReceiver = this.mHiCoexReceiver;
        if (hiCoexReceiver == null) {
            HiCoexUtils.logE(TAG, "HiCoexReceiver is null");
        } else if (!hiCoexReceiver.isNrNetwork()) {
            HiCoexUtils.logE("handleWifiDisplayConnected isNrNetwork:false");
        } else {
            int mainNetworkType = this.mHiCoexReceiver.getActiveNetworkType();
            if (mainNetworkType != 801) {
                HiCoexUtils.logE("handleWifiDisplayConnected mainNetworkType:" + mainNetworkType);
            } else if (msg.arg1 > 0) {
                this.mCurrentScene |= 256;
            } else {
                this.mCurrentScene &= -257;
            }
        }
    }

    private int handleDataConnection(int sceneNo) {
        HiCoexUtils.logD(TAG, "handleDataConnection:" + this.mIsDisplayEnabled);
        if (this.mIsDisplayEnabled) {
            return sceneNo | 256;
        }
        return sceneNo & -257;
    }
}
