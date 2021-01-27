package com.android.server.wifi.tcpack;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.WifiNative;

public class TcpAckArbitration {
    private static final int CHARIOT_APP_SHIFT = 0;
    private static final int CMD_SET_TCP_ACK_SWITCH = 162;
    private static final String IFACE = "wlan0";
    private static final int INITIAL_CONDITION = 3;
    private static final int SCREEN_STATE_SHIFT = 1;
    private static final int SHIFT_BIT_UTIL = 1;
    private static final String TAG = "TcpArbitration";
    private static final char TCPACK_DISABLE = 'N';
    private static final char TCPACK_ENABLE = 'Y';
    private static TcpAckArbitration sTcpAckArbitration = null;
    private int mArbitraCond;
    private Context mContext;
    private Handler mHandler;
    private boolean mIsTcpAckOn = false;
    private TcpAckMonitor mTcpAckMonitor = null;
    private WifiNative mWifiNative;

    private TcpAckArbitration(Context context, WifiNative wifiNative) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mArbitraCond = 3;
        initTcpAckHandler();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initTcpAckMonitor() {
        this.mTcpAckMonitor = new TcpAckMonitor(this.mContext, this.mHandler);
    }

    public static synchronized TcpAckArbitration createTcpAckArbitration(Context context, WifiNative wifiNative) {
        TcpAckArbitration tcpAckArbitration;
        synchronized (TcpAckArbitration.class) {
            if (sTcpAckArbitration == null) {
                sTcpAckArbitration = new TcpAckArbitration(context, wifiNative);
            }
            tcpAckArbitration = sTcpAckArbitration;
        }
        return tcpAckArbitration;
    }

    public static synchronized TcpAckArbitration getInstance() {
        TcpAckArbitration tcpAckArbitration;
        synchronized (TcpAckArbitration.class) {
            tcpAckArbitration = sTcpAckArbitration;
        }
        return tcpAckArbitration;
    }

    private synchronized void checkTcpAckSwitch(int arbitraCond, boolean isTcpAckOn) {
        HwHiLog.d(TAG, false, "checkTcpAckSwitch, mArbitraCond: %{public}d", new Object[]{Integer.valueOf(this.mArbitraCond)});
        if (arbitraCond != 0 || this.mIsTcpAckOn) {
            if (arbitraCond != 0 && this.mIsTcpAckOn) {
                HwHiLog.d(TAG, false, "tcpack off", new Object[0]);
                this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, (int) CMD_SET_TCP_ACK_SWITCH, new byte[]{78});
                this.mIsTcpAckOn = false;
            }
            return;
        }
        HwHiLog.d(TAG, false, "tcpack on", new Object[0]);
        this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, (int) CMD_SET_TCP_ACK_SWITCH, new byte[]{89});
        this.mIsTcpAckOn = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleChariotActivityChanged(Message msg) {
        if (msg.arg1 > 0) {
            this.mArbitraCond &= -2;
        } else {
            this.mArbitraCond |= 1;
        }
        checkTcpAckSwitch(this.mArbitraCond, this.mIsTcpAckOn);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOff() {
        this.mArbitraCond &= -3;
        checkTcpAckSwitch(this.mArbitraCond, this.mIsTcpAckOn);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOn() {
        this.mArbitraCond |= 2;
        checkTcpAckSwitch(this.mArbitraCond, this.mIsTcpAckOn);
    }

    private void initTcpAckHandler() {
        HandlerThread handlerThread = new HandlerThread("TcpAckArbitration_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.tcpack.TcpAckArbitration.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    TcpAckArbitration.this.initTcpAckMonitor();
                } else if (i == 2) {
                    TcpAckArbitration.this.handleChariotActivityChanged(msg);
                } else if (i == 3) {
                    TcpAckArbitration.this.handleScreenOff();
                } else if (i == 4) {
                    TcpAckArbitration.this.handleScreenOn();
                }
            }
        };
        this.mHandler.sendEmptyMessage(1);
    }
}
