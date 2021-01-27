package com.android.systemui.shared.plugins;

import android.content.ComponentName;

public interface PluginEnabler {
    public static final int DISABLED_FROM_EXPLICIT_CRASH = 2;
    public static final int DISABLED_FROM_SYSTEM_CRASH = 3;
    public static final int DISABLED_INVALID_VERSION = 1;
    public static final int DISABLED_MANUALLY = 1;
    public static final int ENABLED = 0;

    public @interface DisableReason {
    }

    @DisableReason
    int getDisableReason(ComponentName componentName);

    boolean isEnabled(ComponentName componentName);

    void setDisabled(ComponentName componentName, @DisableReason int i);

    void setEnabled(ComponentName componentName);
}
