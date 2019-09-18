package com.huawei.android.bluetooth.radio;

import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class RadioStateMachineEx extends StateMachine {
    private static final int BEGIN_DISABLE_RADIO = 205;
    private static final int BEGIN_ENABLE_RADIO = 201;
    private static final boolean DBG = true;
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
    /* access modifiers changed from: private */
    public long mBluetoothInterfaceNative = 0;
    /* access modifiers changed from: private */
    public boolean mIsRadioOn = false;
    private RadioStateChangedListener mListener;
    /* access modifiers changed from: private */
    public OffState mOffState = new OffState();
    /* access modifiers changed from: private */
    public OnState mOnState = new OnState();
    /* access modifiers changed from: private */
    public PendingCommandState mPendingCommandState = new PendingCommandState();

    private class OffState extends State {
        private OffState() {
        }

        public void enter() {
            RadioStateMachineEx.this.infoLog("Entering OffState");
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 200) {
                Log.d(RadioStateMachineEx.TAG, "ERROR: UNEXPECTED MESSAGE: CURRENT_STATE=OFF, MESSAGE = " + msg.what);
                return false;
            }
            Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=OFF, MESSAGE = USER_TURN_ON_RADIO");
            RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mPendingCommandState);
            RadioStateMachineEx.this.sendMessage(201);
            return true;
        }
    }

    private class OnState extends State {
        private OnState() {
        }

        public void enter() {
            RadioStateMachineEx.this.infoLog("Entering On State");
        }

        public boolean processMessage(Message msg) {
            if (msg.what != RadioStateMachineEx.USER_TURN_OFF_RADIO) {
                Log.d(RadioStateMachineEx.TAG, "ERROR: UNEXPECTED MESSAGE: CURRENT_STATE=ON, MESSAGE = " + msg.what);
                return false;
            }
            Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=ON, MESSAGE = USER_TURN_OFF_RADIO");
            RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mPendingCommandState);
            RadioStateMachineEx.this.sendMessage(RadioStateMachineEx.BEGIN_DISABLE_RADIO);
            return true;
        }
    }

    private class PendingCommandState extends State {
        private PendingCommandState() {
        }

        public void enter() {
            RadioStateMachineEx.this.infoLog("Entering PendingCommandState State");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 201:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=PENDING, MESSAGE = BEGIN_ENABLE_RADIO");
                    if (RadioStateMachineEx.enableRadioNative(RadioStateMachineEx.this.mBluetoothInterfaceNative)) {
                        RadioStateMachineEx.this.sendMessageDelayed(203, 80000);
                        break;
                    } else {
                        Log.e(RadioStateMachineEx.TAG, "Error while turning Radio On");
                        RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mOffState);
                        break;
                    }
                case 202:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=PENDING, MESSAGE = ENABLED_RADIO,");
                    RadioStateMachineEx.this.removeMessages(203);
                    RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.DISABLE_TIMEOUT);
                    boolean unused = RadioStateMachineEx.this.mIsRadioOn = true;
                    RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mOnState);
                    RadioStateMachineEx.this.notifyAdapterRadioStateChange(17);
                    break;
                case 203:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=PENDING, MESSAGE = ENABLE_TIMEOUT");
                    RadioStateMachineEx.this.errorLog("Error enabling FM-Radio");
                    boolean unused2 = RadioStateMachineEx.this.mIsRadioOn = false;
                    RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mOffState);
                    break;
                case RadioStateMachineEx.BEGIN_DISABLE_RADIO /*205*/:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=PENDING, MESSAGE = BEGIN_DISABLE_RADIO");
                    RadioStateMachineEx.this.sendMessageDelayed(RadioStateMachineEx.DISABLE_TIMEOUT, 80000);
                    if (!RadioStateMachineEx.disableRadioNative(RadioStateMachineEx.this.mBluetoothInterfaceNative)) {
                        RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.DISABLE_TIMEOUT);
                        Log.e(RadioStateMachineEx.TAG, "Error while turning Radio Off");
                        RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mOnState);
                        break;
                    }
                    break;
                case RadioStateMachineEx.DISABLED_RADIO /*206*/:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=PENDING, MESSAGE = DISABLED_RADIO");
                    RadioStateMachineEx.this.removeMessages(203);
                    RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.DISABLE_TIMEOUT);
                    boolean unused3 = RadioStateMachineEx.this.mIsRadioOn = false;
                    RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mOffState);
                    RadioStateMachineEx.this.notifyAdapterRadioStateChange(18);
                    break;
                case RadioStateMachineEx.DISABLE_TIMEOUT /*207*/:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=PENDING, MESSAGE = DISABLE_TIMEOUT");
                    RadioStateMachineEx.this.errorLog("Error disabling Bluetooth");
                    RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mOnState);
                    break;
                default:
                    Log.d(RadioStateMachineEx.TAG, "ERROR: UNEXPECTED MESSAGE: CURRENT_STATE=PENDING, MESSAGE = " + msg.what);
                    return false;
            }
            return true;
        }
    }

    public interface RadioStateChangedListener {
        void onStateChanged(int i);
    }

    /* access modifiers changed from: private */
    public static native boolean disableRadioNative(long j);

    /* access modifiers changed from: private */
    public static native boolean enableRadioNative(long j);

    static {
        Log.d(TAG, "Loading bluetoothex_jni JNI Library");
        System.loadLibrary("bluetoothex_jni");
    }

    public boolean isRadioOn() {
        return this.mIsRadioOn;
    }

    public boolean enableRadio() {
        if ("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE)) {
            this.mIsRadioOn = true;
        } else {
            Log.d(TAG, "enableRadio() called...");
            sendMessage(200);
        }
        return true;
    }

    public boolean disableRadio() {
        if ("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE)) {
            this.mIsRadioOn = false;
        } else {
            Log.d(TAG, "disableRadio() called...");
            sendMessage(USER_TURN_OFF_RADIO);
        }
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
    public void notifyAdapterRadioStateChange(int newState) {
        infoLog("Bluetooth adapter radio state changed: " + newState);
        if (this.mListener != null) {
            this.mListener.onStateChanged(newState);
        }
    }

    /* access modifiers changed from: private */
    public void infoLog(String msg) {
        Log.i(TAG, msg);
    }

    /* access modifiers changed from: private */
    public void errorLog(String msg) {
        Log.e(TAG, msg);
    }
}
