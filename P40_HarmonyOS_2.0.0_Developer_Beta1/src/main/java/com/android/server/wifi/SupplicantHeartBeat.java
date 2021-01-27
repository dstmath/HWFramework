package com.android.server.wifi;

import android.os.Message;
import android.os.SystemProperties;
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class SupplicantHeartBeat extends StateMachine implements HwSupplicantHeartBeat {
    static final int BASE = 999900;
    private static final int CMD_BEGIN_HEART_BEAT = 999903;
    static final int CMD_ENTER_SUPPLICANT_STARTED = 999901;
    static final int CMD_EXIT_SUPPLICANT_STARTED = 999902;
    private static final int CMD_HEART_BEAT_ACK_TIMEOUT = 999904;
    private static boolean DBG = true;
    public static final String HEART_BEAT = "HEART_BEAT";
    public static final int HEART_BEAT_ACK_EVENT = 147506;
    private static final long HEART_BEAT_ACK_TIMEOUT = 3000;
    private static final long HEART_BEAT_INTERVAL = 15000;
    private static final int RETRY_MAX_TIMES = 1;
    private static final String TAG = "SupplicantHeartBeat";
    private State mDefaultState = new DefaultState();
    private State mEnterSupplicantStarted = new EnterSupplicantStarted();
    private State mExitSupplicantStarted = new ExitSupplicantStarted();
    private State mHeartAckState = new HeartAckState();
    private boolean mHeartBeatEnabled = false;
    private State mWaitingHeartAckState = new WaitingHeartAckState();
    private WifiNative mWifiNative;
    private ClientModeImpl mWifiStateMachine;

    public static HwSupplicantHeartBeat createHwSupplicantHeartBeat(ClientModeImpl wifiStateMachine, WifiNative wifiNative) {
        if (DBG) {
            HwHiLog.d(TAG, false, "createHwSupplicantHeartBeat is called!", new Object[0]);
        }
        return new SupplicantHeartBeat(wifiStateMachine, wifiNative);
    }

    public SupplicantHeartBeat(ClientModeImpl wsm, WifiNative wifiNative) {
        super(TAG);
        this.mWifiStateMachine = wsm;
        this.mWifiNative = wifiNative;
        addState(this.mDefaultState);
        addState(this.mExitSupplicantStarted, this.mDefaultState);
        addState(this.mEnterSupplicantStarted, this.mDefaultState);
        addState(this.mWaitingHeartAckState, this.mEnterSupplicantStarted);
        addState(this.mHeartAckState, this.mEnterSupplicantStarted);
        setInitialState(this.mExitSupplicantStarted);
        setDbg(DBG);
        setLogOnlyTransitions(true);
        start();
    }

    public void enterSupplicantStarted() {
        sendMessage(CMD_ENTER_SUPPLICANT_STARTED);
    }

    public void exitSupplicantStarted() {
        sendMessage(CMD_EXIT_SUPPLICANT_STARTED);
    }

    public void handleHeartBeatAckEvent() {
        sendMessage(147506);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void heartBeat() {
        if (DBG) {
            HwHiLog.d(TAG, false, "enter heartBeat ...", new Object[0]);
        }
        this.mWifiNative.mHwWifiNativeEx.heartBeat("CHECK");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkHeartBeatStatus() {
        if ("HEART-BEAT-ENABLED".equals(this.mWifiNative.mHwWifiNativeEx.heartBeat("STATUS"))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isEnableCheck() {
        return SystemProperties.getInt("ro.config.hw_suppHeartBeat", 1) != 0;
    }

    class EnterSupplicantStarted extends State {
        EnterSupplicantStarted() {
        }

        public void enter() {
            SupplicantHeartBeat supplicantHeartBeat = SupplicantHeartBeat.this;
            supplicantHeartBeat.mHeartBeatEnabled = supplicantHeartBeat.checkHeartBeatStatus();
        }
    }

    class HeartAckState extends State {
        HeartAckState() {
        }

        public void enter() {
            if (SupplicantHeartBeat.this.mHeartBeatEnabled && SupplicantHeartBeat.this.isEnableCheck()) {
                SupplicantHeartBeat.this.sendMessageDelayed(SupplicantHeartBeat.CMD_BEGIN_HEART_BEAT, SupplicantHeartBeat.HEART_BEAT_INTERVAL);
            } else if (SupplicantHeartBeat.DBG) {
                HwHiLog.d(SupplicantHeartBeat.TAG, false, "HeartAckState not enable", new Object[0]);
            }
        }

        public boolean processMessage(Message msg) {
            if (SupplicantHeartBeat.DBG) {
                HwHiLog.d(SupplicantHeartBeat.TAG, false, "enter HeartAckState: msg = %{public}s", new Object[]{msg.toString()});
            }
            int i = msg.what;
            if (i == 147506) {
                SupplicantHeartBeat.this.deferMessage(msg);
            } else if (i != SupplicantHeartBeat.CMD_BEGIN_HEART_BEAT) {
                return false;
            } else {
                SupplicantHeartBeat.this.heartBeat();
                SupplicantHeartBeat supplicantHeartBeat = SupplicantHeartBeat.this;
                supplicantHeartBeat.transitionTo(supplicantHeartBeat.mWaitingHeartAckState);
            }
            return true;
        }
    }

    class WaitingHeartAckState extends State {
        private int mRetryCount;

        WaitingHeartAckState() {
        }

        public void enter() {
            SupplicantHeartBeat.this.sendMessageDelayed(SupplicantHeartBeat.CMD_HEART_BEAT_ACK_TIMEOUT, SupplicantHeartBeat.HEART_BEAT_ACK_TIMEOUT);
            this.mRetryCount = 0;
        }

        public boolean processMessage(Message msg) {
            if (SupplicantHeartBeat.DBG) {
                HwHiLog.d(SupplicantHeartBeat.TAG, false, "enter WaitingHeartAckState: msg = %{public}s", new Object[]{msg.toString()});
            }
            int i = msg.what;
            if (i == 147506) {
                SupplicantHeartBeat supplicantHeartBeat = SupplicantHeartBeat.this;
                supplicantHeartBeat.transitionTo(supplicantHeartBeat.mHeartAckState);
            } else if (i != SupplicantHeartBeat.CMD_HEART_BEAT_ACK_TIMEOUT) {
                return false;
            } else {
                int i2 = this.mRetryCount;
                if (i2 > 1) {
                    this.mRetryCount = 0;
                    recoverSupplicantConnection();
                    if (SupplicantHeartBeat.DBG) {
                        HwHiLog.d(SupplicantHeartBeat.TAG, false, "ABNORMAL_SUPPLICANT_SOCKET", new Object[0]);
                    }
                } else {
                    this.mRetryCount = i2 + 1;
                    SupplicantHeartBeat.this.heartBeat();
                    SupplicantHeartBeat.this.sendMessageDelayed(SupplicantHeartBeat.CMD_HEART_BEAT_ACK_TIMEOUT, SupplicantHeartBeat.HEART_BEAT_ACK_TIMEOUT);
                }
            }
            return true;
        }

        private void recoverSupplicantConnection() {
            if (SupplicantHeartBeat.DBG) {
                HwHiLog.d(SupplicantHeartBeat.TAG, false, "recoverSupplicantConnection", new Object[0]);
            }
        }

        public void exit() {
            this.mRetryCount = 0;
        }
    }

    class ExitSupplicantStarted extends State {
        ExitSupplicantStarted() {
        }

        public void enter() {
            SupplicantHeartBeat.this.getHandler().removeMessages(SupplicantHeartBeat.CMD_BEGIN_HEART_BEAT);
            SupplicantHeartBeat.this.getHandler().removeMessages(SupplicantHeartBeat.CMD_HEART_BEAT_ACK_TIMEOUT);
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message msg) {
            if (SupplicantHeartBeat.DBG) {
                HwHiLog.d(SupplicantHeartBeat.TAG, false, "enter DefaultState: msg = %{public}s", new Object[]{msg.toString()});
            }
            switch (msg.what) {
                case SupplicantHeartBeat.CMD_ENTER_SUPPLICANT_STARTED /* 999901 */:
                    SupplicantHeartBeat supplicantHeartBeat = SupplicantHeartBeat.this;
                    supplicantHeartBeat.transitionTo(supplicantHeartBeat.mHeartAckState);
                    break;
                case SupplicantHeartBeat.CMD_EXIT_SUPPLICANT_STARTED /* 999902 */:
                    SupplicantHeartBeat supplicantHeartBeat2 = SupplicantHeartBeat.this;
                    supplicantHeartBeat2.transitionTo(supplicantHeartBeat2.mExitSupplicantStarted);
                    break;
            }
            return true;
        }
    }
}
