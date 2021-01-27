package com.android.server.wifi.wifibthybrid;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;

public class WifiBtHybridArbitration {
    private static final int A2DP_SHIFT = 1;
    private static final int BT_STATUS_SHIFT = 4;
    private static final int CMD_HYBRID_MODE_SET = 168;
    private static final int GAME_SCENE_SHIFT = 0;
    private static final char HYBRID_MODE = '2';
    private static final int HYBRID_MODE_THROSHOLD = 7;
    private static final int HYBRID_RSSI_LOWER_THRESHOLD = -60;
    private static final int HYBRID_RSSI_UPPER_THRESHOLD = -40;
    private static final String IFACE = "wlan0";
    private static final Object LOCK_OBJECT = new Object();
    private static final char NORMAL_MODE = '8';
    private static final int SCAN_RSSI_DELAY = 3000;
    private static final int SHIFT_BIT_UTIL = 1;
    private static final int SIX_SLOT_SHIFT = 2;
    private static final String TAG = "WifiBtHybridArbitration";
    private static final int WIFI_CONNECT_SHIFT = 3;
    private static volatile WifiBtHybridArbitration sInstance;
    private Context mContext;
    private Handler mHandler;
    private boolean mIsApInHybridList = false;
    private boolean mIsBtConnected = false;
    private boolean mIsBtIn6SLOT = false;
    private boolean mIsBtInA2Dp = false;
    private boolean mIsGameActivity = false;
    private boolean mIsHybridOn = false;
    private boolean mIsRssiMonitorOn = false;
    private boolean mIsWifiConnected = false;
    private WifiBtHybridMonitor mWifiBtHybridMonitor = null;
    private WifiNative mWifiNative;

