package com.android.server.connectivity.usbp2p;

import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.util.HashMap;
import java.util.Map;

public class UsbP2pStateMachine extends StateMachine {
    private static final int BASE_CMD = 594020;
    static final int CMD_IFACE_ADDED = 594025;
    private static final Map<Integer, String> CMD_MAP = new HashMap(10);
    static final int CMD_P2P_START = 594021;
    static final int CMD_P2P_STOP = 594022;
    static final int CMD_TETHERING_OFF = 594024;
    static final int CMD_TETHERING_ON = 594023;
    static final int CMD_USB_CONNECTED = 594026;
    private static final int INITIAL_CAPACITY = 10;
    static final String STATE_IDLE = "IdleState";
    static final String STATE_JOINT = "JointState";
    static final String STATE_P2P = "P2pState";
    static final String STATE_STARTING = "StartingState";
    static final String STATE_STOPPING = "StoppingState";
    static final String STATE_TETHER = "TetherState";
    private static final String TAG = "UsbP2pSM";
    private final UsbP2pCommands mCommands;
    private final State mIdleState;
    private final State mJointState;
    private final UsbP2pManager mManager;
    private String mP2pIface = null;
    private final State mP2pState;
    private final State mStartingState;
    private int mState = 0;
    private final Object mStateLock = new Object();
    private final State mStoppingState;
    private final State mTetherState;

    static {
        CMD_MAP.put(Integer.valueOf((int) CMD_P2P_START), "CMD_P2P_START");
        CMD_MAP.put(Integer.valueOf((int) CMD_P2P_STOP), "CMD_P2P_STOP");
        CMD_MAP.put(Integer.valueOf((int) CMD_TETHERING_ON), "CMD_TETHERING_ON");
        CMD_MAP.put(Integer.valueOf((int) CMD_TETHERING_OFF), "CMD_TETHERING_OFF");
        CMD_MAP.put(Integer.valueOf((int) CMD_IFACE_ADDED), "CMD_IFACE_ADDED");
        CMD_MAP.put(Integer.valueOf((int) CMD_USB_CONNECTED), "CMD_USB_CONNECTED");
    }

    public UsbP2pStateMachine(HandlerThread thread, UsbP2pCommands commands, UsbP2pManager manager) {
        super(TAG, thread.getLooper());
        this.mCommands = commands;
        this.mManager = manager;
        this.mIdleState = new IdleState();
        this.mStartingState = new StartingState();
        this.mP2pState = new P2pState();
        this.mStoppingState = new StoppingState();
        this.mJointState = new JointState();
        this.mTetherState = new TetherState();
        addState(this.mIdleState);
        addState(this.mStartingState, this.mIdleState);
        addState(this.mP2pState, this.mIdleState);
        addState(this.mStoppingState, this.mIdleState);
        addState(this.mJointState, this.mIdleState);
        addState(this.mTetherState, this.mIdleState);
        setInitialState(this.mIdleState);
    }

    public int getState() {
        int i;
        synchronized (this.mStateLock) {
            i = this.mState;
        }
        return i;
    }

