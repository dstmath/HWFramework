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
import android.util.wifi.HwHiLog;
import com.android.server.hidata.arbitration.HwArbitrationCallbackImpl;
import com.android.server.hidata.arbitration.IGameReportCallback;
import com.android.server.wifi.WifiNative;

public class HwWiTasMonitor implements IGameReportCallback {
    private static HwWiTasMonitor mWiTasMonitor = null;
    PhoneStateListener listener = new PhoneStateListener() {
        /* class com.android.server.wifi.HwWiTas.HwWiTasMonitor.AnonymousClass1 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (state == 0) {
                HwHiLog.d(HwWiTasUtils.TAG, false, "CALL_STATE_IDLE", new Object[0]);
                HwWiTasMonitor.this.mHandler.sendEmptyMessage(6);
            } else if (state == 1) {
                HwHiLog.d(HwWiTasUtils.TAG, false, "CALL_STATE_RINGING", new Object[0]);
            } else if (state == 2) {
                HwHiLog.d(HwWiTasUtils.TAG, false, "CALL_STATE_OFFHOOK", new Object[0]);
                HwWiTasMonitor.this.mHandler.sendEmptyMessage(5);
            }
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new WiTasBroadcastReceiver();
    private Context mContext;
    private Handler mHandler;
    private boolean mIsRegister = false;
    private TelephonyManager mTelephonyManager;
    private HwWiTasArbitra mWiTasArbitra;
    private HwWiTasTest mWiTasTest;

    public void onReportGameDelay(int delay) {
        HwHiLog.d(HwWiTasUtils.TAG, false, "reportGameDelay, delay: %{public}d", new Object[]{Integer.valueOf(delay)});
        if (this.mWiTasArbitra.isTestMode()) {
            delay = this.mWiTasTest.getGameDelay(delay);
        }
        this.mWiTasArbitra.setGameDelay(delay);
        if (this.mWiTasArbitra.isGameLag(delay)) {
            this.mHandler.sendEmptyMessage(9);
        }
    }

    public void onReportGameState(boolean enable, boolean inWhiteList, int id) {
        HwHiLog.d(HwWiTasUtils.TAG, false, "reportGameState, enable: %{public}s, inWhiteList: %{public}s, id: %{public}d", new Object[]{String.valueOf(enable), String.valueOf(inWhiteList), Integer.valueOf(id)});
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
        HwHiLog.d(HwWiTasUtils.TAG, false, "reportAntRssi, measureState %{public}d, antRssi: %{public}d", new Object[]{Integer.valueOf(measureState), Integer.valueOf(antRssi)});
        if (this.mWiTasArbitra.isTestMode()) {
            if (measureState == 1) {
                antRssi = this.mWiTasTest.getSrcRssi(antRssi);
            } else if (measureState == 2) {
                antRssi = this.mWiTasTest.getDstRssi(antRssi);
            } else {
                return;
            }
        }
        if (HwWiTasUtils.mCoreAntIndex == 0 && antRssi == -130) {
            this.mHandler.sendEmptyMessage(21);
            return;
        }
        if (measureState == 1) {
            msgId = 12;
        } else if (measureState == 2) {
            msgId = 15;
        } else {
            return;
        }
        if (HwWiTasUtils.mCoreAntIndex == 0 && antRssi == -128) {
            this.mHandler.sendEmptyMessage(20);
            return;
        }
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(msgId, antRssi, 0));
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
        HwHiLog.d(HwWiTasUtils.TAG, false, "init HwWiTasMonitor", new Object[0]);
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
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
            TelephonyManager telephonyManager = this.mTelephonyManager;
            if (telephonyManager != null) {
                telephonyManager.listen(this.listener, 32);
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

    private class WiTasBroadcastReceiver extends BroadcastReceiver {
        private WiTasBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    int wifiState = intent.getIntExtra("wifi_state", 4);
                    if (wifiState == 1) {
                        HwHiLog.d(HwWiTasUtils.TAG, false, "WIFI_STATE_DISABLED", new Object[0]);
                        HwWiTasMonitor.this.mHandler.sendEmptyMessage(4);
                    } else if (wifiState == 3) {
                        HwHiLog.d(HwWiTasUtils.TAG, false, "WIFI_STATE_ENABLED", new Object[0]);
                        HwWiTasMonitor.this.mHandler.sendEmptyMessage(3);
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (netInfo == null) {
                        return;
                    }
                    if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                        HwHiLog.d(HwWiTasUtils.TAG, false, "NetworkInfo.State.CONNECTED", new Object[0]);
                        HwWiTasMonitor.this.mHandler.sendEmptyMessage(1);
                    } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        HwHiLog.d(HwWiTasUtils.TAG, false, "NetworkInfo.State.DISCONNECTED", new Object[0]);
                        HwWiTasMonitor.this.mHandler.sendEmptyMessage(2);
                    }
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    HwHiLog.d(HwWiTasUtils.TAG, false, "BOOT_COMPLETED", new Object[0]);
                    HwArbitrationCallbackImpl.getInstance(context).registGameReport(HwWiTasMonitor.mWiTasMonitor);
                    if (HwWiTasUtils.getWitasMode() == 1 || HwWiTasUtils.getWitasMode() == 3) {
                        HwWiTasMonitor.this.mWiTasArbitra.switchAntenna(HwWiTasUtils.mDefaultAntIndex);
                    }
                } else if ("android.intent.action.CONFIGURATION_CHANGED".equals(action)) {
                    HwHiLog.d(HwWiTasUtils.TAG, false, "ACTION_CONFIGURATION_CHANGED", new Object[0]);
                    HwWiTasMonitor.this.mHandler.sendEmptyMessage(27);
                } else {
                    HwHiLog.d(HwWiTasUtils.TAG, false, "receive unkown broadcast", new Object[0]);
                }
            }
        }
    }
}
