package com.android.commands.monkey;

import android.app.IActivityManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.IWindowManager;

public class MonkeyInstrumentationEvent extends MonkeyEvent {
    String mRunnerName;
    String mTestCaseName;

    public MonkeyInstrumentationEvent(String testCaseName, String runnerName) {
        super(4);
        this.mTestCaseName = testCaseName;
        this.mRunnerName = runnerName;
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        ComponentName cn = ComponentName.unflattenFromString(this.mRunnerName);
        if (cn == null || this.mTestCaseName == null) {
            throw new IllegalArgumentException("Bad component name");
        }
        Bundle args = new Bundle();
        args.putString("class", this.mTestCaseName);
        try {
            iam.startInstrumentation(cn, null, 0, args, null, null, 0, null);
            return 1;
        } catch (RemoteException e) {
            Logger.err.println("** Failed talking with activity manager!");
            return -1;
        }
    }
}
