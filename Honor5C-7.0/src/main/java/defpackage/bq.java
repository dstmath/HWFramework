package defpackage;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;

/* renamed from: bq */
public class bq {
    private static String TAG;
    private static int cj;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: bq.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: bq.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: bq.<clinit>():void");
    }

    private static void a(Context context, long j, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null) {
            aw.i(TAG, "get AlarmManager error");
            return;
        }
        try {
            Object[] objArr = new Object[]{Integer.valueOf(0), Long.valueOf(j), pendingIntent};
            alarmManager.getClass().getDeclaredMethod("setExact", new Class[]{Integer.TYPE, Long.TYPE, PendingIntent.class}).invoke(alarmManager, objArr);
        } catch (Throwable e) {
            aw.d(TAG, " setExact NoSuchMethodException " + e.toString(), e);
            alarmManager.set(0, j, pendingIntent);
        } catch (Throwable e2) {
            aw.d(TAG, " setExact IllegalAccessException " + e2.toString(), e2);
            alarmManager.set(0, j, pendingIntent);
        } catch (Throwable e22) {
            aw.d(TAG, " setExact InvocationTargetException " + e22.toString(), e22);
            alarmManager.set(0, j, pendingIntent);
        } catch (Throwable e222) {
            aw.d(TAG, " setExact wrong " + e222.toString(), e222);
            alarmManager.set(0, j, pendingIntent);
        }
    }

    public static void a(Context context, Intent intent, long j) {
        aw.d(TAG, "enter AlarmTools:setHeartAlarm(intent:" + intent + " interval:" + j + "ms");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 134217728);
        if (VERSION.SDK_INT >= cj) {
            bq.a(context, System.currentTimeMillis() + j, broadcast);
        } else {
            alarmManager.setRepeating(0, System.currentTimeMillis() + j, j, broadcast);
        }
    }

    public static void b(Context context, Intent intent, long j) {
        aw.d(TAG, "enter AlarmTools:setDelayAlarm(intent:" + intent + " interval:" + j + "ms, context:" + context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 134217728);
        if (VERSION.SDK_INT >= cj) {
            bq.a(context, System.currentTimeMillis() + j, broadcast);
        } else {
            alarmManager.set(0, System.currentTimeMillis() + j, broadcast);
        }
    }

    public static void c(Context context, Intent intent, long j) {
        aw.d(TAG, "enter AlarmTools:setDelayNotifyService(intent:" + intent + " interval:" + j + ")");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        PendingIntent service = PendingIntent.getService(context, 0, intent, 0);
        if (VERSION.SDK_INT >= cj) {
            bq.a(context, System.currentTimeMillis() + j, service);
        } else {
            alarmManager.set(0, System.currentTimeMillis() + j, service);
        }
    }

    public static void h(Context context, Intent intent) {
        aw.d(TAG, "enter cancelAlarm(Intent=" + intent);
        ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, 0, intent, 0));
    }

    public static void w(Context context, String str) {
        aw.d(TAG, "enter cancelAlarm(Action=" + str);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        Intent intent = new Intent(str);
        intent.setPackage(context.getPackageName());
        alarmManager.cancel(PendingIntent.getBroadcast(context, 0, intent, 0));
    }
}
