package com.android.server.vr;

import android.os.SystemProperties;
import android.util.Slog;

public final class HwVrManagerServiceEx implements IHwVrManagerServiceEx {
    private static final String TAG = "HwVrManagerServiceEx";

    public void setHwEnviroment(boolean enabled) {
        Slog.i(TAG, "setHwEnviroment " + enabled);
        SystemProperties.set("persist.sys.ui.hw", enabled ? "true" : "false");
    }
}
