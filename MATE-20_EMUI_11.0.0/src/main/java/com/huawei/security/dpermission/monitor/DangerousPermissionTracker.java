package com.huawei.security.dpermission.monitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.huawei.security.dpermission.DPermissionInitializer;
import java.security.SecureRandom;
import ohos.event.notification.NotificationRequest;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DangerousPermissionTracker {
    private static final int DAYS_OF_ONE_WEEK = 7;
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "DangerousPermissionTracker");
    private static final int FOREGROUND_HOUR = 8;
    private static final long MILLISECONDS_OF_ONE_DAY = 86400000;
    private static final long MILLISECONDS_OF_ONE_WEEK = 604800000;
    private static final int REPORT_NUM = 10;

    private DangerousPermissionTracker() {
    }

    public static void startTracker(Context context) {
        if (context == null) {
            HiLog.error(DPERMISSION_LABEL, "get null context.", new Object[0]);
            return;
        }
        Object systemService = context.getSystemService(NotificationRequest.CLASSIFICATION_ALARM);
        AlarmManager alarmManager = null;
        if (systemService instanceof AlarmManager) {
            alarmManager = (AlarmManager) systemService;
        }
        if (alarmManager == null) {
            HiLog.error(DPERMISSION_LABEL, "get null alarm manager.", new Object[0]);
            return;
        }
        Intent intent = new Intent(DangerousPermissionTrackerReceiver.ACTION_UPDATE_STATS);
        intent.putExtra(DangerousPermissionTrackerReceiver.INTENT_SET_HOUR, 8);
        intent.setPackage(context.getPackageName());
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 268435456);
        long nextInt = ((long) new SecureRandom().nextInt(7)) * 86400000;
        alarmManager.setRepeating(0, 86400000 + nextInt, MILLISECONDS_OF_ONE_WEEK, broadcast);
        HiLog.info(DPERMISSION_LABEL, "start update alarm, next alarm after %{public}d milliseconds.", new Object[]{Long.valueOf(nextInt)});
        Intent intent2 = new Intent(DangerousPermissionTrackerReceiver.ACTION_REPORT_STATS);
        intent2.putExtra(DangerousPermissionTrackerReceiver.INTENT_SET_REPORT_NUM, 10);
        intent2.setPackage(context.getPackageName());
        alarmManager.setRepeating(0, 86400000, 86400000, PendingIntent.getBroadcast(context, 0, intent2, 268435456));
        HiLog.info(DPERMISSION_LABEL, "start report alarm.", new Object[0]);
    }
}
