package com.android.systemui.shared.system;

import com.android.internal.logging.MetricsLogger;

public class MetricsLoggerCompat {
    public static final int OVERVIEW_ACTIVITY = 224;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();

    public void action(int category) {
        this.mMetricsLogger.action(category);
    }

    public void action(int category, int value) {
        this.mMetricsLogger.action(category, value);
    }

    public void visible(int category) {
        this.mMetricsLogger.visible(category);
    }

    public void hidden(int category) {
        this.mMetricsLogger.hidden(category);
    }

    public void visibility(int category, boolean visible) {
        this.mMetricsLogger.visibility(category, visible);
    }
}
