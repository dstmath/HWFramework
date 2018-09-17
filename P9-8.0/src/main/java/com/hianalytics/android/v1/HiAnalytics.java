package com.hianalytics.android.v1;

import android.content.Context;
import android.os.Handler;
import com.hianalytics.android.a.a.a;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HiAnalytics {
    private static int reportPeriod = 0;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean timerOn = false;

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
                g.post(new d(context, 0, System.currentTimeMillis()));
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
                g.post(new d(context, 2, System.currentTimeMillis()));
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
                g.post(new d(context, 1, System.currentTimeMillis()));
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
                        newScheduledThreadPool.scheduleAtFixedRate(new d(context, 2, System.currentTimeMillis()), (long) i, (long) i, TimeUnit.SECONDS);
                        return;
                    }
                    return;
                }
                timerOn = true;
                a.h();
                scheduler.scheduleAtFixedRate(new d(context, 2, System.currentTimeMillis()), (long) i, (long) i, TimeUnit.SECONDS);
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
