package com.android.server.GcmFixer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.format.DateFormat;
import android.util.Slog;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;

public class GcmHeartBeatFixer {
    private static final boolean FEATURE_GCM_FIXER = false;
    private static final int INTERNAL_POWERGENIE = 300000;
    private static final int INTERNAL_WINDOW = 60000;
    private static final int INTERVAL_MOBILE_HB = 0;
    private static final int INTERVAL_WIFI_HB = 0;
    private static final String TAG = "GcmHeartBeatFixer";
    private static NetworkState sNetworkState;

    private enum NetworkState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.GcmFixer.GcmHeartBeatFixer.NetworkState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.GcmFixer.GcmHeartBeatFixer.NetworkState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.GcmFixer.GcmHeartBeatFixer.NetworkState.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.GcmFixer.GcmHeartBeatFixer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.GcmFixer.GcmHeartBeatFixer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.GcmFixer.GcmHeartBeatFixer.<clinit>():void");
    }

    public GcmHeartBeatFixer() {
    }

    public static void scheduleHeartbeatRequest(Context context, boolean fromNetworkStateChange, boolean adjust) {
        if (!FEATURE_GCM_FIXER) {
            Slog.i(TAG, "Gcm fixer is close...");
        } else if (isChinaMarket()) {
            Slog.i(TAG, "China market version, just return...");
        } else {
            Slog.i(TAG, "scheduleHeartbeatRequest, fromNetworkStateChange: " + fromNetworkStateChange);
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
                if (!(fromNetworkStateChange && sNetworkState == NetworkState.DISCONNECTED)) {
                    sNetworkState = NetworkState.DISCONNECTED;
                    cancelHeartbeatRequest(context);
                }
            } else if (!(fromNetworkStateChange && sNetworkState == NetworkState.CONNECTED)) {
                int intervalMillis;
                sNetworkState = NetworkState.CONNECTED;
                if (activeNetworkInfo.getType() == 1) {
                    intervalMillis = INTERVAL_WIFI_HB;
                } else {
                    intervalMillis = INTERVAL_MOBILE_HB;
                }
                setNextHeartbeatRequest(context, intervalMillis, adjust);
            }
        }
    }

    private static void setNextHeartbeatRequest(Context context, int intervalMillis, boolean adjust) {
        long triggerAtMillis;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        long currentTime = System.currentTimeMillis();
        if (adjust && intervalMillis == INTERNAL_POWERGENIE) {
            long elapsedTime = SystemClock.elapsedRealtime();
            long t_elapsedTime = ((elapsedTime / ((long) intervalMillis)) + 1) * ((long) intervalMillis);
            long diff = t_elapsedTime - elapsedTime;
            triggerAtMillis = currentTime + diff;
            Slog.i(TAG, "currentTime: " + currentTime + " elapsedTime:" + elapsedTime + " t_elapsedTime:" + t_elapsedTime + " triggerAtMillis:" + triggerAtMillis + " diff:" + diff);
        } else {
            triggerAtMillis = currentTime + ((long) intervalMillis);
        }
        Slog.i(TAG, "setNextHeartbeatRequest at: " + DateFormat.format("yyyy-MM-dd hh:mm:ss", triggerAtMillis));
        PendingIntent broadcastPendingIntent = getBroadcastPendingIntent(context);
        if (VERSION.SDK_INT >= 19) {
            alarmManager.setWindow(INTERVAL_WIFI_HB, triggerAtMillis, AppHibernateCst.DELAY_ONE_MINS, broadcastPendingIntent);
        } else {
            alarmManager.set(INTERVAL_WIFI_HB, triggerAtMillis, broadcastPendingIntent);
        }
    }

    private static void cancelHeartbeatRequest(Context context) {
        Slog.d(TAG, "cancelHeartbeatRequest...");
        ((AlarmManager) context.getSystemService("alarm")).cancel(getBroadcastPendingIntent(context));
    }

    private static PendingIntent getBroadcastPendingIntent(Context context) {
        return PendingIntent.getBroadcast(context, INTERVAL_WIFI_HB, new Intent(HeartbeatReceiver.HEARTBEAT_FIXER_ACTION), INTERVAL_WIFI_HB);
    }

    public static boolean isChinaMarket() {
        if (SystemProperties.getInt("ro.config.hw_optb", INTERVAL_WIFI_HB) == 156) {
            return true;
        }
        return FEATURE_GCM_FIXER;
    }
}