    private class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            UsbP2pStateMachine.this.logi("Idle state enter.");
            UsbP2pStateMachine.this.updateState(0);
        }

        public boolean processMessage(Message msg) {
            UsbP2pStateMachine usbP2pStateMachine = UsbP2pStateMachine.this;
            usbP2pStateMachine.logi("IdleState handle message " + UsbP2pStateMachine.this.cmdToString(msg.what));
            int i = msg.what;
            if (i == UsbP2pStateMachine.CMD_P2P_START) {
                UsbP2pStateMachine usbP2pStateMachine2 = UsbP2pStateMachine.this;
                usbP2pStateMachine2.transitionTo(usbP2pStateMachine2.mStartingState);
                return true;
            } else if (i != UsbP2pStateMachine.CMD_TETHERING_ON) {
                return true;
            } else {
                UsbP2pStateMachine usbP2pStateMachine3 = UsbP2pStateMachine.this;
                usbP2pStateMachine3.transitionTo(usbP2pStateMachine3.mTetherState);
                return true;
            }
        }

        public String getName() {
            return UsbP2pStateMachine.STATE_IDLE;
        }
    }

    private class StartingState extends State {
        private StartingState() {
        }

        public void enter() {
            UsbP2pStateMachine.this.logi("StartingState enter.");
            if (!UsbP2pStateMachine.this.mCommands.isUsbRndisEnabled()) {
                UsbP2pStateMachine.this.mCommands.setUsbFunction(true);
            }
            if (!UsbP2pStateMachine.this.mCommands.isTetheringStarted() && !UsbP2pStateMachine.this.mCommands.startTethering()) {
                UsbP2pStateMachine.this.loge("Error in start tethering.");
            }
            UsbP2pStateMachine usbP2pStateMachine = UsbP2pStateMachine.this;
            if (usbP2pStateMachine.configIface(true, usbP2pStateMachine.mP2pIface)) {
                UsbP2pStateMachine usbP2pStateMachine2 = UsbP2pStateMachine.this;
                usbP2pStateMachine2.transitionTo(usbP2pStateMachine2.mP2pState);
            }
        }

        public boolean processMessage(Message msg) {
            UsbP2pStateMachine usbP2pStateMachine = UsbP2pStateMachine.this;
            usbP2pStateMachine.logi("StartingState handle message " + UsbP2pStateMachine.this.cmdToString(msg.what));
            switch (msg.what) {
                case UsbP2pStateMachine.CMD_P2P_START /* 594021 */:
                    return true;
                case UsbP2pStateMachine.CMD_P2P_STOP /* 594022 */:
                    UsbP2pStateMachine.this.stopUsbP2p();
                    UsbP2pStateMachine usbP2pStateMachine2 = UsbP2pStateMachine.this;
                    usbP2pStateMachine2.transitionTo(usbP2pStateMachine2.mIdleState);
                    return true;
                case UsbP2pStateMachine.CMD_TETHERING_ON /* 594023 */:
                    UsbP2pStateMachine usbP2pStateMachine3 = UsbP2pStateMachine.this;
                    usbP2pStateMachine3.transitionTo(usbP2pStateMachine3.mJointState);
                    return true;
                case UsbP2pStateMachine.CMD_TETHERING_OFF /* 594024 */:
                default:
                    return false;
                case UsbP2pStateMachine.CMD_IFACE_ADDED /* 594025 */:
                case UsbP2pStateMachine.CMD_USB_CONNECTED /* 594026 */:
                    if (msg.obj instanceof String) {
                        UsbP2pStateMachine.this.mP2pIface = (String) msg.obj;
                    }
                    UsbP2pStateMachine usbP2pStateMachine4 = UsbP2pStateMachine.this;
                    if (!usbP2pStateMachine4.configIface(true, usbP2pStateMachine4.mP2pIface)) {
                        return true;
                    }
                    UsbP2pStateMachine usbP2pStateMachine5 = UsbP2pStateMachine.this;
                    usbP2pStateMachine5.transitionTo(usbP2pStateMachine5.mP2pState);
                    return true;
            }
        }

        public String getName() {
            return UsbP2pStateMachine.STATE_STARTING;
        }
    }

    private class P2pState extends State {
        private P2pState() {
        }

        public void enter() {
            UsbP2pStateMachine.this.logi("P2pState enter.");
            UsbP2pStateMachine.this.updateState(1);
        }

        public boolean processMessage(Message msg) {
            UsbP2pStateMachine usbP2pStateMachine = UsbP2pStateMachine.this;
            usbP2pStateMachine.logi("P2pState handle message " + UsbP2pStateMachine.this.cmdToString(msg.what));
            switch (msg.what) {
                case UsbP2pStateMachine.CMD_P2P_START /* 594021 */:
                    return true;
                case UsbP2pStateMachine.CMD_P2P_STOP /* 594022 */:
                    UsbP2pStateMachine usbP2pStateMachine2 = UsbP2pStateMachine.this;
                    usbP2pStateMachine2.transitionTo(usbP2pStateMachine2.mStoppingState);
                    return true;
                case UsbP2pStateMachine.CMD_TETHERING_ON /* 594023 */:
                    UsbP2pStateMachine usbP2pStateMachine3 = UsbP2pStateMachine.this;
                    usbP2pStateMachine3.transitionTo(usbP2pStateMachine3.mJointState);
                    return true;
                default:
                    return false;
            }
        }

        public String getName() {
            return UsbP2pStateMachine.STATE_P2P;
        }
    }

    private class StoppingState extends State {
        private StoppingState() {
        }

        public void enter() {
            UsbP2pStateMachine.this.logi("StoppingState enter.");
            UsbP2pStateMachine usbP2pStateMachine = UsbP2pStateMachine.this;
            usbP2pStateMachine.configIface(false, usbP2pStateMachine.mP2pIface);
            if (UsbP2pStateMachine.this.mCommands.isUsbRndisEnabled()) {
                UsbP2pStateMachine.this.mCommands.setUsbFunction(false);
            }
            if (!UsbP2pStateMachine.this.mManager.isOtherIfaceTethered()) {
                UsbP2pStateMachine.this.mCommands.stopTethering();
            }
            UsbP2pStateMachine.this.mP2pIface = null;
            UsbP2pStateMachine usbP2pStateMachine2 = UsbP2pStateMachine.this;
            usbP2pStateMachine2.transitionTo(usbP2pStateMachine2.mIdleState);
        }

        public String getName() {
            return UsbP2pStateMachine.STATE_STOPPING;
        }
    }

    private class JointState extends State {
        private JointState() {
        }

        public void enter() {
            UsbP2pStateMachine.this.logi("JointState enter.");
            UsbP2pStateMachine.this.updateState(2);
        }

        public boolean processMessage(Message msg) {
            UsbP2pStateMachine usbP2pStateMachine = UsbP2pStateMachine.this;
            usbP2pStateMachine.logi("JointState handle message " + UsbP2pStateMachine.this.cmdToString(msg.what));
            int i = msg.what;
            if (i == UsbP2pStateMachine.CMD_P2P_STOP) {
                UsbP2pStateMachine usbP2pStateMachine2 = UsbP2pStateMachine.this;
                usbP2pStateMachine2.transitionTo(usbP2pStateMachine2.mTetherState);
                return true;
            } else if (i != UsbP2pStateMachine.CMD_TETHERING_OFF) {
                return false;
            } else {
                UsbP2pStateMachine usbP2pStateMachine3 = UsbP2pStateMachine.this;
                usbP2pStateMachine3.transitionTo(usbP2pStateMachine3.mP2pState);
                return true;
            }
        }

        public String getName() {
            return UsbP2pStateMachine.STATE_JOINT;
        }
    }

    private class TetherState extends State {
        private TetherState() {
        }

        public void enter() {
            UsbP2pStateMachine.this.logi("TetherState enter.");
            UsbP2pStateMachine.this.updateState(2);
        }

        public boolean processMessage(Message msg) {
            UsbP2pStateMachine usbP2pStateMachine = UsbP2pStateMachine.this;
            usbP2pStateMachine.logi("TetherState handle message " + UsbP2pStateMachine.this.cmdToString(msg.what));
            int i = msg.what;
            if (i == UsbP2pStateMachine.CMD_P2P_START) {
                UsbP2pStateMachine usbP2pStateMachine2 = UsbP2pStateMachine.this;
                usbP2pStateMachine2.transitionTo(usbP2pStateMachine2.mJointState);
                return true;
            } else if (i != UsbP2pStateMachine.CMD_TETHERING_OFF) {
                return false;
            } else {
                UsbP2pStateMachine usbP2pStateMachine3 = UsbP2pStateMachine.this;
                usbP2pStateMachine3.transitionTo(usbP2pStateMachine3.mIdleState);
                return true;
            }
        }

        public String getName() {
            return UsbP2pStateMachine.STATE_TETHER;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean configIface(boolean isToUp, String iface) {
        boolean isIpAddressCongiured = false;
        boolean isTetherConfigured = false;
        if (!TextUtils.isEmpty(iface)) {
            isIpAddressCongiured = this.mCommands.configureIpv4(isToUp, iface);
            UsbP2pCommands usbP2pCommands = this.mCommands;
            isTetherConfigured = isToUp ? usbP2pCommands.tetherInterface(iface) : usbP2pCommands.untetherInterface(iface);
        }
        logi("configIface: iface = " + iface + " to " + isToUp + " result = [" + isIpAddressCongiured + "," + isTetherConfigured + "]");
        return isIpAddressCongiured && isTetherConfigured;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopUsbP2p() {
        this.mCommands.setUsbFunction(false);
        if (this.mCommands.isTetheringStarted() && !this.mManager.isOtherIfaceTethered()) {
            this.mCommands.stopTethering();
            logi("stop usb p2p, ");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateState(int newState) {
        synchronized (this.mStateLock) {
            if (this.mState != newState) {
                this.mState = newState;
                this.mManager.notifyStateChange(newState);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String cmdToString(int cmd) {
        if (CMD_MAP.containsKey(Integer.valueOf(cmd))) {
            return CMD_MAP.get(Integer.valueOf(cmd));
        }
        return "UNKOWN CMD ID " + cmd;
    }
}
