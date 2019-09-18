package com.android.systemui.shared.system;

import android.content.Context;
import com.android.internal.util.LatencyTracker;

public class LatencyTrackerCompat {
    public static boolean isEnabled(Context context) {
        return LatencyTracker.isEnabled(context);
    }

    public static void logToggleRecents(int duration) {
        LatencyTracker.logAction(1, duration);
    }
}
