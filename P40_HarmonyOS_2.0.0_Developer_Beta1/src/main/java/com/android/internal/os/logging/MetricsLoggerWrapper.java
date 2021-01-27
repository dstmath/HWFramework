package com.android.internal.os.logging;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Pair;
import android.util.StatsLog;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;

public class MetricsLoggerWrapper {
    private static final int METRIC_VALUE_DISMISSED_BY_DRAG = 1;
    private static final int METRIC_VALUE_DISMISSED_BY_TAP = 0;

    public static void logPictureInPictureDismissByTap(Context context, Pair<ComponentName, Integer> topActivityInfo) {
        MetricsLogger.action(context, 822, 0);
        StatsLog.write(52, getUid(context, topActivityInfo.first, topActivityInfo.second.intValue()), topActivityInfo.first.flattenToString(), 4);
    }

    public static void logPictureInPictureDismissByDrag(Context context, Pair<ComponentName, Integer> topActivityInfo) {
        MetricsLogger.action(context, 822, 1);
        StatsLog.write(52, getUid(context, topActivityInfo.first, topActivityInfo.second.intValue()), topActivityInfo.first.flattenToString(), 4);
    }

    public static void logPictureInPictureMinimize(Context context, boolean isMinimized, Pair<ComponentName, Integer> topActivityInfo) {
        MetricsLogger.action(context, 821, isMinimized);
        StatsLog.write(52, getUid(context, topActivityInfo.first, topActivityInfo.second.intValue()), topActivityInfo.first.flattenToString(), 3);
    }

    private static int getUid(Context context, ComponentName componentName, int userId) {
        if (componentName == null) {
            return -1;
        }
        try {
            return context.getPackageManager().getApplicationInfoAsUser(componentName.getPackageName(), 0, userId).uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static void logPictureInPictureMenuVisible(Context context, boolean menuStateFull) {
        MetricsLogger.visibility(context, 823, menuStateFull);
    }

    public static void logPictureInPictureEnter(Context context, int uid, String shortComponentName, boolean supportsEnterPipOnTaskSwitch) {
        MetricsLogger.action(context, (int) MetricsProto.MetricsEvent.ACTION_PICTURE_IN_PICTURE_ENTERED, supportsEnterPipOnTaskSwitch);
        StatsLog.write(52, uid, shortComponentName, 1);
    }

    public static void logPictureInPictureFullScreen(Context context, int uid, String shortComponentName) {
        MetricsLogger.action(context, (int) MetricsProto.MetricsEvent.ACTION_PICTURE_IN_PICTURE_EXPANDED_TO_FULLSCREEN);
        StatsLog.write(52, uid, shortComponentName, 2);
    }

    public static void logAppOverlayEnter(int uid, String packageName, boolean changed, int type, boolean usingAlertWindow) {
        if (!changed) {
            return;
        }
        if (type != 2038) {
            StatsLog.write(59, uid, packageName, true, 1);
        } else if (!usingAlertWindow) {
            StatsLog.write(59, uid, packageName, false, 1);
        }
    }

    public static void logAppOverlayExit(int uid, String packageName, boolean changed, int type, boolean usingAlertWindow) {
        if (!changed) {
            return;
        }
        if (type != 2038) {
            StatsLog.write(59, uid, packageName, true, 2);
        } else if (!usingAlertWindow) {
            StatsLog.write(59, uid, packageName, false, 2);
        }
    }
}
