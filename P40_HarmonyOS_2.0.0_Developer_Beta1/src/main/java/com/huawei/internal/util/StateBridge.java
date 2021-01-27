package com.huawei.internal.util;

import android.os.Message;
import com.android.internal.util.State;

public class StateBridge extends State {
    private StateEx mStateEx;

    protected StateBridge() {
    }

    /* access modifiers changed from: package-private */
    public void setStateEx(StateEx stateEx) {
        this.mStateEx = stateEx;
    }

    /* access modifiers changed from: protected */
    public StateEx getStateEx() {
        return this.mStateEx;
    }

    @Override // com.android.internal.util.State, com.android.internal.util.IState
    public void enter() {
        StateEx stateEx = this.mStateEx;
        if (stateEx != null) {
            stateEx.enter();
        }
    }

    @Override // com.android.internal.util.State, com.android.internal.util.IState
    public void exit() {
        StateEx stateEx = this.mStateEx;
        if (stateEx != null) {
            stateEx.exit();
        }
    }

    @Override // com.android.internal.util.State, com.android.internal.util.IState
    public boolean processMessage(Message msg) {
        StateEx stateEx = this.mStateEx;
        if (stateEx != null) {
            return stateEx.processMessage(msg);
        }
        return false;
    }
}
