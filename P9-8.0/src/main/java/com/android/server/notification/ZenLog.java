package com.android.server.notification;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.ZenModeConfig;
import android.util.Slog;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ZenLog {
    private static final boolean DEBUG = Build.IS_DEBUGGABLE;
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    private static final String[] MSGS = new String[SIZE];
    private static final int SIZE = (Build.IS_DEBUGGABLE ? 100 : 20);
    private static final String TAG = "ZenLog";
    private static final long[] TIMES = new long[SIZE];
    private static final int[] TYPES = new int[SIZE];
    private static final int TYPE_ALLOW_DISABLE = 2;
    private static final int TYPE_CONFIG = 11;
    private static final int TYPE_DISABLE_EFFECTS = 13;
    private static final int TYPE_DOWNTIME = 5;
    private static final int TYPE_EXIT_CONDITION = 8;
    private static final int TYPE_INTERCEPTED = 1;
    private static final int TYPE_LISTENER_HINTS_CHANGED = 15;
    private static final int TYPE_NOT_INTERCEPTED = 12;
    private static final int TYPE_SET_RINGER_MODE_EXTERNAL = 3;
    private static final int TYPE_SET_RINGER_MODE_INTERNAL = 4;
    private static final int TYPE_SET_ZEN_MODE = 6;
    private static final int TYPE_SUBSCRIBE = 9;
    private static final int TYPE_SUPPRESSOR_CHANGED = 14;
    private static final int TYPE_UNSUBSCRIBE = 10;
    private static final int TYPE_UPDATE_ZEN_MODE = 7;
    private static int sNext;
    private static int sSize;

    public static void traceIntercepted(NotificationRecord record, String reason) {
        if (record == null || !record.isIntercepted()) {
            append(1, record.getKey() + "," + reason);
        }
    }

    public static void traceNotIntercepted(NotificationRecord record, String reason) {
        if (record == null || !record.isUpdate) {
            append(12, record.getKey() + "," + reason);
        }
    }

    public static void traceSetRingerModeExternal(int ringerModeOld, int ringerModeNew, String caller, int ringerModeInternalIn, int ringerModeInternalOut) {
        append(3, caller + ",e:" + ringerModeToString(ringerModeOld) + "->" + ringerModeToString(ringerModeNew) + ",i:" + ringerModeToString(ringerModeInternalIn) + "->" + ringerModeToString(ringerModeInternalOut));
    }

    public static void traceSetRingerModeInternal(int ringerModeOld, int ringerModeNew, String caller, int ringerModeExternalIn, int ringerModeExternalOut) {
        append(4, caller + ",i:" + ringerModeToString(ringerModeOld) + "->" + ringerModeToString(ringerModeNew) + ",e:" + ringerModeToString(ringerModeExternalIn) + "->" + ringerModeToString(ringerModeExternalOut));
    }

    public static void traceDowntimeAutotrigger(String result) {
        append(5, result);
    }

    public static void traceSetZenMode(int zenMode, String reason) {
        append(6, zenModeToString(zenMode) + "," + reason);
    }

    public static void traceUpdateZenMode(int fromMode, int toMode) {
        append(7, zenModeToString(fromMode) + " -> " + zenModeToString(toMode));
    }

    public static void traceExitCondition(Condition c, ComponentName component, String reason) {
        append(8, c + "," + componentToString(component) + "," + reason);
    }

    public static void traceSubscribe(Uri uri, IConditionProvider provider, RemoteException e) {
        append(9, uri + "," + subscribeResult(provider, e));
    }

    public static void traceUnsubscribe(Uri uri, IConditionProvider provider, RemoteException e) {
        append(10, uri + "," + subscribeResult(provider, e));
    }

    public static void traceConfig(String reason, ZenModeConfig oldConfig, ZenModeConfig newConfig) {
        String str = null;
        StringBuilder append = new StringBuilder().append(reason).append(",");
        if (newConfig != null) {
            str = newConfig.toString();
        }
        append(11, append.append(str).append(",").append(ZenModeConfig.diff(oldConfig, newConfig)).toString());
    }

    public static void traceDisableEffects(NotificationRecord record, String reason) {
        append(13, record.getKey() + "," + reason);
    }

    public static void traceEffectsSuppressorChanged(List<ComponentName> oldSuppressors, List<ComponentName> newSuppressors, long suppressedEffects) {
        append(14, "suppressed effects:" + suppressedEffects + "," + componentListToString(oldSuppressors) + "->" + componentListToString(newSuppressors));
    }

    public static void traceListenerHintsChanged(int oldHints, int newHints, int listenerCount) {
        append(15, hintsToString(oldHints) + "->" + hintsToString(newHints) + ",listeners=" + listenerCount);
    }

    private static String subscribeResult(IConditionProvider provider, RemoteException e) {
        if (provider == null) {
            return "no provider";
        }
        return e != null ? e.getMessage() : "ok";
    }

    private static String typeToString(int type) {
        switch (type) {
            case 1:
                return "intercepted";
            case 2:
                return "allow_disable";
            case 3:
                return "set_ringer_mode_external";
            case 4:
                return "set_ringer_mode_internal";
            case 5:
                return "downtime";
            case 6:
                return "set_zen_mode";
            case 7:
                return "update_zen_mode";
            case 8:
                return "exit_condition";
            case 9:
                return "subscribe";
            case 10:
                return "unsubscribe";
            case 11:
                return "config";
            case 12:
                return "not_intercepted";
            case 13:
                return "disable_effects";
            case 14:
                return "suppressor_changed";
            case 15:
                return "listener_hints_changed";
            default:
                return Shell.NIGHT_MODE_STR_UNKNOWN;
        }
    }

    private static String ringerModeToString(int ringerMode) {
        switch (ringerMode) {
            case 0:
                return "silent";
            case 1:
                return "vibrate";
            case 2:
                return "normal";
            default:
                return Shell.NIGHT_MODE_STR_UNKNOWN;
        }
    }

    private static String zenModeToString(int zenMode) {
        switch (zenMode) {
            case 0:
                return "off";
            case 1:
                return "important_interruptions";
            case 2:
                return "no_interruptions";
            case 3:
                return "alarms";
            default:
                return Shell.NIGHT_MODE_STR_UNKNOWN;
        }
    }

    private static String hintsToString(int hints) {
        switch (hints) {
            case 0:
                return "none";
            case 1:
                return "disable_effects";
            default:
                return Integer.toString(hints);
        }
    }

    private static String componentToString(ComponentName component) {
        return component != null ? component.toShortString() : null;
    }

    private static String componentListToString(List<ComponentName> components) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(componentToString((ComponentName) components.get(i)));
        }
        return stringBuilder.toString();
    }

    private static void append(int type, String msg) {
        synchronized (MSGS) {
            TIMES[sNext] = System.currentTimeMillis();
            TYPES[sNext] = type;
            MSGS[sNext] = msg;
            sNext = (sNext + 1) % SIZE;
            if (sSize < SIZE) {
                sSize++;
            }
        }
        if (DEBUG) {
            Slog.d(TAG, typeToString(type) + ": " + msg);
        }
    }

    public static void dump(PrintWriter pw, String prefix) {
        synchronized (MSGS) {
            int start = ((sNext - sSize) + SIZE) % SIZE;
            for (int i = 0; i < sSize; i++) {
                int j = (start + i) % SIZE;
                pw.print(prefix);
                pw.print(FORMAT.format(new Date(TIMES[j])));
                pw.print(' ');
                pw.print(typeToString(TYPES[j]));
                pw.print(": ");
                pw.println(MSGS[j]);
            }
        }
    }
}
