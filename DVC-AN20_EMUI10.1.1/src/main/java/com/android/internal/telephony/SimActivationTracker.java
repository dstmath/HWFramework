package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.Rlog;
import android.util.LocalLog;
import com.android.internal.util.IndentingPrintWriter;
import com.huawei.internal.telephony.IccCardConstantsEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SimActivationTracker {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "SAT";
    private static final boolean VDBG = Rlog.isLoggable(LOG_TAG, 2);
    private int mDataActivationState;
    private final LocalLog mDataActivationStateLog = new LocalLog(10);
    private Phone mPhone;
    private final BroadcastReceiver mReceiver;
    private int mVoiceActivationState;
    private final LocalLog mVoiceActivationStateLog = new LocalLog(10);

    public SimActivationTracker(Phone phone) {
        this.mPhone = phone;
        this.mVoiceActivationState = 0;
        this.mDataActivationState = 0;
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.SimActivationTracker.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (SimActivationTracker.VDBG) {
                    SimActivationTracker simActivationTracker = SimActivationTracker.this;
                    simActivationTracker.log("action: " + action);
                }
                if ("android.intent.action.SIM_STATE_CHANGED".equals(action) && IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT.equals(intent.getStringExtra(IccCardConstantsEx.INTENT_KEY_ICC_STATE))) {
                    SimActivationTracker.this.log("onSimAbsent, reset activation state to UNKNOWN");
                    SimActivationTracker.this.setVoiceActivationState(0);
                    SimActivationTracker.this.setDataActivationState(0);
                }
            }
        };
        this.mPhone.getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
    }

    public void setVoiceActivationState(int state) {
        if (!isValidActivationState(state) || 4 == state) {
            throw new IllegalArgumentException("invalid voice activation state: " + state);
        }
        log("setVoiceActivationState=" + state);
        this.mVoiceActivationState = state;
        this.mVoiceActivationStateLog.log(toString(state));
        this.mPhone.notifyVoiceActivationStateChanged(state);
    }

    public void setDataActivationState(int state) {
        if (isValidActivationState(state)) {
            log("setDataActivationState=" + state);
            this.mDataActivationState = state;
            this.mDataActivationStateLog.log(toString(state));
            this.mPhone.notifyDataActivationStateChanged(state);
            return;
        }
        throw new IllegalArgumentException("invalid data activation state: " + state);
    }

    public int getVoiceActivationState() {
        return this.mVoiceActivationState;
    }

    public int getDataActivationState() {
        return this.mDataActivationState;
    }

    private static boolean isValidActivationState(int state) {
        if (state == 0 || state == 1 || state == 2 || state == 3 || state == 4) {
            return true;
        }
        return false;
    }

    private static String toString(int state) {
        if (state == 0) {
            return "unknown";
        }
        if (state == 1) {
            return "activating";
        }
        if (state == 2) {
            return "activated";
        }
        if (state == 3) {
            return "deactivated";
        }
        if (state != 4) {
            return "invalid";
        }
        return "restricted";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        pw.println(" mVoiceActivationState Log:");
        ipw.increaseIndent();
        this.mVoiceActivationStateLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        pw.println(" mDataActivationState Log:");
        ipw.increaseIndent();
        this.mDataActivationStateLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
    }

    public void dispose() {
        this.mPhone.getContext().unregisterReceiver(this.mReceiver);
    }
}
