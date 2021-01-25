package com.android.server.wifi.rxlisten;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiNative;

public class RxListenArbitration {
    private static final int CMD_GET_RXLISTENINTERVAL = 124;
    private static final int CMD_GET_RXLISTENSTATE = 123;
    private static final int CMD_SET_RX_LISTEN_POWER_SAVING_SWITCH = 125;
    private static final int GAME_APP_SHIFT = 0;
    private static final String IFACE = "wlan0";
    private static final int INITIAL_CONDITION = 0;
    private static final int INVALID_CMD = -1;
    private static final char RXLISTEN_DISABLE = 'N';
    private static final char RXLISTEN_ENABLE = 'Y';
    private static final char RXLISTEN_INTERVAL = 'I';
    private static final char RXLISTEN_STATE = 'S';
    private static final int SHIFT_BIT_UTIL = 1;
    private static final String TAG = "RxListenArbitration";
    private static RxListenArbitration sRxListenArbitration = null;
    private int mArbitraCond;
    private Context mContext;
    private Handler mHandler;
    private HwWifiCHRService mHwWifiChrService = null;
    private boolean mIsRxListenOn = true;
    private RxListenMonitor mRxListenMonitor = null;
    private WifiNative mWifiNative;

    private RxListenArbitration(Context context, WifiNative wifiNative) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mArbitraCond = 0;
        this.mHwWifiChrService = HwWifiServiceFactory.getHwWifiCHRService();
        initRxListenHandler();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initRxListenMonitor() {
        this.mRxListenMonitor = new RxListenMonitor(this.mContext, this.mHandler);
    }

    public static synchronized RxListenArbitration createRxListenArbitration(Context context, WifiNative wifiNative) {
        RxListenArbitration rxListenArbitration;
        synchronized (RxListenArbitration.class) {
            if (sRxListenArbitration == null) {
                sRxListenArbitration = new RxListenArbitration(context, wifiNative);
            }
            rxListenArbitration = sRxListenArbitration;
        }
        return rxListenArbitration;
    }

    public static synchronized RxListenArbitration getInstance() {
        RxListenArbitration rxListenArbitration;
        synchronized (RxListenArbitration.class) {
            rxListenArbitration = sRxListenArbitration;
        }
        return rxListenArbitration;
    }

    public int sendRxListenCmdtoDriver(int cmd) {
        if (cmd <= 0) {
            HwHiLog.e(TAG, false, "Invalid command", new Object[0]);
            return -1;
        } else if (cmd == 123) {
            return this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 123, new byte[]{83});
        } else {
            if (cmd != 124) {
                return -1;
            }
            return this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 124, new byte[]{73});
        }
    }

    private synchronized void checkRxListenSwitch(int arbitraCond, boolean isRxListenOn) {
        HwHiLog.d(TAG, false, "checkFastSleepSwitch, mArbitraCond: %{public}d", new Object[]{Integer.valueOf(this.mArbitraCond)});
        if (arbitraCond != 0 || isRxListenOn) {
            if (arbitraCond != 0 && isRxListenOn) {
                HwHiLog.d(TAG, false, "rxlisten off", new Object[0]);
                this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 125, new byte[]{78});
                this.mIsRxListenOn = false;
            }
            return;
        }
        HwHiLog.d(TAG, false, "rxlisten on", new Object[0]);
        this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 125, new byte[]{89});
        this.mIsRxListenOn = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGameActivityChanged(Message msg) {
        if (msg.arg1 > 0) {
            this.mArbitraCond |= 1;
            HwHiLog.d(TAG, false, "LowLatency SCENE, mArbitraCond: %{public}d", new Object[]{Integer.valueOf(this.mArbitraCond)});
        } else {
            this.mArbitraCond &= -2;
            HwHiLog.d(TAG, false, "NOT LowLatency SCENE, mArbitraCond: %{public}d", new Object[]{Integer.valueOf(this.mArbitraCond)});
        }
        checkRxListenSwitch(this.mArbitraCond, this.mIsRxListenOn);
    }

    private void initRxListenHandler() {
        HandlerThread handlerThread = new HandlerThread("RxListenArbitration_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.rxlisten.RxListenArbitration.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    RxListenArbitration.this.initRxListenMonitor();
                } else if (i == 2) {
                    RxListenArbitration.this.handleGameActivityChanged(msg);
                }
            }
        };
        this.mHandler.sendEmptyMessage(1);
    }
}
