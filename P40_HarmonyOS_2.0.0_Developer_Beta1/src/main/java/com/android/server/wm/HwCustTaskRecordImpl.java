package com.android.server.wm;

import android.content.ComponentName;
import android.os.SystemProperties;
import android.util.Slog;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HwCustTaskRecordImpl extends HwCustTaskRecord {
    private static final Set<String> FULL_SCREEN_APP_LIST = new HashSet(Arrays.asList("com.tencent.mm"));
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final String TAG = "HwCustTaskRecord";

    /* access modifiers changed from: package-private */
    public boolean isForcedToFullScreen(ComponentName activity, int forcedOrientation, int parentOrientation) {
        if (!IS_TV || activity == null || !FULL_SCREEN_APP_LIST.contains(activity.getPackageName())) {
            return false;
        }
        Slog.i(TAG, activity.getPackageName() + "force to full screen on tv. forcedOrientation" + forcedOrientation + "parentOrientation" + parentOrientation);
        return true;
    }
}
