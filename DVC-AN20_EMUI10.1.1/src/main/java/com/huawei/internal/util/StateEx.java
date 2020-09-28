package com.huawei.internal.util;

import android.os.Message;
import com.android.internal.util.State;

public class StateEx {
    private StateBridge mState = new StateBridge();

    public StateEx() {
        this.mState.setStateEx(this);
    }

    public void enter() {
    }

    public void exit() {
    }

    public boolean processMessage(Message msg) {
        return false;
    }

    public String getName() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf(36) + 1);
    }

    public State getState() {
        return this.mState;
    }
}
