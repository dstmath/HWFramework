package android.app;

import android.app.IActivityManager;
import android.content.Intent;
import android.os.IBinder;

@Deprecated
public abstract class ActivityManagerNative {
    public static IActivityManager asInterface(IBinder obj) {
        return IActivityManager.Stub.asInterface(obj);
    }

    public static IActivityManager getDefault() {
        return ActivityManager.getService();
    }

    public static boolean isSystemReady() {
        return ActivityManager.isSystemReady();
    }

    public static void broadcastStickyIntent(Intent intent, String permission, int userId) {
        broadcastStickyIntent(intent, permission, -1, userId);
    }

    public static void broadcastStickyIntent(Intent intent, String permission, int appOp, int userId) {
        ActivityManager.broadcastStickyIntent(intent, appOp, userId);
    }

    public static void noteWakeupAlarm(PendingIntent ps, int sourceUid, String sourcePkg, String tag) {
        ActivityManager.noteWakeupAlarm(ps, null, sourceUid, sourcePkg, tag);
    }

    public static void noteAlarmStart(PendingIntent ps, int sourceUid, String tag) {
        ActivityManager.noteAlarmStart(ps, null, sourceUid, tag);
    }

    public static void noteAlarmFinish(PendingIntent ps, int sourceUid, String tag) {
        ActivityManager.noteAlarmFinish(ps, null, sourceUid, tag);
    }
}
