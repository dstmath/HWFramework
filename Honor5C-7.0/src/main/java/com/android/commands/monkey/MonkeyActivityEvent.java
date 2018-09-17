package com.android.commands.monkey;

import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.IWindowManager;

public class MonkeyActivityEvent extends MonkeyEvent {
    long mAlarmTime;
    private ComponentName mApp;

    public MonkeyActivityEvent(ComponentName app) {
        super(4);
        this.mAlarmTime = 0;
        this.mApp = app;
    }

    public MonkeyActivityEvent(ComponentName app, long arg) {
        super(4);
        this.mAlarmTime = 0;
        this.mApp = app;
        this.mAlarmTime = arg;
    }

    private Intent getEvent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        if (this.mApp != null && this.mApp.getPackageName().equals("com.huawei.android.launcher") && (this.mApp.getClassName().equals("com.huawei.android.launcher.drawer.DrawerLauncher") || this.mApp.getClassName().equals("com.huawei.android.launcher.unihome.UniHomeLauncher") || this.mApp.getClassName().equals("com.huawei.android.launcher.simpleui.SimpleUILauncher"))) {
            intent.addCategory("android.intent.category.HOME");
            intent.setPackage("com.huawei.android.launcher");
        } else {
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setComponent(this.mApp);
            intent.addFlags(270532608);
        }
        return intent;
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        Intent intent = getEvent();
        if (verbose > 0) {
            System.out.println(":Switch: " + intent.toUri(0));
        }
        if (this.mAlarmTime != 0) {
            Bundle args = new Bundle();
            args.putLong("alarmTime", this.mAlarmTime);
            intent.putExtras(args);
        }
        try {
            iam.startActivity(null, null, intent, null, null, null, 0, 0, null, null);
            return 1;
        } catch (RemoteException e) {
            System.err.println("** Failed talking with activity manager!");
            return -1;
        } catch (SecurityException e2) {
            if (verbose > 0) {
                System.out.println("** Permissions error starting activity " + intent.toUri(0));
            }
            return -2;
        }
    }
}
