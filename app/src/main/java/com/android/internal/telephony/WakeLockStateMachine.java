package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.Rlog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public abstract class WakeLockStateMachine extends StateMachine {
    protected static final boolean DBG = true;
    protected static final int EVENT_BROADCAST_COMPLETE = 2;
    public static final int EVENT_NEW_SMS_MESSAGE = 1;
    static final int EVENT_RELEASE_WAKE_LOCK = 3;
    static final int EVENT_UPDATE_PHONE_OBJECT = 4;
    private static final int WAKE_LOCK_TIMEOUT = 3000;
    protected Context mContext;
    private final DefaultState mDefaultState;
    private final IdleState mIdleState;
    protected Phone mPhone;
    protected final BroadcastReceiver mReceiver;
    private final WaitingState mWaitingState;
    private final WakeLock mWakeLock;

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WakeLockStateMachine.EVENT_UPDATE_PHONE_OBJECT /*4*/:
                    WakeLockStateMachine.this.mPhone = (Phone) msg.obj;
                    WakeLockStateMachine.this.log("updatePhoneObject: phone=" + WakeLockStateMachine.this.mPhone.getClass().getSimpleName());
                    break;
                default:
                    String errorText = "processMessage: unhandled message type " + msg.what;
                    if (!Build.IS_DEBUGGABLE) {
                        WakeLockStateMachine.this.loge(errorText);
                        break;
                    }
                    throw new RuntimeException(errorText);
            }
            return WakeLockStateMachine.DBG;
        }
    }

    class IdleState extends State {
        IdleState() {
        }

        public void enter() {
            WakeLockStateMachine.this.sendMessageDelayed(WakeLockStateMachine.EVENT_RELEASE_WAKE_LOCK, 3000);
        }

        public void exit() {
            WakeLockStateMachine.this.mWakeLock.acquire();
            WakeLockStateMachine.this.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WakeLockStateMachine.EVENT_NEW_SMS_MESSAGE /*1*/:
                    if (WakeLockStateMachine.this.handleSmsMessage(msg)) {
                        WakeLockStateMachine.this.transitionTo(WakeLockStateMachine.this.mWaitingState);
                    }
                    return WakeLockStateMachine.DBG;
                case WakeLockStateMachine.EVENT_RELEASE_WAKE_LOCK /*3*/:
                    WakeLockStateMachine.this.mWakeLock.release();
                    if (WakeLockStateMachine.this.mWakeLock.isHeld()) {
                        WakeLockStateMachine.this.log("mWakeLock is still held after release");
                    } else {
                        WakeLockStateMachine.this.log("mWakeLock released");
                    }
                    return WakeLockStateMachine.DBG;
                default:
                    return false;
            }
        }
    }

    class WaitingState extends State {
        WaitingState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WakeLockStateMachine.EVENT_NEW_SMS_MESSAGE /*1*/:
                    WakeLockStateMachine.this.log("deferring message until return to idle");
                    WakeLockStateMachine.this.deferMessage(msg);
                    return WakeLockStateMachine.DBG;
                case WakeLockStateMachine.EVENT_BROADCAST_COMPLETE /*2*/:
                    WakeLockStateMachine.this.log("broadcast complete, returning to idle");
                    WakeLockStateMachine.this.transitionTo(WakeLockStateMachine.this.mIdleState);
                    return WakeLockStateMachine.DBG;
                case WakeLockStateMachine.EVENT_RELEASE_WAKE_LOCK /*3*/:
                    WakeLockStateMachine.this.mWakeLock.release();
                    if (!WakeLockStateMachine.this.mWakeLock.isHeld()) {
                        WakeLockStateMachine.this.loge("mWakeLock released while still in WaitingState!");
                    }
                    return WakeLockStateMachine.DBG;
                default:
                    return false;
            }
        }
    }

    protected abstract boolean handleSmsMessage(Message message);

    protected WakeLockStateMachine(String debugTag, Context context, Phone phone) {
        super(debugTag);
        this.mDefaultState = new DefaultState();
        this.mIdleState = new IdleState();
        this.mWaitingState = new WaitingState();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WakeLockStateMachine.this.sendMessage(WakeLockStateMachine.EVENT_BROADCAST_COMPLETE);
            }
        };
        this.mContext = context;
        this.mPhone = phone;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(EVENT_NEW_SMS_MESSAGE, debugTag);
        this.mWakeLock.acquire();
        addState(this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mWaitingState, this.mDefaultState);
        setInitialState(this.mIdleState);
    }

    public void updatePhoneObject(Phone phone) {
        sendMessage(EVENT_UPDATE_PHONE_OBJECT, phone);
    }

    public final void dispose() {
        quit();
    }

    protected void onQuitting() {
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    public final void dispatchSmsMessage(Object obj) {
        sendMessage(EVENT_NEW_SMS_MESSAGE, obj);
    }

    protected void log(String s) {
        String tag = getName();
        if (this.mPhone != null) {
            tag = tag + "[SUB" + this.mPhone.getPhoneId() + "]";
        }
        Rlog.d(tag, s);
    }

    protected void loge(String s) {
        String tag = getName();
        if (this.mPhone != null) {
            tag = tag + "[SUB" + this.mPhone.getPhoneId() + "]";
        }
        Rlog.e(tag, s);
    }

    protected void loge(String s, Throwable e) {
        String tag = getName();
        if (this.mPhone != null) {
            tag = tag + "[SUB" + this.mPhone.getPhoneId() + "]";
        }
        Rlog.e(tag, s, e);
    }
}
