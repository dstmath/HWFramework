package com.android.server.pg;

import android.os.WorkSource;

public abstract class PGManagerInternal {
    public abstract void noteChangeWakeLock(String str, WorkSource workSource, String str2, int i, String str3, WorkSource workSource2, String str4, int i2);

    public abstract void noteStartWakeLock(String str, WorkSource workSource, String str2, int i);

    public abstract void noteStopWakeLock(String str, WorkSource workSource, String str2, int i);
}
