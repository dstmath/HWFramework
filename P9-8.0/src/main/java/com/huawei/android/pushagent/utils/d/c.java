package com.huawei.android.pushagent.utils.d;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class c {
    private static c fp = null;
    private static String fq = "";

    private c() {
    }

    private static synchronized c sl() {
        c cVar;
        synchronized (c.class) {
            if (fp == null) {
                fp = new c();
            }
            cVar = fp;
        }
        return cVar;
    }

    public static void sn(Context context) {
        if (fp == null) {
            sl();
        }
        if (TextUtils.isEmpty(fq)) {
            String packageName = context.getPackageName();
            if (packageName != null) {
                String[] split = packageName.split("\\.");
                if (split != null && split.length > 0) {
                    fq = split[split.length - 1];
                }
            }
        }
    }

    public static void sg(String str, String str2) {
        sl().sp(3, str, str2, null, 2);
    }

    public static void si(String str, String str2, Throwable th) {
        sl().sp(3, str, str2, th, 2);
    }

    public static void sh(String str, String str2) {
        sl().sp(4, str, str2, null, 2);
    }

    public static void sj(String str, String str2) {
        sl().sp(5, str, str2, null, 2);
    }

    public static void sk(String str, String str2, Throwable th) {
        sl().sp(5, str, str2, th, 2);
    }

    public static void sf(String str, String str2) {
        sl().sp(6, str, str2, null, 2);
    }

    public static void se(String str, String str2, Throwable th) {
        sl().sp(6, str, str2, th, 2);
    }

    public static String sm(Throwable th) {
        return Log.getStackTraceString(th);
    }

    private synchronized void sp(int i, String str, String str2, Throwable th, int i2) {
        try {
            if (so(i)) {
                String str3 = "[" + Thread.currentThread().getName() + "-" + Thread.currentThread().getId() + "]" + str2;
                StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                if (stackTrace.length > i2) {
                    str3 = str3 + "(" + fq + "/" + stackTrace[i2].getFileName() + ":" + stackTrace[i2].getLineNumber() + ")";
                } else {
                    str3 = str3 + "(" + fq + "/unknown source)";
                }
                if (th != null) {
                    str3 = str3 + 10 + sm(th);
                }
                Log.println(i, str, str3);
            } else {
                return;
            }
        } catch (Throwable e) {
            Log.e("PushLog2951", "call writeLog cause:" + e.toString(), e);
        }
        return;
    }

    private static boolean so(int i) {
        try {
            return Log.isLoggable("hwpush", i);
        } catch (Exception e) {
            return false;
        }
    }
}
