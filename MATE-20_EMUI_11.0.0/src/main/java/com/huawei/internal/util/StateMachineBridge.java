package com.huawei.internal.util;

import android.os.Looper;
import android.os.Message;
import com.android.internal.util.StateMachine;

public class StateMachineBridge extends StateMachine {
    private StateMachineEx mStateMachineEx;

    StateMachineBridge(String name) {
        super(name);
    }

    StateMachineBridge(String name, Looper looper) {
        super(name, looper);
    }

    /* access modifiers changed from: package-private */
    public void setStateMachineEx(StateMachineEx stateMachineEx) {
        this.mStateMachineEx = stateMachineEx;
    }

    /* access modifiers changed from: protected */
    public final boolean hasMessagesHw(int what) {
        return hasMessages(what);
    }

    /* access modifiers changed from: protected */
    public final void removeMessagesHw(int what) {
        removeMessages(what);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.util.StateMachine
    public void unhandledMessage(Message msg) {
        this.mStateMachineEx.unhandledMessage(msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.util.StateMachine
    public void logd(String s) {
        this.mStateMachineEx.logd(s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.util.StateMachine
    public void logi(String s) {
        this.mStateMachineEx.logi(s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.util.StateMachine
    public void loge(String s) {
        this.mStateMachineEx.loge(s);
    }
}
