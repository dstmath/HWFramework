package com.android.server.wifi.HwWiTas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.hidata.arbitration.HwArbitrationCallbackImpl;
import com.android.server.hidata.arbitration.IGameReportCallback;
import com.android.server.wifi.WifiNative;

public class HwWiTasMonitor implements IGameReportCallback {
    /* access modifiers changed from: private */
    public static HwWiTasMonitor mWiTasMonitor = null;
    PhoneStateListener listener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case 0:
                    Log.d(HwWiTasUtils.TAG, "CALL_STATE_IDLE");
                    HwWiTasMonitor.this.mHandler.sendEmptyMessage(6);
                    return;
                case 1:
                    Log.d(HwWiTasUtils.TAG, "CALL_STATE_RINGING");
                    return;
                case 2:
                    Log.d(HwWiTasUtils.TAG, "CALL_STATE_OFFHOOK");
                    HwWiTasMonitor.this.mHandler.sendEmptyMessage(5);
                    return;
                default:
                    return;
            }
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new WiTasBroadcastReceiver();
    private Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private boolean mIsRegister = false;
    private TelephonyManager mTelephonyManager;
    private HwWiTasArbitra mWiTasArbitra;
    private HwWiTasTest mWiTasTest;

    private class WiTasBroadcastReceiver extends BroadcastReceiver {
        private WiTasBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                int wifiState = intent.getIntExtra("wifi_state", 4);
                if (wifiState == 1) {
                    Log.d(HwWiTasUtils.TAG, "WIFI_STATE_DISABLED");
                    HwWiTasMonitor.this.mHandler.sendEmptyMessage(4);
                } else if (wifiState == 3) {
                    Log.d(HwWiTasUtils.TAG, "WIFI_STATE_ENABLED");
                    HwWiTasMonitor.this.mHandler.sendEmptyMessage(3);
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo == null) {
                    return;
                }
                if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    Log.d(HwWiTasUtils.TAG, "NetworkInfo.State.CONNECTED");
                    HwWiTasMonitor.this.mHandler.sendEmptyMessage(1);
                } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    Log.d(HwWiTasUtils.TAG, "NetworkInfo.State.DISCONNECTED");
                    HwWiTasMonitor.this.mHandler.sendEmptyMessage(2);
                }
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                Log.d(HwWiTasUtils.TAG, "BOOT_COMPLETED");
                HwArbitrationCallbackImpl.getInstance(context).registGameReport(HwWiTasMonitor.mWiTasMonitor);
            }
        }
    }

    public void onReportGameDelay(int delay) {
        Log.d(HwWiTasUtils.TAG, "reportGameDelay, delay: " + delay);
        if (this.mWiTasArbitra.isTestMode()) {
            delay = this.mWiTasTest.getGameDelay(delay);
        }
        this.mWiTasArbitra.setGameDelay(delay);
        if (this.mWiTasArbitra.isGameLag(delay)) {
            this.mHandler.sendEmptyMessage(9);
        }
    }

    public void onReportGameState(boolean enable, boolean inWhiteList, int id) {
        Log.d(HwWiTasUtils.TAG, "reportGameState, enable: " + enable + ", inWhiteList: " + inWhiteList + ", id: " + id);
        if (this.mHandler.hasMessages(7)) {
            this.mHandler.removeMessages(7);
        }
        if (enable) {
            this.mHandler.sendEmptyMessageDelayed(7, 5000);
        } else {
            this.mHandler.sendEmptyMessage(8);
            this.mWiTasArbitra.setGameDelay(0);
        }
        this.mWiTasArbitra.setGameId(id);
        this.mWiTasArbitra.updateGameWhiteListInfo(inWhiteList);
    }

    public void reportAntRssi(int index, int antRssi) {
        int msgId;
        int measureState = this.mWiTasArbitra.getMeasureState();
        Log.d(HwWiTasUtils.TAG, "reportAntRssi, measureState" + measureState + ", antRssi: " + antRssi);
        if (this.mWiTasArbitra.isTestMode()) {
            switch (measureState) {
                case 1:
                    antRssi = this.mWiTasTest.getSrcRssi(antRssi);
                    break;
                case 2:
                    antRssi = this.mWiTasTest.getDstRssi(antRssi);
                    break;
                default:
                    return;
            }
        }
        if (antRssi == -130) {
            this.mHandler.sendEmptyMessage(21);
            return;
        }
        switch (measureState) {
            case 1:
                msgId = 12;
                break;
            case 2:
                msgId = 15;
                break;
            default:
                return;
        }
        if (antRssi == -128) {
            this.mHandler.sendEmptyMessage(20);
        } else {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(msgId, antRssi, 0));
        }
    }

    public static synchronized HwWiTasMonitor getInstance() {
        HwWiTasMonitor hwWiTasMonitor;
        synchronized (HwWiTasMonitor.class) {
            hwWiTasMonitor = mWiTasMonitor;
        }
        return hwWiTasMonitor;
    }

    protected static synchronized HwWiTasMonitor getInstance(Context context, Handler handler, WifiManager wifiManager, WifiNative wifiNative) {
        HwWiTasMonitor hwWiTasMonitor;
        synchronized (HwWiTasMonitor.class) {
            if (mWiTasMonitor == null) {
                mWiTasMonitor = new HwWiTasMonitor(context, handler, wifiManager, wifiNative);
            }
            hwWiTasMonitor = mWiTasMonitor;
        }
        return hwWiTasMonitor;
    }

    /* access modifiers changed from: protected */
    public void startMonitor() {
        registerBroadcastReceiver();
    }

    /* access modifiers changed from: protected */
    public void stopMonitor() {
        unRegisterBroadcastReceiver();
    }

    private HwWiTasMonitor(Context context, Handler handler, WifiManager wifiManager, WifiNative wifiNative) {
        Log.d(HwWiTasUtils.TAG, "init HwWiTasMonitor");
        this.mContext = context;
        this.mHandler = handler;
        this.mWiTasArbitra = HwWiTasArbitra.getInstance(wifiManager, wifiNative);
        this.mWiTasTest = HwWiTasTest.getInstance();
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    private void registerBroadcastReceiver() {
        if (!this.mIsRegister) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
            this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
            if (this.mTelephonyManager != null) {
                this.mTelephonyManager.listen(this.listener, 32);
            }
            this.mIsRegister = true;
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (this.mIsRegister) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mIsRegister = false;
        }
    }
}
