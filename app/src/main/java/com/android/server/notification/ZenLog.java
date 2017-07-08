package com.android.server.notification;

import android.content.ComponentName;
import android.net.Uri;
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
    private static final boolean DEBUG = false;
    private static final SimpleDateFormat FORMAT = null;
    private static final String[] MSGS = null;
    private static final int SIZE = 0;
    private static final String TAG = "ZenLog";
    private static final long[] TIMES = null;
    private static final int[] TYPES = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.ZenLog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.ZenLog.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ZenLog.<clinit>():void");
    }

    public static void traceIntercepted(NotificationRecord record, String reason) {
        if (record == null || !record.isIntercepted()) {
            append(TYPE_INTERCEPTED, record.getKey() + "," + reason);
        }
    }

    public static void traceNotIntercepted(NotificationRecord record, String reason) {
        if (record == null || !record.isUpdate) {
            append(TYPE_NOT_INTERCEPTED, record.getKey() + "," + reason);
        }
    }

    public static void traceSetRingerModeExternal(int ringerModeOld, int ringerModeNew, String caller, int ringerModeInternalIn, int ringerModeInternalOut) {
        append(TYPE_SET_RINGER_MODE_EXTERNAL, caller + ",e:" + ringerModeToString(ringerModeOld) + "->" + ringerModeToString(ringerModeNew) + ",i:" + ringerModeToString(ringerModeInternalIn) + "->" + ringerModeToString(ringerModeInternalOut));
    }

    public static void traceSetRingerModeInternal(int ringerModeOld, int ringerModeNew, String caller, int ringerModeExternalIn, int ringerModeExternalOut) {
        append(TYPE_SET_RINGER_MODE_INTERNAL, caller + ",i:" + ringerModeToString(ringerModeOld) + "->" + ringerModeToString(ringerModeNew) + ",e:" + ringerModeToString(ringerModeExternalIn) + "->" + ringerModeToString(ringerModeExternalOut));
    }

    public static void traceDowntimeAutotrigger(String result) {
        append(TYPE_DOWNTIME, result);
    }

    public static void traceSetZenMode(int zenMode, String reason) {
        append(TYPE_SET_ZEN_MODE, zenModeToString(zenMode) + "," + reason);
    }

    public static void traceUpdateZenMode(int fromMode, int toMode) {
        append(TYPE_UPDATE_ZEN_MODE, zenModeToString(fromMode) + " -> " + zenModeToString(toMode));
    }

    public static void traceExitCondition(Condition c, ComponentName component, String reason) {
        append(TYPE_EXIT_CONDITION, c + "," + componentToString(component) + "," + reason);
    }

    public static void traceSubscribe(Uri uri, IConditionProvider provider, RemoteException e) {
        append(TYPE_SUBSCRIBE, uri + "," + subscribeResult(provider, e));
    }

    public static void traceUnsubscribe(Uri uri, IConditionProvider provider, RemoteException e) {
        append(TYPE_UNSUBSCRIBE, uri + "," + subscribeResult(provider, e));
    }

    public static void traceConfig(String reason, ZenModeConfig oldConfig, ZenModeConfig newConfig) {
        String str = null;
        StringBuilder append = new StringBuilder().append(reason).append(",");
        if (newConfig != null) {
            str = newConfig.toString();
        }
        append(TYPE_CONFIG, append.append(str).append(",").append(ZenModeConfig.diff(oldConfig, newConfig)).toString());
    }

    public static void traceDisableEffects(NotificationRecord record, String reason) {
        append(TYPE_DISABLE_EFFECTS, record.getKey() + "," + reason);
    }

    public static void traceEffectsSuppressorChanged(List<ComponentName> oldSuppressors, List<ComponentName> newSuppressors, long suppressedEffects) {
        append(TYPE_SUPPRESSOR_CHANGED, "suppressed effects:" + suppressedEffects + "," + componentListToString(oldSuppressors) + "->" + componentListToString(newSuppressors));
    }

    public static void traceListenerHintsChanged(int oldHints, int newHints, int listenerCount) {
        append(TYPE_LISTENER_HINTS_CHANGED, hintsToString(oldHints) + "->" + hintsToString(newHints) + ",listeners=" + listenerCount);
    }

    private static String subscribeResult(IConditionProvider provider, RemoteException e) {
        if (provider == null) {
            return "no provider";
        }
        return e != null ? e.getMessage() : "ok";
    }

    private static String typeToString(int type) {
        switch (type) {
            case TYPE_INTERCEPTED /*1*/:
                return "intercepted";
            case TYPE_ALLOW_DISABLE /*2*/:
                return "allow_disable";
            case TYPE_SET_RINGER_MODE_EXTERNAL /*3*/:
                return "set_ringer_mode_external";
            case TYPE_SET_RINGER_MODE_INTERNAL /*4*/:
                return "set_ringer_mode_internal";
            case TYPE_DOWNTIME /*5*/:
                return "downtime";
            case TYPE_SET_ZEN_MODE /*6*/:
                return "set_zen_mode";
            case TYPE_UPDATE_ZEN_MODE /*7*/:
                return "update_zen_mode";
            case TYPE_EXIT_CONDITION /*8*/:
                return "exit_condition";
            case TYPE_SUBSCRIBE /*9*/:
                return "subscribe";
            case TYPE_UNSUBSCRIBE /*10*/:
                return "unsubscribe";
            case TYPE_CONFIG /*11*/:
                return "config";
            case TYPE_NOT_INTERCEPTED /*12*/:
                return "not_intercepted";
            case TYPE_DISABLE_EFFECTS /*13*/:
                return "disable_effects";
            case TYPE_SUPPRESSOR_CHANGED /*14*/:
                return "suppressor_changed";
            case TYPE_LISTENER_HINTS_CHANGED /*15*/:
                return "listener_hints_changed";
            default:
                return "unknown";
        }
    }

    private static String ringerModeToString(int ringerMode) {
        switch (ringerMode) {
            case SIZE /*0*/:
                return "silent";
            case TYPE_INTERCEPTED /*1*/:
                return "vibrate";
            case TYPE_ALLOW_DISABLE /*2*/:
                return "normal";
            default:
                return "unknown";
        }
    }

    private static String zenModeToString(int zenMode) {
        switch (zenMode) {
            case SIZE /*0*/:
                return "off";
            case TYPE_INTERCEPTED /*1*/:
                return "important_interruptions";
            case TYPE_ALLOW_DISABLE /*2*/:
                return "no_interruptions";
            case TYPE_SET_RINGER_MODE_EXTERNAL /*3*/:
                return "alarms";
            default:
                return "unknown";
        }
    }

    private static String hintsToString(int hints) {
        switch (hints) {
            case SIZE /*0*/:
                return "none";
            case TYPE_INTERCEPTED /*1*/:
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
        for (int i = SIZE; i < components.size(); i += TYPE_INTERCEPTED) {
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
            sNext = (sNext + TYPE_INTERCEPTED) % SIZE;
            if (sSize < SIZE) {
                sSize += TYPE_INTERCEPTED;
            }
        }
        if (DEBUG) {
            Slog.d(TAG, typeToString(type) + ": " + msg);
        }
    }

    public static void dump(PrintWriter pw, String prefix) {
        synchronized (MSGS) {
            int start = ((sNext - sSize) + SIZE) % SIZE;
            for (int i = SIZE; i < sSize; i += TYPE_INTERCEPTED) {
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
