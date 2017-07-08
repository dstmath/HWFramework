package com.android.server.vr;

import android.content.ComponentName;

public abstract class VrManagerInternal {
    public static final int NO_ERROR = 0;

    public abstract int hasVrPackage(ComponentName componentName, int i);

    public abstract boolean isCurrentVrListener(String str, int i);

    public abstract void setVrMode(boolean z, ComponentName componentName, int i, ComponentName componentName2);

    public abstract void setVrModeImmediate(boolean z, ComponentName componentName, int i, ComponentName componentName2);
}
