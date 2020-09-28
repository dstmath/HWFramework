package com.huawei.android.bluetooth.radio;

import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class RadioStateMachineEx extends StateMachine {
    private static final int BEGIN_DISABLE_RADIO = 205;
    private static final int BEGIN_ENABLE_RADIO = 201;
    private static final boolean DBG = false;
    private static final int DISABLED_RADIO = 206;
    private static final int DISABLE_TIMEOUT = 207;
    private static final int DISABLE_TIMEOUT_DELAY = 80000;
    private static final int ENABLED_RADIO = 202;
    private static final int ENABLE_TIMEOUT = 203;
    private static final int ENABLE_TIMEOUT_DELAY = 80000;
    private static final String FM_CHIPTYPE = SystemProperties.get("ro.connectivity.chiptype");
    private static final int NATIVE_STATE_RADIO_OFF = 2;
    private static final int NATIVE_STATE_RADIO_ON = 3;
    public static final int STATE_RADIO_OFF = 18;
    public static final int STATE_RADIO_ON = 17;
    private static final String TAG = "RadioStateMachine";
    private static final int USER_TURN_OFF_RADIO = 204;
    private static final int USER_TURN_ON_RADIO = 200;
    private static final boolean VDBG = false;
    private long mBluetoothInterfaceNative = 0;
    private boolean mIsRadioOn = false;
    private RadioStateChangedListener mListener;
    private OffState mOffState = new OffState();
    private OnState mOnState = new OnState();
    private PendingCommandState mPendingCommandState = new PendingCommandState();

    public interface RadioStateChangedListener {
        void onStateChanged(int i);
    }

    /* access modifiers changed from: private */
    public static native boolean disableRadioNative(long j);

    /* access modifiers changed from: private */
    public static native boolean enableRadioNative(long j);

    static {
        System.loadLibrary("bluetoothex_jni");
    }

    public boolean isRadioOn() {
        return this.mIsRadioOn;
    }

    public boolean enableRadio() {
        if ("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE)) {
            this.mIsRadioOn = true;
        } else {
            sendMessage(200);
        }
        return true;
    }

    public boolean disableRadio() {
        if ("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE)) {
            this.mIsRadioOn = false;
            return true;
        }
        sendMessage(204);
        return true;
    }

    public RadioStateMachineEx(RadioStateChangedListener listener, long bluetoothInterfaceNative) {
        super("RadioStateMachineEx");
        this.mListener = listener;
        this.mBluetoothInterfaceNative = bluetoothInterfaceNative;
        addState(this.mOnState);
        addState(this.mOffState);
        addState(this.mPendingCommandState);
        setInitialState(this.mOffState);
        start();
    }

    public void doQuit() {
        quitNow();
    }

    public void cleanup() {
        if (this.mListener != null) {
            this.mListener = null;
        }
    }

    public void stateChangeCallback(int status) {
        if (status == 2) {
            sendMessage(DISABLED_RADIO);
        } else if (status == 3) {
            sendMessage(202);
        }
    }

    /* access modifiers changed from: private */
    public class OffState extends State {
        private OffState() {
        }

        public void enter() {
            RadioStateMachineEx.this.infoLog("Entering OffState");
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 200) {
                return false;
            }
            RadioStateMachineEx radioStateMachineEx = RadioStateMachineEx.this;
            radioStateMachineEx.transitionTo(radioStateMachineEx.mPendingCommandState);
            RadioStateMachineEx.this.sendMessage(201);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class OnState extends State {
        private OnState() {
        }

        public void enter() {
            RadioStateMachineEx.this.infoLog("Entering On State");
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 204) {
                return false;
            }
            RadioStateMachineEx radioStateMachineEx = RadioStateMachineEx.this;
            radioStateMachineEx.transitionTo(radioStateMachineEx.mPendingCommandState);
            RadioStateMachineEx.this.sendMessage(RadioStateMachineEx.BEGIN_DISABLE_RADIO);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class PendingCommandState extends State {
        private PendingCommandState() {
        }

        public void enter() {
            RadioStateMachineEx.this.infoLog("Entering PendingCommandState State");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 201:
                    if (RadioStateMachineEx.enableRadioNative(RadioStateMachineEx.this.mBluetoothInterfaceNative)) {
                        RadioStateMachineEx.this.sendMessageDelayed(203, 80000);
                        break;
                    } else {
                        Log.e(RadioStateMachineEx.TAG, "Error while turning Radio On");
                        RadioStateMachineEx radioStateMachineEx = RadioStateMachineEx.this;
                        radioStateMachineEx.transitionTo(radioStateMachineEx.mOffState);
                        break;
                    }
                case 202:
                    RadioStateMachineEx.this.removeMessages(203);
                    RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.DISABLE_TIMEOUT);
                    RadioStateMachineEx.this.mIsRadioOn = true;
                    RadioStateMachineEx radioStateMachineEx2 = RadioStateMachineEx.this;
                    radioStateMachineEx2.transitionTo(radioStateMachineEx2.mOnState);
                    RadioStateMachineEx.this.notifyAdapterRadioStateChange(17);
                    break;
                case 203:
                    RadioStateMachineEx.this.errorLog("Error enabling FM-Radio");
                    RadioStateMachineEx.this.mIsRadioOn = false;
                    RadioStateMachineEx radioStateMachineEx3 = RadioStateMachineEx.this;
                    radioStateMachineEx3.transitionTo(radioStateMachineEx3.mOffState);
                    break;
                case 204:
                default:
                    return false;
                case RadioStateMachineEx.BEGIN_DISABLE_RADIO /*{ENCODED_INT: 205}*/:
                    RadioStateMachineEx.this.sendMessageDelayed(RadioStateMachineEx.DISABLE_TIMEOUT, 80000);
                    if (!RadioStateMachineEx.disableRadioNative(RadioStateMachineEx.this.mBluetoothInterfaceNative)) {
                        RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.DISABLE_TIMEOUT);
                        Log.e(RadioStateMachineEx.TAG, "Error while turning Radio Off");
                        RadioStateMachineEx radioStateMachineEx4 = RadioStateMachineEx.this;
                        radioStateMachineEx4.transitionTo(radioStateMachineEx4.mOnState);
                        break;
                    }
                    break;
                case RadioStateMachineEx.DISABLED_RADIO /*{ENCODED_INT: 206}*/:
                    RadioStateMachineEx.this.removeMessages(203);
                    RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.DISABLE_TIMEOUT);
                    RadioStateMachineEx.this.mIsRadioOn = false;
                    RadioStateMachineEx radioStateMachineEx5 = RadioStateMachineEx.this;
                    radioStateMachineEx5.transitionTo(radioStateMachineEx5.mOffState);
                    RadioStateMachineEx.this.notifyAdapterRadioStateChange(18);
                    break;
                case RadioStateMachineEx.DISABLE_TIMEOUT /*{ENCODED_INT: 207}*/:
                    RadioStateMachineEx.this.errorLog("Error disabling Bluetooth");
                    RadioStateMachineEx radioStateMachineEx6 = RadioStateMachineEx.this;
                    radioStateMachineEx6.transitionTo(radioStateMachineEx6.mOnState);
                    break;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAdapterRadioStateChange(int newState) {
        infoLog("Bluetooth adapter radio state changed: " + newState);
        RadioStateChangedListener radioStateChangedListener = this.mListener;
        if (radioStateChangedListener != null) {
            radioStateChangedListener.onStateChanged(newState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void infoLog(String msg) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void errorLog(String msg) {
        Log.e(TAG, msg);
    }
}
