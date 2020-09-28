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

    @Override // com.android.internal.util.IState, com.android.internal.util.State
    public void enter() {
        StateEx stateEx = this.mStateEx;
        if (stateEx != null) {
            stateEx.enter();
        }
    }

    @Override // com.android.internal.util.IState, com.android.internal.util.State
    public void exit() {
        StateEx stateEx = this.mStateEx;
        if (stateEx != null) {
            stateEx.exit();
        }
    }

    @Override // com.android.internal.util.IState, com.android.internal.util.State
    public boolean processMessage(Message msg) {
        StateEx stateEx = this.mStateEx;
        if (stateEx != null) {
            return stateEx.processMessage(msg);
        }
        return false;
    }
}
