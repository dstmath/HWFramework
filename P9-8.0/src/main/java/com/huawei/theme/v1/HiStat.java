package com.huawei.theme.v1;

import android.content.Context;
import android.os.Handler;
import com.huawei.theme.a.a.a;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HiStat {
    private static int reportPeriod = 0;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean timerOn = false;

    public static void e(Context context, String str, String str2) {
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
                "BD.BDService:[" + str + ", " + str2 + "]";
            }
            a.h();
        }
    }

    public static void onPause(Context context) {
        if (context == null) {
            a.h();
            return;
        }
        Handler g = a.g();
        if (g != null) {
            g.post(new d(context, 0, System.currentTimeMillis()));
        }
        a.h();
    }

    public static void onResume(Context context) {
        if (context == null) {
            a.h();
            return;
        }
        Handler g = a.g();
        if (g != null) {
            g.post(new d(context, 1, System.currentTimeMillis()));
        }
        a.h();
    }

    public static void r(Context context) {
        if (context == null) {
            a.h();
            return;
        }
        Handler g = a.g();
        if (g != null) {
            g.post(new d(context, 2, System.currentTimeMillis()));
        }
        a.h();
    }

    public static void setEventSize(int i) {
        if (i >= 0) {
            a.a(i);
        }
    }

    public static void setMaxMsg(Long l) {
        if (l.longValue() >= 1000) {
            a.c(l);
        }
    }

    public static void setMaxSessionIDTimeOut(long j) {
        if (j >= 30) {
            a.d(Long.valueOf(60 * j));
        }
    }

    public static void setRFull(boolean z) {
        a.a(z);
    }

    public static void setRTimer(Context context, int i) {
        if (i == 0) {
            try {
                timerOn = false;
                scheduler.shutdown();
                scheduler = Executors.newScheduledThreadPool(1);
            } catch (Exception e) {
                e.getMessage();
                a.h();
                e.printStackTrace();
            }
        } else if (!timerOn) {
            timerOn = true;
            a.h();
            scheduler.scheduleAtFixedRate(new d(context, 2, System.currentTimeMillis()), (long) i, (long) i, TimeUnit.SECONDS);
        } else if (timerOn && i != reportPeriod) {
            reportPeriod = i;
            a.h();
            scheduler.shutdown();
            ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(1);
            scheduler = newScheduledThreadPool;
            newScheduledThreadPool.scheduleAtFixedRate(new d(context, 2, System.currentTimeMillis()), (long) i, (long) i, TimeUnit.SECONDS);
        }
    }

    public static void setRecordExpireTimeOut(Long l) {
        if (l.longValue() >= 24) {
            a.b(Long.valueOf((l.longValue() * 60) * 60));
        }
    }

    public static void setSessionExpireTimeOut(Long l) {
        if (l.longValue() >= 30) {
            a.a(l);
        }
    }
}
