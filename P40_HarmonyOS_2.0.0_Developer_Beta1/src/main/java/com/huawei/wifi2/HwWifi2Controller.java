package com.huawei.wifi2;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.huawei.wifi2.HwWifi2ClientModeManager;
import java.util.concurrent.atomic.AtomicInteger;

public class HwWifi2Controller extends StateMachine {
    static final int BASE = 155648;
    static final int CMD_DEFERRED_ENABLE = 155651;
    static final int CMD_STA_START_FAILURE = 155652;
    static final int CMD_STA_STOPPED = 155653;
    static final int CMD_WIFI_TOGGLED_OFF = 155650;
    static final int CMD_WIFI_TOGGLED_ON = 155649;
    private static final long DEFAULT_REENABLE_DELAY_MS = 500;
    private static final long DEFER_MARGIN_MS = 5;
    private static final String TAG = "HwWifi2Controller";
    private final HwWifi2ActiveModeWarden mActiveModeWarden;
    private HwWifi2ClientModeManager.Listener mClientModeCallback = new ClientModeCallback();
    private final HwWifi2ClientModeImpl mClientModeImpl;
    private final Looper mClientModeImplLooper;
    private Context mContext;
    private DefaultState mDefaultState = new DefaultState();
    private long mReEnableDelayMillis;
    private StaDisabledState mStaDisabledState = new StaDisabledState();
    private StaEnabledState mStaEnabledState = new StaEnabledState();
    private final AtomicInteger mWifi2State = new AtomicInteger(1);

    HwWifi2Controller(Context context, HwWifi2ClientModeImpl clientModeImpl, Looper clientModeImplLooper, Looper wifiServiceLooper, HwWifi2ActiveModeWarden hwWifi2ActiveModeWarden) {
        super(TAG, wifiServiceLooper);
        this.mContext = context;
        this.mClientModeImpl = clientModeImpl;
        this.mClientModeImplLooper = clientModeImplLooper;
        this.mActiveModeWarden = hwWifi2ActiveModeWarden;
        addState(this.mDefaultState);
        addState(this.mStaDisabledState, this.mDefaultState);
        addState(this.mStaEnabledState, this.mDefaultState);
        this.mActiveModeWarden.registerClientModeCallback(this.mClientModeCallback);
        readWifiReEnableDelay();
    }

    public void start() {
        HwHiLog.i(TAG, false, "start", new Object[0]);
        setInitialState(this.mStaDisabledState);
        HwWifi2Controller.super.start();
    }

    public int getWifi2State() {
        return this.mWifi2State.get();
    }

    private class ClientModeCallback implements HwWifi2ClientModeManager.Listener {
        private ClientModeCallback() {
        }

        @Override // com.huawei.wifi2.HwWifi2ClientModeManager.Listener
        public void onStateChanged(int state) {
            HwWifi2Controller.this.mWifi2State.set(state);
            if (state == 4) {
                HwHiLog.w(HwWifi2Controller.TAG, false, "ClientMode unexpected failure: state unknown", new Object[0]);
                HwWifi2Controller.this.sendMessage(HwWifi2Controller.CMD_STA_START_FAILURE);
            } else if (state == 1) {
                HwHiLog.w(HwWifi2Controller.TAG, false, "ClientMode stopped", new Object[0]);
                HwWifi2Controller.this.sendMessage(HwWifi2Controller.CMD_STA_STOPPED);
            } else if (state == 3) {
                HwHiLog.i(HwWifi2Controller.TAG, false, "client mode active", new Object[0]);
            } else {
                HwHiLog.w(HwWifi2Controller.TAG, false, "unexpected state update: %{public}d", new Object[]{Integer.valueOf(state)});
            }
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2Controller.TAG, false, "%{public}s enter.", new Object[]{getName()});
        }