    private WifiBtHybridArbitration(Context context, WifiNative wifiNative) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        initWifiBtHybridHandler();
    }

    public static WifiBtHybridArbitration getInstance(Context context, WifiNative wifiNative) {
        if (sInstance == null) {
            synchronized (LOCK_OBJECT) {
                if (sInstance == null) {
                    sInstance = new WifiBtHybridArbitration(context, wifiNative);
                }
            }
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWifiBtHybridMonitor() {
        this.mWifiBtHybridMonitor = new WifiBtHybridMonitor(this.mContext, this.mHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStartRssiCheck() {
        int rssi = getWifiRssi();
        if (rssi >= HYBRID_RSSI_UPPER_THRESHOLD && !this.mIsHybridOn) {
            HwHiLog.d(TAG, false, "rssi = [%{public}d], hybird mode on", new Object[]{Integer.valueOf(rssi)});
            this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, (int) CMD_HYBRID_MODE_SET, new byte[]{50});
            this.mIsHybridOn = true;
        }
        if (rssi < HYBRID_RSSI_LOWER_THRESHOLD && this.mIsHybridOn) {
            HwHiLog.d(TAG, false, "rssi = [%{public}d], hybird mode off because of low rssi.", new Object[]{Integer.valueOf(rssi)});
            this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, (int) CMD_HYBRID_MODE_SET, new byte[]{56});
            this.mIsHybridOn = false;
        }
        this.mHandler.sendEmptyMessageDelayed(11, 3000);
    }

    private int getWifiRssi() {
        int rssi;
        WifiNative.SignalPollResult signalInfo = WifiInjector.getInstance().getWifiNative().signalPoll(IFACE);
        if (signalInfo != null) {
            rssi = signalInfo.currentRssi;
        } else {
            HwHiLog.e(TAG, false, "getRssiFail! signalInfo is null", new Object[0]);
            rssi = -1000;
        }
        HwHiLog.d(TAG, false, "RSSI[%{public}d]", new Object[]{Integer.valueOf(rssi)});
        return rssi;
    }

    private synchronized void checkHybridlist() {
        this.mIsApInHybridList = false;
    }

    private synchronized void checkWifiBtHybridSwitch() {
        boolean needToTurnOnHybrid = false;
        if (this.mIsWifiConnected) {
            if (this.mIsGameActivity) {
                needToTurnOnHybrid = true;
            } else if (this.mIsApInHybridList && this.mIsBtConnected) {
                needToTurnOnHybrid = true;
            } else if (this.mIsBtIn6SLOT || this.mIsBtInA2Dp) {
                needToTurnOnHybrid = true;
            }
        }
        HwHiLog.d(TAG, false, "WifiConnected: " + Boolean.toString(this.mIsWifiConnected) + " ,GameActivity: " + Boolean.toString(this.mIsGameActivity) + " ,ApInHybridList: " + Boolean.toString(this.mIsApInHybridList) + " ,BtConnected: " + Boolean.toString(this.mIsBtConnected) + " ,6Slot:" + Boolean.toString(this.mIsBtIn6SLOT) + " ,A2dp:" + Boolean.toString(this.mIsBtInA2Dp), new Object[0]);
        StringBuilder sb = new StringBuilder();
        sb.append("checkWifiBtHybridSwitch, needToTurnOnHybrid?");
        sb.append(Boolean.toString(needToTurnOnHybrid));
        sb.append(". isHybridOn?");
        sb.append(Boolean.toString(this.mIsHybridOn));
        HwHiLog.d(TAG, false, sb.toString(), new Object[0]);
        if (!needToTurnOnHybrid || this.mIsRssiMonitorOn) {
            if (!needToTurnOnHybrid && this.mIsRssiMonitorOn) {
                HwHiLog.d(TAG, false, "hybird mode off because of scence changed.", new Object[0]);
                if (this.mHandler.hasMessages(11)) {
                    this.mHandler.removeMessages(11);
                }
                this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, (int) CMD_HYBRID_MODE_SET, new byte[]{56});
                this.mIsHybridOn = false;
                this.mIsRssiMonitorOn = false;
            }
            return;
        }
        this.mIsRssiMonitorOn = true;
        this.mHandler.sendEmptyMessage(11);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGameActivityChanged(Message msg) {
        if (msg.arg1 > 0) {
            HwHiLog.d(TAG, false, "gameActivityChanged, LowLatency SCENE", new Object[0]);
            this.mIsGameActivity = true;
        } else {
            HwHiLog.d(TAG, false, "gameActivityChanged, NOT LowLatency SCENE", new Object[0]);
            this.mIsGameActivity = false;
        }
        checkWifiBtHybridSwitch();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleA2dpStart() {
        HwHiLog.d(TAG, false, "A2DP start", new Object[0]);
        this.mIsBtInA2Dp = true;
        checkWifiBtHybridSwitch();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleA2dpStop() {
        HwHiLog.d(TAG, false, "A2DP stop", new Object[0]);
        this.mIsBtInA2Dp = false;
        checkWifiBtHybridSwitch();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBluetoothDisconnected() {
        HwHiLog.d(TAG, false, "bluetooth disconnect", new Object[0]);
        this.mIsBtConnected = false;
        checkWifiBtHybridSwitch();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBluetoothConnected() {
        HwHiLog.d(TAG, false, "bluetooth connect", new Object[0]);
        this.mIsBtConnected = true;
        checkWifiBtHybridSwitch();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiConnected() {
        HwHiLog.d(TAG, false, "wifi connect", new Object[0]);
        this.mIsWifiConnected = true;
        checkHybridlist();
        checkWifiBtHybridSwitch();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiDisconnect() {
        HwHiLog.d(TAG, false, "wifi disconnect", new Object[0]);
        this.mIsWifiConnected = false;
        checkWifiBtHybridSwitch();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handle6SlotStop() {
        HwHiLog.d(TAG, false, "6slot stop", new Object[0]);
        this.mIsBtIn6SLOT = false;
        checkWifiBtHybridSwitch();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handle6SlotStart() {
        HwHiLog.d(TAG, false, "6slot start", new Object[0]);
        this.mIsBtIn6SLOT = true;
        checkWifiBtHybridSwitch();
    }

    private void initWifiBtHybridHandler() {
        HandlerThread handlerThread = new HandlerThread("WifiBtHybridArbitration_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.wifibthybrid.WifiBtHybridArbitration.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        WifiBtHybridArbitration.this.initWifiBtHybridMonitor();
                        return;
                    case 2:
                        WifiBtHybridArbitration.this.handleGameActivityChanged(msg);
                        return;
                    case 3:
                        WifiBtHybridArbitration.this.handleA2dpStart();
                        return;
                    case 4:
                        WifiBtHybridArbitration.this.handleA2dpStop();
                        return;
                    case 5:
                        WifiBtHybridArbitration.this.handle6SlotStart();
                        return;
                    case 6:
                        WifiBtHybridArbitration.this.handle6SlotStop();
                        return;
                    case 7:
                        WifiBtHybridArbitration.this.handleBluetoothDisconnected();
                        return;
                    case 8:
                        WifiBtHybridArbitration.this.handleBluetoothConnected();
                        return;
                    case 9:
                        WifiBtHybridArbitration.this.handleWifiConnected();
                        return;
                    case 10:
                        WifiBtHybridArbitration.this.handleWifiDisconnect();
                        return;
                    case 11:
                        WifiBtHybridArbitration.this.handleStartRssiCheck();
                        return;
                    default:
                        HwHiLog.e(WifiBtHybridArbitration.TAG, false, "handle error message!", new Object[0]);
                        return;
                }
            }
        };
        this.mHandler.sendEmptyMessage(1);
    }
}
