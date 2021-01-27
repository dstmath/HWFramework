package com.android.server.vr;

import android.app.Vr2dDisplayProperties;
import android.content.ComponentName;
import android.service.vr.IPersistentVrStateCallbacks;

public abstract class VrManagerInternal {
    public static final int NO_ERROR = 0;

    public abstract void addPersistentVrModeStateListener(IPersistentVrStateCallbacks iPersistentVrStateCallbacks);

    public abstract int getVr2dDisplayId();

    public abstract int hasVrPackage(ComponentName componentName, int i);

    public abstract boolean isCurrentVrListener(String str, int i);

    public abstract void onScreenStateChanged(boolean z);

    public abstract void setPersistentVrModeEnabled(boolean z);

    public abstract void setVr2dDisplayProperties(Vr2dDisplayProperties vr2dDisplayProperties);

    public abstract void setVrMode(boolean z, ComponentName componentName, int i, int i2, ComponentName componentName2);
}
