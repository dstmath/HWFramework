package com.android.server.pg;

import android.content.ComponentName;
import android.os.WorkSource;
import java.util.Collection;

public abstract class PGManagerInternal {
    public abstract boolean isGmsWakeLockFilterTag(int i, String str, WorkSource workSource);

    public abstract boolean isServiceMatchList(ComponentName componentName, String str, Collection<String> collection);

    public abstract boolean isServiceProxy(ComponentName componentName, String str);

    public abstract boolean isServiceProxySelf(String str);

    public abstract void noteChangeWakeLock(String str, WorkSource workSource, String str2, int i, String str3, WorkSource workSource2, String str4, int i2);

    public abstract void noteStartWakeLock(String str, WorkSource workSource, String str2, int i);

    public abstract void noteStopWakeLock(String str, WorkSource workSource, String str2, int i);

    public abstract void notifyWakelock(int i, String str, int i2, int i3, WorkSource workSource, int i4);

    public abstract void notifyWakelock(int i, String str, int i2, int i3, WorkSource workSource, WorkSource workSource2);
}