        public boolean processMessage(Message msg) {
            if (msg == null) {
                return true;
            }
            HwHiLog.i(HwWifi2Controller.TAG, false, "%{public}s: %{public}s", new Object[]{getName(), HwWifi2Controller.this.getMessageString(msg.what)});
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class StaDisabledState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mIsDeferredEnable = false;

        StaDisabledState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2Controller.TAG, false, "%{public}s enter.", new Object[]{getName()});
            HwWifi2Controller.this.mActiveModeWarden.disableWifi();
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mIsDeferredEnable = false;
        }

        private boolean doDeferEnable(Message msg) {
            long delayTime = SystemClock.elapsedRealtime() - this.mDisabledTimestamp;
            if (delayTime >= HwWifi2Controller.this.mReEnableDelayMillis || this.mDeferredEnableSerialNumber == 1) {
                return false;
            }
            HwHiLog.i(HwWifi2Controller.TAG, false, "WifiController msg %{public}s deferred for %{public}s ms", new Object[]{msg, String.valueOf(HwWifi2Controller.this.mReEnableDelayMillis - delayTime)});
            Message deferredMsg = HwWifi2Controller.this.obtainMessage(HwWifi2Controller.CMD_DEFERRED_ENABLE);
            deferredMsg.obj = Message.obtain(msg);
            this.mDeferredEnableSerialNumber++;
            deferredMsg.arg1 = this.mDeferredEnableSerialNumber;
            HwWifi2Controller hwWifi2Controller = HwWifi2Controller.this;
            hwWifi2Controller.sendMessageDelayed(deferredMsg, (hwWifi2Controller.mReEnableDelayMillis - delayTime) + HwWifi2Controller.DEFER_MARGIN_MS);
            return true;
        }

        public boolean processMessage(Message msg) {
            HwHiLog.i(HwWifi2Controller.TAG, false, "%{public}s: %{public}s", new Object[]{getName(), HwWifi2Controller.this.getMessageString(msg.what)});
            int i = msg.what;
            if (i != HwWifi2Controller.CMD_WIFI_TOGGLED_ON) {
                if (i != HwWifi2Controller.CMD_DEFERRED_ENABLE) {
                    return false;
                }
                if (msg.arg1 != this.mDeferredEnableSerialNumber) {
                    HwHiLog.i(HwWifi2Controller.TAG, false, "DEFERRED_TOGGLE ignored due to serial mismatch", new Object[0]);
                    return true;
                } else if (!(msg.obj instanceof Message)) {
                    return true;
                } else {
                    HwWifi2Controller.this.sendMessage((Message) msg.obj);
                    return true;
                }
            } else if (doDeferEnable(msg)) {
                if (this.mIsDeferredEnable) {
                    this.mDeferredEnableSerialNumber++;
                }
                this.mIsDeferredEnable = !this.mIsDeferredEnable;
                return true;
            } else {
                HwWifi2Controller hwWifi2Controller = HwWifi2Controller.this;
                hwWifi2Controller.transitionTo(hwWifi2Controller.mStaEnabledState);
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class StaEnabledState extends State {
        StaEnabledState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2Controller.TAG, false, "%{public}s enter.", new Object[]{getName()});
            HwWifi2Controller.this.mActiveModeWarden.enterClientMode();
        }

        public boolean processMessage(Message msg) {
            HwHiLog.i(HwWifi2Controller.TAG, false, "%{public}s: %{public}s", new Object[]{getName(), HwWifi2Controller.this.getMessageString(msg.what)});
            switch (msg.what) {
                case HwWifi2Controller.CMD_WIFI_TOGGLED_OFF /* 155650 */:
                case HwWifi2Controller.CMD_STA_START_FAILURE /* 155652 */:
                case HwWifi2Controller.CMD_STA_STOPPED /* 155653 */:
                    HwWifi2Controller hwWifi2Controller = HwWifi2Controller.this;
                    hwWifi2Controller.transitionTo(hwWifi2Controller.mStaDisabledState);
                    return true;
                case HwWifi2Controller.CMD_DEFERRED_ENABLE /* 155651 */:
                default:
                    return false;
            }
        }
    }

    private void readWifiReEnableDelay() {
        this.mReEnableDelayMillis = Settings.Global.getLong(this.mContext.getContentResolver(), "wifi_reenable_delay", DEFAULT_REENABLE_DELAY_MS);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getMessageString(int what) {
        switch (what) {
            case CMD_WIFI_TOGGLED_ON /* 155649 */:
                return "CMD_WIFI_TOGGLED_ON";
            case CMD_WIFI_TOGGLED_OFF /* 155650 */:
                return "CMD_WIFI_TOGGLED_OFF";
            case CMD_DEFERRED_ENABLE /* 155651 */:
                return "CMD_DEFERRED_ENABLE";
            case CMD_STA_START_FAILURE /* 155652 */:
                return "CMD_STA_START_FAILURE";
            case CMD_STA_STOPPED /* 155653 */:
                return "CMD_STA_STOPPED";
            default:
                return new Integer(what).toString();
        }
    }
}
