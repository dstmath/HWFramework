package android.support.v4.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;
import android.support.annotation.NonNull;

public final class AlarmManagerCompat {
    public static void setAlarmClock(@NonNull AlarmManager alarmManager, long triggerTime, @NonNull PendingIntent showIntent, @NonNull PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 21) {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerTime, showIntent), operation);
        } else {
            setExact(alarmManager, 0, triggerTime, operation);
        }
    }

    public static void setAndAllowWhileIdle(@NonNull AlarmManager alarmManager, int type, long triggerAtMillis, @NonNull PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setAndAllowWhileIdle(type, triggerAtMillis, operation);
        } else {
            alarmManager.set(type, triggerAtMillis, operation);
        }
    }

    public static void setExact(@NonNull AlarmManager alarmManager, int type, long triggerAtMillis, @NonNull PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(type, triggerAtMillis, operation);
        } else {
            alarmManager.set(type, triggerAtMillis, operation);
        }
    }

    public static void setExactAndAllowWhileIdle(@NonNull AlarmManager alarmManager, int type, long triggerAtMillis, @NonNull PendingIntent operation) {
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, operation);
        } else {
            setExact(alarmManager, type, triggerAtMillis, operation);
        }
    }

    private AlarmManagerCompat() {
    }
}
