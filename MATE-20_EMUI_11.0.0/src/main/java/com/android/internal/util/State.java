package com.android.internal.util;

import android.annotation.UnsupportedAppUsage;
import android.os.Message;

public class State implements IState {
    @UnsupportedAppUsage
    protected State() {
    }

    @Override // com.android.internal.util.IState
    @UnsupportedAppUsage
    public void enter() {
    }

    @Override // com.android.internal.util.IState
    @UnsupportedAppUsage
    public void exit() {
    }

    @Override // com.android.internal.util.IState
    @UnsupportedAppUsage
    public boolean processMessage(Message msg) {
        return false;
    }

    @Override // com.android.internal.util.IState
    @UnsupportedAppUsage
    public String getName() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf(36) + 1);
    }
}
