package com.android.commands.monkey;

import android.app.IActivityManager;
import android.view.IWindowManager;

public class MonkeyNoopEvent extends MonkeyEvent {
    public MonkeyNoopEvent() {
        super(8);
    }

    @Override // com.android.commands.monkey.MonkeyEvent
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 1) {
            Logger.out.println("NOOP");
        }
        return 1;
    }
}
