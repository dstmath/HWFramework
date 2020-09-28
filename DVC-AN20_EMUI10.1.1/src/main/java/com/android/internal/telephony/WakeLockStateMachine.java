package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.Rlog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class WakeLockStateMachine extends StateMachine {
    protected static final boolean DBG = true;
    protected static final int EVENT_BROADCAST_COMPLETE = 2;
    public static final int EVENT_NEW_SMS_MESSAGE = 1;
    static final int EVENT_RELEASE_WAKE_LOCK = 3;
    private static final int WAKE_LOCK_TIMEOUT = 3000;
    @UnsupportedAppUsage
    protected Context mContext;
    private final DefaultState mDefaultState = new DefaultState();
    @UnsupportedAppUsage
    private final IdleState mIdleState = new IdleState();
    @UnsupportedAppUsage
    protected Phone mPhone;
    protected final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.WakeLockStateMachine.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (WakeLockStateMachine.this.mReceiverCount.decrementAndGet() == 0) {
                WakeLockStateMachine.this.sendMessage(2);
            }
        }
    };
    protected AtomicInteger mReceiverCount = new AtomicInteger(0);
    private final WaitingState mWaitingState = new WaitingState();
    private final PowerManager.WakeLock mWakeLock;

    /* access modifiers changed from: protected */
    public abstract boolean handleSmsMessage(Message message);

    protected WakeLockStateMachine(String debugTag, Context context, Phone phone) {
        super(debugTag);
        this.mContext = context;
        this.mPhone = phone;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, debugTag);
        this.mWakeLock.acquire();
        addState(this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mWaitingState, this.mDefaultState);
        setInitialState(this.mIdleState);
    }

    public final void dispose() {
        quit();
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    public final void dispatchSmsMessage(Object obj) {
        sendMessage(1, obj);
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            String errorText = "processMessage: unhandled message type " + msg.what;
            if (!Build.IS_DEBUGGABLE) {
                WakeLockStateMachine.this.loge(errorText);
                return true;
            }
            throw new RuntimeException(errorText);
        }
    }

    /* access modifiers changed from: package-private */
    public class IdleState extends State {
        IdleState() {
        }

        public void enter() {
            WakeLockStateMachine.this.sendMessageDelayed(3, 3000);
        }

        public void exit() {
            WakeLockStateMachine.this.mWakeLock.acquire();
            WakeLockStateMachine.this.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                if (WakeLockStateMachine.this.handleSmsMessage(msg)) {
                    WakeLockStateMachine wakeLockStateMachine = WakeLockStateMachine.this;
                    wakeLockStateMachine.transitionTo(wakeLockStateMachine.mWaitingState);
                }
                return true;
            } else if (i != 3) {
                return false;
            } else {
                WakeLockStateMachine.this.mWakeLock.release();
                if (WakeLockStateMachine.this.mWakeLock.isHeld()) {
                    WakeLockStateMachine.this.log("mWakeLock is still held after release");
                } else {
                    WakeLockStateMachine.this.log("mWakeLock released");
                }
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class WaitingState extends State {
        WaitingState() {
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                WakeLockStateMachine.this.log("deferring message until return to idle");
                WakeLockStateMachine.this.deferMessage(msg);
                return true;
            } else if (i == 2) {
                WakeLockStateMachine.this.log("broadcast complete, returning to idle");
                WakeLockStateMachine wakeLockStateMachine = WakeLockStateMachine.this;
                wakeLockStateMachine.transitionTo(wakeLockStateMachine.mIdleState);
                return true;
            } else if (i != 3) {
                return false;
            } else {
                WakeLockStateMachine.this.mWakeLock.release();
                if (!WakeLockStateMachine.this.mWakeLock.isHeld()) {
                    WakeLockStateMachine.this.loge("mWakeLock released while still in WaitingState!");
                }
                return true;
            }
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void log(String s) {
        String tag = getName();
        if (this.mPhone != null) {
            tag = tag + "[SUB" + this.mPhone.getPhoneId() + "]";
        }
        Rlog.d(tag, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        String tag = getName();
        if (this.mPhone != null) {
            tag = tag + "[SUB" + this.mPhone.getPhoneId() + "]";
        }
        Rlog.e(tag, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        String tag = getName();
        if (this.mPhone != null) {
            tag = tag + "[SUB" + this.mPhone.getPhoneId() + "]";
        }
        Rlog.e(tag, s, e);
    }
}
