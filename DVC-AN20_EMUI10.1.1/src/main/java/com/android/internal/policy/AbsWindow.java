package com.android.internal.policy;

import android.content.Context;
import android.os.IBinder;
import android.view.Window;

public abstract class AbsWindow extends Window {
    public AbsWindow(Context context) {
        super(context);
    }

    public void onWindowStateChanged(int windowState) {
    }

    public void setAppToken(IBinder token) {
    }

    public IBinder getAppToken() {
        return null;
    }
}
