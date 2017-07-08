package com.hianalytics.android.v1;

import android.content.Context;
import android.os.Handler;
import com.hianalytics.android.a.a.a;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public class HiAnalytics {
    private static int reportPeriod;
    private static ScheduledExecutorService scheduler;
    private static boolean timerOn;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.hianalytics.android.v1.HiAnalytics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.hianalytics.android.v1.HiAnalytics.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.hianalytics.android.v1.HiAnalytics.<clinit>():void");
    }

    public static void onEvent(Context context, String str, String str2) {
        if (context == null) {
            a.h();
        } else if (str == null || str.equals("")) {
            a.h();
        } else if (str2 == null || str2.equals("")) {
            a.h();
        } else {
            Handler g = a.g();
            if (g != null) {
                g.post(new a(context, str, str2, System.currentTimeMillis()));
                new StringBuilder(String.valueOf(context.getClass().getName())).append(" onEvent!").toString();
            }
            a.h();
        }
    }

    public static void onPause(Context context) {
        if (context != null) {
            Handler g = a.g();
            if (g != null) {
                g.post(new e(context, 0, System.currentTimeMillis()));
                new StringBuilder(String.valueOf(context.getClass().getName())).append(" onPause() !").toString();
            }
            a.h();
            return;
        }
        a.h();
    }

    public static void onReport(Context context) {
        if (context != null) {
            Handler g = a.g();
            if (g != null) {
                g.post(new e(context, 2, System.currentTimeMillis()));
                new StringBuilder(String.valueOf(context.getClass().getName())).append(" onReport !").toString();
            }
            a.h();
            return;
        }
        a.h();
    }

    public static void onResume(Context context) {
        if (context != null) {
            Handler g = a.g();
            if (g != null) {
                g.post(new e(context, 1, System.currentTimeMillis()));
                new StringBuilder(String.valueOf(context.getClass().getName())).append(" onResume() !").toString();
            }
            a.h();
            return;
        }
        a.h();
    }

    public static void setEventSize(int i) {
        if (i >= 0) {
            a.a(i);
        }
    }

    public static void setMaxMsg(Long l) {
        if ((l.longValue() < 1000 ? 1 : null) == null) {
            a.c(l);
        }
    }

    public static void setMaxSessionIDTimeOut(long j) {
        if ((j < 30 ? 1 : null) == null) {
            a.d(Long.valueOf(60 * j));
        }
    }

    public static void setRecordExpireTimeOut(Long l) {
        if ((l.longValue() < 24 ? 1 : null) == null) {
            a.b(Long.valueOf((l.longValue() * 60) * 60));
        }
    }

    public static void setReportTimer(Context context, int i) {
        if (i != 0) {
            try {
                if (timerOn) {
                    if (timerOn && i != reportPeriod) {
                        reportPeriod = i;
                        a.h();
                        scheduler.shutdown();
                        ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(1);
                        scheduler = newScheduledThreadPool;
                        newScheduledThreadPool.scheduleAtFixedRate(new e(context, 2, System.currentTimeMillis()), (long) i, (long) i, TimeUnit.SECONDS);
                        return;
                    }
                    return;
                }
                timerOn = true;
                a.h();
                scheduler.scheduleAtFixedRate(new e(context, 2, System.currentTimeMillis()), (long) i, (long) i, TimeUnit.SECONDS);
            } catch (Exception e) {
                "setReportTimer() throw exception:" + e.getMessage();
                a.h();
                e.printStackTrace();
            }
        } else {
            timerOn = false;
            scheduler.shutdown();
            scheduler = Executors.newScheduledThreadPool(1);
        }
    }

    public static void setReportWhenFull(boolean z) {
        a.a(z);
    }

    public static void setSessionExpireTimeOut(Long l) {
        if ((l.longValue() < 30 ? 1 : null) == null) {
            a.a(l);
        }
    }
}
