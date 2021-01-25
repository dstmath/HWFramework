package com.android.commands.monkey;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.IWindowManager;

public class MonkeyActivityEvent extends MonkeyEvent {
    long mAlarmTime = 0;
    private ComponentName mApp;

    public MonkeyActivityEvent(ComponentName app) {
        super(4);
        this.mApp = app;
    }

    public MonkeyActivityEvent(ComponentName app, long arg) {
        super(4);
        this.mApp = app;
        this.mAlarmTime = arg;
    }

    private Intent getEvent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setComponent(this.mApp);
        intent.addFlags(270532608);
        return intent;
    }

    @Override // com.android.commands.monkey.MonkeyEvent
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        Intent intent = getEvent();
        if (verbose > 0) {
            Logger logger = Logger.out;
            logger.println(":Switch: " + intent.toUri(0));
        }
        if (this.mAlarmTime != 0) {
            Bundle args = new Bundle();
            args.putLong("alarmTime", this.mAlarmTime);
            intent.putExtras(args);
        }
        try {
            iam.startActivityAsUser((IApplicationThread) null, (String) null, intent, (String) null, (IBinder) null, (String) null, 0, 0, (ProfilerInfo) null, (Bundle) null, ActivityManager.getCurrentUser());
            return 1;
        } catch (RemoteException e) {
            Logger.err.println("** Failed talking with activity manager!");
            return -1;
        } catch (SecurityException e2) {
            if (verbose <= 0) {
                return -2;
            }
            Logger logger2 = Logger.out;
            logger2.println("** Permissions error starting activity " + intent.toUri(0));
            return -2;
        }
    }
}
