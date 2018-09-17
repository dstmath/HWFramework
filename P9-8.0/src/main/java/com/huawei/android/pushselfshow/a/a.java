package com.huawei.android.pushselfshow.a;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.android.pushagent.a.a.c;
import java.util.HashMap;

public class a {
    private static final Object a = new Object();
    private static Context b;
    private static String c;
    private static String d;
    private static HashMap e = new HashMap();
    private static final HandlerThread f = new HandlerThread("push-badge-work");
    private static final Handler g = new Handler(f.getLooper());
    private static Runnable h = new b();

    static {
        f.start();
    }

    private static Bundle a(Context context, String str, String str2, String str3, int i) {
        Bundle bundle = null;
        Bundle bundle2 = new Bundle();
        bundle2.putString("basepackage", "com.huawei.android.pushagent");
        bundle2.putString("package", str2);
        bundle2.putString("class", str3);
        bundle2.putInt("badgenumber", i);
        try {
            bundle = context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), str, null, bundle2);
            c.b("PushSelfShowLog", "callLauncherMethod:" + str + " sucess");
            return bundle;
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
            return bundle;
        }
    }

    private static void a(Context context, String str, String str2) {
        b = context;
        c = str;
        d = str2;
    }

    public static synchronized void a(Context context, String str, String str2, int i) {
        synchronized (a.class) {
            c.b("PushSelfShowLog", "refresh");
            a(context, str, str2);
            try {
                a(str, i);
                g.removeCallbacks(h);
                g.postDelayed(h, 600);
            } catch (Exception e) {
                c.d("PushSelfShowLog", e.toString());
            }
        }
        return;
    }

    private static synchronized void a(String str, int i) {
        synchronized (a.class) {
            int i2 = 0;
            if (e.containsKey(str)) {
                i2 = ((Integer) e.get(str)).intValue();
            }
            c.b("PushSelfShowLog", "existnum " + i2 + ",new num " + i);
            e.put(str, Integer.valueOf(i2 + i));
        }
    }

    private static synchronized void b(String str) {
        synchronized (a.class) {
            c.b("PushSelfShowLog", "resetCachedNum " + str);
            e.remove(str);
        }
    }

    private static int d(Context context, String str, String str2, int i) {
        synchronized (a) {
            Bundle a = a(context, "getbadgeNumber", str, str2, i);
            if (a == null) {
                c.b("PushSelfShowLog", "get current exist badgenumber failed");
                return 0;
            }
            int i2 = a.getInt("badgenumber");
            c.b("PushSelfShowLog", "current exist badgenumber:" + i2);
            return i2;
        }
    }

    private static void e(Context context, String str, String str2, int i) {
        synchronized (a) {
            if (a(context, "change_badge", str, str2, i) == null) {
                c.b("PushSelfShowLog", "refreashBadgeNum failed");
            } else {
                c.b("PushSelfShowLog", "refreashBadgeNum success");
            }
        }
    }
}
