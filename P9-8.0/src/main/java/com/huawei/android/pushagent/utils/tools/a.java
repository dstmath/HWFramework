package com.huawei.android.pushagent.utils.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import com.huawei.android.pushagent.utils.d.c;

public class a {
    private static String TAG = "PushLog2951";
    private static int fb = 19;

    private static void qj(Context context, long j, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null) {
            c.sh(TAG, "get AlarmManager error");
            return;
        }
        try {
            Object[] objArr = new Object[]{Integer.valueOf(0), Long.valueOf(j), pendingIntent};
            alarmManager.getClass().getDeclaredMethod("setExact", new Class[]{Integer.TYPE, Long.TYPE, PendingIntent.class}).invoke(alarmManager, objArr);
        } catch (Throwable e) {
            c.se(TAG, " setExact NoSuchMethodException " + e.toString(), e);
            alarmManager.set(0, j, pendingIntent);
        } catch (Throwable e2) {
            c.se(TAG, " setExact IllegalAccessException " + e2.toString(), e2);
            alarmManager.set(0, j, pendingIntent);
        } catch (Throwable e22) {
            c.se(TAG, " setExact InvocationTargetException " + e22.toString(), e22);
            alarmManager.set(0, j, pendingIntent);
        } catch (Throwable e222) {
            c.se(TAG, " setExact wrong " + e222.toString(), e222);
            alarmManager.set(0, j, pendingIntent);
        }
    }

    public static void qf(Context context, Intent intent, long j) {
        c.sg(TAG, "enter AlarmTools:setExactAlarm(intent:" + intent + " interval:" + j + "ms");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 134217728);
        if (VERSION.SDK_INT >= fb) {
            qj(context, System.currentTimeMillis() + j, broadcast);
        } else {
            alarmManager.set(0, System.currentTimeMillis() + j, broadcast);
        }
    }

    public static void qi(Context context, Intent intent, long j) {
        c.sg(TAG, "enter AlarmTools:setAlarm(intent:" + intent + " interval:" + j + "ms");
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 134217728);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager != null) {
            alarmManager.set(0, System.currentTimeMillis() + j, broadcast);
        } else {
            c.sf(TAG, "fail to setAlarm");
        }
    }

    public static void qk(Context context, Intent intent, long j) {
        c.sg(TAG, "enter AlarmTools:setDelayNotifyService(intent:" + intent + " interval:" + j + ")");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        PendingIntent service = PendingIntent.getService(context, 0, intent, 0);
        if (VERSION.SDK_INT >= fb) {
            qj(context, System.currentTimeMillis() + j, service);
        } else {
            alarmManager.set(0, System.currentTimeMillis() + j, service);
        }
    }

    public static void qg(Context context, String str) {
        c.sg(TAG, "enter cancelAlarm(Action=" + str);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        Intent intent = new Intent(str);
        intent.setPackage(context.getPackageName());
        alarmManager.cancel(PendingIntent.getBroadcast(context, 0, intent, 0));
    }

    public static void qh(Context context, Intent intent) {
        c.sg(TAG, "enter cancelAlarm(Intent=" + intent);
        ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, 0, intent, 0));
    }
}
