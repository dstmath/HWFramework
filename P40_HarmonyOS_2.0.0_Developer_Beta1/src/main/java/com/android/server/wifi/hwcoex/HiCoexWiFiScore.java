package com.android.server.wifi.hwcoex;

import android.content.Context;
import android.os.Message;

public class HiCoexWiFiScore extends HiCoexScore {
    public static final int SCENE_DOUBLE_LINK = 2048;
    private static final int SCENE_DOUBLE_LINK_SCORE = 33;
    public static final int SCENE_FGROUND_SCAN = 128;
    private static final int SCENE_FGROUND_SCAN_SCORE = 31;
    public static final int SCENE_LOWLATENCY = 4096;
    private static final int SCENE_LOWLATENCY_SCORE = 51;
    public static final int SCENE_P2P_GC = 64;
    private static final int SCENE_P2P_GC_SCORE = 13;
    public static final int SCENE_P2P_GO = 32;
    private static final int SCENE_P2P_GO_SCORE = 13;
    public static final int SCENE_VOWIFI_CALLING = 512;
    private static final int SCENE_VOWIFI_CALLING_SCORE = 61;
    public static final int SCENE_WIFIAP_ENABLED = 8;
    private static final int SCENE_WIFIAP_ENABLED_SCORE = 11;
    public static final int SCENE_WIFI_BRIDGE = 16;
    private static final int SCENE_WIFI_BRIDGE_SCORE = 11;
    public static final int SCENE_WIFI_CONNECTED = 4;
    private static final int SCENE_WIFI_CONNECTED_SCORE = 15;
    public static final int SCENE_WIFI_CONNECTING = 256;
    private static final int SCENE_WIFI_CONNECTING_SCORE = 31;
    public static final int SCENE_WIFI_DISABLED = 1;
    private static final int SCENE_WIFI_DISABLED_SCORE = 1;
    public static final int SCENE_WIFI_DISPLAY = 1024;
    private static final int SCENE_WIFI_DISPLAY_SCORE = 25;
    public static final int SCENE_WIFI_ENABLED = 2;
    private static final int SCENE_WIFI_ENABLED_SCORE = 5;
    private static final int SCORE_DEFAULT = 1;
    private static final String TAG = "HiCoexWiFiScore";
    private HiCoexReceiver mHiCoexReceiver;

    public HiCoexWiFiScore(Context context, HiCoexReceiver receiver) {
        super(context);
        this.mHiCoexReceiver = receiver;
        loadScoreConfiguration();
    }

    @Override // com.android.server.wifi.hwcoex.HiCoexScore
    public void onReceiveEvent(Message msg) {
        if (msg == null) {
            HiCoexUtils.logE(TAG, "onReceiveEvent message is null");
            return;
        }
        switch (msg.what) {
            case 2:
                this.mCurrentScene = 2;
                return;
            case 3:
                handleWifiDisabled();
                return;
            case 4:
                this.mCurrentScene |= 4;
                handleNetworkChanged();
                return;
            case 5:
                this.mCurrentScene = 8;
                return;
            case 6:
                this.mCurrentScene &= -9;
                return;
            case 7:
                this.mCurrentScene &= -5;
                this.mCurrentScene &= -2049;
                return;
            case 8:
            case 9:
            case 14:
            case 15:
            case 20:
            default:
                HiCoexUtils.logV(TAG, "HiCoexWiFiScore ignore message:" + msg.what);
                return;
            case 10:
                handleForegroundScan(msg);
                return;
            case 11:
                this.mCurrentScene &= -129;
                return;
            case 12:
                handleWifiConnecting(msg);
                return;
            case 13:
                this.mCurrentScene &= -257;
                return;
            case 16:
                handleP2pConnected(msg);
                return;
            case 17:
                handleRptEnabled(msg);
                return;
            case 18:
                handleWifiDisplayConnected(msg);
                return;
            case 19:
                handleNetworkChanged();
                return;
            case 21:
                handleLowLatencyChanged(msg);
                return;
        }
    }

    private void handleWifiDisabled() {
        if ((this.mCurrentScene & 8) == 8) {
            this.mCurrentScene = 8;
        } else {
            this.mCurrentScene = 1;
        }
    }

    private void handleForegroundScan(Message msg) {
        if (msg.arg1 == 0) {
            this.mCurrentScene &= -129;
        } else {
            this.mCurrentScene |= 128;
        }
    }

    private void handleWifiConnecting(Message msg) {
        if (msg.arg1 == 0) {
            this.mCurrentScene &= -257;
        } else {
            this.mCurrentScene |= 256;
        }
    }

    private void handleP2pConnected(Message msg) {
        if (msg.arg1 != 0) {
            this.mCurrentScene |= 64;
        } else {
            this.mCurrentScene &= -65;
        }
    }

    private void handleRptEnabled(Message msg) {
        if (msg.arg1 != 0) {
            this.mCurrentScene |= 16;
        } else {
            this.mCurrentScene &= -17;
        }
    }

    private void handleWifiDisplayConnected(Message msg) {
        if (msg.arg1 != 0) {
            this.mCurrentScene |= 1024;
        } else {
            this.mCurrentScene &= -1025;
        }
    }

    private void handleLowLatencyChanged(Message msg) {
        if (msg.arg1 > 0) {
            int mainNetworkType = this.mHiCoexReceiver.getActiveNetworkType();
            int foreNetworkType = this.mHiCoexReceiver.getForegroundNetworkType();
            HiCoexUtils.logD("lowLatencyChanged mainNetType:" + mainNetworkType + ",foreNetType:" + foreNetworkType);
            if (foreNetworkType == 801 || mainNetworkType != 800) {
                this.mCurrentScene &= -4097;
            } else {
                this.mCurrentScene |= SCENE_LOWLATENCY;
            }
        } else {
            this.mCurrentScene &= -4097;
        }
    }

    private void handleNetworkChanged() {
        int mainNetworkType = this.mHiCoexReceiver.getActiveNetworkType();
        int foreNetworkType = this.mHiCoexReceiver.getForegroundNetworkType();
        boolean isDoubleLink = false;
        boolean isWifiConnected = this.mHiCoexReceiver.isWifiConnected();
        boolean isCellConnected = foreNetworkType == 801 || mainNetworkType == 801;
        if (isWifiConnected && isCellConnected) {
            isDoubleLink = true;
        }
        if (isDoubleLink) {
            this.mCurrentScene |= 2048;
        } else {
            this.mCurrentScene &= -2049;
        }
    }

    @Override // com.android.server.wifi.hwcoex.HiCoexScore
    public int getDefaultScore() {
        return 1;
    }

    private void loadScoreConfiguration() {
        this.mSceneNames = new String[]{"scene_wifi_disabled", "scene_wifi_enabled", "scene_wifi_connected", "scene_wifiap_enabled", "scene_wifi_bridge", "scene_p2p_go", "scene_p2p_gc", "scene_fground_scan", "scene_wifi_connecting", "scene_vowifi_calling", "scene_wifi_display", "scene_double_link", "scene_lowlatency"};
        if (HiCoexUtils.isDebugEnable()) {
            this.mSceneScores = HiCoexDebugger.loadScoreConfiguration(1);
            if (this.mSceneScores != null && this.mSceneScores.length > 0) {
                return;
            }
        }
        this.mSceneScores = new int[]{1, 5, 15, 11, 11, 13, 13, 31, 31, SCENE_VOWIFI_CALLING_SCORE, 25, 33, SCENE_LOWLATENCY_SCORE};
    }
}
