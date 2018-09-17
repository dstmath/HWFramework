package com.huawei.android.bluetooth.radio;

import android.os.Message;
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
    private static final String FM_CHIPTYPE = null;
    private static final int NATIVE_STATE_RADIO_OFF = 2;
    private static final int NATIVE_STATE_RADIO_ON = 3;
    public static final int STATE_RADIO_OFF = 18;
    public static final int STATE_RADIO_ON = 17;
    private static final String TAG = "RadioStateMachine";
    private static final int USER_TURN_OFF_RADIO = 204;
    private static final int USER_TURN_ON_RADIO = 200;
    private static final boolean VDBG = false;
    private long mBluetoothInterfaceNative;
    private boolean mIsRadioOn;
    private RadioStateChangedListener mListener;
    private OffState mOffState;
    private OnState mOnState;
    private PendingCommandState mPendingCommandState;

    private class OffState extends State {
        private OffState() {
        }

        public void enter() {
            RadioStateMachineEx.this.infoLog("Entering OffState");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case RadioStateMachineEx.USER_TURN_ON_RADIO /*200*/:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=OFF, MESSAGE = USER_TURN_ON_RADIO");
                    RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mPendingCommandState);
                    RadioStateMachineEx.this.sendMessage(RadioStateMachineEx.BEGIN_ENABLE_RADIO);
                    return RadioStateMachineEx.DBG;
                default:
                    Log.d(RadioStateMachineEx.TAG, "ERROR: UNEXPECTED MESSAGE: CURRENT_STATE=OFF, MESSAGE = " + msg.what);
                    return false;
            }
        }
    }

    private class OnState extends State {
        private OnState() {
        }

        public void enter() {
            RadioStateMachineEx.this.infoLog("Entering On State");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case RadioStateMachineEx.USER_TURN_OFF_RADIO /*204*/:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=ON, MESSAGE = USER_TURN_OFF_RADIO");
                    RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mPendingCommandState);
                    RadioStateMachineEx.this.sendMessage(RadioStateMachineEx.BEGIN_DISABLE_RADIO);
                    return RadioStateMachineEx.DBG;
                default:
                    Log.d(RadioStateMachineEx.TAG, "ERROR: UNEXPECTED MESSAGE: CURRENT_STATE=ON, MESSAGE = " + msg.what);
                    return false;
            }
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
                case RadioStateMachineEx.BEGIN_ENABLE_RADIO /*201*/:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=PENDING, MESSAGE = BEGIN_ENABLE_RADIO");
                    if (!RadioStateMachineEx.enableRadioNative(RadioStateMachineEx.this.mBluetoothInterfaceNative)) {
                        Log.e(RadioStateMachineEx.TAG, "Error while turning Radio On");
                        RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mOffState);
                        break;
                    }
                    RadioStateMachineEx.this.sendMessageDelayed(RadioStateMachineEx.ENABLE_TIMEOUT, 80000);
                    break;
                case RadioStateMachineEx.ENABLED_RADIO /*202*/:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=PENDING, MESSAGE = ENABLED_RADIO,");
                    RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.ENABLE_TIMEOUT);
                    RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.DISABLE_TIMEOUT);
                    RadioStateMachineEx.this.mIsRadioOn = RadioStateMachineEx.DBG;
                    RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mOnState);
                    RadioStateMachineEx.this.notifyAdapterRadioStateChange(RadioStateMachineEx.STATE_RADIO_ON);
                    break;
                case RadioStateMachineEx.ENABLE_TIMEOUT /*203*/:
                    Log.d(RadioStateMachineEx.TAG, "CURRENT_STATE=PENDING, MESSAGE = ENABLE_TIMEOUT");
                    RadioStateMachineEx.this.errorLog("Error enabling FM-Radio");
                    RadioStateMachineEx.this.mIsRadioOn = false;
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
                    RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.ENABLE_TIMEOUT);
                    RadioStateMachineEx.this.removeMessages(RadioStateMachineEx.DISABLE_TIMEOUT);
                    RadioStateMachineEx.this.mIsRadioOn = false;
                    RadioStateMachineEx.this.transitionTo(RadioStateMachineEx.this.mOffState);
                    RadioStateMachineEx.this.notifyAdapterRadioStateChange(RadioStateMachineEx.STATE_RADIO_OFF);
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
            return RadioStateMachineEx.DBG;
        }
    }

    public interface RadioStateChangedListener {
        void onStateChanged(int i);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.bluetooth.radio.RadioStateMachineEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.bluetooth.radio.RadioStateMachineEx.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.bluetooth.radio.RadioStateMachineEx.<clinit>():void");
    }

    private static native boolean disableRadioNative(long j);

    private static native boolean enableRadioNative(long j);

    public boolean isRadioOn() {
        return this.mIsRadioOn;
    }

    public boolean enableRadio() {
        if ("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE)) {
            this.mIsRadioOn = DBG;
        } else {
            Log.d(TAG, "enableRadio() called...");
            sendMessage(USER_TURN_ON_RADIO);
        }
        return DBG;
    }

    public boolean disableRadio() {
        if ("hi110x".equals(FM_CHIPTYPE) || "hisi".equals(FM_CHIPTYPE) || "Qualcomm".equals(FM_CHIPTYPE)) {
            this.mIsRadioOn = false;
        } else {
            Log.d(TAG, "disableRadio() called...");
            sendMessage(USER_TURN_OFF_RADIO);
        }
        return DBG;
    }

    public RadioStateMachineEx(RadioStateChangedListener listener, long bluetoothInterfaceNative) {
        super("RadioStateMachineEx");
        this.mBluetoothInterfaceNative = 0;
        this.mPendingCommandState = new PendingCommandState();
        this.mOnState = new OnState();
        this.mOffState = new OffState();
        this.mIsRadioOn = false;
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
        if (status == NATIVE_STATE_RADIO_OFF) {
            sendMessage(DISABLED_RADIO);
        } else if (status == NATIVE_STATE_RADIO_ON) {
            sendMessage(ENABLED_RADIO);
        }
    }

    private void notifyAdapterRadioStateChange(int newState) {
        infoLog("Bluetooth adapter radio state changed: " + newState);
        if (this.mListener != null) {
            this.mListener.onStateChanged(newState);
        }
    }

    private void infoLog(String msg) {
        Log.i(TAG, msg);
    }

    private void errorLog(String msg) {
        Log.e(TAG, msg);
    }
}
