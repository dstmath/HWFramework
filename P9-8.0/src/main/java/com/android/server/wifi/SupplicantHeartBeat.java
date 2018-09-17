package com.android.server.wifi;

import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class SupplicantHeartBeat extends StateMachine implements HwSupplicantHeartBeat {
    private static final int BASE = 999900;
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
    private WifiStateMachine mWifiStateMachine;

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message msg) {
            if (SupplicantHeartBeat.DBG) {
                Log.d(SupplicantHeartBeat.TAG, "enter DefaultState: msg = " + msg.toString() + "\n");
            }
            switch (msg.what) {
                case SupplicantHeartBeat.CMD_ENTER_SUPPLICANT_STARTED /*999901*/:
                    SupplicantHeartBeat.this.transitionTo(SupplicantHeartBeat.this.mHeartAckState);
                    break;
                case SupplicantHeartBeat.CMD_EXIT_SUPPLICANT_STARTED /*999902*/:
                    SupplicantHeartBeat.this.transitionTo(SupplicantHeartBeat.this.mExitSupplicantStarted);
                    break;
            }
            return true;
        }
    }

    class EnterSupplicantStarted extends State {
        EnterSupplicantStarted() {
        }

        public void enter() {
            SupplicantHeartBeat.this.mHeartBeatEnabled = SupplicantHeartBeat.this.checkHeartBeatStatus();
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

    class HeartAckState extends State {
        HeartAckState() {
        }

        public void enter() {
            if (SupplicantHeartBeat.this.mHeartBeatEnabled && SupplicantHeartBeat.this.isEnableCheck()) {
                SupplicantHeartBeat.this.sendMessageDelayed(SupplicantHeartBeat.CMD_BEGIN_HEART_BEAT, SupplicantHeartBeat.HEART_BEAT_INTERVAL);
            } else if (SupplicantHeartBeat.DBG) {
                Log.d(SupplicantHeartBeat.TAG, "HeartAckState not enable");
            }
        }

        public boolean processMessage(Message msg) {
            if (SupplicantHeartBeat.DBG) {
                Log.d(SupplicantHeartBeat.TAG, "enter HeartAckState: msg = " + msg.toString() + "\n");
            }
            switch (msg.what) {
                case 147506:
                    SupplicantHeartBeat.this.deferMessage(msg);
                    break;
                case SupplicantHeartBeat.CMD_BEGIN_HEART_BEAT /*999903*/:
                    SupplicantHeartBeat.this.heartBeat();
                    SupplicantHeartBeat.this.transitionTo(SupplicantHeartBeat.this.mWaitingHeartAckState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class WaitingHeartAckState extends State {
        private int mRetryCount;

        WaitingHeartAckState() {
        }

        public void enter() {
            SupplicantHeartBeat.this.sendMessageDelayed(SupplicantHeartBeat.CMD_HEART_BEAT_ACK_TIMEOUT, 3000);
            this.mRetryCount = 0;
        }

        public boolean processMessage(Message msg) {
            if (SupplicantHeartBeat.DBG) {
                Log.d(SupplicantHeartBeat.TAG, "enter WaitingHeartAckState: msg = " + msg.toString() + "\n");
            }
            switch (msg.what) {
                case 147506:
                    SupplicantHeartBeat.this.transitionTo(SupplicantHeartBeat.this.mHeartAckState);
                    break;
                case SupplicantHeartBeat.CMD_HEART_BEAT_ACK_TIMEOUT /*999904*/:
                    if (this.mRetryCount <= 1) {
                        this.mRetryCount++;
                        SupplicantHeartBeat.this.heartBeat();
                        SupplicantHeartBeat.this.sendMessageDelayed(SupplicantHeartBeat.CMD_HEART_BEAT_ACK_TIMEOUT, 3000);
                        break;
                    }
                    this.mRetryCount = 0;
                    recoverSupplicantConnection();
                    if (SupplicantHeartBeat.DBG) {
                        Log.d(SupplicantHeartBeat.TAG, "ABNORMAL_SUPPLICANT_SOCKET");
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void recoverSupplicantConnection() {
            if (SupplicantHeartBeat.DBG) {
                Log.d(SupplicantHeartBeat.TAG, "recoverSupplicantConnection");
            }
            SupplicantHeartBeat.this.mWifiStateMachine.setSupplicantRunning(false);
            SupplicantHeartBeat.this.mWifiStateMachine.setSupplicantRunning(true);
        }

        public void exit() {
            this.mRetryCount = 0;
        }
    }

    public static HwSupplicantHeartBeat createHwSupplicantHeartBeat(WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        if (DBG) {
            Log.d(TAG, "createHwSupplicantHeartBeat is called!");
        }
        return new SupplicantHeartBeat(wifiStateMachine, wifiNative);
    }

    public SupplicantHeartBeat(WifiStateMachine wsm, WifiNative wifiNative) {
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

    private void heartBeat() {
        if (DBG) {
            Log.d(TAG, "enter heartBeat ...");
        }
        this.mWifiNative.heartBeat("CHECK");
    }

    private boolean checkHeartBeatStatus() {
        if ("HEART-BEAT-ENABLED".equals(this.mWifiNative.heartBeat("STATUS"))) {
            return true;
        }
        return false;
    }

    private boolean isEnableCheck() {
        return SystemProperties.getInt("ro.config.hw_suppHeartBeat", 1) != 0;
    }
}
